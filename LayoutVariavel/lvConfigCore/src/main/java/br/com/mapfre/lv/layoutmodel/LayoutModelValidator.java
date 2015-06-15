package br.com.mapfre.lv.layoutmodel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import org.apache.commons.collections.CollectionUtils;


public class LayoutModelValidator implements ConstraintValidator<LayoutModelConsistency, LayoutModelVO> {

	public void initialize(LayoutModelConsistency constraintAnnotation) {
	}
	
	/**
	 * 
	 */
	public boolean isValid(LayoutModelVO lm, ConstraintValidatorContext constraintContext) {
		boolean alternativeResult = true;
		constraintContext.disableDefaultConstraintViolation();
		
		if(lm.getServiceModelVO()==null){
			constraintContext.buildConstraintViolationWithTemplate("O serviço de destino não pode ser nulo!").addNode("serviceModelVO").addConstraintViolation();
			alternativeResult = false;
		}else{

			List<ServiceParamVO> serviceFields = lm.getServiceModelVO().getServiceFields();
			List<ServiceParamVO> pickedSfs = getPickedFields(lm);
			
			Collection<ServiceParamVO> sobras = CollectionUtils.subtract(serviceFields, pickedSfs);
			
			for (ServiceParamVO sobra : sobras) {
				if(notNull(lm, sobra) && sobra.getFieldRequired()){
					
					alternativeResult=false;
					
					constraintContext.buildConstraintViolationWithTemplate(
						"O campo '"+sobra.getName()+"' do serviço '"+lm.getServiceModelVO().getNome()+"' não foi escolhido! Determine qual será a coluna de origem deste dado!")
						.addNode("fields").addConstraintViolation();
				}
			}
		}
		
		return alternativeResult;
	}

	protected boolean notNull(LayoutModelVO lm, ServiceParamVO sobra) {
		return sobra!=null && lm!=null && lm.getServiceModelVO()!=null;
	}


	
	protected List<ServiceParamVO> getPickedFields(LayoutModelVO lm) {
		ServiceModelVO sm = lm.getServiceModelVO();
		
		Collection<LayoutColumnVO> sfs = lm.getFields().values();
		
		List<ServiceParamVO> pickedSfs = new ArrayList<ServiceParamVO>(sfs.size());
		for (LayoutColumnVO lf : sfs) {
			pickedSfs.add(lf.getServiceFieldVO());
		}
		return pickedSfs;
	}
	

}
