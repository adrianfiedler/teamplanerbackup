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
import javax.persistence.criteria.Root;

import org.jboss.as.quickstarts.kitchensink.model.LoginToken;
import org.jboss.as.quickstarts.kitchensink.model.LoginToken_;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class LoginTokenService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    public LoginToken save(LoginToken loginToken) throws Exception {
        return em.merge(loginToken);
    }
    
    public LoginToken findById(String id) {
        return em.find(LoginToken.class, id);
    }
    
    public LoginToken findTokenByUserId(String userId){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LoginToken> criteria = cb.createQuery(LoginToken.class);
        Root<LoginToken> token = criteria.from(LoginToken.class);
        criteria.select(token).where(cb.equal(token.get(LoginToken_.userId), userId));
        List<LoginToken> ergs = em.createQuery(criteria).getResultList();
        if(ergs == null || ergs.size() == 0){
        	return null;
        } else{
        	return ergs.get(0);
        }
    }
    
    public LoginToken findTokenByTokenstring(String token){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<LoginToken> criteria = cb.createQuery(LoginToken.class);
        Root<LoginToken> tokenRoot = criteria.from(LoginToken.class);
        criteria.select(tokenRoot).where(cb.equal(tokenRoot.get(LoginToken_.token), token));
        return em.createQuery(criteria).getSingleResult();
    }
    
    public String findUserIdByTokenId(String tokenId){
        LoginToken loginToken = em.find(LoginToken.class, tokenId);
        if(loginToken != null){
        	return loginToken.getUserId();
        } else{
        	return null;
        }
    }
    
    public void delete(LoginToken loginToken) throws Exception {
    	em.remove(loginToken);
    }
    
    public LoginToken login(String userId) throws Exception{
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.HOUR_OF_DAY, 2);

    	LoginToken loginToken = findTokenByUserId(userId);
    	if(loginToken == null){
    		loginToken = new LoginToken();
    		loginToken.setUserId(userId);
    	} 
		loginToken.setTimeOut(cal.getTime());
		loginToken.setToken(UUID.randomUUID().toString());

		loginToken = save(loginToken);
		return loginToken;
    }
    
    public boolean checkLoggedIn(String token){
    	LoginToken loginToken = findTokenByTokenstring(token);
    	if(loginToken != null){
    		Date now = new Date();
    		Date timeoutDate = loginToken.getTimeOut();
    		if(timeoutDate.after(now)){
    			return true;
    		} else{
    			return false;
    		}
    	} else{
    		return false;
    	}
    }
}
