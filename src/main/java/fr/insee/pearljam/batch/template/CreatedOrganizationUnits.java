package fr.insee.pearljam.batch.template;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "organizationUnitId",
})
@XmlRootElement(name = "Created")
public class CreatedOrganizationUnits {


@XmlElement(name = "OrganizationUnitId", required = false)
 protected List<String> organizationUnitId;

 
 	public CreatedOrganizationUnits() {
		super();
	}

	
	public CreatedOrganizationUnits(List<String> organizationUnitId) {
		super();
		this.organizationUnitId = organizationUnitId;
	}
	
	
	public List<String> getOrganizationUnitId() {
		return organizationUnitId;
	}
	
	
	public void setOrganizationUnitId(List<String> organizationUnitId) {
		this.organizationUnitId = organizationUnitId;
	}


}
