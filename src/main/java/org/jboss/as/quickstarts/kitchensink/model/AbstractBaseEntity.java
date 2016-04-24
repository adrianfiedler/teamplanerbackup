package org.jboss.as.quickstarts.kitchensink.model;
import java.io.Serializable;
import java.util.Date;
import java.util.UUID;

import javax.persistence.Id;
import javax.persistence.MappedSuperclass;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

@MappedSuperclass
public abstract class AbstractBaseEntity implements Serializable {
	private static final long serialVersionUID = -5105475871840382676L;
	
	@Id
	private String id;
	
	/** The creation date. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date creationDate;
    
    /** The modification date. */
    @Temporal(TemporalType.TIMESTAMP)
    private Date modificationDate;

	public AbstractBaseEntity() {
		this.id = UUID.randomUUID().toString();
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (!(obj instanceof AbstractBaseEntity)) {
			return false;
		}
		AbstractBaseEntity other = (AbstractBaseEntity) obj;
		return getId().equals(other.getId());
	}
	
	public void setId(String id) {
		this.id = id;
	}
	
	public String getId() {
		return id;
	}
	
    @PrePersist
    public void markCreated() {
        Date date = new Date();
        this.creationDate = date;
        this.modificationDate = date;
    }

    @PreUpdate
    public void markChanged() {
        this.modificationDate = new Date();
    }

	public Date getCreationDate() {
		return creationDate;
	}

	public void setCreationDate(Date creationDate) {
		this.creationDate = creationDate;
	}

	public Date getModificationDate() {
		return modificationDate;
	}

	public void setModificationDate(Date modificationDate) {
		this.modificationDate = modificationDate;
	}

}