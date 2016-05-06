package de.masalis.teamplanner.timer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.mail.MessagingException;

import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.TeamMailSettings;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;
import org.jboss.as.quickstarts.kitchensink.service.TerminService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.util.Constants;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
import org.jboss.as.quickstarts.kitchensink.util.MailTexts;

import de.masalis.teamplanner.mail.SendMail;

@Stateless
public class WeeklyTimer {
	@Resource
	private SessionContext context;
	
	@Inject 
	UserService userService;
	
	@Inject
	SendMail sendMail;
	
	@Inject
	TerminService terminService;
	
	@Inject
    private Logger log;
	
	@Schedule(dayOfWeek = "Sun", hour = "0", minute = "0", second = "0")
	public void sendWeeklyTeamNotification() {
		List<User> users = userService.findAllWeeklyNotifiedUsers();
		if(users != null){
			Calendar nowCal = Calendar.getInstance();
			Calendar inOneWeekCal = Calendar.getInstance();
			inOneWeekCal.add(Calendar.DAY_OF_YEAR, 7);
			StringBuilder builder = new StringBuilder();
			for(User user : users){
				builder.append("Hallo "+user.getVorname()+"!<br /><br />"
						+ "Hier ist dein Teamplaner-Überblick über deine Termine der kommenden Woche.<br /><br />");
				
				//adde fuer jedes Team alle Termine
				for(TeamRolle rolle : user.getRollen()){
					Team team = rolle.getTeam();
					List<String> teamIds = new ArrayList<String>();
					teamIds.add(team.getId());
					List<Termin> teamTermine = terminService.findByTeamIdsAndDates(teamIds, nowCal.getTime(), inOneWeekCal.getTime());
					appendTeamEntry(builder, team, teamTermine, user);
				}
				
				List<String> toList = new ArrayList<String>();
				toList.add(user.getEmail());
				builder.append("<br />"+MailTexts.TUTORIAL_TEXT+"<br />");
				builder.append("<br />"+MailTexts.UNREGISTER_WEEKLY_TEXT+"<br />");
				builder.append("<br />"+MailTexts.SUPPORT_TEXT+"<br />");
				try {
					sendMail.sendEmail(toList, "Deine wöchentliche Terminübersicht", builder.toString(), "noreply-termine");
					Thread.sleep(2000);
					builder.setLength(0);
					log.log(Level.INFO, "Weekly mail sent");
				} catch (MessagingException e) {
					e.printStackTrace();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
	}

	private void appendTerminEntry(StringBuilder builder, Termin termin, User user) {
		try {
			String encodedTerminId = URLEncoder.encode(termin.getId(), "UTF-8");
			Locale locale = Locale.GERMAN;
			Calendar terminCal = Calendar.getInstance();
			terminCal.setTime(termin.getDatum());
			SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy HH:mm", locale);  
			formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
			SimpleDateFormat dayFormatter=new SimpleDateFormat("EE", locale);  
			String currentDate = formatter.format(termin.getDatum());
			String currentDay = dayFormatter.format(termin.getDatum());
			builder.append("<b>"+currentDay+"</b> ");
			builder.append(currentDate);
			builder.append(" <b>"+termin.getName()+"</b><br />");
			String terminBeschreibung = termin.getBeschreibung() != null? termin.getBeschreibung()+"<br />" : "";
			builder.append(terminBeschreibung);
			Zusage userZusage = Helper.getZusageFromUserInTermin(termin, user);
			String userZusageString = "";
			if(userZusage != null){
				userZusageString = Helper.getZusageStringFromStatus(userZusage.getStatus());
			}
			builder.append("Dein Status: <b>"+userZusageString+"</b> <a href=\""+Constants.TERMIN_URL+"?terminId="+encodedTerminId+"\">Zur Terminübersicht</a>");
			builder.append("<br />");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private void appendTeamEntry(StringBuilder builder, Team team, List<Termin> teamTermine, User user){
		builder.append("<h3>"+team.getName()+"</h3>");
		TeamMailSettings settings = team.getWeeklyTeamMailSettings();
		if(settings != null){
			if(settings.isShowMailText()){
				builder.append("<p>"+settings.getMailText()+"</p>");
			}
		}
		for(Termin termin : teamTermine){
			appendTerminEntry(builder, termin, user);
		}
	}
}