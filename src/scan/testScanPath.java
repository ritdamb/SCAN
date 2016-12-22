package scan;

import model.Block;
import model.Path;
import model.Pixel;
import scanpaths.ScanPaths;

public class testScanPath {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int matrix[][] = null;
		ScanPaths s = new ScanPaths();
		Path path = s.scanPath(matrix, new Block(0, 15, 0, 15), "O3");
		for(Pixel p: path.getPath() ){
			System.out.println(p);
		}
		
		/*Byte b1 = (byte)255;
		Byte b2 = (byte)2;
		String s1 = String.format("%8s", Integer.toBinaryString(254)).replace(' ', '0');
		System.out.println(s1); // 10000001
		String s2 = String.format("%8s", Integer.toBinaryString(2)).replace(' ', '0');
		System.out.println(s2); // 10000001
*/		
	}

}
