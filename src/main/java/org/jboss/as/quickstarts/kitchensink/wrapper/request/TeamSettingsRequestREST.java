package org.jboss.as.quickstarts.kitchensink.wrapper.request;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TeamSettingsRequestREST implements Serializable {
	private static final long serialVersionUID = -6117119930436843568L;
	
	public boolean trainerMussZusagen;
	
	public String teamId;

	public String userId;
}
