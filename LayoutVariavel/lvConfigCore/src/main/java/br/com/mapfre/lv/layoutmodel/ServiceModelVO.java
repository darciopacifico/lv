package br.com.mapfre.lv.layoutmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

/**
 * Entidade persistente. Registra o metamodelo de um serviço e seus parametros de chamada (@ServiceParamVO)
 * 
 * @author darcio
 */
@Entity
public class ServiceModelVO implements Serializable {
	private static final long serialVersionUID = -6715241268583900796L;
	
	private Long PK;
	
	private String nome;
	public List<ServiceParamVO> serviceFields = new ArrayList<ServiceParamVO>();
	
	private String bridgeName;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getPK() {
		return PK;
	}
	
	
	@OneToMany(cascade=CascadeType.ALL, orphanRemoval=true, fetch=FetchType.EAGER)
	@Fetch(FetchMode.SELECT)
	@JoinColumn
	public List<ServiceParamVO> getServiceFields() {
		return serviceFields;
	}

	
	
	public String getNome() {
		return nome;
	}

	
	public void setServiceFields(List<ServiceParamVO> serviceFields) {
		this.serviceFields = serviceFields;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}

	@Override
	public boolean equals(Object obj) {
		
		if(obj==null || !(obj instanceof ServiceModelVO) || this.PK==null){
			return false;
		}
		
		ServiceModelVO serviceModelVO = (ServiceModelVO) obj;
		
			
		return this.PK.equals(serviceModelVO.getPK()); 
	}


	public void setPK(Long pk) {
		this.PK = pk;
	}


	public String getBridgeName() {
		return bridgeName;
	}


	public void setBridgeName(String bridgeName) {
		this.bridgeName = bridgeName;
	}
	
}
