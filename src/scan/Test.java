package scan;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Test {

    public static void main(String[] args) {
     
    	BufferedImage image=null;
    	
		try {
			image = ImageIO.read(new File("lena512.bmp"));
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		

    	int[][] coverImage = new int[image.getWidth()][image.getHeight()];
		
	    for (int xPixel = 0; xPixel < image.getWidth(); xPixel++){
	    	for (int yPixel = 0; yPixel < image.getHeight(); yPixel++){
	            	int p = (image.getRGB(xPixel, yPixel));
	            	coverImage[xPixel][yPixel]= (p & 0xFF); //and bit a bit con 255 
	            		
	    	}
	    }
	    
	    
    	
    }
}