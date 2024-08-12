package td.android.voyageevasion;

import static td.android.voyageevasion.Accueil.LesVilles;
import static td.android.voyageevasion.Accueil.date;
import static td.android.voyageevasion.Accueil.fichierCharged;
import static td.android.voyageevasion.Accueil.nombreGroupe;

import android.annotation.SuppressLint;
import android.app.AlertDialog;

import android.os.Bundle;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;


public class Details extends Fragment {

    // INDICES VOYAGE
    public static final int IND_VOYAGE_NOM = 1;
    public static final int IND_VOYAGE_TRANSP_ALLER = 2;
    public static final int IND_VOYAGE_TRANSP_RETOUR = 3;
    public static final int IND_VOYAGE_VILLE_ALLER = 4;
    public static final int IND_VOYAGE_VILLE_RETOUR = 5;

    // INDICES PAYS
    public static final int IND_PAYS_NOM = 0;
    public static final int IND_PAYS_DETAILS = 1;
    public static final int IND_PAYS_CONSEILS = 2;

    // INDICES HOTELS
    public static final int IND_HOTEL_NOM = 0;
    public static final int IND_HOTEL_ADRESSE = 1;
    public static final int IND_HOTEL_VILLE = 2;
    public static final int IND_HOTEL_NUMERO = 3;

    // INDICES VISITES
    public static final int IND_VISITE_NOM = 0;
    public static final int IND_VISITE_DESC = 1;

    // INDICES DATES ET TARIFS
    public static final int IND_DATE_DEPART = 1;
    public static final int IND_DATE_RETOUR = 2;

    /* Logger, permettant de garder une trace des actions sur l'application */
    private static final Logger logger = Logger.getLogger(Details.class.getName());

    StringBuilder listePays = new StringBuilder();
    StringBuilder listeHotel = new StringBuilder();
    StringBuilder listeVisite = new StringBuilder();
    StringBuilder listeDate = new StringBuilder();
    StringBuilder listeTarif = new StringBuilder();

    public static String affichageVilleTarif;

    public Details() {
        // Generated stub
    }

    @SuppressLint("SetTextI18n")
    @Override
    public View onCreateView(LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_details, container, false);

        ScrollView pageDetails = (ScrollView) rootView.findViewById(R.id.pageDetails);
        ImageView info = rootView.findViewById(R.id.info);
        CardView cvHotel = rootView.findViewById(R.id.cvHotel);
        CardView cvVisite = rootView.findViewById(R.id.cvVisite);
        TextView tvNomVoyage = rootView.findViewById(R.id.tvNomVoyage);
        LinearLayout llPays = rootView.findViewById(R.id.llPays);
        LinearLayout llVisite = rootView.findViewById(R.id.llVisite);
        LinearLayout llHotel = rootView.findViewById(R.id.llHotel);
        TextView tvTransportAller = rootView.findViewById(R.id.tvTransportAller);
        TextView tvTransportRetour = rootView.findViewById(R.id.tvTransportRetour);
        TextView tvDate = rootView.findViewById(R.id.tvDate);
        TextView tvTarif = rootView.findViewById(R.id.tvTarif);

        // Popup d'aide
        info.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.info_titre)
                        .setMessage(R.string.info_message)
                        .setNeutralButton(R.string.fermer, null)
                        .show();
                logger.log(Level.INFO,"Clique sur la popup d'aide");
            }
        });

        if (fichierCharged) {
            // Affiche le nom du voyage
            tvNomVoyage.setText(Accueil.voyage[IND_VOYAGE_NOM]);

            // Affiche la date de départ et de retour du voyage
            String dateDep = MesVoyages.dateFormatFr(date[IND_DATE_DEPART]);
            String dateRet = MesVoyages.dateFormatFr(date[IND_DATE_RETOUR]);
            tvDate.setText(getString(R.string.listeDate, dateDep, dateRet));

            // Affiche tous les pays
            affichagePays(llPays);

            // Affiche le moyen de transport pour l'aller et le retour
            tvTransportAller.setText(Accueil.voyage[IND_VOYAGE_TRANSP_ALLER]);
            tvTransportRetour.setText(Accueil.voyage[IND_VOYAGE_TRANSP_RETOUR]);

            // Affiche tous les hôtels
            affichageHotels(llHotel);
            if (Accueil.nombreHotel == 0) {
                cvHotel.setVisibility(View.GONE);
            }

            // Affiche toutes les visites
            affichageVisites(llVisite);
            if (Accueil.nombreVisite == 0) {
                cvVisite.setVisibility(View.GONE);
            }

            // OPERATIONS SUR LES DATES
            listeDate.setLength(0);
            listeTarif.setLength(0);
            for (int j = 3; j < date.length - 1; j++) {
                listeDate.append(date[j]);
                if (isInteger(date[j]) && !date[j].equals("-1")) {
                    listeTarif.append(date[j]).append(" €, ");
                }
            }
            listeTarif.setLength(listeTarif.length() - 2);

            // associations groupes de villes / tarifs
            affichageVilleTarifs();

            tvTarif.setText(affichageVilleTarif);

        } else {
            // Si aucun voyage n'est chargé, on cache ces éléments
            pageDetails.setVisibility(View.GONE);
            tvNomVoyage.setVisibility(View.GONE);
            tvDate.setVisibility(View.GONE);
        }

        return rootView;
    }

    public static boolean isInteger(String str) {
        boolean isInteger = false;
        try {
            Integer.parseInt(str);
            isInteger = true;
        } catch (NumberFormatException e) {
            logger.log(Level.SEVERE, "isInteger: " + e.getMessage());
        }
        return isInteger;
    }

    /**
     * Ajoute une espace tous les deux caractères pour l'affichage des numéros
     * de téléphone.
     * @param num
     * @return result   le numéro formatté
     */
    public static String numTelFormat(String num) {
        String result = num.replaceAll("(.{" + 2 + "})", "$1 ").trim();
        return result;
    }

    /**
     * Ajout un TextView par pays au LinearLayout passé en argument
     * Lors du clique sur le pays, une popup affiche les détails et
     * conseils concernant le pays.
     * @param llPays
     */
    public void affichagePays(LinearLayout llPays) {
        // OPERATIONS LISTE DES PAYS
        listePays.setLength(0);
        for (int i = 0, j = 0; j < Accueil.nombrePays; i+=3, j++) {
            TextView text = new TextView(new ContextThemeWrapper(getContext(), R.style.details_texte));
            llPays.addView(text);
            text.setText(getString(R.string.liste_element, Accueil.pays[IND_PAYS_NOM + i]));

            // Lorsque l'on clique sur le TextView
            int ind = i;
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(Accueil.pays[IND_PAYS_NOM + ind])
                            .setMessage("Details :\n️  " + Accueil.pays[IND_PAYS_DETAILS + ind]
                                    + "\n\nConseils :\n  " + Accueil.pays[IND_PAYS_CONSEILS + ind])
                            .setNeutralButton(R.string.fermer, null)
                            .show();
                    logger.log(Level.INFO,"Clique sur un pays");
                }
            });
        }
    }

    /**
     * Ajout un TextView par hôtel au LinearLayout passé en argument
     * Lors du clique sur l'hôtel, une popup affiche la ville,
     * l'adresse et le numéro de téléphone de l'hôtel.
     * @param llHotel
     */
    public void  affichageHotels(LinearLayout llHotel) {
        // OPERATIONS LISTE DES HOTELS
        listeHotel.setLength(0);
        // Crée un TextView par hôtel
        for (int i = 0, j = 0; j < Accueil.nombreHotel; i+=4, j++) {
            TextView text = new TextView(new ContextThemeWrapper(getContext(), R.style.details_texte));
            llHotel.addView(text);  // Ajoute le TextView au LinearLayout
            text.setText(getString(R.string.liste_element, Accueil.hotel[IND_HOTEL_NOM + i]));

            // Lorsque l'on clique sur le TextView, une popup affiche les détails de l'hôtel
            int ind = i;
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(Accueil.hotel[IND_HOTEL_NOM + ind])
                            .setMessage("\uD83C\uDFD9️  " + Accueil.hotel[IND_HOTEL_VILLE + ind]
                                    + "\n\uD83D\uDCCD  " + Accueil.hotel[IND_HOTEL_ADRESSE + ind]
                                    + "\n☎️  " + numTelFormat(Accueil.hotel[IND_HOTEL_NUMERO + ind]))
                            .setNeutralButton(R.string.fermer, null)
                            .show();
                    logger.log(Level.INFO,"Clique sur un hotel");
                }
            });
        }
    }

    /**
     * Ajout un TextView par visite au LinearLayout passé en argument
     * Lors du clique sur la visite, une popup en affiche la descriptipn.
     * @param llVisite
     */
    public void affichageVisites(LinearLayout llVisite) {
        // OPERATIONS LISTE DES VISITES
        listeVisite.setLength(0);
        for (int i = 0, j = 0; j < Accueil.nombreVisite; i+=2, j++) {
            TextView text = new TextView(new ContextThemeWrapper(getContext(), R.style.details_texte));
            llVisite.addView(text);
            text.setText(getString(R.string.liste_element, Accueil.visite[IND_VISITE_NOM + i]));

            // Lorsque l'on clique sur le TextView
            int ind = i;
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                            .setTitle(Accueil.visite[IND_VISITE_NOM + ind])
                            .setMessage(Accueil.visite[IND_VISITE_DESC + ind])
                            .setNeutralButton(R.string.fermer, null)
                            .show();
                    logger.log(Level.INFO,"Clique sur une visite");
                }
            });
        }
    }

    /**
     * Ajoute dans un TextView les villes suivies de leur tarif associé
     * Format :
     * Rodez, Montpellier : 378 €
     * Lyon : 895 €
     */
    public void affichageVilleTarifs() {
        StringBuilder affichageVilles = new StringBuilder();
        ArrayList<String> tarifs = new ArrayList<>(Arrays.asList(date));
        tarifs.remove(0); // on supprimer l'id qui est inutile
        tarifs.remove(tarifs.size()-1);
        tarifs.removeAll(Collections.singleton("-1"));
        tarifs.remove(0); // on supprime la date de départ
        tarifs.remove(0); // on supprime la date de retour

        int buff = 0;
        HashMap<Integer, ArrayList<String>> hm = new HashMap<>();
        for (int i = 0, k = 0; k < tarifs.size(); k++) {
            ArrayList<String> arrayList = new ArrayList<>();
            if (Character.isDigit(LesVilles.get(i).charAt(0))) {
                buff = Integer.parseInt(LesVilles.get(i));
                i++;
                if (i < LesVilles.size()) {
                    do {
                        arrayList.add(LesVilles.get(i));
                        i++;
                    } while (i < LesVilles.size() && !Character.isDigit(LesVilles.get(i).charAt(0)));
                }
            }
            hm.put(buff, arrayList);
        }

        for (int i = 1; i < nombreGroupe + 1; i++) {
            affichageVilles.append(hm.get(i)).append(" : ").append(tarifs.get(i - 1)).append(" €\n");
        }

        // supprime les [ et ] de l'affichage, ainsi que le dernier retour à la ligne
        affichageVilleTarif = affichageVilles.toString().replace("[", "").replace("]", "");
        affichageVilleTarif = affichageVilleTarif.substring(0, affichageVilleTarif.toString().length() - 1);
        logger.log(Level.INFO,"Les tarifs :\n" + affichageVilleTarif);
    }
}


