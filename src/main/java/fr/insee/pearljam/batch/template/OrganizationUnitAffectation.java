package fr.insee.pearljam.batch.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
