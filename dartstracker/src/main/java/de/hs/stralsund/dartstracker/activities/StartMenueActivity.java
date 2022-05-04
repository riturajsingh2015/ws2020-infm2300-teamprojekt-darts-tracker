package de.hs.stralsund.dartstracker.activities;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;

import com.jakewharton.rxbinding4.view.RxView;
import com.tbruyelle.rxpermissions3.RxPermissions;

import org.opencv.android.OpenCVLoader;

import de.hs.stralsund.dartstracker.dartgame.GameInformation;
import de.hs.stralsund.dartstracker.utils.DataManager;

public class StartMenueActivity extends AppCompatActivity {
    private static final String TAG = "StartMenueActivity";

    // load native libs as early as possible
    static {
        if (OpenCVLoader.initDebug()) {
            // Load native library after(!) OpenCV initialization
            System.loadLibrary("native-lib");
            Log.i(CameraCalibrationActivity.class.getSimpleName(), "  OpenCVLoader.initDebug(), working.");
        }
        else {
            Log.e(CameraCalibrationActivity.class.getSimpleName(), "  OpenCVLoader.initDebug(), not working.");
        }
    }

    private CardView cardStartGame;
    private RxPermissions rxPermissions;

    public static DataManager dataManager;


    /*
    Temporary comment added to this file
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_menue);

        cardStartGame = findViewById(R.id.cardStartGame);
        rxPermissions = new RxPermissions(this);
        dataManager = new DataManager(this);

        // create camera permission listener on calibrateCamera-Buttom
        RxView.clicks(findViewById(R.id.gameSettings))
                .compose(rxPermissions.ensureEachCombined(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .subscribe(permissions -> {
                            if (permissions.granted) {
                                // R.id.enableCamera has been clicked
                                Intent intent = new Intent(this, PlayerSettingsActivity.class);
                                startActivity(intent);
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "You need to allow permissions to use this feature.", Toast.LENGTH_SHORT).show();
                            }
                        }
                );

        activateStartButton();
    }

    private void activateStartButton() {

        TextView gameSettingsAdvice = findViewById(R.id.gameSettingsAdvice);
        if (getGameInformation().getPlayerList().isEmpty()) {
            cardStartGame.setEnabled(false);
            gameSettingsAdvice.setVisibility(View.VISIBLE);

        }
        else {
            cardStartGame.setEnabled(true);
            gameSettingsAdvice.setVisibility(View.INVISIBLE);
            RxView.clicks(cardStartGame)
                    .compose(rxPermissions.ensureEachCombined(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                    .subscribe(permission -> {
                        if (permission.granted) {
                            //Spiel starten
                            Intent intent = new Intent(this, CameraCalibrationActivity.class);
                            startActivity(intent);
                        }
                    });
        }
    }

    private GameInformation getGameInformation() {

        GameInformation gameInformation = dataManager.getGameinformationFromFile();
        if(gameInformation == null){
            gameInformation = new GameInformation();
            dataManager.writeData(gameInformation);
        }

        return gameInformation;
    }

    @Override
    protected void onResume() {

        super.onResume();
        activateStartButton();
    }
}