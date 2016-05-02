package org.jboss.as.quickstarts.kitchensink.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.EJBContext;
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
import org.jboss.as.quickstarts.kitchensink.service.TerminService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.service.ZusageService;
import org.jboss.as.quickstarts.kitchensink.util.CipherUtil;
import org.jboss.as.quickstarts.kitchensink.util.Constants;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.util.MailTexts;
import org.jboss.as.quickstarts.kitchensink.util.ResponseTypes;
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
    
    @Resource
    private EJBContext context;
    
    @Inject TerminService terminService;
    
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
    					LoginToken loginToken = login(user);
    					builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createLoginRest(user, loginToken)));
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
    
    private LoginToken login(User user){
    	Calendar cal = Calendar.getInstance();
    	cal.add(Calendar.HOUR_OF_DAY, 2);

    	if(user == null){
    		return null;
    	} else{
    		LoginToken loginToken = loginTokenService.findTokenByUserId(user.getId());
        	if(loginToken == null){
        		loginToken = new LoginToken();
        	} 
    		loginToken.setTimeOut(cal.getTime());
    		loginToken.setToken(UUID.randomUUID().toString());
    		loginToken.setUser(user);

    		loginToken = loginTokenService.save(loginToken);
    		return loginToken;
    	}
    }
    
    private boolean handleInvitation(User user, String email, String encryptedInvitationId){
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
        			TeamRolle rolle = new TeamRolle();
        			rolle.setInTeam(true);
        			rolle.setRolle(einladung.getRolle());
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
        			einladung.setInviter(null);
        			einladung.setTeam(null);
        			team.getEinladungen().remove(einladung);
        			einladungService.delete(einladung);
        			user.getRollen().add(rolle);
        			user.getEinladungen().remove(einladung);
        			user = userService.update(user);
        			return true;
        		}
        	}
    	}
    	return false;
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@FormParam("email") String email, @FormParam("vorname") String vorname, @FormParam("name") String name, 
    		@FormParam("password") String password, @FormParam("invitationId") String invitationId) throws MessagingException, UnsupportedEncodingException {
        Response.ResponseBuilder builder = null;
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
                UserSettings userSettings = new UserSettings();
                user.setUserSettings(userSettings);
                user.setRollen(new ArrayList<TeamRolle>());
                user.setEinladungen(new ArrayList<Einladung>());
                user.setZusagen(new ArrayList<Zusage>());
                userService.register(user);
                
                String encodedToken = URLEncoder.encode(aktivierToken, "UTF-8");
                List<String> emailList = new ArrayList<String>();
                emailList.add(email);
                try{
                	sendMailService.sendEmail(emailList, "Willkommen beim TeamPlaner", "Hallo "+vorname+",<br /><br />"
                			+ "herzlich willkommen beim Teamplaner!<br />"
                			+ "Vielen Dank für die Registrierung.<br />"
                			+ "Bitte aktiviere deine Email mit Klick auf folgenden Link: "
                		+ "<p><a href='"+Constants.ACTIVATION_URL+"?activationToken="+encodedToken+"'>Aktivierung</a></p>"
                				+ "Dein Teamplaner-Team<br />"
                				+ MailTexts.SUPPORT_TEXT, null);
                } catch(MessagingException messagingException){
                	// could not send mail : rollback
                	context.setRollbackOnly();
                	return Response.ok(Helper.createResponse("ERROR", "SEND MAIL ERROR", null)).build();
                }
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
        return builder.build();
    }
    
    @GET
    @Path("/activate")
    @Produces(MediaType.APPLICATION_JSON)
    public Response activateUser(@QueryParam("activationToken") String activationToken) throws MessagingException {
        Response.ResponseBuilder builder = null;
    	User existingUser = userService.findByActivationToken(activationToken);
    	if(existingUser != null){
    		existingUser.setAktiviert(true);
    		existingUser = userService.update(existingUser);
            
    		if(existingUser.getEmail() != null){
    			List<String> emailList = new ArrayList<String>();
                emailList.add(existingUser.getEmail());
    			sendMailService.sendEmail(emailList, "Deine Aktivierung bei TeamPlaner", "Hallo "+existingUser.getVorname()+",<br /><br />"
    					+ "dein Accout bei TeamPlaner wurde erfolgreich aktiviert. Vielen Dank! Du kannst dich jetzt hier einloggen: "
    					+ "<p><a href='"+Constants.LOGIN_URL+"'>TeamPlaner Login</a></p>"
    							+ "Dein Teamplaner-Team<br />"
    							+ MailTexts.SUPPORT_TEXT, null);
    			builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    		} else{
    			builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
    		}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO USER", null));
    	}
        return builder.build();
    }
    
    @GET
    @Path("/inviteMember")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inviteMember(@QueryParam("trainerId") String token, @QueryParam("email") String email,
    		@QueryParam("vorname") String vorname, @QueryParam("name") String name, @QueryParam("teamId") String teamId, @QueryParam("rolle") String rolle){
        Response.ResponseBuilder builder = null;
        if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User trainerUser = loginTokenService.getUserIfLoggedIn(token);
    		if(trainerUser != null){
    			Team team = teamService.findById(teamId);
    			if(team != null){
    				if(Helper.checkIfUserInTeamAndTrainer(trainerUser, team) || 
    						(trainerUser.isAdmin() && team.getVerein().getId().equals(trainerUser.getVerein().getId()))){
    					if(email != null){
    						User existingEmailUser = userService.findByEmail(email);
    						if(existingEmailUser == null || !Helper.checkIfUserInTeam(existingEmailUser, team)){
    							if(vorname != null && name != null){
    								if(rolle != null && rolle.length() > 0){
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
    											if(rolle.equals(Constants.TRAINER_ROLE)){
    												einladung.setRolle(Constants.TRAINER_ROLE);
    											} else{
    												einladung.setRolle(Constants.SPIELER_ROLE);
    											}
    											einladungService.save(einladung);
    										}
    										List<String> emailList = new ArrayList<String>();
    										String encryptedInvitationId = CipherUtil.encrypt(einladung.getId());
    										emailList.add(email);
    										sendMailService.sendEmail(emailList, "Du wurdest von "+trainerUser.getVorname()+" "+trainerUser.getName()+" zu TeamPlaner eingeladen",
    												"Hallo " + vorname + " " + name + ",<br /><br />" +
    														"du wurdest von "+trainerUser.getVorname()+" "+trainerUser.getName()+" zum Team <b>"+team.getName()+"</b> in TeamPlaner eingeladen."
    														+ "<br />Um diesem Team beizutreten, logge dich unter folgendem Link ein oder registriere dich hier: "
    														+ "<p><a href='"+Constants.LOGIN_URL+"?id="+URLEncoder.encode(encryptedInvitationId, "UTF-8")+"'>TeamPlaner Login</a></p>"
    																+ "TeamPlaner ist ein plattformunabhängiges Tool zum Verwalten von Teams und Terminen. Und das Beste: Es ist kostenlos für dich!<br />"
    																+ "Also nimm die Einladung an und sei mit dabei!<br />"
    																+ MailTexts.SUPPORT_TEXT, null);
    										builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    									} catch(MessagingException messagingException){
    					                	// could not send mail : rollback
    					                	context.setRollbackOnly();
    					                	return Response.ok(Helper.createResponse("ERROR", "MAIL SEND ERROR", null)).build();
    					                } catch (Exception e) {
    					                	context.setRollbackOnly();
    										builder = Response.ok(Helper.createResponse("ERROR", "DATABASE SAVE ERROR", null));
    									} 
    								} else{
    									builder = Response.ok(Helper.createResponse("ERROR", "NO ROLLE", null));
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
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		}
    	}
        return builder.build();
    }
    
    @POST
    @Path("/changePassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changePassword(@FormParam("userId") String token, @FormParam("oldPw") String oldPw, @FormParam("newPw") String newPw) {
        Response.ResponseBuilder builder = null;
        if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User existingUser = loginTokenService.getUserIfLoggedIn(token);
    		if(existingUser != null){
    			if(existingUser.getPasswort().equals(oldPw)){
    				existingUser.setPasswort(newPw);
    				userService.update(existingUser);
    				builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    			} else{
    				builder = Response.ok(Helper.createResponse("ERROR", "WRONG PASSWORD", null));
    			}
    		} else{
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		}
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
					} catch(MessagingException messagingException){
	                	// could not send mail : rollback
	                	context.setRollbackOnly();
	                	return Response.ok(Helper.createResponse("ERROR", "MAIL SEND ERROR", null)).build();
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
    			User user = loginTokenService.getUserIfLoggedIn(userSettings.userId);
    			if(user == null){
    				builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
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
    				
    				List<Termin> termine = terminService.findByUserIdAndStartDate(user.getId(), new Date());
    				if(termine != null){
    					Calendar terminCal = Calendar.getInstance();
    					for(Termin termin : termine){
    						terminCal.setTime(termin.getDatum());
    						int terminDay = terminCal.get(Calendar.DAY_OF_WEEK);
    						if((terminDay == Calendar.MONDAY && userSettings.montagsAbsagen)
    								||(terminDay == Calendar.TUESDAY && userSettings.dienstagsAbsagen)
    								||(terminDay == Calendar.WEDNESDAY && userSettings.mittwochsAbsagen)
    								||(terminDay == Calendar.THURSDAY && userSettings.donnerstagsAbsagen)
    								||(terminDay == Calendar.FRIDAY && userSettings.freitagsAbsagen)
    								||(terminDay == Calendar.SATURDAY && userSettings.samstagsAbsagen)
    								||(terminDay == Calendar.SUNDAY && userSettings.sonntagsAbsagen)){
    							Zusage zusage = Helper.getZusageFromUserInTermin(termin, user);
    							zusage.setStatus(Constants.ABGESAGT);
    							zusage.setAutoSet(true);
    						}
    					}
    				}
    				user = userService.update(user);
    				builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    			}
    		} else{
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    		}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO SETTINGS", null));
    	}
    	return builder.build();
    }
    
    @GET
    @Path("/getUserSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUserSettings(@QueryParam("userId") String token) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if (user == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else{
    			if(user.getUserSettings() == null){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO USER SETTINGS FOUND", null));
    			} else{
    				builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(user.getUserSettings(), user.getId())));
    			}
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
						sendMailService.sendEmail(emailList, "Willkommen beim TeamPlaner", "Hallo "+user.getVorname()+",<br /><br />"
	                			+ "herzlich willkommen beim Teamplaner!<br />"
	                			+ "Vielen Dank für die Registrierung.<br />"
	                			+ "Bitte aktiviere deine Email mit Klick auf folgenden Link: "
	                		+ "<p><a href='"+Constants.ACTIVATION_URL+"?activationToken="+encodedToken+"'>Aktivierung</a></p>"
	                				+ "Dein Teamplaner-Team<br />"
	                				+ MailTexts.SUPPORT_TEXT, null);
						builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
					} catch(MessagingException messagingException){
	                	// could not send mail : rollback
	                	context.setRollbackOnly();
	                	return Response.ok(Helper.createResponse("ERROR", "SEND MAIL ERROR", null)).build();
	                } catch (UnsupportedEncodingException e) {
	                	context.setRollbackOnly();
						return Response.ok(Helper.createResponse("ERROR", "ENCODING ERROR", null)).build();
					}
				}
			}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
    	}
        return builder.build();
    }
    
    @GET
    @Path("/getUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response getUser(@QueryParam("token") String token){
    	Response.ResponseBuilder builder = null;
    	if(token != null && token.length() > 0){
    		User user = loginTokenService.getUserIfLoggedIn(token);
        	if(user == null){
        		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
        	} else{
				LoginToken loginToken = user.getLoginToken();
				if(loginToken == null){
					builder = Response.ok(Helper.createResponse("ERROR", "NO TOKEN FOR USER FOUND", null));					
				} else{
					builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createLoginRest(user, loginToken)));
				}
        	}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	}
    	return builder.build();
    }
    
    @GET
    @Path("/logout")
    @Produces(MediaType.APPLICATION_JSON)
    public Response logout(@QueryParam("userId") String token){
    	Response.ResponseBuilder builder = null;
    	if(token != null && token.length() > 0){
    		User user = loginTokenService.getUserIfLoggedIn(token);
        	if(user == null){
        		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
        	} else{
				LoginToken loginToken = user.getLoginToken();
				if(loginToken == null){
					builder = Response.ok(Helper.createResponse("ERROR", "NO TOKEN FOR USER FOUND", null));					
				} else{
					loginToken.getUser().setLoginToken(null);
					loginToken.setUser(null);
					loginTokenService.delete(loginToken);
					builder = Response.ok(Helper.createResponse("SUCCESS", "",null));
				}
        	}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	}
    	return builder.build();
    }
    
    
    @GET
    @Path("/setAllUserMailSettings")
    @Produces(MediaType.APPLICATION_JSON)
    public Response setAllUserMailSettings(@QueryParam("userId") String token, @QueryParam("weeklyStatusMail") Boolean weeklyStatusMail, 
    		@QueryParam("terminReminderMail") Boolean terminReminderMail) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User user = loginTokenService.getUserIfLoggedIn(token);
    		if (user == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else{
    			if(weeklyStatusMail == null){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO WEEKLY STATUS MAIL SET", null));
    			} else{
    				if(terminReminderMail == null){
        				builder = Response.ok(Helper.createResponse("ERROR", "NO TERMIN REMINDER MAIL SET", null));
        			} else{
        				user.setWeeklyStatusMail(weeklyStatusMail);
        				user.setTerminReminderMail(terminReminderMail);
        				userService.update(user);
        				builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
        			}
    			}
    		}
    	}
        return builder.build();
    }
    
    @POST
    @Path("/askForInvitation")
    @Produces(MediaType.APPLICATION_JSON)
    public Response askForInvitation(@FormParam("email") String email, @FormParam("userId") String token, @FormParam("message") String message) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User requestUser = loginTokenService.getUserIfLoggedIn(token);
    		if (requestUser == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else{
    			if(email == null || email.length() == 0){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
    			} else{
    				User askedUser = userService.findByEmail(email);
    				if (askedUser == null || askedUser.getAktivierToken() == null) {
    					builder = Response.ok(Helper.createResponse("ERROR", "USER MAIL NOT FOUND", null));
    				} else{
						try {
							if(message == null || message.length() == 0){
								message = "Keine Nachricht angegeben";
							}
							List<String> emailList = new ArrayList<String>();
							emailList.add(askedUser.getEmail());
							sendMailService.sendEmail(emailList, requestUser.getVorname()+" "+requestUser.getName()+" möchte in ein Team eingeladen werden", "Hallo "+askedUser.getVorname()+",<br /><br />"
		                			+ requestUser.getVorname() + " " + requestUser.getName() + " möchte mit der Email <b>"+email+"</b> in eines deiner Teams eingeladen werden.<br /><br />"
		                			+ "Als Nachricht wurde angegeben:<br />"
		                			+ message + "<br /><br />"
		                			+ "So kannst du "+requestUser.getVorname() + " " + requestUser.getName() + " in eines deiner Teams einladen: "
		                			+ "<ol>"
		                			+ 	"<li>Logge dich bei <a href=\""+Constants.LOGIN_URL+"\">Teamplaner</a> ein</li>"
		                			+   "<li>Gehe auf \"mein Team\"</li>"
		                			+   "<li>Klicke auf \"Teammitglied einladen\"</li>"
		                			+   "<li>Trage Name, Email und Rolle ein</li>"
		                			+   "<li>Klicke auf \"Einladen\"</li>"
		                			+ "</ol><br />"
		                			+ "Dein Teamplaner Team<br /><br />"
		                		    + MailTexts.SUPPORT_TEXT, null);
							builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
						} catch(MessagingException messagingException){
		                	// could not send mail : rollback
		                	context.setRollbackOnly();
		                	return Response.ok(Helper.createResponse("ERROR", "SEND MAIL ERROR", null)).build();
		                }
    				}
    			}
    		}
    	}
        return builder.build();
    }
}
