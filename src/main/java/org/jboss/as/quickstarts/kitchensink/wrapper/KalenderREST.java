package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class KalenderREST implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6370311864514856174L;

	public String teamId;
	
	public List<TerminREST> termine;
	
}
