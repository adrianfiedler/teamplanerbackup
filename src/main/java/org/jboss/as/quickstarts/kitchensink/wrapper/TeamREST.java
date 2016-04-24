package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TeamREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3435028137000808894L;
	public String name;
	public String id;
	public String userRolle;
	public int spielerAnzahl;
}
