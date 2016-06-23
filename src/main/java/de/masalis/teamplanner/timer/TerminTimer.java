package de.masalis.teamplanner.timer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
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
					if(Helper.checkIfTerminNeedsTerminReminder(termin)){
						Team team = termin.getTeam();
						int hoursBefore = 2;
						if(team.getWeeklyTeamMailSettings() != null){
							hoursBefore = team.getWeeklyTeamMailSettings().getHoursBeforeTrainerReminder();
						}
						for(TeamRolle rolle : team.getRollen()){
							if(rolle.getRolle().equals(Constants.TRAINER_ROLE)){
								User user = rolle.getUser();
								Calendar reminderTimeCal = Calendar.getInstance();
								reminderTimeCal.setTime(termin.getDatum());
								reminderTimeCal.add(Calendar.HOUR_OF_DAY, hoursBefore*(-1));
								TimerInfo info = new TimerInfo();
								info.setTerminId(termin.getId());
								info.setUserId(user.getId());
								try {
									timerService.createTimer(reminderTimeCal.getTime(), info);
									amountScheduled++;
									// TerminReminder terminReminder = new TerminReminder();
									// terminReminder.setTerminId(termin.getId());
									// terminReminder.setTimerHandle(timer.getHandle());
									//terminReminderService.save(terminReminder);
								} catch (Exception e) {
									// timer.cancel();
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
			
			// timer = timerService.createSingleActionTimer(duration, new TimerConfig());
			//System.out.println("time remaining to timeout:" +timer.getTimeRemaining());
			log.log(Level.INFO, "TerminTimer: Scheduled Timers: "+amountScheduled);
		}
	}

	@Timeout
	public void timeout(Timer timer) {
		TimerInfo info = (TimerInfo)timer.getInfo();
		String terminId = info.getTerminId();
		String userId = info.getUserId();
		Locale locale = Locale.GERMANY;
		if(terminId != null && userId != null){
			SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy HH:mm", locale); 
			formatter.setTimeZone(TimeZone.getTimeZone("Europe/Berlin"));
			// TerminReminder reminder = terminReminderService.findReminderByTermin(terminId);
			try {
				Termin termin = terminService.findById(terminId);
				User user = userService.findById(userId);
				Team team = termin.getTeam();
				if(termin != null && user != null && team != null){
					ZusagenCount count = Helper.getTerminZusagenCounts(termin, false);
					StringBuilder builder = new StringBuilder();
					builder.append("Hallo "+user.getVorname()+"!<br /><br />Hier ist dein Überblick über kommenden Termin <b>'"+termin.getName()+"'</b> am <b>"+formatter.format(termin.getDatum())+"</b>.<br />");
					builder.append("<br />Gesamtzahl der Spieler: " + count.getYesCount() + " Zusagen, " + count.getNoCount()+ " Absagen und " + count.getMaybeCount() + " Vielleichts.");
					builder.append("<br /><b>Zusagen:</b><br />");
					createZusagenList(builder, termin);
					
					builder.append("<br /><b>Absagen:</b><br />");
					createAbsagenList(builder, termin);
					
					builder.append("<br /><b>Vielleicht:</b><br />");
					createVielleichtList(builder, termin);
					
					builder.append("<br />").append(MailTexts.UNREGISTER_TRAINER_NOTIF_TEXT);
					List<String> toList = new ArrayList<String>();
					toList.add(user.getEmail());
					sendMail.sendEmail(toList, "Statusmail Termin "+formatter.format(termin.getDatum()), builder.toString(), Constants.MAIL_SENDER);
					// terminReminderService.delete(reminder);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		log.log(Level.INFO, "TerminTimer: Termin Reminder occurred: terminId:"+terminId+", userId:"+userId);
	}
	
	private void createZusagenList(StringBuilder builder, Termin termin){
		for(Zusage zusage : termin.getZusagen()){
			User user = zusage.getUser();
			if(zusage.getStatus() == Constants.ZUGESAGT){
				builder.append(user.getVorname()+" "+user.getName()+"<br />");
			}
		}
	}
	
	private void createAbsagenList(StringBuilder builder, Termin termin){
		for(Zusage zusage : termin.getZusagen()){
			User user = zusage.getUser();
			if(zusage.getStatus() == Constants.ABGESAGT){
				builder.append(user.getVorname()+" "+user.getName()+"<br />");
			}
		}
	}
	
	private void createVielleichtList(StringBuilder builder, Termin termin){
		for(Zusage zusage : termin.getZusagen()){
			User user = zusage.getUser();
			if(zusage.getStatus() == Constants.VIELLEICHT){
				builder.append(user.getVorname()+" "+user.getName()+"<br />");
			}
		}
	}
}
