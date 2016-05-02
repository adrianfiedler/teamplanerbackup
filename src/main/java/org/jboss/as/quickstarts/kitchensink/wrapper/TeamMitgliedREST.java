package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TeamMitgliedREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -5505378457327236679L;
	public UserREST user;
	public String rolle;
}
