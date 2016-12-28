package main;

import java.io.IOException;
import java.util.ArrayList;

import ac.ArithmeticDecoder;
import ac.BitInputStream;
import ac.FlatFrequencyTable;
import ac.FrequencyTable;
import ac.SimpleFrequencyTable;

public class DecompressSCAN {


	public DecompressSCAN() {

	}

	public ArrayList<ArrayList<Integer>> arithmeticCodingDecode(ArrayList<byte[]> stream) throws IOException {

		ArrayList<ArrayList<Integer>> result = new ArrayList<ArrayList<Integer>>();
		ArrayList<Integer> buff0 = new ArrayList<Integer>();
		ArrayList<Integer> buff1 = new ArrayList<Integer>();
		ArrayList<Integer> buff2 = new ArrayList<Integer>();
		ArrayList<Integer> buff3 = new ArrayList<Integer>();

		result.add(buff0);
		result.add(buff1);
		result.add(buff2);
		result.add(buff3);

		int i = 0;
		while (i < 3) {
			FlatFrequencyTable initFreqs = new FlatFrequencyTable(513);
			FrequencyTable freqs = new SimpleFrequencyTable(initFreqs);
			ArithmeticDecoder dec = new ArithmeticDecoder(new BitInputStream(stream.get(i)));
			while (true) {
				// Decode and write one byte
				int symbol = dec.read(freqs);
				if (symbol == 512) // EOF symbol
					break;
				result.get(i).add(symbol - 255);
				freqs.increment(symbol);
			}
			i++;
		}

		return result;
	}

}
