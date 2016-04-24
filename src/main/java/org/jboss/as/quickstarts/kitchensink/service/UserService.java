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
import javax.persistence.EntityExistsException;
import javax.persistence.EntityManager;
import javax.persistence.TransactionRequiredException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jboss.as.quickstarts.kitchensink.model.Einladung;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle_;
import org.jboss.as.quickstarts.kitchensink.model.Team_;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.User_;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.VereinModule;
import org.jboss.as.quickstarts.kitchensink.model.VereinModule_;
import org.jboss.as.quickstarts.kitchensink.model.Verein_;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class UserService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    public void register(User user) throws EntityExistsException, IllegalArgumentException, TransactionRequiredException{
        log.info("Registering " + user.getName());
        em.persist(user);
    }
    
    public void delete(User user) throws Exception {
    	em.remove(user);
    }
    
    public User update(User user){
    	return em.merge(user);
    }
    
    public User findById(String id) {
        return em.find(User.class, id);
    }

    public User findByEmail(String email) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> User = criteria.from(User.class);
        criteria.select(User).where(cb.equal(User.get(User_.email), email));
        List<User> users = em.createQuery(criteria).getResultList();
        if(users != null && users.size() > 0){
        	return users.get(0);
        } else{
        	return null;
        }
    }
    
    public List<User> findAllWeeklyNotifiedUsers() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        Join<User, Verein> verein = user.join(User_.verein);
        Join<Verein, VereinModule> module = verein.join(Verein_.module);
        criteria.select(user).where(cb.and(
        		cb.isTrue(user.get(User_.weeklyStatusMail)), 
        		cb.isTrue(module.get(VereinModule_.mailModul))
        	));
        List<User> users = em.createQuery(criteria).getResultList();
        return users;
    }
    
    public User findByActivationToken(String activationToken) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> User = criteria.from(User.class);
        criteria.select(User).where(cb.equal(User.get(User_.aktivierToken), activationToken));
        List<User> users = em.createQuery(criteria).getResultList();
        if(users.size() > 0){
        	return users.get(0);
        } else{
        	return null;
        }
    }
    
    public List<User> findUsersByTeamId(String teamId) {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        Join<User, TeamRolle> rolle = user.join(User_.rollen);
        Join<TeamRolle, Team> team = rolle.join(TeamRolle_.team);
        criteria.select(user).where(cb.equal(team.get(Team_.id), teamId));
        return em.createQuery(criteria).getResultList();
    }

    public List<User> findAllOrderedByName() {
        CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> User = criteria.from(User.class);
        // Swap criteria statements if you would like to try out type-safe criteria queries, a new
        // feature in JPA 2.0
        // criteria.select(User).orderBy(cb.asc(User.get(User_.name)));
        criteria.select(User).orderBy(cb.asc(User.get("name")));
        return em.createQuery(criteria).getResultList();
    }
}
