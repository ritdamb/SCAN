package scan;

import model.Block;
import scanpaths.ScanPaths;

public class testScanPath {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		int matrix[][] = null;
		ScanPaths s = new ScanPaths();
		s.scanPath(matrix, new Block(0, 3, 0, 3), "D3");
	}

}
