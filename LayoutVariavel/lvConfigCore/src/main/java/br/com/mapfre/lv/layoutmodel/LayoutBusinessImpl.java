/**
 * 
 */
package br.com.mapfre.lv.layoutmodel;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.Validator;
import javax.validation.ValidatorFactory;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.hibernate.FetchMode;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import br.com.mapfre.lv.LVRuntimeException;

/**
 * Implementa funcionalidades para manutenção de layout
 * 
 * @author darcio
 */
@Component
public class LayoutBusinessImpl implements ILayoutBusiness {
	private static final Logger log = LoggerFactory.getLogger(LayoutBusinessImpl.class);
	private static final String CHECKSUM_IMPLEMENTATION_MD5 = "MD5";
	private static final long serialVersionUID = -2553329139834630181L;
	private static final String SAX_PARSER = "org.apache.xerces.parsers.SAXParser";

	@Autowired
	private HibernateTemplate hibernateTemplate;

	protected ValidatorFactory validatorFactory = Validation.buildDefaultValidatorFactory();
	

	/**
	 * Implementacao de busca por layouts
	 */
	@Override
	public List<LayoutModelVO> findLayoutModels(String nome, ServiceModelVO serviceModelPesquisa){
		
		DetachedCriteria criteria = DetachedCriteria.forClass(LayoutModelVO.class);
		
		FetchMode mode = FetchMode.JOIN;
		criteria.setFetchMode("arquivo", mode );
		
		if(StringUtils.isNotBlank(nome)){
			criteria.add(Restrictions.ilike("nome", "%"+nome+"%"));
		}
		
		if(serviceModelPesquisa!=null){
			criteria.add(Restrictions.eq("serviceModelVO", serviceModelPesquisa));
		}
		
		List<LayoutModelVO> layouts = hibernateTemplate.findByCriteria(criteria);
		
		return layouts;
	}
	
	
	/**
	 * Exclusao simples
	 */
	public void excluir(LayoutModelVO layoutModelVO) {
		hibernateTemplate.delete(layoutModelVO);
	}
	
	/**
	 * Exclui os fields informados.
	 * 
	 * Atende ao processo de mudança de arquivo de exemplo de um layout durante a edição do mesmo
	 */
	public void excluir(Collection<LayoutColumnVO> fields) {
		hibernateTemplate.deleteAll(fields);
	}
	
	/**
	 * @throws LvConfigException 
	 * 
	 */
	@Override
	@Transactional
	public void saveOrUpdate(LayoutModelVO lm) throws LvConfigException{

		removeCamposNaoMapeados(lm);
		
		Set<ConstraintViolation<LayoutModelVO>> faults = validateLayoutModel(lm);
		
		if(!CollectionUtils.isEmpty(faults)){
			throw new LvConfigException("Erro ao tentar salvar layoutModel!",faults);
		}
		
		hibernateTemplate.saveOrUpdate(lm);
	}


	/**
	 * Remove todos os LayoutColumnVO não mapeados.
	 * @param lm
	 */
	protected void removeCamposNaoMapeados(LayoutModelVO lm) {
		Map<Integer, LayoutColumnVO> mapFields = lm.getFields();

		Set<Integer> keys = mapFields.keySet();
		
		Set<Integer> keysToRemove = new HashSet<Integer>();

		List<LayoutColumnVO> lfsToRemove = new ArrayList<LayoutColumnVO>();
		
		for (Integer key : keys) {
			LayoutColumnVO lf = mapFields.get(key);
			if(lf.getServiceFieldVO()==null){

				keysToRemove.add(key);
				
				if(lf.getPK()!=null){
					
					lfsToRemove.add(lf);
				}
			}
		}

		
		for (Integer key : keysToRemove) {
			mapFields.remove(key);
		}
		hibernateTemplate.deleteAll(lfsToRemove);
		
		
	}

	

	
	@Override
	public Set<ConstraintViolation<LayoutModelVO>> validateLayoutModel(LayoutModelVO lm) {
		Validator validator = validatorFactory.getValidator();
		Set<ConstraintViolation<LayoutModelVO>> faults = validator.validate(lm);
		
		for (ConstraintViolation<LayoutModelVO> constraintViolation : faults) {
			System.out.println(constraintViolation.getMessage());
		}
		return faults;
	}


	/**
	 * 
	 */
	@Override
	public void saveOrUpdate(ServiceModelVO serviceModelVO){
		hibernateTemplate.saveOrUpdate(serviceModelVO);
	}
	
	@Override
	public List<ServiceModelVO> getServiceModels() {
		
		DetachedCriteria criteria = DetachedCriteria.forClass(ServiceModelVO.class);
		
		List<ServiceModelVO> services = hibernateTemplate.findByCriteria(criteria);
		
		return services;
	}

	
	public List<ServiceModelVO> getServiceModelsMock() {
		List<ServiceModelVO> services = new ArrayList<ServiceModelVO>();
		
		Long pk=0l;
				
		{
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Incluir Vidas");
		//modelVO.setPK(pk++);
		
		long i = 1l;
		
		addServiceField(modelVO, i++, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, i++, "dtInclusao", 			true, Date.class);

		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);

		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, i++, "nome"+i, 						true, 2d, 60d, String.class,null);


		services.add(modelVO);
		}
		
		{
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Cotizador");
		//modelVO.setPK(pk++);

		addServiceField(modelVO, 1l, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 2l, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 3l, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, 4l, "dtInclusao", 			true, Date.class);
		services.add(modelVO);
		}
		
		{
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Alterar Pessoa");
		//modelVO.setPK(pk++);

		addServiceField(modelVO, 1l, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 2l, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 3l, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, 4l, "dtInclusao", 			true, Date.class);
		services.add(modelVO);
		}
		
		{
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Atualizar Telefone");
		//modelVO.setPK(pk++);

		addServiceField(modelVO, 1l, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 2l, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 3l, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, 4l, "dtInclusao", 			true, Date.class);
		addServiceField(modelVO, 5l, "telefoneRes", 				true, 4d, 30d, String.class, null);
		addServiceField(modelVO, 6l, "telefoneCel", 				true, 4d, 30d, String.class, null);
		services.add(modelVO);
		}
		
		/*
		addFieldTest(layoutModelVO, 0, "nome", String.class, true, 1d, 20d, null);
		addFieldTest(layoutModelVO, 3, "dtNasc", Date.class, true);
		addFieldTest(layoutModelVO, 6, "sobrenome", String.class, true, 1d, 20d, null);
		addFieldTest(layoutModelVO, 13, "dtInclusao", Date.class, true);
		*/
		
		
		return services;
	}

	private void addServiceField(ServiceModelVO modelVO, Long order, String name, boolean mandatory, Class<Date> type) {

		ServiceParamVO serviceField1 = new ServiceParamVO();
		
		
		//serviceField1.setPK(order);
		serviceField1.setFieldOrder(order.intValue());
		serviceField1.setName(name);
		serviceField1.setFieldRequired(mandatory);
		serviceField1.setFieldType(type);
		
		modelVO.getServiceFields().add(serviceField1);
		
	}

	protected void addServiceField(ServiceModelVO modelVO, final Long order, final String name, Boolean mandatory, Double minSize, Double maxSize, final Class<String> type, String regex) {
		ServiceParamVO serviceField1 = new ServiceParamVO();
		
		//serviceField1.setPK(order);
		serviceField1.setFieldOrder(order.intValue());
		serviceField1.setName(name);
		serviceField1.setFieldRequired(mandatory);
		serviceField1.setFieldType(type);
		
		serviceField1.setMaxSize(maxSize);
		serviceField1.setMinSize(minSize);
		serviceField1.setRegex(regex);
		
		modelVO.getServiceFields().add(serviceField1);
	}
	
	/**
	 * 
	 * Cria um objeto LayoutModelVO básico, a partir dos campos da planilha informada.
	 * Este objeto será completado pelo usuário na tela.
	 * 
	 * @param refFileName
	 * @param fis
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws FileNotFoundException
	 */
	@Override
	public void parseXLSXFile(LayoutModelVO layoutModelVO, InputStream fis, Integer maxLinhas){
		
		/**
		 * Nota: Seria perfeitamente possível fazer todo o parseamento direto de InputStream fis, mas,
		 * para arquivos XLSX grandes (>30mb) houve alguns erros e estouro de memória. A forma
		 * mais segura é processar o XLSX direto do disco.
		 * 
		 * Ao final, o arquivo temporário é excluído!
		 */
		
		//cria nome exclusivo para o arquivo (UUID)
		File uploadedFile = getFileDestination();

		FileOutputStream fos = getFOSDestination(uploadedFile);
	
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		
		String refFileChecksum ="";
		try{
			//copia para diretório temporário e já calcula checksum ao mesmo tempo
			refFileChecksum = copyAndCalculateChecksum(fis,fos, baos);
		}catch(Throwable e){
			log.error("Erro ao tentar copiar arquivo",e);
		}
		
		//apenas log
		critidaChecksum(layoutModelVO, refFileChecksum);
		
		//atualiza dados do arquivo
		layoutModelVO.getArquivo().setChecksum(refFileChecksum);
		layoutModelVO.getArquivo().setFileBytes(baos.toByteArray());
		
	
		//parseia arquivo XLSX criando modelo de layout básico. 
		//Este modelo será complementado pelo usuário na tela
		carregaLayoutModel(uploadedFile, layoutModelVO, maxLinhas);
		
		
		//tenta excluir o arquivo temporario. 
		//Loga em caso de erro! risco de acabar espaco em disco
		if( uploadedFile.delete()){
			if(log.isDebugEnabled()){
				log.debug("Arquivo "+uploadedFile+" excluido com sucesso!");
			}
		}else{
			log.error("Erro ao tentar excluir arquivo "+uploadedFile+"!");
		}
		
	}


	/**
	 * Apenas loga como erro caso o checksum do arquivo de exemplo não bata com o ultimo checksum calculado para o mesmo
	 * @param layoutModelVO
	 * @param refFileChecksum
	 */
	protected void critidaChecksum(LayoutModelVO layoutModelVO, String refFileChecksum) {
		String checksum = layoutModelVO.getArquivo().getChecksum();
		
		if(!StringUtils.isBlank(checksum) && refFileChecksum.equals(checksum)){
			log.error("ATENCAO! O CHECKSUM DO ARQUIVO {} ARMAZENADO NO LAYOUTMODEL {} ESTA DIFERENTE! Checksum calculado {}, checksum antigo {}!", 
					new Object[]{layoutModelVO.getArquivo().getNome(), layoutModelVO.getPK(),refFileChecksum,checksum });
		}
	}

	
	/**
	 * Carrega o Layout Model com as metainformações e dados do xlsx informado.
	 * 
	 * @param uploadedFile
	 * @param layoutModelVO
	 */
	protected void carregaLayoutModel(File uploadedFile, LayoutModelVO layoutModelVO, Integer maxLinhas) {

		OPCPackage pkg;
		try {
			pkg = OPCPackage.open(uploadedFile.getAbsolutePath());
			if (log.isDebugEnabled()) {
				log.debug("Iniciando parsing do arquivo " + uploadedFile);
			}
		} catch (InvalidFormatException e) {
			throw new LVRuntimeException("Erro ao tentar ler arquivo XLSX!",e);
		}
		
		
		try {
		
			XSSFReader reader = new XSSFReader(pkg);
			StylesTable styles = reader.getStylesTable();
			ReadOnlySharedStringsTable stringsTable = new ReadOnlySharedStringsTable(pkg);
			final XMLReader parser = XMLReaderFactory.createXMLReader(SAX_PARSER);
		
			Iterator<InputStream> sheets = reader.getSheetsData();
		
		
			while (sheets.hasNext()) {
		
				InputStream sheetStream = sheets.next();
		
		
				final InputSource sheetSource = new InputSource(sheetStream);
				
				// um handler novo para cada sheet!
				AbstractSAXHandler handler = new LayoutSAXHandler(styles,stringsTable, layoutModelVO, maxLinhas);
				parser.setContentHandler(handler);
		
				try {
					parser.parse(sheetSource);
				} catch (LVStopSAXParserException e) {
					log.warn("Numero maximo de linhas lidas alcancado! "+e.getMessage());
					
				} catch (Exception e) {
					log.error("Erro inesperado! Os catches do parserHandler não funcionaram!", e);
					
					throw new LVRuntimeException("Erro não esperado ao tentar processar o arquivo!", e);
				}
			}
		
		} catch (IOException e) {
			throw new LVRuntimeException("Erro ao tentar ler arquivo XLSX!",e);
		} catch (OpenXML4JException e) {
			throw new LVRuntimeException("Erro ao tentar ler arquivo XLSX!",e);
		} catch (SAXException e) {
			throw new LVRuntimeException("Erro ao tentar ler arquivo XLSX!",e);
		}
	}


	/**
	 * Calcula Checksum do arquivo
	 * @param baos 
	 * @param fileInputStream
	 * @return
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 */
	protected String copyAndCalculateChecksum(InputStream fis, OutputStream fos, ByteArrayOutputStream baos)  {
		BufferedInputStream bis = new BufferedInputStream(fis);
		BufferedOutputStream bos = new BufferedOutputStream(fos);
		BufferedOutputStream bbaos = new BufferedOutputStream(baos);
		
		MessageDigest md;
		try {
			md = MessageDigest.getInstance(CHECKSUM_IMPLEMENTATION_MD5);
		
	    byte[] dataBytes = new byte[1024];
	
	    int nread = 0;
			while ((nread = bis.read(dataBytes)) != -1) {
					bos.write(dataBytes, 0, nread);
					bbaos.write(dataBytes, 0, nread);
			    md.update(dataBytes, 0, nread);
			}
			
		} catch (IOException e) {
			throw new LVRuntimeException("Erro ao tentar ler bytes do arquivo para calcular checksum!",e);
			
		} catch (NoSuchAlgorithmException e) {
			throw new LVRuntimeException("Algoritmo para calcular checksum nao encontrado!",e);
			
		}finally{
			try {
				bbaos.flush();
				bbaos.close();
				bos.flush();
				bos.close();
				bis.close();
				
				fos.flush();
				fos.close();
				fis.close();
			} catch (IOException e) {
				log.error("Erro ao tentar finalizar copia do arquivo",e);
			}
		}

		
    byte[] mdbytes = md.digest();

    //convert the byte to hex format method 2
    StringBuffer hexString = new StringBuffer();
    for (int i = 0; i < mdbytes.length; i++) {
        String hex = Integer.toHexString(0xff & mdbytes[i]);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
    }
    
		return hexString.toString();
		
	}
	
	



	protected FileOutputStream getFOSDestination(File uploadedFile) {
		FileOutputStream fos;
		try {
			fos = new FileOutputStream(uploadedFile);
		} catch (FileNotFoundException e) {
			throw new LVRuntimeException("Erro ao tentar criar fileoutputStream para diretorio temp!",e);
		}
		return fos;
	}


	/**
	 * Cria arquivo de destino temporario para processamento do XLSX.
	 * @return
	 */
	protected File getFileDestination() {
		File fTempDir = new File(System.getProperty("java.io.tmpdir"));
		
		//File fTempDir = new File("/home/darcio/lixo/");
		
		
		String fileName = "lvModelo_" + UUID.randomUUID()+".xlsx";
		
		File uploadedFile = new File(fTempDir, fileName);
		
		if (log.isDebugEnabled()) {
			log.debug("Local de destino: {}.", new Object[] { uploadedFile.getAbsolutePath()});
		}
		return uploadedFile;
	} 



}
