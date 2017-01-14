package main;

import java.io.IOException;


public class Test {
	
	 
	public static void main(String[] args) throws IOException{
	
		System.out.println("CODIFICO...");

		CompressSCAN compressor = new CompressSCAN("lena512.bmp", "compress");
		compressor.compress(); // crea il file compress
		
		System.out.println("DECODIFICO...");

		DecompressSCAN decompressor = new DecompressSCAN("compress", "output.bmp");
		decompressor.decompress();
		
		
		
		
	}
	
	
}
