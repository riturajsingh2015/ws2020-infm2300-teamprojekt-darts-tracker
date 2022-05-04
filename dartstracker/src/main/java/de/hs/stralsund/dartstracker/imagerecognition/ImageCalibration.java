package de.hs.stralsund.dartstracker.imagerecognition;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;

import androidx.camera.core.CameraSelector;
import androidx.camera.core.ImageAnalysis;
import androidx.camera.core.impl.ImageAnalysisConfig;

import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;

import java.util.Collections;
import java.util.List;

import de.hs.stralsund.dartstracker.dartgame.Tuple;

/*
This class tracks and stores the image calibration
 */
public class ImageCalibration {

    // ImageCalibration is singleton
    private static ImageCalibration instance = null;

    private ImageCalibration() {
        // hide constructor due to singleton
    }

    public static ImageCalibration getInstance() {
        if (instance == null) {
            instance = new ImageCalibration();
        }
        return instance;
    }

    // starting points or our source points from calibrated image
    private Point oben = new Point(0,0);
    private Point unten = new Point(0,0);
    private Point links = new Point(0,0);
    private Point rechts = new Point(0,0);

    // Die Maximale Toleranz in Pixel bei minimal Bewegungen in Pixel
    int calcTolerance = 10;

    // Der gespeicherte Zoomfaktor bei der Kalibrierung
    float zoomLevel = 0f;

    // Size in pixel in which the image will be taken and analized
    private final Size imageAnalysisResolution = new Size(1280, 960);

    // Select back camera as a default
    private final CameraSelector cameraSelector = new CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build();

    // Image Analysis
    private final ImageAnalysis imageAnalysis = new ImageAnalysis.Builder()
            .setTargetResolution(imageAnalysisResolution)
            .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST) // non blocking
    //HandlerThread analyzerThread = new HandlerThread("OpenCVAnalysis");
       // analyzerThread.start();
            // .setImageReaderMode(ImageAnalysis.ImageReaderMode.ACQUIRE_LATEST_IMAGE)
            //                .setCallbackHandler(new Handler(analyzerThread.getLooper()))
            //                .setImageQueueDepth(1).build();
            .build();

    // these are known points of a svg dart board - also our target points
    public static final MatOfPoint2f destPoints = new MatOfPoint2f(
            new Point(750, 220), // oben
            new Point(220, 750), // links
            new Point(1480, 750), // rechts
            new Point(750, 1480)); // unten

    // holds the timestamp of the last know 4 good points
    private Tuple<Long, MatOfPoint2f> lastValidPoints = null;

    /*
     Neue Punkte werden berechnet und das Ergebnis zurückgeliefert. Punkte haben eine Qualität von 0 bis 5
     0 (ganz mies - keiner der 4 Punkte liegt in der Toleranz)
     4 (ganz toll - alle 4 Punkte liegen in der Toleranz)
     Diese Qualität wird berechnet im Vergleich zu letzten bekannten sehr guten 5er Qualität
     Sind die neuen Punkte wieder maximale Qualtität. Wird der Stand gespeichert und für die nächste Berechnung als Basis verwendet.
     */
    public int calculateNewPoints(List<Point> markerPoints, int orientation) {
        int pointQuality = 0;
        if(markerPoints == null || markerPoints.size() == 0 || markerPoints.size() != 4){
            return pointQuality;
        }
        // determine we have "Hochkant" with orientation flip. todo maybe evaluate orientation
            /*
              order points to fit the target points
              oben = lowest Y
              links = lowest X
              rechts = highest X
              unten = highest Y
             */
        Collections.sort(markerPoints, (o1, o2) -> Double.compare(o1.y, o2.y));
        Point neuOben = markerPoints.get(0);
        Point neuUnten = markerPoints.get(3);
        Collections.sort(markerPoints, (o1, o2) -> Double.compare(o1.x, o2.x));
        Point neuLinks = markerPoints.get(0);
        Point neuRechts = markerPoints.get(3);

        // check every point
        if(isPointInTolerance(oben, neuOben)) {
            pointQuality++;
        }
        if(isPointInTolerance(links, neuLinks)) {
            pointQuality++;
        }
        if(isPointInTolerance(rechts, neuRechts)) {
            pointQuality++;
        }
        if(isPointInTolerance(unten, neuUnten)) {
            pointQuality++;
        }

        if(pointQuality == 4) {
            // if every 4 points are nice we save them
            MatOfPoint2f srcPoints = new MatOfPoint2f(
                    oben,
                    links,
                    rechts,
                    unten
            );
            lastValidPoints = new Tuple<>(System.currentTimeMillis(), srcPoints);
        }

        return pointQuality;
    }

    private boolean isPointInTolerance(Point a, Point b) {
        return calcTolerance > Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y));
    }

    public boolean setInitialSourcePoints (List<Point> markerPoints, int orientation) {
        if(markerPoints.size() == 4){
            // determine we have "Hochkant" with orientation flip. todo maybe evaluate orientation
            /*
              order points to fit the target points
              oben = lowest Y
              links = lowest X
              rechts = highest X
              unten = highest Y
             */
            Collections.sort(markerPoints, (o1, o2) -> Double.compare(o1.y, o2.y));
            oben = markerPoints.get(0);
            unten = markerPoints.get(3);
            Collections.sort(markerPoints, (o1, o2) -> Double.compare(o1.x, o2.x));
            links = markerPoints.get(0);
            rechts = markerPoints.get(3);
            return true;
        }
        return false;
    }

    public Tuple<Long, MatOfPoint2f> getLastValidPoints(){
        return lastValidPoints;
    }

    public void resetValidPoints(){
        lastValidPoints = null;
    }

    public Size getImageAnalysisResolution() {
        return imageAnalysisResolution;
    }

    public ImageAnalysis getImageAnalysis() {
        return imageAnalysis;
    }

    public CameraSelector getCameraSelector(){
        return cameraSelector;
    }

    public float getZoomLevel() {
        return zoomLevel;
    }

    public void setZoomLevel(float zoomLevel) {
        this.zoomLevel = zoomLevel;
    }
}
