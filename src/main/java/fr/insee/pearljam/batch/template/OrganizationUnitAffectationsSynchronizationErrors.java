package fr.insee.pearljam.batch.template;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "organizationUnitAffectationSynchronizationError",
})
@XmlRootElement(name = "OrganizationUnitAffectationSynchronizationErrors")
public class OrganizationUnitAffectationsSynchronizationErrors {


 @XmlElement(name = "OrganizationUnitAffectationSynchronizationError", required = false)
 protected List<OrganizationUnitAffectationSynchronizationError> organizationUnitAffectationSynchronizationError;



	public OrganizationUnitAffectationsSynchronizationErrors() {
			super();
		}
	
	public OrganizationUnitAffectationsSynchronizationErrors(List<OrganizationUnitAffectationSynchronizationError> organizationUnitAffectationSynchronizationError) {
		super();
		this.organizationUnitAffectationSynchronizationError = organizationUnitAffectationSynchronizationError;
	}
	
	public List<OrganizationUnitAffectationSynchronizationError> getOrganizationUnitAffectationSynchronizationError() {
		return organizationUnitAffectationSynchronizationError;
	}
	
	
	public void setOrganizationUnitAffectationSynchronizationError(List<OrganizationUnitAffectationSynchronizationError> organizationUnitAffectationSynchronizationError) {
		this.organizationUnitAffectationSynchronizationError = organizationUnitAffectationSynchronizationError;
	}


}
