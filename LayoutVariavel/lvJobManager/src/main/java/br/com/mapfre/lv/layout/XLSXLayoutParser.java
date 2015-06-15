package br.com.mapfre.lv.layout;

import java.io.Serializable;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;

import br.com.mapfre.lv.executor.LVLayoutException;
import br.com.mapfre.lv.jobmanager.EnumParseResult;
import br.com.mapfre.lv.layoutmodel.LayoutColumnVO;
import br.com.mapfre.lv.layoutmodel.LayoutModelVO;
import br.com.mapfre.lv.layoutmodel.ServiceParamVO;
import br.com.mapfre.lv.payload.LotVO;
import br.com.mapfre.lv.payload.PayloadVO;

/**
 * Implementa��o de ILayoutParser. Aplica as regras de layout aos dados lidos.
 * 
 * @author darcio
 */
public class XLSXLayoutParser implements ILayoutParser {

	private static final long serialVersionUID = 1591814513502064503L;
	private static final Logger log = LoggerFactory.getLogger(XLSXLayoutParser.class);

	private LayoutModelVO layoutModelVO;
	private Map<String, Serializable> substituteVals;

	public XLSXLayoutParser(LayoutModelVO layoutModelVO, Map<String, Serializable> substituteVals) {
		this.layoutModelVO = layoutModelVO;
		this.substituteVals = substituteVals;
	}



	/**
	 * Aplica as valida��es previstas no layoutModel e coloca as criticas no lote de dados referente (@LotVO lotVO)
	 */
	@Override
	public void validatePayload(PayloadVO payloadVO, LotVO lotVO) {
		
		//recupera campos definidos no layout escolhido pelo usu�rio
		Collection<LayoutColumnVO> fields = layoutModelVO.getFields().values();
		
		
		for (LayoutColumnVO layoutColumnVO : fields) {

			//recupera o respectivo campo do servi�o de destino (um para um)
			ServiceParamVO serviceParamVO = layoutColumnVO.getServiceFieldVO();
			
			if(serviceParamVO!=null){
			
				//recupera o valor lido da planilha e contido no payload
				Serializable value = payloadVO.getFields().get(serviceParamVO.getName());
				
				//aplica validacoes basicas para este campo e colhe as faltas na colecao
				List<FaultVO> faults = validateCellVal(layoutColumnVO, value, payloadVO.getLinha(), serviceParamVO.getFieldOrder());
				
				
				if(!CollectionUtils.isEmpty(faults)){
				
					//TODO: Esta decis�o dever� ser din�mica, de acodo com as regras definidas pelo usu�rio no layout. 
					//Por enquanto qqr erro de valida��o rejeita o payload 
					payloadVO.setProcessar(false);
	
					//coleta as faltas
					lotVO.getFaults().addAll(faults);
				}
			}
		}
	}
	
	
	/**
	 * Testa se a linha atual faz parte do header
	 */
	@Override
	public boolean isNotHeader(Integer linhaAtual) {
		return linhaAtual > layoutModelVO.getHeaderLine();
	}
	
	
	/**
	 * L� o valor bruto contido na c�lula da planilha excel (cellValRaw) e determina qual ser� o objeto de sa�da, a partir do tipo de dado (nextDataType2),
	 * formato (formatString2) e layout escolhido pelo usu�rio. <br/>
	 * Tamb�m determina qual ser� a chave (String) da respectiva coluna no mapa de valores contido em @PayloadVO.getFields(). <br/>
	 * O mapeamento dos valores de cada linha da planilha, contido em @PayloadVO.getFields() ser� utilizado pela respectiva implementa��o de @IBridge
	 * 
	 * @param cellValueRaw valor bruto contido na celula (Sempre string)
	 * @param valObject valor objeto contido na celular (Number ou Date, normalmente)
	 * @param colunaAtual coluna atual da planilha
	 * @param payloadVO carga de dados para processamento por uma implementacao de @IBridge
	 * 
	 */
	@Override
	public void parseCellData(String cellValueRaw, Serializable valObject, Integer colunaAtual, PayloadVO payloadVO) {

		//recupera modelo de campos do layout e do servico de destino
		LayoutColumnVO layoutColumnVO = layoutModelVO.getFields().get(colunaAtual);
		
		//checa se a coluna atua � necess�ria no layout pr� definido
		if(layoutColumnVO==null){
			if(log.isDebugEnabled()){
				log.debug("A coluna {} n�o � utilizada no layoutModel {} ", new Object[]{colunaAtual, layoutModelVO});
			}
			return;
		}
		
		//Somente aplica as regras de valida��o se a linha atual n�o for parte do cabecalho da planilha. 
		if(isNotHeader(payloadVO.getLinha())){
		
			if(layoutColumnVO.getServiceFieldVO()!=null){// testa se h� mapeamento de campo de servico mapeado.
				
				//recupera nome e valor da coluna/celula
				String mapKey = getMapKey(layoutColumnVO);
	
				//Recupera valor da coluna/celula, de acordo com as regras, formatacoes e tipos estabelecidos em layoutFields
				Serializable cellVal = getCellValue(cellValueRaw, valObject, layoutColumnVO, mapKey);
	
				//coleciona os dados retornados
				payloadVO.getFields().put(mapKey, cellVal);
				
			}
			
		}
	}



	protected Serializable getCellValue(String cellValueRaw, Serializable valObject, LayoutColumnVO layoutColumnVO, String mapKey) {
		
		Serializable returnVal;
		
		if(substituteVals!=null && substituteVals.containsKey(mapKey) ){
		
			returnVal = substituteVals.get(mapKey);
			
		}else{
			
			returnVal = layoutColumnVO.getObjectValue(cellValueRaw, valObject);
		}
		
		return returnVal;
	}

	
	
	/**
	 * Recupera o nome do campo
	 * @param layoutColumnVO
	 * @return
	 */
	protected String getMapKey(LayoutColumnVO layoutColumnVO) {
		ServiceParamVO serviceParamVO = layoutColumnVO.getServiceFieldVO();
		String mapKey = serviceParamVO.getName();
		return mapKey;
	}
	
	/**
	 * Aplica as valida��es basicas ao valor lido, de acordo com o esperado pelo @ServiceParamVO contido no layoutFieldVO informado
	 * 
	 * @param layoutColumnVO
	 * @param nextDataType
	 * @param cellVal
	 * @param mapKey
	 * @param colunaAtual 
	 * @param linhaAtual 
	 * @return
	 * @throws LVLayoutException
	 */
	protected List<FaultVO> validateCellVal(
			LayoutColumnVO  layoutColumnVO   , 
			Serializable   cellVal, 
			Integer linhaAtual, 
			Integer colunaAtual           ) {
		
		List<FaultVO> result = new ArrayList<FaultVO>();
		
		validateMandatory(result, cellVal,  layoutColumnVO, linhaAtual,  colunaAtual);
		validateRange(result, cellVal, layoutColumnVO, linhaAtual,  colunaAtual);
		validateRegex(result, cellVal, layoutColumnVO, linhaAtual,  colunaAtual);
		
		return result;
	}

	/**
	 * Checa se o dado � mandat�rio para o servicefield.
	 * 
	 * Caso seja mandat�rio e o dado seja null ou blank, cria um @FaultVO e adiciona-o � faults.
	 * 
	 * @param faults
	 * @param cellVal
	 * @param layoutColumnVO
	 * @param mandatory
	 */
	private void validateMandatory(List<FaultVO> faults, Serializable cellVal, LayoutColumnVO  layoutColumnVO, Integer linhaAtual, Integer colunaAtual) {
		
		final ServiceParamVO serviceField = layoutColumnVO.getServiceFieldVO();
		Boolean mandatory = serviceField.getFieldRequired();
		
		if(mandatory!=null && mandatory && (cellVal == null || StringUtils.isBlank("" + cellVal))){
			
			FaultVO faultVO = new FaultVO();
			faultVO.setParseResult(EnumParseResult.ERR_MANDATORY);
			faultVO.setLinha(linhaAtual);
			faultVO.setColuna(colunaAtual);
			faultVO.setValorOriginal(""+cellVal);
			faultVO.setNomeColuna(serviceField.getName());
			faultVO.setMsg(MessageFormat.format("O valor � obrigatorio!", new Object[]{cellVal}));
			
			faults.add(faultVO);
		}
	}

	/**
	 * Avalia se o dado da celula est� de acordo com o range de valores.
	 * 
	 * Caso n�o seja valido, cria um @FaultVO e adiciona-o na colecao result
	 * 
	 * @param result
	 * @param cellVal
	 * @param layoutColumnVO
	 * @param minSize
	 * @param maxSize
	 * @param colunaAtual 
	 * @param linhaAtual 
	 */
	private void validateRange(List<FaultVO> result, Serializable cellVal, LayoutColumnVO  layoutColumnVO, Integer linhaAtual, Integer colunaAtual) {
		
		final ServiceParamVO serviceParamVO = layoutColumnVO.getServiceFieldVO();
		Double minSize = serviceParamVO.getMinSize();
		Double maxSize = serviceParamVO.getMaxSize();
		
		if(minSize==null && maxSize==null){
			 //nao � necessario continuar
			return;
		}
		
		
		if(cellVal!=null){
			
			if(cellVal instanceof String){
				
				Double len = new Double(((String) cellVal).length());
				
				if(isOverRange(minSize, maxSize, len)){
					
					FaultVO faultVO = new FaultVO();
					faultVO.setParseResult(EnumParseResult.ERR_RANGE);
					faultVO.setLinha(linhaAtual);
					faultVO.setColuna(colunaAtual);
					faultVO.setValorOriginal(""+cellVal);
					faultVO.setNomeColuna(serviceParamVO.getName());
					faultVO.setMsg(MessageFormat.format("O valor ''{0}'' esta fora do limite de tamanho, determinado entre {1} e {2}!", new Object[]{cellVal,minSize,maxSize}));
					
					result.add(faultVO);
				}
				
			}else if(cellVal instanceof Number){
				
				Double nVall = ((Number)cellVal).doubleValue();
				
				if(isOverRange(minSize, maxSize, nVall)){
					
					FaultVO faultVO = new FaultVO();
					faultVO.setParseResult(EnumParseResult.ERR_RANGE);
					faultVO.setLinha(linhaAtual);
					faultVO.setColuna(colunaAtual);
					faultVO.setNomeColuna(serviceParamVO.getName());
					faultVO.setValorOriginal(""+cellVal);
					faultVO.setMsg(MessageFormat.format("O valor {0} esta fora do limite de tamanho, determinado entre {1} e {2}!", new Object[]{cellVal,minSize,maxSize}));
				
					result.add(faultVO);
					
				}
			}else{
				
				log.warn("A checagem de range s� pode ser feita em tipos de dados string ou numericos! O dado '{}' n�o � v�lido para checagem de range!",new Object[]{cellVal});
				
			}
		}
	}

	
	/**
	 * Checa range de valores
	 * @param minSize
	 * @param maxSize
	 * @param len
	 * @return
	 */
	protected boolean isOverRange(Double minSize, Double maxSize, Double len) {
		
		Boolean bool=false; 

		if(minSize!=null && maxSize!=null){
			bool = len>maxSize || len<minSize;
			
		}else if(minSize!=null){
			bool = len<minSize;

		}else if(maxSize!=null){
			bool = len>maxSize;
		}
		
		return bool;
	}
	
	/**
	 * Aplica validacao do dado da celula de acordo com um regex definido
	 * 
	 * @param result
	 * @param cellVal
	 * @param layoutColumnVO
	 * @param regex
	 * @param colunaAtual 
	 * @param linhaAtual 
	 */
	private void validateRegex(List<FaultVO> result, Serializable cellVal, LayoutColumnVO  layoutColumnVO, Integer linhaAtual, Integer colunaAtual) {
		
		final ServiceParamVO serviceParamVO = layoutColumnVO.getServiceFieldVO();
		String regex = serviceParamVO.getRegex();
		
		if(StringUtils.isBlank(regex))
			return;
		
		if(cellVal!=null){
			String strVal = cellVal+"";
			if(!strVal.matches(regex)){
				
				FaultVO faultVO = new FaultVO();
				faultVO.setParseResult(EnumParseResult.ERR_REGEX);
				faultVO.setLinha(linhaAtual);
				faultVO.setColuna(colunaAtual);
				faultVO.setValorOriginal(""+cellVal);
				faultVO.setNomeColuna(serviceParamVO.getName());
				faultVO.setMsg(MessageFormat.format("O valor {} n�o � compat�vel com a regex {}!", new Object[]{cellVal,regex}));
			
				result.add(faultVO);
			}
		}
	}

	/**
	 * Checa se o tipo de dado esperado pelo layout � compativel com o tipo de dado contido na celula
	 * @param payloadVO
	 * @param colunaAtual
	 * @param serviceParamVO
	 * @param cellValRaw
	 * @param type
	 * @param class1
	 * @throws LVLayoutException
	 */
	protected void checkType(PayloadVO payloadVO, Integer colunaAtual, ServiceParamVO serviceParamVO, String cellValRaw, Class<?> type, Class<?> class1) throws LVLayoutException {
		if(!type.isAssignableFrom(class1)){
			String textMsg = MessageFormat.format(
					"A linha ''{0}'', coluna ''{1}'', valor ''{2}'' da planilha n�o possui tipo de dado compat�vel com ''{3}'', esperado pelo campo ''{4}'' do layout informado!", 
					payloadVO.getLinha(), colunaAtual, cellValRaw, type.getName(), serviceParamVO.getName());
			
			throw new LVLayoutException(textMsg);
		}
	}

	/**
	 * 
	 * @return
	 */
	public LayoutModelVO getLayoutModelVO() {
		return layoutModelVO;
	}

	/**
	 * 
	 * @param layoutModelVO
	 */
	public void setLayoutModelVO(LayoutModelVO layoutModelVO) {
		this.layoutModelVO = layoutModelVO;
	}

}
