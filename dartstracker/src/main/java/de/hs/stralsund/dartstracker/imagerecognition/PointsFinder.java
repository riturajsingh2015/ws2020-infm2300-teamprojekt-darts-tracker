package de.hs.stralsund.dartstracker.imagerecognition;

import org.opencv.core.Point;
import org.opencv.imgproc.Imgproc;

import de.hs.stralsund.dartstracker.dartgame.Tuple;

/**
 * <pre>
 * Grundidee:
 * - Mittelpunkt des Koordinatensystems = Mittelpunkt der Dartscheibe
 * - Für einen gegebenen Punkt berechnen wir uns den Drehwinkel um den Mittelpunkt
 * - Den Drehwinkel verschieben wir so, dass dieser bei der rechten Linie für 20 beginnt und von 0°-360° läuft
 * - Mit Hilfe der Radien ermitteln wir 50/25/Single/Double/Triple
 *
 * <pre>
 * Abmessungen der Dartscheibe Nach der Sport- und Wettkampfordnung des Deutschen Dartverbandes:
 *
 * Double- und Triple-Ring (Innenmaß) 8,0 mm
 * Durchmesser des Doppelbull (Innenmaß) 12,7 mm
 * Größe des gesamten Bull (Innenmaß) 31,8 mm
 * Entfernung vom äußeren Doppeldraht zum Bull 170,0 mm
 * Entfernung vom äußeren Tripledraht zum Bull 107,0 mm
 * Entfernung von einem äußeren Doubledraht zum gegenüberliegenden äußeren Doubledraht 340,0 mm
 * Drahtdurchmesser: 16–18 SWG (entspricht etwa 1,2 bis 1,6 mm) (wir legen 1,4mm fest)
 * </pre>
 *
 * Aus den Abmessungen ergeben sich Radien für die jeweiligen Zonen Bull's eye, Bull, Single, Double, Triple.
 * Auf diese Radien werden an entsprechender ein halber Drahtdurchmesser hinzuaddiert um die Drahtzonen zu elemenieren.
 *
 * @see <a href="https://de.wikipedia.org/wiki/Darts#Dartscheibe">Wikipedia</a>
 */
public class PointsFinder {

    private final int   SEGMENT_ANGLE = 18; // 360* / 20 Punktsegmente = 18° pro Dartsegment im umgekehrten Uhrzeigersinn
    private final Point MIDDLE;             // todo ungenau hier schon zu integern
    private final float WIRE_THICKNESS = 1.2f; // Drahtdicke

    // Radien in mm
    private final Float RADIUS_OUT= 170f;
    // Radien <Außenkante/Innenkante> um Drahtdicke bereinigt
    private final Tuple<Float,Float> RADIUS_INTERVALL_DOUBLE = new Tuple<>(170-WIRE_THICKNESS/2, 170-(WIRE_THICKNESS/2)-8-(WIRE_THICKNESS/2));
    private final Tuple<Float,Float> RADIUS_INTERVALL_TRIPLE = new Tuple<>(107-WIRE_THICKNESS/2, 107-(WIRE_THICKNESS/2)-8 -(WIRE_THICKNESS/2));
    private final Tuple<Float,Float> RADIUS_INTERVALL_BULL = new Tuple<>((31.8f+2*WIRE_THICKNESS)/2, (12.7f+2*WIRE_THICKNESS)/2);
    private final Tuple<Float,Float> RADIUS_INTERVALL_BULLS_EYE = new Tuple<>((12.7f+2*WIRE_THICKNESS)/2, 0f);
    // Radien in Pixel <Außenkante/Innenkante> mit gegenen Pixelradius berechnet
    private final Tuple<Integer,Integer>RADIUS_INTERVALL_DOUBLE_PIXEL;
    private final Tuple<Integer,Integer>RADIUS_INTERVALL_TRIPLE_PIXEL;
    private final Tuple<Integer,Integer>RADIUS_INTERVALL_BULL_PIXEL;
    private final Tuple<Integer,Integer>RADIUS_INTERVALL_BULLS_EYE_PIXEL;

    public PointsFinder(Point middle,  int radius) {

        RADIUS_INTERVALL_DOUBLE_PIXEL = new Tuple<>(Math.round(radius * RADIUS_INTERVALL_DOUBLE.a / RADIUS_OUT), Math.round(radius * RADIUS_INTERVALL_DOUBLE.b / RADIUS_OUT));
        RADIUS_INTERVALL_TRIPLE_PIXEL = new Tuple<>(Math.round(radius * RADIUS_INTERVALL_TRIPLE.a / RADIUS_OUT), Math.round(radius * RADIUS_INTERVALL_TRIPLE.b / RADIUS_OUT));
        RADIUS_INTERVALL_BULL_PIXEL = new Tuple<>(Math.round(radius * RADIUS_INTERVALL_BULL.a / RADIUS_OUT), Math.round(radius * RADIUS_INTERVALL_BULL.b / RADIUS_OUT));
        RADIUS_INTERVALL_BULLS_EYE_PIXEL = new Tuple<>(Math.round(radius * RADIUS_INTERVALL_BULLS_EYE.a / RADIUS_OUT), 0);

        MIDDLE = middle; // take care of x/y
    }

    public int getPointsFromXYCoordinates(int x, int y) {

        // gegeben Punkt zum Mittelpunkt normalisieren
        // Drehwinkel Theta -180 bis 180
        double theta = Math.toDegrees(Math.atan2(MIDDLE.y - y, x - MIDDLE.x));
        // Jetzt wird ein bisschen getrickst.
        // Drehwinkel manuell "drehen" um +360°(ganzer Kreis) - 81°(Virtelkreis gegen Uhrzeigersinn und halbes Punktsegment wieder nach vorne)
        // Intervall wäre jetzt 99° - 456° -> floorMod 360° "verschiebt" es zurück auf 0° - 360°
        // Jetzt beginnt der Drehwinkel exakt bei der rechten Linie vom Segment 20, Läuft gegen den Uhrzeigersinn und alle 18° ist ein weiteres Segment erreicht
        long angle = modulo(Math.round(theta + 360 - (90 - SEGMENT_ANGLE / 2)), 360);   // TODO fehleranfällig bei leicht schrägem Bord
        int segment = (int) (angle / SEGMENT_ANGLE);
        int dartValue;
        switch (segment) {
            case 0:
                dartValue = 20;
                break;
            case 1:
                dartValue = 5;
                break;
            case 2:
                dartValue = 12;
                break;
            case 3:
                dartValue = 9;
                break;
            case 4:
                dartValue = 14;
                break;
            case 5:
                dartValue = 11;
                break;
            case 6:
                dartValue = 8;
                break;
            case 7:
                dartValue = 16;
                break;
            case 8:
                dartValue = 7;
                break;
            case 9:
                dartValue = 19;
                break;
            case 10:
                dartValue = 3;
                break;
            case 11:
                dartValue = 17;
                break;
            case 12:
                dartValue = 2;
                break;
            case 13:
                dartValue = 15;
                break;
            case 14:
                dartValue = 10;
                break;
            case 15:
                dartValue = 6;
                break;
            case 16:
                dartValue = 13;
                break;
            case 17:
                dartValue = 4;
                break;
            case 18:
                dartValue = 18;
                break;
            case 19:
                dartValue = 1;
                break;
            default:
                dartValue = -1; // shouldn't be possible
                break;
        }
        // nun ermitteln wir Anhand der Radien den richtigen Punktwert
        return calculateValueOnRadius(dartValue, x, y);
    }

    private long modulo(long number, int modulo) {

        while (number >= modulo) {
            number -= modulo;
        }

        return number;
    }

    private int calculateValueOnRadius(int dartValue, int x, int y) {
        double distance = calculateDistance(x, y);
        if(distance > RADIUS_INTERVALL_DOUBLE_PIXEL.a){
            // out
            return 0;
        }
        else if(distance < RADIUS_INTERVALL_DOUBLE_PIXEL.a && distance > RADIUS_INTERVALL_DOUBLE_PIXEL.b) {
            // double of dartValue
            return dartValue * 2;
        }
        else if(distance < RADIUS_INTERVALL_TRIPLE_PIXEL.a && distance > RADIUS_INTERVALL_TRIPLE_PIXEL.b) {
            // triple of dartValue
            return dartValue * 3;
        }
        else if(distance < RADIUS_INTERVALL_BULL_PIXEL.a && distance > RADIUS_INTERVALL_BULL_PIXEL.b) {
            // Bull
            return 25;
        }
        else if(distance < RADIUS_INTERVALL_BULLS_EYE_PIXEL.a && distance >= RADIUS_INTERVALL_BULLS_EYE_PIXEL.b) {
            // Bull's eye
            return 50;
        }
        return dartValue;
    }

    /**
     * Calculates the distance of a given point(x,y) to coordinate center
     * d = √ ( x2 - x1 )^2 + (y2 - y1 )^2
     *
     * @param x
     * @param y
     * @return distance
     */
    private double calculateDistance(int x, int y) {
        return Math.sqrt(Math.pow(MIDDLE.x - x, 2) + Math.pow(MIDDLE.y - y, 2));
    }

    /**
     * Calculates the distance of a given point(x,y) to coordinate center
     * d = √ ( x2 - x1 )^2 + (y2 - y1 )^2
     *
     * @return distance
     */
    public static double calculateDistance(double x1, double y1, double x2, double y2) {
        return Math.sqrt(Math.pow(x1 - x2, 2) + Math.pow(y1 - y2, 2));
    }
}