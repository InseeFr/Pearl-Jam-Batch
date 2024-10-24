package fr.insee.pearljam.batch.communication;

import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommunicationTemplate {

    private String communicationId;
    private String communicationLabel;
    private String collectionBatchLabel;
    private String communicationType;
    private String communicationMedium;
    private String communicationModel;
    private String idOperation;
    private boolean rnvp;
    private String context;
    private boolean sendPaperQuestionnaire;
    private boolean initAccuseReception;
    private String PartieNomFichierLibreZip;
    private List<Metadata> metadatas;

}
