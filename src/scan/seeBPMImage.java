package scan;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import model.*;
import scanpaths.ConstantsScan;
import scanpaths.ScanPaths;

public class seeBPMImage {
	static HashMap<String, String> scannedPixel = new HashMap<String, String>();

	public static void main(String[] args) throws IOException {

		//BufferedImage image = ImageIO.read(new File("lena_gray.bmp"));
		BufferedImage image = ImageIO.read(new File("lena512.bmp"));

		int[][] matrix = new int[image.getWidth()][image.getHeight()];
		int x = 0,y = 0;
		for (int yPixel = 0; yPixel < image.getWidth(); yPixel++, x++){
			y=0;
			for (int xPixel = 0; xPixel < image.getHeight(); xPixel++, y++) {      

				int color = image.getRGB(xPixel, yPixel);
				//System.out.println("Pixel [" + xPixel + "," + yPixel + "]: " + (color & 0xFF) );	
				matrix[x][y] = color & 0xFF;

			}
		}

		
		ArrayList<Block> blocks = getBlocks(matrix, ConstantsScan.blockSize);
		Pixel lastPixel = null;
		
		BestPathOutput bpo = BestPath(matrix, blocks.get(0), lastPixel);
		
		/*for(int i=0; i<blocks.size();i++){

			BestPathOutput bpo = BestPath(matrix, blocks.get(i), lastPixel);
			lastPixel = bpo.getLastPixel();
			
			System.out.println("Block "+i+" :"+ bpo.getBestPathName());
			
		}*/
		
		
		
		System.out.println(encode("(D2,(S0,C1,C2,O3),C3,(O2,C0,C0,D1))",64));
		System.out.println("100110111000001001010110001111010000000000101");

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



	//La matrice in input deve essere gi� quadrata multiplo di N, eventualmente il controllo sulle dimensioni lo faremo prima
	public static ArrayList<Block> getBlocks (int matrix[][], int N){
		int len = matrix.length;
		ArrayList<Block> blocks = new ArrayList<Block>();

		//# di blocchi che entrano in larghezza e/o in altezza (dato che la matrice � quadrata)
		int numOfBlockPerLine = len/N;
		for(int i=1; i <= numOfBlockPerLine; i++){
			if(i%2 != 0){ //scorro da destra a sinistra
				for(int j=1; j<= numOfBlockPerLine; j++){
					int xStart = (N*(i-1));
					int xEnd = (N*i)-1;
					int yStart = (len-1)-((N*j)-1);
					int yEnd = (len-1)-(N*(j-1));
					
					blocks.add(new Block(xStart, xEnd, yStart, yEnd));
					//System.out.println(blocks.get(blocks.size()-1));
				}
			}
			else{ //else scorro da sinistra a destra
				for(int j=numOfBlockPerLine; j>=1; j--){
					int xStart = (N*(i-1));
					int xEnd = (N*i)-1;
					int yStart = (len-1)-((N*j)-1);
					int yEnd = (len-1)-(N*(j-1));
					
					blocks.add(new Block(xStart, xEnd, yStart, yEnd));
					//System.out.println(blocks.get(blocks.size()-1));
				}
			}
		}

		for(int i=0; i<blocks.size(); i++){
			Block b = blocks.get(i);
			//System.out.println("Blocco: xStart="+b.xStart+"; xEnd="+b.xEnd+"; yStart="+b.yStart+"; yEnd="+b.yEnd);
		}

		return blocks;
	}


	public static BestPathOutput BestPath(int matrix[][], Block block, Pixel prevLastPixel){
		BestPathOutput bpo = new BestPathOutput();
		ScanPaths s = new ScanPaths();
		char[] k = new char[]{'C','O'};
		int t; 
		String minScanKT="";
		int minError=0;
		ArrayList<Integer> listOfPrediction = null; 
		Path path;
		BlockErrorOutput beo;
		
		for(int i =0; i < k.length; i++){
			for(t=0; t < ConstantsScan.maxDirectionScan; t++){
				if(k[i]== 'C' && t == 0){ // se � la prima volta che calcolo l'errore allora � l'errore minimo
					path = s.scanPath(matrix, block, ""+k[0]+t);
					beo = BlockError(matrix, path, prevLastPixel,block);
					minScanKT=""+k[0]+t;
					minError = beo.getE();
					listOfPrediction = beo.getL();

					continue;
				}
				
				path = s.scanPath(matrix, block, ""+k[i]+t);
				beo = BlockError(matrix, path, prevLastPixel,block);
				
				if(beo.getE()< minError){
					minError = beo.getE();
					minScanKT=""+k[i]+t;
					listOfPrediction = beo.getL();
					bpo.setLastPixel(path.getPath().get( path.getPath().size()-1 ));
				}
			}
		}
		
	
		
		int B = 4;
		if(block.length() > ConstantsScan.minimumBlockSize)
			B = 5;


		if(block.length() > ConstantsScan.minimumBlockSize){
			Block subRegions[] = splitBlock(block);

			BestPathOutput bpo1 = BestPath(matrix, subRegions[0], prevLastPixel);
			BestPathOutput bpo2 = BestPath(matrix, subRegions[1], bpo1.getLastPixel());
			BestPathOutput bpo3 = BestPath(matrix, subRegions[2], bpo2.getLastPixel());
			BestPathOutput bpo4 = BestPath(matrix, subRegions[3], bpo3.getLastPixel());

			int errorTotalSum = bpo1.getE() + bpo2.getE() + bpo3.getE() + bpo4.getE();
			int totalBit =  bpo1.getB() + bpo2.getB() + bpo3.getB() + bpo4.getB();
			ArrayList<Integer> predErrTotal= new ArrayList<Integer>();
			predErrTotal.addAll(bpo1.getL());
			predErrTotal.addAll(bpo2.getL());
			predErrTotal.addAll(bpo3.getL());
			predErrTotal.addAll(bpo4.getL());

			if((minError + B) <= errorTotalSum + totalBit){
				bpo.setBestPathName(minScanKT);
				bpo.setE(minError);
				bpo.setB(B);
				bpo.setL(listOfPrediction);
			}
			else{
				bpo.setBestPathName("("+bpo1.getBestPathName()+ "," + bpo2.getBestPathName() + "," + bpo3.getBestPathName() + "," + bpo4.getBestPathName() +")");
				bpo.setE(errorTotalSum);
				bpo.setB(totalBit);
				bpo.setL(predErrTotal);
				bpo.setLastPixel(bpo4.getLastPixel());
			}
		}
		else{

			
			bpo.setBestPathName(minScanKT);
			bpo.setE(minError);
			bpo.setB(B);
			bpo.setL(listOfPrediction);
		}
		
		return bpo;
	}

	private static Block[] splitBlock(Block block) {
		int xStart = block.getxStart();
		int xEnd = block.getxEnd();
		int yStart = block.getyStart();
		int yEnd = block.getyEnd();

		int newLength = block.length()/2;
		Block subRegions[] = new Block[4];
		subRegions[0] = new Block((xEnd - newLength + 1), xEnd, yStart, (yStart + newLength - 1) );
		subRegions[1] = new Block(xStart, (xEnd - newLength), yStart, (yStart + newLength - 1));
		subRegions[2] = new Block(xStart, (xEnd - newLength), (yStart + newLength), yEnd);
		subRegions[3] = new Block((xEnd - newLength + 1), xEnd, (yStart + newLength), yEnd);

		return subRegions;
	}



	public static BlockErrorOutput BlockError(int matrix[][], Path path, Pixel PrevLastPixel, Block block){

		Pixel pixel, prevPixel = null; //prevPixel sarebbe il nostro pixel s
		String lastPredictorUsed = null;

		ArrayList<Integer> L = new ArrayList<Integer>(); //Sequence L of prediction errors along P

		for(int i=0; i < path.size(); i++){
			pixel=path.getPixel(i);
			//vado a vedere se � il primo pixel del primo blocco scansionato dell'immagine
			if(PrevLastPixel == null && i == 0){
				L.add(0);
				prevPixel=pixel;
				scannedPixel.put(pixel.x+"-"+pixel.y, null);
				if(path.startAngle.equals(ConstantsScan.NORTH_EAST))
					lastPredictorUsed="UR";
				else if(path.startAngle.equals(ConstantsScan.NORTH_WEST))
					lastPredictorUsed="UL";
				else if(path.startAngle.equals(ConstantsScan.SOUTH_WEST))
					lastPredictorUsed="BL";
				else if(path.startAngle.equals(ConstantsScan.SOUTH_EAST))
					lastPredictorUsed="BR";

				continue;
			}
			else if(i == 0){ //Come faccio a capire la direzione (UL/UR/BL/BR) 
				//al primo pixel? Vedo l'angolo di partenza
				String startAngle = path.getStartAngle();

				if(startAngle.equals(ConstantsScan.NORTH_EAST)){//UR {N,E}
					int e = calcPredictionErr(pixel, PrevLastPixel, "UR", matrix,block);
					L.add(e);
					lastPredictorUsed = "UR";
				}

				else if(startAngle.equals(ConstantsScan.NORTH_WEST)){//UL {N,W}
					int e = calcPredictionErr(pixel, PrevLastPixel, "UL", matrix,block);
					L.add(e);
					lastPredictorUsed = "UL";
				}

				else if(startAngle.equals(ConstantsScan.SOUTH_WEST)){//BL {S,W}
					int e = calcPredictionErr(pixel, PrevLastPixel, "BL", matrix,block);
					L.add(e);
					lastPredictorUsed = "BL";
				}
				else if(startAngle.equals(ConstantsScan.SOUTH_EAST)){//BR {S,E}
					int e = calcPredictionErr(pixel, PrevLastPixel, "BR", matrix,block);
					L.add(e);
					lastPredictorUsed = "BR";
				}

				prevPixel=pixel;

			}
			else { //qui entro se non sono nel primo pixel del path
				//La prima cosa da fare � determinare il movimento

				int Xmove = pixel.x - prevPixel.x;
				int Ymove = pixel.y - prevPixel.y;

				String s = Xmove+";"+Ymove;
				int e = 0;

				if(s.equals("0;-1")){ //spostamento verso Ovest <-

					if(lastPredictorUsed.startsWith("U")){
						e = calcPredictionErr(pixel, prevPixel, "UR", matrix, block);
					}
					else{
						e = calcPredictionErr(pixel, prevPixel, "BR", matrix, block);
					}
				}
				else if(s.equals("1;0")){ //spostamento verso Sud
					if(lastPredictorUsed.endsWith("R")){
						e = calcPredictionErr(pixel, prevPixel, "UR", matrix, block);
					}
					else{
						e = calcPredictionErr(pixel, prevPixel, "UL", matrix, block);
					}
				}
				else if(s.equals("0;1")){ //spostamento verso Est ->
					if(lastPredictorUsed.startsWith("U")){
						e = calcPredictionErr(pixel, prevPixel, "UL", matrix, block);
					}
					else{
						e = calcPredictionErr(pixel, prevPixel, "BL", matrix, block);
					}
				}
				else if(s.equals("-1;0")){ //spostamento verso Nord
					if(lastPredictorUsed.endsWith("R")){
						e = calcPredictionErr(pixel, prevPixel, "BR", matrix, block);
					}
					else{
						e = calcPredictionErr(pixel, prevPixel, "BL", matrix, block);
					}
				}
				else if(s.equals("1;-1")){ //spostamento verso Sud-Ovest
					e = calcPredictionErr(pixel, prevPixel, "UR", matrix, block);
				}
				else if(s.equals("1;1")){ //spostamento verso Sud-Est
					e = calcPredictionErr(pixel, prevPixel, "UL", matrix, block);
				}
				else if(s.equals("-1;1")){ //spostamento verso Nord-Est
					e = calcPredictionErr(pixel, prevPixel, "BL", matrix, block);
				}
				else if(s.equals("-1;-1")){ //spostamento verso Nord-Ovest
					e = calcPredictionErr(pixel, prevPixel, "BR", matrix, block);
				}

				L.add(e);

				prevPixel = pixel;

			}	

		}

		//faccio la somma dei valori assoluti di tutti gli errori di predizione
		int sum = 0;
		for (Integer e : L) {
			sum += Math.abs(e);
		}


		BlockErrorOutput beo = new BlockErrorOutput(sum, L);
		return beo;
	}

	public static int calcPredictionErr(Pixel actualPixel, Pixel prevPixel, String predictor, int matrix[][], Block block){

		boolean use_s_Pixel = false;

		if(predictor == "UR"){
			if((actualPixel.x - 1) >= block.getyStart()  && (actualPixel.y + 1) <= block.getxEnd()){ //vedo se il pixel a Nord e ad Est non escono fuori dal blocco
				Pixel q = new Pixel(actualPixel.x-1, actualPixel.y);
				Pixel r = new Pixel(actualPixel.x, actualPixel.y+1);
				//vedo se i pixel q ed r sono stati gi� scansionati
				if(scannedPixel.containsKey(q.x+"-"+ (q.y)) && scannedPixel.containsKey((r.x) +"-"+r.y)){
					int pVal = matrix[actualPixel.x][actualPixel.y];
					int qVal = matrix[q.x][q.y];
					int rVal = matrix[r.x][r.y];
					int e = pVal - (qVal+rVal)/2;
					return e;
				}
				else
					use_s_Pixel = true;
			}
			else
				use_s_Pixel = true;
		}
		else if(predictor == "UL"){
			if((actualPixel.y - 1) >= block.getxStart()  && (actualPixel.x - 1) >= block.getyStart()){ //vedo se il pixel a Nord e ad Ovest non escono fuori dal blocco
				Pixel q = new Pixel(actualPixel.x-1, actualPixel.y);
				Pixel r = new Pixel(actualPixel.x, actualPixel.y-1);
				//vedo se i pixel q ed r sono stati gi� scansionati
				if(scannedPixel.containsKey(q.x+"-"+ (q.y)) && scannedPixel.containsKey((r.x) +"-"+r.y)){
					int pVal = matrix[actualPixel.x][actualPixel.y];
					int qVal = matrix[q.x][q.y];
					int rVal = matrix[r.x][r.y];
					int e = pVal - (qVal+rVal)/2;
					return e;
				}
				else
					use_s_Pixel = true;
			}
			else
				use_s_Pixel = true;

		}
		else if(predictor == "BL"){
			if((actualPixel.y -1) >= block.getxStart()  && (actualPixel.x + 1) <= block.getyEnd()){ //vedo se il pixel a Sud e ad Ovest non escono fuori dal blocco
				Pixel q = new Pixel(actualPixel.x+1, actualPixel.y);
				Pixel r = new Pixel(actualPixel.x, actualPixel.y-1);
				//vedo se i pixel q ed r sono stati gi� scansionati
				if(scannedPixel.containsKey(q.x+"-"+ (q.y)) && scannedPixel.containsKey((r.x) +"-"+r.y)){
					int pVal = matrix[actualPixel.x][actualPixel.y];
					int qVal = matrix[q.x][q.y];
					int rVal = matrix[r.x][r.y];
					int e = pVal - (qVal+rVal)/2;
					return e;
				}
				else
					use_s_Pixel = true;
			}
			else
				use_s_Pixel = true;
		}
		else if(predictor == "BR"){
			if((actualPixel.y + 1) <= block.getxEnd()  && (actualPixel.x + 1) <= block.getyEnd()){ //vedo se il pixel a Sud e ad Est non escono fuori dalla matrice
				Pixel q = new Pixel(actualPixel.x+1, actualPixel.y);
				Pixel r = new Pixel(actualPixel.x, actualPixel.y+1);
				//vedo se i pixel q ed r sono stati gi� scansionati
				if(scannedPixel.containsKey(q.x+"-"+ (q.y)) && scannedPixel.containsKey((r.x) +"-"+r.y)){
					int pVal = matrix[actualPixel.x][actualPixel.y];
					int qVal = matrix[q.x][q.y];
					int rVal = matrix[r.x][r.y];
					int e = pVal - (qVal+rVal)/2;
					return e;
				}
				else
					use_s_Pixel = true;
			}
			else
				use_s_Pixel = true;
		}

		if (use_s_Pixel){
			int pVal = matrix[actualPixel.x][actualPixel.y];
			int sVal = matrix[prevPixel.x][prevPixel.y];
			int e = pVal - sVal;
			return e;
		}

		return -1;
	}

	public static String encode(String scanpath, int blockSize){
		
		String scan = scanpath.replace(",", "");
		if(scan.length() == 2 ){
			String k = scan.substring(0,1);
			String t = scan.substring(1);
			if(blockSize > ConstantsScan.minimumBlockSize)
				return "0"+code(k)+code(t);
			else
				return code(k)+code(t);
			
		}else{
			String[] scans;
			if(scanpath.contains("(") || scanpath.contains(")"))
				scans = scanpath.split("[\\( | \\)]");
			else
				scans = scanpath.split(",");
			
			String[] p = new String[4];
			int i=0;
			for(String s: scans){
				if (s.length()!=0){
					p[i] = s;
					i++;
				}
			}
			
			return "1"+ encode(p[0],blockSize/2)+ encode(p[1],blockSize/2)
					+encode(p[2],blockSize/2)+encode(p[3],blockSize/2);
		}
	}
	
	private static String code(String k){
		if(k.equals("C")){
			return "00";
		}else if(k.equals("D")){
			return "01";
		}else if(k.equals("O")){
			return "10";
		}else if(k.equals("S")){
			return "11";
		}else if(k.equals("0")){
			return "00";
		}else if(k.equals("1")){
			return "01";
		}else if(k.equals("2")){
			return "10";
		}else if(k.equals("3")){
			return "11";
		}else{
			return null;
		}
	}
	
}
