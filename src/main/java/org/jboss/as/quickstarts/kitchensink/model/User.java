/*
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
 * JBoss, Home of Professional Open Source
 * contributors by the @authors tag. See the copyright.txt in the
 * distribution for a full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.as.quickstarts.kitchensink.model;

import java.io.Serializable;
import java.util.List;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Convert;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

import org.hibernate.validator.constraints.Email;

import de.masalis.teamplanner.converters.StringCryptoConverter;


@SuppressWarnings("serial")
@Entity
@Table
@XmlRootElement
public class User extends AbstractBaseEntity implements Serializable {

	@Convert(converter = StringCryptoConverter.class)
    private String name;
	@Convert(converter = StringCryptoConverter.class)
    private String vorname;
    
    @Convert(converter = StringCryptoConverter.class)
    private String passwort;
    
    private String facebookUserId;
    private String facebookToken;
    
    private boolean admin;
    
    private boolean aktiviert;
    
    private String aktivierToken;
    
    private boolean weeklyStatusMail;
    
    private boolean terminReminderMail;
    
    private String timeZone;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "verein_ref")
    private Verein verein;

    @Email
    @Convert(converter = StringCryptoConverter.class)
    private String email;

    @OneToMany(fetch=FetchType.EAGER, mappedBy="user", cascade={CascadeType.ALL})
    private List<TeamRolle> rollen;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="user", cascade={CascadeType.ALL}, orphanRemoval=true)
    private Set<PushToken> pushToken;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="user", cascade={CascadeType.ALL})
    private List<Zusage> zusagen;
    
    private String googlePlus;
    private String fotoUrl;
    
    @OneToOne(cascade = CascadeType.ALL, optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name="user_settings_ref", nullable=false)
    private UserSettings userSettings;

    @OneToMany(fetch=FetchType.EAGER, mappedBy="inviter", cascade={CascadeType.ALL}, orphanRemoval=true)
    private List<Einladung> einladungen;
    
    @OneToOne(fetch=FetchType.LAZY, mappedBy="user", cascade={CascadeType.ALL}, orphanRemoval=true)
    private LoginToken loginToken;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

	public String getGooglePlus() {
		return googlePlus;
	}

	public void setGooglePlus(String googlePlus) {
		this.googlePlus = googlePlus;
	}

	public String getFotoUrl() {
		return fotoUrl;
	}

	public void setFotoUrl(String fotoUrl) {
		this.fotoUrl = fotoUrl;
	}

	public List<TeamRolle> getRollen() {
		return rollen;
	}

	public void setRollen(List<TeamRolle> rollen) {
		this.rollen = rollen;
	}

	public List<Zusage> getZusagen() {
		return zusagen;
	}

	public void setZusagen(List<Zusage> zusagen) {
		this.zusagen = zusagen;
	}

	public String getVorname() {
		return vorname;
	}

	public void setVorname(String vorname) {
		this.vorname = vorname;
	}

	public String getPasswort() {
		return passwort;
	}

	public void setPasswort(String passwort) {
		this.passwort = passwort;
	}

	public String getFacebookToken() {
		return facebookToken;
	}

	public void setFacebookToken(String facebookToken) {
		this.facebookToken = facebookToken;
	}

	public boolean isAdmin() {
		return admin;
	}

	public void setAdmin(boolean admin) {
		this.admin = admin;
	}

	public Verein getVerein() {
		return verein;
	}

	public void setVerein(Verein verein) {
		this.verein = verein;
	}

	public boolean isAktiviert() {
		return aktiviert;
	}

	public void setAktiviert(boolean aktiviert) {
		this.aktiviert = aktiviert;
	}

	public String getAktivierToken() {
		return aktivierToken;
	}

	public void setAktivierToken(String aktivierToken) {
		this.aktivierToken = aktivierToken;
	}

	public boolean isWeeklyStatusMail() {
		return weeklyStatusMail;
	}

	public void setWeeklyStatusMail(boolean weeklyStatusMail) {
		this.weeklyStatusMail = weeklyStatusMail;
	}

	public boolean isTerminReminderMail() {
		return terminReminderMail;
	}

	public void setTerminReminderMail(boolean terminReminderMail) {
		this.terminReminderMail = terminReminderMail;
	}

	public String getFacebookUserId() {
		return facebookUserId;
	}

	public void setFacebookUserId(String facebookUserId) {
		this.facebookUserId = facebookUserId;
	}

	public UserSettings getUserSettings() {
		return userSettings;
	}

	public void setUserSettings(UserSettings userSettings) {
		this.userSettings = userSettings;
	}

	public List<Einladung> getEinladungen() {
		return einladungen;
	}

	public void setEinladungen(List<Einladung> einladungen) {
		this.einladungen = einladungen;
	}

	public LoginToken getLoginToken() {
		return loginToken;
	}

	public void setLoginToken(LoginToken loginToken) {
		this.loginToken = loginToken;
	}

	public Set<PushToken> getPushToken() {
		return pushToken;
	}

	public void setPushToken(Set<PushToken> pushToken) {
		this.pushToken = pushToken;
	}

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}
}
