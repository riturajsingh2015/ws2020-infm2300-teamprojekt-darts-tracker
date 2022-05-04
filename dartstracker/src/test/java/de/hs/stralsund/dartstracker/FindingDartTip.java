package de.hs.stralsund.dartstracker;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import de.hs.stralsund.dartstracker.dartgame.Tuple;

public class FindingDartTip {

    static {
        /*
        programatically load opencv native lib
        you need to put the correct open-cv nativ lib for your actual plattform in test/resources to run this test
        for me it was windows x64 'opencv\build\java\x64'
        NOT Android lib
         */
        System.load(FindingDartTip.class.getClassLoader().getResource("opencv_java452.dll").getPath());
    }

    /**
     * Ausgangsbild:
     * Greyscale binary Bild eines Dartpfeils + Rauschen
     *
     * Ziel:
     * Finde (markiere) die Spitze des Dartpfeils
     *
     * Erste Lösungsidee
     * Die Max-Contourpunkte zeichnen exakt den Übergang zwischen Hintergrund und Pfeil - also Pfeil wird umrahmt
     * Massenmittelpunkt aller Rahmen Pixel ermitteln - Sollte immer exakt gegenüber der gesuchten Pfeilsitze sein und sich "hinter" dem Mittelpunkt der Bounding Box befinden
     * Damit können wir davon ausgehen, dass der am weitesten entfernte Kontourpunkt = Pfeilspitze sein müsste
     *
     * Achtung:
     * Sollte der Pfeil keinen zusammenhängenden Pixelhaufen ergeben bricht die Idee. (wohl nicht soo wahrscheinlich?)
     * Unklar ob die Annahme für den Massenmittelpunkt wirklich in jeder räumlichen Drehung des Pfeils zustimmt
     * Keine Chance bei überdeckungen von/durch andere Pfeile (könnte man ermitteln wie bei der Massenmittelpunkt von der Dartscheibe entfernt ist, wären vielleicht neue Lösungsmöglichkeiten eröffnet)
     *
     * @throws IOException
     */
    @Test
    public void detectDartTip_biggestObject() throws IOException {

        String inputImagePath = "src/test/resources/testInput/darts/input_dart_extrama_point.jpg";
        //String inputImagePath = "src/test/resources/testInput/darts/detectedBinaryArrow.jpg";
        String outputImagePath = "src/test/resources/testOutput/detectDartTip/biggestObject.jpg";

        Files.deleteIfExists(Paths.get(outputImagePath));

        Mat testImg = Imgcodecs.imread(inputImagePath);

        Mat tempImg = new Mat();
        Imgproc.cvtColor(testImg, tempImg, Imgproc.COLOR_RGB2GRAY);

        // Sammle alle Konturen
        List<MatOfPoint> contourPoints = new ArrayList();
        Mat hierarchy = new Mat();
        Imgproc.findContours(tempImg, contourPoints, hierarchy, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Finde die Max-Contourpunkte = Größte Zusammenhängende Pixelmasse = Übergang zwischen Hintergrund und Pfeil = Rahmen des Pfeils
        MatOfPoint max = contourPoints.stream().max((o1, o2) -> Double.compare(Imgproc.contourArea(o1),Imgproc.contourArea(o2))).get();
        Imgproc.drawContours(testImg, Collections.singletonList(max), -1, new Scalar(0,255,255), 1);

        // Bounding box von diesem Rahmen einzeichnen
        Rect boundingBox = Imgproc.boundingRect(max);
        Imgproc.rectangle(testImg, boundingBox,new Scalar(255, 0, 0) );

        // MAssenmittelpunkt finden und Einzeichnen
        Moments moments = Imgproc.moments(max);
        int centerMassX = (int) (moments.m10 / moments.m00);
        int centerMassY = (int) (moments.m01 / moments.m00);
        Point massCentroid = new Point(centerMassX, centerMassY);
        Imgproc.circle(testImg,massCentroid, 3, new Scalar(255, 0, 0), 2);

        // Weit entferntesten Punkt finden und einzeichnen
        Tuple<Point,Double> farestContourPoint = new Tuple<>(massCentroid, 0d);
        for (Point contourPoint : max.toArray()) {
            double newDistance = pointDistance(contourPoint, massCentroid);
            if(farestContourPoint.b < newDistance){
                farestContourPoint = new Tuple<>(contourPoint, newDistance);
            }
        }
        Imgproc.circle(testImg, farestContourPoint.a, 3, new Scalar(0, 0, 255), 2);

        // Ergebnisse abspeichern
        Imgcodecs.imwrite(outputImagePath, testImg);
    }


    @Test
    public void detectDartTip_multipleObjects() throws IOException {

//        String inputImagePath = "src/test/resources/testInput/darts/input_dart_extrama_point.jpg";
        String inputImagePath = "src/test/resources/testInput/darts/detectedBinaryArrow.jpg";
        String outputImagePath = "src/test/resources/testOutput/detectDartTip/multipleObjects.jpg";

        Mat testImg = Imgcodecs.imread(inputImagePath);

        Mat grayImg = new Mat();
        Imgproc.cvtColor(testImg, grayImg, Imgproc.COLOR_RGB2GRAY);

        Imgproc.threshold(grayImg, grayImg, 100, 255, Imgproc.THRESH_BINARY);

        // Sammle alle Konturen
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(grayImg, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // Finde die Max-Contourpunkte = Größte Zusammenhängende Pixelmasse = Übergang zwischen Hintergrund und Pfeil = Rahmen des Pfeils
        Imgproc.drawContours(testImg, contours, -1, new Scalar(0, 255, 255), 1);

        MatOfPoint mergedContours = mergeMatOfPoints(contours);

        // Bounding box von diesem Rahmen einzeichnen
        Rect boundingBox = Imgproc.boundingRect(mergedContours);
        Imgproc.rectangle(testImg, boundingBox, new Scalar(255, 0, 0));

        // Massenmittelpunkt finden und Einzeichnen
        Moments moments = Imgproc.moments(mergedContours);
        int centerMassX = (int) (moments.m10 / moments.m00);
        int centerMassY = (int) (moments.m01 / moments.m00);
        Point massCentroid = new Point(centerMassX, centerMassY);
        Imgproc.circle(testImg,massCentroid, 3, new Scalar(255, 0, 0), 2);

        // Weit entferntesten Punkt finden und einzeichnen
        Tuple<Point, Double> farthestContourPoint = new Tuple<>(massCentroid, 0d);
        for (Point contourPoint : mergedContours.toArray()) {
            double newDistance = pointDistance(contourPoint, massCentroid);
            if(farthestContourPoint.b < newDistance){
                farthestContourPoint = new Tuple<>(contourPoint, newDistance);
            }
        }
        Imgproc.circle(testImg, farthestContourPoint.a, 3, new Scalar(0, 0, 255), 2);

        // Ergebnisse abspeichern
        Imgcodecs.imwrite(outputImagePath, testImg);
    }

    public static MatOfPoint mergeMatOfPoints(List<MatOfPoint> mops) {

        List<Point> mergedPoints = new ArrayList<>();
        mops.forEach(mop -> mergedPoints.addAll(mop.toList()));

        MatOfPoint mopMerged = new MatOfPoint();
        mopMerged.fromList(mergedPoints);

        return mopMerged;
    }

    private double pointDistance(Point a, Point b) {
        try {
            return Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y));
        }
        catch (Exception e){
            return 0d;
        }
    }
}
