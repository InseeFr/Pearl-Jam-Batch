package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationTemplateType;

public interface CommunicationTemplateDao {

    CommunicationTemplateType findById(Long id);
}
