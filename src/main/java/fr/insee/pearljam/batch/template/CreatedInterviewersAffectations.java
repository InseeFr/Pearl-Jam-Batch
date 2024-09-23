package fr.insee.pearljam.batch.template;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "interviewerAffectation",
})
@XmlRootElement(name = "CreatedInterviewersAffectations")
public class CreatedInterviewersAffectations {


@XmlElement(name = "InterviewerAffectation", required = false)
 protected List<InterviewerAffectation> interviewerAffectation;

 
 	public CreatedInterviewersAffectations() {
		super();
	}


	public CreatedInterviewersAffectations(List<InterviewerAffectation> interviewerAffectation) {
		super();
		this.interviewerAffectation = interviewerAffectation;
	}


	public List<InterviewerAffectation> getInterviewerId() {
		return interviewerAffectation;
	}


	public void setInterviewerId(List<InterviewerAffectation> interviewerAffectation) {
		this.interviewerAffectation = interviewerAffectation;
	}

	


}
