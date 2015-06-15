package br.com.mapfre.lv.layoutmodel;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.usermodel.XSSFRichTextString;
import org.xml.sax.helpers.DefaultHandler;

import br.com.mapfre.lv.parser.EnumDataType;

public class AbstractSAXHandler extends DefaultHandler {

	protected StringBuffer cellValRaw = new StringBuffer();
	protected EnumDataType dataType;
	protected ReadOnlySharedStringsTable stringsTable;

	public AbstractSAXHandler() {
		super();
	}

	/**
	 * 
	 * @return
	 */
	protected final String getValRaw() {
		return this.cellValRaw.toString();
	}

	/**
	 * Lê o valor da celula, de acordo com o tipo da mesma, formatacoes etc..
	 * 
	 * @param nextDataType
	 * @param cellValRaw
	 * @param strings
	 * @return
	 */
	protected final  Serializable getVal() {
	
		Serializable cellVal = null;
		switch (this.dataType) {
		case BOOL:
	
			char first = this.cellValRaw.charAt(0);
			cellVal = new Boolean(first == '0' ? false : true); // esta é a regra aplicada pelo excell. Inclusive valores negativos são interpretados como true
	
			break;
	
		case ERROR:
			cellVal = "\"ERROR:" + this.cellValRaw.toString() + '"';
			break;
	
		case FORMULA:
			// A formula could result in a string value,
			// so always add double-quote characters.
			cellVal = '"' + this.cellValRaw.toString() + '"';
			break;
	
		case INLINESTR:
	
			// TODO: have seen an example of this, so it's untested.
			XSSFRichTextString rtsi = new XSSFRichTextString(this.cellValRaw.toString());
			cellVal = rtsi.toString();
			break;
	
		case SSTINDEX:
	
			String sstIndex = this.cellValRaw.toString();
			try {
				int idx = Integer.parseInt(sstIndex);
				XSSFRichTextString rtss = new XSSFRichTextString(this.stringsTable.getEntryAt(idx));
				cellVal = rtss.toString();
			} catch (NumberFormatException ex) {
				System.out.println("Failed to parse SST index '" + sstIndex + "': " + ex.toString());
			}
			break;
	
		case NUMBER:
	
			String numVal = this.cellValRaw.toString();
			cellVal = Double.parseDouble(numVal);
	
			break;
	
		case DATE:
			String dateVal = this.cellValRaw.toString();
			cellVal = DateUtil.getJavaDate(Double.parseDouble(dateVal));
	
			break;
	
		default:
			cellVal = "(TODO: Unexpected type: " + this.dataType + ")";
			break;
		}
		return cellVal;
	}

}