package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Team.class)
public abstract class Team_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile ListAttribute<Team, Termin> termine;
	public static volatile ListAttribute<Team, TeamRolle> rollen;
	public static volatile SingularAttribute<Team, Verein> verein;
	public static volatile SingularAttribute<Team, String> name;

}

