package de.hs.stralsund.dartstracker;

import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import de.hs.stralsund.dartstracker.imagerecognition.ColorMask;
import de.hs.stralsund.dartstracker.imagerecognition.ImageUtils;

import static org.junit.Assert.assertNotNull;

public class  FindingMarkers {

    static {
        /*
        programatically load opencv native lib
        you need to put the correct open-cv nativ lib for your actual plattform in test/resources to run this test
        for me it was windows x64 'opencv\build\java\x64'
        NOT Android lib
         */
        System.load(FindingMarkers.class.getClassLoader().getResource("opencv_java452.dll").getPath());
    }


    @Test
    public void testColorPlateTransformation() throws IOException {

        String inputImagePath = "src/test/resources/testInput/bordB/connected_0.jpg";

        String outputDir = "src/test/resources/testOutput/findingMarkers/";
        String outputImagePath_rgbaToBrg = outputDir + "transformedImage_rgbaToBrg.jpg";
        String outputImagePath_brgToHsv = outputDir + "transformedImage_brgToHsv.jpg";

        Files.deleteIfExists(Paths.get(outputImagePath_rgbaToBrg));
        Files.deleteIfExists(Paths.get(outputImagePath_brgToHsv));

        Mat inputImage = Imgcodecs.imread(inputImagePath);

        Mat imageRgbaToBGR = new Mat();
        Mat imageBrgToHsv = new Mat();
        Imgproc.cvtColor(inputImage, imageRgbaToBGR, Imgproc.COLOR_RGBA2BGR);
        Imgproc.cvtColor(imageRgbaToBGR, imageBrgToHsv, Imgproc.COLOR_BGR2HSV);

        Imgcodecs.imwrite(outputImagePath_rgbaToBrg, imageRgbaToBGR);
        Imgcodecs.imwrite(outputImagePath_brgToHsv, imageBrgToHsv);
    }

    @Test
    public void findingMarkers() throws IOException {

        //String inputImagePath = "src/test/resources/testInput/bordB/connected_0.jpg";
        String inputImagePath = "src/test/resources/testInput/bordA/20210119_100740.jpg";
        Mat inputImage = Imgcodecs.imread(inputImagePath);
        assertNotNull(inputImage);

//        Scalar lowerYellow = new Scalar(25, 75, 91);
//        Scalar upperYellow = new Scalar(35, 255, 255);

        Scalar lowerYellow = new Scalar(19, 75, 91);
        Scalar upperYellow = new Scalar(50, 255, 255);

        Imgproc.cvtColor(inputImage, inputImage, Imgproc.COLOR_BGRA2RGBA);
        List<Point> foundPoints = ImageUtils.findMarkers(lowerYellow, upperYellow, inputImage);

        System.out.println("Points found: " + foundPoints.size());
        for (Point point : foundPoints) {
            System.out.printf("    Point at: %d / %d%n",
                    Double.valueOf(point.x).intValue(),
                    Double.valueOf(point.y).intValue());
        }

       Imgproc.cvtColor(inputImage, inputImage, Imgproc.COLOR_RGBA2BGR);
        String outputFile = "src/test/resources/testOutput/findingMarkers/marked_points.jpg";
        Imgcodecs.imwrite(outputFile, ImageUtils.drawYellowMarkers(foundPoints, inputImage));
    }



    @Test
    public void imageDifference_findAndDrawYellowMarkers() {

//        String boardImagePath = "src/test/resources/testImages/connected_0.jpg";
//        String dartImagePath = "src/test/resources/testImages/connected_1.jpg";

        String boardImagePath = "src/test/resources/testInput/bordA/input_diffImages2BinaryImage_one.jpg";
        String dartImagePath = "src/test/resources/testInput/bordA/input_diffImages2BinaryImage_two.jpg";

        Mat boardImage = Imgcodecs.imread(boardImagePath);
        Mat dartImage = Imgcodecs.imread(dartImagePath);

        ImageUtils.findAndDrawMarkers(boardImage, ColorMask.YellowNew);
        ImageUtils.findAndDrawMarkers(dartImage, ColorMask.YellowNew);

        Imgcodecs.imwrite("src/test/resources/testOutput/findingMarkers/difference_rectifiedRefOne.jpg", boardImage);
        Imgcodecs.imwrite("src/test/resources/testOutput/findingMarkers/difference_rectifiedRefTwo.jpg", dartImage);
    }

    /**
     * Ziel ist es bestimmte Markierungen pixelgenau im Bild zu finden.
     * Es gibt diverse Möglichkeiten:
     * - Farbsegementierung des Bildes -> Binary Bild (zero / Markerfarbe) -> Contouren im Bild suchen -> je nach Threashold die 4 größten Kontouren = 4 gesuchte Punkte. Davon das Zentrum = gesuchter Punkt
     * - kMeans Cluster
     *
     *  Verbesserbar mit:
     * - Blur() nutzen um Farbrauschen zu minimieren
     * - angepassteren Gelbwerten bei der Farbsegmentierung
     * - erode() und dilate()  nutzen um Bildrauschen im segmentierten Bild zu minimieren
     * - findContours() anpassen
     * - inout image size bigger = for better results = but slower
     *
     *  Hinweis:
     *  Statt bestimmte Punkte zu suchen: weitere Möglichkeit wäre Canny Edges und den äußersten Dartboard Cirle nehmen und rektifiziern.
     * don't commit output.jpg
     * @throws IOException
     */
    @Test
//    @Ignore
    public void findingMarkersWithContours() throws IOException {

        String testOutput = "src/test/resources/testOutput/findingMarkers/output_contours.jpg";
        Files.deleteIfExists(Paths.get(testOutput));

        // due to really weird android sdk behavior the resources path get  a '/' in front of it.
        Mat testImg = Imgcodecs.imread(getClass().getClassLoader().getResource("testInput/bordA/20210119_100740.jpg").getPath().substring(1), Imgcodecs.IMREAD_ANYCOLOR);

        Mat tempImg = new Mat();
        Imgproc.cvtColor(testImg, tempImg, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(tempImg, tempImg, Imgproc.COLOR_BGR2HSV);

        // Marker sind gelb (Diese Farbe kommt auf Dartboards nicht vor)
        Mat yellowSegmented = new Mat();
        Scalar lowYellow = new Scalar(19, 75, 91);
        Scalar upYellow = new Scalar(50, 255, 255);

        // Bild nach Gelb segmentieren
        Core.inRange(tempImg, lowYellow, upYellow, yellowSegmented);

        // finde alle Contouren
        List<MatOfPoint> contours = new ArrayList<MatOfPoint>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(yellowSegmented,contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // sammle die größte Konturen raus
        for (MatOfPoint matOfPoint : contours) {
            // Mindestgröße der Kontour
            if(Imgproc.contourArea(matOfPoint) > 20){
                Moments moments = Imgproc.moments(matOfPoint);
                // calculate center and draw cirle + text
                int centerX = (int) (moments.m10 / moments.m00);
                int centerY = (int) (moments.m01 / moments.m00);
                System.out.println("Found Point at: " + centerX + " / " + centerY);

                // draw a white cirle
                Imgproc.circle(testImg, new Point(centerX, centerY), 5, new Scalar(255, 255, 255), -1);
                Imgproc.putText(testImg, "Point",new Point(centerX - 20, centerY -20 ), Imgproc.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(255, 255, 255),3 );
            }
        }
        Imgcodecs.imwrite(testOutput, testImg);
    }
}
