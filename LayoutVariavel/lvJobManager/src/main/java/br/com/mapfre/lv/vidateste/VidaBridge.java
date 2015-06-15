package br.com.mapfre.lv.vidateste;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import br.com.mapfre.lv.connector.AbstractBridge;
import br.com.mapfre.lv.connector.IBridge;
import br.com.mapfre.lv.executor.IProcessMonitor;
import br.com.mapfre.lv.payload.LotVO;
import br.com.mapfre.lv.payload.PayloadVO;

/**
 * Bridge para acionamento do componente VidaDAO, para realizar a inserção do registro
 * @author darcio
 *
 */
@Component("vidaBridge")
public class VidaBridge extends AbstractBridge implements IBridge {
		
	@Autowired
	private VidaDAO vidaDAO;
	

	@Autowired
	@Override
	@Qualifier("persistentProcessMonitor")
	public void setProcessMonitor(IProcessMonitor processMonitor) {
		// TODO Auto-generated method stub
		super.setProcessMonitor(processMonitor);
	}
	
	
	/**
	 * Constroi bridge para acionamento do compoente VidaDAO
	 * @param processMonitor
	 */
	public VidaBridge(){
	}
	
	/**
	 * Aciona componente DAO para isert da vida
	 */
	@Override
	public int[] execute(Map<String, Serializable> params, LotVO lotVO) throws Exception {

		List<PayloadVO> payloadVOs = lotVO.getPayloadVOs();
		
		List<VidaVO> vidas = new ArrayList<VidaVO>(payloadVOs.size());
		
		for (PayloadVO payloadVO : payloadVOs) {
			VidaVO vidaVO = compoeVidaVO(payloadVO);

			vidas.add(vidaVO);
		}
		int[] results = vidaDAO.save(params,vidas);
		
		return results;
		
	}

	
	
	/**
	 * Compoe Vo com dados lidos do arquivo original
	 * @param payloadVO
	 * @return
	 */
	private VidaVO compoeVidaVO(PayloadVO payloadVO) {
		VidaVO vidaVO = new VidaVO();
		
		/*
		addFieldTest(new LayoutColumnVO(), i++, "nome", 				10d, 9d, null, true, String.class);
		addFieldTest(new LayoutColumnVO(), i++, "sobrenome", 	20d, 1d, null, true, String.class);
		addFieldTest(new LayoutColumnVO(), i++, "dtNasc", 			10d, 9d, null, true, Date.class);
		addFieldTest(new LayoutColumnVO(), i++, "dtInclusao", 	10d, 9d, null, true, Date.class);
		 */
		
		vidaVO.setNome(payloadVO.getFields().get("nome")+"");
		vidaVO.setSobrenome(payloadVO.getFields().get("sobrenome")+"");
		vidaVO.setDtNasc((Date)payloadVO.getFields().get("dtNasc"));
		vidaVO.setDtInclusao((Date)payloadVO.getFields().get("dtInclusao"));
		 

	 /*
	 vidaVO.setField0(payloadVO.getFields().get("Field1")+"");
		 vidaVO.setField1(payloadVO.getFields().get("Field1")+"");
		 vidaVO.setField2(payloadVO.getFields().get("Field2")+"");
		 vidaVO.setField3(payloadVO.getFields().get("Field3")+"");
		 vidaVO.setField4(payloadVO.getFields().get("Field4")+"");
		 vidaVO.setField5(payloadVO.getFields().get("Field5")+"");
		 vidaVO.setField6(payloadVO.getFields().get("Field6")+"");
		 vidaVO.setField7(payloadVO.getFields().get("Field7")+"");
		 vidaVO.setField8(payloadVO.getFields().get("Field8")+"");
		 vidaVO.setField9(payloadVO.getFields().get("Field9")+"");
		vidaVO.setField10(payloadVO.getFields().get("Field10")+"");
		vidaVO.setField11(payloadVO.getFields().get("Field11")+"");
		vidaVO.setField12(payloadVO.getFields().get("Field12")+"");
		vidaVO.setField13(payloadVO.getFields().get("Field13")+"");
		vidaVO.setField14(payloadVO.getFields().get("Field14")+"");
		vidaVO.setField15(payloadVO.getFields().get("Field15")+"");
		vidaVO.setField16(payloadVO.getFields().get("Field16")+"");
		vidaVO.setField17(payloadVO.getFields().get("Field17")+"");
		vidaVO.setField18(payloadVO.getFields().get("Field18")+"");
		vidaVO.setField19(payloadVO.getFields().get("Field19")+"");
		vidaVO.setField20(payloadVO.getFields().get("Field20")+"");
		vidaVO.setField21(payloadVO.getFields().get("Field21")+"");
		vidaVO.setField22(payloadVO.getFields().get("Field22")+"");
		vidaVO.setField23(payloadVO.getFields().get("Field23")+"");
		vidaVO.setField24(payloadVO.getFields().get("Field24")+"");
		vidaVO.setField25(payloadVO.getFields().get("Field25")+"");
		vidaVO.setField26(payloadVO.getFields().get("Field26")+"");
		vidaVO.setField27(payloadVO.getFields().get("Field27")+"");
		vidaVO.setField28(payloadVO.getFields().get("Field28")+"");
		vidaVO.setField29(payloadVO.getFields().get("Field29")+"");
		vidaVO.setField30(payloadVO.getFields().get("Field30")+"");
		*/
		return vidaVO;
	}

}
