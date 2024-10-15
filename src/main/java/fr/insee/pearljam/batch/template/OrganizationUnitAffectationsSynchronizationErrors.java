package fr.insee.pearljam.batch.template;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
