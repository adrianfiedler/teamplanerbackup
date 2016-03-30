package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class SerieREST implements Serializable {

	/**
	* 
	*/
	private static final long serialVersionUID = 7095214181017669120L;
	public int intervall;
	public String serieEndDate;
}
