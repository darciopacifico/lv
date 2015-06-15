package br.com.mapfre.lv.test;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(locations = "/ApplicationContext.xml")
@Test
public class TestCheckSum extends AbstractTestNGSpringContextTests {
	
	@Test
	public void testMD5() throws NoSuchAlgorithmException, IOException{
		
		//InputStream bis = new FileInputStream("planilhaGigante_1000k.xlsx");
		
		final FileInputStream fileInputStream = new FileInputStream("planilhaGigante_1000k.xlsx");
		
		StringBuffer hexString = calculateChecksum(fileInputStream);
    
    System.out.println(hexString.toString());
    
	}

	protected StringBuffer calculateChecksum(final FileInputStream fileInputStream) throws NoSuchAlgorithmException, IOException {
		BufferedInputStream bis = new BufferedInputStream(fileInputStream);
		
		//BufferedInputStream bis = new BufferedInputStream(new FileInputStream("e_planilhaGigante_1k.xlsx"));

		
		MessageDigest md = MessageDigest.getInstance("MD5");
		
		
    byte[] dataBytes = new byte[8150];

    int nread = 0;
    while ((nread = bis.read(dataBytes)) != -1) {
        md.update(dataBytes, 0, nread);
    }

    byte[] mdbytes = md.digest();
		
    

    //convert the byte to hex format method 2
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < mdbytes.length; i++) {
        String hex = Integer.toHexString(0xff & mdbytes[i]);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
    }
		return hexString;
	}
	
	
}
