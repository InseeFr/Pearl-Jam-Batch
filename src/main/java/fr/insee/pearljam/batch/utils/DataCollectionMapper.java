package fr.insee.pearljam.batch.utils;

import java.util.stream.Collectors;

import fr.insee.pearljam.batch.sampleprocessing.Campagne;

/**
 * Operation on XML Content 
 * - getXmlNodeFile 
 * - validateXMLSchema 
 * - xmlToObject 
 * - objectToXML 
 * - removeSurveyUnitNode
 * - updateSampleFileErrorList
 * 
 * @author Claudel Benjamin
 * 
 */
public class DataCollectionMapper {
	private DataCollectionMapper() {
		throw new IllegalStateException("Utility class");
	}
	
	public static fr.insee.queen.batch.sample.Campaign mapSampleProcessingToDataCollectionCampaign(Campagne c) {
		fr.insee.queen.batch.sample.Campaign campaign = new fr.insee.queen.batch.sample.Campaign();
		campaign.setId(c.getIdSource() + c.getMillesime() + c.getIdPeriode());
		campaign.setSurveyUnits(new fr.insee.queen.batch.sample.SurveyUnitsType());
		campaign.getSurveyUnits().getSurveyUnit().addAll(
				c.getQuestionnaires().getQuestionnaire().stream().map(su -> {
					fr.insee.queen.batch.sample.SurveyUnitType surveyUnitType = new fr.insee.queen.batch.sample.SurveyUnitType();
					surveyUnitType.setId(su.getIdInterrogation());
					surveyUnitType.setQuestionnaireModelId(su.getIdModele());
					surveyUnitType.setData(su.getInformationsPersonnalisees().getData());
					return surveyUnitType;
				}).collect(Collectors.toList())
		);
		return campaign;
	}
}
