package br.com.mapfre.lv.layoutmodel;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletResponse;
import javax.validation.ConstraintViolation;

import org.hibernate.SessionFactory;
import org.richfaces.event.FileUploadEvent;
import org.richfaces.model.UploadedFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;

import br.com.mapfre.lv.LVRuntimeException;

/**
 * 
 * 
 * @author darcio
 */
@ManagedBean
@SessionScoped
@Scope(value = "session")
@Component
public class LayoutJSFBean implements Serializable {
	private static final ArrayList<SelectItem> SERVICE_FIELDS_LISTA_VAZIA = new ArrayList<SelectItem>(0);
	private static final long serialVersionUID = -8907511064599037068L;
	private static final Logger log = LoggerFactory.getLogger(LayoutJSFBean.class);

	private LayoutModelVO layoutModelVO = new LayoutModelVO();

	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	@Autowired
	private ILayoutBusiness layoutBusiness;
	
	private String nomeLayout; 
	private ServiceModelVO serviceModelPesquisa;
	
	public ServiceModelVO getServiceModelPesquisa() {
		return serviceModelPesquisa;
	}


	public void setServiceModelPesquisa(ServiceModelVO serviceModelPesquisa) {
		this.serviceModelPesquisa = serviceModelPesquisa;
	}


	private List<LayoutModelVO> findResults = null;
	

	/**
	 * 
	 */
	public void pesquisar(){
		
		this.findResults = layoutBusiness.findLayoutModels(this.nomeLayout, this.serviceModelPesquisa);
	}

	
	public void novo(){
		this.layoutModelVO = new LayoutModelVO();
		
	}
	
	
	/**
	 * Inicia um processode edicao de um modelo de layout
	 * @param layoutModelVO
	 */
	public void editar(LayoutModelVO layoutModelVO){
		
		this.layoutModelVO = layoutModelVO;
		

		ByteArrayInputStream bais = new ByteArrayInputStream(this.layoutModelVO.getArquivo().getFileBytes());
		
		//Lê o XLSX informado, carrega as definições de colunas encontradas e os dados de exemplo
		layoutBusiness.parseXLSXFile(this.layoutModelVO, bais, this.layoutModelVO.getArquivo().getLinhasExemplo());

		
	}


	public void excluir(){
		
		if(this.layoutModelVO!=null && this.layoutModelVO.getPK()!=null){
			layoutBusiness.excluir(layoutModelVO);
		
			pesquisar();
		
			String message = "Layout excluído com sucesso!";
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
		
		}else{
			log.error("O comando excluir foi acionado, mas nenhum layout model foi selecionado para exclusao!");
		}
	}
	
	/**
	 * 
	 * @param layoutModelVO
	 */
	public void confirmarExclusao(LayoutModelVO layoutModelVO){
		this.layoutModelVO = layoutModelVO;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<LayoutModelVO> getListModel(){
		
		if(this.findResults==null){
			this.findResults = layoutBusiness.findLayoutModels(this.nomeLayout, this.serviceModelPesquisa);
		}
		
		return findResults;
	}
	
	
	
	/**
	 * Processa o download dos arquivos de prova como arquivo PDF
	 * @param examReportBytes
	 */
	public void downloadFile(ArquivoVO arquivo) {
		
		if(arquivo==null || arquivo.isNotInvalidToDownload()){
			
			String message = "Não foi possível processar o download do arquivo!";
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));

			return;
		}
		
		
		byte[] bytesPDF = arquivo.getFileBytes();
		
		String fileName = arquivo.getNome();
		
		HttpServletResponse response = prepareHttpResponse(bytesPDF, fileName , "application/xlsx");

		ByteArrayInputStream bais = new ByteArrayInputStream(bytesPDF);
		writeFileDownloadResponse(bais, response);
	}
	
	
	
	/**
	 * Prepara httpResponse para a saida de bytes do arquivo pdf (1 relatorio) ou zip (>1 relatorio)
	 * 
	 * @param eventVO
	 * @param bytesFile
	 * @param contentType TODO
	 * @param fileNamePattern TODO
	 * @return
	 */
	protected HttpServletResponse prepareHttpResponse(byte[] bytesFile, String fileName, String contentType) {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		
		HttpServletResponse httpResp = (HttpServletResponse) context.getResponse();

		httpResp.setHeader("Content-Disposition", "attachment;filename=\"" + fileName + "\"");
		
		httpResp.setContentLength((int) bytesFile.length );
		
		httpResp.setContentType(contentType);
		
		return httpResp;
	}
	

	/**
	 * Joga array de bytes do arquivo em httpServletResponde para download
	 * 
	 * @param bais
	 * @param response
	 */
	protected void writeFileDownloadResponse(ByteArrayInputStream bais, HttpServletResponse response) {
		try {
			OutputStream out = response.getOutputStream();

			byte[] buf = new byte[1024];
			int count;
			while ((count = bais.read(buf)) >= 0) {
				out.write(buf, 0, count);
			}
			bais.close();
			out.flush();
			out.close();
			FacesContext.getCurrentInstance().responseComplete();

		} catch (IOException e) {
			throw new LVRuntimeException("Erro ao tentar enviar stream de arquivo para download!", e);
		}
	}
	
	
	/**
	 * 
	 */
	public void salvar(){
		
		try {
			layoutBusiness.saveOrUpdate(this.layoutModelVO);
			
			FacesContext context = FacesContext.getCurrentInstance();
			String message="Registro salvo com sucesso!";
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, message, message));
		
			pesquisar();
			
			
		} catch (LvConfigException e) {
			Set<ConstraintViolation<LayoutModelVO>> faults = e.getFaults();
			for (ConstraintViolation<LayoutModelVO> constraintViolation : faults) {
				
				String message = constraintViolation.getMessage();

				FacesContext context = FacesContext.getCurrentInstance();
				context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
				
				log.warn( message);
				
			}
		} catch (Exception e) {
			String message="Erro não identificado ao tentar salvar registro. Verificar Log!";
			log.error(message,e);
			FacesContext context = FacesContext.getCurrentInstance();
			context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, message, message));
			
		}
		
	}
	


	/**
	 * Pesquisa todos os 
	 * @return
	 */
	public List<SelectItem> getServiceFields() {
		ServiceModelVO serviceModelVO = layoutModelVO.getServiceModelVO();
		if( serviceModelVO !=null){
			
			List<ServiceParamVO> fields = serviceModelVO.getServiceFields(); 
			List<SelectItem> items = new ArrayList<SelectItem>(fields.size());
			
			for (ServiceParamVO field : fields) {
				
				String itemLabel = "("+field.getFieldType().getSimpleName()+") "+field.getName();
				
				SelectItem selectItem = new SelectItem(field,itemLabel);
				
				items.add(selectItem);
			}
			
			return items;
			
		}else{
			return SERVICE_FIELDS_LISTA_VAZIA;
		}
	}
	
	/**
	 * 
	 * @return
	 */
	public List<SelectItem> getServiceModels(){
		List<ServiceModelVO> services = this.layoutBusiness.getServiceModels();
		List<SelectItem> items = new ArrayList<SelectItem>(services.size());
		
		for (ServiceModelVO serviceModelVO : services) {
			items.add(new SelectItem(serviceModelVO, serviceModelVO.getNome()));
		}

		return items;
	}
	
	/**
	 * 
	 * @return
	 */
	public List<Integer> getKeyFields(){
		List<Integer> keys = new ArrayList<Integer>(layoutModelVO.getFields().keySet());
		return keys;
	}

	/**
	 * Processa o upload de um XLSX de exemplo
	 * 
	 * @param event
	 * @throws Exception
	 */
	public void uploadListener(FileUploadEvent event) throws Exception {
		UploadedFile item = event.getUploadedFile();
		
		//reseta definicoes atreladas ao ultimo arquivo
		//prepara o registro de ArquivoVO para receber os dados novos
		resetTemplateFileDefinitions();
		
		//apenas atualiza o nome do arquivo
		this.layoutModelVO.getArquivo().setNome(item.getName());

		//Lê o XLSX informado, carrega as definições de colunas encontradas, dados de exemplo e checksum
		layoutBusiness.parseXLSXFile(this.layoutModelVO, item.getInputStream(),this.layoutModelVO.getArquivo().getLinhasExemplo());
		
	}


	/**
	 * Apaga todas as definições de layout e dados de exemplo. Reseta todas as definicoes referentes ao último arquivo
	 * 
	 * Normalmente acionado após o upload de um novo arquivo de exemplo, onde as definições antigas passa a ser totalmente inconsistentes.
	 */
	protected void resetTemplateFileDefinitions() {
		
		Collection<LayoutColumnVO> fields = this.layoutModelVO.getFields().values();

		if(!fields.isEmpty()){
			this.layoutBusiness.excluir(fields);
			this.layoutModelVO.getFields().clear();
		}
		
		this.layoutModelVO.getValues().clear();
		this.layoutModelVO.getArquivo().setChecksum(null);
		this.layoutModelVO.getArquivo().setNome(null);
		this.layoutModelVO.getArquivo().setFileBytes(null);
	}
	
	/**
	 * 
	 * @param vals
	 * @return
	 */
	public List<Integer> fieldKeys(Map<Integer, CellValueVO> vals){
		Set<Integer> keys = vals.keySet();
		List<Integer> lKeys = new ArrayList<Integer>(keys.size());
		lKeys.addAll(keys);
		return lKeys;
		
	}
	
	public LayoutModelVO getLayoutModelVO() {
		return layoutModelVO;
	}

	public void setLayoutModelVO(LayoutModelVO layoutModelVO) {
		this.layoutModelVO = layoutModelVO;
	}

	
	public ServiceModelVO getServiceModelVO() {
		return this.layoutModelVO.getServiceModelVO();
	}


	public void setServiceModelVO(ServiceModelVO serviceModelVO) {
		layoutModelVO.setServiceModelVO(serviceModelVO);
		layoutModelVO.resetFields();
	}



	public String getNomeLayout() {
		return nomeLayout;
	}


	public void setNomeLayout(String nomeLayout) {
		this.nomeLayout = nomeLayout;
	}

}
