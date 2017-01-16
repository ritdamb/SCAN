package main;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import javax.imageio.ImageIO;

import ac.*;
import io.Writer;
import model.BestPathOutput;
import model.Block;
import model.BlockErrorOutput;
import model.Path;
import model.Pixel;
import scanpaths.ConstantsScan;
import scanpaths.ScanPaths;

public class CompressSCAN {

	private HashMap<String, String> scannedPixel;
	private Pixel u_pixel;
	private Pixel v_pixel;
	private int first_pixel, second_pixel;
	private int[][] matrix;
	private ArrayList<Block> blocks;
	private int buffSize0,buffSize1,buffSize2,buffSize3;
	private String pathOutFile;
	
	public CompressSCAN(String pathInputFile, String pathOutFile) throws IOException {

		scannedPixel = new HashMap<String, String>();
		u_pixel = null;
		v_pixel = null;
		matrix = loadImage(ImageIO.read(new File(pathInputFile)));
		blocks = Block.getBlocks(matrix, ConstantsScan.blockSize);
		this.pathOutFile = pathOutFile;
	}

	public void compress() throws IOException {
		// scanning and prediction
		Pixel lastPixel = null;
		ArrayList<String> scanPathsName = new ArrayList<String>();
		ArrayList<Path> scanPaths = new ArrayList<Path>();
		ArrayList<Integer> predictionsError = new ArrayList<Integer>();

		for (int i = 0; i < blocks.size(); i++) {

			BestPathOutput bpo = BestPath(blocks.get(i), lastPixel);

			lastPixel = bpo.getLastPixel();
			predictionsError.addAll(bpo.getL());
			scanPathsName.add(bpo.getBestPathName());
			scanPaths.add(bpo.getBestPath());

		}
		// contextmodeling

		ArrayList<Integer> contexts = context(scanPaths);

		// scanPath encode

		String encodeScanPaths = "";
		for (String scan : scanPathsName)
			encodeScanPaths += encode(scan, ConstantsScan.blockSize);

		// arithmetic coding
		// arithmeticCoding(predictionsError, contexts);

		ArrayList<Integer> stream = arithmeticCodingEncode(predictionsError, contexts);

		// scrivo i byte nel file
		Writer writer = new Writer();

		writer.writeImage(pathOutFile, matrix.length, encodeScanPaths, first_pixel, second_pixel,
				buffSize0,buffSize1,buffSize2,buffSize3,stream);

	}

	private int[][] loadImage(BufferedImage image) {
		int[][] matrix = new int[image.getWidth()][image.getHeight()];
		int x = 0, y = 0;
		for (int yPixel = 0; yPixel < image.getWidth(); yPixel++, x++) {
			y = 0;
			for (int xPixel = 0; xPixel < image.getHeight(); xPixel++, y++) {

				int color = image.getRGB(xPixel, yPixel);
				matrix[x][y] = color & 0xFF;
			}
		}
		return matrix;
	}

	private BestPathOutput BestPath(Block block, Pixel prevLastPixel) {
		BestPathOutput bpo = new BestPathOutput();
		ScanPaths s = new ScanPaths();
		char[] k = new char[] { 'C', 'D', 'O', 'S' };
		int t;
		String minScanKT = "";
		int minError = -1;
		ArrayList<Integer> listOfPrediction = null;
		Path path, bestPath = null;
		BlockErrorOutput beo;

		for (int i = 0; i < k.length; i++) {
			for (t = 0; t < ConstantsScan.maxDirectionScan; t++) {

				path = s.scanPath(matrix, block, "" + k[i] + t);
				beo = BlockError(path, prevLastPixel);

				if (beo.getE() < minError || minError == -1) {
					minError = beo.getE();
					minScanKT = "" + k[i] + t;
					bestPath = path;
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

			BestPathOutput bpo1 = BestPath(subRegions[0], prevLastPixel);
			BestPathOutput bpo2 = BestPath(subRegions[1], bpo1.getLastPixel());
			BestPathOutput bpo3 = BestPath(subRegions[2], bpo2.getLastPixel());
			BestPathOutput bpo4 = BestPath(subRegions[3], bpo3.getLastPixel());

			int errorTotalSum = bpo1.getE() + bpo2.getE() + bpo3.getE() + bpo4.getE();
			int totalBit = bpo1.getB() + bpo2.getB() + bpo3.getB() + bpo4.getB();
			ArrayList<Integer> predErrTotal = new ArrayList<Integer>();
			predErrTotal.addAll(bpo1.getL());
			predErrTotal.addAll(bpo2.getL());
			predErrTotal.addAll(bpo3.getL());
			predErrTotal.addAll(bpo4.getL());

			if ((minError + B) <= errorTotalSum + totalBit) {
				bpo.setBestPathName(minScanKT);
				bpo.setBestPath(bestPath);
				bpo.setE(minError);
				bpo.setB(B);
				bpo.setL(listOfPrediction);
			} else {
				bpo.setBestPathName("(" + bpo1.getBestPathName() + "," + bpo2.getBestPathName() + ","
						+ bpo3.getBestPathName() + "," + bpo4.getBestPathName() + ")");
				bestPath = bpo1.getBestPath();
				bestPath.getPath().addAll(bpo2.getBestPath().getPath());
				bestPath.getPath().addAll(bpo3.getBestPath().getPath());
				bestPath.getPath().addAll(bpo4.getBestPath().getPath());
				bpo.setBestPath(bestPath);
				bpo.setE(errorTotalSum);
				bpo.setB(totalBit);
				bpo.setL(predErrTotal);
				bpo.setLastPixel(bpo4.getLastPixel());
			}
		} else {

			bpo.setBestPathName(minScanKT);
			bpo.setBestPath(bestPath);
			bpo.setE(minError);
			bpo.setB(B);
			bpo.setL(listOfPrediction);
		}

		for (Pixel p : bestPath.getPath()) {
			scannedPixel.put(p.x + "-" + p.y, null);
		}
		return bpo;
	}

	private boolean isScannedPixel(HashMap<String, String> tempScanned, Pixel pixel) {
		String key = pixel.x + "-" + pixel.y;
		if (scannedPixel.containsKey(key) || tempScanned.containsKey(key))
			return true;
		return false;
	}

	private BlockErrorOutput BlockError(Path path, Pixel PrevLastPixel) {

		Pixel pixel; // prevPixel sarebbe il nostro pixel s
		ArrayList<Integer> L = new ArrayList<Integer>(); // Sequence L of prediction error along P

		HashMap<String, String> tempScanned = new HashMap<String, String>();
		// il primo pixel del blocco lo faccio fuori perchè prendo il
		// PrevLastPixel che è l'ultimo dello scanpath precendente
		// poi invece prendo il pixel precedente (i-1)
		pixel = path.getPixel(0);
		int err = calcPredictionErr(pixel, PrevLastPixel, tempScanned);
		L.add(err);
		tempScanned.put(pixel.x + "-" + pixel.y, null);

		for (int i = 1; i < path.size(); i++) {
			pixel = path.getPixel(i);
			int e = calcPredictionErr(pixel, path.getPixel(i - 1), tempScanned);
			L.add(e);
			tempScanned.put(pixel.x + "-" + pixel.y, null);
		}

		// faccio la somma dei valori assoluti di tutti gli errori di predizione
		int sum = 0;
		for (Integer e : L) {
			sum += Math.abs(e);
		}

		BlockErrorOutput beo = new BlockErrorOutput(sum, L);
		return beo;
	}

	private int[] predictionNeighbors(Pixel p, HashMap<String, String> tempScanned) {
		int[] neighbors = new int[2];
		if (p.getPredictor().equals("UR")) {
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(0, 1);
			if (isScannedPixel(tempScanned, q) && isScannedPixel(tempScanned, r)) {
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
			} else
				return null;
		} else if (p.getPredictor().equals("UL")) {
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(0, -1);
			if (isScannedPixel(tempScanned, q) && isScannedPixel(tempScanned, r)) {
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
			} else
				return null;
		} else if (p.getPredictor().equals("BR")) {
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(0, 1);
			if (isScannedPixel(tempScanned, q) && isScannedPixel(tempScanned, r)) {
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
			} else
				return null;
		} else if (p.getPredictor().equals("BL")) {
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(0, -1);
			if (isScannedPixel(tempScanned, q) && isScannedPixel(tempScanned, r)) {
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
			} else
				return null;
		}

		return neighbors;
	}

	private int calcPredictionErr(Pixel actualPixel, Pixel prevPixel, HashMap<String, String> tempScanned) {

		int[] neighbors = predictionNeighbors(actualPixel, tempScanned);
		if (neighbors != null) {
			return matrix[actualPixel.x][actualPixel.y] - ((neighbors[0] + neighbors[1]) / 2);
		} else {
			if (prevPixel == null) {
				return 0;
			}
			int pVal = matrix[actualPixel.x][actualPixel.y];
			int sVal = matrix[prevPixel.x][prevPixel.y];
			int e = pVal - sVal;
			return e;
		}

	}

	private ArrayList<Integer> context(ArrayList<Path> scanPaths) {

		ArrayList<Integer> L = new ArrayList<Integer>();
		Pixel pixel;
		scannedPixel = new HashMap<String, String>();
		for (Path p : scanPaths) {
			for (int i = 0; i < p.size(); i++) {
				pixel = p.getPixel(i);
				if (v_pixel == null || u_pixel == null) {
					L.add(0);
					if (v_pixel == null) {
						first_pixel = matrix[pixel.x][pixel.y];
						v_pixel = pixel;
					} else {
						second_pixel = matrix[pixel.x][pixel.y];
						u_pixel = pixel;
					}
				} else {
					int e = calcContext(pixel);
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

				if (u_pixel != null && u_pixel != pixel) {
					v_pixel = u_pixel;
					u_pixel = pixel;
				}
			}
		}
		return L;
	}

	private int[] contextNeighbors(Pixel p) {
		int[] neighbors = new int[3];
		if (p.getPredictor().equals("UR")) {
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(-1, 1);
			Pixel s = p.transform(0, 1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)
					&& scannedPixel.containsKey(s.x + "-" + s.y)) {
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
				neighbors[2] = matrix[s.x][s.y];
			} else
				return null;
		} else if (p.getPredictor().equals("UL")) {
			Pixel q = p.transform(-1, 0);
			Pixel r = p.transform(-1, -1);
			Pixel s = p.transform(0, -1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)
					&& scannedPixel.containsKey(s.x + "-" + s.y)) {
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
				neighbors[2] = matrix[s.x][s.y];
			} else
				return null;
		} else if (p.getPredictor().equals("BR")) {
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(1, 1);
			Pixel s = p.transform(0, 1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)
					&& scannedPixel.containsKey(s.x + "-" + s.y)) {
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
				neighbors[2] = matrix[s.x][s.y];
			} else
				return null;
		} else if (p.getPredictor().equals("BL")) {
			Pixel q = p.transform(1, 0);
			Pixel r = p.transform(1, -1);
			Pixel s = p.transform(0, -1);
			if (scannedPixel.containsKey(q.x + "-" + q.y) && scannedPixel.containsKey(r.x + "-" + r.y)
					&& scannedPixel.containsKey(s.x + "-" + s.y)) {
				neighbors[0] = matrix[q.x][q.y];
				neighbors[1] = matrix[r.x][r.y];
				neighbors[2] = matrix[s.x][s.y];
			} else
				return null;
		}

		return neighbors;
	}

	private int calcContext(Pixel actualPixel) {

		int[] neighbors = contextNeighbors(actualPixel);
		if (neighbors != null)
			return (Math.abs(neighbors[0] - neighbors[1]) + Math.abs(neighbors[1] - neighbors[2])) / 2;
		else {
			int uVal = matrix[u_pixel.x][u_pixel.y];
			int vVal = matrix[v_pixel.x][v_pixel.y];
			int e = Math.abs(uVal - vVal);
			return e;
		}

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
			throw new IllegalArgumentException();
		}
	}

	private ArrayList<Integer> arithmeticCodingEncode(ArrayList<Integer> predictionsError,
			ArrayList<Integer> contexts) throws IOException {

		ArrayList<Integer> buff0 = new ArrayList<Integer>();
		ArrayList<Integer> buff1 = new ArrayList<Integer>();
		ArrayList<Integer> buff2 = new ArrayList<Integer>();
		ArrayList<Integer> buff3 = new ArrayList<Integer>();
		ArrayList<Integer> list = new ArrayList<Integer>();

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

		
		AdaptiveArithmeticCompress comp = new AdaptiveArithmeticCompress();
		
		comp.compress( buff0);
		buffSize0 = comp.getStream().size();
		list.addAll(comp.getStream());
		
		comp.compress( buff1);
		buffSize1 = comp.getStream().size();
		list.addAll(comp.getStream());
		
		comp.compress( buff2);
		buffSize2 = comp.getStream().size();
		list.addAll(comp.getStream());
		
		comp.compress( buff3);
		buffSize3 = comp.getStream().size();
		list.addAll(comp.getStream());
		
		return list;
		
	}

}
