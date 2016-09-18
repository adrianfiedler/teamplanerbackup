package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.as.quickstarts.kitchensink.interfaces.DisplayNameInterface;

@XmlRootElement
public class SpielerZusageREST implements Serializable, DisplayNameInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7057969049453938146L;

	public int status;

	public String kommentar;
	
	public String rolle;
	
	public String displayName;
	
	public boolean autoSet;
	
	@Override
	public String getDisplayName(){
		return displayName;
	}
	
}
