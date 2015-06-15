package br.com.mapfre.lv.executor;

/**
 * Contrato para um executor de processamento de um lote de dados. Ver @RunnableExecutorSlave para exemplo 
 *
 * @author darcio
 */
public interface IExecutorSlave {

	/**
	 * Executa processamento do lote de dados
	 */
	void execute();
	
}
