package br.com.mapfre.lv.payload;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinColumns;
import javax.persistence.Lob;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import br.com.mapfre.lv.layout.FaultVO;

/**
 * Lote de payloads para serem processados. Normalmente cada item no map é uma linha de um TXT ou XLS.
 * @author darcio
 *
 */
@Entity
public class LotVO implements Serializable{

	private static final long serialVersionUID = 5449728873915513868L;
	
	private LotPK PK;
	private Integer fromLine;
	private Integer toLine;
	private String results;
	
	private Date dtCriacao; 
	private Date dtIniciacao; 
	private Date dtFinalizacao; 
	
	private List<FaultVO> faults = new ArrayList<FaultVO>(20);
	
	private List<PayloadVO> payloadVOs = new ArrayList<PayloadVO>(200);

	
	private EnumLotStatus status;

	
	public LotVO(){
		payloadVOs = new ArrayList<PayloadVO>(100);
	}

	public LotVO(int lotSize){
		payloadVOs = new ArrayList<PayloadVO>(lotSize);
	}
	

	@Id
	public LotPK getPK() {
		return PK;
	}

	/**
	 * O conteúdo do lote não será persistido para evitar o consumo excessivo de disco.
	 * A solução deverá ser capaz de recuperar o conteúdo das linhas para análise a partir do arquivo original e do numero da linha.
	 * @return
	 */

	@Transient
	public List<PayloadVO> getPayloadVOs() {
		return payloadVOs;
	}

	@Transient
	public boolean isLotOK() {
		//TODO: TESTA SE O LOTE ESTÁ OK
		return true;
	}

	public void setPayloadVOs(List<PayloadVO> payloadVOs) {
		this.payloadVOs = payloadVOs;
	}

	public Integer getFromLine() {
		return fromLine;
	}

	public Integer getToLine() {
		return toLine;
	}

	public void setFromLine(Integer from) {
		this.fromLine = from;
	}

	public void setToLine(Integer to) {
		this.toLine = to;
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.DEFAULT_STYLE);
	}

	public void setResults(String strResults) {
		this.results = strResults;
	}

	
	
	@Lob
	public String getResults() {
		return results;
	}

	public void setPK(LotPK pK) {
		PK = pK;
	}

	public Date getDtCriacao() {
		return dtCriacao;
	}

	public Date getDtIniciacao() {
		return dtIniciacao;
	}

	public Date getDtFinalizacao() {
		return dtFinalizacao;
	}

	public void setDtCriacao(Date dt_criacao) {
		this.dtCriacao = dt_criacao;
	}

	public void setDtIniciacao(Date dt_iniciacao) {
		this.dtIniciacao = dt_iniciacao;
	}

	public void setDtFinalizacao(Date dt_finalizacao) {
		this.dtFinalizacao = dt_finalizacao;
	}

	@Transient
	public Integer getLotSize() {
		
		if(payloadVOs==null){
			return 0;
		}
		
		return this.payloadVOs.size();
	}

	@Enumerated(EnumType.STRING)
	public EnumLotStatus getStatus() {
		return status;
	}

	public void setStatus(EnumLotStatus status) {
		this.status = status;
	}
/*
 */
	@OneToMany
	@JoinColumns(
			{
				@JoinColumn(name="filePK"),
				@JoinColumn(name="lotPK"),
				
				}
			)
	public List<FaultVO> getFaults() {
		return faults;
	}

	public void setFaults(List<FaultVO> faults) {
		this.faults = faults;
	}
	
}
