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
 "totalUpdated",
 "created",
 "updated",
 "interviewerSynchronizationErrors"
})
@XmlRootElement(name = "InterviewersSynchronizationResult")
public class InterviewersSynchronizationResult {

 @XmlElement(name = "Status", required = true)
 protected String status;
 @XmlElement(name = "TotalProcessed", required = true)
 protected Long totalProcessed;
 @XmlElement(name = "TotalCreated", required = true)
 protected Long totalCreated;
 @XmlElement(name = "TotalUpdated", required = true)
 protected Long totalUpdated;
 @XmlElement(name = "Created", required = true)
 protected CreatedInterviewers created;
 @XmlElement(name = "Updated", required = true)
 protected UpdatedInterviewers updated;
 @XmlElement(name = "InterviewerSynchronizationErrors", required = false)
 protected InterviewerSynchronizationErrors interviewerSynchronizationErrors;


public InterviewersSynchronizationResult() {
		super();

	}

public InterviewersSynchronizationResult(String status, Long totalProcessed, Long totalCreated, Long totalUpdated,
		CreatedInterviewers created, UpdatedInterviewers updated, InterviewerSynchronizationErrors interviewerSynchronizationErrors) {
	super();
	this.status = status;
	this.totalProcessed = totalProcessed;
	this.totalCreated = totalCreated;
	this.totalUpdated = totalUpdated;
	this.created = created;
	this.updated = updated;
	this.interviewerSynchronizationErrors = interviewerSynchronizationErrors;
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



public Long getTotalUpdated() {
	return totalUpdated;
}



public void setTotalUpdated(Long totalUpdated) {
	this.totalUpdated = totalUpdated;
}



public InterviewerSynchronizationErrors getInterviewerSynchronizationErrors() {
		return interviewerSynchronizationErrors;
	}

	public void setInterviewerSynchronizationErrors(InterviewerSynchronizationErrors interviewerSynchronizationErrors) {
		this.interviewerSynchronizationErrors = interviewerSynchronizationErrors;
	}

	public CreatedInterviewers getCreated() {
		return created;
	}

	public void setCreated(CreatedInterviewers created) {
		this.created = created;
	}

	public UpdatedInterviewers getUpdated() {
		return updated;
	}

	public void setUpdated(UpdatedInterviewers updated) {
		this.updated = updated;
	}
	
	
	
	

}
