package de.hs.stralsund.dartstracker.imagerecognition;

import org.opencv.core.Scalar;

/**
 * These thresholds are all in HSV color range
 */
public enum ColorMask {

    Blue(new Scalar(85, 60, 45), new Scalar(130, 255, 255)),
    Yellow(new Scalar(22, 66, 98), new Scalar(44, 255, 255)),
    YellowNew(new Scalar(23, 75, 90), new Scalar(35, 255, 255)),
    Red(new Scalar(80, 90, 90), new Scalar(180, 255, 190)),
    Green(new Scalar(30, 50, 0), new Scalar(90, 255, 255)),
    Black(new Scalar(0, 30, 30), new Scalar(255, 255, 255));

    final Scalar lowerBorder;
    final Scalar upperBorder;

    ColorMask(Scalar lowerBorder, Scalar upperBorder) {
        this.lowerBorder = lowerBorder;
        this.upperBorder = upperBorder;
    }
}
