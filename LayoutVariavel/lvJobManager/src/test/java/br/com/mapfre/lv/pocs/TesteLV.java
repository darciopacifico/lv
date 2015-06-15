package br.com.mapfre.lv.pocs;

import java.io.File;
import java.io.IOException;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.Parser;
import net.sf.flatpack.brparse.BuffReaderParseFactory;

public class TesteLV {

	/**
	 * @param args
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {

		
		
		// Obtain the proper parser for your needs
		Parser parser = BuffReaderParseFactory
				.getInstance()
				.newDelimitedParser(
						new File("/home/darcio/workspaceAndroid/testeLV/layout.xml"), // xml
						new File("/home/darcio/workspaceAndroid/testeLV/txtGigante.txt"), // txt
						',', // delimiter
						'"', // text qualfier
						false); // ignore the first record (may need to be done
								// if first record contain column names)

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
		
		System.out.println("milisegundos =" + (System.currentTimeMillis()-ini));
		
	}
}
