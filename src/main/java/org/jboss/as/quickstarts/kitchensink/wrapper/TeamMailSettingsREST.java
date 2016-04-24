package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TeamMailSettingsREST implements Serializable {

	private static final long serialVersionUID = -5469819885573828323L;
	public String mailText;
	public boolean showIntroduction;
	public boolean showMailText;
	public int hoursBeforeTrainerReminder;
}
