package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserSettingsREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5787656997997615269L;
	
	public String userId;
	public boolean montagsAbsagen;
	public boolean dienstagsAbsagen;
	public boolean mittwochsAbsagen;
	public boolean donnerstagsAbsagen;
	public boolean freitagsAbsagen;
	public boolean samstagsAbsagen;
	public boolean sonntagsAbsagen;
}
