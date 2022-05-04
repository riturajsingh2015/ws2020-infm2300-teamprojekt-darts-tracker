package de.hs.stralsund.dartstracker.dartgame;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Player implements Serializable {

    private boolean isTurn = false;
    private boolean arrowFinished = false;
    private int playerID;
    private int pointsInLeg;
    private int numberOfLegs = 0;
    private int numberOfSets = 0;
    private String playerName;
    private List<Integer> arrowThrown = new ArrayList<Integer>();
    private Map<String, Integer> listOfArrowResults = new HashMap<>();

    // GETTER
    public boolean getIsTurn() {
        return this.isTurn;
    }

    public boolean getArrowFinished(){return this.arrowFinished;}

    public int getPointsInLeg() {
        return this.pointsInLeg;
    }

    public int getNumberOfLegs() {
        return this.numberOfLegs;
    }

    public int getNumberOfSets() {
        return this.numberOfSets;
    }

    public String getPlayerName() {
        return this.playerName;
    }

    public int getPlayerID() {
        return this.playerID;
    }

    public Map<String,Integer> getListOfArrowResults(){return this.listOfArrowResults;}

    public List<Integer> getArrowThrownList(){return this.arrowThrown;}

    //SETTER
    public void setSet() {
        this.numberOfSets++;
    }

    public void setLeg() {

        if (numberOfLegs < 2) {

            this.numberOfLegs++;
        } else {

            this.numberOfLegs = 0;
            setSet();
        }
    }

    public void setStartPointsInLeg(int pointsInLeg) {
        this.pointsInLeg = pointsInLeg;
    }

    public void setPlayerID(int playerID) {
        this.playerID = playerID;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public void setPointsInLeg(int pointsInLeg) {
        this.pointsInLeg -= pointsInLeg;
    }

    public void setIsTurn(boolean isTurn) {
        this.isTurn = isTurn;
    }

    public void setArrowFinished(boolean arrowFinished){ this.arrowFinished = arrowFinished; }

    public void setListOfArrowResults(String key,Integer value){this.listOfArrowResults.put(key,value);}

    public void setArrowThrown(int x){this.arrowThrown.add(x);}

    public void clearArrowThrown(){this.arrowThrown.clear();}
}
