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
	
	public void O0(int matrix[][], Block b){
		int LimiteX = 1;
		int LimiteY = 1;
		int xAdd = -1;
		int yAdd = 1;
		char turn = 'x';
		
		int myX = b.xEnd;
		int myY = b.yStart;
		System.out.println("Pixel ["+myX+"]"+"["+myY+"]:"+" "+matrix[myX][myY]);
		
		int total = 0;
		int BlockSize = (b.xEnd - b.xStart) + 1;
		
		while(total < (BlockSize * BlockSize)){
			if(turn == 'x'){
				for(int i=1; i <= LimiteX; i++){
					int x = myX + xAdd;
					if(x > b.xEnd){
						turn = 'y';
						xAdd = xAdd * -1;
						break;
					}
					myX = x; 
					System.out.println("Pixel ["+myX+"]"+"["+myY+"]:"+" "+matrix[myX][myY]);
					
					if(i == LimiteX){
						turn = 'y';
						LimiteX ++;
						xAdd = xAdd * -1;
						break;
					}
				}
				
			}
			else{
				for(int i=1; i <= LimiteY; i++){
					int y = myY + yAdd;
					if(y < b.yStart){
						turn = 'x';	
						yAdd = yAdd * -1;
						break;
					}
					myY = y;
					
					System.out.println("Pixel ["+myX+"]"+"["+myY+"]:"+" "+matrix[myX][myY]);
					
					if(i == LimiteY){
						turn = 'x';
						LimiteY ++;
						yAdd = yAdd * -1;
						break;
					}
				}
			}
		}
		
	}

}
