package br.com.mapfre.lv.jobmanager;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Map;

import br.com.mapfre.lv.LVException;
import br.com.mapfre.lv.executor.IExecutorMaster;
import br.com.mapfre.lv.executor.IProcessMonitor;


/**
 * Implementação abstrata de um gerenciador de processamento de um arquivo
 * @author darcio
 */
public abstract class AbstractJobManager {

	public IExecutorMaster executorMaster;
	public IProcessMonitor processMonitor;
	protected int lotSize;


	
	public IExecutorMaster getExecutorMaster() {
		return executorMaster;
	}

	public void setExecutorMaster(IExecutorMaster executorMaster) {
		this.executorMaster = executorMaster;
	}

	public IProcessMonitor getProcessMonitor() {
		return processMonitor;
	}

	public void setProcessMonitor(IProcessMonitor processMonitor) {
		this.processMonitor = processMonitor;
	}

	public abstract FileVO startProcess(Map<String, Serializable> params, InputStream inputStream, String fileName) throws LVException;

}
