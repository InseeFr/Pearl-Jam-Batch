package fr.insee.pearljam.batch.service;

import java.util.List;

import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.dto.InterviewerAffectationsDto;
import fr.insee.pearljam.batch.dto.InterviewerDto;
import fr.insee.pearljam.batch.dto.OrganizationUnitAffectationsDto;
import fr.insee.pearljam.batch.dto.OrganizationUnitDto;
import fr.insee.pearljam.batch.exception.SynchronizationException;

// Class to call Opale endpoints

@Service
public interface ContextReferentialService {

	List<InterviewerDto> getInterviewersFromOpale() throws SynchronizationException;
	List<OrganizationUnitDto> getOrganizationUnitsFromOpale() throws SynchronizationException;		
	List<InterviewerAffectationsDto> getInterviewersAffectationsFromOpale() throws SynchronizationException;
	List<OrganizationUnitAffectationsDto> getOrganizationUnitsAffectationsFromOpale() throws SynchronizationException;
	void contextReferentialServiceIsAvailable() throws SynchronizationException;
}
