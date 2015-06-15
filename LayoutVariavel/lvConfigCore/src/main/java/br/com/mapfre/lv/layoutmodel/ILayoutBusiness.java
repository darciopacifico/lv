package br.com.mapfre.lv.layoutmodel;

import java.io.InputStream;
import java.io.Serializable;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import javax.validation.ConstraintViolation;

import org.springframework.stereotype.Component;

/**
 * Implementa funcionalidades de negócio manutenção de layouts
 *
 * @author darcio
 */
@Component
public interface ILayoutBusiness extends Serializable {

	void parseXLSXFile(LayoutModelVO layoutModelVO, InputStream fis, Integer maxLinhas);
	//void parseXLSXFile(LayoutModelVO layoutModelVO, String fileName, InputStream fis, Integer linhas);
	
	List<ServiceModelVO> getServiceModels();

	void saveOrUpdate(LayoutModelVO layoutModelVO) throws LvConfigException;

	void saveOrUpdate(ServiceModelVO sm);

	Set<ConstraintViolation<LayoutModelVO>> validateLayoutModel(LayoutModelVO lm);

	List<LayoutModelVO> findLayoutModels(String nome, ServiceModelVO serviceModelPesquisa);

	void excluir(LayoutModelVO layoutModelVO);

	void excluir(Collection<LayoutColumnVO> fields);

	
}
