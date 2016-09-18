package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.as.quickstarts.kitchensink.interfaces.DisplayNameInterface;

@XmlRootElement
public class TrainerZusageREST implements Serializable, DisplayNameInterface {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3026045626693493111L;

	public int status;

	public String kommentar;
	
	public String rolle;
	
	public String displayName;
	
	public boolean autoSet;

	@Override
	public String getDisplayName() {
		return displayName;
	}
	
}
