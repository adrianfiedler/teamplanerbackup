package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class OrtREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -8890422628343998383L;
	public String beschreibung;
	public String strasse;
	public String nummer;
	public String plz;
	public String stadt;
	public boolean vorlage;
	public String id;
	public String longitude;
	public String latitude;

}
