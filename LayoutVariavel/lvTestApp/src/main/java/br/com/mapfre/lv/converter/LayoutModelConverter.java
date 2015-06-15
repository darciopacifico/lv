package br.com.mapfre.lv.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;

import br.com.mapfre.lv.layoutmodel.LayoutModelVO;

/**
 * Converter JSF para LayoutModel.
 * 
 * Permite criar combos e outros controles para manipular layout model
 * 
 * @author darcio
 * 
 */
public class LayoutModelConverter extends AbstractJSONConverter {

	public LayoutModelConverter() {
		gsonBuilder.registerTypeHierarchyAdapter(Class.class, new ClassTypeAdapter());
	}

	@Override
	protected Class<? extends Object> getClassType() {
		return LayoutModelVO.class;
	}

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String strValue) {
		// TODO Auto-generated method stub
		return super.getAsObject(context, component, strValue);
	}

	
	/**
	 * Cria um layoutModel novo somente com id e nome. Reduz o tamanho do JSON
	 * TODO: uma anotation JSON poderia dar o mesmo efeito (ignorar todos os demais atributos), mas nao funcionou corretamente... 
	 */
	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {

		Object toSerialize = value;
		
		if (value != null && value instanceof LayoutModelVO) {

			LayoutModelVO layoutModelVO = (LayoutModelVO) value;

			LayoutModelVO novoLm = new LayoutModelVO();

			novoLm.setPK(layoutModelVO.getPK());
			novoLm.setNome(layoutModelVO.getNome());
			
			toSerialize = novoLm;
		}
		return super.getAsString(context, component, toSerialize);
		
	}

}
