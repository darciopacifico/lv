package br.com.mapfre.lv.jobmanager;

import java.io.Serializable;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.util.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

import br.com.mapfre.lv.executor.IExecutorMaster;
import br.com.mapfre.lv.executor.IProcessMonitor;
import br.com.mapfre.lv.executor.LVLayoutException;
import br.com.mapfre.lv.executor.LotExecutionException;
import br.com.mapfre.lv.layout.ILayoutParser;
import br.com.mapfre.lv.layoutmodel.AbstractSAXHandler;
import br.com.mapfre.lv.parser.EnumDataType;
import br.com.mapfre.lv.payload.LotVO;
import br.com.mapfre.lv.payload.PayloadVO;

/**
 * Implementação de um SAX XML content handler, capaz de ler uma planilha contendo registros e 
 * acionar um IExecutorMaster, que enfileirará estes registros para processamento
 */
public class XLSXSAXHandler extends AbstractSAXHandler  {

	private static final Logger log = LoggerFactory.getLogger(XLSXSAXHandler.class);
	
	//Estado do processamento da planilha. Muda a cada célula processada
	private Integer colunaAtual;
	private int formatIndex;
	private String formatString;
	private String numeroCelulaAtual;
	
	// Lote e payload q está sendo lido
	private PayloadVO payloadVO;
	private LotVO lotVO =null;
	
	//flag para controlar leitura de conteuco. True qdo o elemento da vez é um "v"
	private boolean lerConteudo = false;
	


	//Componentes para processamento de planilhas
	//Mantem-se os mesmos até o final do processamento
	private final StylesTable styles;
	private final IExecutorMaster executorMaster;
	private final IProcessMonitor processMonitor;
	private final ILayoutParser parser;

	//define o tamanho de cada lote de processamento
	private final int lotSize;

	

	/**
	 * Constroi XLSXSheetHandler. Apenas recebe componentes necessários para processamento da planilha
	 * 
	 * @param executorMaster
	 * @param processMonitor
	 * @param stringsTable 
	 * @param stringsTable
	 */
	public XLSXSAXHandler(StylesTable styles, IExecutorMaster executorMaster, IProcessMonitor processMonitor, int lotSize, ILayoutParser layoutParser, ReadOnlySharedStringsTable stringsTable) {
		this.styles = styles;
		this.executorMaster = executorMaster;
		this.processMonitor = processMonitor;
		this.lotSize = lotSize;
		this.parser = layoutParser;
		
		this.stringsTable = stringsTable;
		
		
	}

	/**
	 * Implementacao de startElement
	 */
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
			
		if(log.isDebugEnabled()){
			log.debug("Iniciando a leitura do elemento {}",new Object[]{localName});
		}
		
		
		if(localName.equals("sheetData")){
			
		}else if(localName.equals("row")){
	  	//iniciando a leitura de um novo registro
		  
	  	String strVal = attributes.getValue("r");
	  	Integer linhaAtual = NumberUtils.parseNumber(strVal, Integer.class);
	  	
	  	
	  	if(this.parser.isNotHeader(linhaAtual)){
	  		
	  		if(this.lotVO==null){
					//inicia novo lote
		  		criarNovoLote(linhaAtual);
	  		}
	  		
	  		this.payloadVO = new PayloadVO();
	  		this.payloadVO.setLinha(linhaAtual);
	  	}
	  	
	  	
	  	
	  }else if ("inlineStr".equals(name) || "v".equals(name)) {
            // Clear contents cache
		  this.lerConteudo=true;
            this.cellValRaw.setLength(0);
            
    }else if ("c".equals(name)) {
	  	this.numeroCelulaAtual = attributes.getValue("r");
	  
      int firstDigit = -1;
      for (int c = 0; c < this.numeroCelulaAtual.length(); ++c) {
          if (Character.isDigit(this.numeroCelulaAtual.charAt(c))) {
              firstDigit = c;
              break;
          }
      }
      colunaAtual = nameToColumn(this.numeroCelulaAtual.substring(0, firstDigit));

      // Set up defaults.
      this.dataType = EnumDataType.NUMBER;
      this.formatIndex = -1;
      this.formatString = null;
      
      String cellType = attributes.getValue("t");
      String cellStyleStr = attributes.getValue("s");
        
      if ("b".equals(cellType))
      	this.dataType = EnumDataType.BOOL;
      else if ("e".equals(cellType))
      	this.dataType = EnumDataType.ERROR;
      else if ("inlineStr".equals(cellType))
      	this.dataType = EnumDataType.INLINESTR;
      else if ("s".equals(cellType))
      	this.dataType = EnumDataType.SSTINDEX;
      else if ("str".equals(cellType))
      	this.dataType = EnumDataType.FORMULA;
      else if (cellStyleStr != null) {
      	
        // It's a number, but almost certainly one
        //  with a special style or format 
        XSSFCellStyle style = styles.getStyleAt(Integer.parseInt(cellStyleStr));
        
        this.formatIndex = style.getDataFormat();
        this.formatString = style.getDataFormatString();
        
        if (this.formatString == null){
          this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);
            
        }else if(DateUtil.isADateFormat(formatIndex, formatString)){
        	this.dataType = EnumDataType.DATE;
        }
      }
    }
	}
	

	/**
	 * Implementacao de endElement. Coleta valores das celulas ou termina a leitura de um registro
	 */
	public void endElement(String uri, String localName, String name) throws SAXException {

		if(this.payloadVO==null){
			return;
		}
		
		if(log.isDebugEnabled()){
			log.debug("Finalizando a leitura do elemento {}",new Object[]{localName});
		}
		
		
		//System.out.println("finaliz "+localName);
		// finalizar elemento V
		if (localName.equals("v")) {

			// fim do elemento "V". Não é necessário ler nada até q outro "V" comece
			this.lerConteudo = false;
			
			//delega à implementação de ILayoutParser o processamento da linha x coluna
			
			
			try {
				
				Serializable valObject 	= this.getVal();
				String valRaw 			 		= this.getValRaw();
			
				this.parser.parseCellData(valRaw, valObject, this.colunaAtual, this.payloadVO);
				

			} catch (LVLayoutException e) {
				
				this.processMonitor.parsingCellDataError(e, this.payloadVO, this.colunaAtual);
			}
				
			
			// finalizar linha
		} else if (localName.equals("row")) {

			if(this.payloadVO!=null){
				
				//delega ao LayoutParser a validacao da linha
				//registra faltas no lotVO.getFaults
				this.parser.validatePayload(this.payloadVO, this.lotVO);
				
				//adiciona payloadVO atual no lote
				this.lotVO.getPayloadVOs().add(this.payloadVO);
				
			}

			
			//teste se o lote atual acabou
			if(fimDeLote()){
				
				try {
					//atribui qual é a linha final do lote
					this.lotVO.setToLine(this.payloadVO.getLinha());
					
					//encaminha o lote atual para processamento
					this.executorMaster.execute(this.lotVO);
					
					//caso haja uma próxima linha, este atributo null sinaliza para criacao de um novo lote
					this.lotVO=null;
					
				} catch (LotExecutionException e) {
					//TODO: IMPLEMENTAR TRATAMENTO DE ERROS com IProcessMonitor. Este erro dever estar contido nas criticas
					throw new RuntimeException("Erro ao tentar processar lote atual ("+this.lotVO+")",e);
				}
			}
			
		}else if (localName.equals("sheetData")){
			//fim da planilha
			
			try {

				this.lotVO.setToLine(this.payloadVO.getLinha());
				this.processMonitor.setFileSize(this.payloadVO.getLinha());
				
				//testa se o ultimo lote possui residuo final para processar
				if(loteAtualPossuiResiduoFinal()){
					//logaPayloadLotVO(payloadLotVO);
					executorMaster.execute(this.lotVO);
				}
				
			} catch (LotExecutionException e) {
				//TODO: IMPLEMENTAR TRATAMENTO DE ERROS com IProcessMonitor. Este erro dever estar contido nas criticas
				throw new RuntimeException("Erro ao tentar processar lote atual ("+this.lotVO+")",e);
			}
			
			
		}
	}

	
	/**
	 * Configura a linha inicial do lote
	 * @param linha
	 */
	private void setLotFrom(Integer linha) {
		
		if(this.lotVO.getFromLine()==null){
			lotVO.setFromLine(linha);
		}
		
	}

	/**
	 * Cria novo lote de registros
	 */
	protected void criarNovoLote(Integer linhaInicial) {
		this.lotVO = new LotVO(this.lotSize);
		setLotFrom(linhaInicial);
	}


	/**
	 * Determina se o lote de registros atual deve ser salvo
	 * @return
	 */
	private boolean fimDeLote() {
		boolean fimLoteAtual = this.lotVO.getPayloadVOs().size()>=this.lotSize;
		
		return fimLoteAtual;
	}
	

	/**
	 * Converts an Excel column name like "C" to a zero-based index.
	 * 
	 * @param name
	 * @return Index corresponding to the specified name
	 */
	private int nameToColumn(String name) {
		int column = -1;
		for (int i = 0; i < name.length(); ++i) {
			int c = name.charAt(i);
			column = (column + 1) * 26 + c - 'A';
		}
		return column;
	}


	/**
	 * Avalia se o lote atual possui resíduo de registros para serem processados.
	 * 
	 * Este método é chamado na finalização da leitura do ultimo arquivo
	 *  
	 * @return
	 */
	protected boolean loteAtualPossuiResiduoFinal() {
		boolean possuiResiduo = !CollectionUtils.isEmpty(this.lotVO.getPayloadVOs());
		
		if(log.isDebugEnabled()){
			log.debug("Processando residuo final do arquivo, contendo {} registros.", new Object[]{this.lotVO.getPayloadVOs()});
		}
		
		return possuiResiduo;
	}

	/**
	 * Apenda dados para leitura. Coleta dados contidos nos elementos "v", do XML da planilha XLSX
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (this.lerConteudo) {
			cellValRaw.append(ch, start, length);
		}
	}

}