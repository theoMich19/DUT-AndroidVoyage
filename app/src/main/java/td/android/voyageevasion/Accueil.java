package td.android.voyageevasion;


import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.fragment.app.Fragment;

import java.io.InputStream;
import java.lang.*;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import org.json.JSONObject;

import java.io.IOException;

import java.io.BufferedReader;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Arrays;

import org.json.*;

public class Accueil extends Fragment implements View.OnClickListener {

    JSONObject json;
    public static SharedPreferences enregistreur;

    public static JSONArray paysArray;
    public static JSONArray visiteArray;
    public static JSONArray hotelArray;
    public static JSONArray villeArray;

    public static ArrayList<String> villesGrp;

    /* Strings pour la mise en forme et affichage des informations */
    public static StringBuilder infosPays = new StringBuilder();
    public static StringBuilder infosVisite = new StringBuilder();
    public static StringBuilder infosHotel = new StringBuilder();
    public static StringBuilder infosVille = new StringBuilder();

    /* Contenu du fichier sous forme de chaine */
    private String fileContents;

    /* Tableaux de stockage des informations du voyage */
    public static String[] voyage;
    public static String[] pays;
    public static String[] visite;
    public static String[] hotel;
    public static String[] date;
    public static String[] ville;

    /* Liste des villes récupérées | Réutilisation pour l'association avec les tarifs */
    public static ArrayList<String> LesVilles;

    public static boolean fichierCharged = false;

    /* Variables de nombres des éléments du voyage */
    public static int nombrePays;
    public static int nombreHotel;
    public static int nombreVisite;
    public static int nombreVille;
    public static int nombreGroupe;

    private ActivityResultLauncher<Intent> getFile;

    public Accueil() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_accueil, container, false);

        /* Persistance des données : récupération des données sauvegardées dans le fichier json */
        enregistreur = requireContext().getSharedPreferences("fichierJSON", Context.MODE_PRIVATE);
        fileContents = enregistreur.getString("fichierJSON","0");

        Button button = (Button) rootView.findViewById(R.id.btnCharger);

        /* Vérification des données récupérées des préférences */
        if (fileContents != null) {
            try {
                getJsonContent(fileContents);
            } catch (Exception ignored) {
            }
        }


        /* Méthode permettant d'aller chercher le fichier dans le file manager */
        getFile = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                new ActivityResultCallback<ActivityResult>() {
                    @Override
                    public void onActivityResult(ActivityResult result) {
                        Uri uri = null;
                        Intent data = result.getData();
                        /* Arrête l'exécution si l'utilisateur a décidé de ne rien choisir */
                        if (data != null) {
                            uri = data.getData();
                        } else {
                            return;
                        }
                        /* Lecture du fichier importé */
                        readTextFile(uri);
                    }
                });

        /* Ouverture de l'explorateur de fichiers */
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (fichierCharged) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(R.string.attention_titre)
                            .setMessage(R.string.msgVoyageDejaCharge)
                            .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.cancel();
                                }
                            })
                            .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    readFile(view);
                                }
                            }).show();
                } else {
                    readFile(view);
                }

            }
        });

        // Inflate the layout for this fragment
        return rootView;
    }

    /**
     * Création de l'intent pour lecture du fichier
     * @param view required
     */
    public void readFile(View view) {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        String [] mimeTypes = {"application/json", "application/octet-stream"};
        intent.setType("*/*");
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        getFile.launch(intent);
    }

    /**
     * Lecture du fichier
     * @param uri required
     */
    private void readTextFile(Uri uri) {
        StringBuilder sb = new StringBuilder();
        InputStream inputStream = null;

        try {
            inputStream = getContext().getContentResolver().openInputStream(uri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            reader.close();
            inputStream.close();

            fileContents = sb.toString();
            if (getJsonContent(fileContents)) {
                Toast.makeText(getContext(), "Voyage chargé", Toast.LENGTH_LONG).show();
            } else {
                Toast.makeText(getContext(), "Le fichier ne peut pas être lu", Toast.LENGTH_LONG).show();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Récupération des données du fichier json
     * @param fileContents contenu du fichier récupéré
     * @return true si la récupération s'est bien passée
     */
    private boolean getJsonContent(String fileContents) {
        boolean hasWorked = true;
        /* Récupération des éléments du fichier Json */
        try {
            json = new JSONObject(fileContents);
            gestionVoyage();
            gestionTarifs();
            gestionPays();
            gestionVilles();
            gestionHotels();
            gestionVisites();
            fichierCharged = true;
        } catch (JSONException e) {
            hasWorked = false;
            e.printStackTrace();
        }
        return hasWorked;
    }

    @Override
    /*
     * Fermeture de l'application
     * Sauvegarde des données du voyage dernièrement importé
     */
    public void onStop() {
        super.onStop();
        try {
            if (fichierCharged) {
                SharedPreferences.Editor editeur = enregistreur.edit();
                editeur.putString("fichierJSON", json.toString());
                editeur.apply();
            }
        } catch (Exception ignored) {
        }
    }

    /**
     * Gestion des voyages
     * @throws JSONException si voyages incorrects
     */
    public void gestionVoyage() throws JSONException {
        String voyageInfo = json.getString("voyage");
        voyage = voyageInfo.split("[\\s][|][\\s]");
    }

    /**
     * Gestion des tarifs
     * @throws JSONException si tarifs incorrects
     */
    public void gestionTarifs() throws JSONException {
        String dateInfo = json.getString("tarif");
        date = dateInfo.split("[\\s][|][\\s]");
    }

    /**
     * Gestion des pays
     * @throws JSONException si pays incorrects
     */
    public void gestionPays() throws JSONException {
        paysArray = json.getJSONArray("listePays");
        infosPays.setLength(0);
        String paysInfo = "";
        for (int i = 0; i < paysArray.length(); i++) {
            JSONObject paysJSON = paysArray.getJSONObject(i);
            paysInfo = paysJSON.getString("pays");
            paysInfo = paysInfo.substring(paysInfo.indexOf("|") + 2); // On supprime l'id
            infosPays.append(paysInfo).append(" | ");
            paysInfo = infosPays.toString();
        }
        pays = paysInfo.split("[\\s][|][\\s]");
        nombrePays = pays.length / 3;
    }

    /**
     * Gestion des villes
     * @throws JSONException si villes incorrectes
     */
    public void gestionVilles() throws JSONException {
        villeArray = json.getJSONArray("listeVilles");
        infosVille.setLength(0);
        LesVilles = new ArrayList<>();
        for (int i = 0, k = 0; i < villeArray.length(); i++) {
            try {
                JSONObject villeJSON = villeArray.getJSONObject(i);
                String villeInfo = villeJSON.getString("villes");
                ville = villeInfo.split("[\\s][|][\\s]");
                villesGrp = new ArrayList<>(Arrays.asList(ville));
                for (int j = 0; j < villesGrp.size(); j++) {
                    if (!Character.isDigit(villesGrp.get(j).charAt(0))) {
                        LesVilles.add(villesGrp.get(j));
                    }
                }
            } catch (Exception e){
                LesVilles.add(String.valueOf(k+1));
                k++;
            }
            nombreGroupe = k;
        }

        nombreVille = LesVilles.size() - nombreGroupe;
    }

    /**
     * Gestion des hôtels
     * @throws JSONException si hôtels incorrects
     */
    public void gestionHotels() throws JSONException {
        hotelArray = json.getJSONArray("listeHotels");
        infosHotel.setLength(0);
        String hotelInfo = "";
        for (int i = 0; i < hotelArray.length(); i++) {
            JSONObject hotelJSON = hotelArray.getJSONObject(i);
            hotelInfo = hotelJSON.getString("hotel");
            hotelInfo = hotelInfo.substring(hotelInfo.indexOf("|") + 2); // On supprime l'id
            infosHotel.append(hotelInfo).append(" | ");
            hotelInfo = infosHotel.toString();
        }
        hotel = hotelInfo.split("[\\s][|][\\s]");
        try {
            if (hotel.length != 0) {
                nombreHotel = hotel.length / 4;
            } else {
                nombreHotel = 0;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /**
     * Gestion des visites
     * @throws JSONException si visites incorrectes
     */
    public void gestionVisites() throws JSONException {
        visiteArray = json.getJSONArray("listeVisites");
        infosVisite.setLength(0);
        String visitesInfo = "";
        for (int i = 0; i < visiteArray.length(); i++) {
            JSONObject visiteJSON = visiteArray.getJSONObject(i);
            visitesInfo = visiteJSON.getString("visite");
            visitesInfo = visitesInfo.substring(visitesInfo.indexOf("|") + 2); // On supprime l'id
            infosVisite.append(visitesInfo).append(" | ");
            visitesInfo = infosVisite.toString();

        }
        visite = visitesInfo.split("[\\s][|][\\s]");

        try {
            if (visite.length != 0) {
                nombreVisite = visite.length / 2;
            } else {
                nombreVisite = 0;
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {

    }
}