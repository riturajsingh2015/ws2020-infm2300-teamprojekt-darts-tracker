package de.hs.stralsund.dartstracker.dartgame;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import de.hs.stralsund.dartstracker.imagerecognition.ImageCalibration;

public class GameInformation implements Serializable{

    private final List<Player> playerList = new ArrayList<>();
    /*
     * Dart - Spiel Informationsbereich
     * */
    private int legNumber = 0;
    private int setNumber = 0;
    private int pointsInLeg = 501;
    private boolean isGameSetting = false;

    public int getLegNumber() {
        return this.legNumber;
    }

    //SETTER
    public void setLegNumber(int legNumber) {
        this.legNumber = legNumber;
    }

    public int getSetNumber() {
        return this.setNumber;
    }

    //GETTER
    public void setSetNumber(int setNumber) {
        this.setNumber = setNumber;
    }

    public List<Player> getPlayerList() {
        return this.playerList;
    }

    public void setPlayerList(Player player) {this.playerList.add(player);}

    public Player getPlayer(int turnNumber) {
        return this.playerList.get(turnNumber);
    }

    public int getPointsInLeg() {
        return this.pointsInLeg;
    }

    public void setPointsInLeg(int points) {
        this.pointsInLeg = points;
    }

    //Getter
    public boolean getIsGameSetting() {
        return this.isGameSetting;
    }

    //Setter
    public void setIsGameSetting(boolean isCalibrated) {
        this.isGameSetting = isCalibrated;
    }


    //String Repräsentation
}
