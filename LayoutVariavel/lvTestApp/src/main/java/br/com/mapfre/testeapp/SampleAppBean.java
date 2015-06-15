package br.com.mapfre.testeapp;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import br.com.mapfre.lv.LVException;
import br.com.mapfre.lv.LVRuntimeException;
import br.com.mapfre.lv.connector.IBridge;
import br.com.mapfre.lv.executor.IProcessMonitor;
import br.com.mapfre.lv.jobmanager.FileVO;
import br.com.mapfre.lv.jobmanager.JobManagerXLSXEventModel;
import br.com.mapfre.lv.layout.XLSXLayoutParser;
import br.com.mapfre.lv.layoutmodel.LayoutModelVO;
import br.com.mapfre.lv.layoutmodel.ServiceModelVO;

/**
 * 
 * @author darcio
 */
@ManagedBean
@SessionScoped
@Scope(value = "session")
@Component
public class SampleAppBean implements Serializable {

	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	private static final Logger log = LoggerFactory.getLogger(SampleAppBean.class);
	
	private static final long serialVersionUID = 4808163906911376622L;
	
	@Autowired
	private ApplicationContext context;
	
	private String companhia;
	private String sucursal;
	private String apolice;
	private Date vigencia;
	
	private String nomeArquivo;
	private Integer tamanho;
	
	private LayoutModelVO layoutModelVO;
	
	private FileVO fileVOProcessing;

	//classe que reporta o andamento do processo
	@Qualifier("persistentProcessMonitor")
	@Autowired
	private IProcessMonitor processMonitor;
	
	/**
	 * Salva arquivo em diretório temporário
	 * @param event
	 * @throws Exception
	 */
	public void uploadListener(FileUploadEvent event) throws Exception {
		
		UploadedFile uploadedFile = event.getUploadedFile();
		InputStream fis = uploadedFile.getInputStream();
		
		File fileDestination = getFileDestination();
		FileOutputStream fos = new FileOutputStream(fileDestination);
	
		this.tamanho = StreamUtils.copy(fis, fos);
		this.nomeArquivo =fileDestination.getAbsolutePath();
				

		fos.flush();
		fis.close();
		fos.close();
	}
	

	/**
	 * Cria arquivo de destino temporario para processamento do XLSX.
	 * @return
	 */
	protected File getFileDestination() {
		File fTempDir = new File(System.getProperty("java.io.tmpdir"));
		
		//File fTempDir = new File("/home/darcio/lixo/");
		
		
		String fileName = "lvProcessar_" + UUID.randomUUID()+".xlsx";
		
		File uploadedFile = new File(fTempDir, fileName);
		
		if (log.isDebugEnabled()) {
			log.debug("Local de destino: {}.", new Object[] { uploadedFile.getAbsolutePath()});
		}
		return uploadedFile;
	} 

	
	/**
	 * 
	 */
	public void processarArquivo(){
		
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		Map<String, Serializable> substituteVals = new HashMap<String, Serializable>();
		
	
		JobManagerXLSXEventModel eventModel = new JobManagerXLSXEventModel();
		
		
		XLSXLayoutParser layoutParser = new XLSXLayoutParser(this.layoutModelVO, substituteVals);
		eventModel.setLayoutParser(layoutParser);
		
		//"vidaBridge"

		IBridge bridge = getBridge(this.layoutModelVO);
		
		eventModel.setBridge(bridge);
		eventModel.setProcessMonitor(this.processMonitor);
		
		
		try {
			
			FileVO fileVOProcessing = eventModel.startProcess(params, new FileInputStream(this.nomeArquivo), this.nomeArquivo );
			setFileVOProcessing(fileVOProcessing);
			
		} catch (FileNotFoundException e) {
			throw new LVRuntimeException("Erro ao tentar iniciar processamento do arquivo.",e);
			
		} catch (LVException e) {
			throw new LVRuntimeException("Erro ao tentar iniciar processamento do arquivo.",e);
			
		}

		
		String message = "Processamento de arquivo iniciado!";
		FacesContext context = FacesContext.getCurrentInstance();
		context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
		
	}
	
	
	/**
	 * Recupera todos os arquivos processados
	 * @return
	 */
	public List<FileVO> getFiles(){
		
		DetachedCriteria criteria = DetachedCriteria.forClass(FileVO.class);
		
		List<FileVO> files = hibernateTemplate.findByCriteria(criteria);
		
		return files;
	}
	

	/**
	 * Recupera implementação de bridge
	 * @param layoutModel
	 * @return
	 */
	protected IBridge getBridge(LayoutModelVO layoutModel) {
		
		if(layoutModel==null){
			throw new LVRuntimeException("Erro ao tentar recuperar implementacao de bridge. LayoutModel==null");
		}
		
		hibernateTemplate.load(layoutModel, layoutModel.getPK());

		
		ServiceModelVO serviceModelVO = layoutModel.getServiceModelVO();
		
		if(serviceModelVO==null){
			throw new LVRuntimeException("Erro ao tentar recuperar implementacao de bridge. ServiceModel==null");
		}
		
		String bridgeName = serviceModelVO.getBridgeName();

		if(StringUtils.isBlank(bridgeName)){
			throw new LVRuntimeException("Erro ao tentar recuperar implementacao de bridge. Nome da bridge não definido");
		}
		
		Object bean = context.getBean(bridgeName);
		
		if(bean==null || !(bean instanceof IBridge)){
			throw new LVRuntimeException("O bean definido como bridge é inválido:" + bean);
		}
		
		IBridge bridge = (IBridge) bean;
		
		return bridge;
	}
	
	
	public String getCompanhia() {
		return companhia;
	}

	public String getSucursal() {
		return sucursal;
	}

	public String getApolice() {
		return apolice;
	}

	public Date getVigencia() {
		return vigencia;
	}

	public void setCompanhia(String companhia) {
		this.companhia = companhia;
	}

	public void setSucursal(String sucursal) {
		this.sucursal = sucursal;
	}

	public void setApolice(String apolice) {
		this.apolice = apolice;
	}

	public void setVigencia(Date vigencia) {
		this.vigencia = vigencia;
	}
	
	public String getNomeArquivo() {
		return nomeArquivo;
	}


	public Integer getTamanho() {
		return tamanho;
	}


	public LayoutModelVO getLayoutModelVO() {
		return layoutModelVO;
	}



	/**
	 * Retorna todos os LayoutModels para teste
	 * @return
	 */
	public List<SelectItem> getLayoutModels() {
		DetachedCriteria criteria = DetachedCriteria.forClass(LayoutModelVO.class);
		List<LayoutModelVO> lms = null;
		
		try{
			lms = hibernateTemplate.findByCriteria(criteria );
		}catch(Exception e){
			throw new LVRuntimeException("Erro ao tentar recuperar layout models!",e);
		}
		
		List<SelectItem> lmItems = new ArrayList<SelectItem>(lms.size());
		
		for (LayoutModelVO layoutModelVO : lms) {
			SelectItem item = new SelectItem(layoutModelVO,layoutModelVO.getNome());
			lmItems.add(item);
		}
		
		return lmItems;
	}


	public void setNomeArquivo(String nomeArquivo) {
		this.nomeArquivo = nomeArquivo;
	}


	public void setTamanho(Integer tamanho) {
		this.tamanho = tamanho;
	}


	public void setLayoutModelVO(LayoutModelVO layoutModelVO) {
		this.layoutModelVO = layoutModelVO;
	}


	public FileVO getFileVOProcessing() {
		return fileVOProcessing;
	}


	public void setFileVOProcessing(FileVO fileVOProcessing) {
		
		DetachedCriteria criteria = DetachedCriteria.forClass(FileVO.class);
		
		criteria.setFetchMode("lotVOs", FetchMode.JOIN);
		criteria.add(Restrictions.eq("PK", fileVOProcessing.getPK()));
		
		List<FileVO> list = hibernateTemplate.findByCriteria(criteria);
		
		if(list.size()>0){
			this.fileVOProcessing = list.get(0);
		}else{
			log.error("Arquivo não encontrado!");
		}
		
	}


}
