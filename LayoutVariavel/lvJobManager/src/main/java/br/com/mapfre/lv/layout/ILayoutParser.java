package br.com.mapfre.lv.layout;

import java.io.Serializable;
import java.util.List;

import org.apache.poi.xssf.eventusermodel.ReadOnlySharedStringsTable;

import br.com.mapfre.lv.executor.LVLayoutException;
import br.com.mapfre.lv.layoutmodel.LayoutColumnVO;
import br.com.mapfre.lv.parser.EnumDataType;
import br.com.mapfre.lv.payload.LotVO;
import br.com.mapfre.lv.payload.PayloadVO;

/**
 * Contrato para o componente que aplicará as regras de layout, 
 * tipos de dados e formatação à leitura de uma planilha Excel (XLSX apenas).
 * <br/>
 * <br/>
 * A classe @XLSXSheetHandler, que é uma implementação de SAX @ContentHandler, 
 * faz a navegação pelas linhas e colunas da planilha, delegando a este componente 
 * a responsabilidade de aplicar as regras de layout, formatação, parsing de dados etc.   
 * 
 * @author darcio
 */
public interface ILayoutParser extends Serializable{

	/**
	 * Lê o valor bruto contido na célula da planilha excel (cellValRaw) 
	 * e determina qual será o objeto de saída, a partir do tipo de dado 
	 * (nextDataType2), formato (formatString2) e layout escolhido pelo usuário.
	 * <br/>
	 * Determina qual será a chave (String) da respectiva 
	 * coluna no mapa de valores contido em @PayloadVO.getFields().
	 * <br/>
	 * O mapeamento dos valores de cada linha da planilha, 
	 * contifo em @PayloadVO.getFields() será utilizado 
	 * pela respectiva implementação de @IBridge
	 *
	 * 
	 * @param cellValRaw valor bruto contido na celula
	 * @param valObject 
	 * @param colunaAtual coluna atual da planilha
	 * @param payloadVO 
	 * 
	 * @throws LVLayoutException 
	 */
	void parseCellData(String cellValRaw, Serializable valObject, Integer colunaAtual, PayloadVO payloadVO) throws LVLayoutException;
	
	
	/**
	 * 
	 * @param payloadVO
	 * @param lotVO
	 */
	void validatePayload(PayloadVO payloadVO, LotVO lotVO);


	boolean isNotHeader(Integer linhaAtual);
	
}
