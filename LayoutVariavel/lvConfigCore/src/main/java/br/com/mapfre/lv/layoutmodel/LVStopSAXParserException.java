/**
 * 
 */
package br.com.mapfre.lv.layoutmodel;

import org.xml.sax.SAXException;

/**
 * Exception para sinalizar parada da leitura do saxParser
 * @author darcio
 */
public class LVStopSAXParserException extends SAXException {
	private static final long serialVersionUID = -3162742940878248722L;

	/**
	 * 
	 */
	public LVStopSAXParserException() {
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public LVStopSAXParserException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param e
	 */
	public LVStopSAXParserException(Exception e) {
		super(e);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 * @param e
	 */
	public LVStopSAXParserException(String message, Exception e) {
		super(message, e);
		// TODO Auto-generated constructor stub
	}

}
