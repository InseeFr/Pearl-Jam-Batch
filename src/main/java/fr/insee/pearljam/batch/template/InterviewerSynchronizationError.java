package fr.insee.pearljam.batch.template;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "interviewerId",
 "error",
 "cause",
})

@XmlRootElement(name = "InterviewerSynchronizationError")
public class InterviewerSynchronizationError {


@XmlElement(name = "InterviewerId", required = false)
 protected String interviewerId;
@XmlElement(name = "Error", required = false)
 protected String error;
 @XmlElement(name = "Cause", required = false)
 protected String cause;
 
 public InterviewerSynchronizationError() {
		super();
	}

 public InterviewerSynchronizationError(String interviewerId, String error, String cause) {
		super();
		this.interviewerId = interviewerId;
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


}
