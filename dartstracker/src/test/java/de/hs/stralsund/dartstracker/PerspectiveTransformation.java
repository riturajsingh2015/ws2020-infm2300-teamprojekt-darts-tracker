package de.hs.stralsund.dartstracker;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

public class PerspectiveTransformation {

    static {
        /*
        programatically load opencv native lib
        you need to put the correct open-cv nativ lib for your actual plattform in test/resources to run this test
        for me it was windows x64 'opencv\build\java\x64'
        NOT Android lib
         */
        System.load(PerspectiveTransformation.class.getClassLoader().getResource("opencv_java452.dll").getPath());
    }

    /**
     * This Method shows geometric tranformation of a taken image of dart board to the destination dartboard image.
     *
     *
     * don't commit output.jpg
     * @throws IOException
     */
    @Test
    public void rectifyImage() throws IOException {

        String testOutput = "src/test/resources/testOutput/perspectiveTransformation/rectifyImage.jpg";
        Files.deleteIfExists(Paths.get(testOutput));

        // due to really weird android sdk behavior the resources path get  a '/' in front of it.
        Mat testImg = Imgcodecs.imread(getClass().getClassLoader().getResource("testInput/bordA/input_PerspectiveTransformation.jpg").getPath().substring(1), Imgcodecs.IMREAD_ANYCOLOR);

        // thse are points which we took to match them to known points
        MatOfPoint2f srcPoints = new MatOfPoint2f(
                new Point(2640f, 627f),
                new Point(1942, 1416),
                new Point(3408, 1380),
                new Point(2646, 2435));

        // these are known points of a svg dart board
        MatOfPoint2f destPoints = new MatOfPoint2f(
                new Point(750, 220), // oben
                new Point(220, 750), // links
                new Point(1480, 750), // rechts
                new Point(750, 1480)); // unten

        Mat transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, destPoints);

        Mat outputImg = new Mat();
        Imgproc.warpPerspective(testImg, outputImg, transformMatrix, new Size(1701,1701));

        Imgcodecs.imwrite(testOutput, outputImg);
    }
}
