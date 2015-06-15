package br.com.mapfre.lv.pocs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;

/**
 * Gera TXT com tamanho de campos fixo
 * @author darcio
 *
 */
public class GeraTXTFixo {

	public static void main(String[] args) throws IOException {

		FileOutputStream fos = new FileOutputStream("txtFixoGrande_10k.txt");

		OutputStreamWriter osw = new OutputStreamWriter(fos);

		for (int i = 0; i < 10000 ; i++) {

			
			
			for(int z=1; z<=100; z++){
				String fieldVal = "field:"+z+":" + Math.random();
				
				fieldVal=fieldVal.substring(0, 15);
				
				osw.write(fieldVal);
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
