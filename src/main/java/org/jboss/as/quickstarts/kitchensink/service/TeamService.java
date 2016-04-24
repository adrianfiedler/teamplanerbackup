package org.jboss.as.quickstarts.kitchensink.service;

import java.util.List;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.persistence.EntityManager;
import javax.persistence.TypedQuery;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Join;
import javax.persistence.criteria.Root;

import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamMailSettings;
import org.jboss.as.quickstarts.kitchensink.model.TeamMailSettings_;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.TeamSettings;
import org.jboss.as.quickstarts.kitchensink.model.TeamSettings_;
import org.jboss.as.quickstarts.kitchensink.model.Team_;

// The @Stateless annotation eliminates the need for manual transaction demarcation
@Stateless
public class TeamService {

    @Inject
    private Logger log;

    @Inject
    private EntityManager em;

    public void register(Team Team) {
        em.persist(Team);
    }
    
    public Team save(Team Team) {
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
    
    public TeamMailSettings findMailSettingsByTeamId(String teamId){
    	CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TeamMailSettings> criteria = cb.createQuery(TeamMailSettings.class);
        Root<TeamMailSettings> mailSettings = criteria.from(TeamMailSettings.class);
        Join<TeamMailSettings, Team> team = mailSettings.join(TeamMailSettings_.team);
        criteria.select(mailSettings).where(cb.equal(team.get(Team_.id), teamId));
        List<TeamMailSettings> ergs = em.createQuery(criteria).getResultList();
        if(ergs == null || ergs.size() == 0){
        	return null;
        } else{
        	return ergs.get(0);
        }
    }
    
    public TeamMailSettings findTeamMailSettingsById(String id) {
        return em.find(TeamMailSettings.class, id);
    }
    
    public TeamMailSettings saveTeamMailSettings(TeamMailSettings teamMailSettings) {
        return em.merge(teamMailSettings);
    }

	public TeamSettings findTeamSettingsByTeamId(String teamId) {
		CriteriaBuilder cb = em.getCriteriaBuilder();
        CriteriaQuery<TeamSettings> criteria = cb.createQuery(TeamSettings.class);
        Root<TeamSettings> teamSettings = criteria.from(TeamSettings.class);
        Join<TeamSettings, Team> team = teamSettings.join(TeamSettings_.team);
        criteria.select(teamSettings).where(cb.equal(team.get(Team_.id), teamId));
        List<TeamSettings> ergs = em.createQuery(criteria).getResultList();
        if(ergs == null || ergs.size() == 0){
        	return null;
        } else{
        	return ergs.get(0);
        }
	}

	public TeamSettings saveTeamSettings(TeamSettings teamSettings) {
		return em.merge(teamSettings);
	}
}
