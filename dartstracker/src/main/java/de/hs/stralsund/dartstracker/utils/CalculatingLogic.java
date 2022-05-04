package de.hs.stralsund.dartstracker.utils;

import android.widget.TextView;
import android.widget.Toast;

import de.hs.stralsund.dartstracker.activities.DartGameActivity;
import de.hs.stralsund.dartstracker.activities.R;
import de.hs.stralsund.dartstracker.activities.StartMenueActivity;
import de.hs.stralsund.dartstracker.dartgame.GameInformation;
import de.hs.stralsund.dartstracker.dartgame.Player;

public class CalculatingLogic {

    private int playerCounter = 0;
    private DartGameActivity dartGame;

    private final TextView arrowOneTextView;
    private final TextView arrowTwoTextView;
    private final TextView arrowThreeTextView;


    public CalculatingLogic(DartGameActivity dartGame) {
        this.dartGame = dartGame;

        this.arrowOneTextView = (TextView) this.dartGame.findViewById(R.id.arrowOne);
        this.arrowTwoTextView = (TextView) this.dartGame.findViewById(R.id.arrowTwo);
        this.arrowThreeTextView = (TextView) this.dartGame.findViewById(R.id.arrowThree);
    }

    public void determineLegPoints(DartGameActivity activity, GameInformation gameInformation, int throwingPoints) {

        Player player = gameInformation.getPlayer(playerCounter);

        int pointsInLeg = player.getPointsInLeg();
        if (pointsInLeg < throwingPoints) {

            Toast.makeText(
                    activity,
                    "Um " + (throwingPoints - pointsInLeg) + " zu hoch geworfen.",
                    Toast.LENGTH_LONG).show();

            arrowOneTextView.setText("");
            arrowTwoTextView.setText("");
            arrowThreeTextView.setText("");

            changeTurn(gameInformation);
            return;
        }

        if (pointsInLeg == throwingPoints) {

            activity.openWinnerDialog(player.getPlayerName());

            player.setLeg();
            resetPlayerScores(gameInformation);
            return;
        }

        player.setPointsInLeg(throwingPoints);
        if(player.getArrowThrownList().size() < 4){
            //1. Gesamtzahl korrigieren
            updatePlayerStats(player);
            if(player.getArrowThrownList().size() < 1){
                //2. Pfeil - Anzeige verbessern
                arrowOneTextView.setText(String.valueOf(throwingPoints));
                arrowTwoTextView.setText("");
                arrowThreeTextView.setText("");
                player.setArrowThrown(1);
            }else if(player.getArrowThrownList().size() < 2){
                //2. Pfeil - Anzeige verbessern
                arrowTwoTextView.setText(String.valueOf(throwingPoints));
                player.setArrowThrown(2);
            }else if(player.getArrowThrownList().size() <= 3){
                //2. Pf eil - Anzeige verbessern
                arrowThreeTextView.setText(String.valueOf(throwingPoints));
                player.setArrowThrown(3);
                changeTurn(gameInformation);
            }
        }
    }

    private void updatePlayerPoints(Player player) {
        ((TextView) this.dartGame.findViewById(DartGameActivity.POINT_ID_INCREMENT + player.getPlayerID()))
                .setText(String.valueOf(player.getPointsInLeg()));
    }

    private void updatePlayerStats(Player player) {

        updateBoxDrawable(player);

        updatePlayerPoints(player);

        ((TextView) this.dartGame.findViewById(DartGameActivity.LEG_ID_INCREMENT + player.getPlayerID()))
                .setText(String.valueOf(player.getNumberOfLegs()));

        ((TextView) this.dartGame.findViewById(DartGameActivity.SET_ID_INCREMENT + player.getPlayerID()))
                .setText(String.valueOf(player.getNumberOfSets()));
    }

    private void updateBoxDrawable(Player player) {
        this.dartGame.findViewById(DartGameActivity.BOX_ID_INCREMENT + player.getPlayerID())
                .setBackgroundResource(getBoxDrawable(player));
    }

    private int getBoxDrawable(Player player) {

        if (player.getPlayerID() == playerCounter) {
            return R.drawable.rounded_highlighted_rectangle;
        } else {
            return R.drawable.rounded_rectangle;
        }
    }

    public void resetPlayerScores(GameInformation gameInformation) {

        for (Player player : gameInformation.getPlayerList()) {

            player.setStartPointsInLeg(gameInformation.getPointsInLeg());
            player.clearArrowThrown();
            updatePlayerStats(player);
        }

        playerCounter = 0;

        arrowOneTextView.setText("");
        arrowTwoTextView.setText("");
        arrowThreeTextView.setText("");

        StartMenueActivity.dataManager.writeData(gameInformation);
    }

    public void changeTurn(GameInformation gameInformation) {

        Player player = gameInformation.getPlayerList().get(playerCounter);
        player.clearArrowThrown();

        if(gameInformation.getPlayerList().size() > 1){
            player.setIsTurn(false);
            this.playerCounter++;
            if (this.playerCounter == gameInformation.getPlayerList().size()){
                this.playerCounter = 0;
            }

            Player nextPlayer = gameInformation.getPlayerList().get(this.playerCounter);
            nextPlayer.setIsTurn(true);

            updateBoxDrawable(player);
            updateBoxDrawable(nextPlayer);
        }

        StartMenueActivity.dataManager.writeData(gameInformation);
    }
}
