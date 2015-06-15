package br.com.mapfre.lv.layout;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;

import br.com.mapfre.lv.jobmanager.EnumParseResult;
import br.com.mapfre.lv.payload.FaultPK;

/**
 * 
 * @author darcio
 */
@Entity
public class FaultVO implements Serializable {
	private static final long serialVersionUID = -5549566141202288882L;

	private FaultPK faultPK;
		
	private EnumParseResult parseResult;
	private Integer linha;
	
	private Integer coluna;
	private String nomeColuna;
	private String valorOriginal;
	private String msg;

	
	public FaultVO() {
		// TODO Auto-generated constructor stub
	}
	
	@EmbeddedId
	public FaultPK getFaultPK() {
		return faultPK;
	}

	
	
	@Enumerated(EnumType.STRING)
	public EnumParseResult getParseResult() {
		return parseResult;
	}
	public Integer getLinha() {
		return linha;
	}
	public Integer getColuna() {
		return coluna;
	}
	
	@Column(length=150)
	public String getValorOriginal() {
		return valorOriginal;
	}
	public void setParseResult(EnumParseResult parseResult) {
		this.parseResult = parseResult;
	}
	public void setLinha(Integer linha) {
		this.linha = linha;
	}
	public void setColuna(Integer coluna) {
		this.coluna = coluna;
	}
	public void setValorOriginal(String valorOriginal) {
		this.valorOriginal = valorOriginal;
	}
	public String getMsg() {
		return msg;
	}
	public void setMsg(String msg) {
		this.msg = msg;
	}

	public String getNomeColuna() {
		return nomeColuna;
	}

	public void setNomeColuna(String nomeColuna) {
		this.nomeColuna = nomeColuna;
	}
	public void setFaultPK(FaultPK faultPK) {
		this.faultPK = faultPK;
	}

		
}