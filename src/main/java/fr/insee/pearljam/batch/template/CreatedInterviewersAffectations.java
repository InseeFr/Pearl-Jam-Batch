package fr.insee.pearljam.batch.template;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


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
