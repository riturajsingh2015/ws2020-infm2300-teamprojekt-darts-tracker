package de.hs.stralsund.dartstracker.dartgame;

import android.widget.TextView;
import android.widget.Toolbar;

import java.util.List;

import de.hs.stralsund.dartstracker.activities.R;


public class GameRulesEngine {


    //Zähler: Hochzählen der Legs und der Sets
    private void legWin(List<Player> eachPlayer, Player legWinner, TextView legNumber, TextView setNumber) {
        //Holen der Textelemente aus der Toolbar
        //TextView legNumber = toolBar.findViewById(R.id.legNumber);
        //TextView setNumber = toolBar.findViewById(R.id.setNumber);
        int legSummary = 0;
        int setSummary = 0;
        //Leg wurde gewonnen
        legWinner.setLeg();

        //Legs zusammenzählen
        for (Player player : eachPlayer) {
            legSummary += player.getNumberOfLegs();
            setSummary += player.getNumberOfSets();
        }

        //Textelemente anpassen
        if (legSummary == 3) {
            legWinner.setSet();
            if (setSummary == 3) {
                //Spiel gewonnen
            }
            else if (setSummary == 2) {
                setNumber.setText("Set 3");
            }
            else if (setSummary == 1) {
                setNumber.setText("Set 2");
            }
            legNumber.setText("Leg 1");
        }
        else if (legSummary == 2) {
            legNumber.setText("Leg 3");
        }
        else if (legSummary == 1) {
            legNumber.setText("Leg 2");
        }
    }
}
