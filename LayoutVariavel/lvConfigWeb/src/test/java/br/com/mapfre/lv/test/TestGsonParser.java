package br.com.mapfre.lv.test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import br.com.mapfre.lv.converter.ClassTypeAdapter;
import br.com.mapfre.lv.layoutmodel.ServiceParamVO;
import br.com.mapfre.lv.layoutmodel.ServiceModelVO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;


@Test
public class TestGsonParser {

	private static final Logger log = LoggerFactory.getLogger(TestGsonParser.class);
	
	/**
	 * @param args
	 */
	@Test
	public void testGSon() {
		
	
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Servico Teste 1");
		addServiceField(modelVO, 1l, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 2l, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 3l, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, 4l, "dtInclusao", 			true, Date.class);
		
		GsonBuilder gsonBuilder = new GsonBuilder();
		
		//gsonBuilder.registerTypeAdapter(Class.class, new ClassTypeAdapter());
		
		gsonBuilder.registerTypeHierarchyAdapter(Class.class , new ClassTypeAdapter());
		
		
		Gson gson = gsonBuilder.create();
		String strJson = gson.toJson(modelVO);

		ServiceModelVO reconstruido = gson.fromJson(strJson, ServiceModelVO.class);
		
		log.debug(strJson);
		log.debug(""+reconstruido);
		
	}
	
	

	public List<ServiceModelVO> getServiceModels() {

		List<ServiceModelVO> services = new ArrayList<ServiceModelVO>();

		{
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Servico Teste 1");
		addServiceField(modelVO, 1l, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 2l, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 3l, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, 4l, "dtInclusao", 			true, Date.class);
		services.add(modelVO);
		}
		
		{
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Servico Teste 2");
		addServiceField(modelVO, 1l, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 2l, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 3l, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, 4l, "dtInclusao", 			true, Date.class);
		services.add(modelVO);
		}
		
		{
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Servico Teste 3");
		addServiceField(modelVO, 1l, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 2l, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 3l, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, 4l, "dtInclusao", 			true, Date.class);
		services.add(modelVO);
		}
		
		{
		ServiceModelVO modelVO = new ServiceModelVO();
		modelVO.setNome("Servico Teste 4");
		addServiceField(modelVO, 1l, "nome", 						true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 2l, "sobrenome", 				true, 2d, 60d, String.class,null);
		addServiceField(modelVO, 3l, "dtNasc", 					true, Date.class);
		addServiceField(modelVO, 4l, "dtInclusao", 			true, Date.class);
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
	protected void addServiceField(ServiceModelVO modelVO, Long order, final String name, Boolean mandatory, Double minSize, Double maxSize, final Class<String> type, String regex) {
		ServiceParamVO serviceField1 = new ServiceParamVO();
		
		serviceField1.setFieldOrder(order.intValue());
		serviceField1.setName(name);
		serviceField1.setFieldRequired(mandatory);
		serviceField1.setMaxSize(maxSize);
		serviceField1.setMinSize(minSize);
		serviceField1.setFieldType(type);
		serviceField1.setRegex(regex);
		
		modelVO.getServiceFields().add(serviceField1);
	}
	
	

	private void addServiceField(ServiceModelVO modelVO, Long order, String name, boolean mandatory, Class<Date> type) {

		ServiceParamVO serviceField1 = new ServiceParamVO();
		
		serviceField1.setFieldOrder(order.intValue());
		serviceField1.setName(name);
		serviceField1.setFieldRequired(mandatory);
		serviceField1.setFieldType(type);
		
		modelVO.getServiceFields().add(serviceField1);
		
	}
}
