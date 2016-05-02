package org.jboss.as.quickstarts.kitchensink.util;

public class ZusagenCount {
	private int yesCount;
	private int noCount;
	private int maybeCount;
	private boolean trainerMitgezaehlt;
	
	public int getYesCount() {
		return yesCount;
	}
	public void setYesCount(int yesCount) {
		this.yesCount = yesCount;
	}
	public int getNoCount() {
		return noCount;
	}
	public void setNoCount(int noCount) {
		this.noCount = noCount;
	}
	public boolean isTrainerMitgezaehlt() {
		return trainerMitgezaehlt;
	}
	public void setTrainerMitgezaehlt(boolean trainerMitgezaehlt) {
		this.trainerMitgezaehlt = trainerMitgezaehlt;
	}
	public int getMaybeCount() {
		return maybeCount;
	}
	public void setMaybeCount(int maybeCount) {
		this.maybeCount = maybeCount;
	}
}
