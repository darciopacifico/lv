package br.com.mapfre.lv.payload;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Contem os dados referentes a uma linha de um arquivo.
 * 
 * Os nomes e valores dos campos estão contidos em fields
 * @author darcio
 */
public class PayloadVO implements Serializable{
	
	private static final long serialVersionUID = -7126875232154635438L;
	
	private Map<Serializable, Serializable> fields = new HashMap<Serializable, Serializable>(30);
	
	private Boolean processar = true;
	
	private Integer linha;
	
	
	/**
	 * 
	 * @return
	 */
	public Map<Serializable, Serializable> getFields(){
		return this.fields;
	}

	/**
	 * 
	 * @param fields
	 */
	public void setFields(Map<Serializable, Serializable> fields) {
		this.fields = fields;
	}

	public Boolean getProcessar() {
		return processar;
	}

	public void setProcessar(Boolean processar) {
		this.processar = processar;
	}

	public Integer getLinha() {
		return linha;
	}

	public void setLinha(Integer linhaAtual) {
		this.linha = linhaAtual;
	}

}
