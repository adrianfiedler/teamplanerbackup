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
import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@Entity
@Table
@XmlRootElement
public class Termin extends AbstractBaseEntity implements Serializable {

    private String name;

    private int status;
    
    @Column(columnDefinition="TEXT")
    private String beschreibung;
    
    private String absageKommentar;
    
    private boolean maybeAllowed;
    
    private int defaultZusageStatus;

    @ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "ort_ref")
    private Ort ort;
    
    @ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST})
    @JoinColumn(name = "serie_ref")
    private Serie serie;


    @OneToMany(fetch=FetchType.LAZY, mappedBy="termin", cascade={CascadeType.ALL})
    private List<Zusage> zusagen;
    
    @Temporal(TemporalType.TIMESTAMP)
    private Date datum;
    
    @ManyToOne(fetch=FetchType.EAGER, cascade={CascadeType.PERSIST,CascadeType.MERGE})
    @JoinColumn(name = "team_ref")
    private Team team;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public Ort getOrt() {
		return ort;
	}

	public void setOrt(Ort ort) {
		this.ort = ort;
	}

	public Date getDatum() {
		return datum;
	}

	public void setDatum(Date datum) {
		this.datum = datum;
	}

	public Serie getSerie() {
		return serie;
	}

	public void setSerie(Serie serie) {
		this.serie = serie;
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}

	public List<Zusage> getZusagen() {
		return zusagen;
	}

	public void setZusagen(List<Zusage> zusagen) {
		this.zusagen = zusagen;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getAbsageKommentar() {
		return absageKommentar;
	}

	public void setAbsageKommentar(String absageKommentar) {
		this.absageKommentar = absageKommentar;
	}

	public boolean isMaybeAllowed() {
		return maybeAllowed;
	}

	public void setMaybeAllowed(boolean maybeAllowed) {
		this.maybeAllowed = maybeAllowed;
	}

	public int getDefaultZusageStatus() {
		return defaultZusageStatus;
	}

	public void setDefaultZusageStatus(int defaultZusageStatus) {
		this.defaultZusageStatus = defaultZusageStatus;
	}
}
