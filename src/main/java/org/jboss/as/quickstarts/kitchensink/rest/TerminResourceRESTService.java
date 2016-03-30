package org.jboss.as.quickstarts.kitchensink.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;
import java.util.logging.Logger;

import javax.ejb.Stateless;
import javax.inject.Inject;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.jboss.as.quickstarts.kitchensink.constants.Rolle;
import org.jboss.as.quickstarts.kitchensink.model.Ort;
import org.jboss.as.quickstarts.kitchensink.model.Serie;
import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.TerminVorlage;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.UserSettings;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;
import org.jboss.as.quickstarts.kitchensink.service.OrtService;
import org.jboss.as.quickstarts.kitchensink.service.TeamService;
import org.jboss.as.quickstarts.kitchensink.service.TerminService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.service.VereinService;
import org.jboss.as.quickstarts.kitchensink.service.ZusageService;
import org.jboss.as.quickstarts.kitchensink.util.Constants;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.wrapper.OrtREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TerminREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TerminRequestREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TerminVorlageREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.util.WrapperUtil;

import de.masalis.teamplanner.mail.SendMail;

/**
 * JAX-RS Example
 * <p/>
 * This class produces a RESTful service to read/write the contents of the
 * termins table.
 */
@Path("/termine")
@Stateless
public class TerminResourceRESTService {

	@Inject
	private Logger log;

	@Inject
	TerminService terminService;

	@Inject
	UserService userService;

	@Inject
	OrtService ortService;

	@Inject
	VereinService vereinService;

	@Inject
	TeamService teamService;

	@Inject
	ZusageService zusageService;

	@Inject
	SendMail sendMailService;

	// Soodle detail
	@GET
	@Path("/byId")
	@Produces(MediaType.APPLICATION_JSON)
	public Response lookupTerminById(@QueryParam("id") String terminId, @QueryParam("userId") String userId) {
		Response.ResponseBuilder builder = null;
		Termin termin = terminService.findById(terminId);
		User user = userService.findById(userId);
		if(user == null){
			builder = Response.ok(Helper.createResponse("ERROR", "USER ID NOT FOUND", null));
		} else{
			if (termin == null) {
				builder = Response.ok(Helper.createResponse("ERROR", "TERMIN ID NOT FOUND", null));
			} else {
				Termin nextTermin = terminService.findNextTermin(termin, user);
				Termin prevTermin = terminService.findPreviousTermin(termin, user);
				builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(termin, userId, prevTermin, nextTermin)));
			}
		}
		
		return builder.build();
	}

	@GET
	@Path("/forTeamIds")
	@Produces(MediaType.APPLICATION_JSON)
	public Response lookupTermineByTeamIds(@QueryParam("ids") List<String> teamIds,
			@QueryParam("userId") String userId) {
		Response.ResponseBuilder builder = null;
		List<Termin> termine = terminService.findByTeamIds(teamIds);
		if (termine == null) {
			builder = Response.status(Status.NO_CONTENT);
		} else {
			List<TerminREST> erg = new ArrayList<TerminREST>(termine.size());
			for (Termin t : termine) {
				erg.add(WrapperUtil.createRest(t, userId, null, null));
			}
			builder = Response.ok(erg);
		}

		return builder.build();
	}

	// Kalender
	@GET
	@Path("/forUserInDates")
	@Produces(MediaType.APPLICATION_JSON)
	public Response lookupTermineByUserAndDates(@QueryParam("id") String userId,
			@QueryParam("startDate") String startDateString, @QueryParam("endDate") String endDateString) {
		Response.ResponseBuilder builder = null;
		Date startDate = new Date(Long.parseLong(startDateString));
		Date endDate = new Date(Long.parseLong(endDateString));
		User user = userService.findById(userId);
		if (user == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "USER ID NOT FOUND", null));
		} else {
			List<Termin> termine = terminService.findByTeamIdsAndDates(Helper.getTeamIdsForUser(user), startDate,
					endDate);
			if (termine == null) {
				builder = Response.ok(Helper.createResponse("ERROR", "NO TERMINE", null));
			} else {
				List<TerminREST> erg = new ArrayList<TerminREST>(termine.size());
				for (Termin t : termine) {
					erg.add(WrapperUtil.createRest(t, userId, null, null));
				}
				builder = Response.ok(Helper.createResponse("SUCCESS", "", erg));
			}
		}
		return builder.build();
	}

	// create termin mit terminserie oder update termin
	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Response createTermin(TerminRequestREST terminRest) {
		Response.ResponseBuilder builder = null;

		int terminCount = 0;
		try {
			// Check user Rechte
			builder = checkUserCreateTerminPrivileg(terminRest);
			if (builder != null) {
				// keine Rechte
				return builder.build();
			}

			Ort ort = this.getOrCreateOrt(terminRest);

			Date terminDatum = new Date(Long.parseLong(terminRest.datum) * 1000);
			Date terminEndDatum = null;
			Calendar cStart = Calendar.getInstance();
			cStart.setTime(terminDatum);
			Calendar cEnd = Calendar.getInstance();
			cEnd.setTime(terminDatum);

			Serie serie = null;
			if (terminRest.serie != null && terminRest.serie.intervall != 0 && terminRest.serie.serieEndDate != null) {
				terminEndDatum = new Date(Long.parseLong(terminRest.serie.serieEndDate) * 1000);
				cEnd.setTime(terminEndDatum);
				serie = new Serie();
				serie.setIntervall(terminRest.serie.intervall);
				serie.setTermine(new ArrayList<Termin>());
			}
			cEnd.add(Calendar.SECOND, 1);
			Date endDate = cEnd.getTime();

			Calendar cCurrent = Calendar.getInstance();
			cCurrent.setTime(cStart.getTime());
			Date currentDate = cCurrent.getTime();
			boolean firstTermin = true;
			while (currentDate.before(endDate)) {
				Termin termin = null;
				if (terminRest.terminId != null) {
					termin = terminService.findById(terminRest.terminId);
					if (termin == null) {
						return Response.ok(Helper.createResponse("ERROR", "TERMIN ID DOES NOT EXIST", "")).build();
					}
				} else {
					termin = new Termin();
					termin = terminService.save(termin);
				}
				termin.setOrt(ort);
				ort.getTermine().add(termin);
				ort = ortService.save(ort);
				termin.setStatus(1);
				termin.setBeschreibung(terminRest.beschreibung);
				termin.setMaybeAllowed(terminRest.maybeAllowed);
				termin.setName(terminRest.name);
				termin.setDatum(currentDate);
				termin.setDefaultZusageStatus(terminRest.defaultZusageStatus);
				termin = terminService.save(termin);

				Team team = teamService.findById(terminRest.teamId);
				termin.setTeam(team);
				team.getTermine().add(termin);
				teamService.save(team);

				List<Zusage> zusagen = new ArrayList<Zusage>();
				List<User> persons = userService.findUsersByTeamId(terminRest.teamId);
				if (persons != null) {
					for (User person : persons) {
						Zusage zusage = new Zusage();
						zusage.setKommentar("");
						zusage.setUser(person);
						int status = getZusageStatus(cCurrent, zusage, person.getUserSettings(), terminRest.defaultZusageStatus);
						zusage.setStatus(status);
						zusage.setTermin(termin);
						zusagen.add(zusage);
						zusageService.save(zusage);
					}
				}
				if (serie != null) {
					termin.setSerie(serie);
					serie.getTermine().add(termin);
					serie = terminService.saveSerie(serie);
				}
				termin.setZusagen(zusagen);
				termin = terminService.save(termin);
				terminCount++;

				TerminVorlage existingVorlage = terminService.getTerminVorlageById(terminRest.vorlageId);
				boolean terminVorlageNullOrChanged = checkTerminVorlageNullOrChanged(existingVorlage, termin);
				if (terminRest.vorlage && firstTermin && terminVorlageNullOrChanged) {
					TerminVorlage terminVorlage = Helper.createVorlage(termin);
					terminService.saveVorlage(terminVorlage);
				}

				if (terminRest.serie != null && terminRest.serie.intervall != 0
						&& terminRest.serie.serieEndDate != null) {
					cCurrent.add(Calendar.DATE, terminRest.serie.intervall);
					currentDate = cCurrent.getTime();
				} else {
					currentDate = endDate;
				}
				firstTermin = false;
			} // end while create termin cycle

			builder = Response.ok(Helper.createResponse("SUCCESS", "", "CREATED TERMINE: " + terminCount));
		} catch (Exception e) {
			StringWriter errors = new StringWriter();
			e.printStackTrace(new PrintWriter(errors));
			builder = Response.ok(Helper.createResponse("ERROR", "COULD NOT CREATE TERMIN", errors.toString()));
		}
		return builder.build();
	}
	
	private int getZusageStatus(Calendar calendar, Zusage zusage, UserSettings userSettings, int defaultZusageStatus){
		int status = defaultZusageStatus;
		int day = calendar.get(Calendar.DAY_OF_WEEK);
		
		if((day == Calendar.MONDAY && userSettings.isMontagsAbsagen())
				|| (day == Calendar.TUESDAY && userSettings.isDienstagsAbsagen())
				|| (day == Calendar.WEDNESDAY && userSettings.isMittwochsAbsagen())
				|| (day == Calendar.THURSDAY && userSettings.isDonnerstagsAbsagen())
				|| (day == Calendar.FRIDAY && userSettings.isFreitagsAbsagen())
				|| (day == Calendar.SATURDAY && userSettings.isSamstagsAbsagen())
				|| (day == Calendar.SUNDAY && userSettings.isSonntagsAbsagen())){
			status = 0;
			zusage.setAutoSet(true);
		}
		return status;
	}

	private boolean checkTerminVorlageNullOrChanged(TerminVorlage existingVorlage, Termin termin) {
		if (existingVorlage == null) {
			return true;
		}
		Calendar calTermin = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calTermin.setTime(termin.getDatum());
		int terminStunde = calTermin.get(Calendar.HOUR_OF_DAY);
		int terminMinute = calTermin.get(Calendar.MINUTE);
		
		Calendar calVorlage = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		calVorlage.setTime(existingVorlage.getTime());
		int vorlageStunde = calVorlage.get(Calendar.HOUR_OF_DAY);
		int vorlageMinute = calVorlage.get(Calendar.MINUTE);
		
		Ort existingOrt = ortService.findById(existingVorlage.getOrt().getId());
		Ort gesendeterOrt = termin.getOrt();
		if (!existingVorlage.getBeschreibung().equals(termin.getBeschreibung())
				|| terminStunde != vorlageStunde
				|| terminMinute != vorlageMinute
				|| !existingVorlage.getName().equals(termin.getName())
				|| !existingOrt.getBeschreibung().equals(gesendeterOrt.getBeschreibung())
				|| !existingOrt.getLatitude().equals(gesendeterOrt.getLatitude())
				|| !existingOrt.getLongitude().equals(gesendeterOrt.getLongitude())
				|| !existingOrt.getNummer().equals(gesendeterOrt.getNummer())
				|| !existingOrt.getPlz().equals(gesendeterOrt.getPlz())
				|| !existingOrt.getStadt().equals(gesendeterOrt.getStadt())
				|| !existingOrt.getStrasse().equals(gesendeterOrt.getStrasse())) {
			return true;
		}
		return false;
	}

	private Ort getOrCreateOrt(TerminRequestREST terminRest) {
		Verein verein = vereinService.findByTeamId(terminRest.teamId);
		Ort ort = null;
		OrtREST ortRest = terminRest.ort;
		if (terminRest.ort != null && terminRest.ort.id != null && ortService.findById(terminRest.ort.id) != null) {
			ort = ortService.findById(terminRest.ort.id);
			if (ort.getBeschreibung().equals(ortRest.beschreibung) && ort.getLatitude().equals(ortRest.latitude)
					&& ort.getLongitude().equals(ortRest.longitude) && ort.getNummer().equals(ortRest.nummer)
					&& ort.getPlz().equals(ortRest.plz) && ort.getStadt().equals(ortRest.stadt)
					&& ort.getStrasse().equals(ortRest.strasse) ) {
				if(ortRest.vorlage){
					ort.setVorlage(true);
				}
				return ort;
			}
		}

		ort = new Ort();
		ort.setBeschreibung(ortRest.beschreibung);
		ort.setNummer(ortRest.nummer);
		ort.setVorlage(ortRest.vorlage);
		ort.setPlz(ortRest.plz);
		ort.setStadt(ortRest.stadt);
		ort.setStrasse(ortRest.strasse);
		ort.setVorlage(ortRest.vorlage);
		ort.setLatitude(ortRest.latitude);
		ort.setLongitude(ortRest.longitude);
		ort.setTeamId(terminRest.teamId);
		ort.setVereinId(verein.getId());
		ort.setTermine(new ArrayList<Termin>());
		ort.setVerein(verein);
		try {
			ort = ortService.save(ort);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ort;
	}

	private ResponseBuilder checkUserCreateTerminPrivileg(TerminRequestREST terminRest) {
		if (terminRest.userId == null || terminRest.teamId == null) {
			return Response.ok(Helper.createResponse("ERROR", "NO USER OR TEAM SET", ""));
		} else {
			User user = userService.findById(terminRest.userId);
			Team team = teamService.findById(terminRest.teamId);
			if (user != null && team != null) {
				for (int i = 0; i < user.getRollen().size(); i++) {
					TeamRolle userRolle = user.getRollen().get(i);
					if (userRolle.getTeam().getId().equals(terminRest.teamId)) {
						if (userRolle.getRolle().equals(Constants.TRAINER_ROLE)) {
							// ok
							return null;
						} else {
							return Response.ok(Helper.createResponse("ERROR", "NO TRAINER RIGHTS", ""));
						}
					}
				}
				return Response.ok(Helper.createResponse("ERROR", "USER NOT IN TEAM", ""));
			} else {
				return Response.ok(Helper.createResponse("ERROR", "NO USER OR TEAM FOUND", ""));
			}
		}
	}

	// set user status of termin
	@POST
	@Path("/userStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setUserStatus(@FormParam("userId") String userId, @FormParam("terminId") String terminId,
			@FormParam("status") String status, @FormParam("kommentar") String kommentar) {
		Response.ResponseBuilder builder = null;
		if (userId == null || terminId == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "PARAMETER IS NULL", null));
			return builder.build();
		}
		Termin termin = terminService.findById(terminId);
		User user = userService.findById(userId);
		if (termin == null || user == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "USER ID OR TERMIN ID NOT FOUND", null));
		} else {
			if (termin.getZusagen() != null) {
				boolean set = false;
				for (Zusage zusage : termin.getZusagen()) {
					if (zusage.getUser().getId() == user.getId()) {
						if (status != null) {
							zusage.setStatus(Integer.parseInt(status));
						}
						if (kommentar != null) {
							zusage.setKommentar(kommentar);
						}
						zusage.setAutoSet(false);
						terminService.updateZusage(zusage);
						set = true;
						break;
					}
				}
				if (set) {
					termin = terminService.findById(terminId);
					builder = Response.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(termin, userId, null, null)));
				} else {
					builder = Response.ok(Helper.createResponse("ERROR", "USER NOT IN TERMIN", null));
				}
			}
		}
		return builder.build();
	}

	// set termin status
	@GET
	@Path("/setStatus")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setTerminStatus(@QueryParam("userId") String userId, @QueryParam("terminId") String terminId,
			@QueryParam("status") String status, @QueryParam("kommentar") String kommentar) {
		Response.ResponseBuilder builder = null;
		if (userId == null || terminId == null || status == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "PARAMETER IS NULL", null));
			return builder.build();
		}
		Termin termin = terminService.findById(terminId);
		User user = userService.findById(userId);

		if (termin == null || user == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "USER ID OR TERMIN ID NOT FOUND", null));
		} else {
			Team team = termin.getTeam();
			if (team == null) {
				builder = Response.ok(Helper.createResponse("ERROR", "TEAM FOR TERMIN NOT FOUND", null));
			} else {
				boolean inTeamAndTrainer = Helper.checkIfUserInTeamAndTrainer(user, team);
				if (!inTeamAndTrainer) {
					builder = Response.ok(Helper.createResponse("ERROR", "USER NOT IN TEAM OR NOT TRAINER", null));
				} else {
					termin.setStatus(Integer.parseInt(status));
					try {
						terminService.save(termin);
						builder = Response
								.ok(Helper.createResponse("SUCCESS", "", WrapperUtil.createRest(termin, userId, null, null)));
						if (status.equals(Constants.TERMIN_STATUS_ABGESAGT)) {
							sendMailService.sendEmailToTeam(termin.getTeam(), "Termin abgesagt", "Grund: " + kommentar);
						} else if (status.equals(Constants.TERMIN_STATUS_FINDET_STATT)) {
							sendMailService.sendEmailToTeam(termin.getTeam(), "Termin findet statt",
									"Grund: " + kommentar);
						}
					} catch (Exception e) {
						builder = Response.ok(Helper.createResponse("ERROR", "COULD NOT UPDATE TERMIN", null));
					}
				}
			}
		}
		return builder.build();
	}

	// get termin Vorlagen
	@GET
	@Path("/getVorlagen")
	@Produces(MediaType.APPLICATION_JSON)
	public Response getTerminVorlagen(@QueryParam("userId") String userId, @QueryParam("teamId") String teamId) {
		Response.ResponseBuilder builder = null;
		if (userId == null || teamId == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "PARAMETER IS NULL", null));
			return builder.build();
		}
		Team team = teamService.findById(teamId);
		User user = userService.findById(userId);

		if (team == null || user == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "USER ID OR TEAM ID NOT FOUND", null));
		} else {
			List<TerminVorlage> vorlagen = terminService.findVorlagenByTeamId(teamId);
			if (vorlagen == null) {
				builder = Response.ok(Helper.createResponse("ERROR", "NO VORLAGEN FOUND", null));
			} else {
				List<TerminVorlageREST> restList = new ArrayList<TerminVorlageREST>(vorlagen.size());
				for (TerminVorlage vorlage : vorlagen) {
					TerminVorlageREST rest = WrapperUtil.createRest(vorlage);
					restList.add(rest);
				}
				builder = Response.ok(Helper.createResponse("SUCCESS", "", restList));
			}
		}
		return builder.build();
	}
	
	@POST
	@Path("/setzeUserStatusInZeitraum")
	@Produces(MediaType.APPLICATION_JSON)
	public Response setzeUserStatusInZeitraum(@FormParam("startDate") String startDateString, @FormParam("endDate") String endDateString, 
			@FormParam("wochenTag") int wochenTag, @FormParam("userId") String userId, @FormParam("status") int status){
		Response.ResponseBuilder builder = null;
		if (userId == null){
			builder = Response.ok(Helper.createResponse("ERROR", "USER ID NOT SET", null));
		} else if (startDateString == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "START DATE NOT SET", null));
		} else if (endDateString == null) {
			builder = Response.ok(Helper.createResponse("ERROR", "END DATE NOT SET", null));
		} else{
			User user = userService.findById(userId);
			if(user == null){
				builder = Response.ok(Helper.createResponse("ERROR", "USER ID NOT FOUND", null));
			} else{
				Date startDate = new Date(Long.parseLong(startDateString) * 1000);
				Date endDate = new Date(Long.parseLong(endDateString) * 1000);
				List<Termin> termine = terminService.findByUserIdAndDates(userId, startDate, endDate);
				if(termine != null && termine.size() > 0){
					for(Termin termin : termine){
						Zusage zusage = Helper.getZusageFromUserInTermin(termin, user);
						if(zusage == null){
							builder = Response.ok(Helper.createResponse("ERROR", "NO ZUSAGE FOUND", null));
							return builder.build();
						} else{
							if(wochenTag == -1){
								zusage.setStatus(status);
							} else{
								Calendar terminCal = Calendar.getInstance();
								terminCal.setTime(termin.getDatum());
								int terminDay = terminCal.get(Calendar.DAY_OF_WEEK);
								int absageDay = Helper.convertToJavaWeekDay(wochenTag);
								if(terminDay == absageDay){
									zusage.setStatus(status);
								}
							}
						}
					}
					builder = Response.ok(Helper.createResponse("SUCCESS", "", null));
				} else{
					builder = Response.ok(Helper.createResponse("ERROR", "NO TERMINE FOUND", null));
				}
			}
		}
		return builder.build();
	}
}