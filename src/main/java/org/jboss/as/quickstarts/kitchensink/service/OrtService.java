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
import org.jboss.as.quickstarts.kitchensink.model.Ort;
import org.jboss.as.quickstarts.kitchensink.model.Ort_;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.Termin_;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.Verein_;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class OrtService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    public Ort save(Ort ort) {
        return em.merge(ort);
    }
    
    public Ort findById(String id) {
        return em.find(Ort.class, id);
    }
    
    public List<Ort> findVorlagenByVerein(String vereinId){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Ort> criteria = cb.createQuery(Ort.class);
        Root<Ort> ortVorlage = criteria.from(Ort.class);
        Join<Ort,Verein> verein = ortVorlage.join(Ort_.verein);
        criteria.select(ortVorlage).where(cb.and(
        		cb.equal(verein.get(Verein_.id), vereinId), 
        		cb.equal(ortVorlage.get(Ort_.vorlage), true)));
        return em.createQuery(criteria).getResultList();
    }
    
    public void delete(Ort ort) {
    	em.remove(ort);
    }
    
    public Ort findByTerminId(String terminId){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Ort> criteria = cb.createQuery(Ort.class);
        Root<Termin> termin = criteria.from(Termin.class);
        Join<Termin,Ort> ort = termin.join(Termin_.ort);
        criteria.select(ort).where(cb.equal(termin.get(Termin_.id), terminId));
        List<Ort> erg = em.createQuery(criteria).getResultList();
        if(erg != null && erg.size() > 0){
        	return erg.get(0);
        } else{
        	return null;
        }
    }
}
