package fr.insee.pearljam.batch.template;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "organizationUnitId",
 "surveyUnitId",
})

@XmlRootElement(name = "OrganizationUnitSynchronizationError")
public class OrganizationUnitAffectation {


@XmlElement(name = "OrganizationUnitId", required = false)
 protected String organizationUnitId;
@XmlElement(name = "SurveyUnitId", required = false)
 protected String surveyUnitId;

 
 public OrganizationUnitAffectation() {
		super();
	}

 public OrganizationUnitAffectation(String organizationUnitId, String surveyUnitId) {
		super();
		this.organizationUnitId = organizationUnitId;
		this.surveyUnitId = surveyUnitId;
	}

 public String getOrganizationUnitId() {
	return organizationUnitId;
}

public void setOrganizationUnitId(String organizationUnitId) {
	this.organizationUnitId = organizationUnitId;
}

public String getSurveyUnitId() {
	return surveyUnitId;
}

public void setSurveyUnitId(String surveyUnitId) {
	this.surveyUnitId = surveyUnitId;
}



}
