package poc;
import java.awt.geom.Point2D;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.highgui.HighGui;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;
import org.opencv.video.BackgroundSubtractor;
import org.opencv.video.Video;
import org.opencv.videoio.VideoCapture;
import org.opencv.videoio.Videoio;

class BlueDartTracking {
    
    private static final MatOfPoint2f destPoints;
    
    static {
        /*
        programatically load opencv native lib
        you need to put the correct open-cv nativ lib for your actual plattform in test/resources to run this test
        for me it was windows x64 'opencv\build\java\x64'
        NOT Android lib
         */
        System.load(BlueDartTracking.class.getClassLoader().getResource("opencv_java452_windowsx64.dll").getPath());
        
        destPoints = new MatOfPoint2f(
                               new Point(750, 220), // oben
                               new Point(220, 750), // links
                               new Point(1480, 750), // rechts
                               new Point(750, 1480)); // unten
    }
    
    public static void main(String[] args) {
        
        // there is a bug when audio is included in mp4 videos on windows systems with java
        VideoCapture capture = new VideoCapture(BlueDartTracking.class.getClassLoader().getResource("20210610_155953_without_Audio.mp4").getPath());
        if (!capture.isOpened()) {
            System.err.println("Unable to open video.");
            System.exit(0);
        }
        Mat frame = new Mat(), blueMask = new Mat(), yellowMask = new Mat();;
        while (true) {
            capture.read(frame);
            if (frame.empty()) {
                break;
            }

            putFrameNumberOnFrame(frame, (int)capture.get(Videoio.CAP_PROP_POS_FRAMES));
            
            // get a hsv color frame 
            Mat hsvFrame = new Mat();
            Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);

            findAndMarkDart(frame, hsvFrame);
            findAndMarkYellowMarkers(frame, hsvFrame);


            // need scaling just for windows
            Imgproc.resize(frame, frame, new Size(506, 900));
            HighGui.imshow("Frame", frame);


            // get the input from the keyboard
            int keyboard = HighGui.waitKey(30);
            if (keyboard == 'q' || keyboard == 27) {
                break;
            }
        }

        HighGui.destroyAllWindows();
        System.exit(0);
    }

    static Mat transformMatrix;
    private static void findAndMarkYellowMarkers(Mat frame, Mat hsvFrame) {
        // TODO Auto-generated method stub
        Mat yellowMask = new Mat();
        Core.inRange(hsvFrame, ColorMask.YellowNew.lowerBorder, ColorMask.YellowNew.upperBorder, yellowMask);
        HighGui.imshow("yell Mask", yellowMask);
        // finde alle Contouren
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(yellowMask, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // sammle die größte Konturen raus
        List<Point> allMarkers = new ArrayList<>();
        for (MatOfPoint matOfPoint : contours) {
            // Mindestgröße der Kontour
            if (Imgproc.contourArea(matOfPoint) > 3) {
                Moments moments = Imgproc.moments(matOfPoint);
                // calculate center
                int centerX = (int) (moments.m10 / moments.m00);
                int centerY = (int) (moments.m01 / moments.m00);
                allMarkers.add(new Point(centerX, centerY));
            }
        }
        if (allMarkers.size() == 4) {
            Collections.sort(allMarkers, (o1, o2) -> Double.compare(o1.y, o2.y));
            Point oben = allMarkers.get(0);
            Point unten = allMarkers.get(3);
            Collections.sort(allMarkers, (o1, o2) -> Double.compare(o1.x, o2.x));
            Point links = allMarkers.get(0);
            Point rechts = allMarkers.get(3);

            MatOfPoint2f srcPoints = new MatOfPoint2f(oben, links, rechts, unten);
            transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, destPoints);
        }
    }

    /**
     * 
     * @param frame
     * @param hsvFrame 
     */
    private static void findAndMarkDart(Mat frame, Mat hsvFrame){
        // color segmentation blue dart
        Mat blueMask = new Mat();
        Core.inRange(hsvFrame, ColorMask.Blue.lowerBorder, ColorMask.Blue.upperBorder, blueMask);
        
        // show blue mask
        HighGui.imshow("FG Mask", blueMask);
        
        // search for countours in blue mask
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(blueMask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
       
        int dartvalue = 0;
        
        // sammle die größte Konturen raus
        for (MatOfPoint matOfPoint : contours) {
            // Mindestgröße der Kontour
            if(Imgproc.contourArea(matOfPoint) > 400){
                // Zeichnen die ergebnisse auf den Frame
                Imgproc.drawContours(frame, Arrays.asList(matOfPoint), -1, new Scalar(0, 255, 255), 1);

                Moments moments = Imgproc.moments(matOfPoint);
                // calculate center and draw cirle + text
                int massCentroidX = (int) (moments.m10 / moments.m00);
                int massCentroidY = (int) (moments.m01 / moments.m00);

                // find farest point from massCentroid
                double distance = 0d;
                Point farestPoint=new Point(massCentroidX, massCentroidY);
                for (Point contourPoint : matOfPoint.toList()) {
                    double temp = Point2D.distance(massCentroidX, massCentroidY, contourPoint.x, contourPoint.y);
                    if(temp > distance){
                        distance = temp;
                        farestPoint = contourPoint;
                    }
                }
                // Zeichen den entferntestes Punkt in den Frame
                Imgproc.circle(frame, farestPoint, 3, new Scalar(0, 0, 255), 2);
                
                // calculate point
                if(transformMatrix != null){
                    MatOfPoint2f transformedPoint = new MatOfPoint2f(farestPoint);
                    Core.perspectiveTransform(transformedPoint, transformedPoint, transformMatrix);
                    PointsFinder2 pointsFinder2 = new PointsFinder2(new Point(851,851), 642);
                    dartvalue = pointsFinder2.getPointsFromXYCoordinates((int)Math.round(transformedPoint.get(0, 0)[0]), (int)Math.round(transformedPoint.get(0, 0)[1]));
                }
                break;
            }
        }
        putDartValueOnFrame(frame, dartvalue);
    }
    
    /**
     * 
     */
    private static void putDartValueOnFrame(Mat frame, int number){
        Imgproc.rectangle(frame, new Point(110, 2), new Point(150, 20), new Scalar(255, 255, 255), -1);
        String numberString = String.format("%d", number);
        Imgproc.putText(frame, numberString, new Point(115, 15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));
    }
    
    /**
     * 
     */
    private static void putFrameNumberOnFrame(Mat frame, int number){
        Imgproc.rectangle(frame, new Point(10, 2), new Point(100, 20), new Scalar(255, 255, 255), -1);
        String frameNumberString = String.format("%d", number);
        Imgproc.putText(frame, frameNumberString, new Point(15, 15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5,
                new Scalar(0, 0, 0));
    }
    
    
}