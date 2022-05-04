package de.hs.stralsund.dartstracker.imagerecognition;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

public class ColoredDartTracker {

    private static int getScoredPoints(Mat frame) {

        Mat hsvFrame = new Mat();
        Imgproc.cvtColor(frame, hsvFrame, Imgproc.COLOR_BGR2HSV);

        List<MatOfPoint> contours = filterAndSortContours(findContours(hsvFrame, ColorMask.Blue), 400);
        if (contours.isEmpty()) {
            return -1;
        }

        MatOfPoint biggestContour = contours.get(0);
        Imgproc.drawContours(frame, Collections.singletonList(biggestContour), -1, new Scalar(0, 255, 255), 1);

        Point furthestPoint = getFurthestPoint(biggestContour, extractPixelMassCentroid(biggestContour));
        Imgproc.circle(frame, furthestPoint, 3, new Scalar(0, 0, 255), 2); // Zeichen den entferntestes Punkt in den Frame

        int scoredPoints = calculateScoredPoints(furthestPoint);
        putDartValueOnFrame(frame, scoredPoints);

        return 0;
    }

    private static void putDartValueOnFrame(Mat frame, int number) {

        Imgproc.rectangle(frame, new Point(110, 2), new Point(150, 20), new Scalar(255, 255, 255), -1);
        Imgproc.putText(frame, String.valueOf(number), new Point(115, 15), Imgproc.FONT_HERSHEY_SIMPLEX, 0.5, new Scalar(0, 0, 0));
    }

    private static int calculateScoredPoints(Point furthestPoint) {

        MatOfPoint2f transformedPoint = new MatOfPoint2f(furthestPoint);
//        Core.perspectiveTransform(transformedPoint, transformedPoint, transformMatrix);
        PointsFinder pointsFinder2 = new PointsFinder(new Point(851,851), 642);
        return pointsFinder2.getPointsFromXYCoordinates((int)Math.round(transformedPoint.get(0, 0)[0]), (int)Math.round(transformedPoint.get(0, 0)[1]));
    }

    private static Point getFurthestPoint(MatOfPoint contour, Point referencePoint) {

        double distance = 0d;
        Point furthestPoint = referencePoint;
        for (Point contourPoint : contour.toList()) {

            double currentDistance = pointDistance(referencePoint, contourPoint);
            if(currentDistance > distance) {

                distance = currentDistance;
                furthestPoint = contourPoint;
            }
        }

        return furthestPoint;
    }

    private static double pointDistance(Point a, Point b) {

        try {
            return Math.sqrt((b.x - a.x) * (b.x - a.x) + (b.y - a.y) * (b.y - a.y));
        } catch (Exception e){
            return -1;
        }
    }

    private static Point extractPixelMassCentroid(MatOfPoint biggestContour) {

        Moments moments = Imgproc.moments(biggestContour);
        return new Point((int) (moments.m10 / moments.m00), (int) (moments.m01 / moments.m00));
    }

    private static ArrayList<MatOfPoint> filterAndSortContours(List<MatOfPoint> contours, int minPixelCount) {

        // contours.removeIf(contour -> Imgproc.contourArea(contour) > minPixelCount); auf höherem Api Level ;p
        // contours.sort((o1, o2) -> Imgproc.contourArea(o1) > Imgproc.contourArea(o2) ? 0 : 1); auf höherem Api Level ;p

        Set<MatOfPoint> filteredContours = new TreeSet<>((contour1, contour2) -> Imgproc.contourArea(contour1) > Imgproc.contourArea(contour2) ? 0 : 1);
        for (MatOfPoint contour : contours) {

            if (Imgproc.contourArea(contour) > minPixelCount) {
                filteredContours.add(contour);
            }
        }

        return new ArrayList<>(filteredContours);
    }

    private static List<MatOfPoint> findContours(Mat hsvFrame, ColorMask colorMask) {

        // color segmentation blue dart
        Mat blueMask = new Mat();
        Core.inRange(hsvFrame, colorMask.lowerBorder, colorMask.upperBorder, blueMask);

        // search for countours in blue mask
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(blueMask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
        return contours;
    }
}
