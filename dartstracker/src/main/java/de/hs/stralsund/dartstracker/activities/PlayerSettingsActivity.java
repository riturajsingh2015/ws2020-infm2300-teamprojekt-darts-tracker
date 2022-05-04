package de.hs.stralsund.dartstracker.activities;

import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.jakewharton.rxbinding4.view.RxView;

import de.hs.stralsund.dartstracker.activities.dialogs.PlayerDeleteDialog;
import de.hs.stralsund.dartstracker.activities.dialogs.PlayerNameDialog;
import de.hs.stralsund.dartstracker.dartgame.GameInformation;
import de.hs.stralsund.dartstracker.dartgame.Player;

public class PlayerSettingsActivity extends AppCompatActivity {

    private PopupWindow popUpName;
    private TextView playerTextView;
    private TextView deletePlayerTextView;
    private int currentPlayerID = 0;
    private GameInformation gameInformation;
    private int deletePlayerID = 0;


    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_player_settings);
        popUpName = new PopupWindow();
        gameInformation = StartMenueActivity.dataManager.getGameinformationFromFile();

        showActualSettings();

        RxView.clicks(findViewById(R.id.settingsPlayerPlus))
                .subscribe(playerPlus -> {
                    openDialog();
                    gameInformation.setIsGameSetting(true);
                });

        RxView.clicks(findViewById(R.id.settingsDeletePlayer))
                .subscribe(playerMinus -> {
                    openDeletePlayerDialog();
                    gameInformation.setIsGameSetting(true);
                });

        RxView.clicks(findViewById(R.id.settingsPoints)).subscribe(settingsPoint -> {
            changePoints();
            StartMenueActivity.dataManager.writeData(gameInformation);
        });

    }

    //Textview wird nicht ans Ende gesetzt.
    public void createPlayersCardView(Player player) {
        playerTextView = new TextView(this);
        playerTextView.setText(player.getPlayerName());
        playerTextView.setTextColor(Color.parseColor("#4CAF50"));
        playerTextView.setTextSize(16);
        playerTextView.setId(player.getPlayerID());
        LinearLayout linearLayout = findViewById(R.id.cardViewLayout);
        linearLayout.addView(playerTextView);
    }

    public void openDeletePlayerDialog(){
        PlayerDeleteDialog playerDeleteDialog = new PlayerDeleteDialog();
        playerDeleteDialog.show(getSupportFragmentManager(), "Player Delete Dialog");
    }

    public GameInformation getGameInformation(){
        return this.gameInformation;
    }

    public void openDialog() {
        PlayerNameDialog playerNameDialog = new PlayerNameDialog();
        playerNameDialog.show(getSupportFragmentManager(), "Playername Dialog");
    }

    public void showCurrentPoints(){
        TextView points = findViewById(R.id.settingsPointsText);
        points.setText(String.valueOf(gameInformation.getPointsInLeg()));
    }

    public void changePoints(){
        TextView points = findViewById(R.id.settingsPointsText);
        if (gameInformation.getPointsInLeg() == 501) {
            gameInformation.setPointsInLeg(301);
            points.setText(String.valueOf(301));
        }
        else if (gameInformation.getPointsInLeg() == 301) {
            gameInformation.setPointsInLeg(501);
            points.setText(String.valueOf(501));
        }

        resetPlayerProgress();
        StartMenueActivity.dataManager.writeData(gameInformation);
    }

    private void resetPlayerProgress() {

        int id = 0;
        for (Player player : gameInformation.getPlayerList()) {
            player.setStartPointsInLeg(gameInformation.getPointsInLeg());
            player.setPlayerID(id++);
        }
    }

    public void showActualSettings(){
        if(this.gameInformation.getPlayerList().size() >= 1){
            for(Player player : this.gameInformation.getPlayerList()){
                this.createPlayersCardView(player);
                this.showCurrentPoints();
            }
        }
    }

    public void deletePlayer(String deletePlayerName){
        for(Player player : this.gameInformation.getPlayerList()){
            if(player.getPlayerName().equals(deletePlayerName)){
                this.gameInformation.getPlayerList().remove(player);
                StartMenueActivity.dataManager.writeData(gameInformation);
                LinearLayout linearLayout = findViewById(R.id.cardViewLayout);
                linearLayout.removeView(findViewById(player.getPlayerID()));
                break;
            }
        }
    }

    public void addPlayer(String playerName) {

        Player player = new Player();
        player.setPlayerName(playerName);
        player.setPlayerID(this.currentPlayerID);

        createPlayersCardView(player);

        this.currentPlayerID++;
        gameInformation.setPlayerList(player);
        resetPlayerProgress();

        StartMenueActivity.dataManager.writeData(gameInformation);
    }
}
