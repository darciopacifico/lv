package br.com.mapfre.lv.layoutmodel;

import java.io.Serializable;

/**
 * Contém dado e metadado referente a uma celular de uma planilha excel.
 * 
 * Será utilizado para composicao de um novo LayoutModelVO.
 * 
 * @author darcio
 * 
 */
public class CellValueVO implements Serializable {
	private static final long serialVersionUID = -3686867959105641367L;

	private Integer linha;
	private Integer coluna;

	private Serializable val;
	private String valRaw;
	
	
	public String  getValRaw() {
		return valRaw;
	}

	public void setValRaw(String strVal) {
		this.valRaw = strVal;
	}

	public Integer getLinha() {
		return linha;
	}

	public Integer getColuna() {
		return coluna;
	}

	public Serializable getVal() {
		return val;
	}

	public void setLinha(Integer linha) {
		this.linha = linha;
	}

	public void setColuna(Integer coluna) {
		this.coluna = coluna;
	}

	public void setVal(Serializable val) {
		this.val = val;
	}

}