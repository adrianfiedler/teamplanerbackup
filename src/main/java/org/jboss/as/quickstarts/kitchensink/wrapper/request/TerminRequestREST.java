package org.jboss.as.quickstarts.kitchensink.wrapper.request;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.as.quickstarts.kitchensink.wrapper.OrtREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.SerieREST;

@XmlRootElement
public class TerminRequestREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5517346238163888827L;

	public String name;

	public String beschreibung;

	public OrtREST ort;

	public SerieREST serie;

	public String datum;

	public boolean vorlage;
	
	public String vorlageId;
	
	public boolean maybeAllowed;
	
	public String teamId;
	
	public String terminId;
	
	public String userId;
	
	public int defaultZusageStatus;
}
