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
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@Entity
@Table
@XmlRootElement
public class Verein extends AbstractBaseEntity implements Serializable {

    private String name;

    @OneToMany(fetch=FetchType.LAZY, mappedBy="verein", cascade={CascadeType.ALL})
    private List<Team> vereinsTeams;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="verein", cascade={CascadeType.ALL})
    private List<Ort> orte;
    
    @OneToMany(fetch=FetchType.LAZY, mappedBy="verein", cascade={CascadeType.ALL})
    private List<User> user;
    
    private int gekaufteTeams;
    
    @OneToOne(fetch=FetchType.LAZY, mappedBy="verein", cascade={CascadeType.ALL})
    private VereinModule module;
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

	public List<Team> getVereinsTeams() {
		return vereinsTeams;
	}

	public void setVereinsTeams(List<Team> vereinsTeams) {
		this.vereinsTeams = vereinsTeams;
	}

	public List<Ort> getOrte() {
		return orte;
	}

	public void setOrte(List<Ort> orte) {
		this.orte = orte;
	}

	public List<User> getUser() {
		return user;
	}

	public void setUser(List<User> user) {
		this.user = user;
	}

	public int getGekaufteTeams() {
		return gekaufteTeams;
	}

	public void setGekaufteTeams(int gekaufteTeams) {
		this.gekaufteTeams = gekaufteTeams;
	}

	public VereinModule getModule() {
		return module;
	}

	public void setModule(VereinModule module) {
		this.module = module;
	}
}
