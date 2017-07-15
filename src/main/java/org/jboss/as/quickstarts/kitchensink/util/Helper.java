package org.jboss.as.quickstarts.kitchensink.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import javax.ws.rs.core.Response;

import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.TerminVorlage;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.Verein;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;
import org.jboss.as.quickstarts.kitchensink.wrapper.ResponseREST;

public class Helper {
	public static List<String> getTeamIdsForUser(User user){
		List<String> erg = new ArrayList<String>();
		for(TeamRolle rolle : user.getRollen()){
			erg.add(rolle.getTeam().getId());
		}
		return erg;
	}
	
	public static ResponseREST createResponse(String status, String description, Object data){
		ResponseREST response = new ResponseREST();
		response.status = status;
		response.description = description;
		if(data != null){
			response.data = data;
		}
		return response;
	}
	
	public static TerminVorlage createVorlage(Termin termin){
		TerminVorlage vorlage = new TerminVorlage();
		vorlage.setBeschreibung(termin.getBeschreibung());
		vorlage.setName(termin.getName());
		vorlage.setOrt(termin.getOrt());
		vorlage.setTeam(termin.getTeam());
		vorlage.setTime(termin.getDatum());
		return vorlage;
	}
	
	public static boolean checkIfUserInTeam(User user, Team team){
		String userId = user.getId();
		for(int i=0; i<team.getRollen().size(); i++){
			TeamRolle rolle = team.getRollen().get(i);
			if(rolle.getUser().getId().equals(userId)){
				return true;
			}
		}
		return false;
	}
	
	public static boolean checkIfUserInTeamAndTrainer(User user, Team team){
		String userId = user.getId();
		for(int i=0; i<team.getRollen().size(); i++){
			TeamRolle rolle = team.getRollen().get(i);
			if(rolle.getUser().getId().equals(userId)){
				if(rolle.getRolle().equals(Constants.TRAINER_ROLE)){
					return true;
				} else{
					return false;
				}
			}
		}
		return false;
	}
	
	public static TeamRolle getRoleOfUserInTeam(User user, Team team){
		String userId = user.getId();
		for(int i=0; i<team.getRollen().size(); i++){
			TeamRolle rolle = team.getRollen().get(i);
			if(rolle.getUser().getId().equals(userId)){
				return rolle;
			}
		}
		return null;
	}
	
	public static boolean checkIfTerminNeedsTerminReminder(Termin termin){
		boolean reminderSet = false;
		for(TeamRolle rolle : termin.getTeam().getRollen()){
			User user = rolle.getUser();
			Verein verein = user.getVerein();
			if(verein.getModule().isMailModul() && 
					rolle.getRolle().equals(Constants.TRAINER_ROLE) && 
					user.isTerminReminderMail()){
				reminderSet = true;
				break;
			}
		}
		return reminderSet;
	}
	
	public static ZusagenCount getTerminZusagenCounts(Termin termin, boolean trainerMitzaehlen){
		ZusagenCount count = new ZusagenCount();
		count.setTrainerMitgezaehlt(trainerMitzaehlen);
		List<Zusage> zusagen = termin.getZusagen();
		int yes=0;
		int no=0;
		int vielleicht=0;
		for(Zusage zusage : zusagen){
			User user = zusage.getUser();
			if(trainerMitzaehlen){
				if(zusage.getStatus() == Constants.ZUGESAGT){
					yes++;
				} else if(zusage.getStatus() == Constants.ABGESAGT){
					no++;
				} else{
					vielleicht++;
				}
			} else {
				if(!checkIfUserInTeamAndTrainer(user, termin.getTeam())){
					if(zusage.getStatus() == Constants.ZUGESAGT){
						yes++;
					} else if(zusage.getStatus() == Constants.ABGESAGT){
						no++;
					} else{
						vielleicht++;
					}
				}
			}
		}
		count.setMaybeCount(vielleicht);
		count.setYesCount(yes);
		count.setNoCount(no);
		return count;
	}
	
	public static Zusage getZusageFromUserInTermin(Termin termin, User user){
		for(Zusage zusage : termin.getZusagen()){
			if(zusage.getUser().getId().equals(user.getId())){
				return zusage;
			}
		}
		return null;
	}
	
	public static int convertFromJavaWeekDay(int javaDayOfWeek){
		switch(javaDayOfWeek){
			case Calendar.MONDAY: return 0;
			case Calendar.TUESDAY: return 1;
			case Calendar.WEDNESDAY: return 2;
			case Calendar.THURSDAY: return 3;
			case Calendar.FRIDAY: return 4;
			case Calendar.SATURDAY: return 5;
			case Calendar.SUNDAY: return 6;
			default: return -1;
		}
	}
	
	public static int convertToJavaWeekDay(int dayOfWeek){
		switch(dayOfWeek){
			case 0: return Calendar.MONDAY;
			case 1: return Calendar.TUESDAY;
			case 2: return Calendar.WEDNESDAY;
			case 3: return Calendar.THURSDAY;
			case 4: return Calendar.FRIDAY;
			case 5: return Calendar.SATURDAY;
			case 6: return Calendar.SUNDAY;
			default: return -1;
		}
	}
	
	public static String getZusageStringFromStatus(int zusageStatus){
		switch(zusageStatus){
			case Constants.ABGESAGT: return "Abgesagt";
			case Constants.ZUGESAGT: return "Zugesagt";
			case Constants.VIELLEICHT: return "Vielleicht";
			default: return "";
		}
	}

	public static int getRollenCountInTeam(Team team, String rolle) {
		int rollenCount = 0;
		for(TeamRolle teamRolle : team.getRollen()){
			if(teamRolle.getRolle().equals(rolle)){
				rollenCount++;
			}
		}
		return rollenCount;
	}
	
	public static List<User> getTrainerOfTeam(Team team){
		List<User> trainer = new ArrayList<User>();
		for(TeamRolle rolle : team.getRollen()){
			if(rolle.getRolle().equals(Constants.TRAINER_ROLE)){
				trainer.add(rolle.getUser());
			}
		}
		return trainer;
	}
	
	public static boolean checkResponseInvNotFound(Response.ResponseBuilder responseBuilder){
		return checkStatusAndDescriptionOfResponse(responseBuilder, "ERROR", "INVITATION NOT FOUND");
	}
	
	public static boolean checkStatusAndDescriptionOfResponse(Response.ResponseBuilder responseBuilder, String status, String description){
		boolean res = false;
		if(responseBuilder != null ){
			Response response = responseBuilder.build();
			if(response != null){
				if(response.getEntity() instanceof ResponseREST){
					ResponseREST responseRest = (ResponseREST)response.getEntity();
					if(responseRest.status != null && responseRest.description != null){
						if(responseRest.status.equals(status) && responseRest.description.equals(description)){
							res = true;
						}
					}
				}
			}
			
		}
		return res;
	}
}
