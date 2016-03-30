package org.jboss.as.quickstarts.kitchensink.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(TerminVorlage.class)
public abstract class TerminVorlage_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<TerminVorlage, Ort> ort;
	public static volatile SingularAttribute<TerminVorlage, String> name;
	public static volatile SingularAttribute<TerminVorlage, Date> time;
	public static volatile SingularAttribute<TerminVorlage, Team> team;
	public static volatile SingularAttribute<TerminVorlage, String> beschreibung;

}

