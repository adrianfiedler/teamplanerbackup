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

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlRootElement;

@SuppressWarnings("serial")
@Entity
@Table
@XmlRootElement
public class Zusage extends AbstractBaseEntity implements Serializable {

    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "termin_ref")
    private Termin termin;
    
    @ManyToOne(fetch=FetchType.EAGER)
    @JoinColumn(name = "user_ref")
    private User user;
    
    private int status;
    
    private String kommentar;
    
    private boolean autoSet;
    
	public Termin getTermin() {
		return termin;
	}

	public void setTermin(Termin termin) {
		this.termin = termin;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getKommentar() {
		return kommentar;
	}

	public void setKommentar(String kommentar) {
		this.kommentar = kommentar;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
	}

	public boolean isAutoSet() {
		return autoSet;
	}

	public void setAutoSet(boolean autoSet) {
		this.autoSet = autoSet;
	}
}
