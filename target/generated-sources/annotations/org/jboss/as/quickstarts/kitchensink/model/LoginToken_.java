package org.jboss.as.quickstarts.kitchensink.model;

import java.util.Date;
import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(LoginToken.class)
public abstract class LoginToken_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<LoginToken, String> userId;
	public static volatile SingularAttribute<LoginToken, String> token;
	public static volatile SingularAttribute<LoginToken, Date> timeOut;

}

