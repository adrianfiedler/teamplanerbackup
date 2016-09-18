package org.jboss.as.quickstarts.kitchensink.util.comparators;

import java.util.Comparator;

import org.jboss.as.quickstarts.kitchensink.model.User;
import org.jboss.as.quickstarts.kitchensink.model.Zusage;

/**
 *  Vergleicht und sortiert Zusagen nach Vorname und bei gleichem Vornamen nach Nachname
 * @author Adrian
 *
 */
public class ZusageComparator implements Comparator<Zusage> {
	
    @Override
    public int compare(Zusage o1, Zusage o2) {
    	if(o1 == null && o2 != null){
    		return 1;
    	} else if(o1 != null && o2 == null){
    		return -1;
    	} else if(o1 == null && o2 == null){
    		return 0;
    	} else{
    		User u1 = o1.getUser();
    		User u2 = o2.getUser();
        	if(u1 == null && u2 != null){
        		return 1;
        	} else if(u1 != null && u2 == null){
        		return -1;
        	} else if(u1 == null && u2 == null){
        		return 0;
        	} else{
        		String vorname1 = u1.getVorname();
        		String vorname2 = u2.getVorname();
        		
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
            			String nachname1 = u1.getName();
            			String nachname2 = u2.getName();
            			
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