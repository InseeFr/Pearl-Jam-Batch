package fr.insee.pearljam.batch.template;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "interviewerId",
})
@XmlRootElement(name = "Created")
public class CreatedInterviewers {


@XmlElement(name = "InterviewerId", required = false)
 protected List<String> interviewerId;

 
 	public CreatedInterviewers() {
		super();
	}

	
	public CreatedInterviewers(List<String> interviewerId) {
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
