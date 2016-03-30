package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(UserSettings.class)
public abstract class UserSettings_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<UserSettings, Boolean> mittwochsAbsagen;
	public static volatile SingularAttribute<UserSettings, Boolean> montagsAbsagen;
	public static volatile SingularAttribute<UserSettings, Boolean> dienstagsAbsagen;
	public static volatile SingularAttribute<UserSettings, Boolean> donnerstagsAbsagen;
	public static volatile SingularAttribute<UserSettings, Boolean> samstagsAbsagen;
	public static volatile SingularAttribute<UserSettings, Boolean> freitagsAbsagen;
	public static volatile SingularAttribute<UserSettings, Boolean> sonntagsAbsagen;

}

