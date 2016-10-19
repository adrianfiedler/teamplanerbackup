package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResendInvitationsREST implements Serializable {
	private static final long serialVersionUID = 1659047453819730529L;
	public String trainerId;
	public String teamId;
	public List<String> emails;
}
