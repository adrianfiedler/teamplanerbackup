package org.jboss.as.quickstarts.kitchensink.model;

import javax.annotation.Generated;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Member.class)
public abstract class Member_ extends org.jboss.as.quickstarts.kitchensink.model.AbstractBaseEntity_ {

	public static volatile SingularAttribute<Member, String> phoneNumber;
	public static volatile SingularAttribute<Member, String> name;
	public static volatile SingularAttribute<Member, String> email;

}

