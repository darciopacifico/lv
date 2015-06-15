package br.com.mapfre.lv.pocs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

public class GeraTXTDelimitado {

	public static void main(String[] args) throws IOException {

		FileOutputStream fos = new FileOutputStream("/home/darcio/Desktop/arquivosGigantes/txtDelimitadoPequeno.csv");

		OutputStreamWriter osw = new OutputStreamWriter(fos);

		for (int i = 0; i < 10 ; i++) {

			
			String virg = "";
			
			for(int z=1; z<=100; z++){
				osw.write(virg+"field "+z+":" + Math.random());
				virg=",";
			}
			
			osw.write("\n");
			
			
			if((i%100)==0){
				osw.flush();
			}
			
		}

		osw.flush();
		osw.close();

		fos.flush();
		fos.close();

	}

}
