package br.com.mapfre.lv.jobmanager;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import br.com.mapfre.lv.LVException;
import br.com.mapfre.lv.LVRuntimeException;
import br.com.mapfre.lv.connector.IBridge;
import br.com.mapfre.lv.executor.IProcessMonitor;
import br.com.mapfre.lv.jobmanager.JobManagerXLSXEventModel;
import br.com.mapfre.lv.layout.XLSXLayoutParser;
import br.com.mapfre.lv.layoutmodel.LayoutColumnVO;
import br.com.mapfre.lv.layoutmodel.LayoutModelVO;
import br.com.mapfre.lv.layoutmodel.ServiceParamVO;
import br.com.mapfre.lv.layoutmodel.ServiceModelVO;

/**
 * Classe de testes cliente de JobManager.
 * Faz a vez de um componente web que receberá os arquivos para o LV e acionará o processamento destes
 * 
 * @author darcio
 */
@ContextConfiguration(locations = "/ApplicationContextLV.xml")
@Test
public class JobManagerTest extends AbstractTestNGSpringContextTests {
	private static final Logger log = LoggerFactory.getLogger(JobManagerTest.class);
	
	@Autowired
	private ApplicationContext context;
	
	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	//classe que reporta o andamento do processo
	@Qualifier("persistentProcessMonitor")
	@Autowired
	private IProcessMonitor processMonitor;
	
	//String fileName="/home/darcio/trabalho/lv/planilhaGigante_32_simples.xlsx";
	
	String fileName="/home/darcio/trabalho/lv/modeloTeste.xlsx";
	
	/*
	String fileName="e_planilhaGigante_1k.xlsx";
	String fileName = "e_planilhaGigante_250k.xlsx";
	  String fileName="e_planilhaGigante_5k.xlsx"; 
	  String fileName="modeloCampos.xlsx"; 
	  String fileName="planilhaGigante_1registro.xlsx"; // tempo total 470 
	  String fileName="planilhaGigante_1registro2_b.xlsx"; // tempo total 470 
	  String fileName="planilhaGigante_1registro_c.xlsx"; // tempo total 470
	  String fileName="planilhaGigante_1k.xlsx"; // tempo total 2695 
	  String fileName="planilhaGigante_1k_b.xlsx"; // tempo total 2695

	  String fileName="planilhaGigante_5k.xlsx"; // tempo total 4714 
	  String fileName="planilhaGigante_10k.xlsx"; // tempo total 6171 
	  String fileName="planilhaGigante_31k.xlsx"; // tempo total 13504 
	  String fileName="planilhaGigante_62k.xlsx"; // tempo total 23180 
	  String fileName="planilhaGigante_125k.xlsx"; // tempo total 42896 
	  String fileName="planilhaGigante_250k.xlsx"; // tempo total 86994 
	  String fileName="planilhaGigante_500k.xlsx"; // tempo total 156086 
	String fileName="planilhaGigante_1000k.xlsx"; // tempo total 311656
	 */
	
	
	@Test
	public void testJobManagerXLSX() throws LVException, FileNotFoundException{

		long now = System.currentTimeMillis();
		
		LayoutModelVO layoutModel = getLayoutModel("LayoutTeste");
		
		log.debug("iniciando processamento de arquivo");
		
		JobManagerXLSXEventModel eventModel = new JobManagerXLSXEventModel();
		
		Map<String, Serializable> substituteVals = new HashMap<String, Serializable>();
		
		XLSXLayoutParser layoutParser = new XLSXLayoutParser(layoutModel, substituteVals);
		eventModel.setLayoutParser(layoutParser);
		
		//"vidaBridge"

		IBridge bridge = getBridge(layoutModel);
		
		eventModel.setBridge(bridge);
		eventModel.setProcessMonitor(this.processMonitor);
		
		Map<String, Serializable> params = new HashMap<String, Serializable>();
		
		//params.put(JobManagerXLSXEventModel.P_LOTBUFFERSIZE, "500");
		
		
		eventModel.startProcess(params, new FileInputStream(this.fileName), this.fileName );
		
		
		log.debug("Tempo total de processamento  "+(System.currentTimeMillis()-now) +"ms");
		
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
	

	/**
	 * 
	 * @param nomeLayout
	 * @return
	 */
	protected LayoutModelVO getLayoutModel(String nomeLayout) {
		DetachedCriteria criteria = DetachedCriteria.forClass(LayoutModelVO.class);
		
		criteria.add(Restrictions.eq("nome", nomeLayout));

		List<LayoutModelVO> val = hibernateTemplate.findByCriteria(criteria );
		
		LayoutModelVO layoutModelVO = val.get(0);
		return layoutModelVO;
	}

	/**
	 * 
	 * @param layoutModelVO
	 * @param pk
	 * @param name
	 * @param type
	 * @param mandatory
	 */
	protected void addFieldTest(LayoutModelVO layoutModelVO, Long pk, String name, Class<?> type, boolean mandatory) {
		this.addFieldTest(layoutModelVO, pk, name, type, mandatory, null, null, null);
	}

	/**
	 * 
	 * @param layoutModelVO
	 * @param pK
	 * @param name
	 * @param type
	 * @param mandatory
	 * @param minSize
	 * @param maxSize
	 * @param regex
	 */
	protected void addFieldTest(LayoutModelVO layoutModelVO, Long pK, String name, Class<?> type, Boolean mandatory, Double minSize, Double maxSize, String regex) {
		ServiceParamVO serviceParamVO = new ServiceParamVO();
		LayoutColumnVO fieldVO = new LayoutColumnVO();

		serviceParamVO.setFieldRequired(mandatory);
		serviceParamVO.setMaxSize(maxSize);
		serviceParamVO.setMinSize(minSize);
		serviceParamVO.setName(name);
		serviceParamVO.setFieldOrder(pK.intValue());
		serviceParamVO.setRegex(regex);

		serviceParamVO.setFieldType(type);

		fieldVO.setServiceFieldVO(serviceParamVO);

		layoutModelVO.getFields().put(pK.intValue(), fieldVO);

	}
	

}
