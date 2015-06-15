package br.com.mapfre.lv.jobmanager;

import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.exceptions.OpenXML4JException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.eventusermodel.XSSFReader;
import org.apache.poi.xssf.model.StylesTable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import br.com.mapfre.lv.LVException;
import br.com.mapfre.lv.LVRuntimeException;
import br.com.mapfre.lv.connector.IBridge;
import br.com.mapfre.lv.executor.IExecutorMaster;
import br.com.mapfre.lv.executor.IProcessMonitor;
import br.com.mapfre.lv.executor.ThreadPoolExecutorMaster;
import br.com.mapfre.lv.layout.ILayoutParser;

/**
 * Contrato básico para um gerenciador de JObs do LV
 * 
 * @author darcio
 */
@Component
public class JobManagerXLSXEventModel extends AbstractJobManager {

	private static final String SAX_PARSER = "org.apache.xerces.parsers.SAXParser";
	private static final Logger log = LoggerFactory.getLogger(JobManagerXLSXEventModel.class);
	

	private ILayoutParser layoutParser;
	
	private IBridge bridge;// bridge para um componente de negócios fake (Apenas printa mensagens em console)

	private IProcessMonitor processMonitor;// classe que reporta o andamento do processo

	// TUNNING DA LEITURA DO ARQUIVO
	private int lotBufferSize=45; // Quantidade de linhas do arquivo para cada lote de registros a serem processados por thread.

	// CONFIGURAÇÕES DO POOL DE THREADS *** PONTO CHAVE DA ARQUITETURA ***
	// IllegalArgumentException - if corePoolSize or keepAliveTime less than zero, or if maximumPoolSize less than or equal to zero, or if corePoolSize greater than maximumPoolSize.
	private int corePoolSize =50; // the number of threads to keep in the pool, even if they are idle.
	private int maximumPoolSize = 60 ; // the maximum number of threads to allow in the pool.
	private int queueCapacity =50 ; // Capacidade máxima de Threads em estado idle. "Trava" a inclusão de novas threads, até que as threads executantes terminem.
	private long keepAliveTime = 4000;// when the number of threads is greater than the core, this is the maximum time that excess idle threads will wait for new tasks before terminating.

	private TimeUnit	threadExecutorTimeoutUnit = TimeUnit.SECONDS;
	private int     	threadExecutorTimeout = 5000;
	private Boolean overrideFile = true;
	
	/**
	 * Registra o início do processo e cria uma thread e dá início ao processamento do arquivo
	 * @param substituteVals 
	 * 
	 * @return Registro do arquivo para acompanhamento
	 */
	@Override
	public FileVO startProcess(final Map<String, Serializable> params, final InputStream inputStream, final String fileName) throws LVException {

		//cria o registro do processamento
		final FileVO fileVO = processMonitor.processingStart(fileName, overrideFile);
		
		//inicia o processamento de forma assincona
		startProcessAsynchronously(params, inputStream, fileName, fileVO);
		
		//retorna o registro do processamento para que o cliente desta classe passe a monitorá-lo 
		return fileVO;
		
	}


	/**
	 * Apenas invoca a execucao do processo de forma assincrona
	 * @param params
	 * @param substituteVals 
	 * @param inputStream
	 * @param fileName
	 * @param fileVO
	 */
	protected void startProcessAsynchronously(final Map<String, Serializable> params, final InputStream inputStream, final String fileName, final FileVO fileVO) {

		//cria runnable
		Runnable runnable = new Runnable(){
			public void run() {
				try {
					//roda o processo efetivamente
					runProcess(params, inputStream, fileName);
				} catch (LVException e) {
					processMonitor.processingError(fileVO);
				}
			}
		};	
		
		Thread thread = new Thread(runnable, "LV Thread principal proc arquivo "+fileVO.getPK()+" - "+fileVO.getFileName());
		
		thread.start();
	}


	/**
	 * Efetivamente dispara o processamento do arquivo. 
	 * Aciona o parser do XML da planilha e aguarda a finalização ou timeout de todas as threads.
	 * 
	 * @param params
	 * @param substituteVals 
	 * @param inputStream
	 * @param fileName
	 * @throws LVException
	 */
	@Async
	protected void runProcess(Map<String, Serializable> params, InputStream inputStream, String fileName) throws LVException {
		
		IExecutorMaster executorMaster = new ThreadPoolExecutorMaster(params, this.corePoolSize, this.maximumPoolSize, this.keepAliveTime, this.threadExecutorTimeoutUnit, this.bridge, this.queueCapacity, this.processMonitor);

		try {
			
			OPCPackage pkg = OPCPackage.open(fileName);

			XSSFReader reader = new XSSFReader(pkg);
			StylesTable styles = reader.getStylesTable();
			ReadOnlySharedStringsTable stringsTable = new ReadOnlySharedStringsTable(pkg);
			final XMLReader parser = XMLReaderFactory.createXMLReader(SAX_PARSER);

			Iterator<InputStream> sheets = reader.getSheetsData();


			while (sheets.hasNext()) {

				InputStream sheetStream = sheets.next();

				if (log.isDebugEnabled()) {
					log.debug("Iniciando parsing do arquivo " + inputStream);
				}

				final InputSource sheetSource = new InputSource(sheetStream);

				// um handler novo para cada sheet!
				XLSXSAXHandler handler = new XLSXSAXHandler(styles, executorMaster, this.processMonitor, this.lotBufferSize, this.layoutParser, stringsTable);
				parser.setContentHandler(handler);

				try {
					parser.parse(sheetSource);
				} catch (Exception e) {
					log.error("Não deveria ter estourado erro aqui!");
					log.error("Erro", e);
					throw new LVRuntimeException("Erro não esperado ao tentar processar o arquivo!", e);
				}
			}
			
			//aguarda todas as threads faltantes terminadas
			executorMaster.waitThreadsCompletion(threadExecutorTimeout, threadExecutorTimeoutUnit);
			processMonitor.processingEnd(threadExecutorTimeout, threadExecutorTimeoutUnit);

			
			if(log.isDebugEnabled()){
				log.debug("Processamento da planilha {} executado com sucesso!", new Object[]{inputStream});
			}
			
		} catch (InvalidFormatException e) {
			log.error("Erro ao tentar abrir planilha. Formato de arquivo invalido!",e);
			throw new LVException("Erro ao tentar abrir planilha. Formato de arquivo invalido!",e);
			
		} catch (IOException e) {
			log.error("Erro ao tentar abrir planilha!",e);
			throw new LVException("Erro ao tentar abrir planilha!",e);
			
		} catch (OpenXML4JException e) {
			log.error("Erro ao tentar abrir planilha!",e);
			throw new LVException("Erro ao tentar abrir planilha!",e);
			
		} catch (SAXException e) {
			log.error("Erro ao tentar parsear XML referente aa planilha!",e);
			throw new LVException("Erro ao tentar parsear XML referente aa planilha!",e);
			
		} catch (InterruptedException e) {
			log.error("Erro aguardando finalizacao das threads!", e);
			throw new RuntimeException("Erro aguardando finalizacao das threads!", e);
		}
	}

	
	public ILayoutParser getLayoutParser() {
		return layoutParser;
	}

	public IBridge getBridge() {
		return bridge;
	}

	public IProcessMonitor getProcessMonitor() {
		return processMonitor;
	}

	public int getLotBufferSize() {
		return lotBufferSize;
	}

	public int getCorePoolSize() {
		return corePoolSize;
	}

	public int getMaximumPoolSize() {
		return maximumPoolSize;
	}

	public int getQueueCapacity() {
		return queueCapacity;
	}

	public long getKeepAliveTime() {
		return keepAliveTime;
	}

	public TimeUnit getThreadExecutorTimeoutUnit() {
		return threadExecutorTimeoutUnit;
	}

	public int getThreadExecutorTimeout() {
		return threadExecutorTimeout;
	}

	public void setLayoutParser(ILayoutParser layoutParser) {
		this.layoutParser = layoutParser;
	}

	public void setBridge(IBridge bridge) {
		this.bridge = bridge;
	}

	public void setProcessMonitor(IProcessMonitor processMonitor) {
		this.processMonitor = processMonitor;
	}

	public void setLotBufferSize(int lotBufferSize) {
		this.lotBufferSize = lotBufferSize;
	}

	public void setCorePoolSize(int corePoolSize) {
		this.corePoolSize = corePoolSize;
	}

	public void setMaximumPoolSize(int maximumPoolSize) {
		this.maximumPoolSize = maximumPoolSize;
	}

	public void setQueueCapacity(int queueCapacity) {
		this.queueCapacity = queueCapacity;
	}

	public void setKeepAliveTime(long keepAliveTime) {
		this.keepAliveTime = keepAliveTime;
	}

	public void setThreadExecutorTimeoutUnit(TimeUnit threadExecutorTimeoutUnit) {
		this.threadExecutorTimeoutUnit = threadExecutorTimeoutUnit;
	}

	public void setThreadExecutorTimeout(int threadExecutorTimeout) {
		this.threadExecutorTimeout = threadExecutorTimeout;
	}

}
