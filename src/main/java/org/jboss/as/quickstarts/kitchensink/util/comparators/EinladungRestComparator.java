package org.jboss.as.quickstarts.kitchensink.util.comparators;

import java.util.Comparator;

import org.jboss.as.quickstarts.kitchensink.wrapper.EinladungREST;

/**
 * @author Adrian
 *
 */
public class EinladungRestComparator implements Comparator<EinladungREST> {
	
    @Override
    public int compare(EinladungREST o1, EinladungREST o2) {
    	if(o1 == null && o2 != null){
    		return 1;
    	} else if(o1 != null && o2 == null){
    		return -1;
    	} else if(o1 == null && o2 == null){
    		return 0;
    	} else{
    		if(o1.vorname.toLowerCase().equals(o2.vorname.toLowerCase())){
    			return o1.name.toLowerCase().compareTo(o2.name.toLowerCase());
    		} else{
    			return o1.vorname.toLowerCase().compareTo(o2.vorname.toLowerCase());
    		}
    	}
    }
}