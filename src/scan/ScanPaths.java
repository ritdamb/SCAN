package scan;

import java.util.ArrayList;

public class ScanPaths {

	public ScanPaths() {
		super();
	}
	
	public Path C0(int matrix[][], Block b){
		
		int xStart = b.xStart;
		int xEnd = b.xEnd;
		int yStart = b.yStart;
		int yEnd = b.yEnd;
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		
		for(int i = yStart; i <= yEnd; i++){
			if(i%2 == 0){
				for(int j = xEnd; j >= xStart; j--){
					//System.out.println("Pixel ["+j+"]"+"["+i+"]:"+" "+matrix[j][i]);
					path.add(new Pixel(j, i));
				}
			}
			else{
				for(int j = xStart; j <= xEnd; j++){
					//System.out.println("Pixel ["+j+"]"+"["+i+"]:"+" "+matrix[j][i]);
					path.add(new Pixel(j, i));
				}
			}
		}
		
		return new Path("NE", path);
	}

}
