package br.com.mapfre.lv.layoutmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Entidade persistente. 
 * 
 * Registra o metamodelo de um parametro de serviço juntamente com as validações mínimas a serem aplicadas.
 * 
 * @author darcio
 */
@Entity
public class ServiceParamVO implements Serializable {
	private static final long serialVersionUID = 488082293317896117L;
	
	private Long PK;
	private String name;
	private Integer fieldOrder;
	private Boolean fieldRequired;
	private Double minSize;
	private Double maxSize;
	private String regex;
	private Class<?> fieldType;
	

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getPK() {
		return PK;
	}

	@org.hibernate.annotations.Type(type = "br.com.mapfre.lv.layoutmodel.LVClassType")
	@Column(length=200)
	public Class<?> getFieldType() {
		return fieldType;
	}
	
	public Integer getFieldOrder() {
		return fieldOrder;
	}

	public String getName() {
		return name;
	}

	public Boolean getFieldRequired() {
		return fieldRequired;
	}

	public Double getMinSize() {
		return minSize;
	}

	public Double getMaxSize() {
		return maxSize;
	}

	public String getRegex() {
		return regex;
	}

	public void setFieldOrder(Integer order) {
		this.fieldOrder = order;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setFieldRequired(Boolean mandatory) {
		this.fieldRequired = mandatory;
	}

	public void setMinSize(Double minSize) {
		this.minSize = minSize;
	}

	public void setMaxSize(Double maxSize) {
		this.maxSize = maxSize;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public void setFieldType(Class<?> type) {
		this.fieldType = type;
	}

	public void setPK(Long pk) {
		this.PK = pk;
	}	
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj==null || !(obj instanceof ServiceParamVO) || this.PK==null){
			return false;
		}
		
		ServiceParamVO serviceParamVO = (ServiceParamVO) obj;
		
		return this.PK.equals(serviceParamVO.getPK()); 
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.PK).hashCode();
	}
	
}


