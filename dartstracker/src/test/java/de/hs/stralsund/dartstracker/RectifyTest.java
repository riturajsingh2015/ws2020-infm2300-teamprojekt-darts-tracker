package de.hs.stralsund.dartstracker;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import de.hs.stralsund.dartstracker.imagerecognition.ColorMask;
import de.hs.stralsund.dartstracker.imagerecognition.ImageUtils;

public class RectifyTest {

    static {
        /*
        programatically load opencv native lib
        you need to put the correct open-cv nativ lib for your actual plattform in test/resources to run this test
        for me it was windows x64 'opencv\build\java\x64'
         */
        System.load(FindingMarkers.class.getClassLoader().getResource("opencv_java452.dll").getPath());
    }

    @Test
    public void rectifyOnYellowPoints() {

        String boardImagePath = "src/test/resources/testInput/bordB/connected_0.jpg";
        String dartImagePath = "src/test/resources/testInput/bordB/connected_1.jpg";

//        String boardImagePath = "src/test/resources/input_diffImages2BinaryImage_one.jpg";
//        String dartImagePath = "src/test/resources/input_diffImages2BinaryImage_two.jpg";

        Mat boardImage = Imgcodecs.imread(boardImagePath);
        Mat dartImage = Imgcodecs.imread(dartImagePath);

        List<Point> boardPoints = ImageUtils.findMarkers(boardImage, ColorMask.YellowNew);
        List<Point> dartPoints = ImageUtils.findMarkers(dartImage, ColorMask.YellowNew);

        boardImage = ImageUtils.rectifyImageOnYellowMarkers(boardPoints, Imgcodecs.imread(boardImagePath));
        dartImage = ImageUtils.rectifyImageOnYellowMarkers(dartPoints, Imgcodecs.imread(dartImagePath));

        Mat imageDifference = ImageUtils.detectImageDifference(boardImage, dartImage, 5);

        Imgcodecs.imwrite("src/test/resources/testResults/rectifyOnYellowPoints/refOne.jpg", boardImage);
        Imgcodecs.imwrite("src/test/resources/testResults/rectifyOnYellowPoints/refTwo.jpg", dartImage);
        Imgcodecs.imwrite("src/test/resources/testResults/rectifyOnYellowPoints/difference.jpg", imageDifference);
    }
}
