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
import java.util.Iterator;
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
import org.jboss.as.quickstarts.kitchensink.model.TeamMailSettings;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.TeamSettings;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;
import org.jboss.as.quickstarts.kitchensink.service.EinladungService;
import org.jboss.as.quickstarts.kitchensink.service.LoginTokenService;
import org.jboss.as.quickstarts.kitchensink.service.RollenService;
import org.jboss.as.quickstarts.kitchensink.service.TeamService;
import org.jboss.as.quickstarts.kitchensink.service.TerminService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.service.VereinService;
import org.jboss.as.quickstarts.kitchensink.service.ZusageService;
import org.jboss.as.quickstarts.kitchensink.util.Constants;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.util.ResponseTypes;
import org.jboss.as.quickstarts.kitchensink.wrapper.TerminREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.request.TeamSettingsRequestREST;
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
    
    @Inject 
    ZusageService zusageService;
    
    @Inject
    VereinService vereinService;
    
    @Inject
    LoginTokenService loginTokenService;
    
    @Inject 
    RollenService rollenService;

    //liste des Teams zur Verwaltung
    @GET
    @Path("/teamListById")
    @Produces(MediaType.APPLICATION_JSON)
    public Response lookupTeamListById(@QueryParam("teamId") String teamId, @QueryParam("userId") String token) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if(user == null){
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
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
    	}
    	return builder.build();
    }
    
    @POST
    @Path("/sendMail")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendMailToTeam(@FormParam("teamId") String teamId, @FormParam("subject") String subject, 
    		@FormParam("message") String message, @FormParam("userIds") List<String> userIds, @FormParam("userId") String token) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if (user == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else {
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
    	    				User userList = userService.findById(userId);
    	    				if(userList != null && Helper.checkIfUserInTeam(userList, team)){
    	    					String email = userList.getEmail();
    	    					toList.add(email);
    	    				}
    	    			}
    	    			if(toList.size() == 0){
    	    				builder = Response.ok(Helper.createResponse("ERROR", "RECIPIENTS = 0", null));
    	    			} else{
    	    				try {
    	    					sendMail.sendEmail(toList, subject, message, Constants.MAIL_SENDER);
    	    					builder = Response.ok(Helper.createResponse("SUCCESS", "SENT TO "+toList.size()+" recipients", null));
    	    				} catch (MessagingException e) {
    	    					builder = Response.ok(Helper.createResponse("ERROR", "SEND MAIL ERROR", null));
    	    					e.printStackTrace();
    	    				}
    	    			}
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
    					einladung.getInviter().getEinladungen().remove(einladung);
    					einladung.getTeam().getEinladungen().remove(einladung);
    					einladung.setInviter(null);
    					einladung.setTeam(null);
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
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createTeam(@FormParam("userId") String token, @FormParam("teamName") String teamName) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		if(teamName == null || teamName.length() == 0){
    			builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM NAME SET", null));
    		} else{
    			User user = loginTokenService.getUserIfLoggedIn(token);
    			if(user == null){
    				builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    			} else{
    				if(user.isAdmin() == false){
    					builder = Response.ok(Helper.createResponse("ERROR", "USER NOT ADMIN", null));
    				} else{
    					Verein verein = user.getVerein();
    					if(verein.getVereinsTeams().size() >= verein.getGekaufteTeams()){
    						builder = Response.ok(Helper.createResponse("ERROR", "NO MORE FREE TEAMS", null));
    					} else{
    						Team team = new Team();
    						team.setName(teamName);
    						team.setVerein(verein);
    						verein.getVereinsTeams().add(team);
    						TeamMailSettings teamMailSettings = new TeamMailSettings();
    						teamMailSettings.setHoursBeforeTrainerReminder(2);
    						teamMailSettings.setMailText("");
    						teamMailSettings.setShowIntroduction(true);
    						teamMailSettings.setShowMailText(true);
    						teamMailSettings.setTeam(team);
    						team.setWeeklyTeamMailSettings(teamMailSettings);
    						
    						TeamSettings teamSettings = new TeamSettings();
    						teamSettings.setTrainerMussZusagen(false);
    						teamSettings.setTeam(team);
    						team.setTeamSettings(teamSettings);
    						
							team = teamService.save(team);
							verein = vereinService.save(verein);
							builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(team)));
    					}
    				}
    			}
    		}
    	}
    	return builder.build();
    }
    
    @POST
    @Path("/deleteTeam")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteTeam(@FormParam("userId") String token, @FormParam("teamId") String teamId) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		if(teamId == null || teamId.length() == 0){
    			builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM ID SET", null));
    		} else{
    			User user = loginTokenService.getUserIfLoggedIn(token);
    			if(user == null){
    				builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    			} else{
    				Team team = teamService.findById(teamId);
    				if(team == null){
    					builder = Response.ok(Helper.createResponse("ERROR", "TEAM NOT FOUND", null));
    				} else{
    					if(user.isAdmin() == false){
    						builder = Response.ok(Helper.createResponse("ERROR", "USER NOT ADMIN", null));
    					} else{
    						Verein verein = user.getVerein();
    						//verein.getVereinsTeams().remove(team);
    						try {
    							teamService.delete(team);
    							verein = vereinService.save(verein);
    							builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    						} catch (Exception e) {
    							builder = Response.ok(Helper.createResponse("ERROR", "DELETE TEAM FAILED", null));
    						}
    					}
    				}
    			}
    		}
    	}
    	return builder.build();
    }
    
    @GET
    @Path("/rename")
    @Produces(MediaType.APPLICATION_JSON)
    public Response renameTeam(@QueryParam("teamId") String teamId, @QueryParam("userId") String token, 
    		@QueryParam("newName") String newName) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	}  else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if(user == null){
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else{
    			Team team = teamService.findById(teamId);
    			if(team == null){
    				builder = Response.ok(Helper.createResponse("ERROR", "TEAM NOT FOUND", null));
    			} else{
    				if(newName == null || newName.length() == 0){
    					builder = Response.ok(Helper.createResponse("ERROR", "NO NEW NAME", null));
    				} else{
    					if(!team.getVerein().getId().equals(user.getVerein().getId())){
    						builder = Response.ok(Helper.createResponse("ERROR", "USER AND TEAM NOT SAME VEREIN", null));
    					} else{
    						if(Helper.checkIfUserInTeamAndTrainer(user, team) || user.isAdmin()){
    							team.setName(newName);
    							teamService.save(team);
    							builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    						} else{
    							builder = Response.ok(Helper.createResponse("ERROR", "USER NOT TRAINER IN TEAM AND NOT ADMIN", null));
    						}
    					}
    				}
    			}
    		}
    	}
    	return builder.build();
    }
    
    @POST
	@Path("/removeUser")
	@Produces(MediaType.APPLICATION_JSON)
	public Response removeUserFromTeam(@FormParam("userId") String token,
			@FormParam("toRemoveId") String toRemoveId, @FormParam("teamId") String teamId) {
		Response.ResponseBuilder builder = null;
		if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if (user == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else {
    			if(toRemoveId == null || toRemoveId.length() == 0){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO TO REMOVE ID SET", null));
    			} else{
    				User toRemoveUser = userService.findById(toRemoveId);
    				if(toRemoveUser == null){
    					builder = Response.ok(Helper.createResponse("ERROR", "USER TO REMOVE NOT FOUND", null));
    				} else{
    					if(teamId == null || teamId.length() == 0){
    						builder = Response.ok(Helper.createResponse("ERROR", "TEAM ID NOT SET", null));
    					} else{
    						Team team = teamService.findById(teamId);
    						if(team == null){
    							builder = Response.ok(Helper.createResponse("ERROR", "TEAM NOT FOUND", null));
    						} else{
    							if(Helper.checkIfUserInTeamAndTrainer(user, team) || user.isAdmin()){
    								if(!Helper.checkIfUserInTeam(toRemoveUser, team)){
    									builder = Response.ok(Helper.createResponse("ERROR", "TO REMOVE USER NOT IN TEAM", null));
    								} else{
    									TeamRolle rolleToRemove = null;
    									for(TeamRolle rolle : team.getRollen()){
    										if(rolle.getUser().getId().equals(toRemoveUser.getId())){
    											rolleToRemove = rolle;
    											break;
    										}
    									}
    									if(rolleToRemove != null){
    										team.getRollen().remove(rolleToRemove);
    										rolleToRemove.setTeam(null);
    										rolleToRemove.setUser(null);
    										toRemoveUser.getRollen().remove(rolleToRemove);
    										rollenService.delete(rolleToRemove);
    										
    										//zukuenftige Terminzusagen loeschen
    										Date now = new Date();
    										for (Iterator<Zusage> iterator = toRemoveUser.getZusagen().iterator(); iterator.hasNext();) {
    										    Zusage zusage = iterator.next();
    										    Termin termin = zusage.getTermin();
    										    if(termin.getDatum().after(now)){
    										    	iterator.remove();
    										    	zusage.setUser(null);
    										    	zusage.setTermin(null);
    										    	termin.getZusagen().remove(zusage);
    										    	zusageService.delete(zusage);
    										    }
    										}
    										
    										//wenn sonst keine Teams dann aus Verein loeschen
    										if(toRemoveUser.getRollen().size() == 0){
    											toRemoveUser.getVerein().getUser().remove(toRemoveUser);
    											toRemoveUser.setVerein(null);
    										}
    										builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    									} else{
    										builder = Response.ok(Helper.createResponse("ERROR", "COULD NOT FIND ROLLE", null));
    									}
    								}
    							} else{
    								builder = Response.ok(Helper.createResponse("ERROR", "USER NOT IN TEAM OR NOT TRAINER AND NOT ADMIN", null));
    							}
    						}
    					}
    				}
    			}
    		}
    	}
		return builder.build();
	}
    
    @POST
	@Path("/setWeeklyMailSettings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setWeeklyMailSettings(@FormParam("userId") String token,
			@FormParam("mailText") String mailText, @FormParam("teamId") String teamId, @FormParam("showAnleitung") Boolean showAnleitung) {
		Response.ResponseBuilder builder = null;
		if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if (user == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else {
    			if(teamId == null || teamId.length() == 0){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM ID SET", null));
    			} else{
    				Team team = teamService.findById(teamId);
    				if(team == null){
    					builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM FOUND", null));
    				} else{
    					if(!Helper.checkIfUserInTeamAndTrainer(user, team)){
    						builder = Response.ok(Helper.createResponse("ERROR", "USER NOT IN TEAM AND TRAINER", null));
    					} else{
    						if(mailText == null || mailText.length() == 0){
    							builder = Response.ok(Helper.createResponse("ERROR", "NO MAIL TEXT SET", null));
    						} else{
    							if(showAnleitung == null){
    								builder = Response.ok(Helper.createResponse("ERROR", "NO SHOW ANLEITUNG SET", null));
    							} else{
    								TeamMailSettings teamMailSettings = teamService.findMailSettingsByTeamId(teamId);
    								if(teamMailSettings == null){
    									builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM MAIL SETTINGS FOUND", null));
    								} else{
    									teamMailSettings.setShowIntroduction(showAnleitung);
    									teamMailSettings.setMailText(mailText);
    									teamService.saveTeamMailSettings(teamMailSettings);
    									builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    	}
		return builder.build();
	}
    
    @GET
	@Path("/getTeamMailSettings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTeamMailSettings(@QueryParam("userId") String token, @QueryParam("teamId") String teamId) {
		Response.ResponseBuilder builder = null;
		if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if (user == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else {
    			if(!user.getVerein().getModule().isMailModul()){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO MAIL MODULE", null));
    			} else{
    				if(teamId == null || teamId.length() == 0){
    					builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM ID SET", null));
    				} else{
    					TeamMailSettings teamMailSettings = teamService.findMailSettingsByTeamId(teamId);
    					if(teamMailSettings == null){
    						builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM MAIL SETTINGS FOUND", null));
    					} else{
    						builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(teamMailSettings)));
    					}
    				}
    			}
    		}
    	}
		return builder.build();
	}
    
    @POST
	@Path("/setTeamSettings")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setTeamSettings(TeamSettingsRequestREST teamSettingsRequest) {
		Response.ResponseBuilder builder = null;
		if(teamSettingsRequest.userId == null || teamSettingsRequest.userId.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(teamSettingsRequest.userId);
    		if (user == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else {
    			if(teamSettingsRequest.teamId == null || teamSettingsRequest.teamId.length() == 0){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM ID SET", null));
    			} else{
    				Team team = teamService.findById(teamSettingsRequest.teamId);
    				if(team == null){
    					builder = Response.ok(Helper.createResponse("ERROR", "TEAM NOT FOUND", null));
    				} else{
    					if(!Helper.checkIfUserInTeamAndTrainer(user, team)){
    						builder = Response.ok(Helper.createResponse("ERROR", "USER NOT IN TEAM AND TRAINER", null));
    					} else{
    						TeamSettings teamSettings = teamService.findTeamSettingsByTeamId(teamSettingsRequest.teamId);
    						if(teamSettings == null){
    							builder = Response.ok(Helper.createResponse("ERROR", "NO TEAMSETTINGS FOUND", null));
    						} else{
    							teamSettings.setTrainerMussZusagen(teamSettingsRequest.trainerMussZusagen);
    							teamService.saveTeamSettings(teamSettings);
    							builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    						}
    					}
    				}
    			}
    		}
    	}
		return builder.build();
	}
    
    @GET
   	@Path("/getTeamSettings")
   	@Produces(MediaType.APPLICATION_JSON)
   	public Response getTeamSettings(@QueryParam("userId") String token, @QueryParam("teamId") String teamId) {
   		Response.ResponseBuilder builder = null;
   		if(token == null || token.length() == 0){
       		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
       	} else{
       		User user = loginTokenService.getUserIfLoggedIn(token);
       		if (user == null) {
       			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
       		} else {
   				if(teamId == null || teamId.length() == 0){
   					builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM ID SET", null));
   				} else{
   					TeamSettings teamSettings = teamService.findTeamSettingsByTeamId(teamId);
   					if(teamSettings == null){
   						builder = Response.ok(Helper.createResponse("ERROR", "NO TEAM MAIL SETTINGS FOUND", null));
   					} else{
   						builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(teamSettings)));
   					}
   				}
       		}
       	}
   		return builder.build();
   	}
}
