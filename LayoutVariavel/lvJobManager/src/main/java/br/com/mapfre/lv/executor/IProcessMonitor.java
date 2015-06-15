package br.com.mapfre.lv.executor;

import java.util.concurrent.TimeUnit;

import br.com.mapfre.lv.LVException;
import br.com.mapfre.lv.jobmanager.FileVO;
import br.com.mapfre.lv.payload.LotVO;
import br.com.mapfre.lv.payload.PayloadVO;


/**
 * Contrato para componente respons�vel por monitorar a execucao dos processos.
 * Deve implementar as pol�ticas de rejeicao de arquivos por erros
 * 
 * @author darcio
 *
 */
public interface IProcessMonitor {

	/**
	 * Registrar o inicio do processamento do arquivo.
	 * @param fileName
	 * @throws LVException 
	 */
	FileVO processingStart(String fileName, Boolean overrideFile) throws LVException;
	
	/**
	 * Registrar a finaliza��o do processamento do arquivo. 
	 * Caso a implementa��o seja ass�ncrono/multithread (recomend�vel!!), aplicar o timeout especificado na finaliza��o do thread pool
	 * 
	 * @param threadExecutorTimeoutUnit 
	 * @param threadExecutorTimeout 
	 * @throws LVException 
	 */
	void processingEnd(int threadExecutorTimeout, TimeUnit threadExecutorTimeoutUnit) throws LVException;
	
	/**
	 * Registrar o in�cio do processamento de um lote de registros
	 * @param payloadLot
	 */
	void lotStart(LotVO payloadLot);
	
	/**
	 * Registrar o fim do processamento de um lote de registros e seus resultados
	 * @param lotVO
	 * @param results
	 */
	void lotEnd(LotVO lotVO, int[] results);
	
	/**
	 * Assim que o arquivo chegar ao final, registrar o tamanho do arquivo
	 * @param linhaAtual
	 */
	void setFileSize(Integer linhaAtual);


	/**
	 * Erro ao processar lote onde n�o h� nenhuma resposta sobre os resultados dos processamentos dos payloads do lote.
	 * Normalmente a implementacao deve marcar todos os payloads como n�o processados.
	 * @param lotVO
	 * @param e
	 */
	void lotError(LotVO lotVO, Exception e);
	
	/**
	 * Erro ao processar lote onde foi poss�vel determinar o resultado do processamento dos payloads do lote. Ex: BatchUpdateException.
	 * A implementa��o deve apurar o resultado de cada payload, representado pelo respectivo item do array, registrar as evetuais faltas.  
	 * @param lotVO
	 * @param e
	 * @param results
	 */
	void lotError(LotVO lotVO, Exception e, int[] results);
	
	/**
	 * Registrar lote como rejeitado. Neste caso as faltas por valida��o j� deve ter sido registradas.
	 * @param lotVO
	 */
	void lotRejected(LotVO lotVO);
	
	/**
	 * Erro da familia runtimeException detectado: Arquivo n�o encontrado, problemas de rede, conex�o jdbc etc.
	 * 
	 * Tentar registrar lote como erro e condenar o processamento do arquivo.
	 * 
	 * @param lotVO
	 * @param RuntimeException
	 */
	void lotRuntimeError(LotVO lotVO, RuntimeException RuntimeException);
	
	/**
	 * De acordo com o estado do monitor e suas pol�ticas, determinar se o lote informado pode ter seu processamento iniciado.
	 * Normalmente quando a quantidade de faltas ou erros fere a pol�tica de processamento, o arquivo todo deve ser condenado.
	 * 
	 * @param lot
	 * @return
	 */
	boolean canLotStart(LotVO lot);
	
	/**
	 * Erro ao tentar realizar a leitura do valor de uma c�lula (linha x coluna).
	 * 
	 * Apenas logar. A avalia��o da execucao deve ser determinada por. canLotStart, que avalia todo o payload de execucao.
	 * 
	 * @param e
	 * @param payloadVO
	 * @param colunaAtual
	 */
	void parsingCellDataError(LVLayoutException e, PayloadVO payloadVO, Integer colunaAtual);

	/**
	 * Acionado sempre que h� erro ao se tentar iniciar o processamento de um arquivo
	 * @param fileVO
	 */
	void processingError(FileVO fileVO);
	
}