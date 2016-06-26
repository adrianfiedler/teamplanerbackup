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
import org.jboss.as.quickstarts.kitchensink.util.MailTexts;

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
			from = "noreply-teamplaner";
		}
		MimeMessage message = new MimeMessage(gmailSession);
		message.setFrom(new InternetAddress(from+"@masalis.de"));
		//message.addRecipients(Message.RecipientType.TO, InternetAddress.parse("teamplaner@masalis.de"));
		StringBuilder replyListBuilder = new StringBuilder();
		for(String to : toList){
			message.addRecipients(Message.RecipientType.TO, InternetAddress.parse(to));
			replyListBuilder.append(to).append(",");
		}
		replyListBuilder.deleteCharAt(replyListBuilder.length()-1);
		message.setReplyTo(InternetAddress.parse(replyListBuilder.toString()));
		message.setSubject(subject, "UTF-8");
		//message.setText(content);
		message.setContent(generateHTMLMail(content).replaceAll("(\r\n|\n)", "<br />"), "text/html; charset=utf-8");

		Transport.send(message);
	}
	
	public void sendEmailToTeam(Team team, String subject, String content) throws MessagingException{
		List<String> toList = new ArrayList<String>();
		for(TeamRolle rolle : team.getRollen()){
			User user = rolle.getUser();
			String email = user.getEmail();
			toList.add(email);
		}
		sendEmail(toList, subject, content, Constants.MAIL_SENDER);
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
		sendEmail(toList, subject, content, Constants.MAIL_SENDER);
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
		sendEmail(toList, subject, content, Constants.MAIL_SENDER);
	}
	
	public String generateHTMLMail(String content){
		StringBuilder builder = new StringBuilder();
		builder.append("<!Doctype html>");
		builder.append("<html>");
		builder.append("<head>");
		builder.append("<style>");
		builder.append(".headerDiv{height: 48px; width: 100%;} ");
		builder.append(".bgBlue{background-color:#1F3950;} ");
		builder.append(".colorOrange, a{color: rgb(223, 105, 26);} ");
		builder.append(".headerLabel{line-height: 48px; float: left; text-align: justify;  padding-right: 10px; font-size: 25px; font-weight: bold; font-style: italic;} ");
		builder.append(".paddingLeft{padding-left: 10px;} ");
		builder.append("p, .content div, .footer div {margin-left: 10px; font-size: 20px; color: #404040;} ");
		builder.append("h3{color: #1F3950; margin-left: 10px;} ");
		
		builder.append("</style>");
		builder.append("</head>");
		builder.append("<body>");
		// --Header start
		builder.append("<div class='headerDiv bgBlue'>");
		builder.append("<span class='headerLabel paddingLeft colorOrange'>TeamPlaner</span>");
		builder.append("</div><br/>");
		//-- Header end
		
		//-- Content start
		builder.append("<div class='content'>");
		builder.append(content);
		builder.append("</div>");
		//-- Content end
		
		//-- Footer start
		builder.append("<div class='footer'>");
		builder.append("<p>"+MailTexts.SUPPORT_TEXT+"</p>");
		builder.append("</div>");
		// -- Footer end
		builder.append("</body>");
		builder.append("</html>");
		return builder.toString();
	}
}