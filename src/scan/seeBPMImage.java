package scan;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class seeBPMImage {
	static HashMap<String, String> scannedPixel = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {

		//BufferedImage image = ImageIO.read(new File("lena_gray.bmp"));
		BufferedImage image = ImageIO.read(new File("lena512.bmp"));

		int[][] array2D = new int[image.getWidth()][image.getHeight()];
		int x = 0,y = 0;
		for (int yPixel = 0; yPixel < image.getWidth(); yPixel++, x++){
			y=0;
			for (int xPixel = 0; xPixel < image.getHeight(); xPixel++, y++) {      

				int color = image.getRGB(xPixel, yPixel);
				//System.out.println("Pixel [" + xPixel + "," + yPixel + "]: " + (color & 0xFF) );	
				array2D[x][y] = color & 0xFF;

			}
		}
		
		ArrayList<Block> blocks = getBlocks(array2D, 8);
		Pixel lastPixel = null;
		for(int i=0; i<blocks.size();i++){

			BestPathOutput bpo = BestPath(array2D, blocks.get(0), 8, lastPixel);
			lastPixel = bpo.lastPixel;
		}
		
		
		//ScanPaths s = new ScanPaths();
	    //Path pathC0 = s.C0(array2D, blocks.get(0));
		//s.O0(array2D, blocks.get(0));


		/*BufferedImage image2;
        for(int i=0; i<array2D.length; i++) {
            for(int j=0; j<array2D.length; j++) {
                int a = array2D[i][j];
                Color newColor = new Color(a,a,a);
                image.setRGB(j,i,newColor.getRGB());
            }
        }
        File output = new File("GrayScale.jpg");
        ImageIO.write(image, "jpg", output);*/
	}



	//La matrice in input deve essere già quadrata multiplo di N, eventualmente il controllo sulle dimensioni lo faremo prima
	public static ArrayList<Block> getBlocks (int matrix[][], int N){
		int len = matrix.length;
		ArrayList<Block> blocks = new ArrayList<Block>();

		//# di blocchi che entrano in larghezza e/o in altezza (dato che la matrice è quadrata)
		int numOfBlockPerLine = len/N;
		for(int i=1; i <= numOfBlockPerLine; i++){
			if(i%2 != 0){ //scorro da destra a sinistra
				for(int j=1; j<= numOfBlockPerLine; j++){
					int xStart = (len-1)-((N*j)-1);
					int xEnd = (len-1)-(N*(j-1));
					int yStart = (N*(i-1));
					int yEnd = (N*i)-1;

					blocks.add(new Block(xStart, xEnd, yStart, yEnd));
				}
			}
			else{ //else scorro da sinistra a destra
				for(int j=numOfBlockPerLine; j>=1; j--){
					int xStart = (len-1)-((N*j)-1);
					int xEnd = (len-1)-(N*(j-1));
					int yStart = (N*(i-1));
					int yEnd = (N*i)-1;

					blocks.add(new Block(xStart, xEnd, yStart, yEnd));
				}
			}
		}
		
		for(int i=0; i<blocks.size(); i++){
			Block b = blocks.get(i);
			//System.out.println("Blocco: xStart="+b.xStart+"; xEnd="+b.xEnd+"; yStart="+b.yStart+"; yEnd="+b.yEnd);
		}
		
		return blocks;
	}
	
	
	public static BestPathOutput BestPath(int matrix[][], Block b, int BlockSize, Pixel PrevLastPixel){
		BestPathOutput bpo = null;
		
		ScanPaths s = new ScanPaths();
		Path path = s.C0(matrix, b);
		
		BlockError(matrix, path, PrevLastPixel);
		
		
		bpo.lastPixel = path.path.get( path.path.size()-1 ); 
		return bpo;
	}
	
	public static BlockErrorOutput BlockError(int matrix[][], Path path, Pixel PrevLastPixel){
		BlockErrorOutput beo = null;
		
		Pixel pixel, prevPixel = null; //prevPixel sarebbe il nostro pixel s
		Character LastHorizontalMove = null;
		Character LastVerticalMove = null;
		
		ArrayList<Integer> L = new ArrayList<Integer>(); //Sequence L of prediction errors along P
		
		for(int i=0; i < path.size(); i++){
			pixel=path.getPixel(i);
			//vado a vedere se è il primo pixel scansionato dell'immagine
			//sarà sempre quello in alto a destra
			if(pixel.x == (matrix.length -1) && pixel.y == 0){//TO-DO sistemare il controllo
				L.add(0);
				prevPixel=pixel;
				scannedPixel.put(pixel.x+"-"+pixel.y, null); //aggiungo il pixel alla mappa nella forma "511-0"
				continue;
			}
			else if(i == 0){ //Come faccio a capire la direzione (UL/UR/BL/BR) 
							//al primo pixel? Vedo l'angolo di partenza
				String startAngle = path.getStartAngle();
				
				if(startAngle.equals("NE")){//UR {N,E}
					if((pixel.y - 1) >= 0  && (pixel.x + 1) < matrix.length){ //vedo se il pixel a Nord e ad Est non escono fuori dalla matrice
						int q = matrix[pixel.x][pixel.y-1];
						int r = matrix[pixel.x+1][pixel.y];
						int p =  matrix[pixel.x][pixel.y];
						//vedo se i pixel q ed r sono stati già scansionati
						if(scannedPixel.containsKey(pixel.x+"-"+ (pixel.y-1)) && scannedPixel.containsKey((pixel.x+1) +"-"+pixel.y)){
							int e = p -(q/r)/2;
							L.add(e);
						}
						else{
							
						}
						
					}
				}
				else if(startAngle.equals("NW")){//UL {N,W}
					
				}
				else if(startAngle.equals("SW")){//BL {S,W}
					
				}
				else if(startAngle.equals("SE")){//BR {S,E}
					
				}
				
			}
			
			
			
			
			
		}
		
				
		return beo;
	}
		
}


