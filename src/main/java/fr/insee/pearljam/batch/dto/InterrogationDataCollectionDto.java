package fr.insee.pearljam.batch.dto;

import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

public record InterrogationDataCollectionDto(
        String id,
        String surveyUnitId,
        String questionnaireId,
        ObjectNode comment,
        ObjectNode data) {

    public InterrogationDataCollectionDto(String id, String surveyUnitId, String questionnaireModelId, ObjectNode data) {
        this(id, surveyUnitId, questionnaireModelId, JsonNodeFactory.instance.objectNode(), data);
    }
}
