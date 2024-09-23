package fr.insee.pearljam.batch.template;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;


@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "organizationUnitAffectation",
})
@XmlRootElement(name = "OrganizationUnitsReaffectations")
public class OrganizationUnitsReaffectations {


@XmlElement(name = "OrganizationUnitAffectation", required = false)
 protected List<OrganizationUnitAffectation> organizationUnitAffectation;

 
 	public OrganizationUnitsReaffectations() {
		super();
	}


	public OrganizationUnitsReaffectations(List<OrganizationUnitAffectation> organizationUnitAffectation) {
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
