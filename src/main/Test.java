package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;


public class Test {

	public static void main(String[] args) throws IOException {
		
		
		// RGB value
		BufferedImage image2 = ImageIO.read(new File("lena512.bmp"));
		
	    for (int xPixel = 0; xPixel < 100; xPixel++){
	    	for (int yPixel = 0; yPixel < 100; yPixel++){
	            	int p = (image2.getRGB(xPixel, yPixel));
	            	System.out.println(new Color(p));
	            	System.out.println("Pixel["+xPixel+"]["+yPixel+"]="+(p & 0xFF)); //and bit a bit con 255 
	            		
	    	}
	    }
	
	
	}

}
