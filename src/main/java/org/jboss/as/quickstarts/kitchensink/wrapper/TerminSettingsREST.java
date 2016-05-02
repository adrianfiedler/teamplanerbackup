package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TerminSettingsREST implements Serializable {
	private static final long serialVersionUID = 3108442265131147643L;
	public boolean maybeAllowed;
}
