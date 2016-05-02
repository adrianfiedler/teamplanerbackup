package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class ResponseREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 5243835025422858201L;

	public String status;
	public String description;
	public Object data;

}
