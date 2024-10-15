package fr.insee.pearljam.batch.template;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "interviewerAffectationSynchronizationError",
})
@XmlRootElement(name = "InterviewerAffectationSynchronizationErrors")
public class InterviewerAffectationsSynchronizationErrors {


 @XmlElement(name = "InterviewerAffectationSynchronizationError", required = false)
 protected List<InterviewerAffectationSynchronizationError> interviewerAffectationSynchronizationError;



	public InterviewerAffectationsSynchronizationErrors() {
			super();
		}
	
	public InterviewerAffectationsSynchronizationErrors(List<InterviewerAffectationSynchronizationError> interviewerAffectationSynchronizationError) {
		super();
		this.interviewerAffectationSynchronizationError = interviewerAffectationSynchronizationError;
	}
	
	public List<InterviewerAffectationSynchronizationError> getInterviewerAffectationSynchronizationError() {
		return interviewerAffectationSynchronizationError;
	}
	
	
	public void setInterviewerAffectationSynchronizationError(List<InterviewerAffectationSynchronizationError> interviewerAffectationSynchronizationError) {
		this.interviewerAffectationSynchronizationError = interviewerAffectationSynchronizationError;
	}


}
