package br.com.mapfre.lv.layoutmodel;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

import org.apache.poi.ss.usermodel.BuiltinFormats;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;
import org.apache.poi.xssf.model.StylesTable;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.NumberUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import br.com.mapfre.lv.parser.EnumDataType;

/**
 * Implementação de default handler apenas para efetuar a leitura de uma planilha XLSX e acumular os valores lidos em layoutModel.getValues
 */
public class LayoutSAXHandler extends AbstractSAXHandler {

	private static final Logger log = LoggerFactory.getLogger(LayoutSAXHandler.class);

	// Estado do processamento da planilha. Muda a cada célula processada
	private Integer colunaAtual;
	private int formatIndex;
	private String formatString;
	private String numeroCelulaAtual;
	// flag para controlar leitura de conteuco. True qdo o elemento da vez é um "v"
	private boolean lerConteudo = false;

	// Componentes para processamento de planilhas
	// Mantem-se os mesmos até o final do processamento
	private final StylesTable styles;
	private LayoutModelVO layoutModel;

	private Map<Integer, CellValueVO> rowValueMap;

	private Integer linha;

	private Integer maxLinhas;

	/**
	 * Constroi XLSXSheetHandler. Apenas recebe componentes necessários para processamento da planilha
	 * 
	 * @param executorMaster
	 * @param processMonitor
	 * @param stringsTable
	 * @param stringsTable
	 * @param layoutModelVO
	 */
	public LayoutSAXHandler(StylesTable styles, ReadOnlySharedStringsTable stringsTable, LayoutModelVO layoutModelVO, Integer maxLinhas) {
		this.styles = styles;
		this.stringsTable = stringsTable;
		this.layoutModel = layoutModelVO;
		this.maxLinhas = maxLinhas;
	}

	/**
	 * Implementacao de startElement
	 */
	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {

		if (log.isDebugEnabled()) {
			log.debug("Iniciando a leitura do elemento {}", new Object[] { localName });
		}

		if (localName.equals("row")) {
			// iniciando a leitura de um novo registro

			String strVal = attributes.getValue("r");
			this.linha = NumberUtils.parseNumber(strVal, Integer.class);

			if (isNotHeader(this.linha)) {

				this.rowValueMap = new HashMap<Integer, CellValueVO>();
				this.layoutModel.getValues().add(this.rowValueMap);

			}

		} else if ("inlineStr".equals(name) || "v".equals(name)) {
			// Clear contents cache
			this.lerConteudo = true;
			this.cellValRaw.setLength(0);

		} else if ("c".equals(name)) {
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
				// with a special style or format
				XSSFCellStyle style = styles.getStyleAt(Integer.parseInt(cellStyleStr));

				this.formatIndex = style.getDataFormat();
				this.formatString = style.getDataFormatString();

				if (this.formatString == null) {
					this.formatString = BuiltinFormats.getBuiltinFormat(this.formatIndex);

				} else if (DateUtil.isADateFormat(formatIndex, formatString)) {
					this.dataType = EnumDataType.DATE;
				}
			}
		}
	}

	/**
	 * Implementacao de endElement.
	 * 
	 * Finaliza a leitura de elementos "v", atribuindo seu valor à lista de campos do cabecalho da planilha ou dos valores da planilha
	 */
	public void endElement(String uri, String localName, String name) throws SAXException {
		if (log.isDebugEnabled()) {
			log.debug("Finalizando a leitura do elemento {}", new Object[] { localName });
		}

		if (localName.equals("v")) {

			// fim do elemento "V". Não é necessário ler nada até q outro "V" comece
			this.lerConteudo = false;

			if (readingHeaderValues()) {

				//apenas carrega os metadados da coluna atual, se a mesma não estiver carregada.
				//Quando um layout esta sendo criado do zero, é importante fazer a carga inicial. Se for uma edição de layout é correto manter os dados atuais editados pelo usuário.
				if(!this.layoutModel.getFields().containsKey(this.colunaAtual)){
					LayoutColumnVO fieldVO = getLayoutFieldVO();
					this.layoutModel.getFields().put(this.colunaAtual, fieldVO);
				}

			} else {

				// le valor da linha/coluna atual da planilha
				CellValueVO cellValueVO = createCelValueVO();

				setLayoutFieldDataType();
				
				//como rowValueMap é transiente, deve ser carregado a cada vez que um layout for criado do zero ou editado.
				this.rowValueMap.put(this.colunaAtual, cellValueVO);
			}
		} else if ("row".equals(localName)) {

			
			
			if (this.maxLinhas != null && layoutModel.getValues()!=null && layoutModel.getValues().size() >= this.maxLinhas) {

				throw new LVStopSAXParserException("Máximo de linhas atingidas (" + this.maxLinhas + ")");

			}

		}

	}

	
	/**
	 * Caso o layoutFieldVO referente à colunaAtual não tenha sido carregado, seta as informações principais.
	 * Se for uma edição de layout por exemplo, não faz nada. 
	 */
	protected void setLayoutFieldDataType() {
		LayoutColumnVO layoutColumnVO = this.layoutModel.getFields().get(this.colunaAtual);
		if(layoutColumnVO.getDataType()==null){
			layoutColumnVO.setDataType(this.dataType);
			layoutColumnVO.setFormatIndex(this.formatIndex);
			layoutColumnVO.setFormatString(this.formatString);
			
		}else if(!layoutColumnVO.getDataType().equals(dataType)){
			log.error("Nem todos os campos da coluna "+this.colunaAtual+" da planilha são do mesmo tipo!");					
		}
	}

	/**
	 * Cria um elemento LayoutColumnVO, referente ao valor do cabecalho da planilha q esta sendo lido no momento.
	 * 
	 * @return
	 */
	protected LayoutColumnVO getLayoutFieldVO() {
		
		
		
		Serializable cellVal = getVal();

		LayoutColumnVO fieldVO = new LayoutColumnVO();
		fieldVO.setLayoutModelVO(this.layoutModel);
		fieldVO.setColuna(this.colunaAtual);
		fieldVO.setNome("" + cellVal);

		
		return fieldVO;
	}
	
	
	/**
	 * Testa se a linha atual não faz parte do header
	 * 
	 * @param linhaAtual
	 * @return
	 */
	protected boolean isNotHeader(Integer linhaAtual) {
		return linhaAtual > this.layoutModel.getHeaderLine();
	}

	/**
	 * 
	 * @param nextDataType
	 * @param cellValRaw
	 * @param cellValueVO
	 * @param colunaAtual
	 * @param formatString
	 * @param strings
	 * @return
	 */
	protected CellValueVO createCelValueVO() {

		CellValueVO cellValueVO = new CellValueVO();
		
		Serializable val = getVal();
		String valRaw = getValRaw();

		cellValueVO.setLinha(this.linha);
		cellValueVO.setColuna(this.colunaAtual);
		cellValueVO.setVal(val);
		cellValueVO.setValRaw(valRaw);
		
		return cellValueVO;
	}

	/**
	 * Converts an Excel column name like "C" to a zero-based index.
	 * 
	 * @param name
	 * @return Index corresponding to the specified name
	 */
	private Integer nameToColumn(String name) {
		Integer column = -1;
		for (int i = 0; i < name.length(); ++i) {
			int c = name.charAt(i);
			column = (column + 1) * 26 + c - 'A';
		}
		return column;
	}

	/**
	 * Apenda dados para leitura. Coleta dados contidos nos elementos "v", do XML da planilha XLSX
	 */
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (this.lerConteudo) {
			cellValRaw.append(ch, start, length);
		}
	}

	/**
	 * testa se os dados que estao sendo lidos atualmente pertencem ao header ou aos valores da planilha
	 * 
	 * @return
	 */
	protected boolean readingHeaderValues() {
		return this.rowValueMap == null;
	}

}