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
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.persistence.NoResultException;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import javax.validation.ValidationException;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.jboss.as.quickstarts.kitchensink.model.Ort;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.service.OrtService;
import org.jboss.as.quickstarts.kitchensink.service.TerminService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.wrapper.OrtREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TerminREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.util.WrapperUtil;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a RESTful service to read/write the contents of the termins table.
 */
@Path("/orte")
@Stateless
public class OrtResourceRESTService {

    @Inject
    private Logger log;

    @Inject
    TerminService terminService;
    
    @Inject
    UserService userService;
    
    @Inject
    OrtService ortService;

    @GET
    @Path("/templatesByVerein")
    @Produces(MediaType.APPLICATION_JSON)
    public Response templatesByVerein(@QueryParam("vereinId") String vereinId) {
    	Response.ResponseBuilder builder = null;
    	List<Ort> vorlagen = ortService.findVorlagenByVerein(vereinId);
    	if(vorlagen == null || vorlagen.size() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", "NO VORLAGEN FOUND", ""));
    	} else{
    		List<OrtREST> orteRest = new ArrayList<OrtREST>(vorlagen.size());
    		for(int i=0; i<vorlagen.size(); i++){
    			OrtREST rest = WrapperUtil.createRest(vorlagen.get(i));
    			rest.vorlage = true;
    			orteRest.add(rest);
    		}
    		builder = Response.ok(Helper.createResponse("SUCCESS", "", orteRest));
    	}
        return builder.build();
    }
}
