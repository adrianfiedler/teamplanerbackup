package de.masalis.teamplanner.timer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.Timeout;
import javax.ejb.Timer;
import javax.ejb.TimerConfig;
import javax.ejb.TimerService;
import javax.inject.Inject;

import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.TerminReminder;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;
import org.jboss.as.quickstarts.kitchensink.service.TerminReminderService;
import org.jboss.as.quickstarts.kitchensink.service.TerminService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.util.Constants;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.util.MailTexts;
import org.jboss.as.quickstarts.kitchensink.util.ZusagenCount;
import org.jboss.ejb3.annotation.TransactionTimeout;

import de.masalis.teamplanner.mail.SendMail;

@Stateless
public class TerminTimer {
	@Inject
    private Logger log;
	
	@Resource
	private SessionContext context;
	
	@Inject 
	TerminService terminService;
	
	@Inject 
	UserService userService;
	
	@Inject
	SendMail sendMail;
	
	@Inject
	TerminReminderService terminReminderService;
	
	@TransactionTimeout(value=45, unit=TimeUnit.MINUTES)
	@Schedule(hour = "0", minute = "0", second = "0")
	public void createBeforeTerminNotification() {
		log.log(Level.INFO, "TerminTimer: Termin Reminder schedulings starting...");
		TimerService timerService = context.getTimerService();
		if(timerService != null){
			int amountScheduled = 0;
			Calendar nowCal = Calendar.getInstance();
			Calendar nextDayCal = Calendar.getInstance();
			nextDayCal.add(Calendar.DAY_OF_YEAR, 1);
			
			List<Termin> nextDayTermine = terminService.findAllByDates(nowCal.getTime(), nextDayCal.getTime());
			if(nextDayTermine != null){
				for(Termin termin : nextDayTermine){
					Team team = termin.getTeam();
					if(team.getVerein().getModule().isMailModul()){
						int hoursBefore = 2;
						if(team.getWeeklyTeamMailSettings() != null){
							hoursBefore = team.getWeeklyTeamMailSettings().getHoursBeforeTrainerReminder();
						}
						Calendar reminderTimeCal = Calendar.getInstance();
						reminderTimeCal.setTime(termin.getDatum());
						reminderTimeCal.add(Calendar.HOUR_OF_DAY, hoursBefore*(-1));
						TimerInfo info = new TimerInfo();
						info.setTerminId(termin.getId());
						try {
							timerService.createTimer(reminderTimeCal.getTime(), info);
							log.log(Level.INFO, "Scheduled Timer for team "+team.getId()+" and termin "+termin.getId()+" on date: "+reminderTimeCal.getTime().toString());
							amountScheduled++;
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}
			log.log(Level.INFO, "TerminTimer: Scheduled Timers: "+amountScheduled);
		}
	}

	@Timeout
	public void timeout(Timer timer) {
		TimerInfo info = (TimerInfo)timer.getInfo();
		String terminId = info.getTerminId();
		log.log(Level.INFO, "TerminTimer: Termin Reminder occurred: terminId:"+terminId);
		if(terminId != null){
			Locale locale = Locale.GERMANY;
			SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy HH:mm", locale); 
			formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
			try {
				Termin termin = terminService.findById(terminId);
				Team team = termin.getTeam();
				List<User> teamTrainer = userService.findTrainerByTeamId(team.getId());
				for(User user : teamTrainer){
					if(user.isTerminReminderMail()){
						if(termin != null && user != null && team != null){
							StringBuilder builder = buildTerminMail(formatter, termin, team, user);
							List<String> toList = new ArrayList<String>();
							toList.add(user.getEmail());
							sendMail.sendEmail(toList, "Statusmail Termin "+formatter.format(termin.getDatum()), builder.toString(), Constants.MAIL_SENDER);
						}
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private StringBuilder buildTerminMail(SimpleDateFormat formatter, Termin termin, Team team, User user) {
		ZusagenCount count = Helper.getTerminZusagenCounts(termin, false);
		StringBuilder builder = new StringBuilder();
		builder.append("<p>Hallo "+user.getVorname()+"!</p><p>Hier ist dein Überblick über kommenden Termin <b>'"+termin.getName()+"'</b> am <b>"+formatter.format(termin.getDatum())+"</b>.</p>");
		HashMap<String, ArrayList<String>> statusMap = createUserStatusMap(termin);
		if(team.getTeamSettings().isTrainerMussZusagen() == true){
			int countRows = Math.max(statusMap.get("TR_ZU").size(), statusMap.get("TR_AB").size());
			builder.append("<p>Trainer:</p>");
			builder.append("<table style='width: 100%;'>");
			builder.append("<thead><tr><th style='text-align: left; color:#5cb85c;' >Zusagen</th><th style='text-align: left; color:#d9534f;'>Absagen</th>");
			if(termin.isMaybeAllowed()){
				builder.append("<th style='text-align: left; color : #f0ad4e;'>Vielleicht</th>");
				countRows = Math.max(countRows, statusMap.get("TR_VI").size());
			}
			builder.append("</tr></thead>");
			builder.append("<tbody>");
			for(int i = 0; i<countRows; i++){
				builder.append("<tr>");
				if(i<statusMap.get("TR_ZU").size()){
					builder.append(statusMap.get("TR_ZU").get(i));
				}
				else{
					builder.append(createEmptyZusageHTML());
				}
				if(i<statusMap.get("TR_AB").size()){
					builder.append(statusMap.get("TR_AB").get(i));
				}
				else{
					builder.append(createEmptyZusageHTML());
				}
				if(termin.isMaybeAllowed()){
					if(i<statusMap.get("TR_VI").size()){
						builder.append(statusMap.get("TR_VI").get(i));
					}
					else{
						builder.append(createEmptyZusageHTML());
					}
				}
				builder.append("</tr>");
			}
			builder.append("</tbody>");
			builder.append("</table>");
		}
		builder.append("<p>Gesamtzahl der Spieler: " + count.getYesCount() + " Zusagen, " + count.getNoCount()+ " Absagen und " + count.getMaybeCount() + " Vielleichts."+"</p>");
		
		int countRows = Math.max(statusMap.get("SP_ZU").size(), statusMap.get("SP_AB").size());
		builder.append("<p>Spieler:</p>");
		builder.append("<table style='width: 100%;'>");
		builder.append("<thead><tr><th style='text-align: left; color:#5cb85c;'>Zusagen</th><th style='text-align: left; color:#d9534f;'>Absagen</th>");
		if(termin.isMaybeAllowed()){
			builder.append("<th style='text-align: left; color : #f0ad4e;'>Vielleicht</th>");
			countRows = Math.max(countRows, statusMap.get("SP_VI").size());
		}
		builder.append("</tr></thead>");
		builder.append("<tbody>");
		for(int i = 0; i<countRows; i++){
			builder.append("<tr>");
			if(i<statusMap.get("SP_ZU").size()){
				builder.append(statusMap.get("SP_ZU").get(i));
			}
			else{
				builder.append(createEmptyZusageHTML());
			}
			if(i<statusMap.get("SP_AB").size()){
				builder.append(statusMap.get("SP_AB").get(i));
			}
			else{
				builder.append(createEmptyZusageHTML());
			}
			if(termin.isMaybeAllowed()){
				if(i<statusMap.get("SP_VI").size()){
					builder.append(statusMap.get("SP_VI").get(i));
				}
				else{
					builder.append(createEmptyZusageHTML());
				}
			}
			builder.append("</tr>");
		}
		builder.append("</tbody>");
		builder.append("</table>");
		
		builder.append("<p>").append(MailTexts.UNREGISTER_TRAINER_NOTIF_TEXT).append("</p>");
		return builder;
	}
	
	/**
	 * Erstellt eine Map die die Terminzusagen nach Rolle und Status für HTML aufbereitet
	 * Key 'yy_xx': yy TR oder SP für Trainer oder Spieler. xx ZU, AB oder VI für Zusage, Absage oder Vielleicht.
	 * Als Value ist eine Liste mit allen HTML Strings für alle entsprechenden Zusageobjekten
	 * @param termin
	 * @return Map die die Terminzusagen nach Rolle und Status für HTML aufbereitet
	 */
	private HashMap<String, ArrayList<String>> createUserStatusMap(Termin termin){
		HashMap<String, ArrayList<String>> resultMap = new HashMap<String, ArrayList<String>>();
		//init Map
		resultMap.put("SP_ZU", new ArrayList<String>());
		resultMap.put("SP_AB", new ArrayList<String>());
		resultMap.put("SP_VI", new ArrayList<String>());
		resultMap.put("TR_ZU", new ArrayList<String>());
		resultMap.put("TR_AB", new ArrayList<String>());
		resultMap.put("TR_VI", new ArrayList<String>());
		
		for(Zusage zusage : termin.getZusagen()){
			User user = zusage.getUser();
			if(zusage.getStatus() == Constants.ZUGESAGT){
				if(Helper.getRoleOfUserInTeam(user, termin.getTeam()).getRolle().equals(Constants.TRAINER_ROLE)){
					resultMap.get("TR_ZU").add(createZusageHTML(true, user.getVorname()+" "+user.getName()));
				}
				else{
					resultMap.get("SP_ZU").add(createZusageHTML(true, user.getVorname()+" "+user.getName()));
				}
			}
			else if(zusage.getStatus() == Constants.ABGESAGT){
				if(Helper.getRoleOfUserInTeam(user, termin.getTeam()).getRolle().equals(Constants.TRAINER_ROLE)){
					resultMap.get("TR_AB").add(createZusageHTML(true, user.getVorname()+" "+user.getName()));
				}
				else{
					resultMap.get("SP_AB").add(createZusageHTML(true, user.getVorname()+" "+user.getName()));
				}
			}
			else{
				if(Helper.getRoleOfUserInTeam(user, termin.getTeam()).getRolle().equals(Constants.TRAINER_ROLE)){
					resultMap.get("TR_VI").add(createZusageHTML(true, user.getVorname()+" "+user.getName()));
				}
				else{
					resultMap.get("SP_VI").add(createZusageHTML(true, user.getVorname()+" "+user.getName()));
				}
			}
		}
		
		return resultMap;
	}
	
	/**
	 * Erstellt einen HTML String für ein Zusageobjekt.
	 * @param trainer Boolean, ob es sich bei dem Nutzer um einen Trainer handelt. Wenn ja kann eventuell eine andere Styleklasse hinzugefügt werden.
	 * @param name Name des Nutzers der Zusage
	 * @return HTML String: in diesem Fall eine HTML <td> Spalte
	 */
	private String createZusageHTML(boolean trainer, String name){
		return "<td>"+name+"</td>";
	}
	
	/**
	 * Erstellt einen HTML String für ein leeres Zusageobjekt
	 * @return HTML String: in diesem Fall eine leere HTML <td> Spalte
	 */
	private String createEmptyZusageHTML(){
		return "<td></td>";
	}
	
	/**
     * @deprecated
     * New Method createUserStatusMap um mehrfache for-Schleifen zu vermeiden
     */
    @Deprecated
	private void createZusagenList(StringBuilder builder, Termin termin){
		for(Zusage zusage : termin.getZusagen()){
			User user = zusage.getUser();
			if(zusage.getStatus() == Constants.ZUGESAGT){
				builder.append(user.getVorname()+" "+user.getName()+"<br />");
			}
		}
	}
	
    /**
     * @deprecated
     * New Method createUserStatusMap um mehrfache for-Schleifen zu vermeiden
     */
    @Deprecated
	private void createAbsagenList(StringBuilder builder, Termin termin){
		for(Zusage zusage : termin.getZusagen()){
			User user = zusage.getUser();
			if(zusage.getStatus() == Constants.ABGESAGT){
				builder.append(user.getVorname()+" "+user.getName()+"<br />");
			}
		}
	}
	
    /**
     * @deprecated
     * New Method createUserStatusMap um mehrfache for-Schleifen zu vermeiden
     */
    @Deprecated
	private void createVielleichtList(StringBuilder builder, Termin termin){
		for(Zusage zusage : termin.getZusagen()){
			User user = zusage.getUser();
			if(zusage.getStatus() == Constants.VIELLEICHT){
				builder.append(user.getVorname()+" "+user.getName()+"<br />");
			}
		}
	}
}
