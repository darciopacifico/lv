package br.com.mapfre.lv.connector;

import java.io.Serializable;
import java.util.Map;

import br.com.mapfre.lv.payload.LotVO;

/**
 * Contrato para componente que deve fazer a ponte entre a estrutura de 
 * processamento do LV e o componente de negócios destino do processo
 *  
 * @author darcio
 */
public interface IBridge {

	int[] execute (Map<String, Serializable > params, LotVO payloadLotVO) throws Exception;
	
}
