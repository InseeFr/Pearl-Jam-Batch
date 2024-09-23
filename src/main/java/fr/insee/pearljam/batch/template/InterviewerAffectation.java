package fr.insee.pearljam.batch.template;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "interviewerId",
 "surveyUnitId",
})

@XmlRootElement(name = "InterviewerSynchronizationError")
public class InterviewerAffectation {


@XmlElement(name = "InterviewerId", required = false)
 protected String interviewerId;
@XmlElement(name = "SurveyUnitId", required = false)
 protected String surveyUnitId;

 
 public InterviewerAffectation() {
		super();
	}

 public InterviewerAffectation(String interviewerId, String surveyUnitId) {
		super();
		this.interviewerId = interviewerId;
		this.surveyUnitId = surveyUnitId;
	}

 public String getInterviewerId() {
	return interviewerId;
}

public void setInterviewerId(String interviewerId) {
	this.interviewerId = interviewerId;
}

public String getSurveyUnitId() {
	return surveyUnitId;
}

public void setSurveyUnitId(String surveyUnitId) {
	this.surveyUnitId = surveyUnitId;
}



}
