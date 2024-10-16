package fr.insee.pearljam.batch.template;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

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
