package org.jboss.as.quickstarts.kitchensink.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Termin.class)
public abstract class Termin_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<Termin, Ort> ort;
	public static volatile SingularAttribute<Termin, Date> datum;
	public static volatile SingularAttribute<Termin, Boolean> maybeAllowed;
	public static volatile SingularAttribute<Termin, Integer> defaultZusageStatus;
	public static volatile SingularAttribute<Termin, String> absageKommentar;
	public static volatile ListAttribute<Termin, Zusage> zusagen;
	public static volatile SingularAttribute<Termin, String> name;
	public static volatile SingularAttribute<Termin, Serie> serie;
	public static volatile SingularAttribute<Termin, Team> team;
	public static volatile SingularAttribute<Termin, String> beschreibung;
	public static volatile SingularAttribute<Termin, Integer> status;

}

