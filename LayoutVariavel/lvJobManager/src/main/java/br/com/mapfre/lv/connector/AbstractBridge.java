package br.com.mapfre.lv.connector;

import br.com.mapfre.lv.executor.IProcessMonitor;

/**
 * Implementacao abstrata de um conector 
 * @author darcio
 *
 */
public abstract class AbstractBridge implements IBridge {

	private IProcessMonitor processMonitor;

	public AbstractBridge(){
		
	}

	public IProcessMonitor getProcessMonitor() {
		return processMonitor;
	}

	public void setProcessMonitor(IProcessMonitor processMonitor) {
		this.processMonitor = processMonitor;
	}

	
	
}
