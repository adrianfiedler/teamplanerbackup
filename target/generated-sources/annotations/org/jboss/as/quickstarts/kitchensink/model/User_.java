package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(User.class)
public abstract class User_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<User, String> passwort;
	public static volatile SingularAttribute<User, String> facebookUserId;
	public static volatile SingularAttribute<User, String> aktivierToken;
	public static volatile ListAttribute<User, Zusage> zusagen;
	public static volatile SingularAttribute<User, String> vorname;
	public static volatile SingularAttribute<User, Verein> verein;
	public static volatile SingularAttribute<User, String> facebook;
	public static volatile SingularAttribute<User, Boolean> admin;
	public static volatile SingularAttribute<User, Boolean> terminReminderMail;
	public static volatile SingularAttribute<User, Boolean> aktiviert;
	public static volatile SingularAttribute<User, Boolean> weeklyStatusMail;
	public static volatile SingularAttribute<User, String> googlePlus;
	public static volatile SingularAttribute<User, String> fotoUrl;
	public static volatile ListAttribute<User, TeamRolle> rollen;
	public static volatile SingularAttribute<User, String> facebookToken;
	public static volatile SingularAttribute<User, UserSettings> userSettings;
	public static volatile SingularAttribute<User, String> name;
	public static volatile SingularAttribute<User, String> email;

}

