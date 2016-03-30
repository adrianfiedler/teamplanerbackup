package de.masalis.teamplanner.timer;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
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

import org.jboss.as.quickstarts.kitchensink.model.Termin;
import org.jboss.as.quickstarts.kitchensink.model.TerminReminder;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;
import org.jboss.as.quickstarts.kitchensink.service.TerminReminderService;
import org.jboss.as.quickstarts.kitchensink.service.TerminService;
import org.jboss.as.quickstarts.kitchensink.util.Constants;
import org.jboss.as.quickstarts.kitchensink.util.Helper;
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
	SendMail sendMail;
	
	@Inject
	TerminReminderService terminReminderService;
	
	@Schedule(dayOfWeek = "0", hour = "1", minute = "0", second = "0")
	public void test() {
		TimerService timerService = context.getTimerService();
		if(timerService != null){
			Calendar nowCal = Calendar.getInstance();
			Calendar nextDayCal = Calendar.getInstance();
			nextDayCal.add(Calendar.DAY_OF_YEAR, 1);
			
			List<Termin> nextDayTermine = terminService.findAllByDates(nowCal.getTime(), nextDayCal.getTime());
			if(nextDayTermine != null){
				for(Termin termin : nextDayTermine){
					if(Helper.checkIfTerminNeedsTerminReminder(termin)){
						Calendar reminderTimeCal = Calendar.getInstance();
						reminderTimeCal.setTime(termin.getDatum());
						reminderTimeCal.add(Calendar.HOUR_OF_DAY, -1);
						TimerInfo info = new TimerInfo();
						info.setTerminId(termin.getId());
						Timer timer = timerService.createTimer(reminderTimeCal.getTime(), info);
						TerminReminder terminReminder = new TerminReminder();
						terminReminder.setTerminId(termin.getId());
						//terminReminder.setTimerHandle(timer.getHandle());
						try {
							terminReminderService.save(terminReminder);
						} catch (Exception e) {
							timer.cancel();
							e.printStackTrace();
						}
					}
				}
			}
			
			// timer = timerService.createSingleActionTimer(duration, new TimerConfig());
			//System.out.println("time remaining to timeout:" +timer.getTimeRemaining());
		}
	}

	@Timeout
	public void timeout(Timer timer) {
		TimerInfo info = (TimerInfo)timer.getInfo();
		String terminId = info.getTerminId();
		Locale locale = Locale.GERMANY;
		if(terminId != null){
			SimpleDateFormat formatter=new SimpleDateFormat("dd.MM.yyyy HH:mm", locale); 
			TerminReminder reminder = terminReminderService.findReminderByTermin(terminId);
			try {
				terminReminderService.delete(reminder);
				Termin termin = terminService.findById(terminId);
				if(termin != null){
					ZusagenCount count = Helper.getTerminZusagenCounts(termin, false);
					StringBuilder builder = new StringBuilder();
					builder.append("Hallo!<br /><br />Hier ist dein Überblick über kommenden Termin ("+formatter.format(termin.getDatum())+").");
					builder.append("<br />" + count.getYesCount() + " Zusagen und " + count.getNoCount()+ " Absagen");
					builder.append("<br />Zusagen:<br />");
					createZusagenList(builder, termin);
					
					builder.append("<br />Absagen:<br />");
					createAbsagenList(builder, termin);
					
					sendMail.sendReminderEmailToTeamTrainer(termin.getTeam(), "Statusmail Termin "+formatter.format(termin.getDatum()), 
							builder.toString());
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			
		}
		log.log(Level.INFO, "TimerBean: Termin Reminder occurred: terminId:"+terminId);
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
