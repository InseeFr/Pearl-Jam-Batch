package fr.insee.pearljam.batch.template;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "organizationUnitAffectation",
})
@XmlRootElement(name = "CreatedOrganizationUnitsAffectations")
public class CreatedOrganizationUnitsAffectations {


@XmlElement(name = "OrganizationUnitAffectation", required = false)
 protected List<OrganizationUnitAffectation> organizationUnitAffectation;

 
 	public CreatedOrganizationUnitsAffectations() {
		super();
	}


	public CreatedOrganizationUnitsAffectations(List<OrganizationUnitAffectation> organizationUnitAffectation) {
		super();
		this.organizationUnitAffectation = organizationUnitAffectation;
	}


	public List<OrganizationUnitAffectation> getOrganizationUnitId() {
		return organizationUnitAffectation;
	}


	public void setOrganizationUnitId(List<OrganizationUnitAffectation> organizationUnitAffectation) {
		this.organizationUnitAffectation = organizationUnitAffectation;
	}

	


}
