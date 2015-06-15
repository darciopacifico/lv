package br.com.mapfre.lv.mapreduce;

import java.io.File;

import net.sf.flatpack.DataSet;
import net.sf.flatpack.Parser;
import net.sf.flatpack.brparse.BuffReaderParseFactory;
import br.com.mapfre.lv.payload.LotVO;
import br.com.mapfre.lv.payload.PayloadVO;


/**
 * Implementação de IMapper especializada em ler arquivos TXT
 * @author darcio
 *
 */
public class FlatpackTXTMapper implements IMapper {

	private DataSet ds;
	private String[] columns;
	private Integer lotSize;
	
	
	/**
	 * Constroi um IMapper apontando para um arquivo TXT com um layout especificado. Cada PayloadLotVO terá a quantidade de registros especificados em lotSize
	 * @param values
	 * @param layout
	 * @param lotSize
	 */
	public FlatpackTXTMapper(File layout, File values, Integer lotSize) {
		Parser parser = BuffReaderParseFactory.getInstance().newFixedLengthParser(layout,values);
		this.ds = parser.parse();
		columns = ds.getColumns();
		this.lotSize = lotSize;
	}

	
	/**
	 * Posiciona no proximo registro, retornando true. Se não há mais registros retorna false.
	 */
	@Override
	public boolean next() {
		return ds.next();
	}

	
	/**
	 * Cria um lote de registros, de acordo com a quantidade estabelecida em lotSize.
	 */
	@Override
	public LotVO nextPayload() {
		
		LotVO payloadLotVO = new LotVO(lotSize);

		Integer lastRow=ds.getRowNo();
		payloadLotVO.setFromLine(lastRow);
		Integer i=0;
		do{
			
			PayloadVO payloadVO =  new PayloadVO(); 
			
			for (String column : columns) {
				String val = ds.getString(column);
				payloadVO.getFields().put(column, val);
			}
			
			payloadLotVO.getPayloadVOs().add(payloadVO);
			
			lastRow=ds.getRowNo();
			i++;
		}while(i<lotSize && ds.next());
		
		payloadLotVO.setToLine(lastRow);
		
		
		return payloadLotVO;
	}

}
