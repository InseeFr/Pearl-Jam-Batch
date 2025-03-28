package fr.insee.pearljam.batch.dao;

import fr.insee.pearljam.batch.campaign.CommunicationTemplateType;
import java.util.List;
import java.util.Set;

public interface CommunicationTemplateDao {

    List<CommunicationTemplateType> findByMeshuggahIds(Set<String> meshuggahIds);
    List<CommunicationTemplateType> findByCampaign(String campaignId);

}
