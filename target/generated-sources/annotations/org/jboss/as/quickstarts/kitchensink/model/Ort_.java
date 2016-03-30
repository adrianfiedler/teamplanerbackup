package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Ort.class)
public abstract class Ort_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile ListAttribute<Ort, Termin> termine;
	public static volatile SingularAttribute<Ort, String> strasse;
	public static volatile SingularAttribute<Ort, String> latitude;
	public static volatile SingularAttribute<Ort, String> teamId;
	public static volatile SingularAttribute<Ort, Verein> verein;
	public static volatile SingularAttribute<Ort, String> stadt;
	public static volatile SingularAttribute<Ort, String> vereinId;
	public static volatile SingularAttribute<Ort, String> beschreibung;
	public static volatile SingularAttribute<Ort, String> nummer;
	public static volatile SingularAttribute<Ort, Boolean> vorlage;
	public static volatile SingularAttribute<Ort, String> plz;
	public static volatile SingularAttribute<Ort, String> longitude;

}

