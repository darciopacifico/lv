package br.com.mapfre.lv.converter;

import br.com.mapfre.lv.layoutmodel.ServiceParamVO;

/**
 * Converter para service model
 * @author darcio
 *
 */
public class ServiceFieldConverter extends AbstractJSONConverter {

	public ServiceFieldConverter(){
		gsonBuilder.registerTypeHierarchyAdapter(Class.class , new ClassTypeAdapter());
	}
	
	@Override
	protected Class<? extends Object> getClassType() {
		return ServiceParamVO.class;
	}

}
