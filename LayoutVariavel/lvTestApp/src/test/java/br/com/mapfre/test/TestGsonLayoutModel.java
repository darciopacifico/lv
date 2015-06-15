
package br.com.mapfre.test;

import java.util.List;

import org.hibernate.criterion.DetachedCriteria;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.HibernateTemplate;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.annotations.Test;

import br.com.mapfre.lv.converter.ClassTypeAdapter;
import br.com.mapfre.lv.layoutmodel.LayoutModelVO;
import br.com.mapfre.lv.layoutmodel.ServiceModelVO;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

@ContextConfiguration(locations = "/ApplicationContextSample.xml")
@Test
public class TestGsonLayoutModel extends AbstractTestNGSpringContextTests {

	@Autowired
	private HibernateTemplate hibernateTemplate;
	
	@Test
	public void testLayoutModel(){  
		
		
		DetachedCriteria criteria = DetachedCriteria.forClass(LayoutModelVO.class);
		List<LayoutModelVO> list = hibernateTemplate.findByCriteria(criteria );

		LayoutModelVO lm = list.get(0);
		
		
		GsonBuilder gsonBuilder = new GsonBuilder();

		gsonBuilder.registerTypeHierarchyAdapter(Class.class , new ClassTypeAdapter());
		
		Gson gson = gsonBuilder.create();

		ServiceModelVO sm = lm.getServiceModelVO();

		lm.setServiceModelVO(null);

		lm = new LayoutModelVO();
		
		String strJson = gson.toJson(lm);
		lm.setServiceModelVO(sm);
		
		System.out.println(strJson);
		
	}

}
