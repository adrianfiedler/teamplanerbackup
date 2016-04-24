package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class VereinAdminREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -6883081460328313281L;
	public String name;
	public String id;
	public List<TeamListREST> teams;
	public int gekaufteTeams;
}
