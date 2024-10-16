package fr.insee.pearljam.batch.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "organizationUnitId",
 "error",
 "cause",
})

@XmlRootElement(name = "OrganizationUnitSynchronizationError")
public class OrganizationUnitSynchronizationError {


@XmlElement(name = "OrganizationUnitId", required = false)
 protected String organizationUnitId;
@XmlElement(name = "Error", required = false)
 protected String error;
 @XmlElement(name = "Cause", required = false)
 protected String cause;
 
 public OrganizationUnitSynchronizationError() {
		super();
	}

 public OrganizationUnitSynchronizationError(String organizationUnitId, String error, String cause) {
		super();
		this.organizationUnitId = organizationUnitId;
		this.error = error;
		this.cause = cause;
	}

 public String getOrganizationUnitId() {
	return organizationUnitId;
}

public void setOrganizationUnitId(String organizationUnitId) {
	this.organizationUnitId = organizationUnitId;
}


public String getError() {
	return error;
}

public void setError(String error) {
	this.error = error;
}

public String getCause() {
	return cause;
}

public void setCause(String cause) {
	this.cause = cause;
}


}
