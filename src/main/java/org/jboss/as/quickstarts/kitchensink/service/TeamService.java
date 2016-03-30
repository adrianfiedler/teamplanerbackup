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
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;

import org.jboss.as.quickstarts.kitchensink.model.Einladung;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.Team_;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class TeamService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    public void register(Team Team) throws Exception {
        em.persist(Team);
    }
    
    public Team save(Team Team) throws Exception {
        return em.merge(Team);
    }
    
    public Team findById(String id) {
        return em.find(Team.class, id);
    }

    public List<TeamRolle> findByUserId(String userId) {
    	TypedQuery<TeamRolle> query = em.createQuery(
    		    "SELECT DISTINCT tr FROM TeamRollen tr JOIN tr.team t WHERE tr.person.id = :userId", TeamRolle.class);
    		query.setParameter("userId", userId);
    		List<TeamRolle> results = query.getResultList();
    		return results;
    }
    
    public List<Team> findByTeamIds(List<String> teamIds) {
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Team> criteria = cb.createQuery(Team.class);
        Root<Team> team = criteria.from(Team.class);
        criteria.select(team).where(team.get(Team_.id).in(teamIds));
        return em.createQuery(criteria).getResultList();
    }

    public List<Team> findAllOrderedByName() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<Team> criteria = cb.createQuery(Team.class);
        Root<Team> Team = criteria.from(Team.class);
        // Swap criteria statements if you would like to try out type-safe criteria queries, a new
        // feature in JPA 2.0
        // criteria.select(Team).orderBy(cb.asc(Team.get(Team_.name)));
        criteria.select(Team).orderBy(cb.asc(Team.get("name")));
        return em.createQuery(criteria).getResultList();
    }
    
    public void delete(Team team) throws Exception {
    	em.remove(team);
    }
}
