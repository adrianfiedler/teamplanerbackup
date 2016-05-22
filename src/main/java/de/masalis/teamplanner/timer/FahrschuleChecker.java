package de.masalis.teamplanner.timer;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
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
public class FahrschuleChecker {
	@Resource
	private SessionContext context;

	@Inject
	SendMail sendMail;

	@Inject
	private Logger log;

	private final String USER_AGENT = "Mozilla/5.0";

	@Schedule(hour = "17", minute = "0", second = "0")
	public void checkFahrschule() {
		try {
			sendGet();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	// HTTP GET request
	private void sendGet() throws Exception {
		ArrayList<String> toList = new ArrayList<String>();
		toList.add("adrian_fiedler@msn.com");
		toList.add("julischka@onlinehome.de");
		
		String url = "https://masalis.de/services/scanpageFahrschule.php";

		URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		// add request header
		con.setRequestProperty("User-Agent", USER_AGENT);

		int responseCode = con.getResponseCode();
		
		if(responseCode == 200){
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
			
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
			
			sendMail.sendEmail(toList, String.format("Fahrschule Check: Listeneinträge: %s", response.toString()), String.format("Listeneinträge: %s", response.toString()), "fahrschule-check");
		} else{
			sendMail.sendEmail(toList, "Fahrschule Check Fehler", String.format("Fehler im Response Check: %s", responseCode), "fahrschule-check");
		}


	}
}