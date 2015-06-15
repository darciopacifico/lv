package br.com.mapfre.lv.vidateste;

import java.io.Serializable;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;


public interface VidaDAO {

	VidaVO save(VidaVO vidaVO);

	int[] save(Map<String, Serializable> params, List<VidaVO> vidaVOs) throws SQLException;

	
	
}
