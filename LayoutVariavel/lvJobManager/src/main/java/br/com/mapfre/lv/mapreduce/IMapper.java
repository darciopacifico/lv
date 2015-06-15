package br.com.mapfre.lv.mapreduce;

import br.com.mapfre.lv.payload.LotVO;


/**
 * Contrato básico para um mapeador de tasks 
 * @author darcio
 *
 */
public interface IMapper {

	boolean next();

	LotVO nextPayload();

}
