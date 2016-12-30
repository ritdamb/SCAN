package main;

import java.io.IOException;

import io.Reader;

public class Test {
	
	 
	public static void main(String[] args) throws IOException{
	
		CompressSCAN compressor = new CompressSCAN("lena512.bmp", "compress");
		compressor.compress(); // crea il file compress
		
		Reader r = new Reader();
		try {
			r.ReadImage("compress");
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		
		
		
	}
	
	
}
