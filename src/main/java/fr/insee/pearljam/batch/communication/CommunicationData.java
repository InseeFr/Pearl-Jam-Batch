package fr.insee.pearljam.batch.communication;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class CommunicationData {

    private String communicationTemplateId;

    private String bddL1;
    private String bddL2;
    private String bddL3;
    private String bddL4;
    private String bddL5;
    private String bddL6;
    private String bddL7;

    /// vu dans xsd_ds_ouvertureFAFCOLEM.xsd
    private String editionDate; //Ue_DateEdition
    private String mailAssistance; //Ue_MailAssistance
    private String telAssistance; //Ue_TelAssistance

    private String interviewerTitle; //Ue_CiviliteEnqueteur
    private String interviewerLastName; //Ue_NomEnqueteur
    private String interviewerFirstName; //Ue_PrenomEnqueteur
    private String interviewerEmail; //Ue_MailEnqueteur
    private String interviewerTel; //Ue_TelEnqueteur

    private String reminderReason; // Ue_TypeRelance

    private String recipientPostCode; //CodePostalDestinataire
    private String communicationRequestId; //BddIdentifiantUniteEnquetee /!\ max 14 chars

    private List<Metadata> templateMetadata;

}
