package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TerminREST implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3373086848800954707L;

	public String name;

	public String beschreibung;

	public String absageKommentar;
	
	public OrtREST ort;

	public SerieREST serie;

	public TeamZusageREST teamZusagen;

	public Date datum;

	public int status;
	
	public String id;
	
	public Date serieEndDate;
	
	public int yesCount;
	
	public int noCount;
	
	public int maybeCount;
	
	public UserZusageREST userZusage;
	
	public TerminSettingsREST terminSettings;
	
	public String teamId;
	
	public String nextTerminId;
	
	public String previousTerminId;
}
