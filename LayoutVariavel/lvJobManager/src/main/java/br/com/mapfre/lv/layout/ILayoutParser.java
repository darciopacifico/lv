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
 * Contrato para o componente que aplicar� as regras de layout, 
 * tipos de dados e formata��o � leitura de uma planilha Excel (XLSX apenas).
 * <br/>
 * <br/>
 * A classe @XLSXSheetHandler, que � uma implementa��o de SAX @ContentHandler, 
 * faz a navega��o pelas linhas e colunas da planilha, delegando a este componente 
 * a responsabilidade de aplicar as regras de layout, formata��o, parsing de dados etc.   
 * 
 * @author darcio
 */
public interface ILayoutParser extends Serializable{

	/**
	 * L� o valor bruto contido na c�lula da planilha excel (cellValRaw) 
	 * e determina qual ser� o objeto de sa�da, a partir do tipo de dado 
	 * (nextDataType2), formato (formatString2) e layout escolhido pelo usu�rio.
	 * <br/>
	 * Determina qual ser� a chave (String) da respectiva 
	 * coluna no mapa de valores contido em @PayloadVO.getFields().
	 * <br/>
	 * O mapeamento dos valores de cada linha da planilha, 
	 * contifo em @PayloadVO.getFields() ser� utilizado 
	 * pela respectiva implementa��o de @IBridge
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
