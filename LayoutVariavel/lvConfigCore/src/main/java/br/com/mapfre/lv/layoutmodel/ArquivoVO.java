package br.com.mapfre.lv.layoutmodel;

import java.io.Serializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;
import javax.persistence.Transient;
import javax.validation.constraints.NotNull;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.hibernate.validator.constraints.NotBlank;

/**
 * Registro 
 * @author darcio
 */
@Entity
public class ArquivoVO implements Serializable {
	
	private static final long serialVersionUID = 884577190568520311L;
	private static final int LINHAS_EXEMPLO_DEFAULT = 10;
	
	private Long PK;
	private String nome;
	private String checksum;
	
	private Integer linhasExemplo=LINHAS_EXEMPLO_DEFAULT;
	
	@NotNull
	public Integer getLinhasExemplo() {
		return linhasExemplo;
	}

	public void setLinhasExemplo(Integer linhasExemplo) {
		this.linhasExemplo = linhasExemplo;
	}

	private byte[] fileBytes;
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public Long getPK() {
		return PK;
	}
	
	@Column
	@Lob
	public byte[] getFileBytes() {
		return fileBytes;
	}

	public void setFileBytes(byte[] fileBytes) {
		this.fileBytes = fileBytes;
	}

	@NotBlank
	public String getNome() {
		return nome;
	}
	
	@NotBlank
	public String getChecksum() {
		return checksum;
	}

	
	public void setChecksum(String checksum) {
		this.checksum = checksum;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}
	public void setPK(Long pK) {
		PK = pK;
	}
	
	
	@Override
	public boolean equals(Object obj) {
		
		if(obj==null || !(obj instanceof ArquivoVO) || this.PK==null){
			return false;
		}
		
		ArquivoVO arquivo = (ArquivoVO) obj;
		
		return this.PK.equals(arquivo.getPK())  ; 
	}
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.PK).hashCode();
	}

	/**
	 * Testa se o arquivo possui 
	 * @return
	 */
	@Transient
	public boolean isNotInvalidToDownload() {
		return StringUtils.isBlank(this.nome) || this.fileBytes == null || this.fileBytes.length<1;
	}
	
}
