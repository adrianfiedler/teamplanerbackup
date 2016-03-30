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

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.mail.MessagingException;
import javax.persistence.EntityExistsException;
import javax.persistence.TransactionRequiredException;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.jboss.as.quickstarts.kitchensink.model.Einladung;
import org.jboss.as.quickstarts.kitchensink.model.LoginToken;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.UserSettings;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;
import org.jboss.as.quickstarts.kitchensink.service.EinladungService;
import org.jboss.as.quickstarts.kitchensink.service.LoginTokenService;
import org.jboss.as.quickstarts.kitchensink.service.RollenService;
import org.jboss.as.quickstarts.kitchensink.service.TeamService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.service.ZusageService;
import org.jboss.as.quickstarts.kitchensink.util.CipherUtil;
import org.jboss.as.quickstarts.kitchensink.util.Constants;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.wrapper.UserREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.UserSettingsREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.util.WrapperUtil;

import de.masalis.teamplanner.mail.SendMail;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a RESTful service to read/write the contents of the users table.
 */
@Path("/user")
@Stateless
public class UserResourceRESTService {

    @Inject
    private Logger log;

    @Inject
    private Validator validator;

    @Inject
    UserService userService;
    
    @Inject
    SendMail sendMailService;
    
    @Inject
    TeamService teamService;
    
    @Inject
    EinladungService einladungService;
    
    @Inject
    RollenService rollenService;
    
    @Inject
    ZusageService zusageService;
    
    @Inject
    LoginTokenService loginTokenService;
    
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public Response listAllUsers() {
    	Response.ResponseBuilder builder = null;
    	List<User> results = userService.findAllOrderedByName();
    	List<UserREST> rest = new ArrayList<UserREST>();
    	for(User user : results){
    		rest.add(WrapperUtil.createLoginRest(user, null));
    	}
    	builder = Response.ok(Helper.createResponse("SUCCESS", "", rest));
        return builder.build();
    }

    @GET
    @Path("/{id:[0-9][0-9]*}")
    @Produces(MediaType.APPLICATION_JSON)
    public Response lookupUserById(@PathParam("id") String id) {
    	Response.ResponseBuilder builder = null;
        User user = userService.findById(id);
        if (user == null) {
        	builder = Response.ok(Helper.createResponse("ERROR", "USER ID NOT FOUND", null));
        } else{
        	builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createLoginRest(user, null)));
        }
        return builder.build();
    }

    @GET
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@QueryParam("email") String email, @QueryParam("password") String password, @QueryParam("invitationId") String invitationId){
    	Response.ResponseBuilder builder = null;
    	if(email != null && email.length() > 0){
    		User user = userService.findByEmail(email);
        	if(user == null || !user.getPasswort().equals(password)){
        		// kein user oder falsches Passwort
        		builder = Response.ok(Helper.createResponse("ERROR", "USER ID NOT FOUND OR WRONG PASSWORD", null));
        	} else{
        		if(!user.isAktiviert()){
        			builder = Response.ok(Helper.createResponse("ERROR", "USER NOT ACTIVATED", null));
        		} else{
        			// login erfolgreich
        			boolean invited = true;
        			if(invitationId != null && invitationId.length() > 0){
        				invited = handleInvitation(user, email, invitationId);
        			}
        			if(invited){
        				try {
        					LoginToken loginToken = loginTokenService.login(user.getId());
        					builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createLoginRest(user, loginToken)));
        				} catch (Exception e) {
        					e.printStackTrace();
        					builder = Response.ok(Helper.createResponse("ERROR", "COULD NOT CREATE LOGIN TOKEN", null));
        				}
        			}
        			else{
        				builder = Response.ok(Helper.createResponse("ERROR", "INVITATION FAILED", null));
        			}
        		}
        	}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
    	}
    	return builder.build();
    }
    
    private boolean handleInvitation(User user, String email, String encryptedInvitationId){
    	try{
	    	String invitationId = CipherUtil.decrypt(encryptedInvitationId);
	    	if(invitationId != null){
	    		Einladung einladung = einladungService.findById(invitationId);
	        	if(einladung != null && einladung.getStatus() == 0){
	        		Team team = einladung.getTeam();
	        		if(team != null){
	        			if(Helper.checkIfUserInTeam(user, team)){
	        				return true;
	        			}
	        			user.setVerein(team.getVerein());
	        			user = userService.update(user);
	        			TeamRolle rolle = new TeamRolle();
	        			rolle.setInTeam(true);
	        			rolle.setRolle(Constants.SPIELER_ROLE);
	        			rolle.setTeam(team);
	        			rolle.setUser(user);
	        			
	        			for(Termin t : team.getTermine()){
	        				Zusage zusage = new Zusage();
	        				zusage.setKommentar("");
	        				zusage.setStatus(0);
	        				zusage.setTermin(t);
	        				zusage.setUser(user);
	        				zusageService.save(zusage);
	        			}
	        			rollenService.save(rolle);
	        			einladungService.delete(einladung);
	        			user.getRollen().add(rolle);
	        			team.getRollen().add(rolle);
	        			return true;
	        		}
	        	}
	    	}
    	} catch (Exception e) {
			return false;
		}
    	return false;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@FormParam("email") String email, @FormParam("vorname") String vorname, @FormParam("name") String name, 
    		@FormParam("password") String password, @FormParam("invitationId") String invitationId) {
        Response.ResponseBuilder builder = null;
        try {
        	User existingUser = userService.findByEmail(email);
        	if(existingUser == null){
        		User user = new User();
                user.setName(name);
                user.setVorname(vorname);
                user.setEmail(email);
                user.setPasswort(password);
                user.setAktiviert(false);
                String aktivierToken = UUID.randomUUID().toString();
                user.setAktivierToken(aktivierToken);
                user.setUserSettings(new UserSettings());
                userService.register(user);
                
                String encodedToken = URLEncoder.encode(aktivierToken, "UTF-8");
                List<String> emailList = new ArrayList<String>();
                emailList.add(email);
                sendMailService.sendEmail(emailList, "Willkommen bei TeamPlanner", "Um deine Aktivierung abzuschließen, hier klicken: "
                		+ "<a href='"+Constants.ACTIVATION_URL+"?activationToken="+encodedToken+"'>Aktivierung</a>", null);
                
                boolean invited = true;
                if(invitationId != null && invitationId.length() > 0){
        			invited = handleInvitation(user, email, invitationId);
        		}
                if(invited){
                	builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
                } else{
            		builder = Response.ok(Helper.createResponse("ERROR", "INVITATION ERROR", null));
            	}
        	} else{
        		builder = Response.ok(Helper.createResponse("ERROR", "EMAIL EXISTS", null));
        	}
        } catch (EntityExistsException e) {
            builder = Response.ok(Helper.createResponse("ERROR", "USER EXISTS", null));
        } catch(IllegalArgumentException e){
        	builder = Response.ok(Helper.createResponse("ERROR", "ILLEGAL ARGUMENT", null));
        } catch(TransactionRequiredException e){
        	builder = Response.ok(Helper.createResponse("ERROR", "TRANSACTION REQUIRED", null));
        } catch (MessagingException e) {
			builder = Response.ok(Helper.createResponse("ERROR", "SEND MAIL ERROR", null));
		} catch (UnsupportedEncodingException e) {
			builder = Response.ok(Helper.createResponse("ERROR", "TOKEN ENCODE ERROR", null));
		}
        return builder.build();
    }
    
    @GET
    @Path("/activate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateUser(@QueryParam("activationToken") String activationToken) {
        Response.ResponseBuilder builder = null;
        try {
        	User existingUser = userService.findByActivationToken(activationToken);
        	if(existingUser != null){
        		existingUser.setAktiviert(true);
        		existingUser = userService.update(existingUser);
                
        		if(existingUser.getEmail() != null){
        			List<String> emailList = new ArrayList<String>();
                    emailList.add(existingUser.getEmail());
        			sendMailService.sendEmail(emailList, "Deine Aktivierung bei TeamPlanner", "Dein Accout wurde erfolgreich aktiviert. Du kannst dich jetz hier einloggen: <a href='"+Constants.LOGIN_URL+"'>TeamPlanner Login</a>", null);
        			builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
        		} else{
        			builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
        		}
        	} else{
        		builder = Response.ok(Helper.createResponse("ERROR", "NO USER", null));
        	}
        } catch (MessagingException e) {
        	builder = Response.ok(Helper.createResponse("ERROR", "MAIL SEND ERROR", null));
		} 
        return builder.build();
    }
    
    @GET
    @Path("/inviteMember")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inviteMember(@QueryParam("trainerId") String trainerId, @QueryParam("email") String email,
    		@QueryParam("vorname") String vorname, @QueryParam("name") String name, @QueryParam("teamId") String teamId){
        Response.ResponseBuilder builder = null;
        	User trainerUser = userService.findById(trainerId);
        	if(trainerUser != null){
        		Team team = teamService.findById(teamId);
        		if(team != null){
        			if(Helper.checkIfUserInTeamAndTrainer(trainerUser, team)){
        				if(email != null){
        					User existingEmailUser = userService.findByEmail(email);
        					if(existingEmailUser == null || !Helper.checkIfUserInTeam(existingEmailUser, team)){
        						if(vorname != null && name != null){
            						try {
            							Einladung einladung = einladungService.findByTeamUndEmail(team.getId(), email);
            							if(einladung == null){
            								einladung = new Einladung();
            								einladung.setEmail(email);
            								einladung.setName(name);
            								einladung.setVorname(vorname);
            								einladung.setInviter(trainerUser);
            								einladung.setStatus(0);
            								einladung.setTeam(team);
            								einladungService.save(einladung);
            							}
            							List<String> emailList = new ArrayList<String>();
            							String encryptedInvitationId = CipherUtil.encrypt(einladung.getId());
            							emailList.add(email);
                                    		sendMailService.sendEmail(emailList, "Du wurdest zu TeamPlaner eingeladen",
                                    		"Hallo " + vorname + " " + name + ",<br /><br />" +
                         					"Du wurdest von "+trainerUser.getVorname()+" "+trainerUser.getName()+" zum Team "+team.getName()+" in TeamPlaner eingeladen."
                        					+ "<br />Um diesem Team beizutreten, logge dich unter folgendem Link ein oder registriere dich hier: <a href='"+Constants.LOGIN_URL+"?id="+URLEncoder.encode(encryptedInvitationId, "UTF-8")+"'>TeamPlanner Login</a>", null);
                                    	builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
                                    } catch (MessagingException e) {
                                    	builder = Response.ok(Helper.createResponse("ERROR", "MAIL SEND ERROR", null));
                            		} catch (Exception e) {
                            			builder = Response.ok(Helper.createResponse("ERROR", "DATABASE SAVE ERROR", null));
    								} 
            					} else{
            						builder = Response.ok(Helper.createResponse("ERROR", "NO USER NAME OR SURNAME", null));
            					}
        					} else{
        						builder = Response.ok(Helper.createResponse("ERROR", "EMAIL ALREADY IN TEAM", null));
        					}
                		} else{
                			builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
                		}
        			} else{
        				builder = Response.ok(Helper.createResponse("ERROR", "USER NOT TRAINER OR NOT IN TEAM", null));
        			}
        		} else{
        			builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM FOUND", null));
        		}
        	} else{
        		builder = Response.ok(Helper.createResponse("ERROR", "NO TRAINER USER", null));
        	}
        return builder.build();
    }
    
    @POST
    @Path("/changePassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePassword(@FormParam("userId") String userId, @FormParam("oldPw") String oldPw, @FormParam("newPw") String newPw) {
        Response.ResponseBuilder builder = null;
    	User existingUser = userService.findById(userId);
    	if(existingUser != null){
    		if(existingUser.getPasswort().equals(oldPw)){
    			existingUser.setPasswort(newPw);
    			userService.update(existingUser);
    			builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    		} else{
    			builder = Response.ok(Helper.createResponse("ERROR", "WRONG PASSWORD", null));
    		}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO USER", null));
    	}
        return builder.build();
    }
    
    @POST
    @Path("/resetPassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(@FormParam("email") String email) {
        Response.ResponseBuilder builder = null;
    	User existingUser = userService.findByEmail(email);
    	if(existingUser != null){
			if(existingUser.getEmail() != null){
				if(existingUser.isAktiviert()){
					String newPw = UUID.randomUUID().toString().substring(0, 12);
					existingUser.setPasswort(newPw);
					userService.update(existingUser);
					List<String> emailList = new ArrayList<String>();
					emailList.add(existingUser.getEmail());
					try {
						sendMailService.sendEmail(emailList, "Dein neues Teamplaner Passwort", "Hallo "+existingUser.getVorname()+",<br /><br />Dein neues Passwort lautet: "+newPw+"<br /><br />Du kannst dich damit unter <a href=\""+Constants.LOGIN_URL+"\">Teamplaner login</a> einloggen.", null);
						builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
					} catch (MessagingException e) {
						builder = Response.ok(Helper.createResponse("ERROR", "MAIL SEND ERROR", null));
					}
				} else{
	    			builder = Response.ok(Helper.createResponse("ERROR", "USER MAIL NOT ACTIVATED", null));
	    		}
    		} else{
    			builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
    		}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO USER", null));
    	}
        return builder.build();
    }
    
    @POST
    @Path("/setUserSettings")
    @Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
    public Response setUserSettings(UserSettingsREST userSettings) {
    	Response.ResponseBuilder builder = null;
    	if(userSettings != null){
    		if(userSettings.userId != null){
    			User user = userService.findById(userSettings.userId);
    			if(user == null){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO USER FOUND", null));
    			} else{
    				UserSettings newUserSettings = new UserSettings();
    				newUserSettings.setMontagsAbsagen(userSettings.montagsAbsagen);
    				newUserSettings.setDienstagsAbsagen(userSettings.dienstagsAbsagen);
    				newUserSettings.setMittwochsAbsagen(userSettings.mittwochsAbsagen);
    				newUserSettings.setDonnerstagsAbsagen(userSettings.donnerstagsAbsagen);
    				newUserSettings.setFreitagsAbsagen(userSettings.freitagsAbsagen);
    				newUserSettings.setSamstagsAbsagen(userSettings.samstagsAbsagen);
    				newUserSettings.setSonntagsAbsagen(userSettings.sonntagsAbsagen);
    				user.setUserSettings(newUserSettings);
    				user = userService.update(user);
    				builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    			}
    		} else{
    			builder = Response.ok(Helper.createResponse("ERROR", "NO USER ID SET", null));
    		}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO SETTINGS", null));
    	}
    	return builder.build();
    }
    
    @GET
    @Path("/getUserSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserSettings(@QueryParam("userId") String userId) {
    	Response.ResponseBuilder builder = null;
        User user = userService.findById(userId);
        if (user == null) {
        	builder = Response.ok(Helper.createResponse("ERROR", "USER ID NOT FOUND", null));
        } else{
        	if(user.getUserSettings() == null){
        		builder = Response.ok(Helper.createResponse("ERROR", "NO USER SETTINGS FOUND", null));
        	} else{
        		builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(user.getUserSettings(), user.getId())));
        	}
        }
        return builder.build();
    }
    
    @GET
    @Path("/resendActivation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resendActivation(@QueryParam("email") String email) {
    	Response.ResponseBuilder builder = null;
    	if(email != null && email.length() > 0){
    		User user = userService.findByEmail(email);
			if (user == null || user.getAktivierToken() == null) {
				builder = Response.ok(Helper.createResponse("ERROR", "USER NOT FOUND", null));
			} else{
				if(user.isAktiviert()){
					builder = Response.ok(Helper.createResponse("ERROR", "USER ALREADY ACTIVATED", null));
				} else{
					try {
						String aktivierToken = user.getAktivierToken();
						String encodedToken = URLEncoder.encode(aktivierToken, "UTF-8");
						List<String> emailList = new ArrayList<String>();
						emailList.add(user.getEmail());
						sendMailService.sendEmail(emailList, "Willkommen bei TeamPlanner", "Um deine Aktivierung abzuschließen, hier klicken: "
								+ "<a href='"+Constants.ACTIVATION_URL+"?activationToken="+encodedToken+"'>Aktivierung</a>", null);
						builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
					} catch (MessagingException e) {
						return Response.ok(Helper.createResponse("ERROR", "SEND MAIL ERROR", null)).build();
					} catch (UnsupportedEncodingException e) {
						return Response.ok(Helper.createResponse("ERROR", "ENCODING ERROR", null)).build();
					}
				}
			}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
    	}
        return builder.build();
    }
    		
}
