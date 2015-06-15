package br.com.mapfre.lv.layoutmodel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.Serializable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.hibernate.criterion.DetachedCriteria;
import org.hibernate.criterion.Restrictions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;


@ContextConfiguration(locations = "/ApplicationContext.xml")
@Test
public class TestLayoutModel extends AbstractTestNGSpringContextTests {

	private static final Logger log = LoggerFactory.getLogger(TestLayoutModel.class);
	
	@Autowired
	private ILayoutBusiness layoutBusiness;  

	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	/**
	 * @throws FileNotFoundException 
	 */
	@Test
	public void testLayoutModel() throws FileNotFoundException{
		
		
		LayoutModelVO layoutModelVO = getLayoutModel("Layout Para Teste");

		
		String fileName = "/home/darcio/trabalho/lv/planilhaGigante_32_simples.xlsx";
		FileInputStream fis = new FileInputStream(fileName);
		long now = System.currentTimeMillis();
		
		
		layoutBusiness.parseXLSXFile(layoutModelVO, fis, 10);
		
		System.out.println("tempo Total: "+(System.currentTimeMillis()-now));
		
		log.debug("file name  = "+layoutModelVO.getArquivo().getNome());
		log.debug("checksum   = "+layoutModelVO.getArquivo().getChecksum());
		log.debug("headerLine = "+layoutModelVO.getHeaderLine());
		
		
		Map<Integer, LayoutColumnVO> fields = layoutModelVO.getFields();
		Set<Integer> keys = fields.keySet();
		for (Integer key: keys) {
			LayoutColumnVO layoutColumnVO = fields.get(key);
			log.debug("coluna="+layoutColumnVO.getColuna()+" nome="+layoutColumnVO.getNome());
		}
		
		
		
		List<Map<Integer, CellValueVO>> values = layoutModelVO.getValues();

		StringBuffer buf = new StringBuffer();
		for (Map<Integer, CellValueVO> map : values) {
			
			Set<Integer> valKeys = map.keySet();

			for (Integer key : valKeys) {
				CellValueVO cellValueVO = map.get(key);
				LayoutColumnVO lf = fields.get(cellValueVO.getColuna());
				
				Serializable 	val 					= cellValueVO.getVal();							//valor objeto puro
				String 				valRaw 				= cellValueVO.getValRaw();					//valor bruto do xml
				String 				valFormatado 	= lf.getFormatedVal(valRaw, val);		//valor formatado para Excel
				
				Serializable 	valorParaProcssamento 		= lf.getObjectValue(valRaw, val);		//
				
				buf.
				append(" linha:"					).append(cellValueVO.getLinha()).
				append(" coluna:"					).append(cellValueVO.getColuna()).
				append(" tipo:"						).append(lf.getDataType()).

				append(" valorCru:"				).append(valRaw).
				append(" valor:"					).append(val).
				append(" vlProc:"					).append(valorParaProcssamento).
				
				append("\n");
			}
		}
		
		log.debug(buf.toString());
	}

	/**
	 * 
	 * @param nomeLayout
	 * @return
	 */
	protected LayoutModelVO getLayoutModel(String nomeLayout) {
		DetachedCriteria criteria = DetachedCriteria.forClass(LayoutModelVO.class);
		
		criteria.add(Restrictions.eq("nome", nomeLayout));

		List<LayoutModelVO> val = hibernateTemplate.findByCriteria(criteria );
		
		LayoutModelVO layoutModelVO = val.get(0);
		return layoutModelVO;
	}

	
	
}
