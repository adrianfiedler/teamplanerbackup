package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VereinREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6883081460328313281L;
	public String name;
	public String id;
	public List<TeamREST> teams;
	public int gekaufteTeams;
}
