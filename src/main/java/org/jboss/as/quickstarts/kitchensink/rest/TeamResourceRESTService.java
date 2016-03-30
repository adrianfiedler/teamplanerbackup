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
package org.jboss.as.quickstarts.kitchensink.rest;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.as.quickstarts.kitchensink.model.Einladung;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.service.EinladungService;
import org.jboss.as.quickstarts.kitchensink.service.TeamService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.wrapper.util.WrapperUtil;

import de.masalis.teamplanner.mail.SendMail;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a RESTful service to read/write the contents of the termins table.
 */
@Path("/team")
@Stateless
public class TeamResourceRESTService {

    @Inject
    private Logger log;

    @Inject
    TeamService teamService;
    
    @Inject
    UserService userService;
    
    @Inject
    SendMail sendMail;
    
    @Inject 
    EinladungService einladungService;

    //liste des Teams zur Verwaltung
    @GET
    @Path("/teamListById")
    @Produces(MediaType.APPLICATION_JSON)
    public Response lookupTeamListById(@QueryParam("teamId") String teamId, @QueryParam("userId") String userId) {
    	Response.ResponseBuilder builder = null;
    	User user = userService.findById(userId);
    	if(user == null){
    		builder = Response.ok(Helper.createResponse("ERROR", "USER NOT FOUND", null));
    	} else{
    		boolean inTeam = false;
    		for(TeamRolle rolle : user.getRollen()){
    			if(rolle.getTeam().getId().equals(teamId)){
    				inTeam = true;
    				break;
    			}
    		}
    		if(!inTeam){
    			builder = Response.ok(Helper.createResponse("ERROR", "USER NOT IN TEAM", null));
    		} else{
    			Team team = teamService.findById(teamId);
    			if (team == null) {
    				builder = Response.ok(Helper.createResponse("ERROR", "TEAM NOT FOUND", null));
    			} else{
    				List<Einladung> einladungen = null;
    				if(Helper.checkIfUserInTeamAndTrainer(user, team)){
    					einladungen = einladungService.findByTeamId(team.getId());
    				}
    				builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createTeamListRest(team, user, einladungen)));
    			}
    		}
    	}
    	return builder.build();
    }
    
    @POST
    @Path("/sendMail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMailToTeam(@FormParam("teamId") String teamId, @FormParam("subject") String subject, 
    		@FormParam("message") String message, @FormParam("userIds") List<String> userIds) {
    	Response.ResponseBuilder builder = null;
    	if(userIds == null || teamId == null || userIds.size() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM OR USERS", null));
    	} else{
    		String listString = "";
    		for (String s : userIds)
    		{
    		    listString += s + "\t";
    		}
    		log.log(Level.INFO, "userIds: "+listString);
    		Team team = teamService.findById(teamId);
    		if(team == null){
    			builder = Response.ok(Helper.createResponse("ERROR", "TEAM NOT FOUND", null));
    		} else{
    			List<String> toList = new ArrayList<String>(userIds.size());
    			for(String userId : userIds){
    				User user = userService.findById(userId);
    				if(user != null && Helper.checkIfUserInTeam(user, team)){
    					String email = user.getEmail();
    					toList.add(email);
    				}
    			}
    			if(toList.size() == 0){
    				builder = Response.ok(Helper.createResponse("ERROR", "RECIPIENTS = 0", null));
    			} else{
    				try {
    					sendMail.sendEmail(toList, subject, message, "noreply-"+team.getName());
    					builder = Response.ok(Helper.createResponse("SUCCESS", "SENT TO "+toList.size()+" recipients", null));
    				} catch (MessagingException e) {
    					builder = Response.ok(Helper.createResponse("ERROR", "SEND MAIL ERROR", null));
    					e.printStackTrace();
    				}
    			}
    		}
    	}
    	return builder.build();
    }
    
    @POST
    @Path("/deleteInvitation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteInvitation(@FormParam("teamId") String teamId, @FormParam("email") String email) {
    	Response.ResponseBuilder builder = null;
    	if(teamId == null || email == null){
    		builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM OR EMAIL", null));
    	} else{
    		Team team = teamService.findById(teamId);
    		if(team == null){
    			builder = Response.ok(Helper.createResponse("ERROR", "TEAM NOT FOUND", null));
    		} else{
    			Einladung einladung = einladungService.findByTeamUndEmail(teamId, email);
    			if(einladung == null){
    				builder = Response.ok(Helper.createResponse("ERROR", "EINLADUNG NOT FOUND", null));
    			} else{
    				try {
						einladungService.delete(einladung);
						builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
					} catch (Exception e) {
						builder = Response.ok(Helper.createResponse("ERROR", "EINLADUNG DELETE ERROR", null));
						e.printStackTrace();
					}
    			}
    		}
    	}
    	return builder.build();
    }
}
