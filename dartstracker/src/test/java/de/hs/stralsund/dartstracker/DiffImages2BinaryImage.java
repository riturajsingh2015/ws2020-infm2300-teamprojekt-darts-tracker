package de.hs.stralsund.dartstracker;

import org.junit.Ignore;
import org.junit.Test;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import static org.opencv.core.CvType.CV_8UC1;

/**
 * Ausgangsbilder:
 * Erstes Bild ist die Aufnahme einer Dartscheibe
 * Zweites Bild ist die gleiche Ausrichtung der Aufnahme nur dieses mal steckt ein Pfeil im Bild
 * Ziel:
 * background subtraction - Die Unterschiede zum 2. Bild sind kenntlich zu machen + Rauschen bereinigen und als Ergebnis ein Graustufen-Binär-Bild zu zeigen.
 * Alles in annehmbarer Geschwindigkeit
 *
 * Verbesserungen:
 * Einsatz eines Features Match Algorithmus um die beiden Bilder vor der Differenzberechnung besser übereinander legen zu können?
 * Structural Similarity Index (SSIM) - kein opencv
 * backgroundSubtractorMOG2 oder BackgroundSubtractorKNN von opencv testen
 *
 * Was nicht geht:
 * Reines Imgproc.threshold() da Pfeile sämtliche andere Farben/Bereiche im Bild überlappen können
 */
public class DiffImages2BinaryImage {

    static {
        /*
        programatically load opencv native lib
        you need to put the correct open-cv nativ lib for your actual plattform in test/resources to run this test
        for me it was windows x64 'opencv\build\java\x64'
        NOT Android lib
         */
        System.load(DiffImages2BinaryImage.class.getClassLoader().getResource("opencv_java452.dll").getPath());
    }

    /**
     *
     * Erste Lösungsidee:
     * Aboslute Differenz Bild 1 und Bild 2 jedes einzelnen Pixels
     * Eine Maske erstellen mit Schwellwert für die Größe der Differenz
     * Ergebnis sollte dann nur noch der Pfeil selbst als Graustufen-Binärbild sein.
     *
     * Probleme:
     * Jede Mikrobewegung, Licht, Schatten, Fokus, etc. macht den Lösungsansatz zunichte
     *
     * https://stackoverflow.com/questions/27035672/cv-extract-differences-between-two-images
     *
     * @throws IOException
     */
    @Test
    public void diffImagesAbsDiff() throws IOException {

        String outputImagePath = "src/test/resources/testOutput/diffImage2BinaryImage/diffImagesAbsDiff.jpg";
        Files.deleteIfExists(Paths.get(outputImagePath));

        // due to really weird android sdk behavior the resources path get  a '/' in front of it.
        Mat imgOne = Imgcodecs.imread(getClass().getClassLoader().getResource("testInput/bordA/input_diffImages2BinaryImage_one.jpg").getPath().substring(1), Imgcodecs.IMREAD_ANYCOLOR);
        Mat imgTow = Imgcodecs.imread(getClass().getClassLoader().getResource("testInput/bordA/input_diffImages2BinaryImage_two.jpg").getPath().substring(1), Imgcodecs.IMREAD_ANYCOLOR);

        // color convert to HSV - not needed but give other results
        Imgproc.cvtColor(imgOne, imgOne, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(imgOne, imgOne, Imgproc.COLOR_BGR2HSV);
        Imgproc.cvtColor(imgTow, imgTow, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(imgTow, imgTow, Imgproc.COLOR_BGR2HSV);

        // diff the images
        Mat diffImg = new Mat();
        Core.absdiff(imgOne, imgTow, diffImg);
        //Imgcodecs.imwrite("src/test/resources/output_diff_image.jpg", diffImg);

        // greyscale diffed image
        //Imgproc.cvtColor(diffImg, diffImg, Imgproc.COLOR_RGB2GRAY);

        // Erstellen einer Maske auf Grundlage eines Unterschied-Schwellwertes (sehr langsame Funktion)
        int threshold = 20;
        Mat mask = new Mat(diffImg.size(), CV_8UC1);
        for (int j = 0; j < diffImg.rows(); ++j) {
            for (int i = 0; i < diffImg.cols(); ++i) {
                double[] pix = diffImg.get(j, i);
                int value = (int) (pix[0] + pix[1] + pix[2]);
                if (value > threshold) {
                    mask.put(j, i, 255);
                }
            }
        }

        // Maske nehmen und aus dem 2. Bild den maskierten Teil ausschneiden
        Mat outputImg = new Mat();
        Core.bitwise_and(imgTow, imgTow, outputImg, mask);

        // Ergebnisse abspeichern - Maske reicht eigentlich - no need aus Bild ausschneiden
        Imgcodecs.imwrite(outputImagePath, mask);
        
        //Imgcodecs.imwrite("src/test/resources/output_diff_image.jpg", outputImg);
    }

    /**
     * Backgroud subtraction.
     * https://docs.opencv.org/3.4/d1/dc5/tutorial_background_subtraction.html
     * https://answers.opencv.org/question/66084/using-backgroundsubtractormog2-for-images/
     *
     * Probleme:
     * Das ist eine auf Videoframes basierte Lösung. Könnte funktionieren. (Thresholds finden, Frames steuern, Learn reset)
     *
     * @throws IOException the io exception
     */
    @Test
    //@Ignore
    public void backgroundSubtraction() throws IOException {

        String outputImagePath = "src/test/resources/testOutput/diffImage2BinaryImage/backgroundSubtraction.jpg";
        Files.deleteIfExists(Paths.get(outputImagePath));

        // due to really weird android sdk behavior the resources path get  a '/' in front of it.
        Mat imgOne = Imgcodecs.imread(getClass().getClassLoader().getResource("testInput/bordA/input_diffImages2BinaryImage_one.jpg").getPath().substring(1), Imgcodecs.IMREAD_ANYCOLOR);
        Mat imgTow = Imgcodecs.imread(getClass().getClassLoader().getResource("testInput/bordA/input_diffImages2BinaryImage_two.jpg").getPath().substring(1), Imgcodecs.IMREAD_ANYCOLOR);

        BackgroundSubtractor backSub;

        //backSub = Video.createBackgroundSubtractorMOG2();
        //backSub = Video.createBackgroundSubtractorMOG2(1,10d,true);
        //backSub = Video.createBackgroundSubtractorKNN();
        backSub = Video.createBackgroundSubtractorKNN(1,50000d,true);

        Mat foregroundMask = new Mat();
        backSub.apply(imgOne, foregroundMask, 0.5d);
        backSub.apply(imgOne, foregroundMask, 0.5d);
        backSub.apply(imgOne, foregroundMask, 0.5d);
        backSub.apply(imgOne, foregroundMask, 0.5d);

        backSub.apply(imgTow, foregroundMask,0d);

        // Ergebnisse abspeichern
        Imgcodecs.imwrite(outputImagePath, foregroundMask);
    }
}
