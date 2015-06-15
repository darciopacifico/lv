package br.com.mapfre.lv.jobmanager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Future;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.Transient;

import br.com.mapfre.lv.payload.LotVO;

/**
 * Registro do ciclo de vida do processamento de um arquivo no LV.
 * 
 * @author darcio
 *
 */
@Entity
public class FileVO implements Serializable {
	
	private static final long serialVersionUID = 7356226611286087193L;
	
	private Long PK;
	private String fileName;
	private String checkSum;
	private Integer fileSize;
	private float percentualMaxErros = 0.2f;

	private Future<Long> futurePK; 
	private List<LotVO> lotVOs = new ArrayList<LotVO>();
	
	private Date dt_criacao; 
	private Date dt_finalizacao; 
	
	public FileVO() {
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getPK() {
		return this.PK;
	}
	
	public String getFileName() {
		return fileName;
	}

	public String getCheckSum() {
		return checkSum;
	}

	public Integer getFileSize() {
		return fileSize;
	}

	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true)
	@JoinColumn(name="filePK")
	public List<LotVO> getLotVOs() {
		return lotVOs;
	}

	@Column(nullable=true)
	public float getPercentualMaxErros() {
		return percentualMaxErros;
	}

	@Transient
	public Future<Long> getFuturePK() {
		return futurePK;
	}

	public Date getDt_criacao() {
		return dt_criacao;
	}

	public Date getDt_finalizacao() {
		return dt_finalizacao;
	}

	
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	public void setCheckSum(String checkSum) {
		this.checkSum = checkSum;
	}
	
	public void setPK(Long PK) {
		this.PK = PK;
	}
	
	public void setFileSize(Integer fileSize) {
		this.fileSize = fileSize;
	}
	
	public void setLotProcess(List<LotVO> payloadLot) {
		this.lotVOs = payloadLot;
	}
	
	public void setPercentualMaxErros(float percentualMaxErros) {
		this.percentualMaxErros = percentualMaxErros;
	}

	public void setLotVOs(List<LotVO> payloadLot) {
		this.lotVOs = payloadLot;
	}

	public void setDt_criacao(Date dt_criacao) {
		this.dt_criacao = dt_criacao;
	}

	public void setDt_finalizacao(Date dt_finalizacao) {
		this.dt_finalizacao = dt_finalizacao;
	}

	public void setFuturePK(Future<Long> futurePK) {
		this.futurePK = futurePK;
	}

}
