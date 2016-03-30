package de.masalis.teamplanner.timer;

import java.io.Serializable;

public class TimerInfo implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 282355013488047087L;
	private String terminId;
	private String reminderId;

	public String getTerminId() {
		return terminId;
	}

	public void setTerminId(String terminId) {
		this.terminId = terminId;
	}

	public String getReminderId() {
		return reminderId;
	}

	public void setReminderId(String reminderId) {
		this.reminderId = reminderId;
	}
}
