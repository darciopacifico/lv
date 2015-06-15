package br.com.mapfre.lv.vidateste;

import java.io.Serializable;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import br.com.mapfre.lv.connector.AbstractBridge;
import br.com.mapfre.lv.connector.IBridge;
import br.com.mapfre.lv.executor.IProcessMonitor;
import br.com.mapfre.lv.payload.LotVO;

/**
 * Bridge generica. Se presta apenas para validacao de arquivos
 * @author darcio
 */
@Component("validatorBridge")
public class GenericValidatorBridge extends AbstractBridge implements IBridge {
		

	private static final int GENERAL_NOT_PROCESSED_RETURN_VALUE = 0;

	@Autowired
	@Override
	@Qualifier("persistentProcessMonitor")
	public void setProcessMonitor(IProcessMonitor processMonitor) {
		super.setProcessMonitor(processMonitor);
	}
	
	
	/**
	 * Constroi bridge para validação
	 * @param processMonitor
	 */
	public GenericValidatorBridge(){
	}
	
	/**
	 * Não aciona nenhum componente. Apenas retorna resultado geral dizendo que nenhum registro foi processado.
	 */
	@Override
	public int[] execute(Map<String, Serializable> params, LotVO lotVO) throws Exception {
		
		Integer lotSize = lotVO.getToLine()- lotVO.getFromLine()+1;

		int[] results = new int[lotSize];
		
		for(int i=0; i<lotSize; i++){
			results[i] = GENERAL_NOT_PROCESSED_RETURN_VALUE;
		}
		
		return results;
		
	}

}
