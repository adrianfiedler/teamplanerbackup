package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Zusage.class)
public abstract class Zusage_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<Zusage, Termin> termin;
	public static volatile SingularAttribute<Zusage, Boolean> autoSet;
	public static volatile SingularAttribute<Zusage, User> user;
	public static volatile SingularAttribute<Zusage, Integer> status;
	public static volatile SingularAttribute<Zusage, String> kommentar;

}

