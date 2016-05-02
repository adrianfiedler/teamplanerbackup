package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.sql.Time;
import java.util.Date;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TerminVorlageREST implements Serializable {

    /**
	 * 
	 */
	private static final long serialVersionUID = 7160834814467222021L;

	public String name;

    public String beschreibung;
    
    public OrtREST ort;
    
    public Date time;
    
    public String id;
}
