package br.com.mapfre.lv.executor;

import java.util.concurrent.TimeUnit;

import br.com.mapfre.lv.payload.LotVO;


/**
 * Contrato de componente responsável por executar o processamento de um lote de dados (@LotVO) e cuidar do ciclo de vida deste processamento.
 *
 * @author darcio
 */
public interface IExecutorMaster {

	/**
	 * Executa o processamento do lote de registros informado
	 * @param payloadLot
	 * @throws LotExecutionException
	 */
	public void execute(LotVO payloadLot) throws LotExecutionException;

	/**
	 * Implementa a politica de tratamento de erro ao tentar executar um lote de registros.
	 */
	public void invalidateAll();
	
	
	/**
	 * Aguarda até que todos os lotes repassados a este componentes tenham sido processados ou que o timeout expire.
	 * 
	 * Na implementacao @ThreadPoolExecutorMaster o processamento dos lotes é repassado para um componente q os processa assincronamente. 
	 * Neste caso, este método deve segurar a execução até q o pool de threads esteja vazio.  
	 * 
	 * @param time
	 * @param timeUnit
	 * @throws InterruptedException
	 */
	public void waitThreadsCompletion(long time, TimeUnit timeUnit) throws InterruptedException;

	
	/**
	 * Finaliza e fecha todos os recursos utilizados pela implementação deste contrato
	 */
	public void finalizeExecutor();
	
}
