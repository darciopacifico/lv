package br.com.mapfre.lv.payload;

import java.io.Serializable;

import javax.persistence.Embeddable;

@Embeddable
public class FaultPK implements Serializable {

	private static final long serialVersionUID = -6806847947659884573L;

	private Long lotPK;
	private Long filePK;
	private Long faultPK;

	public Long getFaultPK() {
		return faultPK;
	}

	public void setFaultPK(Long faultPK) {
		this.faultPK = faultPK;
	}

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
