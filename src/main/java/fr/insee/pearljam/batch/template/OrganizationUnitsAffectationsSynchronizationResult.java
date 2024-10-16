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
 "totalReaffected",
 "created",
 "reaffected",
 "organizationUnitAffectationsSynchronizationErrors"
})
@XmlRootElement(name = "OrganizationUnitsAffectationsSynchronizationResult")
public class OrganizationUnitsAffectationsSynchronizationResult {

 @XmlElement(name = "Status", required = true)
 protected String status;
 @XmlElement(name = "TotalProcessed", required = true)
 protected Long totalProcessed;
 @XmlElement(name = "TotalCreated", required = true)
 protected Long totalCreated;
 @XmlElement(name = "TotalReaffected", required = true)
 protected Long totalReaffected;
 @XmlElement(name = "Created", required = true)
 protected CreatedOrganizationUnitsAffectations created;
 @XmlElement(name = "Reaffected", required = true)
 protected OrganizationUnitsReaffectations reaffected;
 @XmlElement(name = "OrganizationUnitAffectationsSynchronizationErrors", required = false)
 protected OrganizationUnitAffectationsSynchronizationErrors organizationUnitAffectationsSynchronizationErrors;


public OrganizationUnitsAffectationsSynchronizationResult() {
		super();
	}



public OrganizationUnitsAffectationsSynchronizationResult(String status, Long totalProcessed, Long totalCreated,
		Long totalReaffected, CreatedOrganizationUnitsAffectations created, OrganizationUnitsReaffectations reaffectations,
		OrganizationUnitAffectationsSynchronizationErrors organizationUnitAffectationsSynchronizationErrors) {
	super();
	this.status = status;
	this.totalProcessed = totalProcessed;
	this.totalCreated = totalCreated;
	this.totalReaffected = totalReaffected;
	this.created = created;
	this.reaffected = reaffectations;
	this.organizationUnitAffectationsSynchronizationErrors = organizationUnitAffectationsSynchronizationErrors;
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


public Long getTotalReaffected() {
	return totalReaffected;
}


public void setTotalReaffected(Long totalReaffected) {
	this.totalReaffected = totalReaffected;
}


public CreatedOrganizationUnitsAffectations getCreated() {
	return created;
}


public void setCreated(CreatedOrganizationUnitsAffectations created) {
	this.created = created;
}



public OrganizationUnitsReaffectations getReaffected() {
	return reaffected;
}



public void setReaffected(OrganizationUnitsReaffectations reaffected) {
	this.reaffected = reaffected;
}



public OrganizationUnitAffectationsSynchronizationErrors getOrganizationUnitAffectationsSynchronizationErrors() {
	return organizationUnitAffectationsSynchronizationErrors;
}


public void setOrganizationUnitAffectationsSynchronizationErrors(
		OrganizationUnitAffectationsSynchronizationErrors organizationUnitAffectationsSynchronizationErrors) {
	this.organizationUnitAffectationsSynchronizationErrors = organizationUnitAffectationsSynchronizationErrors;
}



	
	

}
