package br.com.mapfre.lv.mapreduce;

import br.com.mapfre.lv.payload.LotVO;


/**
 * Contrato b�sico para um mapeador de tasks 
 * @author darcio
 *
 */
public interface IMapper {

	boolean next();

	LotVO nextPayload();

}
