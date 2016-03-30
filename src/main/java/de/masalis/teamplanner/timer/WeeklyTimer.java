package de.masalis.teamplanner.timer;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.Resource;
import javax.ejb.Schedule;
import javax.ejb.SessionContext;
import javax.ejb.Stateless;
import javax.ejb.TimerService;
import javax.inject.Inject;
import javax.mail.MessagingException;

import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.service.TerminService;
import org.jboss.as.quickstarts.kitchensink.service.UserService;
import org.jboss.as.quickstarts.kitchensink.util.Constants;

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
	
	@Schedule(hour = "0", minute = "0", second = "0")
	public void sendWeeklyNotification() {
		TimerService timerService = context.getTimerService();
		//int allTimersCount = timerService.getAllTimers().size();
		List<User> users = userService.findAllWeeklyNotifiedUsers();
		if(users != null){
			Calendar nowCal = Calendar.getInstance();
			Calendar inOneWeekCal = Calendar.getInstance();
			inOneWeekCal.add(Calendar.DAY_OF_YEAR, 7);
			StringBuilder builder = new StringBuilder();
			for(User user : users){
				List<Termin> termine = terminService.findByUserIdAndDates(user.getId(), nowCal.getTime(), inOneWeekCal.getTime());
				List<String> toList = new ArrayList<String>();
				toList.add(user.getEmail());
				builder.append("Hallo "+user.getVorname()+"!<br /><br />"
						+ "Hier ist dein wöchentlicher Überblick über alle deine kommenden Termine:<br /><br />");
				for(Termin termin : termine){
					appendTerminEntry(builder, termin);
				}
				builder.append("<br /><br />P.S. Hast du Probleme mit dieser Mail? Dann wende dich bitte an <a href=\"mailto:bastian.golic@gmx.de\">bastian.golic@gmx.de</a>");
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
		//System.out.println("Scheduled Timer");
	}

	private void appendTerminEntry(StringBuilder builder, Termin termin) {
		try {
			String encodedTerminId = URLEncoder.encode(termin.getId(), "UTF-8");
			Locale locale = Locale.GERMAN;
			Calendar terminCal = Calendar.getInstance();
			terminCal.setTime(termin.getDatum());
			SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy HH:mm", locale);  
			SimpleDateFormat dayFormatter=new SimpleDateFormat("EE", locale);  
			String currentDate = formatter.format(termin.getDatum());
			String currentDay = dayFormatter.format(termin.getDatum());
			builder.append("<b>"+currentDay+"</b> ");
			builder.append(currentDate);
			builder.append(" <b>"+termin.getName()+"</b> <a href=\""+Constants.TERMIN_URL+"?terminId="+encodedTerminId+"\">Zur Terminübersicht</a>");
			builder.append("<br />");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
}