package br.com.mapfre.lv.layoutmodel;

import java.io.Serializable;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Transient;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.HashCodeBuilder;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import br.com.mapfre.lv.LVException;
import br.com.mapfre.lv.parser.EnumDataType;

/**
 * Definicao de um campo de um @LayoutModelVO
 * 
 * Mapeia qual será o campo de destino de um serviço, um pra um. Contém as regras e algoritmos de formatação e conversão de dados. 
 * 
 * @author darcio
 */
@Entity
public class LayoutColumnVO implements Serializable {
	private static final long serialVersionUID = 5758704947708941833L;
	private static final Logger log = LoggerFactory.getLogger(LayoutColumnVO.class);
	private LayoutModelVO layoutModelVO;
	
	private Integer PK;
	private String nome;
	private Integer coluna;
	
	/**
	 * Tipo de dado 
	 */
	private EnumDataType dataType;
	
	/**
	 * Formato original do excel
	 */
	private Integer formatIndex;

	/**
	 * Formato original do excel
	 */
	private String formatString;
	
	/**
	 * Mapeia qual será o @ServiceParamVO de destino.
	 */
	private ServiceParamVO serviceParamVO;
	
	/**
	 * Determina qual é a formatação para o destino, no caso de haver necessidade de parsear dados. ex: String "20140101" para um java.util.Date
	 */
	private String formatoDestino;
	
	/**
	 * Em conjunto com a String de formatacao, determina qual é o separador de milhar de um número representado por String
	 */
	private Character separadorMilhar = '.';
	
	/**
	 * Em conjunto com a String de formatacao, determina qual é o separador de decimais de um número representado por String
	 */
	private Character separadorDecimal = ',';

	/**
	 * Cache da implementação de format. Deve ser destruido (=null) se o formatodestino ou separadores forem alterados 
	 */
	private Format format;

	/**
	 * Formatador do POI, capaz de transformar uma Double na String representativa do dado.
	 */
	private DataFormatter formatter = new DataFormatter();

	
		
	/**
	 * Testa se o valor a ser formatado não é nulo e se o tipo de dado referente a esta coluna é Number ou Date, que obrigatoriamente contem um formato de saída no excel.
	 * @param valRaw
	 * @return
	 */
	protected boolean isFormatabble(String valRaw) {
		return valRaw != null && (EnumDataType.NUMBER.equals(dataType) || EnumDataType.DATE.equals(dataType));
	}


	/**
	 * Transforma um valor formatado no excel para o valor esperado definido por uma formatacao do usuário
	 *  
	 * @param valInExcelFormat valor formatado como no excel
	 * @return
	 */
	public Serializable getObjectValue(String valInExcelFormat) {

		Serializable parsedVal = valInExcelFormat;//saída padrão

		if (possuiServiceFieldMapeado()) {//Testa se o layoutfield referente à coluna desta celula possui serviceField de destino mapeado

			Class<?> tipoDestino = getServiceFieldVO().getFieldType();
			
			if (tipoDestino.isAssignableFrom(Boolean.class)){
				//se for um double
				parsedVal = Boolean.parseBoolean(valInExcelFormat);
				
			} else if (possuiFormatoDestino()) {// Caso possuia um formatador para o destino. Normalmente de String para Date ou Number
				parsedVal = parseStringVal(valInExcelFormat, tipoDestino);
			}
		}
		return parsedVal;
	}

	/**
	 * Retorna o valor da célula como deveria ser exibido  
	 * 
	 * @param valRaw Valor bruto da célula, lido como string do elemento do XML de valor da planilha
	 * @param val valor objeto da célula. Interpretado como 
	 * 
	 * @return Valor formatado como seria visualizado no excel
	 */
	@Transient
	public String getFormatedVal(String valRaw, Serializable val) {

		String formatedVal = val + "";//saída padrão
		
		if (isFormatabble(valRaw)) {// se existem regras de formatacao definidas em excel
			try {
				
				formatedVal = formatter.formatRawCellContents(Double.parseDouble(valRaw), this.formatIndex, this.formatString==null?"":this.formatString);
				
			} catch (Exception e) {
				log.error("Erro ao tentar formatar coluna:" + this.coluna+ ", valor original: "+valRaw, e);
			}
		}
		
		//formatedVal = getObjectValue(formatedVal)+"";
		
		return formatedVal;
	}



	/**
	 * Recupera o valor final a ser considerado da leitura de uma celula, 
	 * já aplicando regras de conversão, caso sejam necessárias e estejam definidas.
	 * 
	 * @param valRaw valor string bruto lido da planilha 
	 * (de um elemento "v" do XML referente à sheet da planilha, na verdade);
	 * 
	 * @param val valor lido conforme definido pelo tipo de dado da coluna. 
	 * (Ex: para uma coluna do tipo data, este objeto será um java.util.Date, 
	 * para um número com várias casas decimais, um java.lang.Double)
	 * 
	 * @return Valor a ser considerado para efeito de processamento da linha
	 */
	public Serializable getObjectValue(String valRaw, Serializable val) {
		
		Serializable 	valorFinal; 		
		
		if(isFormacaoObrigatoria()){
			//caso seja obrigatorio a especificacao de uma formatacao de destino. Ex: converter um string "20140101" para java.util.Date. 
			//aplica regras de parseamento do dado, especificadas em layout
			String valFormatado = getFormatedVal(valRaw, val);	//valor formatado exatamente como visto no excel
			valorFinal 		= getObjectValue(valFormatado);				//valor parseado a partir de uma regra de formatacao definida em layout
			
		}else if(isServiceFieldString() && isNotStringTableValue()){
			// Caso não seja formatacao obrigatoria, o valor esperado seja string e o tipo de campo não seja SSTINDEX (tabela de strings), leva a representação do valor como string, exatamente como apresentada no excel.
			// no modelo SSTINDEX, ou tabela de strings, o valor valRaw (valor cru), é um índice de uma tabela de Strings, e o não o valor propriamente. Neste caso, o valor da String estará em val, caindo no else abaixo;
			//Ex: se a coluna for numerica, formatada como currency: R$500,50, o valor lido será a literal String "R$500,50". 
			valorFinal = valRaw;
			
		}else{
			// Caso não seja formatacao obrigatoria e o valor não seja string, leva o valor interpretado da coluna excel
			//Ex: se a coluna for numerica, formatada como currency: R$500,50, o valor lido será um double 500.50d 
			valorFinal = val;
		}

		return valorFinal;
	}


	
	/**
	 * Testa se o valor de origem vem de um modelo de tabela de strings do excel. 
	 * Neste modelo, o valor armazenado no elemento "v" do XML na verdade não é a literal esperada, mas o índice de uma tabela de strings do excel.
	 * Este modelo implementado pela Microsoft ajuda a economizar com o armazenamento de literais repetidas, que é bastante comum em planilhas.
	 * @return
	 */
	@Transient
	protected boolean isNotStringTableValue() {
		return !( EnumDataType.SSTINDEX.equals(this.dataType));
	}

	
	/**
	 * Testa se o tipo requerido para o serviço é String
	 * @return
	 */
	@Transient
	protected boolean isServiceFieldString() {
		return String.class.equals(this.getServiceFieldVO().getFieldType());
	}

	
	/**
	 * Parseia a String de valor para o formato de destino. Exemplo: uma string contendo "20/11/1980" para um objeto Date equivalente, ou uma String "R$9,90" para um Double equivalente
	 * 
	 * @param formatedVal
	 * @param tipoDestino
	 * @return
	 */
	protected Serializable parseStringVal(String formatedVal, Class<?> tipoDestino) {

		Format format;
		try {
			format = getFormat();
		} catch (LVException e1) {
			formatedVal = formatedVal + "(Err: "+e1.getMessage()+")";
			return formatedVal;
		}
		
		Serializable parsedVal = formatedVal;

		if(format!=null){
			
			try {
				parsedVal = (Serializable)format.parseObject(formatedVal);
			} catch (ParseException e) {
				formatedVal = formatedVal + " (Erro ao formatar)";
				if(log.isDebugEnabled()){
					log.debug("Erro ao tentar formatar valor",e);
				}
				return formatedVal;
			}
			
			parsedVal = typeNumber(tipoDestino, parsedVal);
			
		}else{
			
			log.warn("Não foi possivel identificar o formatador para o campo");
		}
		
		return parsedVal;
	}


	/**
	 * Converte o valor parseado para o tipo numerico de destino correto
	 * 
	 * @param tipoDestino
	 * @param parsedVal
	 * @return
	 */
	protected Serializable typeNumber(Class<?> tipoDestino, Serializable parsedVal) {
		if (tipoDestino.isAssignableFrom(Byte.class)) {
			parsedVal = ((Number)parsedVal).byteValue();
		} else if (tipoDestino.isAssignableFrom(Short.class)) {
			parsedVal = ((Number)parsedVal).shortValue();
		} else if (tipoDestino.isAssignableFrom(Integer.class)) {
			parsedVal = ((Number)parsedVal).intValue();
		} else if (tipoDestino.isAssignableFrom(Long.class)) {
			parsedVal = ((Number)parsedVal).longValue();
		} else if (tipoDestino.isAssignableFrom(Float.class)) {
			parsedVal = ((Number)parsedVal).floatValue();
		} else if (tipoDestino.isAssignableFrom(Double.class)) {
			parsedVal = ((Number)parsedVal).doubleValue();
		} else if (tipoDestino.isAssignableFrom(Number.class)) {
			parsedVal = (Number)parsedVal;
		}
		return parsedVal;
	}

	
	
	/**
	 * Recupera o formatador para o campo de saída
	 * @return
	 * @throws LVException 
	 */
	@Transient
	public Format getFormat() throws LVException{

		
		
		if(this.format==null && isFormacaoObrigatoria() && StringUtils.isNotBlank(getFormatoDestino()) ){
			
			Class<?> destType = serviceParamVO.getFieldType();
			
			if(destType.isAssignableFrom(Date.class)){
				
				try{
					this.format = new SimpleDateFormat(this.formatoDestino);
				}catch(IllegalArgumentException e){
					if(log.isDebugEnabled()){
						log.debug("Erro ao tentar recuperar formatador!",e);
					}
					throw new LVException("Erro ao tentar recuperar formatador!",e);
					
				}
				
			}else if(destType.isAssignableFrom(Number.class)){
				
				if(this.separadorDecimal!=null || this.separadorMilhar!=null){
					DecimalFormatSymbols symbols = new DecimalFormatSymbols();
					
					if(this.separadorDecimal!=null){
						symbols.setDecimalSeparator(this.separadorDecimal);
					}
					
					if(this.separadorMilhar!=null){
						symbols.setGroupingSeparator(this.separadorMilhar);
					}
					
					this.format = new DecimalFormat(this.formatoDestino,symbols);
					
				}else{
					this.format = new DecimalFormat(this.formatoDestino);
				}
			}
		}
		
		return format;
	}
	

	/**
	 * Testa se o layoutfield referente à coluna desta celula possui serviceField de destino mapeado 
	 * @return
	 */
	@Transient
	public boolean possuiServiceFieldMapeado() {

		boolean possui = this.getServiceFieldVO()!=null;
		
		return possui;
	}
	


	/**
	 * Testa se a formatação de destino é obrigatória e foi definida pelo usuário.  
	 * @return
	 */
	@Transient
	public boolean possuiFormatoDestino() {
		boolean possui = isFormacaoObrigatoria() && StringUtils.isNotBlank(getFormatoDestino());
		
		return possui;
	}
	
	/**
	 * Testa se será necessário especificar formato de conversão.
	 * Para conversões de string para number, date ou boolean, é necessário especificar o formato para parsing.
	 * @return
	 */
	@Transient
	public Boolean isFormacaoObrigatoria(){
		
		Boolean formatoObrigatorio =	
				
			possuiServiceFieldMapeado()
			
			&&
				
			/*tipos de origem*/
			(EnumDataType.SSTINDEX.equals(dataType) || 
			 EnumDataType.INLINESTR.equals(dataType)||
			 EnumDataType.NUMBER.equals(dataType)) 
			
			 //proteger de npe
			 && serviceParamVO !=null &&	serviceParamVO.getFieldType()!=null	&&
			
			/*tipos de destino*/
			(serviceParamVO.getFieldType().isAssignableFrom(Number.class) ||
			 serviceParamVO.getFieldType().isAssignableFrom(Date.class) ||
			 serviceParamVO.getFieldType().isAssignableFrom(Boolean.class));
			
		return formatoObrigatorio;
	}
	
	
	public String getFormatoDestino() {
		return formatoDestino;
	}

	public void setFormatoDestino(String formatoDestino) {
		this.format = null;
		this.formatoDestino = formatoDestino;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)	
	public Integer getPK() {
		return PK;
	}

	@ManyToOne
	@JoinColumn
	public ServiceParamVO getServiceFieldVO() {
		return serviceParamVO;
	}

	/**
	 * Determina se este campo está em duplicidade com outro campo do mesmo layoutModel
	 * @return
	 */
	@Transient
	public Boolean getDuplicated(){
		 
		boolean dup = false;
		
		if(this.layoutModelVO!=null && this.serviceParamVO!=null){
			 Collection<LayoutColumnVO> fields = this.layoutModelVO.getFields().values();
			 for (LayoutColumnVO layoutColumnVO : fields) {
				 ServiceParamVO sf = layoutColumnVO.getServiceFieldVO();
				 
				 if(sf!=null && sf.equals(this.serviceParamVO) && !this.equals(layoutColumnVO)){
					 dup=true;
					 break;//nao analisar mais nada
				 }
			 }
		 }
		 return dup;
	}
	
	/**
	 * Testa se o tipo de dado é data com informação de hora/minuto/segundo
	 * @return
	 */
	@Transient
	public Boolean isDateTime(){
		
		Boolean isDateTime = EnumDataType.DATE.equals(this.dataType)  && this.formatString!=null && this.formatString.matches(".*H.*");
		
		return isDateTime;
		
	}
	
	
	@ManyToOne
	public LayoutModelVO getLayoutModelVO() {
		return layoutModelVO;
	}
	
	
	@Enumerated(EnumType.STRING)
	public EnumDataType getDataType() {
		return dataType;
	}

	public String getNome() {
		return nome;
	}
	
	public Integer getColuna() {
		return coluna;
	}

	public Integer getFormatIndex() {
		return formatIndex;
	}

	public String getFormatString() {
		return formatString;
	}

	
	public void setFormatIndex(Integer formatIndex) {
		this.formatIndex = formatIndex;
	}

	public void setFormatString(String formatString) {
		this.formatString = formatString;
	}
	public void setColuna(Integer coluna) {
		this.coluna = coluna;
	}
	
	public void setNome(String name) {
		this.nome = name;
	}
	
	public void setPK(Integer pK) {
		PK = pK;
	}


	public void setLayoutModelVO(LayoutModelVO layoutModelVO) {
		this.layoutModelVO = layoutModelVO;
	}
	

	public void setDataType(EnumDataType dataType) {
		this.format = null;
		this.dataType = dataType;
	}

	public void setServiceFieldVO(ServiceParamVO serviceParamVO) {
		this.format=null;
		this.serviceParamVO = serviceParamVO;
	}
	
	@Override
	public boolean equals(Object obj) {
		
		if(this==obj){
			return true;
		}
		
		if(obj==null || !(obj instanceof LayoutColumnVO) || this.PK==null){
			return false;
		}
		
		LayoutColumnVO lf = (LayoutColumnVO) obj;
		
		return this.PK.equals(lf.getPK()); 
	}
	
	
	
	@Override
	public int hashCode() {
		return new HashCodeBuilder().append(this.PK).hashCode();
	}


	public Character getSeparadorMilhar() {
		return separadorMilhar;
	}


	public Character getSeparadorDecimal() {
		return separadorDecimal;
	}


	public void setSeparadorMilhar(Character separadorMilhar) {
		this.format = null;
		this.separadorMilhar = separadorMilhar;
	}


	public void setSeparadorDecimal(Character separadorDecimal) {
		this.format = null;
		this.separadorDecimal = separadorDecimal;
	}

}
