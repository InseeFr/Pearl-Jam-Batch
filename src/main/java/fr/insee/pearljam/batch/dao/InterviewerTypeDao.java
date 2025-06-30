package fr.insee.pearljam.batch.dao;

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
     * @param id interviewerId
     * @return boolean
     */
	boolean existInterviewer(String id);
	
	/**
     * Create an Interviewer in database
     * @param interviewer InterviewerData
     */
	void createInterviewer(InterviewerType interviewer);


	void createInterviewerFromDto(InterviewerDto interviewer);

	void updateInterviewerFromDto(InterviewerDto interviewer);

	boolean isDifferentFromDto(InterviewerDto interviewer);

	InterviewerType findById(String interviewerId);
}
