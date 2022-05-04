package de.hs.stralsund.dartstracker.utils;

import android.Manifest;
import android.content.Context;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;

import de.hs.stralsund.dartstracker.dartgame.GameInformation;

public class DataManager implements Serializable {

    private String fileName = "GameSettings.txt";
    Gson gson = new Gson();
    GameInformation gameInformation;
    Context context;

    public DataManager(Context context){
        this.context = context;
    }

    //Game Settings schreiben
    public void writeData(GameInformation gameInformation) {
       //GameInformationen in Datei abspeichern
        FileOutputStream fileOutputStream;
        String gameInformationJSON = gson.toJson(gameInformation);

        try {
            //Stringausgabe des Objektes hier einfügen
            fileOutputStream = this.context.openFileOutput(fileName, this.context.MODE_PRIVATE);
            fileOutputStream.write(gameInformationJSON.getBytes());
            fileOutputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Game Settings einlesen
    public GameInformation getGameinformationFromFile(){
        FileInputStream fileInputStream;
        StringBuffer fileContent = new StringBuffer("");

        try{
            fileInputStream = this.context.openFileInput(fileName);
            int size;
            String newData = new String();
            while((size = fileInputStream.read()) != -1){
                newData += Character.toString((char) size);
            }
            gameInformation = gson.fromJson(newData, GameInformation.class);
            return this.gameInformation;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
