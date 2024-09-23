package fr.insee.pearljam.batch.template;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "interviewerId",
})
@XmlRootElement(name = "Updated")
public class UpdatedInterviewers {


@XmlElement(name = "InterviewerId", required = false)
 protected List<String> interviewerId;

 
 	public UpdatedInterviewers() {
		super();
	}

	
	public UpdatedInterviewers(List<String> interviewerId) {
		super();
		this.interviewerId = interviewerId;
	}
	
	
	public List<String> getInterviewerId() {
		return interviewerId;
	}
	
	
	public void setInterviewerId(List<String> interviewerId) {
		this.interviewerId = interviewerId;
	}


}
