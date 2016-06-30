/*
 * JBoss, Home of Professional Open Source
 * Copyright 2013, Red Hat, Inc. and/or its affiliates, and individual
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

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@Entity
@Table
@XmlRootElement
public class Team extends AbstractBaseEntity implements Serializable {

    private String name;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "verein_ref")
    private Verein verein;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="team", cascade={CascadeType.ALL})
    private List<Termin> termine;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="team", cascade={CascadeType.ALL})
    private List<TeamRolle> rollen;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="team", cascade={CascadeType.ALL}, orphanRemoval=true)
    private List<Einladung> einladungen;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="team", cascade={CascadeType.ALL})
    private List<TerminVorlage> terminVorlagen;
    
    @OneToOne(fetch=FetchType.LAZY, mappedBy="team", cascade={CascadeType.ALL})
    private TeamMailSettings weeklyTeamMailSettings;
    
    @OneToOne(fetch=FetchType.LAZY, mappedBy="team", cascade={CascadeType.ALL})
    private TeamSettings teamSettings;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public List<Termin> getTermine() {
		return termine;
	}

	public void setTermine(List<Termin> termine) {
		this.termine = termine;
	}

	public List<TeamRolle> getRollen() {
		return rollen;
	}

	public void setRollen(List<TeamRolle> rollen) {
		this.rollen = rollen;
	}

	public Verein getVerein() {
		return verein;
	}

	public void setVerein(Verein verein) {
		this.verein = verein;
	}

	public List<Einladung> getEinladungen() {
		return einladungen;
	}

	public void setEinladungen(List<Einladung> einladungen) {
		this.einladungen = einladungen;
	}

	public List<TerminVorlage> getTerminVorlagen() {
		return terminVorlagen;
	}

	public void setTerminVorlagen(List<TerminVorlage> terminVorlagen) {
		this.terminVorlagen = terminVorlagen;
	}

	public TeamMailSettings getWeeklyTeamMailSettings() {
		return weeklyTeamMailSettings;
	}

	public void setWeeklyTeamMailSettings(TeamMailSettings weeklyTeamMailSettings) {
		this.weeklyTeamMailSettings = weeklyTeamMailSettings;
	}

	public TeamSettings getTeamSettings() {
		return teamSettings;
	}

	public void setTeamSettings(TeamSettings teamSettings) {
		this.teamSettings = teamSettings;
	}
}
