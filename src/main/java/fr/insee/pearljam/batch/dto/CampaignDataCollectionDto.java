package fr.insee.pearljam.batch.dto;

import java.util.List;

public record CampaignDataCollectionDto(
        String id,
        List<String> questionnaireIds) {
}
