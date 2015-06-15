package br.com.mapfre.lv.converter;

import br.com.mapfre.lv.layoutmodel.ServiceModelVO;

/**
 * Converter para service model
 * @author darcio
 *
 */
public class ServiceModelConverter extends AbstractJSONConverter {

	public ServiceModelConverter(){
		gsonBuilder.registerTypeHierarchyAdapter(Class.class , new ClassTypeAdapter());
	}
	
	@Override
	protected Class<? extends Object> getClassType() {
		return ServiceModelVO.class;
	}

}
