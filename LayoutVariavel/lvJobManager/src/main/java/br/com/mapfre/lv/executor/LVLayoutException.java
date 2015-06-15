package br.com.mapfre.lv.executor;

/**
 * Algum erro durante a leitura da planilha e aplicacao do layout
 * @author darcio
 *
 */
public class LVLayoutException extends Exception {

	private static final long serialVersionUID = -873991388816894412L;

	public LVLayoutException() {
		// TODO Auto-generated constructor stub
	}

	public LVLayoutException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	public LVLayoutException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public LVLayoutException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

}
