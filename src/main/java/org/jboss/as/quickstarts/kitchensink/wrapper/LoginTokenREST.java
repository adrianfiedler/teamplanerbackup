package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class LoginTokenREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7894650133643910563L;
	public String token;
	public Date timeOut;
}
