package br.com.mapfre.lv.layoutmodel;

import java.sql.Types;

import org.hibernate.type.AbstractLongBinaryType;

import br.com.mapfre.lv.LVRuntimeException;


/**
 * Converte tipo Class para string simples. Aciona class.forname para recuperar classe original
 * @author darcio
 *
 */
public class LVClassType extends AbstractLongBinaryType {

	private static final String LV_CLASS = "lvClass";
	private static final long serialVersionUID = 8703578207200100533L;
	public static final String TYPE_NAME = "omrimg";

	
	/**
	 * omrimg
	 */
	@Override
	public String getName() {
		return LV_CLASS;
	}

	/**
	 * @return BufferedImage.class;  
	 */
	public Class<?> getReturnedClass() {
		return Class.class;
	}

	/**
	 * Espera um array de bytes contendo uma imagem. Tenta ler o array de bytes como imagem
	 */
	protected Object toExternalFormat(byte[] bytes) {
		String canonicalName = new String(bytes);
		
		Class<?> clazz;
		try {
			clazz = Class.forName(canonicalName);
		} catch (ClassNotFoundException e) {
			throw new LVRuntimeException("Erro ao tentar recuperar Classe de nome '"+canonicalName+"'",e);
		}
		
		return clazz;
	}

	/**
	 * Converte objeto (BufferedImage) informado para array de bytes
	 * 
	 */
	protected byte[] toInternalFormat(Object object) {
		
		if(object==null || !(object instanceof Class))
			throw new LVRuntimeException("Erro ao tentar converter objeto para formato interno (object="+object+")!");
			
		Class<?> clazz = (Class<?>) object;
		
		String canonicalName = clazz.getCanonicalName();
		
		return canonicalName.getBytes();
	}


	/**
	 * Retorna tipo padrao para sql blob
	 */
	@Override
	public int sqlType() {
		return Types.VARCHAR;
	}
	

}
