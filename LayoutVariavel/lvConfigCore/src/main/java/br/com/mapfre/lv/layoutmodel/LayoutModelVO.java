package br.com.mapfre.lv.layoutmodel;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MapKey;
import javax.persistence.OneToMany;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.collections.CollectionUtils;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.validator.constraints.NotBlank;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Entidade persistente. Registro de um modelo de layout para um modelo de serviço.
 * 
 * Possui colecao de LayoutColumnVO que mapeia cada coluna de um arquivo para um campo de um serviço.
 * 
 * @author darcio
 */
@Entity
@LayoutModelConsistency
public class LayoutModelVO implements Serializable {
 
	private static final int DEFAULT_HEADER_LINE = 1;
	private static final long serialVersionUID = 5585432280336587160L;
	
	private Long PK;
	private String nome;
	
	private ArquivoVO arquivo = new ArquivoVO(); 

	private Integer headerLine=DEFAULT_HEADER_LINE;

	private ServiceModelVO serviceModelVO;
	
	private Map<Integer, LayoutColumnVO> fields = new HashMap<Integer, LayoutColumnVO>(40);
	
	private List<Map<Integer, CellValueVO>> values = new ArrayList<Map<Integer, CellValueVO>>();
	
	private static final Logger log = LoggerFactory.getLogger(LayoutModelVO.class);	
	

	
	
	@NotBlank
	public String getNome() {
		return nome;
	}
	
	public void setNome(String nome) {
		this.nome = nome;
	}
	
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)	
	public Long getPK() {
		return PK;
	}
	
	@Fetch(FetchMode.SELECT)
	@ManyToOne(fetch=FetchType.LAZY,cascade=CascadeType.ALL )
	@JoinColumn
	public ArquivoVO getArquivo() {
		return arquivo;
	}

	public void setArquivo(ArquivoVO arquivo) {
		this.arquivo = arquivo;
	}

	@ManyToOne
	@JoinColumn
	@NotNull
	public ServiceModelVO getServiceModelVO() {
		return serviceModelVO;
	}
	
	
	@OneToMany(cascade=CascadeType.ALL, mappedBy="layoutModelVO",fetch=FetchType.EAGER)
	@Fetch(FetchMode.SUBSELECT)
	@MapKey(name="coluna")
	public Map<Integer, LayoutColumnVO> getFields() {
		return fields;
	}

	@Transient
	public List<Map<Integer, CellValueVO>> getValues() {
		return values;
	}
	
	@NotNull
	public Integer getHeaderLine() {
		return headerLine;
	}

	public void setHeaderLine(Integer headerLine) {
		this.headerLine = headerLine;
	}

	public void setValues(List<Map<Integer, CellValueVO>> values) {
		this.values = values;
	}
	/**
	 * Atribui novo service model o 
	 * @param serviceModelVO
	 */
	public void setServiceModelVO(ServiceModelVO serviceModelVO) {
		this.serviceModelVO = serviceModelVO;
	}
	
	/**
	 * Reseta campos
	 */
	public void resetFields(){
		Map<Integer, LayoutColumnVO> fields = getFields();
		Set<Integer> keys = fields.keySet();
		
		for (Integer key : keys) {
			fields.get(key).setServiceFieldVO(null);
		}
	}

	public void setPK(Long pK) {
		PK = pK;
	}

	public void setFields(Map<Integer, LayoutColumnVO> fields) {
		this.fields = fields;
	}
	
	
	@Transient
	public Collection<ServiceParamVO> getUnselectedFields() {
		
		if(getServiceModelVO()!=null){
			List<ServiceParamVO> serviceFields = getServiceModelVO().getServiceFields();
			List<ServiceParamVO> pickedSfs = getPickedFields();
			
			Collection<ServiceParamVO> sobras = CollectionUtils.subtract(serviceFields, pickedSfs);
			
			return sobras;
		}else{
			
			return new ArrayList<ServiceParamVO>(0);
			
		}
	}

	
	/**
	 * 
	 * @return
	 */
	@Transient
	public List<ServiceParamVO> getPickedFields() {
		
		Map<Integer, LayoutColumnVO> fields2 = getFields();
		
		ServiceModelVO sm = getServiceModelVO();
		
		Set<ServiceParamVO> pickedSfs = new HashSet<ServiceParamVO>(10);
		
		if(fields2!=null && sm!=null ){
			Collection<LayoutColumnVO> sfs = fields2.values();
			for (LayoutColumnVO lf : sfs) {
				
				if(lf!=null && lf.getServiceFieldVO()!=null){
					pickedSfs.add(lf.getServiceFieldVO());
				}
				
			}
		}
		return new ArrayList<ServiceParamVO>(pickedSfs);
	}
	
	
	@Transient
	public Map<ServiceParamVO, List<LayoutColumnVO>> getDuplicatedFields(){
		
		Map<ServiceParamVO, List<LayoutColumnVO>> duplicatedMap = new HashMap<ServiceParamVO, List<LayoutColumnVO>>();
				
		if(this.fields!=null){
			for (LayoutColumnVO layoutColumnVO : this.fields.values()) {
				ServiceParamVO serviceParamVO = layoutColumnVO.getServiceFieldVO();

				if(serviceParamVO!=null){
					
					List<LayoutColumnVO> lFiedls = getField(duplicatedMap, serviceParamVO);
					
					lFiedls.add(layoutColumnVO);
					
				}
			}
		}
		
		return duplicatedMap;
	}
	

	@Transient
	public List<ServiceParamVO> getDuplicatedKeys(){
		return new ArrayList<ServiceParamVO>(getDuplicatedFields().keySet());
	}  
	
	
	protected List<LayoutColumnVO> getField(Map<ServiceParamVO, List<LayoutColumnVO>> mapping, ServiceParamVO serviceParamVO) {
		List<LayoutColumnVO> lFiedls = mapping.get(serviceParamVO);
		
		if(lFiedls==null){
			lFiedls = new ArrayList<LayoutColumnVO>();
			mapping.put(serviceParamVO, lFiedls);
		}
		return lFiedls;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		if(obj==null || !(obj instanceof LayoutModelVO) || this.PK==null){
			return false;
		}
		
		LayoutModelVO layoutModel = (LayoutModelVO) obj;
		return this.PK.equals(layoutModel.getPK()); 
	}


}
