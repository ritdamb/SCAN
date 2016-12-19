package scanpaths;

import java.util.ArrayList;
import java.util.HashMap;

import com.sun.org.apache.xalan.internal.xsltc.compiler.sym;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import model.Block;
import model.Pixel;
import model.Path;
import model.Pixel;

public class ScanPaths {

	
	private HashMap<String,String> scanMap;

	public ScanPaths() {
		
		scanMap = new HashMap<String,String>();
		scanMap.put("O0", "03021213232221110100102030313233");
		scanMap.put("O1", "00101101021222212030313233231303");
		scanMap.put("O2", "30312120101112223233231303020100");
		scanMap.put("O3", "33232232312111121303020100102030");
		
	}
	
	public Path scanPath(int matrix[][], Block b,String kt){
		
		if(kt.equalsIgnoreCase("C0"))
			return C0(matrix,b);
		else if(kt.equalsIgnoreCase("C1"))
			return C1(matrix,b);
		else if(kt.equalsIgnoreCase("C2"))
			return C2(matrix,b);
		else if(kt.equalsIgnoreCase("C3"))
			return C3(matrix,b);
		else if(kt.equalsIgnoreCase("D0"))
			return D0(matrix,b);
		else if(kt.equalsIgnoreCase("D1"))
			return D1(matrix,b);
		else if(kt.equalsIgnoreCase("D2"))
			return D2(matrix,b);
		else if(kt.equalsIgnoreCase("D3"))
			return D3(matrix,b);
		else if(kt.equalsIgnoreCase("S0"))
			return S0(matrix,b);
		else if(kt.equalsIgnoreCase("S1"))
			return S1(matrix,b);
		else if(kt.equalsIgnoreCase("S2"))
			return S2(matrix,b);
		else if(kt.equalsIgnoreCase("S3"))
			return S3(matrix,b);
		
		int dim= b.length();
		//controllare che kt � uno scanpath implementato

		ArrayList<Pixel> scan4 = populate(scanMap.get(kt));
		ArrayList<Pixel> scan= new ArrayList<Pixel>();
		Method method;
		
		try {
			method = getClass().getDeclaredMethod("extends"+kt, ArrayList.class, int.class);
			scan = (ArrayList<Pixel>) method.invoke(this,scan4,dim);
			
		} catch (NoSuchMethodException | SecurityException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} 

		String direction;
		String scanNum = kt.substring(1);
		
		if(scanNum.equalsIgnoreCase("0")){
			direction = ConstantsScan.NORTH_EAST;
		}else if(scanNum.equalsIgnoreCase("1")){
			direction = ConstantsScan.NORTH_WEST;
		}else if(scanNum.equalsIgnoreCase("2")){
			direction = ConstantsScan.SOUTH_WEST;
		}else if(scanNum.equalsIgnoreCase("3")){
			direction = ConstantsScan.SOUTH_EAST;
		}else{
			throw new IllegalArgumentException(kt+ " non valido");
		}
		
		return scanOnImage(scan,b,matrix,direction);
	
	}
	
	private Path C0(int matrix[][], Block b){
		
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		//System.out.println("----- PATH C0 Start ---");
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		for(int x = xStart; x <= xEnd; x++){
			if(x%2 == 0){
				for(int y = yEnd; y >= yStart; y--){
					//System.out.println("Pixel ["+x+"]"+"["+y+"]:"+" "+matrix[x][y]);
					path.add(new Pixel(x, y));
				}
			}
			else{
				for(int y = yStart; y <= yEnd; y++){
					//System.out.println("Pixel ["+x+"]"+"["+y+"]:"+" "+matrix[x][y]);
					path.add(new Pixel(x, y));
				}
			}
		}
		//System.out.println("----- PATH C0 End ---");
		
		return new Path(ConstantsScan.NORTH_EAST, path);
	}
	
	private Path C1(int matrix[][], Block b){
		
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		for(int y = yStart; y <= yEnd; y++){
			if(y%2 == 0){
				for(int x = xStart; x <= xEnd; x++){
					path.add(new Pixel(x, y));
				}
			}
			else{
				for(int x = xEnd; x >= xStart; x--){
					path.add(new Pixel(x, y));
				}
			}
		}
		
		return new Path(ConstantsScan.NORTH_WEST, path);
	}
	
	
	private Path C2(int matrix[][], Block b){
		
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		for(int x = xEnd; x >= xStart; x--){
			if(x%2 == 0){
				for(int y = yStart; y <= yEnd; y++){
					path.add(new Pixel(x, y));
				}
			}
			else{
				for(int y = yEnd; y >= yStart; y--){
					path.add(new Pixel(x, y));
				}
			}
		}
		
		return new Path(ConstantsScan.SOUTH_WEST, path);
	}
	
	private Path C3(int matrix[][], Block b){
		
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		for(int yi = yEnd; yi >= yStart; yi--){
			if(yi%2 == 0){
				for(int x = xEnd; x >= xStart; x--){
					path.add(new Pixel(x, yi));
				}
			}
			else{
				for(int x = xStart; x <= xEnd; x++){
					path.add(new Pixel(x, yi));
				}
			}
		}
		
		return new Path(ConstantsScan.SOUTH_WEST, path);
	}
	
	public Path D0 (int matrix[][], Block b){
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		Pixel p = new Pixel(xStart, yEnd);
		path.add(p);
		p = p.transform(0, -1);
		path.add(p);
		
		boolean fall = true;
		while( !(p.x == xEnd && p.y == yStart) ){
			if(fall){
				p = p.transform(1, 1);
				path.add(p);
				if(p.x == xEnd){
					p = p.transform(0, -1);
					path.add(p);
					fall = false;
				}
				else if(p.y == yEnd){
					p = p.transform(1, 0);
					path.add(p);
					fall = false;
				}
			}
			else{
				p = p.transform(-1, -1);
				path.add(p);
				if(p.y == yStart){
					p = p.transform(1, 0);
					path.add(p);
					fall = true;
				}
				else if(p.x == xStart){
					p = p.transform(0, -1);
					path.add(p);
					fall = true;
				}
			}
		}
		
		return new Path(ConstantsScan.NORTH_EAST, path);
	}
	
	public Path D1 (int matrix[][], Block b){
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		Pixel p = new Pixel(xStart, yStart);
		path.add(p);
		p = p.transform(1, 0);
		path.add(p);
		
		boolean fall = false;
		while( !(p.x == xEnd && p.y == yEnd) ){
			if(fall){
				p = p.transform(1, -1);
				path.add(p);
				if(p.x == xEnd){
					p = p.transform(0, 1);
					path.add(p);
					fall = false;
				}
				else if(p.y == yStart){
					p = p.transform(1, 0);
					path.add(p);
					fall = false;
				}
			}
			else{
				p = p.transform(-1, 1);
				path.add(p);
				if(p.y == yEnd){
					p = p.transform(1, 0);
					path.add(p);
					fall = true;
				}
				else if(p.x == xStart){
					p = p.transform(0, 1);
					path.add(p);
					fall = true;
				}
			}
		}
		
		return new Path(ConstantsScan.NORTH_WEST, path);
	}
	
	
	public Path D2 (int matrix[][], Block b){
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		Pixel p = new Pixel(xEnd, yStart);
		path.add(p);
		p = p.transform(-1, 0);
		path.add(p);
		
		boolean fall = true;
		while( !(p.x == xStart && p.y == yEnd) ){
			if(fall){
				p = p.transform(1, 1);
				path.add(p);
				if(p.y == yEnd){
					p = p.transform(-1, 0);
					path.add(p);
					fall = false;
				}
				else if(p.x == xEnd){
					p = p.transform(0, 1);
					path.add(p);
					fall = false;
				}
			}
			else{
				p = p.transform(-1, -1);
				path.add(p);
				if(p.x == xStart){
					p = p.transform(0, 1);
					path.add(p);
					fall = true;
				}
				else if(p.y == yStart){
					p = p.transform(-1, 0);
					path.add(p);
					fall = true;
				}
			}
		}
		
		return new Path(ConstantsScan.SOUTH_WEST, path);
	}
	
	public Path D3 (int matrix[][], Block b){
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		Pixel p = new Pixel(xEnd, yEnd);
		path.add(p);
		p = p.transform(0, -1);
		path.add(p);
		
		boolean fall = false;
		while( !(p.x == xStart && p.y == yStart) ){
			if(fall){
				p = p.transform(1, -1);
				path.add(p);
				if(p.y == yStart){
					p = p.transform(-1, 0);
					path.add(p);
					fall = false;
				}
				else if(p.x == xEnd){
					p = p.transform(0, -1);
					path.add(p);
					fall = false;
				}
			}
			else{
				p = p.transform(-1, 1);
				path.add(p);
				if(p.x == xStart){
					p = p.transform(0, -1);
					path.add(p);
					fall = true;
				}
				else if(p.y == yEnd){
					p = p.transform(-1, 0);
					path.add(p);
					fall = true;
				}
			}
		}
		
		return new Path(ConstantsScan.SOUTH_EAST, path);
	}
	
	public Path S0 (int matrix[][], Block b){
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		int n = b.length()/2;
		Pixel p  = new Pixel(xStart+n, yStart+n);
		path.add(p);
		
		char move = 'U';
		int add = 1;
		int counter1 = 0;
		int counter2 = 0;
		
		while( !(p.x==xEnd && p.y==yStart)){
			if(move == 'U'){
				p = p.transform(-1, 0);
				path.add(p);				
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'L';
					counter2++;
					if (counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'L'){
				p = p.transform(0, -1);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'D';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'D'){
				p = p.transform(1, 0);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'R';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'R'){
				p = p.transform(0, 1);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'U';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
		}
		
		for (Pixel pixel : path) {
			System.out.println(pixel);
		}
		
		//NON ABBIAMO UN ANGOLO DI PARTENZA, SI PARTE DAL CENTRO
		return new Path(ConstantsScan.NORTH_EAST, path); 
	}
	
	public Path S1 (int matrix[][], Block b){
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		int n = b.length()/2;
		Pixel p  = new Pixel(xStart+n-1, yStart+n);
		path.add(p);
		
		char move = 'L';
		int add = 1;
		int counter1 = 0;
		int counter2 = 0;
		
		while( !(p.x==xEnd && p.y==yEnd)){
			if(move == 'U'){
				p = p.transform(-1, 0);
				path.add(p);				
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'L';
					counter2++;
					if (counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'L'){
				p = p.transform(0, -1);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'D';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'D'){
				p = p.transform(1, 0);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'R';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'R'){
				p = p.transform(0, 1);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'U';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
		}
		
		for (Pixel pixel : path) {
			System.out.println(pixel);
		}
		
		//NON ABBIAMO UN ANGOLO DI PARTENZA, SI PARTE DAL CENTRO
		return new Path(ConstantsScan.NORTH_WEST, path); 
	}
	
	public Path S2 (int matrix[][], Block b){
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		int n = b.length()/2;
		Pixel p  = new Pixel(xStart+n-1, yStart+n-1);
		path.add(p);
		
		char move = 'D';
		int add = 1;
		int counter1 = 0;
		int counter2 = 0;
		
		while( !(p.x==xStart && p.y==yEnd)){
			if(move == 'U'){
				p = p.transform(-1, 0);
				path.add(p);				
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'L';
					counter2++;
					if (counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'L'){
				p = p.transform(0, -1);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'D';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'D'){
				p = p.transform(1, 0);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'R';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'R'){
				p = p.transform(0, 1);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'U';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
		}
		
		for (Pixel pixel : path) {
			System.out.println(pixel);
		}
		
		//NON ABBIAMO UN ANGOLO DI PARTENZA, SI PARTE DAL CENTRO
		return new Path(ConstantsScan.SOUTH_WEST, path); 
	}
	
	
	public Path S3 (int matrix[][], Block b){
		int xStart = b.getxStart();
		int xEnd = b.getxEnd();
		int yStart = b.getyStart();
		int yEnd = b.getyEnd();
		
		ArrayList<Pixel> path = new ArrayList<Pixel>();
		
		int n = b.length()/2;
		Pixel p  = new Pixel(xStart+n, yStart+n-1);
		path.add(p);
		
		char move = 'R';
		int add = 1;
		int counter1 = 0;
		int counter2 = 0;
		
		while( !(p.x==xStart && p.y==yStart)){
			if(move == 'U'){
				p = p.transform(-1, 0);
				path.add(p);				
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'L';
					counter2++;
					if (counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'L'){
				p = p.transform(0, -1);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'D';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'D'){
				p = p.transform(1, 0);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'R';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
			if(move == 'R'){
				p = p.transform(0, 1);
				path.add(p);
				counter1++;
				
				if(counter1 == add){
					counter1 = 0;
					move = 'U';
					counter2++;
					if(counter2 == 2){
						add++;
						counter2 = 0;
					}
				}
			}
			
		}
		
		for (Pixel pixel : path) {
			System.out.println(pixel);
		}
		
		//NON ABBIAMO UN ANGOLO DI PARTENZA, SI PARTE DAL CENTRO
		return new Path(ConstantsScan.SOUTH_EAST, path); 
	}
	

	private static ArrayList<Pixel> extendsO3(ArrayList<Pixel> scan4, int dim){
		
		ArrayList<Pixel> scan = new ArrayList<Pixel>();

		
		for (Pixel p : scan4) {
			scan.add(new Pixel(p.getX()+(dim-4),p.getY()+(dim-4)));
		}
		
		int i=scan4.size()-1;
		int n=dim;
		int goRx=0,goUp=0,goDown=0,goLx=0;
		
		
		while( i < ((n*n)-1) ){
			Pixel currentPath = scan.get(i);
			if(currentPath.getX() == n-1){ // cambio colonna
				scan.add(new Pixel(currentPath.getX(), currentPath.getY()-1));
				scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()-1));
				
				goUp = ((n-1)-currentPath.getY());
				goRx=((n-1)-currentPath.getY())+1;
				goLx=((n-1)-currentPath.getY())+1;
				goDown=((n-1)-currentPath.getY())+2;
				i= i+2;
			}else if(goUp != 0){ // salgo
				scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()));
				i++;
				goUp--;
			}else if(goRx != 0){ // vado a destra
				scan.add(new Pixel(currentPath.getX(), currentPath.getY()+1));
				i++;
				goRx--;
			}else if(goRx == 0 && currentPath.getY() == n-1){//cambio riga
				scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()));
				scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()-1));
				i= i+2;
			}else if( goLx != 0 ){ // vado a sinistra
				scan.add(new Pixel(currentPath.getX(), currentPath.getY()-1));
				i++;
				goLx--;
			}else if(goDown != 0 ){// scendo
				scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()));
				i++;
				goDown--;
			}
		}
		
		return scan;
}
	
	private static ArrayList<Pixel> extendsO2(ArrayList<Pixel> scan4,int dim){
		
		ArrayList<Pixel> scan = new ArrayList<Pixel>();

		
		for (Pixel p : scan4) {
			scan.add(new Pixel(p.getX()+(dim-4),p.getY()));
		}
		
		int i=scan4.size()-1;
		int n=dim;
		int goRx=0,goUp=0,goDown=0,goLx=0;
		
		
		while( i < ((n*n)-1) ){
			Pixel currentPath = scan.get(i);
			if(currentPath.getY() == 0){ // cambio riga
				scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()));
				scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()+1));
				goRx=((n-1)-currentPath.getX());
				goDown=((n-1)-currentPath.getX())+1;
				goUp = ((n-1)-currentPath.getX())+1;
				goLx=((n-1)-currentPath.getX())+2;
				i= i+2;
			}else if(goRx != 0){ // vado a destra
				scan.add(new Pixel(currentPath.getX(), currentPath.getY()+1));
				i++;
				goRx--;
			}else if(goDown != 0 ){// scendo
				scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()));
				i++;
				goDown--;
			}else if(goDown == 0 && currentPath.getX() == n-1){//cambio colonna
				scan.add(new Pixel(currentPath.getX(), currentPath.getY()+1));
				scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()+1));
				i= i+2;
			}else if(goUp != 0){ // salgo
				scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()));
				i++;
				goUp--;
			}else if( goLx != 0 ){ // vado a sinistra
				scan.add(new Pixel(currentPath.getX(), currentPath.getY()-1));
				i++;
				goLx--;
				
			}
		}
		
		return scan;
}
	
	private static ArrayList<Pixel> extendsO1(ArrayList<Pixel> scan4, int dim){
		
				ArrayList<Pixel> scan = new ArrayList<Pixel>();

				
				for (Pixel p : scan4) {
					scan.add(p);
				}
				
				int i=scan4.size()-1;
				int n=dim;
				int goRx=0,goUp=0,goDown=0,goLx=0;
				
				
				while( i < ((n*n)-1) ){
					Pixel currentPath = scan.get(i);
					if(currentPath.getX() == 0){ // cambio colonna
						scan.add(new Pixel(currentPath.getX(), currentPath.getY()+1));
						scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()+1));
						goDown=currentPath.getY();
						goLx=currentPath.getY()+1;
						goRx=currentPath.getY()+1;
						goUp = currentPath.getY()+2;
						i= i+2;
					}else if(goDown != 0 ){// scendo
						scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()));
						i++;
						goDown--;
					}else if( goLx != 0 ){ // vado a sinistra
						scan.add(new Pixel(currentPath.getX(), currentPath.getY()-1));
						i++;
						goLx--;
						
					}else if(goLx == 0 && currentPath.getY() == 0){//cambio riga
						scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()));
						scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()+1));
						i= i+2;
					}else if(goRx != 0){ // vado a destra
						scan.add(new Pixel(currentPath.getX(), currentPath.getY()+1));
						i++;
						goRx--;
					}else if(goUp != 0){ // salgo
						scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()));
						i++;
						goUp--;
					}
				}
				
				return scan;
	}
	
	private static ArrayList<Pixel> extendsO0(ArrayList<Pixel> scan4, int dim){
		
				ArrayList<Pixel> scan = new ArrayList<Pixel>();

				
				for (Pixel p : scan4) {
					scan.add(new Pixel(p.getX(),p.getY()+(dim-4)));
				}
				
				int i=scan4.size()-1;
				int n=dim;
				int goRx=0,goUp=0,goDown=0,goLx=0;
				
				// scan path north-east
				
				while( i < ((n*n)-1) ){
					Pixel currentPath = scan.get(i);
					if(currentPath.getY() == n-1){ // cambio riga
						scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()));
						scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()-1));
						goLx=currentPath.getX();
						goUp = currentPath.getX()+1;
						goDown=currentPath.getX()+1;
						goRx=currentPath.getX()+2;
						i= i+2;
					}else if( goLx != 0 ){ // vado a sinistra
						scan.add(new Pixel(currentPath.getX(), currentPath.getY()-1));
						i++;
						goLx--;
						
					}else if(goUp != 0){ // salgo
						scan.add(new Pixel(currentPath.getX()-1, currentPath.getY()));
						i++;
						goUp--;
					}else if(goUp == 0 && currentPath.getX() == 0){//cambio colonna
						scan.add(new Pixel(currentPath.getX(), currentPath.getY()-1));
						scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()-1));
						i= i+2;
					}else if(goDown != 0 ){// scendo
						scan.add(new Pixel(currentPath.getX()+1, currentPath.getY()));
						i++;
						goDown--;
					}else if(goRx != 0){ // vado a destra
						scan.add(new Pixel(currentPath.getX(), currentPath.getY()+1));
						i++;
						goRx--;
					}
				}
				
				return scan;
	}
	
	private static Path scanOnImage(ArrayList<Pixel> scan8, Block block, int[][] matrix, String direction) {
		int i=0;
		int x1=0,y1=0,posx=0,posy=0,x2=0,y2=0,xStart=0,yStart=0;
		Pixel p = scan8.get(i); 
		ArrayList<Pixel> path = new ArrayList<Pixel>();

		
		if(direction.equals(ConstantsScan.NORTH_EAST)){
			yStart=block.getyEnd();
			xStart=block.getxStart();
		}else if(direction.equals(ConstantsScan.NORTH_WEST)){
			yStart=block.getyStart();
			xStart=block.getxStart();
		}else if(direction.equals(ConstantsScan.SOUTH_WEST)){
			yStart=block.getyStart();
			xStart=block.getxEnd();
		}else{
			yStart=block.getyEnd();
			xStart=block.getxEnd();
		}
		
		//non controllo che parto da noth-east poi va inserito
		//int pixel = matrix[xStart][yStart];
		//System.out.println("["+ xStart+"]["+yStart +"]:" + pixel);
		path.add(new Pixel(xStart,yStart));

		x1=p.getX();
		y1=p.getY();
		posx=xStart;
		posy=yStart;

		
		for(i=1; i < scan8.size(); i++){
			p = scan8.get(i);
			x2=p.getX();
			y2=p.getY();
			
			int x=posx +(x2-x1);
			int y=posy+(y2-y1);
			
			
			path.add(new Pixel(x,y));
			//System.out.println("["+x+"]["+y+"]:" + matrix[x][y]);
			
			posx=x;
			posy=y;
			x1=x2;
			y1=y2;
		}
		
		return new Path(direction, path);
	}
	
	private static ArrayList<Pixel> populate(String scan4){
		ArrayList<Pixel> scan = new ArrayList<Pixel>();
		while(scan4.length()!= 0){
			char xy[] = new char[2];
			scan4.getChars(0, 2, xy, 0);
			scan4 = scan4.substring(2);
			scan.add(new Pixel( Integer.parseInt(""+xy[0]), Integer.parseInt(""+xy[1])));
		}
		return scan;

	}

}
