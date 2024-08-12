package td.android.voyageevasion;

import static td.android.voyageevasion.Accueil.fichierCharged;
import static td.android.voyageevasion.Accueil.infosHotel;
import static td.android.voyageevasion.Accueil.infosPays;
import static td.android.voyageevasion.Accueil.infosVille;
import static td.android.voyageevasion.Accueil.infosVisite;
import static td.android.voyageevasion.Accueil.nombrePays;
import static td.android.voyageevasion.Accueil.nombreHotel;
import static td.android.voyageevasion.Accueil.nombreVisite;
import static td.android.voyageevasion.Accueil.*;
import static td.android.voyageevasion.Details.*;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

public class MesVoyages extends Fragment {

    public MesVoyages() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_mes_voyages, container, false);

        TextView tvNom = rootView.findViewById(R.id.mes_voyages);
        TextView tvAucunVoyage = rootView.findViewById(R.id.tvAucunVoyage);
        TextView tvVoyageOrganise = rootView.findViewById(R.id.tvVoyageOrganise);
        CardView card = rootView.findViewById(R.id.card);
        TextView tvVoyageDate = rootView.findViewById(R.id.tvVoyageDate);
        TextView tvVilleAller = rootView.findViewById(R.id.tvVilleAller);
        TextView tvVilleRetour = rootView.findViewById(R.id.tvVilleRetour);
        TextView tvTranspAller = rootView.findViewById(R.id.tvTranspAller);
        TextView tvTranspRetour = rootView.findViewById(R.id.tvTranspRetour);
        TextView tvNbPays = rootView.findViewById(R.id.tvNbPays);
        TextView tvNbHotel = rootView.findViewById(R.id.tvNbHotel);
        TextView tvNbVisite = rootView.findViewById(R.id.tvNbVisite);
        Button btnDelete = (Button) rootView.findViewById(R.id.btnDelete);
        Button btnAfficherDetails = (Button) rootView.findViewById(R.id.btnDetails);

        if (Accueil.fichierCharged) {
            // N'affiche pas le message aucun voyage
            tvVoyageOrganise.setVisibility(View.GONE);
            tvNbHotel.setVisibility(View.GONE);
            tvNbVisite.setVisibility(View.GONE);
            tvAucunVoyage.setVisibility(View.GONE);
            tvNom.setText(Accueil.voyage[IND_VOYAGE_NOM]);
            tvVoyageDate.setText(getString(R.string.voyageDate, dateFormatFr(Accueil.date[IND_DATE_DEPART]), dateFormatFr(Accueil.date[IND_DATE_RETOUR])));

            tvVilleAller.setText(Accueil.voyage[IND_VOYAGE_VILLE_ALLER]);
            tvVilleRetour.setText(Accueil.voyage[IND_VOYAGE_VILLE_RETOUR]);
            tvTranspAller.setText(getString(R.string.transportAller, Accueil.voyage[IND_VOYAGE_TRANSP_ALLER]));
            tvTranspRetour.setText(getString(R.string.transportRetour, Accueil.voyage[IND_VOYAGE_TRANSP_RETOUR]));
            tvNbPays.setText(getString(R.string.nbPays, nombrePays));

            // Affiche le nombre d'hôtel ou de visite si le nombre est > 0
            if (Accueil.nombreHotel > 0) {
                tvNbHotel.setVisibility(View.VISIBLE);
                tvVoyageOrganise.setVisibility(View.VISIBLE);
                tvNbHotel.setText(getString(R.string.nbHotel, nombreHotel));
            }
            if (Accueil.nombreVisite > 0) {
                tvNbVisite.setVisibility(View.VISIBLE);
                tvVoyageOrganise.setVisibility(View.VISIBLE);
                tvNbVisite.setText(getString(R.string.nbVisite, nombreVisite));
            }
        } else {
            card.setVisibility(View.GONE);
            tvAucunVoyage.setVisibility(View.VISIBLE);
        }

        btnDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(getContext())
                        .setTitle(R.string.attention_titre)
                        .setMessage(getString(R.string.attention_message, Accueil.voyage[IND_VOYAGE_NOM]))
                        .setNegativeButton(R.string.annuler, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                        .setPositiveButton(R.string.supprimer, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                fichierCharged = false;
                                SharedPreferences.Editor editeur = Accueil.enregistreur.edit();
                                editeur.clear();
                                editeur.remove("fichierJSON");
                                editeur.apply();
                                card.setVisibility(View.GONE);
                                tvAucunVoyage.setVisibility(View.VISIBLE);
                                Toast.makeText(getContext(), R.string.voyage_supprime, Toast.LENGTH_LONG).show();
                            }
                        }).show();
            }
        });

        btnAfficherDetails.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FragmentTransaction ft = getActivity().getSupportFragmentManager().beginTransaction();
                ft.replace(getId(), new Details());
                ft.commit();
            }
        });

        return rootView;
    }

    /**
     * Passe une date en String du format aaaa-mm-jj au format
     * jj mois
     * @param date
     * @return le string de la date formattée
     */
    @SuppressLint("SimpleDateFormat")
    public static String dateFormatFr(String date) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM");

        String[] dateTab = date.split("-");
        int jour = Integer.parseInt(dateTab[2]);
        int mois = Integer.parseInt(dateTab[1]) -1; // -1 car les mois commencent à 0
        int annee = Integer.parseInt(dateTab[0]);
        Calendar calendar = new GregorianCalendar(annee, mois , jour);

        Date dateCalendar = calendar.getTime();

        return dateFormat.format(dateCalendar);
    }
}