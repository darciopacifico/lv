package br.com.mapfre.lv.vidateste;


import java.io.Serializable;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * VO de vida teste
 * @author darcio
 */

@Entity
public class VidaVO implements Serializable {

	private static final long serialVersionUID = -5299215441215213939L;

	private Long PK;
	private String nome;
	private String sobrenome;
	
	private Date dtNasc;
	private Date dtInclusao;
	
	private Boolean fuma;
	
	/*
	private String endereco;
	private String telefone;
	private String bairro;
	private String cidade;
	private String apelido;
	private String field0;
	private String field1;
	private String field2;
	private String field3;
	private String field4;
	private String field5;
	private String field6;
	private String field7;
	private String field8;
	private String field9;
	private String field10;
	private String field11;
	private String field12;
	private String field13;
	private String field14;
	private String field15;
	private String field16;
	private String field17;
	private String field18;
	private String field19;
	private String field20;
	private String field21;
	private String field22;
	private String field23;
	private String field24;
	private String field25;
	private String field26;
	private String field27;
	private String field28;
	private String field29;
	private String field30;
	*/
	
	public VidaVO() {
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getPK() {
		return PK;
	}

	public String getNome() {
		return nome;
	}

	public String getSobrenome() {
		return sobrenome;
	}


	public void setNome(String nome) {
		this.nome = nome;
	}

	public void setSobrenome(String sobrenome) {
		this.sobrenome = sobrenome;
	}

	public Date getDtNasc() {
		return dtNasc;
	}

	public Date getDtInclusao() {
		return dtInclusao;
	}

	public void setDtNasc(Date dtNasc) {
		this.dtNasc = dtNasc;
	}

	public void setDtInclusao(Date dtInclusao) {
		this.dtInclusao = dtInclusao;
	}

	public void setPK(Long pK) {
		PK = pK;
	}

	public Boolean getFuma() {
		return fuma;
	}

	public void setFuma(Boolean fuma) {
		this.fuma = fuma;
	}
	
	
	
	
	
	
}
