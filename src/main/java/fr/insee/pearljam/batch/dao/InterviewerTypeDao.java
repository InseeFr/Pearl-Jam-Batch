package fr.insee.pearljam.batch.dao;

import java.util.List;
import java.util.Optional;

import fr.insee.pearljam.batch.context.InterviewerType;
import fr.insee.pearljam.batch.dto.InterviewerDto;

/**
 * Interface for the Interviewer entity
 * @author scorcaud
 *
 */
public interface InterviewerTypeDao {
	/**
     * Get an Interviewer by id in database
     * @param id
     * @return boolean
     */
	boolean existInterviewer(String id);
	
	/**
     * Create an Interviewer in database
     * @param interviewer
     */
	void createInterviewer(InterviewerType interviewer);
	/**
     * Update an Interviewer in database
     * @param id
     */
	void updateOrganizationalUnitByInterviewerId(String interviewerId, String id);
	/**
     * check if at least one interviewer is already associated in database
     * @param List of interviewerId
     * @param organizationUnitId ouId
     * @return boolean
     */
	boolean interviewerAlreadyAssociated(List<String> interviewerId, String organizationUnitId);
	
	/**
     * check if interviewer is already associated to a specific Organization Unit in database
     * @param interviewerId
     * @param organizationalUnitId
     * @return boolean
     */
	boolean interviewerAlreadyAssociatedToOrganizationUnitId(String interviewerId, String organizationalUnitId);
	List<String> findAllInterviewrsWithoutOrganizationUnit();


	void createInterviewerFromDto(InterviewerDto interviewer);

	void updateInterviewerFromDto(InterviewerDto interviewer);

	boolean isDifferentFromDto(InterviewerDto interviewer);

	InterviewerType findById(String interviewerId);
}
