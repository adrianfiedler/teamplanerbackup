package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.sql.Time;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TeamVorlageREST implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 3373086848800954707L;

	public String name;

	public String beschreibung;

	public OrtREST ort;

	public Time time;

	public String id;
}
