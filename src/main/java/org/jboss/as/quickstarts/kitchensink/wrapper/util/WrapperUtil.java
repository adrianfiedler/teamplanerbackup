package org.jboss.as.quickstarts.kitchensink.wrapper.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.as.quickstarts.kitchensink.constants.Rolle;
import org.jboss.as.quickstarts.kitchensink.model.Einladung;
import org.jboss.as.quickstarts.kitchensink.model.LoginToken;
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
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.wrapper.EinladungREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.LoginTokenREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.OrtREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.SerieREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.SpielerZusageREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TeamListREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TeamMitgliedREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TeamREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TeamZusageREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TerminREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TerminSettingsREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TerminVorlageREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.TrainerZusageREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.UserREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.UserSettingsREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.UserZusageREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.VereinREST;

public class WrapperUtil {
	public static UserREST createLoginRest(User user, LoginToken loginToken){
		UserREST rest = new UserREST();
		rest.id = user.getId();
		rest.admin = user.isAdmin();
		rest.email = user.getEmail();
		rest.facebookToken = user.getFacebookToken();
		rest.name = user.getName();
		rest.vorname = user.getVorname();
		rest.verein = createLoginRest(user.getVerein(), user);
		if(loginToken != null){
			rest.loginToken = createRest(loginToken);
		}
		return rest;
	}
	
	public static VereinREST createLoginRest(Verein verein, User user){
		VereinREST rest = new VereinREST();
		rest.name = verein.getName();
		rest.id = verein.getId();
		List<TeamREST> teams = new ArrayList<TeamREST>();
		for(TeamRolle rolle : user.getRollen()){
			TeamREST team = createLoginRest(rolle, user);
			teams.add(team);
		}
		rest.teams = teams;
		return rest;
	}
	
	public static TeamREST createLoginRest(TeamRolle rolle, User user){
		TeamREST rest = new TeamREST();
		Team team = rolle.getTeam();
		rest.name = team.getName();
		rest.id = team.getId();
		rest.userRolle = rolle.getRolle();
		return rest;
	}
	
	public static TerminREST createRest(Termin termin, String userId, Termin prevTermin, Termin nextTermin){
		TerminREST rest = new TerminREST();
		rest.beschreibung = termin.getBeschreibung();
		rest.datum = termin.getDatum();
		Team team = termin.getTeam();
		rest.name = termin.getName();
		rest.status = termin.getStatus();
		rest.absageKommentar = termin.getAbsageKommentar();
		rest.terminSettings = new TerminSettingsREST();
		rest.terminSettings.maybeAllowed = termin.isMaybeAllowed();
		rest.teamZusagen = createRest();
		rest.teamId = termin.getTeam().getId();
		if(termin.getOrt() != null){
			rest.ort = createRest(termin.getOrt());
		}
		if(termin.getSerie() != null){
			rest.serie = createRest(termin.getSerie());
		}
		List<String> alleVornamen = new ArrayList<String>();
		for(Zusage zusage : termin.getZusagen()){
			alleVornamen.add(zusage.getUser().getVorname());
		}
		for(Zusage zusage : termin.getZusagen()){
			User user = zusage.getUser();
			int frequency = Collections.frequency(alleVornamen, user.getVorname());
			createZusagen(userId, rest, team, zusage, user, frequency);
			
			if(!Helper.checkIfUserInTeamAndTrainer(user, team)){
				switch(zusage.getStatus()){
					case 0: rest.noCount++; break;
					case 1: rest.yesCount++; break;
					case 2: rest.maybeCount++; break;
				}
			}
		}
		rest.id = termin.getId();
		if(nextTermin != null && nextTermin.getId() != null){
			rest.nextTerminId = nextTermin.getId();
		}
		if(prevTermin != null && prevTermin.getId() != null){
			rest.previousTerminId = prevTermin.getId();
		}
		return rest;
	}

	public static void createZusagen(String userId, TerminREST rest, Team team, Zusage zusage, User user,
			int frequency) {
		for(TeamRolle rolle : user.getRollen()){
			if(rolle.getTeam().getId().equals(team.getId())){
				if(userId.equals(user.getId())){
					UserZusageREST userZusage = new UserZusageREST();
					userZusage.status = zusage.getStatus();
					userZusage.kommentar = zusage.getKommentar();
					userZusage.rolle = rolle.getRolle();
					userZusage.displayName = user.getVorname();
					rest.userZusage = userZusage;
				} else{
					if(rolle.getRolle().equals(Rolle.TRAINER)){
						TrainerZusageREST trainerZusage = new TrainerZusageREST();
						trainerZusage.rolle = rolle.getRolle();
						trainerZusage.status = zusage.getStatus();
						trainerZusage.kommentar = zusage.getKommentar();
						if(frequency > 1){
							trainerZusage.displayName = user.getVorname() + " " +user.getName();
						} else{
							trainerZusage.displayName = user.getVorname();
						}
						rest.teamZusagen.trainerZusagen.add(trainerZusage);
					} else if(rolle.getRolle().equals(Rolle.SPIELER)){
						SpielerZusageREST spielerZusage = new SpielerZusageREST();
						spielerZusage.rolle = rolle.getRolle();
						spielerZusage.status = zusage.getStatus();
						spielerZusage.kommentar = zusage.getKommentar();
						if(frequency > 1){
							spielerZusage.displayName = user.getVorname() + " " +user.getName();
						} else{
							spielerZusage.displayName = user.getVorname();
						}
						rest.teamZusagen.spielerZusagen.add(spielerZusage);
					}
				}
				break;
			}
		}
	}
	
	public static OrtREST createRest(Ort ort){
		OrtREST rest = new OrtREST();
		rest.beschreibung = ort.getBeschreibung();
		rest.nummer = ort.getNummer();
		rest.strasse = ort.getStrasse();
		rest.plz = ort.getPlz();
		rest.stadt = ort.getStadt();
		rest.vorlage = ort.isVorlage();
		rest.id = ort.getId();
		rest.longitude = ort.getLongitude();
		rest.latitude = ort.getLatitude();
		return rest;
	}
	
	public static SerieREST createRest(Serie serie){
		SerieREST rest = new SerieREST();
		rest.intervall = serie.getIntervall();
		return rest;
	}
	
	public static TeamZusageREST createRest(){
		TeamZusageREST rest = new TeamZusageREST();
		rest.spielerZusagen = new ArrayList<SpielerZusageREST>();
		rest.trainerZusagen = new ArrayList<TrainerZusageREST>();
		return rest;
	}
	
	public static TeamListREST createTeamListRest(Team team, User user, List<Einladung> einladungen){
		TeamListREST rest = new TeamListREST();
		rest.mitglieder = new ArrayList<TeamMitgliedREST>();
		for(TeamRolle rolle : team.getRollen()){
			TeamMitgliedREST userRest = new TeamMitgliedREST();
			userRest.rolle = rolle.getRolle();
			userRest.user = createListRest(rolle.getUser());
			if(rolle.getUser().getId().equals(user.getId())){
				rest.user = userRest;
			} else{
				rest.mitglieder.add(userRest);
			}
		}
		if(einladungen != null){
			rest.einladungen = createRest(einladungen);
		}
		return rest;
	}
	
	private static List<EinladungREST> createRest(List<Einladung> einladungen) {
		List<EinladungREST> rest = new ArrayList<EinladungREST>(einladungen.size());
		for(Einladung einladung : einladungen){
			EinladungREST einladungRest = new EinladungREST();
			einladungRest.email = einladung.getEmail();
			einladungRest.name = einladung.getName();
			einladungRest.vorname = einladung.getVorname();
			einladungRest.status = einladung.getStatus();
			rest.add(einladungRest);
		}
		return rest;
	}

	public static UserREST createListRest(User user){
		UserREST rest = new UserREST();
		rest.id = user.getId();
		rest.admin = user.isAdmin();
		rest.email = user.getEmail();
		rest.facebookToken = user.getFacebookToken();
		rest.name = user.getName();
		rest.vorname = user.getVorname();
		rest.active = user.isAktiviert();
		//rest.verein = createLoginRest(user.getVerein(), user);
		return rest;
	}

	public static TerminVorlageREST createRest(TerminVorlage vorlage) {
		TerminVorlageREST rest = new TerminVorlageREST();
		rest.beschreibung = vorlage.getBeschreibung();
		rest.time = vorlage.getTime();
		rest.name = vorlage.getName();
		rest.id = vorlage.getId();
		if(vorlage.getOrt() != null){
			rest.ort = createRest(vorlage.getOrt());
		}
		return rest;
	}
	
	public static LoginTokenREST createRest(LoginToken loginToken){
		LoginTokenREST rest = new LoginTokenREST();
		rest.timeOut = loginToken.getTimeOut();
		rest.token = loginToken.getToken();
		return rest;
	}

	public static UserSettingsREST createRest(UserSettings userSettings, String userId) {
		if(userSettings != null){
			UserSettingsREST rest = new UserSettingsREST();
			rest.userId = userId;
			rest.montagsAbsagen = userSettings.isMontagsAbsagen();
			rest.dienstagsAbsagen = userSettings.isDienstagsAbsagen();
			rest.mittwochsAbsagen = userSettings.isMittwochsAbsagen();
			rest.donnerstagsAbsagen = userSettings.isDonnerstagsAbsagen();
			rest.freitagsAbsagen = userSettings.isFreitagsAbsagen();
			rest.samstagsAbsagen = userSettings.isSamstagsAbsagen();
			rest.sonntagsAbsagen = userSettings.isSonntagsAbsagen();
			return rest;
		} else{
			return null;
		}
	}
	
}
