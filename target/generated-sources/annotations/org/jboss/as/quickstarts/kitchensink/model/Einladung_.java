package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Einladung.class)
public abstract class Einladung_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<Einladung, String> vorname;
	public static volatile SingularAttribute<Einladung, String> name;
	public static volatile SingularAttribute<Einladung, User> inviter;
	public static volatile SingularAttribute<Einladung, Team> team;
	public static volatile SingularAttribute<Einladung, String> email;
	public static volatile SingularAttribute<Einladung, Integer> status;

}

