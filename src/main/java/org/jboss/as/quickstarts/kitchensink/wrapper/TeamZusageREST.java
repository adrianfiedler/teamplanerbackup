package org.jboss.as.quickstarts.kitchensink.wrapper;

import java.io.Serializable;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement
public class TeamZusageREST implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 785207383933708316L;

	public List<TrainerZusageREST> trainerZusagen;
	public List<SpielerZusageREST> spielerZusagen;
	
}
