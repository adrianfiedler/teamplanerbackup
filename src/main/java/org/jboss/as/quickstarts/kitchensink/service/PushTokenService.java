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

import org.jboss.as.quickstarts.kitchensink.model.PushToken;
import org.jboss.as.quickstarts.kitchensink.model.PushToken_;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.User_;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class PushTokenService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    public PushToken save(PushToken pushToken) {
        return em.merge(pushToken);
    }
    
    public PushToken findById(String id) {
        return em.find(PushToken.class, id);
    }
    
    public List<PushToken> findTokenByOS(String os){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PushToken> criteria = cb.createQuery(PushToken.class);
        Root<PushToken> token = criteria.from(PushToken.class);
        criteria.select(token).where(cb.equal(token.get(PushToken_.os), os));
        List<PushToken> ergs = em.createQuery(criteria).getResultList();
        return ergs;
    }
    
    public PushToken findTokenByOSAndUser(String os, String userId){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<PushToken> criteria = cb.createQuery(PushToken.class);
        Root<PushToken> token = criteria.from(PushToken.class);
        Join<PushToken, User> user = token.join(PushToken_.user);
        criteria.select(token).where(cb.and(
        		cb.equal(token.get(PushToken_.os), os),
        		cb.equal(user.get(User_.id), userId)
        ));
        List<PushToken> ergs = em.createQuery(criteria).getResultList();
        if(ergs == null || ergs.size() == 0){
        	return null;
        } else{
        	return ergs.get(0);
        }
    }
    
    public void delete(PushToken pushToken) throws Exception {
    	em.remove(pushToken);
    }
}
