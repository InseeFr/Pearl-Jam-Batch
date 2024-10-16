package fr.insee.pearljam.batch.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "interviewerId",
 "surveyUnitId",
 "error",
 "cause",
})

@XmlRootElement(name = "InterviewerSynchronizationError")
public class InterviewerAffectationSynchronizationError {


@XmlElement(name = "InterviewerId", required = false)
 protected String interviewerId;
@XmlElement(name = "SurveyUnitId", required = false)
protected String surveyUnitId;
@XmlElement(name = "Error", required = false)
 protected String error;
 @XmlElement(name = "Cause", required = false)
 protected String cause;
 
 public InterviewerAffectationSynchronizationError() {
		super();
	}



 public InterviewerAffectationSynchronizationError(String interviewerId, String surveyUnitId, String error,
		String cause) {
	super();
	this.interviewerId = interviewerId;
	this.surveyUnitId = surveyUnitId;
	this.error = error;
	this.cause = cause;
}



public String getInterviewerId() {
	return interviewerId;
}

public void setInterviewerId(String interviewerId) {
	this.interviewerId = interviewerId;
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



public String getSurveyUnitId() {
	return surveyUnitId;
}



public void setSurveyUnitId(String surveyUnitId) {
	this.surveyUnitId = surveyUnitId;
}


}
