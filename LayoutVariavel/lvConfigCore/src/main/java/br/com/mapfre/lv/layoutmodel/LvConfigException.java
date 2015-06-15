/**
 * 
 */
package br.com.mapfre.lv.layoutmodel;

import java.util.Set;

import javax.ejb.ApplicationException;
import javax.validation.ConstraintViolation;

import br.com.mapfre.lv.LVException;

/**
 *
 * @author darcio
 */
@ApplicationException(rollback=true)
public class LvConfigException extends LVException {
	private static final long serialVersionUID = -5558827670639035961L;
	private Set<ConstraintViolation<LayoutModelVO>> faults;

	/**
	 * @param message
	 * @param cause
	 */
	public LvConfigException(String message, Throwable cause) {
		super(message, cause);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param message
	 */
	public LvConfigException(String message) {
		super(message);
		// TODO Auto-generated constructor stub
	}

	/**
	 * @param cause
	 */
	public LvConfigException(Throwable cause) {
		super(cause);
		// TODO Auto-generated constructor stub
	}

	public LvConfigException(String message, Set<ConstraintViolation<LayoutModelVO>> faults) {
		super(message);
		this.faults = faults;
	}

	public Set<ConstraintViolation<LayoutModelVO>> getFaults() {
		return faults;
	}

	public void setFaults(Set<ConstraintViolation<LayoutModelVO>> faults) {
		this.faults = faults;
	}

}
