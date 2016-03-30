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
public class Team extends AbstractBaseEntity implements Serializable {

    private String name;

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "verein_ref")
    private Verein verein;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="team")
    private List<Termin> termine;
    
    @OneToMany(fetch=FetchType.EAGER, mappedBy="team")
    private List<TeamRolle> rollen;
    
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
}
