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

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jboss.as.quickstarts.kitchensink.model.LoginToken;
import org.jboss.as.quickstarts.kitchensink.model.LoginToken_;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle_;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.User_;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class LoginTokenService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    public LoginToken save(LoginToken loginToken) {
        return em.merge(loginToken);
    }
    
    public LoginToken findById(String id) {
        return em.find(LoginToken.class, id);
    }
    
    public LoginToken findTokenByUserId(String userId){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LoginToken> criteria = cb.createQuery(LoginToken.class);
        Root<LoginToken> token = criteria.from(LoginToken.class);
        Join<LoginToken, User> user = token.join(LoginToken_.user);
        criteria.select(token).where(cb.equal(user.get(User_.id), userId));
        List<LoginToken> ergs = em.createQuery(criteria).getResultList();
        if(ergs == null || ergs.size() == 0){
        	return null;
        } else{
        	return ergs.get(0);
        }
    }
    
    public LoginToken findTokenByTokenstring(String tokenString){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LoginToken> criteria = cb.createQuery(LoginToken.class);
        Root<LoginToken> tokenRoot = criteria.from(LoginToken.class);
        criteria.select(tokenRoot).where(cb.equal(tokenRoot.get(LoginToken_.token), tokenString));
        List<LoginToken> ergs = em.createQuery(criteria).getResultList();
        if(ergs == null || ergs.size() == 0){
        	return null;
        } else{
        	return ergs.get(0);
        }
    }
    
    public User findUserByTokenstring(String tokenString){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<User> criteria = cb.createQuery(User.class);
        Root<User> user = criteria.from(User.class);
        Join<User, LoginToken> token = user.join(User_.loginToken);
        criteria.select(user).where(cb.equal(token.get(LoginToken_.token), tokenString));
        List<User> ergs = em.createQuery(criteria).getResultList();
        if(ergs == null || ergs.size() == 0){
        	return null;
        } else{
        	return ergs.get(0);
        }
    }
    
    public String findUserIdByTokenstring(String tokenString){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<String> criteria = cb.createQuery(String.class);
        Root<User> user = criteria.from(User.class);
        Join<User, LoginToken> token = user.join(User_.loginToken);
        criteria.select(user.get(User_.id)).where(cb.equal(token.get(LoginToken_.token), tokenString));
        List<String> ergs = em.createQuery(criteria).getResultList();
        if(ergs == null || ergs.size() == 0){
        	return null;
        } else{
        	return ergs.get(0);
        }
    }
    
    public void delete(LoginToken loginToken) {
    	em.remove(loginToken);
    }
    
    
    public User getUserIfLoggedIn(String tokenString){
    	LoginToken loginToken = findTokenByTokenstring(tokenString);
    	if(loginToken != null){
    		User user = loginToken.getUser();
    		if(user != null){
    			Date now = new Date();
    			Date timeoutDate = loginToken.getTimeOut();
    			if(timeoutDate.after(now)){
    				return user;
    			} 
    		}
    	} 
    	return null;
    }
}
