package fr.insee.pearljam.batch.template;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


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
