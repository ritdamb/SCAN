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

        //BufferedImage image = ImageIO.read(new File("lena_gray.bmp"));
        BufferedImage image = ImageIO.read(new File("lena512.bmp"));
        
        int[][] array2D = new int[image.getWidth()][image.getHeight()];
        int x = 0,y = 0;
        for (int yPixel = 0; yPixel < image.getHeight(); yPixel++, x++){
        	y=0;
        	for (int xPixel = 0; xPixel < image.getWidth(); xPixel++, y++) {      

                    int color = image.getRGB(xPixel, yPixel);
                    System.out.println("Pixel [" + xPixel + "," + yPixel + "]: " + (color & 0xFF) );	
                    array2D[x][y] = color & 0xFF;
                    
            }
        }
        
        BufferedImage image2;
        for(int i=0; i<array2D.length; i++) {
            for(int j=0; j<array2D.length; j++) {
                int a = array2D[i][j];
                Color newColor = new Color(a,a,a);
                image.setRGB(j,i,newColor.getRGB());
            }
        }
        File output = new File("GrayScale.jpg");
        ImageIO.write(image, "jpg", output);
    }
}
