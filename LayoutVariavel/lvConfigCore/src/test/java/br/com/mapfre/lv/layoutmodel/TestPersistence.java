package br.com.mapfre.lv.layoutmodel;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.xml.ws.FaultAction;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

@ContextConfiguration(locations = "/ApplicationContext.xml")
@Test
public class TestPersistence extends AbstractTestNGSpringContextTests {

	private static final Logger log = LoggerFactory.getLogger(TestPersistence.class);

	@Autowired
	private ILayoutBusiness layoutBusiness;

	/**
	 * @throws FileNotFoundException
	 * 
	 */
	@Test
	public void testSave() throws FileNotFoundException {

		String fileName = "planilhaGigante_55.xlsx";
		InputStream fis = new FileInputStream(fileName);
		Integer linhas = 10;
		
		LayoutModelVO lm = new LayoutModelVO();
		lm.getArquivo().setNome(fileName);
		
		layoutBusiness.parseXLSXFile(lm, fis, linhas);
		
		lm.setNome("Layout Cotizador para Corretora XYZ!");
		
		ServiceModelVO sm = layoutBusiness.getServiceModels().get(1);

		layoutBusiness.saveOrUpdate(sm);

		int qtdCampos = sm.getServiceFields().size();

		
		for (int index = 0; index < qtdCampos; index++) {
			LayoutColumnVO layoutColumnVO = lm.getFields().get(index);
			ServiceParamVO serviceParamVO = sm.getServiceFields().get(index);

			if (layoutColumnVO != null && serviceParamVO != null ) {
				layoutColumnVO.setServiceFieldVO(serviceParamVO);
			}
		}

		lm.setServiceModelVO(sm);
		
		try {
			layoutBusiness.saveOrUpdate(lm);
		} catch (LvConfigException e) {
			Set<ConstraintViolation<LayoutModelVO>> faults = e.getFaults();
			for (ConstraintViolation<LayoutModelVO> constraintViolation : faults) {
				log.warn( constraintViolation.getMessage());
			}
		}

	}

}
