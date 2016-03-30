package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class PersonREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1053676529776567211L;

	public String name;
	public String vorname;
	public String displayName;

	public String email;

	public String facebook;
	public String googlePlus;
	public String fotoUrl;

}
