package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TeamRolle.class)
public abstract class TeamRolle_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<TeamRolle, Boolean> inTeam;
	public static volatile SingularAttribute<TeamRolle, String> rolle;
	public static volatile SingularAttribute<TeamRolle, Team> team;
	public static volatile SingularAttribute<TeamRolle, User> user;

}

