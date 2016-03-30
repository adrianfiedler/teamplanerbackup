package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TrainerZusageREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3026045626693493111L;

	public int status;

	public String kommentar;
	
	public String rolle;
	
	public String displayName;
	
}
