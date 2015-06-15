package br.com.mapfre.lv.pocs;

import java.io.File;
import java.io.IOException;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.Parser;
import net.sf.flatpack.brparse.BuffReaderParseFactory;

public class TesteFixoLV {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		
		// Obtain the proper parser for your needs
		Parser parser = BuffReaderParseFactory.getInstance().newFixedLengthParser(new File("layoutFixo.xml"),new File("txtFixoGigante.txt"));

		
		// obtain DataSet
		DataSet ds = parser.parse();

		int i=0;
		
		long ini = System.currentTimeMillis();
		
		while (ds.next()) { // loop through file
			
			i++;
			String valLinha = ds.getString("field1") + "-" +ds.getString("field2");
			
			System.out.println(valLinha);
			/*
			if((i%100000)==0){
			}*/
		}
		
		System.out.println("milisegundos =" + (System.currentTimeMillis()-ini)+" linhas:"+i);
		
	}
}
