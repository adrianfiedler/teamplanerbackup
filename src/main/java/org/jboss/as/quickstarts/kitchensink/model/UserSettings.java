package org.jboss.as.quickstarts.kitchensink.model;

import java.io.Serializable;

import javax.persistence.Entity;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@Entity
@Table
@XmlRootElement
public class UserSettings extends AbstractBaseEntity implements Serializable {
	private boolean montagsAbsagen;
	private boolean dienstagsAbsagen;
	private boolean mittwochsAbsagen;
	private boolean donnerstagsAbsagen;
	private boolean freitagsAbsagen;
	private boolean samstagsAbsagen;
	private boolean sonntagsAbsagen;
	
	public boolean isMontagsAbsagen() {
		return montagsAbsagen;
	}
	public void setMontagsAbsagen(boolean montagsAbsagen) {
		this.montagsAbsagen = montagsAbsagen;
	}
	public boolean isDienstagsAbsagen() {
		return dienstagsAbsagen;
	}
	public void setDienstagsAbsagen(boolean dienstagsAbsagen) {
		this.dienstagsAbsagen = dienstagsAbsagen;
	}
	public boolean isMittwochsAbsagen() {
		return mittwochsAbsagen;
	}
	public void setMittwochsAbsagen(boolean mittwochsAbsagen) {
		this.mittwochsAbsagen = mittwochsAbsagen;
	}
	public boolean isDonnerstagsAbsagen() {
		return donnerstagsAbsagen;
	}
	public void setDonnerstagsAbsagen(boolean donnerstagsAbsagen) {
		this.donnerstagsAbsagen = donnerstagsAbsagen;
	}
	public boolean isFreitagsAbsagen() {
		return freitagsAbsagen;
	}
	public void setFreitagsAbsagen(boolean freitagsAbsagen) {
		this.freitagsAbsagen = freitagsAbsagen;
	}
	public boolean isSamstagsAbsagen() {
		return samstagsAbsagen;
	}
	public void setSamstagsAbsagen(boolean samstagsAbsagen) {
		this.samstagsAbsagen = samstagsAbsagen;
	}
	public boolean isSonntagsAbsagen() {
		return sonntagsAbsagen;
	}
	public void setSonntagsAbsagen(boolean sonntagsAbsagen) {
		this.sonntagsAbsagen = sonntagsAbsagen;
	}
}
