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
import org.jboss.as.quickstarts.kitchensink.model.Ort;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.VereinModule;
import org.jboss.as.quickstarts.kitchensink.service.EinladungService;
import org.jboss.as.quickstarts.kitchensink.service.LoginTokenService;
import org.jboss.as.quickstarts.kitchensink.service.TeamService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.service.VereinService;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.util.ResponseTypes;
import org.jboss.as.quickstarts.kitchensink.wrapper.util.WrapperUtil;

import de.masalis.teamplanner.mail.SendMail;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a RESTful service to read/write the contents of the termins table.
 */
@Path("/verein")
@Stateless
public class VereinResourceRESTService {

    @Inject
    private Logger log;

    @Inject
    TeamService teamService;
    
    @Inject
    UserService userService;
    
    @Inject
    VereinService vereinService;
    
    @Inject 
    EinladungService einladungService;
    
    @Inject
    LoginTokenService loginTokenService;

    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createVerein(@FormParam("userId") String token, @FormParam("vereinName") String vereinName, 
    		@FormParam("teamAnzahl") int teamAnzahl) {
    	Response.ResponseBuilder builder = null;
    	if(token == null){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		if(vereinName == null || vereinName.length() == 0){
    			builder = Response.ok(Helper.createResponse("ERROR", "NO VEREIN NAME SET", null));
    		} else{
    			if(teamAnzahl == 0){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO ANZAHL SET", null));
    			} else{
    				User user = loginTokenService.getUserIfLoggedIn(token);
    				if(user == null){
    					builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    				} else{
    					Verein verein = new Verein();
    					verein.setName(vereinName);
    					verein.setGekaufteTeams(teamAnzahl);
    					List<User> vereinUser = new ArrayList<User>();
    					vereinUser.add(user);
    					verein.setUser(vereinUser);
    					verein.setOrte(new ArrayList<Ort>());
    					verein.setVereinsTeams(new ArrayList<Team>());
    					VereinModule module = new VereinModule();
    					module.setMailModul(true);
    					module.setVerein(verein);
    					verein.setModule(module);
    					user.setAdmin(true);
    					user.setVerein(verein);
    					
						verein = vereinService.save(verein);
						builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(verein)));
    				}
    			}
    		}
    	}
    	return builder.build();
    }
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response getVereinById(@QueryParam("vereinId") String vereinId, @QueryParam("userId") String token) {
    	Response.ResponseBuilder builder = null;
    	if(token == null){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if(user == null){
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else{
    			if(user.isAdmin() == false){
    				builder = Response.ok(Helper.createResponse("ERROR", "USER NOT ADMIN", null));
    			} else{
    				if(vereinId == null){
    					builder = Response.ok(Helper.createResponse("ERROR", "NO VEREIN ID", null));
    				} else{
    					Verein verein = vereinService.findById(vereinId);
    					if(verein == null){
    						builder = Response.ok(Helper.createResponse("ERROR", "NO VEREIN FOUND", null));
    					} else{
    						builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createAdminRest(verein)));
    					}
    				}
    			}
    		}
    	}
    	return builder.build();
    }
}
