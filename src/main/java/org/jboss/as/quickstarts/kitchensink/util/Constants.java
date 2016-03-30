package org.jboss.as.quickstarts.kitchensink.util;

public class Constants {
	public static final String PROTOCOL = "https://";
	public static final String MAIN_URL = PROTOCOL + "masalis.de/teamPlanner";
	public static final String LOGIN_URL = MAIN_URL + "/login.php";
	public static final String SERVICE_URL = PROTOCOL + "teamplaner-datacollection.rhcloud.com/rest";
	public static final String ACTIVATION_URL = MAIN_URL + "/php/activate.php";
	public static final String TRAINER_ROLE = "Trainer";
	public static final String SPIELER_ROLE = "Spieler";

	public static final String TERMIN_STATUS_ABGESAGT = "0";
	public static final String TERMIN_STATUS_FINDET_STATT = "1";
	public static final String TERMIN_STATUS_GELOESCHT = "2";
	
	public static final int ZUGESAGT = 1;
	public static final int ABGESAGT = 0;
	public static final int VIELLEICHT = 2;
	
	public static final String TERMIN_URL = MAIN_URL + "/termin.php";
	
	
}
