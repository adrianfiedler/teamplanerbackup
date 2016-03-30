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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@Entity
@Table
@XmlRootElement
public class Ort extends AbstractBaseEntity implements Serializable {

    private String beschreibung;
    private String strasse;
    private String nummer;
    private String plz;
    private String stadt;
    private boolean vorlage;
    private String longitude;
    private String latitude;
    private String vereinId;
    private String teamId;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "verein_ref")
    private Verein verein;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="ort")
    private List<Termin> termine;
    
    public String getBeschreibung() {
		return beschreibung;
	}

	public void setBeschreibung(String beschreibung) {
		this.beschreibung = beschreibung;
	}

	public String getStrasse() {
		return strasse;
	}

	public void setStrasse(String strasse) {
		this.strasse = strasse;
	}

	public String getNummer() {
		return nummer;
	}

	public void setNummer(String nummer) {
		this.nummer = nummer;
	}

	public String getPlz() {
		return plz;
	}

	public void setPlz(String plz) {
		this.plz = plz;
	}

	public String getStadt() {
		return stadt;
	}

	public void setStadt(String stadt) {
		this.stadt = stadt;
	}

	public List<Termin> getTermine() {
		return termine;
	}

	public void setTermine(List<Termin> termine) {
		this.termine = termine;
	}

	public boolean isVorlage() {
		return vorlage;
	}

	public void setVorlage(boolean vorlage) {
		this.vorlage = vorlage;
	}

	public Verein getVerein() {
		return verein;
	}

	public void setVerein(Verein verein) {
		this.verein = verein;
	}

	public String getLongitude() {
		return longitude;
	}

	public void setLongitude(String longitude) {
		this.longitude = longitude;
	}

	public String getLatitude() {
		return latitude;
	}

	public void setLatitude(String latitude) {
		this.latitude = latitude;
	}

	public String getVereinId() {
		return vereinId;
	}

	public void setVereinId(String vereinId) {
		this.vereinId = vereinId;
	}

	public String getTeamId() {
		return teamId;
	}

	public void setTeamId(String teamId) {
		this.teamId = teamId;
	}

}
