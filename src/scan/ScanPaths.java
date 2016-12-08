package scan;

public class ScanPaths {

	public ScanPaths() {
		super();
		// TODO Auto-generated constructor stub
	}
	
	public void C0(int matrix[][], Block b){
		
		int xStart = b.xStart;
		int xEnd = b.xEnd;
		int yStart = b.yStart;
		int yEnd = b.yEnd;
		
		
		for(int i = yStart; i <= yEnd; i++){
			if(i%2 == 0){
				for(int j = xEnd; j >= xStart; j--){
					System.out.println("Pixel ["+j+"]"+"["+i+"]:"+" "+matrix[j][i]);
				}
			}
			else{
				for(int j = xStart; j <= xEnd; j++){
					System.out.println("Pixel ["+j+"]"+"["+i+"]:"+" "+matrix[j][i]);
				}
			}
		}
	}

}
