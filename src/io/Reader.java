package io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import javax.imageio.ImageIO;

import main.CompressSCAN;
import model.Block;
import model.Path;
import model.Pixel;
import scanpaths.ConstantsScan;
import scanpaths.ScanPaths;

public class Reader {
	
	HashMap<String, String> scannedPixel; 
	
	public Reader() {
		super();
		scannedPixel = new HashMap<String, String>();
	}

	public void ReadImage(String path) throws IOException{
		BitInputStream in = new BitInputStream(new FileInputStream(path));

		String inputBits = "";

		int i = in.read();
		while(i != -1 ){
			inputBits += i;
			i = in.read();
		}

		//Leggo i primi tre bit per calcolarmi la dimensione:
		int n = Integer.parseInt(inputBits.substring(0, 3), 2)+2;
		int size = (int) Math.pow(2, n);
		System.out.println("size "+ size);

		//mi calcolo il numero di blocchi 32*32 da leggere (alcuni potranno anche non essere basic)
		int blocksToRead = (int) Math.pow(size/32, 2);
		int index = 3;
		ArrayList<String> scanPathTypes = new ArrayList<String>();
		
		while(blocksToRead > 0){
			char c = inputBits.charAt(index++);
			if(c=='0'){ //è un basic scanpath allora leggo 4 bit
				String k = "" + inputBits.charAt(index++);
				k += inputBits.charAt(index++);
				String t = "" + inputBits.charAt(index++);
				t += inputBits.charAt(index++);

				String KT = "";
				if(k.equals("00"))
					KT = "C";
				else if(k.equals("01"))
					KT = "D";
				else if(k.equals("10"))
					KT = "O";
				else if(k.equals("11"))
					KT = "S";

				if(t.equals("00"))
					KT += "0";
				else if(t.equals("01"))
					KT += "1";
				else if(t.equals("10"))
					KT += "2";
				else if(t.equals("11"))
					KT += "3";

				blocksToRead--;
				//System.out.println(KT);
				scanPathTypes.add(KT);

			}
			else{ //è uno scanpath composto quindi dovrò leggerne 4*4 = 16 bit
				String compositePath="";
				//System.out.print("(");
				for(i=0; i<4; i++){
					String k = "" + inputBits.charAt(index++);
					k += inputBits.charAt(index++);
					String t = "" + inputBits.charAt(index++);
					t += inputBits.charAt(index++);
					
					String KT;
					if(i==0)
						KT = "";
					else
						KT = ","; 
						
					if(k.equals("00"))
						KT += "C";
					else if(k.equals("01"))
						KT += "D";
					else if(k.equals("10"))
						KT += "O";
					else if(k.equals("11"))
						KT += "S";

					if(t.equals("00"))
						KT += "0";
					else if(t.equals("01"))
						KT += "1";
					else if(t.equals("10"))
						KT += "2";
					else if(t.equals("11"))
						KT += "3";
					
					compositePath += KT;
					//System.out.print(KT);	
				}
				//System.out.println(")");
				blocksToRead--;
				scanPathTypes.add(compositePath);
			}
		}
		
		for (String p : scanPathTypes) {
			System.out.println(p);
		}
		
		//A questo punto abbiamo 2 byte (16 bit)che corrispondono ai primi due pixel dell'immagine
		String pixel = inputBits.substring(index, index+8);
		index = index+8;
		int pixel1 = Integer.parseInt(pixel, 2);
		
		pixel = inputBits.substring(index, index+8);
		index = index+8;
		int pixel2 = Integer.parseInt(pixel, 2);
		
		System.out.println("pixel1="+pixel1);
		System.out.println("pixel2="+pixel2);
		
		int[][] matrix = new int[size][size];
		for (int[] row: matrix)
		 Arrays.fill(row, 200);
		
		ArrayList<Block> blocks = Block.getBlocks(matrix, ConstantsScan.blockSize);
		//a questo punto ho due ArrayList blocks e scanPaths della stessa size
		
		ScanPaths sp = new ScanPaths();
		ArrayList<Path> pathSequence = new ArrayList<Path>();
		
		Path prevPath = null;
		for (int z = 0; z < scanPathTypes.size(); z++){
			if(scanPathTypes.get(z).length() == 2){
				Path b_path = sp.scanPath(matrix, blocks.get(z), scanPathTypes.get(z));
				b_path.setPreviousPath(prevPath);
				pathSequence.add(b_path);
				prevPath = b_path;
			}
			else{ //è un path composto
				Block[] blks = Block.splitBlock(blocks.get(z));
				String subPaths[] =  scanPathTypes.get(z).split(",");
				for(int y=0; y<4; y++){
					Path b_path = sp.scanPath(matrix, blks[y], subPaths[y]);
					b_path.setPreviousPath(prevPath);
					pathSequence.add(b_path);
					prevPath = b_path;
				}
			}
		}
		
		//OK A QUESTO PUNTO HO LA LISTA DI PATH IN ORDINE SULLA MATRICE
		//MI MANCANO LE SEQUENZE DI CONTESTI
		//E POI BISOGNA RICOSTRUIRE L'IMMAGINE
		
		//PEZZA
		ArrayList<ArrayList<Integer>> buffersList = CompressSCAN.getBuffersList();
		System.out.println(buffersList.get(0).toString());
		System.out.println(buffersList.get(1).toString());
		System.out.println(buffersList.get(2).toString());
		System.out.println(buffersList.get(3).toString());
		
		int index0 = 2;
		int index1 = 0;
		int index2 = 0;
		int index3 = 0;
		int v = pixel1;
		int u = pixel2;
		
		
		try {
			for (int a=0; a < pathSequence.size(); a++){
				ArrayList<Pixel> p = pathSequence.get(a).getPath();
				Pixel actualPixel;
				int b = 0;
				if(a == 0){
					matrix[p.get(0).x][p.get(0).y] = pixel1;
					matrix[p.get(1).x][p.get(1).y] = pixel2;
					scannedPixel.put(p.get(0).x + "-" + p.get(0).y, null);
					scannedPixel.put(p.get(1).x + "-" + p.get(1).y, null);
					b = 2;
					
				}
				for ( ;b < p.size(); b++){
					int[] neighbors;
					actualPixel = p.get(b);
					int predErr, e;
					if( (neighbors = contextNeighbors(actualPixel, matrix)) != null)
						e = (Math.abs(neighbors[0] - neighbors[1]) + Math.abs(neighbors[1] - neighbors[2])) / 2;
					else
						e = Math.abs(u-v);
					
					if (e >= 0 && e <= 2)
						predErr = buffersList.get(0).get(index0++);
					else if (e >= 3 && e <= 8)
						predErr = buffersList.get(1).get(index1++);
					else if (e >= 9 && e <= 15)
						predErr = buffersList.get(2).get(index2++);
					else
						predErr = buffersList.get(3).get(index3++);
						
					int[] predictionNeighbors = predictionNeighbors(actualPixel, matrix);
					int pixelVal;
					if(predictionNeighbors != null)
						pixelVal = predErr + (predictionNeighbors[0] +  predictionNeighbors[1])/2;
					else
						pixelVal = predErr + u;
					if(pixelVal < 0){
						//System.out.println(predictionNeighbors[0] + " - " + predictionNeighbors[1]);
						System.out.println("PredErr="+predErr);
					}
					
					matrix[actualPixel.x][actualPixel.y] = pixelVal;
					
					v = u;
					u = pixelVal;
					scannedPixel.put(actualPixel.x + "-" + actualPixel.y, null);
						
				}
				
			}
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
		
		BufferedImage outputImage = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
		
		for(int k=0; k<size; k++) {
            for(int j=0; j<size; j++) {
                int a = matrix[k][j];
                if(a > 255 || a < 0)
                	System.out.println("pixelVal="+a);

                Color newColor = new Color(a, a, a);
                outputImage.setRGB(j,k,newColor.getRGB());
            }
        }
        File output = new File("output.bmp");
        ImageIO.write(outputImage, "bmp", output);


	}
	
	
	private int[] contextNeighbors(Pixel p, int matrix[][]){
		int[] neighbors = new int[3]; 
		if(p.getPredictor().equals("UR")){
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(-1, 1);
			Pixel s = p.transform(0, 1);
			if(scannedPixel.containsKey(q.x + "-" + q.y) && 
					scannedPixel.containsKey(r.x + "-" + r.y) && 
					scannedPixel.containsKey(s.x + "-" + s.y)
					){
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
				neighbors[2] = matrix[s.x][s.y];
			}
			else
				return null;
		}
		else if(p.getPredictor().equals("UL")){
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(-1, -1);
			Pixel s = p.transform(0, -1);
			if(scannedPixel.containsKey(q.x + "-" + q.y) && 
					scannedPixel.containsKey(r.x + "-" + r.y) && 
					scannedPixel.containsKey(s.x + "-" + s.y)
					){
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
				neighbors[2] = matrix[s.x][s.y];
			}
			else
				return null;
		}
		else if(p.getPredictor().equals("BR")){
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(1, 1);
			Pixel s = p.transform(0, 1);
			if(scannedPixel.containsKey(q.x + "-" + q.y) && 
					scannedPixel.containsKey(r.x + "-" + r.y) && 
					scannedPixel.containsKey(s.x + "-" + s.y)
					){
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
				neighbors[2] = matrix[s.x][s.y];
			}
			else
				return null;
		}
		else if(p.getPredictor().equals("BL")){
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(1, -1);
			Pixel s = p.transform(0, -1);
			if(scannedPixel.containsKey(q.x + "-" + q.y) && 
					scannedPixel.containsKey(r.x + "-" + r.y) && 
					scannedPixel.containsKey(s.x + "-" + s.y)
					){
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
				neighbors[2] = matrix[s.x][s.y];
			}
			else
				return null;
		}

		return neighbors;
	}
	
	private int[] predictionNeighbors(Pixel p, int matrix[][]){
		int[] neighbors = new int[2]; 
		if(p.getPredictor().equals("UR")){
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(0, 1);
			if(scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)){
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
			}
			else
				return null;
		}
		else if(p.getPredictor().equals("UL")){
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(0, -1);
			if(scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)){
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
			}
			else
				return null;
		}
		else if(p.getPredictor().equals("BR")){
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(0, 1);
			if(scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y) ){
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
			}
			else
				return null;
		}
		else if(p.getPredictor().equals("BL")){
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(0, -1);
			if(scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)){
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
			}
			else
				return null;
		}

		return neighbors;
	}


}
