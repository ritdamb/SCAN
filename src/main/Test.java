package main;

import java.io.IOException;


public class Test {
	
	 
	public static void main(String[] args) throws IOException{
	
		System.out.println("CODIFICO...");
		
		String fileName = "baboon.tiff";

		CompressSCAN compressor = new CompressSCAN("images/"+fileName, "output/"+fileName+".compressed");
		compressor.compress(); // crea il file compress
		
		System.out.println("DECODIFICO...");

		DecompressSCAN decompressor = new DecompressSCAN("output/"+fileName+".compressed", "output/"+fileName);
		decompressor.decompress();
		
		
		
		
	}
	
	
}
