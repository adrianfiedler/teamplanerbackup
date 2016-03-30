package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class UserStatusRequest implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = 3696287766622391278L;

	public String kommentar;
	
	public int status;
	
	public String terminId;

	public String userId;
}
