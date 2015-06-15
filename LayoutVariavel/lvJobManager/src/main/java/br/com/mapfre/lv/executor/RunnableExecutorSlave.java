package br.com.mapfre.lv.executor;

import java.io.Serializable;
import java.sql.BatchUpdateException;
import java.util.Date;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.mapfre.lv.connector.IBridge;
import br.com.mapfre.lv.payload.LotVO;

/**
 * Implementação de IExecutorSlave Runnable.
 * Parte integrante da implementação de Executors baseado em pool de threads. Trabalha para  @ThreadPoolExecutorMaster.
 * 
 * @author darcio
 */
public class RunnableExecutorSlave implements IExecutorSlave, Runnable {
	private static final int COD_ERRO_DESCONHECIDO = -9;
	private static final Logger log = LoggerFactory.getLogger(RunnableExecutorSlave.class);
	private LotVO lotVO;
	private IBridge bridge;
	private IProcessMonitor processMonitor;
	private Map<String, Serializable> params;
	
	/**
	 * Cria um executor
	 * @param params 
	 * @param payloadLot
	 * @param bridge
	 * @param processMonitor 
	 */
	public RunnableExecutorSlave(Map<String, Serializable> params, LotVO payloadLot, IBridge bridge, IProcessMonitor processMonitor) {
		this.lotVO = payloadLot;
		this.bridge = bridge;
		this.processMonitor = processMonitor;
		this.params = params;
	}


	/**
	 * Implementação de run de Runnable. Apenas aciona o execute do contrato @IExecutorSlave
	 */
	@Override
	public void run() {
		this.execute();
	}
	
	/**
	 * Implementação de execute do contrato @IExecutorSlave.
	 * Apenas aciona a @IBridge contida no atributo {@link #bridge}.
	 */
	@Override
	public void execute() {
		
		lotVO.setDtIniciacao(new Date());
		
		//invoca bridge
		
		try{
			// delega a execucao da tarega para a bridge, esperando a lista dos resultados no padrao JDBC Batch
			int[] results = bridge.execute(this.params, this.lotVO);
			
			// OK, sucesso. Registra resultados do lote
			processMonitor.lotEnd(lotVO, results);
			
		}catch(BatchUpdateException e){
				
			log.warn("Erro ao tentar processar registros",e);
			
			int[] results = e.getUpdateCounts();
			
			processMonitor.lotError(this.lotVO, e, results);
			
			
		} catch (Exception e) {
			processMonitor.lotError(lotVO, e);
		}
	}
	

	/**
	 * @return
	 */
	public LotVO getLotVO() {
		return lotVO;
	}


	/**
	 * @param payloadLotVO
	 */
	public void setLotVO(LotVO payloadLotVO) {
		this.lotVO = payloadLotVO;
	}


	/**
	 * @return
	 */
	public IBridge getBridge() {
		return bridge;
	}


	/**
	 * @param bridge
	 */
	public void setBridge(IBridge bridge) {
		this.bridge = bridge;
	}
	
}
