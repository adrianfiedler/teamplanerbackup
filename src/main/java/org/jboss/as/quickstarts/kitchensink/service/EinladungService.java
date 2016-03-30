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
package org.jboss.as.quickstarts.kitchensink.service;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jboss.as.quickstarts.kitchensink.model.Einladung;
import org.jboss.as.quickstarts.kitchensink.model.Einladung_;
import org.jboss.as.quickstarts.kitchensink.model.Ort;
import org.jboss.as.quickstarts.kitchensink.model.Ort_;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.Team_;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.Verein_;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class EinladungService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    public void save(Einladung einladung) throws Exception {
        em.persist(einladung);
    }
    
    public void delete(Einladung einladung) throws Exception {
    	em.remove(einladung);
    }
    
    public Einladung findById(String id) {
        return em.find(Einladung.class, id);
    }

	public List<Einladung> findByTeamId(String teamId) {
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Einladung> criteria = cb.createQuery(Einladung.class);
        Root<Einladung> einladung = criteria.from(Einladung.class);
        Join<Einladung,Team> team = einladung.join(Einladung_.team);
        criteria.select(einladung).where(cb.equal(team.get(Team_.id), teamId));
        return em.createQuery(criteria).getResultList();
	}

	public Einladung findByTeamUndEmail(String teamId, String email) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Einladung> criteria = cb.createQuery(Einladung.class);
        Root<Einladung> einladung = criteria.from(Einladung.class);
        Join<Einladung,Team> team = einladung.join(Einladung_.team);
        criteria.select(einladung).where(cb.and(
        		cb.equal(einladung.get(Einladung_.email), email), 
        		cb.equal(team.get(Team_.id), teamId)));
        List<Einladung> results = em.createQuery(criteria).getResultList();
        if(results != null && results.size() > 0){
        	return results.get(0);
        } else{
        	return null;
        }
	}
}
