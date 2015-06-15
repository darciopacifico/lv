package br.com.mapfre.lv.jobmanager;

/**
 * Enum de resultados referentes ao parseamento do valor de um campo
 * @author darcio
 */
public enum EnumParseResult {
	SUCESSO(0), 
	ERR_TIPO_INCOMPATIVEL(-91), 
	ERROR(-92), 
	ERR_TAMANHO_INCOMPATIVEL(-93),
	ERR_VALIDACAO(-94),
	ERR_DESCONHECIDO(-95), 
	ERR_MANDATORY(-96), 
	ERR_RANGE(-97), 
	ERR_REGEX(-98), 
	ERR_INESPERADO(-99), 
	ERR_RUNTIME(-100);
		
	int code;
	
	private EnumParseResult(int res) {
		this.code = res;
	}

	public int getCode() {
		return code;
	}

	public void setCode(int res) {
		this.code = res;
	}
	
	
	
	
}
