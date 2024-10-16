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
