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
import org.jboss.as.quickstarts.kitchensink.model.PushToken;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.VereinModule;
import org.jboss.as.quickstarts.kitchensink.service.EinladungService;
import org.jboss.as.quickstarts.kitchensink.service.LoginTokenService;
import org.jboss.as.quickstarts.kitchensink.service.PushTokenService;
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
@Path("/pushToken")
@Stateless
public class PushTokenResourceRESTService {

    @Inject
    private Logger log;

    @Inject
    LoginTokenService loginTokenService;
    
    @Inject
    PushTokenService pushTokenService;
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response savePushToken(@FormParam("pushToken") String pushToken, @FormParam("os") String os, 
    		@FormParam("userId") String token) {
    	Response.ResponseBuilder builder = null;
    	if(token == null){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else {
			User user = loginTokenService.getUserIfLoggedIn(token);
			if(user == null){
				builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
			} else{
				if(pushToken == null || pushToken.length() == 0){
					builder = Response.ok(Helper.createResponse("ERROR", "NO PUSH TOKEN SET", null));
				} else{
					if(os == null || os.length() == 0){
						builder = Response.ok(Helper.createResponse("ERROR", "NO OS SET", null));
					} else{
						PushToken pushTokenObj = pushTokenService.findTokenByOSAndUser(os, user.getId());
						if(pushTokenObj == null){
							pushTokenObj = new PushToken();
						}
						pushTokenObj.setOs(os);
						pushTokenObj.setToken(pushToken);
						pushTokenObj.setUser(user);
						pushTokenObj = pushTokenService.save(pushTokenObj);
						builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
					}
				}
			}
    	}
    	return builder.build();
    }
}
