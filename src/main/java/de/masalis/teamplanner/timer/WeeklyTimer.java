package de.masalis.teamplanner.timer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
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
import org.jboss.ejb3.annotation.TransactionTimeout;

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
	
	@TransactionTimeout(value=45, unit=TimeUnit.MINUTES)
	@Schedule(dayOfWeek = "Mon", hour = "0", minute = "0", second = "0")
	public void sendWeeklyTeamNotification() {
		List<User> users = userService.findAllWeeklyNotifiedUsers();
		if(users != null){
			Calendar nowCal = Calendar.getInstance();
			Calendar inOneWeekCal = Calendar.getInstance();
			inOneWeekCal.add(Calendar.DAY_OF_YEAR, 7);
			StringBuilder builder = new StringBuilder();
			for(User user : users){
				builder.append("<p>Hallo "+user.getVorname()+"!</p>"
						+ "<p>Hier ist dein Teamplaner-Überblick über deine Termine des Vereins "+user.getVerein().getName()+" der kommenden Woche.</p>");
				
				boolean showIntroduction = false;
				//adde fuer jedes Team alle Termine
				for(TeamRolle rolle : user.getRollen()){
					Team team = rolle.getTeam();
					List<String> teamIds = new ArrayList<String>();
					teamIds.add(team.getId());
					List<Termin> teamTermine = terminService.findByTeamIdsAndDates(teamIds, nowCal.getTime(), inOneWeekCal.getTime());
					appendTeamEntry(builder, team, teamTermine, user);
					if(team.getWeeklyTeamMailSettings() != null & team.getWeeklyTeamMailSettings().isShowIntroduction()){
						//zeige Anleitung wenn fuer min 1 Team showIntroduction = true ist
						showIntroduction = true;
					}
				}
				
				List<String> toList = new ArrayList<String>();
				toList.add(user.getEmail());
				builder.append("<p><a href=\""+Constants.LOGIN_URL+"\">Direkt zum TeamPlaner</a></p>");
				builder.append("<hr>");
				if(showIntroduction){
					builder.append(""+MailTexts.TUTORIAL_TEXT+"");
				}
				builder.append("<p>"+MailTexts.TEAM_QUESTION+"</p>");
				builder.append("<p>"+MailTexts.UNREGISTER_WEEKLY_TEXT+"</p>");
				try {
					sendMail.sendEmail(toList, "Deine wöchentliche Terminübersicht", builder.toString(), Constants.MAIL_SENDER);
					builder.setLength(0);
					log.log(Level.INFO, "Weekly mail sent");
				} catch (MessagingException e) {
					e.printStackTrace();
				}
			}
			log.log(Level.INFO, "#All Weekly mails sent: "+users.size());
		}
	}

	private void appendTerminEntry(StringBuilder builder, Termin termin, User user, int index) {
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
			if(index%2==0){
				builder.append("<tr style='background-color: rgb(216, 216, 216);'>");
			}
			else{
				builder.append("<tr style='background-color: rgb(230, 230, 230);'>");
			}
			if(termin.getStatus() == Constants.ABGESAGT){
				builder.append("<td><div style='border-radius: 50%; -moz-border-radius: 50%; -webkit-border-radius: 50%; width: 20px; height: 20px; margin-left: 5px; background-color:#d9534f; float:left;'></div> Abgesagt");
				String terminBeschreibung = termin.getBeschreibung() != null? "<br/>"+termin.getBeschreibung() : "";
				builder.append(terminBeschreibung);
				builder.append("</td>");
			}
			else{
				builder.append("<td><div style='border-radius: 50%; -moz-border-radius: 50%; -webkit-border-radius: 50%; width: 20px; height: 20px; margin-left: 5px; background-color:#5cb85c; float:left;'></div> Findet statt");
				String terminBeschreibung = termin.getBeschreibung() != null? "<br/>"+termin.getBeschreibung() : "";
				builder.append(terminBeschreibung);
				builder.append("</td>");
			}
			builder.append("<td>"+currentDay+"</td> ");
			builder.append("<td>"+currentDate+"</td> ");
			builder.append("<td>"+termin.getName()+"</td> ");
			Zusage userZusage = Helper.getZusageFromUserInTermin(termin, user);
			String userZusageString = "";
			String zusageColor="";
			if(userZusage != null){
				userZusageString = Helper.getZusageStringFromStatus(userZusage.getStatus());
				if(userZusage.getStatus() == Constants.ZUGESAGT){
					zusageColor = "color:#5cb85c;";
				}
				else if(userZusage.getStatus() == Constants.ABGESAGT){
					zusageColor = "color:#d9534f;";
				}
				else{
					zusageColor = "color : #f0ad4e;";
				}
			}
			
			builder.append("<td style='"+zusageColor+" font-weight:bold;'>"+userZusageString+"</td>");
			//builder.append("<td><a href='"+Constants.TERMIN_URL+"?terminId="+encodedTerminId+"' style='color: rgb(223, 105, 26);'>Zur Terminübersicht</a></td>");
			builder.append("</tr>");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	private void appendTeamEntry(StringBuilder builder, Team team, List<Termin> teamTermine, User user){
		builder.append("<h3>"+team.getName()+"</h3>");
		appendTeamTrainerList(builder, team);
		TeamMailSettings settings = team.getWeeklyTeamMailSettings();
		if(settings != null){
			if(settings.isShowMailText()){
				builder.append("<p>"+settings.getMailText()+"</p>");
			}
		}
		if(teamTermine == null || teamTermine.size() == 0){
			builder.append("<p>Zur Zeit gibt es für kommende Woche keine Termine für dieses Team.</p>");
		} else{
			builder.append("<div style='font-weight: bold; margin-bottom: 5px;'>Deine Termine:</div>");
			builder.append("<p><a href=\""+Constants.LOGIN_URL+"\">Direkt zum TeamPlaner</a></p>");
			builder.append("<table style='width:100%;'>");
			builder.append("<thead>");
			builder.append("<tr style='background-color:#1F3950; color: white;'>");
			builder.append("<th>Termin-Status</th><th>Tag</th><th>Datum &amp; Uhrzeit</th><th>Termin</th><th>Dein Status</th>");
			builder.append("</tr>");
			builder.append("</thead>");
			builder.append("<tbody>");
			/*for(Termin termin : teamTermine){
				appendTerminEntry(builder, termin, user);
			}*/
			for(int i = 0; i<teamTermine.size(); i++){
				appendTerminEntry(builder, teamTermine.get(i), user, i);
			}
			builder.append("</tbody></table><br>");
		}
	}
	
	private void appendTeamTrainerList(StringBuilder builder, Team team){
		List<User> trainers = Helper.getTrainerOfTeam(team);
		if(trainers.size() > 0){
			builder.append("<div style='font-weight: bold;'>Trainer:</div>");
			for(User trainer : trainers){
				builder.append("<div>");
				builder.append(trainer.getVorname()+" "+trainer.getName()+" - "
						+ "<a href=\"mailto:"+trainer.getEmail()+"\">"+trainer.getEmail()+"</a>")
				.append("</div>");
			}
			builder.append("<br />");
		}
	}
}