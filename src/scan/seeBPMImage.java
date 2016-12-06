package scan;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;

public class seeBPMImage {


    public static void main(String[] args) throws IOException {

        BufferedImage image = ImageIO.read(new File("lena_gray.bmp"));
        //BufferedImage image = ImageIO.read(new File("lena512.bmp"));
        
        int[][] array2D = new int[image.getWidth()][image.getHeight()];
        for (int xPixel = 0; xPixel < image.getWidth(); xPixel++) {
            for (int yPixel = 0; yPixel < image.getHeight(); yPixel++) {      

                    int color = image.getRGB(xPixel, yPixel);
                    System.out.println("Pixel [" + xPixel + "," + yPixel + "]: " + (color & 0xFF) );	
                    array2D[xPixel][yPixel] = color & 0xFF;
                    
            }
        }
    }
}
