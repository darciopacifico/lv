package br.com.mapfre.lv.jobmanager;

import java.io.Serializable;
import java.util.Date;

import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class AbstractStatusVO implements Serializable {

	private static final long serialVersionUID = -8579916422003279186L;

	public static enum EStatus{
		CRIADO, INICIADO, FINALIZADO;		
	}
	
	private Date data;
	private EStatus status;
	private String obs;

	public Date getData() {
		return data;
	}

	@Enumerated(EnumType.STRING)
	public EStatus getStatus() {
		return status;
	}

	public String getObs() {
		return obs;
	}

	public void setData(Date data) {
		this.data = data;
	}

	public void setStatus(EStatus status) {
		this.status = status;
	}

	public void setObs(String obs) {
		this.obs = obs;
	}

}
