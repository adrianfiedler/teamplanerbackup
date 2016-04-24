package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TeamListREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1530550907070350936L;
	public String name;
	public String id;
	public List<TeamMitgliedREST> mitglieder;
	public TeamMitgliedREST user;
	public List<EinladungREST> einladungen;
	public int spielerAnzahl;
	public int trainerAnzahl;
}
