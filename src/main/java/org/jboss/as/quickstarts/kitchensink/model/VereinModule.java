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
public class VereinModule extends AbstractBaseEntity implements Serializable {

	private boolean mailModul;
    
    @OneToOne(fetch=FetchType.LAZY)
    @JoinColumn(name = "verein_ref")
    private Verein verein;

	public boolean isMailModul() {
		return mailModul;
	}

	public void setMailModul(boolean mailModul) {
		this.mailModul = mailModul;
	}

	public Verein getVerein() {
		return verein;
	}

	public void setVerein(Verein verein) {
		this.verein = verein;
	}

}
