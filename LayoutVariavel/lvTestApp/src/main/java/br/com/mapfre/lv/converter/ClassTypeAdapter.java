package br.com.mapfre.lv.converter;

import java.io.IOException;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonWriter;

/**
 * TypeAdapter de JSon para guardar o tipo do campo contido num atributo de ServiceParamVO
 * @author darcio
 *
 */
public class ClassTypeAdapter extends TypeAdapter<Class<?>> {

	@Override
	public void write(JsonWriter out, Class<?> clazz) throws IOException {
		
		if(clazz!=null){
			String canonicalName = clazz.getCanonicalName();
			out.value(canonicalName);
		}
		
	}

	@Override
	public Class<?> read(JsonReader in) throws IOException {
		String className = in.nextString();
		Class<?> clazz;
		
		try {
			clazz = Class.forName(className);
		} catch (ClassNotFoundException e) {
			throw new IOException("Erro ao tentar carregar classe de nome "+className+"!",e);
		}

		return clazz;
	}

}
