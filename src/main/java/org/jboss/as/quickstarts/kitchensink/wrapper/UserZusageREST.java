package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserZusageREST implements Serializable {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 7625447186569925490L;

	public int status;

	public String kommentar;
	
	public String rolle;
	
	public String displayName;
	
	public boolean autoSet;
	
}	
