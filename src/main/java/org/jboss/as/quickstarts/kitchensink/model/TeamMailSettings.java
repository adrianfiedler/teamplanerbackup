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
public class TeamMailSettings extends AbstractBaseEntity implements Serializable {

	private String mailText;
	private boolean showIntroduction;
	private boolean showMailText;
	private int hoursBeforeTrainerReminder;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "team_ref")
    private Team team;
    
	public Team getTeam() {
		return team;
	}
	public void setTeam(Team team) {
		this.team = team;
	}
	public String getMailText() {
		return mailText;
	}
	public void setMailText(String mailText) {
		this.mailText = mailText;
	}
	public boolean isShowIntroduction() {
		return showIntroduction;
	}
	public void setShowIntroduction(boolean showIntroduction) {
		this.showIntroduction = showIntroduction;
	}
	public boolean isShowMailText() {
		return showMailText;
	}
	public void setShowMailText(boolean showMailText) {
		this.showMailText = showMailText;
	}
	public int getHoursBeforeTrainerReminder() {
		return hoursBeforeTrainerReminder;
	}
	public void setHoursBeforeTrainerReminder(int hoursBeforeTrainerReminder) {
		this.hoursBeforeTrainerReminder = hoursBeforeTrainerReminder;
	}

}
