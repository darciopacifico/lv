package br.com.mapfre.lv.layoutmodel;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;

public class TestFormatter {

	public static void main(String[] args) throws ParseException {
		String strVal = "199999,9";
		
		String pattern = "##,#";
		char separadorMilhar = '.';
		char separadorDecimal = ',';
		
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		
		symbols.setDecimalSeparator(separadorDecimal);
		symbols.setGroupingSeparator(separadorMilhar);
		
		DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
		
		Object val = decimalFormat.parse(strVal);
		
		System.out.println(val.getClass().getCanonicalName() + " = " + val);

		Long iVal = ((Number) val).longValue();
		
		System.out.println(val.getClass().getCanonicalName() + " = " + iVal);
		
	}
	
}
