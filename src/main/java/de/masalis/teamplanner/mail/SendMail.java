package de.masalis.teamplanner.mail;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;
import javax.ejb.Asynchronous;
import javax.ejb.LocalBean;
import javax.ejb.Stateless;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeUtility;

import org.jboss.as.quickstarts.kitchensink.model.Team;
import org.jboss.as.quickstarts.kitchensink.model.TeamRolle;
import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.util.Constants;

/**
 * Session Bean implementation class GwMessage
 */
@Stateless
@LocalBean
public class SendMail {

	@Resource(mappedName = "java:jboss/mail/Default")
	Session gmailSession;

	/**
	 * Default constructor.
	 */
	public SendMail() {
	}

	public void sendEmail(List<String> toList, String subject, String content, String from) throws MessagingException{
		if(from == null){
			from = "noreply-teamplanner";
		}
		MimeMessage message = new MimeMessage(gmailSession);
		message.setFrom(new InternetAddress(from+"@masalis.de"));
		//message.addRecipients(Message.RecipientType.TO, InternetAddress.parse("teamplanner@masalis.de"));
		StringBuilder replyListBuilder = new StringBuilder();
		for(String to : toList){
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			replyListBuilder.append(to).append(",");
		}
		replyListBuilder.deleteCharAt(replyListBuilder.length()-1);
		message.setReplyTo(InternetAddress.parse(replyListBuilder.toString()));
		message.setSubject(subject, "UTF-8");
		//message.setText(content);
		message.setContent(content, "text/html; charset=utf-8");

		Transport.send(message);
	}
	
	public void sendEmailToTeam(Team team, String subject, String content) throws MessagingException{
		List<String> toList = new ArrayList<String>();
		for(TeamRolle rolle : team.getRollen()){
			User user = rolle.getUser();
			String email = user.getEmail();
			toList.add(email);
		}
		sendEmail(toList, subject, content, "noreply-"+team.getName());
	}
	
	public void sendEmailToTeamTrainer(Team team, String subject, String content) throws MessagingException{
		List<String> toList = new ArrayList<String>();
		for(TeamRolle rolle : team.getRollen()){
			User user = rolle.getUser();
			if(rolle.getRolle().equals(Constants.TRAINER_ROLE)){
				String email = user.getEmail();
				toList.add(email);
			}
		}
		sendEmail(toList, subject, content, "noreply-"+team.getName());
	}

	public void sendReminderEmailToTeamTrainer(Team team, String subject, String content) throws MessagingException{
		List<String> toList = new ArrayList<String>();
		for(TeamRolle rolle : team.getRollen()){
			User user = rolle.getUser();
			if(rolle.getRolle().equals(Constants.TRAINER_ROLE) && user.isTerminReminderMail()){
				String email = user.getEmail();
				toList.add(email);
			}
		}
		sendEmail(toList, subject, content, "noreply-"+team.getName());
	}
}