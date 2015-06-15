package br.com.mapfre.lv.jobmanager;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.ApplicationContext;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import br.com.mapfre.lv.LVException;
import br.com.mapfre.lv.executor.IProcessMonitor;
import br.com.mapfre.lv.executor.LVLayoutException;
import br.com.mapfre.lv.layout.FaultVO;
import br.com.mapfre.lv.payload.EnumLotStatus;
import br.com.mapfre.lv.payload.LotPK;
import br.com.mapfre.lv.payload.LotVO;
import br.com.mapfre.lv.payload.PayloadVO;

/**
 * Implementação de monitor de processos com modelo persistente. 
 * Registra início, sucesso ou erros no processamento de arquivos e lotes de registros.
 *
 * @author darcio
 */
@Qualifier("persistentProcessMonitor")
@Component("persistentProcessMonitor")
public class PersistentProcessMonitor extends JdbcDaoSupport implements IProcessMonitor {

	private static final String QUERY_FOR_FILEVO = "select * from FileVO where fileName=? ";

	private static final Logger log = LoggerFactory.getLogger(PersistentProcessMonitor.class);

	private SimpleJdbcInsert jdbcInsertFileVO;
	private SimpleJdbcInsert jdbcInsertLotVO;
	private SimpleJdbcInsert jdbcInsertFaultVO;

	private Long longLotPK = 0l;

	private FileVO fileVO = new FileVO();

	@Autowired
	private ApplicationContext applicationContext;


	
	/**
	 * 
	 */
	public PersistentProcessMonitor() {
	}

	
	/**
	 * Constroi persistent monitor com os JDBC inserts de spring
	 * 
	 * @param dataSource
	 */
	@Autowired
	public PersistentProcessMonitor(@Qualifier("lvControlDS") DataSource dataSource) {
		setDataSource(dataSource);
		jdbcInsertFileVO = new SimpleJdbcInsert(dataSource).withTableName("FileVO").usingGeneratedKeyColumns("PK");
		jdbcInsertLotVO = new SimpleJdbcInsert(dataSource).withTableName("LotVO");
		jdbcInsertFaultVO = new SimpleJdbcInsert(dataSource).withTableName("FaultVO");
	}

	/**
	 * Cria o Vo para controle do processo e marca o início do processo
	 * @return 
	 * @throws LVException 
	 */
	@Override
	public FileVO processingStart(String fileName, Boolean overrideFileProcessing) throws LVException {

		checkPreviousFileProcessing(fileName, overrideFileProcessing);
		
		this.fileVO.setFileName(fileName);
		this.fileVO.setDt_criacao(new Date());

		SqlParameterSource fileParams = new BeanPropertySqlParameterSource(this.fileVO);
		Number pk = jdbcInsertFileVO.executeAndReturnKey(fileParams);

		this.fileVO.setPK(pk.longValue());
		
		return this.fileVO;
	}

	/**
	 * Checa se o arquivo já foi processado
	 * 
	 * @param fileName
	 * @param overrideFileProcessing
	 * @throws LVException
	 */
	protected void checkPreviousFileProcessing(String fileName, Boolean overrideFileProcessing) throws LVException {
		List<FileVO> registrosEncontrados = getJdbcTemplate().query(QUERY_FOR_FILEVO, new Object[]{fileName}, new BeanPropertyRowMapper<FileVO>(FileVO.class));
		
		if(!CollectionUtils.isEmpty(registrosEncontrados)){
			
			for (FileVO fileVO : registrosEncontrados) {
				log.warn("O arquivo de nome {}  já foi processado anteriormente! Registo de processamento = {} (tabela FileVO)!", new Object[]{fileVO.getFileName(), fileVO.getPK()});
			}
			
			if(!overrideFileProcessing){
				throw new LVException(MessageFormat.format("O arquivo de nome {0}  já foi processado! ",fileName));
			}
			
		}
	}

	/**
	 * Atribui o tamanho final do arquivo em quantidade de linhas
	 */
	@Override
	public void setFileSize(Integer qtdDeLinhas) {
		if (qtdDeLinhas == null || qtdDeLinhas <= 0)
			return;

		fileVO.setFileSize(qtdDeLinhas);

		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		jdbcTemplate.update("update FileVO set fileSize=? where PK = ?", qtdDeLinhas, fileVO.getPK());

	}

	/**
	 * Registra a finalização do processamento de um arquivo
	 */
	@Override
	public void processingEnd(int threadExecutorTimeout, TimeUnit threadExecutorTimeoutUnit) throws LVException{
		JdbcTemplate jdbcTemplate = getJdbcTemplate();
		jdbcTemplate.update("update FileVO set dt_finalizacao=?, fileSize=? where PK = ?", new Date(), fileVO.getFileSize(), fileVO.getPK());
		
		ThreadPoolTaskExecutor executor = (ThreadPoolTaskExecutor) applicationContext.getBean("jobControlTaskExecutor");
		executor.setWaitForTasksToCompleteOnShutdown(true);

		ThreadPoolExecutor threadExecutor = executor.getThreadPoolExecutor();

		threadExecutor.shutdown();
		try {
			threadExecutor.awaitTermination(threadExecutorTimeout, threadExecutorTimeoutUnit);
		} catch (InterruptedException e) {
			throw new LVException("Erro ao tentar aguardar finalizacao das threads do process monitor",e);
		}
	}

	
	/**
	 * Registrar falha ao tentar processar o arquivo
	 */
	@Override
	public void processingError(FileVO fileVO) {
		//TODO: REGISTRAR ERRO AO TENTAR PROCESSAR ARQUIVO
	}

	
	/**
	 * Cria um novo registro de lote
	 */
	@Override
	public void lotStart(LotVO lot) {

		LotPK lotPK = new LotPK();
		lotPK.setLotPK(getNextLotPK());
		lotPK.setFilePK(this.fileVO.getPK());

		lot.setDtCriacao(new Date());
		lot.setPK(lotPK);
	}

	/**
	 * Implementa política para testar se o lote pode ser processado
	 */
	@Override
	public boolean canLotStart(LotVO lot) {
		return lot.getFaults().isEmpty();
	}

	/**
	 * OK codigo resultado
	 */
	@Override
	public void lotEnd(LotVO lot, int[] results) {
		lot.setStatus(EnumLotStatus.PROCESSED);
		lot.setDtFinalizacao(new Date());
		lot.setResults(this.resultsToString(results));
		
		insertLot(lot);
	}


	/**
	 * Incrementador da PK de lote. Protegido com synchronized. Um acesso por vez no pool de threads...
	 * 
	 * @return
	 */
	protected synchronized Long getNextLotPK() {
		return ++longLotPK;
	}

	/**
	 * Converte um array de resultados numa string separada por virgulas
	 * 
	 * @param results
	 * @return
	 */
	private String resultsToString(int[] results) {
		String strResults;
		StringBuffer stringBuffer = new StringBuffer();

		String virgula = "";

		for (int r : results) {
			stringBuffer.append(virgula).append(r);
			virgula = ",";
		}

		strResults = stringBuffer.toString();
		return strResults;
	}

	
	/**
	 * Registra o erro informado na exception e o array de resultados
	 */
	@Override
	public void lotError(LotVO lotVO, Exception e, int[] results) {
		
		EnumLotStatus status = determinaLotStatus(results);
		
		lotVO.setDtFinalizacao(new Date());
		lotVO.setStatus(status);
		lotVO.setResults(resultsToString(results));
		
		List<FaultVO> faults = createFaults(e, lotVO, results);
		
		lotVO.getFaults().addAll(faults);
		
		log.error("Erro ao tentar o lote de dados de {} até {}. {}",new Object[]{lotVO.getFromLine(), lotVO.getToLine(), e.getMessage()});

		insertLot(lotVO);
		
	}

	
	/**
	 * Determina qual é o status do lote de registros
	 * @param results
	 * @return
	 */
	protected EnumLotStatus determinaLotStatus(int[] results) {
		EnumLotStatus status=EnumLotStatus.ERROR;
		
		boolean possuiErros = false;
		boolean possuiSucessos = false;
		for (int i : results) {
			if(i>=0){
				//zero mais registros afetados. Denota sucesso no processamento
				possuiSucessos=true;
			}else{
				//menor que zero denota erro no processamento
				possuiErros=true;
			}
			
			if(possuiErros && possuiSucessos){
				//OK. o lote foi parcialmente processado. Não preciso mais verificar o resto dos resultados
				break;
			}
		}
		
		if(possuiErros && possuiSucessos){
			status = EnumLotStatus.PARTIALLY_PROCESSED;
		}else if (possuiErros){
			status = EnumLotStatus.ERROR;
		}else if (possuiSucessos){
			status = EnumLotStatus.PROCESSED;
		}
		return status;
	}

	
	/**
	 * A partir da exceção e do array de resultados, registra os erros para cada linha
	 * @param e
	 * @param lotVO 
	 * @param results
	 * @return
	 */
	protected List<FaultVO> createFaults(Exception e, LotVO lotVO, int[] results) {
		
		
		List<FaultVO> faults = new ArrayList<FaultVO>(4);
		
		for(int i = 0; i<results.length; i++){

			int result = results[i];
			
			if(isFault(result)){
				
				FaultVO faultVO = new FaultVO();
				
				faultVO.setColuna(null);
				faultVO.setLinha(calculaLinha(lotVO, i ));
				faultVO.setMsg(e.getMessage());
				faultVO.setNomeColuna("[NAO ESPECIFICADO]");
				faultVO.setParseResult(EnumParseResult.ERR_INESPERADO);
				faultVO.setValorOriginal("[NAO ESPECIFICADO]");
				
				faults.add(faultVO);
			}
		}
		
		return faults;
	}
	

	/**
	 * Determina o numero da linha referente ao item i, de um array de resultados. 
	 * @param lotVO
	 * @param i
	 * @return
	 */
	private Integer calculaLinha(LotVO lotVO, int i) {
		return lotVO.getFromLine()+i;
	}

	/**
	 * 
	 * @param result
	 * @return
	 */
	private boolean isFault(int result) {
		return result<0;
	}

	
	/**
	 * Registra o erro informado como fault
	 */
	@Override
	public void lotError(LotVO lotVO, Exception e) {
		
		log.warn("Erro inesperado ao tentar executar o lote de registros. Arquivo {}, lote de {} até {}", new Object[]{lotVO.getPK().getFilePK(), lotVO.getFromLine(), lotVO.getToLine()});
		log.warn("Msg de erro:",e);
		
		lotVO.setStatus(EnumLotStatus.ERROR);
		applyGeneralResult(lotVO, EnumParseResult.ERR_DESCONHECIDO);
		
		FaultVO faultVO = new FaultVO();
		
		faultVO.setColuna(null);
		faultVO.setMsg("Erro inesperado: "+e.getMessage());
		faultVO.setNomeColuna(null);
		faultVO.setParseResult(EnumParseResult.ERR_DESCONHECIDO);
		faultVO.setValorOriginal(null);
		
		lotVO.getFaults().add(faultVO);
		
		insertLot(lotVO);
	}
	


	/**
	 * Registra o erro informado como fault
	 */
	@Override
	public void lotRuntimeError(LotVO lotVO, RuntimeException e) {

		lotVO.setStatus(EnumLotStatus.RUNTIME_ERROR);
		lotVO.setDtFinalizacao(new Date());
		applyGeneralResult(lotVO, EnumParseResult.ERR_RUNTIME);
		
		log.error("Erro de runtime ao tentar o lote de dados de {} até {}. {}",new Object[]{lotVO.getFromLine(), lotVO.getToLine(), e.getMessage()});
		
		insertLot(lotVO);
	}

	
	/**
	 * Registra lote como rejeitado. Normalmente invocado quando um lote não pode ser iniciado, de acordo com a politica implementada em @IProcessMonitor.canLotStart  
	 */
	@Override
	public void lotRejected(LotVO lotVO) {
		
		lotVO.setStatus(EnumLotStatus.REJECTED);
		lotVO.setDtFinalizacao(new Date());
		
		String results = getStringResultsLotRejected(lotVO);
		
		lotVO.setResults(results);
		
		log.error("Rejeitando o lote de registros de {} até {}!", new Object[]{lotVO.getFromLine(), lotVO.getToLine()});
		
		insertLot(lotVO);
	}

	
	/**
	 * Monta um string de resultados considerando que todos os registros foram rejeitados (-1) ou, 
	 * se existir, com o código referente à falta encontrada naquele registros, por exemplo, um dado obrigatório que não foi encontrado.
	 * 
	 * Recupera as eventuais faltas contidas em um lote de registros 
	 * @param lotVO
	 * @return
	 */
	protected String getStringResultsLotRejected(LotVO lotVO) {
		
		//monta mapa de linha vs faltas
		List<FaultVO> faults = lotVO.getFaults();
		Map<Integer, EnumParseResult> faultsMap = new HashMap<Integer, EnumParseResult>();
		for (FaultVO faultVO : faults) {
			faultsMap.put(faultVO.getLinha(), faultVO.getParseResult());
		}
		
		int qtdLinhas = lotVO.getToLine()-lotVO.getFromLine()+1;
		
		StringBuffer bufRes = new StringBuffer();
		
		String virg = "";
		
		//roda todas as linhas do lote
		for(int i=0; i<qtdLinhas; i++){
			
			//recupera a falta encontrada numa linha especifica
			EnumParseResult eRes = faultsMap.get(i+lotVO.getFromLine());
			
			String res;
			
			if(eRes!=null){
				// se há falta na linhas usa como código de retorno o código da falta
				res = ""+eRes.getCode(); 
			}else{
				// se não há falta na linha apenas atribui -1, que denota erro na linha
				res = "-1";
			}
			
			bufRes.append(virg).append(res);
			
			virg=",";
		}
		
		return bufRes.toString();
	}

	
	
	
	
	/**
	 * Deve registrar um erro ao tentar ler o dado de uma célula
	 */
	@Override
	public void parsingCellDataError(LVLayoutException e, PayloadVO payloadVO, Integer colunaAtual) {
		
		Integer linhaAtual = payloadVO!=null?payloadVO.getLinha():null;
		
		//TODO: IMPLEMENTAR REGISTRO PERSISTENTE DO ERRO
		log.error("Erro ao tentar processar o valor da linha {}, coluna, {}, mensagem: {}",new Object[]{linhaAtual,colunaAtual,e.getMessage()});
	}


	/**
	 * Cria uma string de resultados ("-1,-1,-1,-1,-1,-1...") com um resultado geral
	 * @param lotVO
	 * @param generalResult
	 */
	protected void applyGeneralResult(LotVO lotVO, EnumParseResult generalResult) {
		
		int qtdLinhas = lotVO.getToLine()-lotVO.getFromLine()+1;
		
 		String genResult = StringUtils.repeat(","+generalResult.getCode(), qtdLinhas);
 		
 		genResult = genResult.substring(1);
 		
 		lotVO.setResults(genResult);
		
	}



	/**
	 * Persiste o registro de processamento de um lote de registros 
	 * @param lot
	 */
	protected void insertLot(LotVO lot) {
		
		MapSqlParameterSource params = new MapSqlParameterSource();

		params.addValue("PK", lot.getPK());
		params.addValue("fromLine", lot.getFromLine());
		params.addValue("toLine", lot.getToLine());
		params.addValue("results", lot.getResults());
		params.addValue("lotPK", lot.getPK().getLotPK());
		params.addValue("filePK", lot.getPK().getFilePK());
		params.addValue("dtCriacao", lot.getDtCriacao());
		params.addValue("dtIniciacao", lot.getDtIniciacao());
		params.addValue("dtFinalizacao", lot.getDtFinalizacao());
		params.addValue("status", lot.getStatus());

		jdbcInsertLotVO.execute(params);

		
		
		List<FaultVO> faults = lot.getFaults();

		if(!CollectionUtils.isEmpty(faults)){
			
			MapSqlParameterSource[] mapFaultArray = new MapSqlParameterSource[faults.size()];
			
			int faultPK=1;
			for (FaultVO faultVO : faults) {
				MapSqlParameterSource mapF = new MapSqlParameterSource();
				
				mapF.addValue("faultPK"				, faultPK);
				mapF.addValue("filePK"				, lot.getPK().getFilePK());
				mapF.addValue("lotPK"					, lot.getPK().getLotPK());
				mapF.addValue("coluna"				, faultVO.getColuna());
				mapF.addValue("linha"					, faultVO.getLinha());
				mapF.addValue("msg"						, faultVO.getMsg());
				mapF.addValue("nomeColuna"		, faultVO.getNomeColuna());
				mapF.addValue("parseResult"		, faultVO.getParseResult());
				mapF.addValue("valorOriginal"	, faultVO.getValorOriginal());
				
				mapFaultArray[faultPK-1] = mapF;
				
				faultPK++;
			}

			jdbcInsertFaultVO.executeBatch(mapFaultArray);
			
		}
		
		this.fileVO.getLotVOs().add(lot);
		
	}	
}
