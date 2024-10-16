package fr.insee.pearljam.batch.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "status",
 "totalProcessed",
 "totalCreated",
 "created",
 "organizationUnitSynchronizationErrors"
})
@XmlRootElement(name = "OrganizationUnitsSynchronizationResult")
public class OrganizationUnitsSynchronizationResult {

 @XmlElement(name = "Status", required = true)
 protected String status;
 @XmlElement(name = "TotalProcessed", required = true)
 protected Long totalProcessed;
 @XmlElement(name = "TotalCreated", required = true)
 protected Long totalCreated;
 @XmlElement(name = "Created", required = true)
 protected CreatedOrganizationUnits created;
 @XmlElement(name = "OrganizationUnitSynchronizationErrors", required = false)
 protected OrganizationUnitSynchronizationErrors organizationUnitSynchronizationErrors;


public OrganizationUnitsSynchronizationResult() {
		super();
	}

public OrganizationUnitsSynchronizationResult(String status, Long totalProcessed, Long totalCreated,
		CreatedOrganizationUnits created, OrganizationUnitSynchronizationErrors organizationUnitSynchronizationErrors) {
	super();
	this.status = status;
	this.totalProcessed = totalProcessed;
	this.totalCreated = totalCreated;
	this.created = created;
	this.organizationUnitSynchronizationErrors = organizationUnitSynchronizationErrors;
}

public String getStatus() {
	return status;
}

public void setStatus(String status) {
	this.status = status;
}


public Long getTotalProcessed() {
	return totalProcessed;
}



public void setTotalProcessed(Long totalProcessed) {
	this.totalProcessed = totalProcessed;
}



public Long getTotalCreated() {
	return totalCreated;
}



public void setTotalCreated(Long totalCreated) {
	this.totalCreated = totalCreated;
}


public OrganizationUnitSynchronizationErrors getOrganizationUnitSynchronizationErrors() {
		return organizationUnitSynchronizationErrors;
	}

	public void setOrganizationUnitSynchronizationErrors(OrganizationUnitSynchronizationErrors organizationUnitSynchronizationErrors) {
		this.organizationUnitSynchronizationErrors = organizationUnitSynchronizationErrors;
	}

	public CreatedOrganizationUnits getCreated() {
		return created;
	}


}
