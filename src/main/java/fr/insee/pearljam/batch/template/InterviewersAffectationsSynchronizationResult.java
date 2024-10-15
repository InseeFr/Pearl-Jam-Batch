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
 "interviewerAffectationsSynchronizationErrors"
})
@XmlRootElement(name = "InterviewersAffectationsSynchronizationResult")
public class InterviewersAffectationsSynchronizationResult {

 @XmlElement(name = "Status", required = true)
 protected String status;
 @XmlElement(name = "TotalProcessed", required = true)
 protected Long totalProcessed;
 @XmlElement(name = "TotalCreated", required = true)
 protected Long totalCreated;
 @XmlElement(name = "TotalReaffected", required = true)
 protected Long totalReaffected;
 @XmlElement(name = "Created", required = true)
 protected CreatedInterviewersAffectations created;
 @XmlElement(name = "Reaffected", required = true)
 protected InterviewersReaffectations reaffected;
 @XmlElement(name = "InterviewerSynchronizationErrors", required = false)
 protected InterviewerAffectationsSynchronizationErrors interviewerAffectationsSynchronizationErrors;


public InterviewersAffectationsSynchronizationResult() {
		super();
	}



public InterviewersAffectationsSynchronizationResult(String status, Long totalProcessed, Long totalCreated,
		Long totalReaffected, CreatedInterviewersAffectations created, InterviewersReaffectations reaffectations,
		InterviewerAffectationsSynchronizationErrors interviewerAffectationsSynchronizationErrors) {
	super();
	this.status = status;
	this.totalProcessed = totalProcessed;
	this.totalCreated = totalCreated;
	this.totalReaffected = totalReaffected;
	this.created = created;
	this.reaffected = reaffectations;
	this.interviewerAffectationsSynchronizationErrors = interviewerAffectationsSynchronizationErrors;
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


public CreatedInterviewersAffectations getCreated() {
	return created;
}


public void setCreated(CreatedInterviewersAffectations created) {
	this.created = created;
}





public InterviewersReaffectations getReaffected() {
	return reaffected;
}



public void setReaffected(InterviewersReaffectations reaffected) {
	this.reaffected = reaffected;
}



public InterviewerAffectationsSynchronizationErrors getInterviewerAffectationsSynchronizationErrors() {
	return interviewerAffectationsSynchronizationErrors;
}


public void setInterviewerAffectationsSynchronizationErrors(
		InterviewerAffectationsSynchronizationErrors interviewerAffectationsSynchronizationErrors) {
	this.interviewerAffectationsSynchronizationErrors = interviewerAffectationsSynchronizationErrors;
}



	
	

}
