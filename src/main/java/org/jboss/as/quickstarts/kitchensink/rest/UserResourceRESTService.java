package org.jboss.as.quickstarts.kitchensink.rest;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
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
import javax.xml.bind.DatatypeConverter;

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

    @POST
    @Path("/login")
    @Produces(MediaType.APPLICATION_JSON)
    public Response login(@FormParam("email") String email, @FormParam("password") String password, @FormParam("invitationId") String invitationId){
    	Response.ResponseBuilder builder = null;
    	if(email != null && email.length() > 0){
    		email = cleanMail(email);
    		User user = userService.findByEmail(email);
        	try {
				if(user == null || !CipherUtil.checkPasswords(password, user.getPasswort())){
					// kein user oder falsches Passwort
					log.log(Level.WARNING, "Login error: "+email +", reason:" + (user == null ? "no user for mail found" : "pw wrong"));
					builder = Response.ok(Helper.createResponse("ERROR", "USER ID NOT FOUND OR WRONG PASSWORD", null));
				} else{
					if(!user.isAktiviert()){
						log.log(Level.INFO, "Login error not activated for mail: "+email);
						builder = Response.ok(Helper.createResponse("ERROR", "USER NOT ACTIVATED", null));
					} else{
						// login erfolgreich
						if(invitationId != null && invitationId.length() > 0){
							builder = handleInvitation(user, email, invitationId);
						}
						// invitation ok wenn builder == null
						if(builder == null){
							LoginToken loginToken = login(user);
							builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createLoginRest(user, loginToken)));
						}
					}
				}
			} catch (NoSuchAlgorithmException e) {
				e.printStackTrace();
            	context.setRollbackOnly();
            	return Response.ok(Helper.createResponse("ERROR", "SERVER ERROR", null)).build();
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
    
    //liefert Response nur im Fehlerfall, sonst null
    private Response.ResponseBuilder handleInvitation(User user, String email, String encryptedInvitationId){
    	String invitationId = CipherUtil.decrypt(encryptedInvitationId);
    	if(invitationId != null){
    		Einladung einladung = einladungService.findById(invitationId);
        	if(einladung != null && einladung.getStatus() == 0){
        		Team team = einladung.getTeam();
        		if(team != null){
        			if(user.getVerein() == null || team.getVerein() == user.getVerein()){
        				if(Helper.checkIfUserInTeam(user, team)){
        					return null;
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
        					zusage.setStatus(t.getDefaultZusageStatus());
        					zusage.setTermin(t);
        					zusage.setUser(user);
        					zusageService.save(zusage);
        				}
        				einladung.getInviter().getEinladungen().remove(einladung);
        				einladung.setInviter(null);
        				einladung.setTeam(null);
        				team.getEinladungen().remove(einladung);
        				einladungService.delete(einladung);
        				user.getRollen().add(rolle);
        				user = userService.update(user);
        				return null;
        			} else{
        				return Response.ok(Helper.createResponse("ERROR", "INVITED USER ALREADY IN DIFFERENT VEREIN", null));
        			}
        		} else{
        			return Response.ok(Helper.createResponse("ERROR", "TEAM FOR INVITATION NOT FOUND", null));
        		}
        	} else{
        		return Response.ok(Helper.createResponse("ERROR", "INVITATION NOT FOUND", null));
        	}
    	} else{
    		return Response.ok(Helper.createResponse("ERROR", "INVITATION ID NOT CORRECT", null));
    	}
    }
    
    @POST
    @Produces(MediaType.APPLICATION_JSON)
    public Response createUser(@FormParam("email") String email, @FormParam("vorname") String vorname, @FormParam("name") String name, 
    		@FormParam("password") String password, @FormParam("invitationId") String invitationId) throws MessagingException, UnsupportedEncodingException {
        Response.ResponseBuilder builder = null;
        if(email == null || email.length() == 0){
        	builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
        } else{
        	if(vorname == null || vorname.length() == 0){
            	builder = Response.ok(Helper.createResponse("ERROR", "NO VORNAME", null));
            } else{
            	if(name == null || name.length() == 0){
                	builder = Response.ok(Helper.createResponse("ERROR", "NO NAME", null));
                } else{
                	if(password == null || password.length() == 0){
                    	builder = Response.ok(Helper.createResponse("ERROR", "NO PASSWORD", null));
                    } else{
			        	email = cleanMail(email);
			        	User existingUser = userService.findByEmail(email);
			        	if(existingUser == null){
			        		name = name.trim();
			        		vorname = vorname.trim();
			        		User user = new User();
			                user.setName(name);
			                user.setVorname(vorname);
			                user.setEmail(email);
			                String hashedPasswordWithSaltAppended;
							try {
								hashedPasswordWithSaltAppended = CipherUtil.createHashedPasswordWithSaltAppended(password);
							} catch (NoSuchAlgorithmException e) {
								e.printStackTrace();
			                	context.setRollbackOnly();
			                	return Response.ok(Helper.createResponse("ERROR", "SERVER ERROR", null)).build();
							}
			                user.setPasswort(hashedPasswordWithSaltAppended);
			                user.setAktiviert(false);
			                String aktivierToken = UUID.randomUUID().toString();
			                user.setAktivierToken(aktivierToken);
			                UserSettings userSettings = new UserSettings();
			                user.setUserSettings(userSettings);
			                user.setRollen(new ArrayList<TeamRolle>());
			                user.setEinladungen(new ArrayList<Einladung>());
			                user.setZusagen(new ArrayList<Zusage>());
			                user.setWeeklyStatusMail(true);
			                user.setTerminReminderMail(true);
			                userService.register(user);
			                
			                String encodedToken = URLEncoder.encode(aktivierToken, "UTF-8");
			                List<String> emailList = new ArrayList<String>();
			                emailList.add(email);
			                try{
			                	String activationUrl = Constants.ACTIVATION_URL+"?activationToken="+encodedToken;
			                	sendMailService.sendEmail(emailList, "Willkommen beim TeamPlaner", "<p>Hallo "+vorname+",</p>"
			                			+ "<div>herzlich willkommen beim Teamplaner!<br />"
			                			+ "Vielen Dank für die Registrierung.<br />"
			                			+ "Bitte aktiviere deine Email mit Klick auf folgenden Link: </div>"
			                		+ "<p><a href='" + activationUrl + "'>" + activationUrl + "</a></p>"
			                				+ "<p>Dein Teamplaner-Team</p>", Constants.MAIL_SENDER);
			                } catch(MessagingException messagingException){
			                	// could not send mail : rollback
			                	context.setRollbackOnly();
			                	return Response.ok(Helper.createResponse("ERROR", "SEND MAIL ERROR", null)).build();
			                }
			                if(invitationId != null && invitationId.length() > 0){
			        			builder = handleInvitation(user, email, invitationId);
			        		}
			                // invitation ok wenn builder == null
			                if(builder == null){
			                	builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
			                } 
			        	} else{
			        		builder = Response.ok(Helper.createResponse("ERROR", "EMAIL EXISTS", null));
			        	}
                    }
                }
            }
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
                String loginUrl = Constants.LOGIN_URL;
    			sendMailService.sendEmail(emailList, "Deine Aktivierung bei TeamPlaner", "<p>Hallo "+existingUser.getVorname()+",</p>"
    					+ "<p>dein Accout bei TeamPlaner wurde erfolgreich aktiviert. Vielen Dank! Du kannst dich jetzt hier einloggen: </p>"
    					+ "<p><a href='"+loginUrl+"'>"+loginUrl+"</a></p>"
    							+ "<p>Dein Teamplaner-Team</p>", Constants.MAIL_SENDER);
    			log.log(Level.INFO, "User activated: "+existingUser.getId());
    			builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    		} else{
    			builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL", null));
    		}
    	} else{
    		builder = Response.ok(Helper.createResponse("ERROR", "NO USER", null));
    	}
        return builder.build();
    }
    
    @POST
    @Path("/inviteMember")
    @Produces(MediaType.APPLICATION_JSON)
    public Response inviteMember(@FormParam("trainerId") String token, @FormParam("email") String email,
    		@FormParam("vorname") String vorname, @FormParam("name") String name, @FormParam("teamId") String teamId, @FormParam("rolle") String rolle){
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
    						email = cleanMail(email);
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
    										String loginUrl = Constants.LOGIN_URL+"?id="+URLEncoder.encode(encryptedInvitationId, "UTF-8");
    										sendMailService.sendEmail(emailList, "Du wurdest von "+trainerUser.getVorname()+" "+trainerUser.getName()+" zu TeamPlaner eingeladen",
    												"<p>Hallo " + vorname + " " + name + ",</p>"
    														+ "<p>du wurdest von "+trainerUser.getVorname()+" "+trainerUser.getName()+" zum Team <b>"+team.getName()+"</b> "
    														+ "in den Verein <b>"+team.getVerein().getName()+"</b> in TeamPlaner eingeladen.</p>"
    														+ "<p>Um diesem Team beizutreten, logge dich unter folgendem Link ein oder registriere dich hier: </p>"
    														+ "<p><a href='"+loginUrl+"'>"+loginUrl+"</a></p>"
    																+ "<p>TeamPlaner ist ein plattformunabhängiges Tool zum Verwalten von Teams und Terminen. Und das Beste: Es ist kostenlos für dich!"
    																+ "Also nimm die Einladung an und sei mit dabei!</p>", Constants.MAIL_SENDER);
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
    			try{
	    			if(CipherUtil.checkPasswords(oldPw, existingUser.getPasswort())){
		                String newHashedPasswordWithSaltAppended = CipherUtil.createHashedPasswordWithSaltAppended(newPw);
	    				existingUser.setPasswort(newHashedPasswordWithSaltAppended);
	    				userService.update(existingUser);
	    				builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
	    			} else{
	    				builder = Response.ok(Helper.createResponse("ERROR", "WRONG PASSWORD", null));
	    			}
    			} catch (NoSuchAlgorithmException e) {
					e.printStackTrace();
                	context.setRollbackOnly();
                	return Response.ok(Helper.createResponse("ERROR", "SERVER ERROR", null)).build();
				}
    		} else{
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		}
    	}
        return builder.build();
    }
    
    //bei Passwort vergessen mit Mail eingabe
    @POST
    @Path("/resetPassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response resetPassword(@FormParam("email") String email) {
        Response.ResponseBuilder builder = null;
        if(email != null && email.length() > 0){
        	User existingUser = userService.findByEmail(email);
        	if(existingUser != null){
        		if(existingUser.getEmail() != null){
        			if(existingUser.isAktiviert()){
        				try {
	        				String encUserId = CipherUtil.encrypt(existingUser.getId());
							encUserId = URLEncoder.encode(encUserId, "UTF-8");
	        				List<String> emailList = new ArrayList<String>();
	        				emailList.add(existingUser.getEmail());
        					String resetPasswordUrl = Constants.RESET_PASSWORD_URL;
        					sendMailService.sendEmail(emailList, "Dein neues Teamplaner Passwort", 
        							"<p>Hallo "+existingUser.getVorname()+",</p>"
        						  + "<div>für deine Email Adresse wurde ein neues Passwort angefordert. Klicke auf folgenden Link, um dir ein neues Passwort zuschicken zu lassen:<br /><br />"
        						  + "<a href=\""+resetPasswordUrl+"?resetToken="+encUserId+"\">"+resetPasswordUrl+"?resetToken="+encUserId+"</a><br /><br />"
        						  + "Hast du kein neues Passwort angefordert, dann kannst du diese Mail einfach ignorieren. Dein Passwort wird nicht geändert.<br /><br /></div>"
        						  + "<p>Dein Teamplaner Team.</p>", Constants.MAIL_SENDER);
        					builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
        				} catch(MessagingException messagingException){
        					// could not send mail : rollback
        					context.setRollbackOnly();
        					return Response.ok(Helper.createResponse("ERROR", "MAIL SEND ERROR", null)).build();
        				} catch (UnsupportedEncodingException e) {
        					// could not encode URL : rollback
        					context.setRollbackOnly();
        					return Response.ok(Helper.createResponse("ERROR", "ENCODING ERROR", null)).build();
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
        } else{
        	builder = Response.ok(Helper.createResponse("ERROR", "NO EMAIL SET", null));
        }
        return builder.build();
    }
    
    //bei Passwort vergessen aus Email mit Token
    @POST
    @Path("/sendNewPassword")
    @Produces(MediaType.APPLICATION_JSON)
    public Response sendNewPassword(@FormParam("token") String token) {
        Response.ResponseBuilder builder = null;
        if(token != null && token.length() > 0){
        	String userId = CipherUtil.decrypt(token);
        	if(userId != null){
        		User existingUser = userService.findById(userId);
        		if(existingUser != null){
        			if(existingUser.getEmail() != null){
        				if(existingUser.isAktiviert()){
        					String newPw = UUID.randomUUID().toString().substring(0, 12);
        	                String hashedPasswordWithSaltAppended;
        					try {
        						hashedPasswordWithSaltAppended = CipherUtil.createHashedPasswordWithSaltAppended(newPw);
        					} catch (NoSuchAlgorithmException e) {
        						e.printStackTrace();
        	                	context.setRollbackOnly();
        	                	return Response.ok(Helper.createResponse("ERROR", "SERVER ERROR", null)).build();
        					}
        					existingUser.setPasswort(hashedPasswordWithSaltAppended);
        					userService.update(existingUser);
        					List<String> emailList = new ArrayList<String>();
        					emailList.add(existingUser.getEmail());
        					try {
        						String loginUrl = Constants.LOGIN_URL;
        						sendMailService.sendEmail(emailList, "Dein neues Teamplaner Passwort", "<p>Hallo "+existingUser.getVorname()+",</p><div>Dein neues Passwort lautet: "+newPw+"<br /><br />Du kannst dich damit unter <a href=\""+loginUrl+"\">"+loginUrl+"</a> einloggen.</div>", Constants.MAIL_SENDER);
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
        	} else{
        		builder = Response.ok(Helper.createResponse("ERROR", "WRONG TOKEN", null));
        	}
        } else{
        	builder = Response.ok(Helper.createResponse("ERROR", "NO TOKEN SET", null));
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
    				UserSettings newUserSettings = user.getUserSettings();
    				if(newUserSettings == null){
    					newUserSettings = new UserSettings();
    				}
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
						String activationUrl = Constants.ACTIVATION_URL+"?activationToken="+encodedToken;
						sendMailService.sendEmail(emailList, "Willkommen beim TeamPlaner", "<p>Hallo "+user.getVorname()+",</p>"
	                			+ "<div>herzlich willkommen beim Teamplaner!<br />"
	                			+ "Vielen Dank für die Registrierung.<br />"
	                			+ "Bitte aktiviere deine Email mit Klick auf folgenden Link: </div>"
	                		+ "<p><a href='"+ activationUrl +"'>"+ activationUrl +"</a></p>"
	                				+ "<p>Dein Teamplaner-Team</p>", Constants.MAIL_SENDER);
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
    				email = cleanMail(email);
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
							String loginUrl = Constants.LOGIN_URL;
							sendMailService.sendEmail(emailList, requestUser.getVorname()+" "+requestUser.getName()+" möchte in ein Team eingeladen werden", "<p>Hallo "+askedUser.getVorname()+",</p>"
		                			+ "<p>"+requestUser.getVorname() + " " + requestUser.getName() + " möchte mit der Email <b>"+requestUser.getEmail()+"</b> in eines deiner Teams eingeladen werden.</p>"
		                			+ "<div>Als Nachricht wurde angegeben:<br />"
		                			+ message + "<br /><br />"
		                			+ "So kannst du "+requestUser.getVorname() + " " + requestUser.getName() + " in eines deiner Teams einladen: "
		                			+ "<ol>"
		                			+ 	"<li>Logge dich bei <a href=\""+loginUrl+"\">"+loginUrl+"</a> ein</li>"
		                			+   "<li>Gehe auf \"mein Team\"</li>"
		                			+   "<li>Klicke auf \"Teammitglied einladen\"</li>"
		                			+   "<li>Trage Name, Email und Rolle ein</li>"
		                			+   "<li>Klicke auf \"Einladen\"</li>"
		                			+ "</ol><br />"
		                			+ "Dein Teamplaner Team<br /><br /></div>", Constants.MAIL_SENDER);
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
    
    private String cleanMail(String email){
    	return email.toLowerCase().replaceAll(" ", "");
    }
    
    @POST
    @Path("/deleteUser")
    @Produces(MediaType.APPLICATION_JSON)
    public Response deleteUser(@FormParam("userId") String token) {
    	Response.ResponseBuilder builder = null;
    	if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User userToDelete = loginTokenService.getUserIfLoggedIn(token);
    		if (userToDelete == null) {
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else{
				try {
					for(TeamRolle rolle : userToDelete.getRollen()){
						rolle.getTeam().getRollen().remove(rolle);
					}
					for(Zusage zusage : userToDelete.getZusagen()){
						zusage.getTermin().getZusagen().remove(zusage);
					}
					List<Einladung> einladungen = einladungService.findByEmail(userToDelete.getEmail());
					if(einladungen != null){
						for(Einladung einladung : einladungen){
							einladung.getTeam().getEinladungen().remove(einladung);
						}
					}
					log.log(Level.INFO, "Deleting user id: "+userToDelete.getId());
					userService.delete(userToDelete);
					builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
				} catch (Exception e) {
					context.setRollbackOnly();
					builder = Response.ok(Helper.createResponse("ERROR", "DELETE SERVICE ERROR", null));
				}
    		}
    	}
        return builder.build();
    }
    
    @POST
    @Path("/changeUsernames")
    @Produces(MediaType.APPLICATION_JSON)
    public Response changeUsernames(@FormParam("userId") String token, @FormParam("newSurname") String newSurname, @FormParam("newName") String newName) {
        Response.ResponseBuilder builder = null;
        if(token == null || token.length() == 0){
    		builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NO_TOKEN_SET, null));
    	} else{
    		User existingUser = loginTokenService.getUserIfLoggedIn(token);
    		if(existingUser == null){
    			builder = Response.ok(Helper.createResponse("ERROR", ResponseTypes.NOT_LOGGED_IN, null));
    		} else{
    			if(newSurname == null || newSurname.length() == 0 || newName == null || newName.length() == 0){
    				builder = Response.ok(Helper.createResponse("ERROR", "NO NAMES SET", null));
    			} else{
    				newName = newName.trim();
    				newSurname = newSurname.trim();
    				existingUser.setName(newName);
    				existingUser.setVorname(newSurname);
    				builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
    			}
    		}
    	}
        return builder.build();
    }
}
