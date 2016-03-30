package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Verein.class)
public abstract class Verein_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile ListAttribute<Verein, Ort> orte;
	public static volatile SingularAttribute<Verein, String> name;
	public static volatile ListAttribute<Verein, Team> vereinsTeams;
	public static volatile ListAttribute<Verein, User> user;

}

