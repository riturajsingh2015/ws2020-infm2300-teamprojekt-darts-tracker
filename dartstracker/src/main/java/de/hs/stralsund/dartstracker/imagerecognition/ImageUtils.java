package de.hs.stralsund.dartstracker.imagerecognition;

import androidx.camera.core.ImageProxy;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.imgproc.Moments;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import de.hs.stralsund.dartstracker.dartgame.Tuple;

/**
 * ImageUtils for {@link android.media.Image}
 * and {@link org.opencv.core.Mat}
 */
public class ImageUtils {

    private static Tuple<Mat, Long> lastValidTransformMatrix = null;

    public static Mat ImageToGrayMat(ImageProxy image) {
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        int w = image.getWidth();
        int h = image.getHeight();
        assert (planes[0].getPixelStride() == 1);
        ByteBuffer y_plane = planes[0].getBuffer();
        int y_plane_step = planes[0].getRowStride();
        return new Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step);
    }

    /**
     * copied methods from {@see org.opencv.android.JavaCamera2View}
     *
     * @param image
     * @return
     */
    public static Mat ImageToRgbaMat(ImageProxy image) {
        Mat mRgba = new Mat();
        ImageProxy.PlaneProxy[] planes = image.getPlanes();
        int w = image.getWidth();
        int h = image.getHeight();
        int chromaPixelStride = planes[1].getPixelStride();

        if (chromaPixelStride == 2) { // Chroma channels are interleaved
            assert (planes[0].getPixelStride() == 1);
            assert (planes[2].getPixelStride() == 2);
            ByteBuffer y_plane = planes[0].getBuffer();
            int y_plane_step = planes[0].getRowStride();
            ByteBuffer uv_plane1 = planes[1].getBuffer();
            int uv_plane1_step = planes[1].getRowStride();
            ByteBuffer uv_plane2 = planes[2].getBuffer();
            int uv_plane2_step = planes[2].getRowStride();
            Mat y_mat = new Mat(h, w, CvType.CV_8UC1, y_plane, y_plane_step);
            Mat uv_mat1 = new Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane1, uv_plane1_step);
            Mat uv_mat2 = new Mat(h / 2, w / 2, CvType.CV_8UC2, uv_plane2, uv_plane2_step);
            long addr_diff = uv_mat2.dataAddr() - uv_mat1.dataAddr();
            if (addr_diff > 0) {
                assert (addr_diff == 1);
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat1, mRgba, Imgproc.COLOR_YUV2RGBA_NV12);
            }
            else {
                assert (addr_diff == -1);
                Imgproc.cvtColorTwoPlane(y_mat, uv_mat2, mRgba, Imgproc.COLOR_YUV2RGBA_NV21);
            }
            return mRgba;
        }
        else { // Chroma channels are not interleaved
            byte[] yuv_bytes = new byte[w * (h + h / 2)];
            ByteBuffer y_plane = planes[0].getBuffer();
            ByteBuffer u_plane = planes[1].getBuffer();
            ByteBuffer v_plane = planes[2].getBuffer();

            int yuv_bytes_offset = 0;

            int y_plane_step = planes[0].getRowStride();
            if (y_plane_step == w) {
                y_plane.get(yuv_bytes, 0, w * h);
                yuv_bytes_offset = w * h;
            }
            else {
                int padding = y_plane_step - w;
                for (int i = 0; i < h; i++) {
                    y_plane.get(yuv_bytes, yuv_bytes_offset, w);
                    yuv_bytes_offset += w;
                    if (i < h - 1) {
                        y_plane.position(y_plane.position() + padding);
                    }
                }
                assert (yuv_bytes_offset == w * h);
            }

            int chromaRowStride = planes[1].getRowStride();
            int chromaRowPadding = chromaRowStride - w / 2;

            if (chromaRowPadding == 0) {
                // When the row stride of the chroma channels equals their width, we can copy
                // the entire channels in one go
                u_plane.get(yuv_bytes, yuv_bytes_offset, w * h / 4);
                yuv_bytes_offset += w * h / 4;
                v_plane.get(yuv_bytes, yuv_bytes_offset, w * h / 4);
            }
            else {
                // When not equal, we need to copy the channels row by row
                for (int i = 0; i < h / 2; i++) {
                    u_plane.get(yuv_bytes, yuv_bytes_offset, w / 2);
                    yuv_bytes_offset += w / 2;
                    if (i < h / 2 - 1) {
                        u_plane.position(u_plane.position() + chromaRowPadding);
                    }
                }
                for (int i = 0; i < h / 2; i++) {
                    v_plane.get(yuv_bytes, yuv_bytes_offset, w / 2);
                    yuv_bytes_offset += w / 2;
                    if (i < h / 2 - 1) {
                        v_plane.position(v_plane.position() + chromaRowPadding);
                    }
                }
            }

            Mat yuv_mat = new Mat(h + h / 2, w, CvType.CV_8UC1);
            yuv_mat.put(0, 0, yuv_bytes);
            Imgproc.cvtColor(yuv_mat, mRgba, Imgproc.COLOR_YUV2RGBA_I420, 4);
            return mRgba;
        }
    }

    public static Mat removeBackground(Mat img) {
        Mat dst = new Mat();
        /////////////// Note kar lo Threshold lagana hai toh Imgproc.COLOR_BGR2GRAY he use hoga
        Mat bgi = img;

        Imgproc.medianBlur(img, bgi, 5);

        Mat hsv_img = new Mat();

        Imgproc.cvtColor(bgi, hsv_img, Imgproc.COLOR_BGR2HSV);
        Mat thresh = new Mat();

        Core.inRange(hsv_img, ColorMask.Red.lowerBorder, ColorMask.Red.upperBorder, thresh);
        dst = thresh;

        return dst;
    }

    ////Note removeBackground will be my test function for now later ////
    /// removeBackground1 is the copy of orignal removebackground functions ///
    public static Mat removeBackground1(Mat img) {
        Mat srcGray = new Mat();
        Mat dst = new Mat();
        /////////////// Note kar lo Threshold lagana hai toh Imgproc.COLOR_BGR2GRAY he use hoga
        Imgproc.cvtColor(img, srcGray, Imgproc.COLOR_BGR2GRAY); //convert to gray scale image
        Imgproc.threshold(srcGray, dst, 0, 255, Imgproc.THRESH_BINARY + Imgproc.THRESH_OTSU);
        return dst;
    }

    /**
     * A method to filter out unwanted colors within an image
     * @param img the input image to apply the color mask on
     * @param colorMask the ColorMask defining the colors to leave in
     * @return the input image with those colors filtered out, that are out of border
     */
    public static Mat applyColorMask(Mat img, ColorMask colorMask) {

        Mat hsvImg = new Mat();
        Imgproc.cvtColor(img, hsvImg, Imgproc.COLOR_BGRA2BGR);
        Imgproc.cvtColor(hsvImg, hsvImg, Imgproc.COLOR_BGR2HSV);

        Mat filteredImg = new Mat();
        Core.inRange(hsvImg, colorMask.lowerBorder, colorMask.upperBorder, filteredImg);

        return filteredImg;
    }

    public static Mat mergeMaskRedGreen(Mat img) {

        Mat maskRed = applyColorMask(img, ColorMask.Red);
        Mat maskGreen = applyColorMask(img, ColorMask.Green);
        Mat dst = new Mat();
        Core.bitwise_or(maskRed, maskGreen, dst);
        return dst;
    }

    public static Mat dilateErodeMask(Mat mask, int kernelSize, int elementType) {
        Mat dilatedMask = dilateMask(mask, kernelSize, elementType);
        Mat subsequentlyErodedMask = erodeMask(dilatedMask, kernelSize, elementType);
        return subsequentlyErodedMask;
    }

    public static Mat zero_Multiplier_Region(Mat img) {
        Mat threshold_img = dilateErodeMask(mergeMaskRedGreen(img), 1, Imgproc.CV_SHAPE_RECT);
        ///zero_Multiplier_Region --> Not of Scoring region
        Mat zero_Multiplier_Region = new Mat();
        Core.bitwise_not(scoring_Region(threshold_img), zero_Multiplier_Region);
        return zero_Multiplier_Region;
    }

    public static Mat single_Multiplier_Region(Mat img) {
        Mat threshold_img = dilateErodeMask(mergeMaskRedGreen(img), 1, Imgproc.CV_SHAPE_RECT);
        Mat single_Multiplier_Region = new Mat();

        Core.absdiff(scoring_Region(threshold_img), threshold_img, single_Multiplier_Region);

        return single_Multiplier_Region;
    }

    public static Mat double_Multiplier_Region(Mat img) {
        Mat threshold_img = dilateErodeMask(mergeMaskRedGreen(img), 1, Imgproc.CV_SHAPE_RECT);
        Mat single_Multiplier_Region = single_Multiplier_Region(img);

        Mat double_Multiplier_Region = new Mat();

        Core.absdiff(scoring_Region(threshold_img), floodFill(single_Multiplier_Region), double_Multiplier_Region);

        return double_Multiplier_Region;
    }

    public static Mat triple_Multiplier_Region(Mat img) {
        Mat threshold_img = dilateErodeMask(mergeMaskRedGreen(img), 1, Imgproc.CV_SHAPE_RECT);
        Mat double_Multiplier_Region = double_Multiplier_Region(img);

        Mat M3_outerBull_innerBull_Region = new Mat();
        Core.absdiff(threshold_img, double_Multiplier_Region, M3_outerBull_innerBull_Region);

        Mat x = M3_outerBull_innerBull_Region;

        Mat y = new Mat();
        Mat triple_Multiplier_Region = new Mat();
        Core.absdiff(x, floodFill(x), y);
        Core.absdiff(floodFill(x), floodFill(y), triple_Multiplier_Region);

        return triple_Multiplier_Region;
    }

    public static Mat outer_Bull_Region(Mat img) {
        Mat x = dilateErodeMask(mergeMaskRedGreen(img), 1, Imgproc.CV_SHAPE_RECT);
        Mat y = new Mat();
        Core.bitwise_or(double_Multiplier_Region(img), triple_Multiplier_Region(img), y);
        Core.absdiff(x, y, y);
        Core.absdiff(y, inner_Bull_Region(img), y);
        return y;
    }

    public static Mat inner_Bull_Region(Mat img) {
        Mat threshold_img = dilateErodeMask(
                applyColorMask(img, ColorMask.Green),
                1, Imgproc.CV_SHAPE_RECT);
        Mat x = threshold_img;
        Core.absdiff(floodFill(x), x, x);

        return x;
    }

    public static Mat scoring_Region(Mat img) {
        return floodFill(img);
    }

    public static Mat contourDetection(Mat img) {
        Mat srcGray = new Mat();
        Random rng = new Random(12345);

        Imgproc.cvtColor(img, srcGray, Imgproc.COLOR_BGR2GRAY);
        Imgproc.blur(srcGray, srcGray, new org.opencv.core.Size(3, 3));

        Mat cannyOutput = new Mat();
        Imgproc.Canny(srcGray, cannyOutput, 100, 100 * 2);
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(cannyOutput, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);
        Mat drawing = Mat.zeros(cannyOutput.size(), CvType.CV_8UC3);
        for (int i = 0; i < contours.size(); i++) {
            Scalar color = new Scalar(rng.nextInt(256), rng.nextInt(256), rng.nextInt(256));
            Imgproc.drawContours(drawing, contours, i, color, 2, Imgproc.LINE_8, hierarchy, 0, new Point());
        }

        return drawing;
    }

    public static Mat hough_Lines_Region(Mat img) {
        Mat srcGray = new Mat();
        Mat dst = new Mat(), cdst = new Mat();

        /////////////// Note kar lo Threshold lagana hai toh Imgproc.COLOR_BGR2GRAY he use hoga
        Imgproc.cvtColor(img, srcGray, Imgproc.COLOR_BGR2GRAY); //convert to gray scale image
        //Imgproc.Canny(srcGray, dst, 80, 90);
        Imgproc.Canny(srcGray, dst, 50, 200, 3, false);

        // Copy edges to the images that will display the results in BGR
        Imgproc.cvtColor(dst, cdst, Imgproc.COLOR_GRAY2BGR);

        // Standard Hough Line Transform
        Mat lines = new Mat(); // will hold the results of the detection
        Imgproc.HoughLines(dst, lines, 1, Math.PI / 180, 150); // runs the actual detection


        // Draw the lines
        for (int x = 0; x < lines.rows(); x++) {
            double rho = lines.get(x, 0)[0],
                    theta = lines.get(x, 0)[1];
            double a = Math.cos(theta), b = Math.sin(theta);
            double x0 = a * rho, y0 = b * rho;
            Point pt1 = new Point(Math.round(x0 + 1000 * (-b)), Math.round(y0 + 1000 * (a)));
            Point pt2 = new Point(Math.round(x0 - 1000 * (-b)), Math.round(y0 - 1000 * (a)));
            Imgproc.line(cdst, pt1, pt2, new Scalar(0, 0, 255), 3, Imgproc.LINE_AA, 0);
        }

        return cdst;
    }

    private static Mat floodFill(Mat threshold_img) {
        ///threshold_img ---> image obtained by masking , dilating and eroding the merged mask of red and green
        Mat im_flood_fill = threshold_img.clone();
        Mat mask = new Mat();
        Imgproc.floodFill(im_flood_fill, mask, new Point(0, 0), new Scalar(255));

        // Invert floodfilled image
        Mat im_floodfill_inv = new Mat();
        Core.bitwise_not(im_flood_fill, im_floodfill_inv);
        Mat im_out = new Mat();
        Core.bitwise_or(threshold_img, im_floodfill_inv, im_out);
        return im_out;
    }

    private static Mat dilateMask(Mat mask, int kernelSize, int elementType) {
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        Mat matImgDst = new Mat();
        Imgproc.dilate(mask, matImgDst, element);
        return matImgDst;
    }

    private static Mat erodeMask(Mat mask, int kernelSize, int elementType) {
        Mat element = Imgproc.getStructuringElement(elementType, new Size(2 * kernelSize + 1, 2 * kernelSize + 1),
                new Point(kernelSize, kernelSize));
        Mat matImgDst = new Mat();
        Imgproc.erode(mask, matImgDst, element);
        return matImgDst;
    }

    public static List<Point> findMarkers(Scalar lowerBorder, Scalar higherBorder, Mat img) {

        List<Point> allMarkers = new ArrayList<>();

        // TODO guess we miss used color channels on other methods -  Take care of RGB (default cameras) and BGR (openCV Style)
        Mat hsvImg = convertImage_RGBA_2_HSV(img);

        // Bild nach Gelb segmentieren
        Mat yellowSegmented = new Mat();
        Core.inRange(hsvImg, lowerBorder, higherBorder, yellowSegmented);

        // finde alle Contouren
        List<MatOfPoint> contours = new ArrayList<>();
        Mat hierarchy = new Mat();
        Imgproc.findContours(yellowSegmented, contours, hierarchy, Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        // sammle die größte Konturen raus
        for (MatOfPoint matOfPoint : contours) {
            // Mindestgröße der Kontour
            if (Imgproc.contourArea(matOfPoint) > 20) {
                Moments moments = Imgproc.moments(matOfPoint);
                // calculate center
                int centerX = (int) (moments.m10 / moments.m00);
                int centerY = (int) (moments.m01 / moments.m00);
                allMarkers.add(new Point(centerX, centerY));
            }
        }
        return allMarkers;
    }

    /**
     * Takes in an binary image, detects its contours and removes the smaller ones.
     * @param img the image to modify
     * @param pixelSize the pixel count determining which contours should get deleted
     */
    public static void removeSmallerObjects(Mat img, int pixelSize) {

        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(img, contours, new Mat(), Imgproc.RETR_TREE, Imgproc.CHAIN_APPROX_SIMPLE);

        List<MatOfPoint> contoursToFill = new ArrayList<>();
        for(MatOfPoint contour : contours) {

            if (Imgproc.contourArea(contour) <= pixelSize) {
                contoursToFill.add(contour);
            }
        }

        Imgproc.fillPoly(img, contoursToFill, new Scalar(0, 0, 0));
    }

    public static Mat convertImage_RGBA_2_HSV(Mat img) {

        Mat tempImg = new Mat();

        Imgproc.cvtColor(img, tempImg, Imgproc.COLOR_RGBA2BGR);
        Imgproc.cvtColor(tempImg, tempImg, Imgproc.COLOR_BGR2HSV);
        return tempImg;
    }

    public static Mat convertImage_HSV_2_RGBA(Mat img) {

        Mat tempImg = new Mat();

        Imgproc.cvtColor(img, tempImg, Imgproc.COLOR_HSV2BGR);
        Imgproc.cvtColor(tempImg, tempImg, Imgproc.COLOR_BGR2RGBA);
        return tempImg;
    }

    public static Mat convertImage_HSV_2_BGRA(Mat img) {

        Mat tempImg = new Mat();

        Imgproc.cvtColor(img, tempImg, Imgproc.COLOR_HSV2BGR);
        Imgproc.cvtColor(tempImg, tempImg, Imgproc.COLOR_BGR2BGRA);
        return tempImg;
    }

    public static List<Point> findMarkers(Mat img, ColorMask colorMask) {
        return findMarkers(colorMask.lowerBorder, colorMask.upperBorder, img);
    }

    public static Mat drawYellowMarkers(List<Point> allMarkers, Mat img) {

        for(Point marker : allMarkers) {
            // draw a cirle TODO make them yellow
            Imgproc.circle(img, new Point(marker.x, marker.y), 5, new Scalar(255, 255, 255), -1);
            Imgproc.putText(img, "Marker", new Point(marker.x - 20, marker.y - 20), Imgproc.FONT_HERSHEY_SIMPLEX, 1.5, new Scalar(255, 255, 255), 3);
        }
        return img;
    }

    public static Mat findAndDrawMarkers(Mat img, ColorMask colorMask) {

        List<Point> allMarkers = findMarkers(img, colorMask);
        return drawYellowMarkers(allMarkers, img);
    }

    public static Mat rectifyImageOnYellowMarkers(Mat img) {
        return rectifyImageOnYellowMarkers(findMarkers(img, ColorMask.YellowNew), img);
    }

    public static Mat getTransformMatrixFromMarkers(List<Point> allMarkers, Mat img) {
        if (allMarkers.size() == 4) {
            // perspective transformation
            /*
              order points to fit the target points
              oben = lowest Y
              links = lowest X
              rechts = highest X
              unten = highest Y
             */
            // TODO can't use java streams due to low api level
            Collections.sort(allMarkers, (o1, o2) -> Double.compare(o1.y, o2.y));
            Point oben = allMarkers.get(0);
            Point unten = allMarkers.get(3);
            Collections.sort(allMarkers, (o1, o2) -> Double.compare(o1.x, o2.x));
            Point links = allMarkers.get(0);
            Point rechts = allMarkers.get(3);

            MatOfPoint2f srcPoints = new MatOfPoint2f(
                    oben,
                    links,
                    rechts,
                    unten
            );
            Mat transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, ImageCalibration.destPoints);
            lastValidTransformMatrix = new Tuple<>(transformMatrix, System.currentTimeMillis());
            return transformMatrix;
        }
        else if (lastValidTransformMatrix != null && System.currentTimeMillis() - lastValidTransformMatrix.b < 15000) {
            // todo find better validation for keeping matrix up
            return lastValidTransformMatrix.a;
        }

        return null;
    }

    public static Mat rectifyImageOnYellowMarkers(List<Point> allMarkers, Mat img) {

        // build it better. != 4 will "jump" a lot between rectified and not rectified
        if (allMarkers.size() == 4) {
            // perspective transformation
            /*
              order points to fit the target points
              oben = lowest Y
              links = lowest X
              rechts = highest X
              unten = highest Y
             */
            // TODO can't use java streams due to low api level
            Collections.sort(allMarkers, (o1, o2) -> Double.compare(o1.y, o2.y));
            Point oben = allMarkers.get(0);
            Point unten = allMarkers.get(3);
            Collections.sort(allMarkers, (o1, o2) -> Double.compare(o1.x, o2.x));
            Point links = allMarkers.get(0);
            Point rechts = allMarkers.get(3);

            MatOfPoint2f srcPoints = new MatOfPoint2f(
                    oben,
                    links,
                    rechts,
                    unten
            );

            Mat transformMatrix = Imgproc.getPerspectiveTransform(srcPoints, ImageCalibration.destPoints);

            Mat rectifiedImg = new Mat(); // target size of svg board
            Imgproc.warpPerspective(img, rectifiedImg, transformMatrix, new Size(1701, 1701));
            lastValidTransformMatrix = new Tuple<>(transformMatrix, System.currentTimeMillis());
            return rectifiedImg;
        }
        else if (lastValidTransformMatrix != null && System.currentTimeMillis() - lastValidTransformMatrix.b < 2000) {
            // using transfomration which is max 2 seconds old
            // todo check übrige punkte
            Mat rectifiedImg = new Mat();
            Imgproc.warpPerspective(img, rectifiedImg, lastValidTransformMatrix.a, new Size(1701, 1701));
            return rectifiedImg;
        }
        else {
            lastValidTransformMatrix = null;
        }
        return img;
    }

    /**
     * Takes an image and widens out its outer borders.
     * In order to expand it uses the pixel at the given border and stretches it outwards
     *
     * @param img the image to expand
     * @param expansion how far the border should get expanded
     */
    public static void expandBorder(Mat img, int expansion) {
        Core.copyMakeBorder(img, img, expansion, expansion, expansion, expansion, Core.BORDER_REPLICATE);
    }

    public static Mat matRotate(Mat matOrg, int rotationDegrees) {
        Mat mat = new Mat(matOrg.cols(), matOrg.rows(), matOrg.type());
        switch (rotationDegrees) {
            default:
                // won't work with Surface.ROTATION_0
            case 0:
                // it´s already good enough
                break;
            case 90:
                Core.transpose(matOrg, mat);
                Core.flip(mat, mat, 1);
                break;
            case 180:
                mat = matOrg;
                Core.flip(mat, mat, -1);
                break;
            case 270:
                mat = matOrg;
                Core.transpose(matOrg, mat);
                Core.flip(mat, mat, 1);
                break;
        }
        return mat;
    }

    /**
     * Compares two images and detect the different pixel.
     * @return an image visualising the different pixel and the intensity of variety.
     */
    public static Mat detectImageDifference(Mat firstImage, Mat secondImage, int blur) {

        Imgproc.resize(secondImage, secondImage, firstImage.size());

        Imgproc.blur(firstImage, firstImage, new Size(blur, blur));
        Imgproc.blur(secondImage, secondImage, new Size(blur, blur));

        Mat imageDifference = new Mat();
        Core.absdiff(firstImage, secondImage, imageDifference);

        return imageDifference;
    }

    /**
     *
     * @param image
     * @return Point of the dart tip
     */
    public static Point findAndMarkDartTip(Mat image){
        Point dartTip = null;

        // color segmentation blue dart
        Mat blueMask = new Mat();
        Mat hsvImg = convertImage_RGBA_2_HSV(image);
        Core.inRange(hsvImg, ColorMask.Blue.lowerBorder, ColorMask.Blue.upperBorder, blueMask);

        // search for countours in blue mask
        List<MatOfPoint> contours = new ArrayList<>();
        Imgproc.findContours(blueMask, contours, new Mat(), Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);

        // sammle die größte Konturen raus
        for (MatOfPoint matOfPoint : contours) {
            // Mindestgröße der Kontour
            if(Imgproc.contourArea(matOfPoint) > 400){
                // Zeichnen die ergebnisse auf den Frame
                Imgproc.drawContours(image, Arrays.asList(matOfPoint), -1, new Scalar(0, 255, 255), 1);

                Moments moments = Imgproc.moments(matOfPoint);
                // calculate center and draw cirle + text
                int massCentroidX = (int) (moments.m10 / moments.m00);
                int massCentroidY = (int) (moments.m01 / moments.m00);

                // find farest point from massCentroid
                double distance = 0d;
                dartTip=new Point(massCentroidX, massCentroidY);
                for (Point contourPoint : matOfPoint.toList()) {

                    double temp = PointsFinder.calculateDistance(massCentroidX,massCentroidY, contourPoint.x, contourPoint.y);
                    if(temp > distance){
                        distance = temp;
                        dartTip = contourPoint; // the furthest point from mass centroid is the dart tip
                    }
                }
                // Zeichen den entferntestes Punkt in den Frame
                Imgproc.circle(image, dartTip, 5, new Scalar(0, 0, 255), 2);

                break;
            }
        }
        return dartTip;
    }

    // possibility to call native cpp libs/code
    public native void FindFeatures(long matAddrGr, long matAddrRgba);


}
