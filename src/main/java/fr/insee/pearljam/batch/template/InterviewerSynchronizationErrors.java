package fr.insee.pearljam.batch.template;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "interviewerSynchronizationError",
})
@XmlRootElement(name = "InterviewerSynchronizationErrors")
public class InterviewerSynchronizationErrors {


 @XmlElement(name = "InterviewerSynchronizationError", required = false)
 protected List<InterviewerSynchronizationError> interviewerSynchronizationError;



	public InterviewerSynchronizationErrors() {
			super();
		}
	
	public InterviewerSynchronizationErrors(List<InterviewerSynchronizationError> interviewerSynchronizationError) {
		super();
		this.interviewerSynchronizationError = interviewerSynchronizationError;
	}
	
	public List<InterviewerSynchronizationError> getInterviewerSynchronizationError() {
		return interviewerSynchronizationError;
	}
	
	
	public void setInterviewerSynchronizationError(List<InterviewerSynchronizationError> interviewerSynchronizationError) {
		this.interviewerSynchronizationError = interviewerSynchronizationError;
	}


}
