package br.com.mapfre.lv.executor;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.RejectedExecutionHandler;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.mapfre.lv.connector.IBridge;
import br.com.mapfre.lv.jobmanager.LotRuntimeException;
import br.com.mapfre.lv.payload.LotVO;


/**
 * Implementação de executor master que utiliza um threadPool. Cria 
 * @author darcio
 *
 */
public class ThreadPoolExecutorMaster implements IExecutorMaster {
	private static final Logger log = LoggerFactory.getLogger(ThreadPoolExecutorMaster.class);

	private ThreadPoolExecutor threadExecutor;
	private IBridge bridge;
	private IProcessMonitor processMonitor;
	private Map<String, Serializable> params;
	
	
	public ThreadPoolExecutorMaster(Map<String, Serializable> params, int corePoolSize,int maximumPoolSize,long keepAliveTime,TimeUnit unit, IBridge bridge,int queueCapacity, IProcessMonitor processMonitor) {
		super();
		this.params = params;
		this.processMonitor = processMonitor;
		this.bridge = bridge;
		BlockingQueue<Runnable> workQueue = new LinkedBlockingQueue<Runnable>(queueCapacity);
		
		RejectedExecutionHandler rejectHandler	= new ThreadPoolExecutor.CallerRunsPolicy();
		
		//if maximumPoolSize less than or equal to zero, or if corePoolSize greater than maximumPoolSize. 
		this.threadExecutor = new ThreadPoolExecutor(corePoolSize,maximumPoolSize,keepAliveTime,unit,workQueue,rejectHandler);
	}

	
	/**
	 * Verifica lote de registros para processamento.
	 */
	@Override
	public void execute(LotVO lotVO) {
		if(lotVO==null){
			//se um lote vier nulo, cancelar todo o processamento do arquivo. Não faz sentido continuar.
			LotRuntimeException lotRuntimeException = new LotRuntimeException("Erro ao tentar processar lote de dados (null)!");
			log.error("Lote informado é nulo!!",lotRuntimeException);
			throw lotRuntimeException;
		}
		
		logLot(lotVO);//linhas de log suprimidas neste método para clareza de codigo

		try {
			
			processMonitor.lotStart(lotVO);

			//loga inicio do processamento do lote e che
			if(processMonitor.canLotStart(lotVO)){

				IExecutorSlave executorSlave = new RunnableExecutorSlave(this.params, lotVO, bridge, processMonitor);
				
				//TODO: trocar modelo de execucao de threads programática, por um modelo declarativo, utilizando @As ync
				threadExecutor.execute((Runnable)executorSlave);
				
			}else{ 
				
				processMonitor.lotRejected(lotVO);
				
			}
			
		} catch (Exception e) {
			log.warn(MessageFormat.format("", lotVO.getFromLine(), lotVO.getFromLine() ),e);
			processMonitor.lotError(lotVO, e);
		}
		
	}


	/**
	 * Apenas separando este log para melhorar a clareza do codigo
	 * @param lotVO
	 */
	protected void logLot(LotVO lotVO) {
		if(log.isDebugEnabled() && lotVO!=null){
			log.debug("Iniciando o processamento do lote referente aas linhas de {} ate {}!",new Object[]{lotVO.getFromLine(), lotVO.getToLine()});
		}else{
			log.debug("Iniciando o processamento do lote {}!",new Object[]{lotVO});
		}
	}
	
	@Override
	public void waitThreadsCompletion(long time, TimeUnit timeUnit) throws InterruptedException {
			threadExecutor.shutdown();
			threadExecutor.awaitTermination(time, timeUnit);
	}
	
	@Override
	public void finalizeExecutor() {
	}
	
	/**
	 * 
	 */
	@Override
	public void invalidateAll() {
	}


	public Executor getThreadExecutor() {
		return threadExecutor;
	}


	public void setThreadExecutor(ThreadPoolExecutor executor) {
		this.threadExecutor = executor;
	}


	public IBridge getBridge() {
		return bridge;
	}


	public void setBridge(IBridge bridge) {
		this.bridge = bridge;
	}
	
}
