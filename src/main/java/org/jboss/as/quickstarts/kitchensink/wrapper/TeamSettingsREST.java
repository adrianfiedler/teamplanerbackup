package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TeamSettingsREST implements Serializable {
	private static final long serialVersionUID = -2460530645950082874L;
	public boolean trainerMussZusagen;
}
