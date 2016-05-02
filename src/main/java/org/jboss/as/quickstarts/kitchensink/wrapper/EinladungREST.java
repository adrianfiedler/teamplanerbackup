package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class EinladungREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7373411321665292238L;
	public String name;
	public String vorname;
	public String email;
	public int status;
}
