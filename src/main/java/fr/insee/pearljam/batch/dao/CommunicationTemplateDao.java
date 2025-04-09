package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationTemplateType;
import java.util.List;

public interface CommunicationTemplateDao {

    List<CommunicationTemplateType> findByCampaign(String campaignId);
    void deleteByCampaignId(String campaignId);
}
