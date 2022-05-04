package poc;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;

import javax.imageio.ImageIO;


/**
 * Eine Idee wäre das dartboard_abmessungen.png bei den Farben zu manipulieren.
 * Jedes Farbfeld mit neuen definierten Werten zuweisen siehe enum DartboardFields
 * Dieses kann eingelesen und die RGB Werte dann Pixelweise exkaten Spielwerten zugordnet werden. (Map: Pixel to Spielwert)
 * Trifft ein Weißes Pixel zu, muss wohl die nächst beste Fläche gefunden werden.
 */
public class DartboardMeasure {

    public enum DartboardFields {
        
        OUT(0, 0, 0, -1),
        WHITE(255, 255, 255, 0),
        ONE(10, 10, 10, 1),
        TWO(20, 20, 30, 2),
        THREE(30, 30, 30, 3),
        FOUR(40, 40, 40, 4),
        FIVE(50, 50, 50, 5),
        SIX(60, 60, 60, 6),
        SEVEN(70, 70, 70, 7),
        EIGHT(80, 80, 80, 8),
        NINE(90, 90, 90, 9),
        TEN(100, 100, 100, 10),
        ELEVEN(110, 110, 110, 11),
        TWELVE(120, 120, 120, 12),
        THIRTEEN(130, 130, 130, 13),
        FOURTEEN(140, 140, 140, 14),
        FIFTEEN(150, 150, 150, 15),
        SIXTEEN(160, 160, 160, 16),
        SEVENTEEN(170, 170, 170, 17),
        EIGHTEEN(180, 180, 180, 18),
        NINETEEN(190, 190, 190, 19),
        TWENTY(200, 200, 200, 20),
        // todo double & trippel
        BULL(210, 210, 210, 25),
        BULLS_EYE(220, 220, 220, 50);
        // RGB colors
        final int red;
        final int green;
        final int blue;
        final int dartValue;
        
        DartboardFields(int red, int green, int blue, int dartValue) {
            this.red = red;
            this.green = green;
            this.blue=blue;
            this.dartValue = dartValue;
        }
    }
    
    public static void main(String[] args) {
        try {
            DartboardMeasure dartboardMeasure = new DartboardMeasure();
            dartboardMeasure.readImage();
        }
        catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private void readImage() throws Exception{
        URL svg = getClass().getClassLoader().getResource("dartboard_abmessungen.png");
        BufferedImage image = ImageIO.read(svg);
        // iterate 
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                  int  pixel   = image.getRGB(x, y); 
                  int alpha = (pixel >> 24) & 0xff;
                  int red   = (pixel >> 16) & 0xff;
                  int green = (pixel >>  8) & 0xff;
                  int blue  = (pixel      ) & 0xff;
                  System.out.println("Alpha Color value = "+ alpha);
                  System.out.println("Red Color value = "+ red);
                  System.out.println("Green Color value = "+ green);
                  System.out.println("Blue Color value = "+ blue);
            }
        }
        
    }
}
