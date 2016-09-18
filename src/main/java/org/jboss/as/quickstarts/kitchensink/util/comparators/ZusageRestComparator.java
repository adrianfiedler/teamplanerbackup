package org.jboss.as.quickstarts.kitchensink.util.comparators;

import java.util.Comparator;

import org.jboss.as.quickstarts.kitchensink.interfaces.DisplayNameInterface;

/**
 *  Vergleicht und sortiert Zusagen nach DisplayName
 * @author Adrian
 *
 */
public class ZusageRestComparator implements Comparator<DisplayNameInterface> {
	
    @Override
    public int compare(DisplayNameInterface o1, DisplayNameInterface o2) {
    	if(o1 == null && o2 != null){
    		return 1;
    	} else if(o1 != null && o2 == null){
    		return -1;
    	} else if(o1 == null && o2 == null){
    		return 0;
    	} else{
    		return o1.getDisplayName().compareTo(o2.getDisplayName());
    	}
    }
}