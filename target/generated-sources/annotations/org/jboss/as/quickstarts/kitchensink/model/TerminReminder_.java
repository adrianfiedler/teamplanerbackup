package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.ejb.TimerHandle;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TerminReminder.class)
public abstract class TerminReminder_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<TerminReminder, TimerHandle> timerHandle;
	public static volatile SingularAttribute<TerminReminder, String> terminId;

}

