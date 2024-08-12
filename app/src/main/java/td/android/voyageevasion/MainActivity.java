package td.android.voyageevasion;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;


import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;

import android.view.MenuItem;


import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity
        implements BottomNavigationView.OnItemSelectedListener {

    BottomNavigationView bottomNavigationView;
    private final int STORAGE_PERMISSION_CODE = 101;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setOnItemSelectedListener(this);
        bottomNavigationView.setSelectedItemId(R.id.accueil);
        /* Gestion des permissions par l'utilisateur */
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                new AlertDialog.Builder(this).setTitle(R.string.titrePermStockage).setMessage(R.string.permStockage)
                        .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                try {
                                    ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{
                                            Manifest.permission.READ_EXTERNAL_STORAGE
                                    }, STORAGE_PERMISSION_CODE);
                                } catch (Exception e) {
                                    e.printStackTrace();
                                }
                            }
                        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                dialogInterface.dismiss();
                            }
                        }).create().show();
            } else {
                ActivityCompat.requestPermissions((Activity) this, new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, STORAGE_PERMISSION_CODE);
            }
        }
    }

    Accueil accueilFragment = new Accueil();
    MesVoyages mesVoyagesFragment = new MesVoyages();
    Details detailsFragment = new Details();

    @SuppressLint("NonConstantResourceId")
    @Override
    /* Gestion de la navigation du menu */
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        boolean result = false;

        switch (item.getItemId()) {
            case R.id.accueil:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, accueilFragment).commit();
                result = true;
                break;

            case R.id.mes_voyages:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, mesVoyagesFragment).commit();
                result = true;
                break;

            case R.id.details:
                getSupportFragmentManager().beginTransaction().replace(R.id.frame_layout, detailsFragment).commit();
                result = true;
                break;
        }
        return result;
    }
}