package br.com.mapfre.lv.payload;

import java.io.Serializable;

import javax.persistence.Embeddable;

/**
 * Chave composta para lote
 * @author darcio
 */

@Embeddable
public class LotPK implements Serializable {
	private static final long serialVersionUID = 2144981983860539871L;
	
	private Long lotPK;
	private Long filePK;
	
	public Long getLotPK() {
		return lotPK;
	}
	public Long getFilePK() {
		return filePK;
	}
	public void setLotPK(Long lotPK) {
		this.lotPK = lotPK;
	}
	public void setFilePK(Long filePK) {
		this.filePK = filePK;
	}
	
	
	

}
