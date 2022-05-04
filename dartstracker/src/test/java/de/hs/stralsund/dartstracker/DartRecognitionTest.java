package de.hs.stralsund.dartstracker;

import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

import de.hs.stralsund.dartstracker.imagerecognition.ColorMask;
import de.hs.stralsund.dartstracker.imagerecognition.ImageUtils;

public class DartRecognitionTest {

    static {
        /*
        programatically load opencv native lib
        you need to put the correct open-cv nativ lib for your actual plattform in test/resources to run this test
        for me it was windows x64 'opencv\build\java\x64'
         */
        System.load(FindingMarkers.class.getClassLoader().getResource("opencv_java452.dll").getPath());
    }

    @Test
    public void imageDifference_edited() {

        String boardImagePath = "src/test/resources/testInput/bordB/connected_0.jpg";
        String dartImagePath = "src/test/resources/testInput/bordB/connected_0_edited.jpg";

        Mat imageDifference = ImageUtils.detectImageDifference(Imgcodecs.imread(boardImagePath), Imgcodecs.imread(dartImagePath), 9);
        Imgcodecs.imwrite("src/test/resources/testOutput/dartRecognition/difference_edited.jpg", imageDifference);
    }

    @Test
    public void imageDifference_frames() {

        String boardImagePath = "src/test/resources/testInput/bordB/connected_0.jpg";
        String dartImagePath = "src/test/resources/testInput/bordB/connected_1.jpg";

        Mat imageDifference = ImageUtils.detectImageDifference(Imgcodecs.imread(boardImagePath), Imgcodecs.imread(dartImagePath), 9);
        Imgcodecs.imwrite("src/test/resources/testOutput/dartRecognition/difference_frames.jpg", imageDifference);
    }

    @Test
    public void imageDifference_filteredFrames() {

        String boardImagePath = "src/test/resources/testInput/bordB/connected_0.jpg";
        String dartImagePath = "src/test/resources/testInput/bordB/connected_1.jpg";

//        String boardImagePath = "src/test/resources/input_diffImages2BinaryImage_one.jpg";
//        String dartImagePath = "src/test/resources/input_diffImages2BinaryImage_two.jpg";

        Mat imageDifference = ImageUtils.detectImageDifference(Imgcodecs.imread(boardImagePath), Imgcodecs.imread(dartImagePath), 19);
        imageDifference = ImageUtils.applyColorMask(imageDifference, ColorMask.Black);

        Imgcodecs.imwrite("src/test/resources/testOutput/dartRecognition/difference_filteredFrames.jpg", imageDifference);
    }

    @Test
    public void imageDifference_rectifiedFrames() {

        String boardImagePath = "src/test/resources/testInput/bordB/connected_0.jpg";
        String dartImagePath = "src/test/resources/testInput/bordB/connected_1.jpg";

//        String boardImagePath = "src/test/resources/input_diffImages2BinaryImage_one.jpg";
//        String dartImagePath = "src/test/resources/input_diffImages2BinaryImage_two.jpg";

        Mat boardImage = Imgcodecs.imread(boardImagePath);
        Mat dartImage = Imgcodecs.imread(dartImagePath);


        ImageUtils.expandBorder(boardImage, 300);
        ImageUtils.expandBorder(dartImage, 300);

        List<Point> boardPoints = ImageUtils.findMarkers(boardImage, ColorMask.YellowNew);
        List<Point> dartPoints = ImageUtils.findMarkers(dartImage, ColorMask.YellowNew);

        boardImage = ImageUtils.rectifyImageOnYellowMarkers(boardPoints, boardImage);
        dartImage = ImageUtils.rectifyImageOnYellowMarkers(dartPoints, dartImage);

        Mat imageDifference = ImageUtils.detectImageDifference(boardImage, dartImage, 5);

        imageDifference = ImageUtils.applyColorMask(imageDifference, ColorMask.Black);

        Mat imageMergedObjects = ImageUtils.dilateErodeMask(imageDifference, 5, Imgproc.CV_SHAPE_RECT);

        ImageUtils.removeSmallerObjects(imageMergedObjects, 3000);

        Imgcodecs.imwrite("src/test/resources/testOutput/dartRecognition/difference_rectifiedRefOne.jpg", boardImage);
        Imgcodecs.imwrite("src/test/resources/testOutput/dartRecognition/difference_rectifiedRefTwo.jpg", dartImage);
        Imgcodecs.imwrite("src/test/resources/testOutput/dartRecognition/difference_rectified.jpg", imageDifference);
        Imgcodecs.imwrite("src/test/resources/testOutput/dartRecognition/difference_mergedObjects.jpg", imageMergedObjects);
    }
}
