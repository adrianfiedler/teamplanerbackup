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

import java.util.ArrayList;
import java.util.Date;
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
import org.jboss.as.quickstarts.kitchensink.model.Serie;
import org.jboss.as.quickstarts.kitchensink.model.Serie_;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.Team_;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.TerminVorlage;
import org.jboss.as.quickstarts.kitchensink.model.TerminVorlage_;
import org.jboss.as.quickstarts.kitchensink.model.Termin_;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.User_;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;
import org.jboss.as.quickstarts.kitchensink.model.Zusage_;
import org.jboss.as.quickstarts.kitchensink.wrapper.request.TerminRequestREST;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class TerminService {

	@Inject
	private Logger log;

	@Inject
	private EntityManager em;

	public Termin save(Termin Termin){
		return em.merge(Termin);
	}

	public TerminVorlage saveVorlage(TerminVorlage terminVorlage){
		return em.merge(terminVorlage);
	}

	public Serie saveSerie(Serie serie){
		return em.merge(serie);
	}
	
    public void delete(Termin termin) {
    	em.remove(termin);
    }

	public void updateZusage(Zusage zusage) {
		em.merge(zusage);
	}

	public Termin findById(String id) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> termin = criteria.from(Termin.class);
		criteria.select(termin).where(cb.equal(termin.get(Termin_.id), id));
		List<Termin> resultList = em.createQuery(criteria).getResultList();
		if (resultList.size() > 0) {
			return resultList.get(0);
		} else {
			return null;
		}
	}
	
	// Findet den Termin, der vor dem parameter Termin kommt eines Users
	public Termin findPreviousTermin(Termin termin, User user){
		Termin prevTermin = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> rootTermin = criteria.from(Termin.class);
		Join<Termin, Zusage> zusage = rootTermin.join(Termin_.zusagen);
		Join<Zusage, User> userJoin = zusage.join(Zusage_.user);
		criteria.select(rootTermin).where(cb.and(
				cb.equal(userJoin.get(User_.id), user.getId()),
				cb.lessThan(rootTermin.get(Termin_.datum), termin.getDatum())
		)).orderBy(cb.desc(rootTermin.get(Termin_.datum)));
		List<Termin> resultList = em.createQuery(criteria).setMaxResults(1).getResultList();
		if (resultList.size() > 0) {
			prevTermin = resultList.get(0);
		} 
		return prevTermin;
	}
	
	// Findet den Termin, der nach dem parameter Termin kommt eines Users
	public Termin findNextTermin(Termin termin, User user){
		Termin prevTermin = null;
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> rootTermin = criteria.from(Termin.class);
		Join<Termin, Zusage> zusage = rootTermin.join(Termin_.zusagen);
		Join<Zusage, User> userJoin = zusage.join(Zusage_.user);
		criteria.select(rootTermin).where(cb.and(
				cb.equal(userJoin.get(User_.id), user.getId()),
				cb.greaterThan(rootTermin.get(Termin_.datum), termin.getDatum())
		)).orderBy(cb.asc(rootTermin.get(Termin_.datum)));
		List<Termin> resultList = em.createQuery(criteria).setMaxResults(1).getResultList();
		if (resultList.size() > 0) {
			prevTermin = resultList.get(0);
		} 
		return prevTermin;
	}

	public List<Termin> findByTeamIds(List<String> teamIds) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> termin = criteria.from(Termin.class);
		Join<Termin, Team> team = termin.join(Termin_.team);
		criteria.select(termin).where(team.get(Team_.id).in(teamIds));
		return em.createQuery(criteria).getResultList();

	}

	public List<Termin> findByTeamIdsAndDates(List<String> teamIds, Date startDate, Date endDate) {
		if(teamIds.size() == 0){
			return new ArrayList<Termin>();
		}
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> termin = criteria.from(Termin.class);
		Join<Termin, Team> team = termin.join(Termin_.team);
		criteria.select(termin)
				.where(cb.and(
						team.get(Team_.id).in(teamIds), 
						cb.between(termin.get(Termin_.datum), startDate, endDate),
						cb.notEqual(termin.get(Termin_.status), "2")
				)).orderBy(cb.asc(termin.get(Termin_.datum)));
		List<Termin> erg = em.createQuery(criteria).getResultList();
		if(erg != null && erg.size() > 0){
			return erg;
		} else{
			return new ArrayList<Termin>();
		}
	}
	
	public List<Termin> findByUserIdAndDates(String userId, Date startDate, Date endDate) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> termin = criteria.from(Termin.class);
		Join<Termin, Zusage> zusagen = termin.join(Termin_.zusagen);
		Join<Zusage, User> user = zusagen.join(Zusage_.user);
		criteria.select(termin)
				.where(cb.and(
						cb.equal(user.get(User_.id), userId), 
						cb.between(termin.get(Termin_.datum), startDate, endDate),
						cb.notEqual(termin.get(Termin_.status), "2")
				));
		return em.createQuery(criteria).getResultList();
	}
	
	public List<Termin> findByUserIdAndStartDate(String userId, Date startDate) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> termin = criteria.from(Termin.class);
		Join<Termin, Zusage> zusagen = termin.join(Termin_.zusagen);
		Join<Zusage, User> user = zusagen.join(Zusage_.user);
		criteria.select(termin)
				.where(cb.and(
						cb.equal(user.get(User_.id), userId), 
						cb.greaterThanOrEqualTo(termin.get(Termin_.datum), startDate),
						cb.notEqual(termin.get(Termin_.status), "2")
				));
		List<Termin> ergs = em.createQuery(criteria).getResultList();
		if(ergs == null || ergs.size() == 0){
			return null;
		} else{
			return ergs;
		}
	}
	
	public List<Termin> findAllByDates(Date startDate, Date endDate) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> termin = criteria.from(Termin.class);
		//Join<Termin, Zusage> zusagen = termin.join(Termin_.zusagen);
		//Join<Zusage, User> user = zusagen.join(Zusage_.user);
		criteria.select(termin)
				.where(cb.and(
						cb.between(termin.get(Termin_.datum), startDate, endDate),
						cb.notEqual(termin.get(Termin_.status), "2")
				));
		return em.createQuery(criteria).getResultList();
	}

	public List<Termin> findByTeamId(String teamId) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> termin = criteria.from(Termin.class);
		Join<Termin, Team> team = termin.join(Termin_.team);
		criteria.select(termin).where(cb.equal(team.get(Team_.id), teamId));
		return em.createQuery(criteria).getResultList();
	}

	public List<Termin> findAllOrderedByName() {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> Termin = criteria.from(Termin.class);
		criteria.select(Termin).orderBy(cb.asc(Termin.get(Termin_.name)));
		return em.createQuery(criteria).getResultList();
	}

	public List<TerminVorlage> findVorlagenByTeamId(String teamId) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TerminVorlage> criteria = cb.createQuery(TerminVorlage.class);
		Root<TerminVorlage> terminVorlage = criteria.from(TerminVorlage.class);
		Join<TerminVorlage, Team> team = terminVorlage.join(TerminVorlage_.team);
		criteria.select(terminVorlage).where(cb.equal(team.get(Team_.id), teamId)).orderBy(cb.asc(terminVorlage.get(TerminVorlage_.name)));
		return em.createQuery(criteria).getResultList();
	}
	
	public TerminVorlage getTerminVorlageById(String terminVorlageId){
		if(terminVorlageId == null || terminVorlageId.length() == 0){
			return null;
		}
		return em.find(TerminVorlage.class, terminVorlageId);
	}
	
	public TerminVorlage getTerminVorlageByVorlageValues(String name, String beschreibung, String teamId){
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<TerminVorlage> criteria = cb.createQuery(TerminVorlage.class);
		Root<TerminVorlage> terminVorlage = criteria.from(TerminVorlage.class);
		Join<TerminVorlage, Team> team = terminVorlage.join(TerminVorlage_.team);
		criteria.select(terminVorlage).where(cb.and(
				cb.equal(terminVorlage.get(TerminVorlage_.name), name),
				cb.equal(terminVorlage.get(TerminVorlage_.beschreibung), beschreibung),
				cb.equal(team.get(Team_.id), teamId)
				)
				).orderBy(cb.asc(terminVorlage.get(TerminVorlage_.name)));
		List<TerminVorlage> erg = em.createQuery(criteria).getResultList();
		if(erg == null || erg.size() == 0){
			return null;
		} else{
			return erg.get(0);
		}
	}
	
	public List<Termin> findInFutureOfTerminAndSerie(Termin terminOrig) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
		CriteriaQuery<Termin> criteria = cb.createQuery(Termin.class);
		Root<Termin> terminRoot = criteria.from(Termin.class);
		Join<Termin, Serie> serie = terminRoot.join(Termin_.serie);
		criteria.select(terminRoot).where(cb.and(
				cb.equal(serie.get(Serie_.id), terminOrig.getSerie().getId()),
				cb.greaterThan(terminRoot.get(Termin_.datum), terminOrig.getDatum())
		));
		criteria.orderBy(cb.asc(terminRoot.get(Termin_.datum)));
		return em.createQuery(criteria).getResultList();

	}
}
