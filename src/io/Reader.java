package io;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import javax.imageio.ImageIO;

import main.CompressSCAN;
import model.Block;
import model.Path;
import scanpaths.ConstantsScan;
import scanpaths.ScanPaths;

public class Reader {

	public Reader() {
		super();
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
		
		
		BufferedImage outputImage = new BufferedImage(size, size, BufferedImage.TYPE_BYTE_GRAY);
        for(int k=0; k<size; k++) {
            for(int j=0; j<size; j++) {
                int a = matrix[k][j];
                Color newColor = new Color(a, a, a);
                outputImage.setRGB(k,j,newColor.getRGB());
            }
        }
        File output = new File("64x64.bmp");
        ImageIO.write(outputImage, "bmp", output);


	}


}
