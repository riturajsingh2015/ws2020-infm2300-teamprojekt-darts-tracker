package poc;

import java.awt.Point;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.imageio.ImageIO;

public class PointsFinder {

    //korrekte Angaben des Bildes sind essentiell für den Erfolg der Berechnung
    int width = 1700;
    int height = 1700;
    int vX;
    int vY;
    int pixelX =0;
    int pixelY = 0;
    int ref_Angle;
    int points= 0;
    int multiplikator = 1;
    Point middlePoint = new Point();
    Point halfBullsEyeStart = new Point();
    Point halfBullsEyeEnd = new Point();
    Point tripleFieldStart = new Point();
    Point tripleFieldEnd = new Point();
    Point doubleFieldStart = new Point();
    Point doubleFieldEnd = new Point();

    /*
    * Setzen von Standard Informationen, die im gesamten Algorithmus verwendet werden
    * Übergabeparameter:
    * pixelX: X- Wert des Pixels
    * pixelY: Y- Wert des Pixels
    * */
    private void setStandardInformation(int pixelX, int pixelY){
        this.pixelX = pixelX;
        this.pixelY = pixelY;
        vX = (pixelX-width/2);
        vY = (height/2 - pixelY);
        ref_Angle = 81;
        middlePoint = new Point(this.height/2,this.width/2);
    }

    /*
    * Einstiegspunkt für die Berechnung aller Punkte.
    * Übergabeparameter:
    * Image: Bild des Pixels
    * Schritt 1: Errechnung des Punktwertes des Pixels
    * Schritt 2: Abgleichen, ob ein besonderes Feld getroffen wurde oder das Pixel im Aus ist.
    * */
    private int getPointsFromStandardInformation(BufferedImage image){
        double magnitude = Math.sqrt(Math.pow(vX,2)+Math.pow(vY,2));
        double angle = Math.floorMod((int)((Math.atan2(vY,vX)*180/Math.PI)+360-ref_Angle),360);
        int angleDiff = (int)(angle/18.0);
        //starting from the 20 points
        if (angleDiff == 0){ this.points = 20;}
        else if (angleDiff == 1){ this.points = 5;}
        else if (angleDiff == 2){ this.points = 12;}
        else if (angleDiff == 3){ this.points = 9;}
        else if (angleDiff == 4){ this.points = 14;}
        else if (angleDiff == 5){ this.points = 11;}
        else if (angleDiff == 6){ this.points = 8;}
        else if (angleDiff == 7){ this.points = 16;}
        else if (angleDiff == 8){ this.points = 7;}
        else if (angleDiff == 9){ this.points = 19;}
        else if (angleDiff == 10){ this.points = 3;}
        else if (angleDiff == 11){ this.points = 17;}
        else if (angleDiff == 12){ this.points = 2;}
        else if (angleDiff == 13){ this.points = 15;}
        else if (angleDiff == 14){ this.points = 10;}
        else if (angleDiff == 15){ this.points = 6;}
        else if (angleDiff == 16){ this.points = 13;}
        else if (angleDiff == 17){ this.points = 4;}
        else if (angleDiff == 18){ this.points = 18;}
        else if (angleDiff == 19){ this.points = 1;}
        else{//something went wrong
            this.points = -300;}

        this.getExtraStoragePoints(image);
        this.points = this.points * this.multiplikator;
        return this.points;
    }

    /*
    * Einstiegspunkt für die Ermittlung der speziellen Punkte:
    * BullsEye, Halfbullseye, Triple, Double Felder und Felder außerhalb des Punktebreiches (AUS)
    * Übergabeparameter:
    * Image: Bild des Pixels
    * */
    private void getExtraStoragePoints(BufferedImage image){
        double bullsEyeRadius = getBullsEyeRadius(image);
        List<Double>halfBullsEyeRadien = new ArrayList<>();
        List<Double>tripleFieldRadien = new ArrayList<>();
        List<Double>doubleFieldRadien = new ArrayList<>();
        halfBullsEyeRadien.addAll(getHalfBullsEyeRadien(image));
        tripleFieldRadien.addAll(getTripleFieldRadien(image));
        doubleFieldRadien.addAll(getDoubleFieldRadien(image));
        double pixelDistanceFromMiddelpoint = calculateDistance(this.pixelX,this.middlePoint.getX(),this.pixelY,this.middlePoint.getY());
        if(bullsEyeRadius > pixelDistanceFromMiddelpoint || bullsEyeRadius == pixelDistanceFromMiddelpoint){
            this.points = 50;
        }else if(halfBullsEyeRadien.get(0) < pixelDistanceFromMiddelpoint && pixelDistanceFromMiddelpoint < halfBullsEyeRadien.get(1)){
            this.points = 25;
        }else if(tripleFieldRadien.get(0) < pixelDistanceFromMiddelpoint && pixelDistanceFromMiddelpoint < tripleFieldRadien.get(1)){
            multiplikator = 3;
        }else if(doubleFieldRadien.get(0) < pixelDistanceFromMiddelpoint && pixelDistanceFromMiddelpoint < doubleFieldRadien.get(1)){
            multiplikator = 2;
        }else if(doubleFieldRadien.get(1) < pixelDistanceFromMiddelpoint){
            this.points = 0;
            this.multiplikator = 0;
        }
    }


    /*
    * Idee ist die Erkennung des Mittelpunktes des jeweiligen Bildes.
    * Der Iterator versucht den ersten Punkt zu treffen, der nicht mehr rot ist. Dadurch wird ein Radius errechnet und das Bullseye ist erkannt
    * Radiusberechnung des Bullseye
    * */
    private double getBullsEyeRadius(BufferedImage image){
        double radius = 0;
        int y = (int)this.middlePoint.getY();
        for (int x = 0; x < image.getWidth(); x++) {
            int  pixel   = image.getRGB(x, (int)y);
            int red   = (pixel >> 16) & 0xff;
            int green = (pixel >>  8) & 0xff;
            if(x > this.middlePoint.getX()){
                if(red == 0 && green != 0){
                    radius = calculateDistance(x,this.middlePoint.getX(),y,this.middlePoint.getY());
                    break;
                }
            }
        }
    return radius;
    }

    /*
     * Einstieg für die Errechnung der HalfBullsEye Radien
     * Übergabeparameter:
     * Image: Bild des Pixels für die Farberkennung
     * Diese Methode bildet den Einstieg für die Errechnung des Triple Feldes.
     *
     */
    private List<Double> getHalfBullsEyeRadien(BufferedImage image){
        halfBullsEyeStart = getIntervalBeginn(this.middlePoint.getX(), this.middlePoint.getY(),image);
        halfBullsEyeEnd = getIntervallEnd(halfBullsEyeStart.getX(),halfBullsEyeStart.getY(), image);
        double halfBullsEyeStartRadius = calculateDistance(this.halfBullsEyeStart.getX(),this.middlePoint.getX(),this.halfBullsEyeStart.getY(),this.middlePoint.getY());
        double halfBullsEyeEndRadius = calculateDistance(this.halfBullsEyeEnd.getX(),this.middlePoint.getX(),this.halfBullsEyeEnd.getY(),this.middlePoint.getY());
        List<Double>halfBullsEyeRadien = new ArrayList<>();
        halfBullsEyeRadien.add(halfBullsEyeStartRadius);
        halfBullsEyeRadien.add(halfBullsEyeEndRadius);
        return halfBullsEyeRadien;
    }

    /*
     * Einstieg für die Errechnung der Triple Field Radien
     * Übergabeparameter:
     * Image: Bild des Pixels für die Farberkennung
     * Diese Methode bildet den Einstieg für die Errechnung des Triple Feldes.
     * */
    private List<Double> getTripleFieldRadien(BufferedImage image){
        List<Double> tripleFieldRadien = new ArrayList<>();
        tripleFieldStart = getIntervalBeginn(this.halfBullsEyeEnd.getX(),this.halfBullsEyeEnd.getY(),image);
        tripleFieldEnd = getIntervallEnd(this.tripleFieldStart.getX(),this.tripleFieldStart.getY(),image);
        double tripleFieldStartRadius = calculateDistance(this.tripleFieldStart.getX(),this.middlePoint.getX(),this.tripleFieldStart.getY(),this.middlePoint.getY());
        double tripleFieldEndRadius = calculateDistance(this.tripleFieldEnd.getX(),this.middlePoint.getX(),this.tripleFieldEnd.getY(),this.middlePoint.getY());
        tripleFieldRadien.add(tripleFieldStartRadius);
        tripleFieldRadien.add(tripleFieldEndRadius);
        return tripleFieldRadien;
    }

    /*
    * Einstieg für die Errechnung der Double Field Radien
    * Übergabeparameter:
    * Image: Bild des Pixels für die Farberkennung
    * Diese Methode bildet den Einstieg für die Errechnung des Double Feldes.
    * */
    private List<Double> getDoubleFieldRadien(BufferedImage image){
        List<Double> doubleFieldRadien = new ArrayList<>();
        doubleFieldStart = getIntervalBeginn(this.tripleFieldEnd.getX(),this.tripleFieldEnd.getY(),image);
        doubleFieldEnd = getIntervallEnd(this.doubleFieldStart.getX(),this.doubleFieldStart.getY(),image);
        double doubleFieldStartRadius = calculateDistance(this.doubleFieldStart.getX(),this.middlePoint.getX(),this.doubleFieldStart.getY(),this.middlePoint.getY());
        double doubleFieldEndRadius = calculateDistance(this.doubleFieldEnd.getX(),this.middlePoint.getX(),this.doubleFieldEnd.getY(),this.middlePoint.getY());
        doubleFieldRadien.add(doubleFieldStartRadius);
        doubleFieldRadien.add(doubleFieldEndRadius);
        return doubleFieldRadien;
    }
    /*
     * Berechnung des Radius für den kleineren/inneren Kreis des Intervalls
     * Übergabeparameter:
     * beginnX: X - Koordinate von der man nach rechts iterieren soll
     * beginnY: Y - Koordinate: konstante Höhe der Iteration
     * Image: Das Bild um die Farbsegemente erkennen zu können
     * Dieser Algortihmus ist darauf ausgelegt, dass bei einem Dartboard der Iterator von einem bestimmte Anfangswert nach "rechts" läuft, um einen grünen Bereich zu erkennen.
     * Nutzung: Triple und Double Field Erkennung, wobei bei der Triple Field - Erkennung der Anfang nach dem Halfbullseye ist und beim Double Field nach dem Triple Field
     * */

    private Point getIntervalBeginn(double beginnX, double beginnY, BufferedImage image){
        int y = (int)beginnY;
        for (int x = 0; x < image.getWidth(); x++) {
            int  pixel   = image.getRGB(x, y);
            int red   = (pixel >> 16) & 0xff;
            int green = (pixel >>  8) & 0xff;
            if(x > beginnX) {
                if (red == 0 && green != 0) {
                    Point beginnPoint = new Point(x, y);
                    return beginnPoint;
                }
            }
       }
        return null;
    }

    /*
    * Berechnung des Radius für den größeren/äußeren Kreis des Intervalls
    * Übergabeparameter:
    * beginnX: X - Koordinate von der man nach rechts iterieren soll
    * beginnY: Y - Koordinate: konstante Höhe der Iteration
    * Image: Das Bild um die Farbsegemente erkennen zu können
    * Dieser Algortihmus ist darauf ausgelegt, dass bei einem Dartboard der Iterator von einem bestimmte Anfangswert nach "rechts" läuft, um einen weißen Bereich zu erkennen.
    * Nutzung: Triple und Double Field Erkennung, wobei bei der Triple Field - Erkennung der Anfang nach dem Halfbullseye ist und beim Double Field nach dem Triple Field
    * */
    private Point getIntervallEnd(double beginnX, double beginnY, BufferedImage image){
        int y = (int)this.middlePoint.getY();
        for (int x = 0; x < image.getWidth(); x++) {
            int  pixel   = image.getRGB(x, y);
            int red   = (pixel >> 16) & 0xff;
            int green = (pixel >>  8) & 0xff;
            if(y == beginnY){
                if(x > beginnX){
                    if(red != 0 && green != 0){
                        Point endPoint = new Point(x,y);
                        return endPoint;
                    }
                }
            }
        }
        return null;
    }

    /*
    * Mathematische Berechnung der Distanz
    * Übergabeparameter in der korrekten Reihenfolge:
    * xPixel == X - Wert des Pixels
    * yPixel == Y - Wert des Pixels
    * xMiddelpoint = X - Wert des Mittelpunktes des Bildes
    * yMiddelpoint = Y - Wert des Mittelpunktes des Bildes
    * Die mathematische Berechnung hinter dem Code ist folgende: d = √ ( x2 - x1 )^2 + (y2 - y1 )^2
    * */
    private double calculateDistance(double xPixel, double xMiddelPoint, double yPixel, double yMiddelpoint){
        double distance = Math.pow( xMiddelPoint - xPixel,2) + Math.pow(yMiddelpoint-yPixel,2);

        // find square root
        distance = Math.sqrt(distance);
        return distance;
    }

    /*
    * Einstieg des Programms
    * Übergabeparameter: X und Y Koordinaten des Pixels
    * Der Sinn dieser Methode ist es anhand des Koordinaten der Pfeilspitze und dem Bild die Punkte zu errechnen.
    * */
    public void getPointsFromXYCoordinates(int x, int y) throws IOException {
        //URL svg = getClass().getClassLoader().getResource("dartboard_abmessungen.png");
        URL svg = new File("D:\\MasterStudium\\semesterII\\Teamprojekt\\poc\\src\\main\\resources\\dartboard_abmessungen.png").toURI().toURL();
        BufferedImage image = ImageIO.read(svg);
        this.setStandardInformation(x, y);
        int points = this.getPointsFromStandardInformation(image);
        System.out.println("X - Koordinate: "+x+"; Y - Koordinate: "+y+" dazugehörige Punkte: "+points);
    }


    public static void main(String[] args) {
        try {
            PointsFinder pointsFinder = new PointsFinder();
            pointsFinder.getPointsFromXYCoordinates(1701,1701);
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}
