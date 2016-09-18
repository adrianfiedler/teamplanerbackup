package org.jboss.as.quickstarts.kitchensink.util.comparators;

import java.util.Comparator;

import org.jboss.as.quickstarts.kitchensink.wrapper.TeamMitgliedREST;
import org.jboss.as.quickstarts.kitchensink.wrapper.UserREST;

/**
 *  Vergleicht und sortiert Zusagen nach Vorname und bei gleichem Vornamen nach Nachname
 * @author Adrian
 *
 */
public class TeamMitgliedRestComparator implements Comparator<TeamMitgliedREST> {
	
    @Override
    public int compare(TeamMitgliedREST o1, TeamMitgliedREST o2) {
    	if(o1 == null && o2 != null){
    		return 1;
    	} else if(o1 != null && o2 == null){
    		return -1;
    	} else if(o1 == null && o2 == null){
    		return 0;
    	} else{
    		UserREST user1 = o1.user;
    		UserREST user2 = o2.user;
        	if(user1 == null && user2 != null){
        		return 1;
        	} else if(user1 != null && user2 == null){
        		return -1;
        	} else if(user1 == null && user2 == null){
        		return 0;
        	} else{
        		String vorname1 = user1.vorname;
        		String vorname2 = user2.vorname;
        		if(vorname1 == null && vorname2 != null){
            		return 1;
            	} else if(vorname1 != null && vorname2 == null){
            		return -1;
            	} else if(vorname1 == null && vorname2 == null){
            		return 0;
            	} else{
            		int compVorname = vorname1.compareTo(vorname2);
            		if(compVorname != 0){
            			return compVorname;
            		} else{
            			String nachname1 = user1.name;
            			String nachname2 = user2.name;
                		if(nachname1 == null && nachname2 != null){
                    		return 1;
                    	} else if(nachname1 != null && nachname2 == null){
                    		return -1;
                    	} else if(nachname1 == null && nachname2 == null){
                    		return 0;
                    	} else{
                    		return nachname1.compareTo(nachname2);
                    	}	
            		}
            	}
        	}
    	}
    }
}