package main;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;


import ac.*;
import model.ArithmeticCodeOutput;
import model.BestPathOutput;
import model.Block;
import model.BlockErrorOutput;
import model.Path;
import model.Pixel;
import scanpaths.ConstantsScan;
import scanpaths.ScanPaths;

public class CompressSCAN {

	private HashMap<String, String> scannedPixel;
	private Pixel first_pixel;
	private Pixel second_pixel;
	private int[][] matrix;
	private ArrayList<Block> blocks;

	public CompressSCAN(String pathInputFile, String pathOutFile) throws IOException {

		scannedPixel = new HashMap<String, String>();
		first_pixel = null;
		second_pixel = null;
		matrix = loadImage(ImageIO.read(new File(pathInputFile)));
		blocks = Block.getBlocks(matrix, ConstantsScan.blockSize);
	}

	public void compress() throws IOException {
		// scanning and prediction
		Pixel lastPixel = null;
		ArrayList<String> scanPaths = new ArrayList<String>();
		ArrayList<Integer> predictionsError = new ArrayList<Integer>();

		for (int i = 0; i < blocks.size(); i++) {

			BestPathOutput bpo = BestPath(matrix, blocks.get(i), lastPixel);

			lastPixel = bpo.getLastPixel();
			predictionsError.addAll(bpo.getL());
			scanPaths.add(bpo.getBestPathName());

		}
		// contextmodeling

		ArrayList<Integer> contexts = context(matrix, blocks, scanPaths);

		// scanPath encode

		String encodeScanPaths = "";
		for (String scan : scanPaths)
			encodeScanPaths += encode(scan, ConstantsScan.blockSize);

		// arithmetic coding

		ArithmeticCodeOutput out =arithmeticCodingEncode(predictionsError,contexts);

		//AdaptiveArithmeticCompress compress = new AdaptiveArithmeticCompress("buff.txt", "compress");
		//AdaptiveArithmeticDecompress decompress = new AdaptiveArithmeticDecompress("compress", "buffOut.txt");
		
	}

	private int[][] loadImage(BufferedImage image) {
		int[][] matrix = new int[image.getWidth()][image.getHeight()];
		int x = 0, y = 0;
		for (int yPixel = 0; yPixel < image.getWidth(); yPixel++, x++) {
			y = 0;
			for (int xPixel = 0; xPixel < image.getHeight(); xPixel++, y++) {

				int color = image.getRGB(xPixel, yPixel);
				// System.out.println("Pixel [" + xPixel + "," + yPixel + "]: "
				// + (color & 0xFF) );
				matrix[x][y] = color & 0xFF;

			}
		}
		return matrix;
	}

	private BestPathOutput BestPath(int matrix[][], Block block, Pixel prevLastPixel) {
		BestPathOutput bpo = new BestPathOutput();
		ScanPaths s = new ScanPaths();
		char[] k = new char[] { 'C', 'O' };
		int t;
		String minScanKT = "";
		int minError = 0;
		ArrayList<Integer> listOfPrediction = null;
		Path path;
		BlockErrorOutput beo;

		for (int i = 0; i < k.length; i++) {
			for (t = 0; t < ConstantsScan.maxDirectionScan; t++) {
				if (k[i] == 'C' && t == 0) { // se � la prima volta che
												// calcolo l'errore allora �
												// l'errore minimo
					path = s.scanPath(matrix, block, "" + k[0] + t);
					beo = BlockError(matrix, path, prevLastPixel, block);
					minScanKT = "" + k[0] + t;
					minError = beo.getE();
					listOfPrediction = beo.getL();

					continue;
				}

				path = s.scanPath(matrix, block, "" + k[i] + t);
				beo = BlockError(matrix, path, prevLastPixel, block);

				if (beo.getE() < minError) {
					minError = beo.getE();
					minScanKT = "" + k[i] + t;
					listOfPrediction = beo.getL();
					bpo.setLastPixel(path.getPath().get(path.getPath().size() - 1));
				}
			}
		}

		int B = 4;
		if (block.length() > ConstantsScan.minimumBlockSize)
			B = 5;

		if (block.length() > ConstantsScan.minimumBlockSize) {
			Block subRegions[] = Block.splitBlock(block);

			BestPathOutput bpo1 = BestPath(matrix, subRegions[0], prevLastPixel);
			BestPathOutput bpo2 = BestPath(matrix, subRegions[1], bpo1.getLastPixel());
			BestPathOutput bpo3 = BestPath(matrix, subRegions[2], bpo2.getLastPixel());
			BestPathOutput bpo4 = BestPath(matrix, subRegions[3], bpo3.getLastPixel());

			int errorTotalSum = bpo1.getE() + bpo2.getE() + bpo3.getE() + bpo4.getE();
			int totalBit = bpo1.getB() + bpo2.getB() + bpo3.getB() + bpo4.getB();
			ArrayList<Integer> predErrTotal = new ArrayList<Integer>();
			predErrTotal.addAll(bpo1.getL());
			predErrTotal.addAll(bpo2.getL());
			predErrTotal.addAll(bpo3.getL());
			predErrTotal.addAll(bpo4.getL());

			if ((minError + B) <= errorTotalSum + totalBit) {
				bpo.setBestPathName(minScanKT);
				bpo.setE(minError);
				bpo.setB(B);
				bpo.setL(listOfPrediction);
			} else {
				bpo.setBestPathName("(" + bpo1.getBestPathName() + "," + bpo2.getBestPathName() + ","
						+ bpo3.getBestPathName() + "," + bpo4.getBestPathName() + ")");
				bpo.setE(errorTotalSum);
				bpo.setB(totalBit);
				bpo.setL(predErrTotal);
				bpo.setLastPixel(bpo4.getLastPixel());
			}
		} else {

			bpo.setBestPathName(minScanKT);
			bpo.setE(minError);
			bpo.setB(B);
			bpo.setL(listOfPrediction);
		}

		return bpo;
	}

	private BlockErrorOutput BlockError(int matrix[][], Path path, Pixel PrevLastPixel, Block block) {

		Pixel pixel; // prevPixel sarebbe il nostro pixel s
		ArrayList<Integer> L = new ArrayList<Integer>(); // Sequence L of
															// prediction errors
															// along P

		// il primo pixel del blocco lo faccio fuori perchè prendo il
		// PrevLastPixel che è l'ultimo dello scanpath precendente
		// poi invece prendo il pixel precedente (i-1)
		pixel = path.getPixel(0);
		int err = calcPredictionErr(pixel, PrevLastPixel, pixel.predictor, matrix, block);
		L.add(err);
		scannedPixel.put(pixel.x + "-" + pixel.y, null);

		for (int i = 1; i < path.size(); i++) {
			pixel = path.getPixel(i);
			int e = calcPredictionErr(pixel, path.getPixel(i - 1), pixel.predictor, matrix, block);
			L.add(e);
			scannedPixel.put(pixel.x + "-" + pixel.y, null);
		}

		// faccio la somma dei valori assoluti di tutti gli errori di predizione
		int sum = 0;
		for (Integer e : L) {
			sum += Math.abs(e);
		}

		BlockErrorOutput beo = new BlockErrorOutput(sum, L);
		return beo;
	}

	private int calcPredictionErr(Pixel actualPixel, Pixel prevPixel, String predictor, int matrix[][], Block block) {

		boolean use_s_Pixel = false;

		if (predictor == "UR") {
			Pixel q = new Pixel(actualPixel.x - 1, actualPixel.y);
			Pixel r = new Pixel(actualPixel.x, actualPixel.y + 1);
			// vedo se i pixel q ed r sono stati già scansionati
			if (scannedPixel.containsKey(q.x + "-" + (q.y)) && scannedPixel.containsKey((r.x) + "-" + r.y)) {
				int pVal = matrix[actualPixel.x][actualPixel.y];
				int qVal = matrix[q.x][q.y];
				int rVal = matrix[r.x][r.y];
				int e = pVal - (qVal + rVal) / 2;
				return e;
			} else
				use_s_Pixel = true;

		} else if (predictor == "UL") {
			Pixel q = new Pixel(actualPixel.x - 1, actualPixel.y);
			Pixel r = new Pixel(actualPixel.x, actualPixel.y - 1);
			// vedo se i pixel q ed r sono stati gi� scansionati
			if (scannedPixel.containsKey(q.x + "-" + (q.y)) && scannedPixel.containsKey((r.x) + "-" + r.y)) {
				int pVal = matrix[actualPixel.x][actualPixel.y];
				int qVal = matrix[q.x][q.y];
				int rVal = matrix[r.x][r.y];
				int e = pVal - (qVal + rVal) / 2;
				return e;
			} else
				use_s_Pixel = true;

		} else if (predictor == "BL") {
			Pixel q = new Pixel(actualPixel.x + 1, actualPixel.y);
			Pixel r = new Pixel(actualPixel.x, actualPixel.y - 1);
			// vedo se i pixel q ed r sono stati gi� scansionati
			if (scannedPixel.containsKey(q.x + "-" + (q.y)) && scannedPixel.containsKey((r.x) + "-" + r.y)) {
				int pVal = matrix[actualPixel.x][actualPixel.y];
				int qVal = matrix[q.x][q.y];
				int rVal = matrix[r.x][r.y];
				int e = pVal - (qVal + rVal) / 2;
				return e;
			} else
				use_s_Pixel = true;

		} else if (predictor == "BR") {
			Pixel q = new Pixel(actualPixel.x + 1, actualPixel.y);
			Pixel r = new Pixel(actualPixel.x, actualPixel.y + 1);
			// vedo se i pixel q ed r sono stati gi� scansionati
			if (scannedPixel.containsKey(q.x + "-" + (q.y)) && scannedPixel.containsKey((r.x) + "-" + r.y)) {
				int pVal = matrix[actualPixel.x][actualPixel.y];
				int qVal = matrix[q.x][q.y];
				int rVal = matrix[r.x][r.y];
				int e = pVal - (qVal + rVal) / 2;
				return e;
			} else
				use_s_Pixel = true;

		}

		if (use_s_Pixel) {
			if (prevPixel == null) {
				return 0;
			}
			int pVal = matrix[actualPixel.x][actualPixel.y];
			int sVal = matrix[prevPixel.x][prevPixel.y];
			int e = Math.abs(pVal - sVal);
			return e;
		}

		return -1;
	}

	private ArrayList<Integer> context(int matrix[][], ArrayList<Block> blocks, ArrayList<String> scanPaths) {

		ArrayList<Integer> contexts = new ArrayList<Integer>();
		ScanPaths s = new ScanPaths();
		Path path;
		scannedPixel = new HashMap<String, String>();
		for (int i = 0; i < blocks.size(); i++) {

			String scan = scanPaths.get(i);
			if (scan.contains("(") || scan.contains(")")) {
				String[] scans = scan.split("[\\( | \\) | , ]");
				String[] p = new String[4];
				int j = 0;
				for (String sc : scans) {
					if (sc.length() != 0) {
						p[j] = sc;
						j++;
					}
				}
				Block[] blockSplitted = Block.splitBlock(blocks.get(i));

				for (int c = 0; c < p.length; c++) {
					path = s.scanPath(matrix, blockSplitted[c], p[c]);
					contexts.addAll(getContext(matrix, path, blocks.get(i)));
				}
			} else {
				path = s.scanPath(matrix, blocks.get(i), scan);
				contexts.addAll(getContext(matrix, path, blocks.get(i)));
			}
		}

		return contexts;
	}

	private ArrayList<Integer> getContext(int matrix[][], Path path, Block block) {

		Pixel pixel;

		ArrayList<Integer> L = new ArrayList<Integer>();
		for (int i = 0; i < path.size(); i++) {
			pixel = path.getPixel(i);
			if (first_pixel == null || second_pixel == null) {
				L.add(0);
			} else {
				int e = calcContext(pixel, pixel.getPredictor(), matrix, block);
				if (e >= 0 && e <= 2)
					L.add(0);
				else if (e >= 3 && e <= 8)
					L.add(1);
				else if (e >= 9 && e <= 15)
					L.add(2);
				else
					L.add(3);
			}

			scannedPixel.put(pixel.x + "-" + pixel.y, null);
			second_pixel = first_pixel;
			first_pixel = pixel;
		}

		return L;
	}

	private int calcContext(Pixel actualPixel, String predictor, int[][] matrix, Block block) {
		boolean use_uv_Pixel = false;

		if (predictor == "UR") { // N,E,NE
			if ((actualPixel.x - 1) >= block.getxStart() && (actualPixel.y + 1) <= block.getyEnd()) { // vedo
																										// se
																										// il
																										// pixel
																										// a
																										// Nord
																										// e
																										// ad
																										// Est
																										// non
																										// escono
																										// fuori
																										// dal
																										// blocco
				Pixel q = new Pixel(actualPixel.x - 1, actualPixel.y);
				Pixel r = new Pixel(actualPixel.x, actualPixel.y + 1);
				Pixel s = new Pixel(actualPixel.x - 1, actualPixel.y + 1);
				// vedo se i pixel q ed r sono stati gi� scansionati
				if (scannedPixel.containsKey(q.x + "-" + (q.y)) && scannedPixel.containsKey((r.x) + "-" + r.y)
						&& scannedPixel.containsKey(s.x + "-" + s.y)) {
					int sVal = matrix[s.x][s.y];
					int qVal = matrix[q.x][q.y];
					int rVal = matrix[r.x][r.y];
					int a = (Math.abs(qVal - rVal) + Math.abs(rVal - sVal)) / 2;
					return a;
				} else
					use_uv_Pixel = true;
			} else
				use_uv_Pixel = true;
		} else if (predictor == "UL") {// N,NW,W
			if ((actualPixel.y - 1) >= block.getyStart() && (actualPixel.x - 1) >= block.getxStart()) { // vedo
																										// se
																										// il
																										// pixel
																										// a
																										// Nord
																										// e
																										// ad
																										// Ovest
																										// non
																										// escono
																										// fuori
																										// dal
																										// blocco
				Pixel q = new Pixel(actualPixel.x - 1, actualPixel.y);
				Pixel r = new Pixel(actualPixel.x, actualPixel.y - 1);
				Pixel s = new Pixel(actualPixel.x - 1, actualPixel.y - 1);
				// vedo se i pixel q ed r sono stati gi� scansionati
				if (scannedPixel.containsKey(q.x + "-" + (q.y)) && scannedPixel.containsKey((r.x) + "-" + r.y)
						&& scannedPixel.containsKey(s.x + "-" + s.y)) {
					int sVal = matrix[s.x][s.y];
					int qVal = matrix[q.x][q.y];
					int rVal = matrix[r.x][r.y];
					int a = (Math.abs(qVal - rVal) + Math.abs(rVal - sVal)) / 2;
					return a;
				} else
					use_uv_Pixel = true;
			} else
				use_uv_Pixel = true;

		} else if (predictor == "BL") {// S,SW,W
			if ((actualPixel.y - 1) >= block.getyStart() && (actualPixel.x + 1) <= block.getxEnd()) { // vedo
																										// se
																										// il
																										// pixel
																										// a
																										// Sud
																										// e
																										// ad
																										// Ovest
																										// non
																										// escono
																										// fuori
																										// dal
																										// blocco
				Pixel q = new Pixel(actualPixel.x + 1, actualPixel.y);
				Pixel r = new Pixel(actualPixel.x, actualPixel.y - 1);
				Pixel s = new Pixel(actualPixel.x + 1, actualPixel.y - 1);
				// vedo se i pixel q ed r sono stati gi� scansionati
				if (scannedPixel.containsKey(q.x + "-" + (q.y)) && scannedPixel.containsKey((r.x) + "-" + r.y)
						&& scannedPixel.containsKey(s.x + "-" + s.y)) {
					int sVal = matrix[s.x][s.y];
					int qVal = matrix[q.x][q.y];
					int rVal = matrix[r.x][r.y];
					int a = (Math.abs(qVal - rVal) + Math.abs(rVal - sVal)) / 2;
					return a;
				} else
					use_uv_Pixel = true;
			} else
				use_uv_Pixel = true;
		} else if (predictor == "BR") {// S,SE,E
			if ((actualPixel.y + 1) <= block.getyEnd() && (actualPixel.x + 1) <= block.getxEnd()) { // vedo
																									// se
																									// il
																									// pixel
																									// a
																									// Sud
																									// e
																									// ad
																									// Est
																									// non
																									// escono
																									// fuori
																									// dalla
																									// matrice
				Pixel q = new Pixel(actualPixel.x + 1, actualPixel.y);
				Pixel r = new Pixel(actualPixel.x, actualPixel.y + 1);
				Pixel s = new Pixel(actualPixel.x + 1, actualPixel.y + 1);
				// vedo se i pixel q ed r sono stati gi� scansionati
				if (scannedPixel.containsKey(q.x + "-" + (q.y)) && scannedPixel.containsKey((r.x) + "-" + r.y)
						&& scannedPixel.containsKey(s.x + "-" + s.y)) {
					int sVal = matrix[s.x][s.y];
					int qVal = matrix[q.x][q.y];
					int rVal = matrix[r.x][r.y];
					int a = (Math.abs(qVal - rVal) + Math.abs(rVal - sVal)) / 2;
					return a;
				} else
					use_uv_Pixel = true;
			} else
				use_uv_Pixel = true;
		}

		if (use_uv_Pixel) {
			int uVal = matrix[first_pixel.x][first_pixel.y];
			int vVal = matrix[second_pixel.x][second_pixel.y];
			int e = Math.abs(uVal - vVal);
			return e;
		}

		return -1;

	}

	private String encode(String scanpath, int blockSize) {

		String scan = scanpath.replace(",", "");
		if (scan.length() == 2) {
			String k = scan.substring(0, 1);
			String t = scan.substring(1);
			if (blockSize > ConstantsScan.minimumBlockSize)
				return "0" + code(k) + code(t);
			else
				return code(k) + code(t);

		} else {
			String[] scans;
			if (scanpath.contains("(") || scanpath.contains(")"))
				scans = scanpath.split("[\\( | \\) | ,]");
			else
				scans = scanpath.split(",");

			String[] p = new String[4];
			int i = 0;
			for (String s : scans) {
				if (s.length() != 0) {
					p[i] = s;
					i++;
				}
			}

			return "1" + encode(p[0], blockSize / 2) + encode(p[1], blockSize / 2) + encode(p[2], blockSize / 2)
					+ encode(p[3], blockSize / 2);
		}
	}

	private String code(String k) {
		if (k.equals("C")) {
			return "00";
		} else if (k.equals("D")) {
			return "01";
		} else if (k.equals("O")) {
			return "10";
		} else if (k.equals("S")) {
			return "11";
		} else if (k.equals("0")) {
			return "00";
		} else if (k.equals("1")) {
			return "01";
		} else if (k.equals("2")) {
			return "10";
		} else if (k.equals("3")) {
			return "11";
		} else {
			return null;
		}
	}

	private ArithmeticCodeOutput arithmeticCodingEncode(ArrayList<Integer> predictionsError,
			ArrayList<Integer> contexts) throws IOException {
		
		
		ArrayList<ArrayList<Integer>> listBuffers = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> buff0 = new ArrayList<Integer>();
		ArrayList<Integer> buff1 = new ArrayList<Integer>();
		ArrayList<Integer> buff2 = new ArrayList<Integer>();
		ArrayList<Integer> buff3 = new ArrayList<Integer>();
		ArrayList<byte[]> stream = new ArrayList<byte[]>();
		
		
		for (int i = 0; i < contexts.size(); i++) {
			if (contexts.get(i) == 0)
				buff0.add(predictionsError.get(i));
			else if (contexts.get(i) == 1)
				buff1.add(predictionsError.get(i));
			else if (contexts.get(i) == 2)
				buff2.add(predictionsError.get(i));
			else
				buff3.add(predictionsError.get(i));
		}
		
		listBuffers.add(buff0);
		listBuffers.add(buff1);
		listBuffers.add(buff2);
		listBuffers.add(buff3);
		
		/*
		//effettuo l'encoding dei buffer
		for(ArrayList<Integer> buff : listBuffers){
			FlatFrequencyTable initFreqs = new FlatFrequencyTable(513);
			FrequencyTable freqs = new SimpleFrequencyTable(initFreqs);
			BitOutputStream out = new BitOutputStream();
			ArithmeticEncoder enc = new ArithmeticEncoder(out);
			for (Integer symbol : buff ) {
				symbol += 255;
				enc.write(freqs, symbol);
				freqs.increment(symbol);
			}
			
			enc.write(freqs, 512); // EOF
			enc.finish(); // Flush remaining code bits
			stream.add(out.getByteStream());
		}*/
		
		
		
		System.out.println(buff0.toString());
		ByteArrayInputStream in = new ByteArrayInputStream(buff0.toString().getBytes());
		BitOutputStream out = new BitOutputStream(new BufferedOutputStream(new FileOutputStream("compress")));
		AdaptiveArithmeticCompress.compress(in, out,buff0.size());
				
		
		BitInputStream ind = new BitInputStream(new BufferedInputStream(new FileInputStream("compress")));
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		AdaptiveArithmeticDecompress.decompress(ind, baos,buff0.size());
		
		byte[] byteArray = baos.toByteArray();
		String string = new String(byteArray);
		System.out.println(string);

		
		
		
		
		
		
		
		
		
		
		
		/*
		ArithmeticCodeOutput output = new ArithmeticCodeOutput(stream, buff0.size(), buff1.size(), buff2.size(),
				buff3.size());

		
		//decompression test
		DecompressSCAN decompressor = new DecompressSCAN();
		ArrayList<ArrayList<Integer>> list = decompressor.arithmeticCodingDecode(stream);

		int i=0;
		
		while(i< 3){
			ArrayList<Integer> deBuff = list.get(i);
			ArrayList<Integer> enBuff = listBuffers.get(i);
			
			if(deBuff.size() != enBuff.size()){
				System.out.println("Size diverse a "+ i);
				break;
			}
			for(int j=0; j < deBuff.size(); j++ )
				if(deBuff.get(j)!= enBuff.get(j)){
					System.out.println("DIVERSI "+j +" di "+i);
					break;
				}
			
			i++;
		}*/
		

		return null;

	}

}
