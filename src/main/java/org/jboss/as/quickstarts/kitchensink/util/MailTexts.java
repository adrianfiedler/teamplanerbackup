package org.jboss.as.quickstarts.kitchensink.util;

public class MailTexts {
	public static final String SUPPORT_TEXT = "Hast du technische Probleme oder einen Fehler gefunden? Dann schreibe eine Mail an <a href=\"mailto:support@masalis.de\">support@masalis.de</a>";
	public static final String UNREGISTER_WEEKLY_TEXT = "Willst du diese Email nicht mehr erhalten, kannst du sie unter Profil verwalten -> Mailverwaltung abstellen. <a href=\""+Constants.LOGIN_URL+"\">"+Constants.LOGIN_URL+"</a>";
	public static final String TEAM_QUESTION = "Hast du inhaltliche oder organisatorische Fragen? Dann wende dich an die oben aufgeführten Trainer";
	
	public static final String UNREGISTER_TRAINER_NOTIF_TEXT = "Du erhältst diese Mail, weil du Termin Benachrichtigungen in deinem Profil aktiviert hast. "
			+ "Du kannst dies jederzeit deaktivieren, indem du dich unter <a href=\""+Constants.LOGIN_URL+"\">"+Constants.LOGIN_URL+"</a> einloggst "
			+ "und in deinen Einstellungen den Haken für Termin Benachrichtigungen rausnimmst.";
	
	public static final String TUTORIAL_TEXT = "<ul>"
			+ "<li> Um deine Termine einzusehen melde dich bei <a href=\""+Constants.LOGIN_URL+"\">"+Constants.LOGIN_URL+"</a> an.</li>"
			+ "<li> Im Kalender siehst du alle deine kommenden Termine.</li>"
			+ "<li> Mit Klick auf einen Termin öffnet sich die Terminansicht. "
			+ "<br />Hier kannst du zu einem Termin ab- und zusagen. "
			+ "<li> Falls dein Trainer den Status erlaubt hat, kannst du auch den Status \"Vielleicht\" setzen.</li>"
			+ "<li> Kannst du an bestimmten Tagen nicht, kannst du unter Profil verwalten->Terminverwaltung Absagetage einstellen. Dein Status wird bei neuen Terminen an diesen Wochentagen automatisch auf \"Abgesagt\" gesetzt.</li>"
			+ "</ul>";
	
}
