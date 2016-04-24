package org.jboss.as.quickstarts.kitchensink.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@Entity
@Table
@XmlRootElement
public class TeamSettings extends AbstractBaseEntity implements Serializable {

	private boolean trainerMussZusagen;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "team_ref")
    private Team team;
    
	public Team getTeam() {
		return team;
	}
	public void setTeam(Team team) {
		this.team = team;
	}
	public boolean isTrainerMussZusagen() {
		return trainerMussZusagen;
	}
	public void setTrainerMussZusagen(boolean trainerMussZusagen) {
		this.trainerMussZusagen = trainerMussZusagen;
	}
}
