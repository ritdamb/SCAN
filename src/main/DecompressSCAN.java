package main;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import javax.imageio.ImageIO;
import ac.AdaptiveArithmeticDecompress;
import ac.BitInputStream;
import model.Block;
import model.Path;
import model.Pixel;
import scanpaths.ConstantsScan;
import scanpaths.ScanPaths;

public class DecompressSCAN {

	private HashMap<String, String> scannedPixel;
	private String inputPath, outputPath;
	private Color[][] matrix;
	private BitInputStream in;

	public DecompressSCAN(String compressFile, String outputFile) throws FileNotFoundException {

		super();
		scannedPixel = new HashMap<String, String>();
		inputPath = compressFile;
		outputPath = outputFile;
		in = new BitInputStream(new FileInputStream(inputPath));
	}

	public void decompress() throws IOException {

		/** HEADER1 **/
		//mi prendo i singoli bit di header1, scanpaths e header2
		//non mi servono i bit dei prediction errors
		String inputBits = "";
		while (true) {
			inputBits += in.read();
			if(inputBits.length() == ConstantsScan.header1BitSize) 
				break;
		}

		int n = Integer.parseInt(inputBits, 2) + 2;
		int size = (int) Math.pow(2, n);

		// inizializzo la matrice
		matrix = new Color[size][size];

		/** SCANPATHS **/
		//System.out.println("Decodifico Scanpaths...");
		// parto da 3 perche' ho gia' letto i primi 3 bit
		ArrayList<String> scanPathTypes = decodeScanPaths(size);

		/** HEADER 2 **/
		// A questo punto abbiamo 2 byte (16 bit)che corrispondono ai primi due
		// pixel dell'immagine e abbiamo 16 byte( 4 int, 32 bit ciascuno)
		// delle size dei 4 buffer

		int bitToRead=ConstantsScan.header2BitSize, i =0;
		inputBits="";
		while(i < bitToRead){
			inputBits+= in.read();
			i++;
		}

		int[] pixel1 = new int[3];
		int[] pixel2 = new int[3];
		
		int index=0;
		String tmp = inputBits.substring(index, index + 8);
		index = index + 8;
		pixel1[0] = Integer.parseInt(tmp, 2);
		
		tmp = inputBits.substring(index, index + 8);
		index = index + 8;
		pixel1[1] = Integer.parseInt(tmp, 2);
		
		tmp = inputBits.substring(index, index + 8);
		index = index + 8;
		pixel1[2] = Integer.parseInt(tmp, 2);

		tmp = inputBits.substring(index, index + 8);
		index = index + 8;
		pixel2[0] = Integer.parseInt(tmp, 2);
		
		tmp = inputBits.substring(index, index + 8);
		index = index + 8;
		pixel2[1] = Integer.parseInt(tmp, 2);
		
		tmp = inputBits.substring(index, index + 8);
		index = index + 8;
		pixel2[2] = Integer.parseInt(tmp, 2);

		tmp = inputBits.substring(index, index + 32);
		index = index + 32;
		int buffSize0 = Integer.parseInt(tmp, 2);

		tmp = inputBits.substring(index, index + 32);
		index = index + 32;
		int buffSize1 = Integer.parseInt(tmp, 2);

		tmp = inputBits.substring(index, index + 32);
		index = index + 32;
		int buffSize2 = Integer.parseInt(tmp, 2);

		tmp = inputBits.substring(index, index + 32);
		index = index + 32;
		int buffSize3 = Integer.parseInt(tmp, 2);

		/** PREDICTION ERRORS **/
		// decompressione arithmeticCoding
		ArrayList<Integer> tmpList = new ArrayList<Integer>();
		ArrayList<ArrayList<Integer>> buffersList = new ArrayList<ArrayList<Integer>>();

		flushInputStream();
		byte[] bytes = new byte[buffSize0];
		in.read(bytes);
		AdaptiveArithmeticDecompress decomp = new AdaptiveArithmeticDecompress();

		decomp.decompress(bytes,tmpList);
		buffersList.add(0,tmpList);
		tmpList= new ArrayList<Integer>();

		bytes = new byte[buffSize1];
		in.read(bytes);
		decomp.decompress(bytes,tmpList);
		buffersList.add(1,tmpList);
		tmpList=new ArrayList<Integer>();

		bytes = new byte[buffSize2];
		in.read(bytes);
		decomp.decompress(bytes,tmpList);
		buffersList.add(2,tmpList);
		tmpList=new ArrayList<Integer>();

		bytes = new byte[buffSize3];
		in.read(bytes);
		decomp.decompress(bytes,tmpList);
		buffersList.add(3,tmpList);


		/** IMAGE RECONSTRUCTION **/
		// creo una lista di Path in ordine sulla matrice
		ArrayList<Path> pathSequence = createPathSequence(scanPathTypes);

		populateMatrix(pathSequence, pixel1, pixel2, buffersList);

		BufferedImage outputImage = new BufferedImage(size, size, BufferedImage.TYPE_3BYTE_BGR);

		for (int k = 0; k < size; k++) {
			for (int j = 0; j < size; j++) {
				Color newColor = matrix[k][j];
				outputImage.setRGB(j, k, newColor.getRGB());
			}
		}
		File output = new File(outputPath);
		ImageIO.write(outputImage, "bmp", output);

	}

	private void flushInputStream() throws IOException {
		//prima di effettuare la decodifica pulisco il byte che sto andando a leggere
		//da eventuali 0 aggiunti dal padding e che non appartengono al primo byte
		//dei predictions error

		while(in.getNumBitsRemaining()!= 0)
			in.read();

	}

	private void populateMatrix(ArrayList<Path> pathSequence, int[] pixel1, int[] pixel2,
			ArrayList<ArrayList<Integer>> buffersList) {

		int index0 = 6;
		int index1 = 0;
		int index2 = 0;
		int index3 = 0;
		int[] v = pixel1;
		int[] u = pixel2;

		for (int a = 0; a < pathSequence.size(); a++) {
			ArrayList<Pixel> p = pathSequence.get(a).getPath();
			Pixel actualPixel;
			int b = 0;
			// mi prendo i primi due pixel scansionati
			if (a == 0) {
				matrix[p.get(0).x][p.get(0).y] = new Color(pixel1[0], pixel1[1], pixel1[2]);
				matrix[p.get(1).x][p.get(1).y] = new Color(pixel2[0], pixel2[1], pixel2[2]);
				scannedPixel.put(p.get(0).x + "-" + p.get(0).y, null);
				scannedPixel.put(p.get(1).x + "-" + p.get(1).y, null);
				b = 2;

			}
			for (; b < p.size(); b++) {
				int[][] neighbors;
				actualPixel = p.get(b);
				int[] e = new int[3];
				int[] predErr = new int[3];
				if ((neighbors = contextNeighbors(actualPixel, matrix)) != null){
					e[0] = (Math.abs(neighbors[0][0] - neighbors[1][0]) + Math.abs(neighbors[1][0] - neighbors[2][0])) / 2;
					e[1] = (Math.abs(neighbors[0][1] - neighbors[1][1]) + Math.abs(neighbors[1][1] - neighbors[2][1])) / 2;
					e[2] = (Math.abs(neighbors[0][2] - neighbors[1][2]) + Math.abs(neighbors[1][2] - neighbors[2][2])) / 2;
				}
				else{
					e[0] = Math.abs(u[0] - v[0]);
					e[1] = Math.abs(u[1] - v[1]);
					e[2] = Math.abs(u[2] - v[2]);
				}
				
				for(int i=0; i<3; i++){
					if (e[i] >= 0 && e[i] <= 2)
						predErr[i] = buffersList.get(0).get(index0++);
					else if (e[i] >= 3 && e[i] <= 8)
						predErr[i] = buffersList.get(1).get(index1++);
					else if (e[i] >= 9 && e[i] <= 15)
						predErr[i] = buffersList.get(2).get(index2++);
					else
						predErr[i] = buffersList.get(3).get(index3++);
				}

				int[][] predictionNeighbors = predictionNeighbors(actualPixel, matrix);
				int[] pixelVal = new int[3];
				for(int i=0; i<3; i++){
					if (predictionNeighbors != null)
						pixelVal[i] = predErr[i] + (predictionNeighbors[0][i] + predictionNeighbors[1][i]) / 2;
					else
						pixelVal[i] = predErr[i] + u[i];
				}

				matrix[actualPixel.x][actualPixel.y] = new Color(pixelVal[0], pixelVal[1], pixelVal[2]);

				v = u;
				u = pixelVal;
				scannedPixel.put(actualPixel.x + "-" + actualPixel.y, null);

			}

		}

	}

	private int[][] contextNeighbors(Pixel p, Color matrix[][]) {
		int[][] neighbors = new int[3][3];
		if (p.getPredictor().equals("UR")) {
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(-1, 1);
			Pixel s = p.transform(0, 1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)
					&& scannedPixel.containsKey(s.x + "-" + s.y)) {
				neighbors[0][0] = matrix[q.x][q.y].getRed();
				neighbors[0][1] = matrix[q.x][q.y].getGreen();
				neighbors[0][2] = matrix[q.x][q.y].getBlue();
					
				neighbors[1][0] = matrix[r.x][r.y].getRed();
				neighbors[1][1] = matrix[r.x][r.y].getGreen();
				neighbors[1][2] = matrix[r.x][r.y].getBlue();
				
				neighbors[2][0] = matrix[s.x][s.y].getRed();
				neighbors[2][1] = matrix[s.x][s.y].getGreen();
				neighbors[2][2] = matrix[s.x][s.y].getBlue();
			} else
				return null;
		} else if (p.getPredictor().equals("UL")) {
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(-1, -1);
			Pixel s = p.transform(0, -1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)
					&& scannedPixel.containsKey(s.x + "-" + s.y)) {
				neighbors[0][0] = matrix[q.x][q.y].getRed();
				neighbors[0][1] = matrix[q.x][q.y].getGreen();
				neighbors[0][2] = matrix[q.x][q.y].getBlue();
					
				neighbors[1][0] = matrix[r.x][r.y].getRed();
				neighbors[1][1] = matrix[r.x][r.y].getGreen();
				neighbors[1][2] = matrix[r.x][r.y].getBlue();
				
				neighbors[2][0] = matrix[s.x][s.y].getRed();
				neighbors[2][1] = matrix[s.x][s.y].getGreen();
				neighbors[2][2] = matrix[s.x][s.y].getBlue();
			} else
				return null;
		} else if (p.getPredictor().equals("BR")) {
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(1, 1);
			Pixel s = p.transform(0, 1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)
					&& scannedPixel.containsKey(s.x + "-" + s.y)) {
				neighbors[0][0] = matrix[q.x][q.y].getRed();
				neighbors[0][1] = matrix[q.x][q.y].getGreen();
				neighbors[0][2] = matrix[q.x][q.y].getBlue();
					
				neighbors[1][0] = matrix[r.x][r.y].getRed();
				neighbors[1][1] = matrix[r.x][r.y].getGreen();
				neighbors[1][2] = matrix[r.x][r.y].getBlue();
				
				neighbors[2][0] = matrix[s.x][s.y].getRed();
				neighbors[2][1] = matrix[s.x][s.y].getGreen();
				neighbors[2][2] = matrix[s.x][s.y].getBlue();
			} else
				return null;
		} else if (p.getPredictor().equals("BL")) {
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(1, -1);
			Pixel s = p.transform(0, -1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)
					&& scannedPixel.containsKey(s.x + "-" + s.y)) {
				neighbors[0][0] = matrix[q.x][q.y].getRed();
				neighbors[0][1] = matrix[q.x][q.y].getGreen();
				neighbors[0][2] = matrix[q.x][q.y].getBlue();
					
				neighbors[1][0] = matrix[r.x][r.y].getRed();
				neighbors[1][1] = matrix[r.x][r.y].getGreen();
				neighbors[1][2] = matrix[r.x][r.y].getBlue();
				
				neighbors[2][0] = matrix[s.x][s.y].getRed();
				neighbors[2][1] = matrix[s.x][s.y].getGreen();
				neighbors[2][2] = matrix[s.x][s.y].getBlue();
			} else
				return null;
		}

		return neighbors;
	}

	private int[][] predictionNeighbors(Pixel p, Color matrix[][]) {
		int[][] neighbors = new int[2][3];
		if (p.getPredictor().equals("UR")) {
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(0, 1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)) {
				neighbors[0][0] = matrix[q.x][q.y].getRed();
				neighbors[0][1] = matrix[q.x][q.y].getGreen();
				neighbors[0][2] = matrix[q.x][q.y].getBlue();
				
				neighbors[1][0] = matrix[r.x][r.y].getRed();
				neighbors[1][1] = matrix[r.x][r.y].getGreen();
				neighbors[1][2] = matrix[r.x][r.y].getBlue();
			} else
				return null;
		} else if (p.getPredictor().equals("UL")) {
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(0, -1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)) {
				neighbors[0][0] = matrix[q.x][q.y].getRed();
				neighbors[0][1] = matrix[q.x][q.y].getGreen();
				neighbors[0][2] = matrix[q.x][q.y].getBlue();
				
				neighbors[1][0] = matrix[r.x][r.y].getRed();
				neighbors[1][1] = matrix[r.x][r.y].getGreen();
				neighbors[1][2] = matrix[r.x][r.y].getBlue();
			} else
				return null;
		} else if (p.getPredictor().equals("BR")) {
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(0, 1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)) {
				neighbors[0][0] = matrix[q.x][q.y].getRed();
				neighbors[0][1] = matrix[q.x][q.y].getGreen();
				neighbors[0][2] = matrix[q.x][q.y].getBlue();
				
				neighbors[1][0] = matrix[r.x][r.y].getRed();
				neighbors[1][1] = matrix[r.x][r.y].getGreen();
				neighbors[1][2] = matrix[r.x][r.y].getBlue();
			} else
				return null;
		} else if (p.getPredictor().equals("BL")) {
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(0, -1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)) {
				neighbors[0][0] = matrix[q.x][q.y].getRed();
				neighbors[0][1] = matrix[q.x][q.y].getGreen();
				neighbors[0][2] = matrix[q.x][q.y].getBlue();
				
				neighbors[1][0] = matrix[r.x][r.y].getRed();
				neighbors[1][1] = matrix[r.x][r.y].getGreen();
				neighbors[1][2] = matrix[r.x][r.y].getBlue();
			} else
				return null;
		}

		return neighbors;
	}

	private ArrayList<Path> createPathSequence(ArrayList<String> scanPathTypes) {

		ArrayList<Block> blocks = Block.getBlocks(matrix, ConstantsScan.blockSize);
		// a questo punto ho due ArrayList blocks e scanPaths della stessa size

		ScanPaths sp = new ScanPaths();
		ArrayList<Path> pathSequence = new ArrayList<Path>();

		Path prevPath = null;
		for (int z = 0; z < scanPathTypes.size(); z++) {
			if (scanPathTypes.get(z).length() == 2) {
				Path b_path = sp.scanPath(blocks.get(z), scanPathTypes.get(z));
				b_path.setPreviousPath(prevPath);
				pathSequence.add(b_path);
				prevPath = b_path;
			} else { // è un path composto
				Block[] blks = Block.splitBlock(blocks.get(z));
				String subPaths[] = scanPathTypes.get(z).split(",");
				for (int y = 0; y < 4; y++) {
					Path b_path = sp.scanPath(blks[y], subPaths[y]);
					b_path.setPreviousPath(prevPath);
					pathSequence.add(b_path);
					prevPath = b_path;
				}
			}
		}

		return pathSequence;
	}

	private ArrayList<String> decodeScanPaths(int size) throws IOException {

		// mi calcolo il numero di blocchi 32*32 da leggere (alcuni potranno
		// anche non essere basic)
		int blocksToRead = (int) Math.pow(size / ConstantsScan.blockSize, 2);

		ArrayList<String> scanPathTypes = new ArrayList<String>();

		while (blocksToRead > 0) {
			String c = ""+in.read();
			if (c.equals("0")) { // è un basic scanpath allora leggo 4 bit
				String k = "" +in.read();
				k += ""+in.read();
				String t = ""+in.read();
				t += ""+in.read();

				String KT = "";
				if (k.equals("00"))
					KT = "C";
				else if (k.equals("01"))
					KT = "D";
				else if (k.equals("10"))
					KT = "O";
				else if (k.equals("11"))
					KT = "S";

				if (t.equals("00"))
					KT += "0";
				else if (t.equals("01"))
					KT += "1";
				else if (t.equals("10"))
					KT += "2";
				else if (t.equals("11"))
					KT += "3";

				blocksToRead--;
				scanPathTypes.add(KT);

			} else { // è uno scanpath composto quindi dovrò leggerne 4*4 = 16 bit
				String compositePath = "";
				for (int i = 0; i < 4; i++) {
					String k = ""+in.read();
					k += ""+in.read();
					String t = ""+in.read();
					t += ""+in.read();

					String KT;
					if (i == 0)
						KT = "";
					else
						KT = ",";

					if (k.equals("00"))
						KT += "C";
					else if (k.equals("01"))
						KT += "D";
					else if (k.equals("10"))
						KT += "O";
					else if (k.equals("11"))
						KT += "S";

					if (t.equals("00"))
						KT += "0";
					else if (t.equals("01"))
						KT += "1";
					else if (t.equals("10"))
						KT += "2";
					else if (t.equals("11"))
						KT += "3";

					compositePath += KT;
				}
				blocksToRead--;
				scanPathTypes.add(compositePath);
			}
		}

		return scanPathTypes;
	}

}
