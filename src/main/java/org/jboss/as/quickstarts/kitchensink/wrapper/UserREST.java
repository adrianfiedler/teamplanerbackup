package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

import org.jboss.as.quickstarts.kitchensink.model.LoginToken;

@XmlRootElement
public class UserREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 4659362526518476860L;

	public String id;
	public boolean admin;
	public String name;
	public String vorname;
	public String email;
	public String facebookToken;
	public VereinREST verein;
	public boolean active;
	public LoginTokenREST loginToken;
}
