package fr.insee.pearljam.batch.template;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "", propOrder = {
 "organizationUnitSynchronizationError",
})
@XmlRootElement(name = "OrganizationUnitSynchronizationErrors")
public class OrganizationUnitSynchronizationErrors {


 @XmlElement(name = "OrganizationUnitSynchronizationError", required = false)
 protected List<OrganizationUnitSynchronizationError> organizationUnitSynchronizationError;



	public OrganizationUnitSynchronizationErrors() {
			super();
		}
	
	public OrganizationUnitSynchronizationErrors(List<OrganizationUnitSynchronizationError> organizationUnitSynchronizationError) {
		super();
		this.organizationUnitSynchronizationError = organizationUnitSynchronizationError;
	}
	
	public List<OrganizationUnitSynchronizationError> getOrganizationUnitSynchronizationError() {
		return organizationUnitSynchronizationError;
	}
	
	
	public void setOrganizationUnitSynchronizationError(List<OrganizationUnitSynchronizationError> organizationUnitSynchronizationError) {
		this.organizationUnitSynchronizationError = organizationUnitSynchronizationError;
	}


}
