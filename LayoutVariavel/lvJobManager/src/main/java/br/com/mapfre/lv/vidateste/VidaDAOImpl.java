package br.com.mapfre.lv.vidateste;

import java.io.Serializable;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * DAO dummy para teste
 * 
 * @author darcio
 *
 */
@Component
public class VidaDAOImpl implements VidaDAO {
	private static final Logger log = LoggerFactory.getLogger(VidaDAOImpl.class);
	
	private static final int NOT_PROCESSED = 0;
	
	@Autowired
	@Qualifier("vidaDS")
	private DataSource dataSource;
	
	/**
	 * dummy save 
	 */
	@Override
	public VidaVO save(VidaVO vidaVO){
		return vidaVO;
	}
	
	
	/**
	 * dummy save
	 */
	@Override
	public int[] save(Map<String, Serializable> params, List<VidaVO> vidaVOs) throws SQLException{

		/*
		try {
			Thread.sleep(3000);
		} catch (InterruptedException e1) {
			e1.printStackTrace();
		}
		*/
		
		int[] listResults;
		int[] results=null;
		Connection connection=null;
		PreparedStatement pst=null;
		
		List<Integer> rejectedLines = new ArrayList<Integer>();
		
			
		connection = dataSource.getConnection();
		
		//RECOMENDAÇÃO É QUE SEJA SUSPENSO O AUTOCOMMIT DA CONEXAO
		boolean autoCommitOriginal = connection.getAutoCommit();
		connection.setAutoCommit(false);

		try {
			
			/*
			String insert = "insert into VidaVO (apelido, bairro, cidade, endereco, nome, sobrenome, telefone,field0,field1,field2,field3," +
					"field4,field5,field6,field7,field8,field9,field10,field11,field12,field13,field14,field15,field16,field17,field18," +
					"field19,field20,field21,field22,field23,field24,field25,field26,field27,field28,field29,field30) values " +
					"(?, ?, ?, ?, ?, ?, ?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
			*/

			String insert = "insert into VidaVO (dtInclusao, dtNasc, nome, sobrenome) values (?, ?, ?, ?)";

			
			pst = connection.prepareStatement(insert);

			Iterator<VidaVO> itVidas = vidaVOs.iterator();
			
			
			
			for (VidaVO vidaVO : vidaVOs) {
				try{
					setInsertParameters(pst, vidaVO);
					pst.addBatch();
				}catch(Exception e){
					int linha = vidaVOs.indexOf(vidaVO);
					rejectedLines.add(linha);
					log.error("A linha {} foi condenada! Não foi possivel salvar Vida {}. Msg: {}: {}",new Object[]{linha, vidaVO, e, e.getMessage()});
				}
			}
			
			results = pst.executeBatch();
			
			listResults = commitResults(results, rejectedLines, connection, autoCommitOriginal);
		
		}catch(BatchUpdateException e){
			//em caso de batchUpdate, ainda é possível tentar recuperar as linhas que foram executadas
			listResults = commitResults(e.getUpdateCounts(), rejectedLines, connection, autoCommitOriginal);
			
			throw e;

		}finally{
			closeJdbcStuffs(connection, pst);
		}
		
		return listResults;
	}


	protected int[] commitResults(int[] results, List<Integer> rejectedLines, Connection connection, boolean autoCommitOriginal) throws SQLException {
		List<Integer> listResults;
		listResults = new ArrayList<Integer>(results.length);
		
		for (Integer result : results) {
			listResults.add(result);
		}
		
		for (Integer rejectedLine : rejectedLines) {
			listResults.add(rejectedLine, NOT_PROCESSED);
		}
		
		
		connection.commit();
		
		//RECOMENDAÇÃO É QUE SEJA SUSPENSO O AUTOCOMMIT DA CONEXAO
		connection.setAutoCommit(autoCommitOriginal);
		
		
		int[] newResults = new int[listResults.size()];
		
		int i=0;
		for (Integer result : listResults) {
			newResults[i++] = result;
		}
		
		return newResults;
	}


	protected void closeJdbcStuffs(Connection connection, PreparedStatement pst) {
		try {
			connection.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			pst.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}


	/**
	 * Seta parâmetros do insert no preparedStatement.
	 * @param pst
	 * @param vidaVO
	 * @throws SQLException
	 */
	private void setInsertParameters(PreparedStatement pst, VidaVO vidaVO) throws SQLException {
		pst.setDate(1, new Date(vidaVO.getDtInclusao().getTime()));
		pst.setDate(2, new Date(vidaVO.getDtNasc().getTime()));
		
		pst.setString(3, vidaVO.getNome		());
		pst.setString(4, vidaVO.getSobrenome());
		
//		pst.setBoolean(5, vidaVO.getFuma());
		
		
		
		/*
		pst.setString(3, vidaVO.getEndereco ());
		pst.setString(4, vidaVO.getTelefone ());
		pst.setString(5, vidaVO.getBairro   ());
		pst.setString(6, vidaVO.getCidade   ());
		pst.setString(7, vidaVO.getApelido  ());
		pst.setString(7, vidaVO.getField0()  );
		pst.setString(8, vidaVO.getField1()  );
		pst.setString(9, vidaVO.getField2()  );
		pst.setString(10, vidaVO.getField3()  );
		pst.setString(11, vidaVO.getField4()  );
		pst.setString(12, vidaVO.getField5()  );
		pst.setString(13, vidaVO.getField6()  );
		pst.setString(14, vidaVO.getField7()  );
		pst.setString(15, vidaVO.getField8()  );
		pst.setString(16, vidaVO.getField9()  );
		pst.setString(17, vidaVO.getField10()  );
		pst.setString(18, vidaVO.getField11()  );
		pst.setString(19, vidaVO.getField12()  );
		pst.setString(20, vidaVO.getField13()  );
		pst.setString(21, vidaVO.getField14()  );
		pst.setString(22, vidaVO.getField15()  );
		pst.setString(23, vidaVO.getField16()  );
		pst.setString(24, vidaVO.getField17()  );
		pst.setString(25, vidaVO.getField18()  );
		pst.setString(26, vidaVO.getField19()  );
		pst.setString(27, vidaVO.getField20()  );
		pst.setString(28, vidaVO.getField21()  );
		pst.setString(29, vidaVO.getField22()  );
		pst.setString(30, vidaVO.getField23()  );
		pst.setString(31, vidaVO.getField24()  );
		pst.setString(32, vidaVO.getField25()  );
		pst.setString(33, vidaVO.getField26()  );
		pst.setString(34, vidaVO.getField27()  );
		pst.setString(35, vidaVO.getField28()  );
		pst.setString(36, vidaVO.getField29()  );
		pst.setString(37, vidaVO.getField30()  );
		pst.setString(38, vidaVO.getField30()  );
		*/
	}
	
	
	
}
