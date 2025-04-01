package fr.insee.pearljam.batch.service.impl;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import fr.insee.pearljam.batch.campaign.*;
import fr.insee.pearljam.batch.communication.*;
import fr.insee.pearljam.batch.context.InterviewerType;
import fr.insee.pearljam.batch.dao.*;
import fr.insee.pearljam.batch.exception.MissingCommunicationException;
import fr.insee.pearljam.batch.exception.PublicationException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.service.MeshuggahService;
import fr.insee.pearljam.batch.utils.XmlUtils;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import fr.insee.pearljam.batch.service.CommunicationService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;

@Service
@RequiredArgsConstructor
public class CommunicationServiceImpl implements CommunicationService {
    private static final Logger LOGGER = LogManager.getLogger(CommunicationServiceImpl.class);

    private final AddressDao addressDao;
    private final CommunicationRequestDao communicationRequestDao;
    private final InterviewerTypeDao interviewerTypeDao;
    private final MeshuggahService meshuggahService;
    private final PersonDao personDao;
    private final SurveyUnitDao surveyUnitDao;
    private final VisibilityDao visibilityDao;
    private final CommunicationRequestStatusDao communicationRequestStatusDao;
    private final CommunicationMetadataDao communicationMetadataDao;

    @Value("${fr.insee.pearljam.folder.out}")
    private String FOLDER_OUT;

    @Override
    public BatchErrorCode handleCommunications() throws SynchronizationException, MissingCommunicationException {
        BatchErrorCode batchResult = BatchErrorCode.OK;
        // What to do for Communications?

        // #1 get all communicationRequests and filter by status
        List<String> toBeSentStatus = List.of("READY");
        List<CommunicationRequestType> communicationsToSend =
                communicationRequestDao.findAll().stream()
                        .filter(cr -> toBeSentStatus.contains(cr.getStatus())).toList();
        List<String> surveyUnitIds =
                communicationsToSend.stream().map(CommunicationRequestType::getSurveyUnitId).toList();

        // retrieve all matching SU
        Map<String, SurveyUnitType> surveyUnits =
                surveyUnitDao.getSurveyUnitsById(surveyUnitIds).stream()
                        .collect(Collectors.toMap(SurveyUnitType::getId, su -> su));

        // retrieve meshuggahId
        Set<String> meshuggahIds = communicationsToSend.stream()
            .map(CommunicationRequestType::getMeshuggahId)
            .collect(Collectors.toSet());

        Map<String, CommunicationTemplate> communicationTemplates = new HashMap<>();

        for (String meshuggahId : meshuggahIds) {
            CommunicationTemplate communicationTemplate = getCommunicationTemplate(meshuggahId);
            communicationTemplates.put(meshuggahId, communicationTemplate);
        }

        Date nowDate = new Date();
        String now = new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH).format(nowDate);
        // then for each retrieve data
        // #2 merge with communication data : interviewer, contact, address, OU-comm-info
        List<CommunicationData> communicationDataList = communicationsToSend.stream().map(cr -> {
            CommunicationData data = new CommunicationData();
            SurveyUnitType su = surveyUnits.get(cr.getSurveyUnitId());

            data.setEditionDate(now);
            data.setCommunicationRequestId(cr.getId());

            String reason = cr.getReason().equals("REFUSAL") ? "REF" : "IAJ";
            data.setReminderReason(reason);

            dispatchAddressData(su, data);
            dispatchOuData(su, data);
            dispatchInterviewerData(su, data);

            // communicationTemplate data
            data.setCommunicationTemplateId(cr.getMeshuggahId());
            CommunicationTemplate communicationTemplate =
                    communicationTemplates.get(cr.getMeshuggahId());

            if (communicationTemplate != null) {
                data.setTemplateMetadata(communicationTemplate.getMetadatas());
            } else {
                data.setTemplateMetadata(List.of());
            }

            return data;
        }).toList();


        // generate Courriers file

        for (Map.Entry<String, CommunicationTemplate> communicationTemplateEntry : communicationTemplates.entrySet()) {
            String comTemplId = communicationTemplateEntry.getKey();
            CommunicationTemplate template = communicationTemplateEntry.getValue();
            if (template == null) break;
            Courriers courriers = new Courriers();
            courriers.setIdOperation(template.getIdOperation());
            courriers.setTypeGenerateur(template.getCommunicationType());
            courriers.setTypeGenerateur("courrier_fo");
            courriers.setPartieNomFichierLibreZip(template.getPartieNomFichierLibreZip());
            String idEdition = meshuggahService.getNewEditionNumber();
            courriers.setEditionId(idEdition);
            courriers.setCommunicationModel(template.getCommunicationModel());

            AtomicInteger numeroDocumentIndex = new AtomicInteger(1); // Start the NumeroDocument index from 1
            // Mapping CommunicationData to Courrier and generating sequential NumeroDocument

            List<CommunicationData> filteredComData =
                    communicationDataList.stream().filter(comData -> comTemplId.equals(comData.getCommunicationTemplateId())).toList();

            surveyUnits.forEach((surveyUnitId, surveyUnit) -> {


                List<Courrier> courrierList = filteredComData.stream().map(comData -> {
                    Courrier courrier = new Courrier();
                    // Set NumeroDocument with leading zeros, format the index to 8 digits
                    String numeroDocument = String.format("%08d", numeroDocumentIndex.getAndIncrement());

                    // Create a Variables object
                    Variables variables = new Variables();
                    courrier.setVariables(variables);

                    variables.setNumeroDocument(numeroDocument);
                    variables.setBddIdentifiantUniteEnquetee(comData.getCommunicationRequestId());
                    variables.setCodePostalDestinataire(comData.getRecipientPostCode());


                    // Map other fields from CommunicationData to Courrier
                    variables.setBddAdressePosteeL1(comData.getBddL1());
                    variables.setBddAdressePosteeL2(comData.getBddL2());
                    variables.setBddAdressePosteeL3(comData.getBddL3());
                    variables.setBddAdressePosteeL4(comData.getBddL4());
                    variables.setBddAdressePosteeL5(comData.getBddL5());
                    variables.setBddAdressePosteeL6(comData.getBddL6());
                    variables.setBddAdressePosteeL7(comData.getBddL7());

                    variables.addAdditionalField("Ue_DateEdition", comData.getEditionDate());
                    variables.addAdditionalField("Ue_CiviliteEnqueteur", comData.getInterviewerTitle());
                    variables.addAdditionalField("Ue_NomEnqueteur", comData.getInterviewerLastName());
                    variables.addAdditionalField("Ue_PrenomEnqueteur", comData.getInterviewerFirstName());
                    variables.addAdditionalField("Ue_MailEnqueteur", comData.getInterviewerEmail());
                    variables.addAdditionalField("Ue_TelEnqueteur", comData.getInterviewerTel());

                    // DB always has reminderReason => check if template is REMINDER type else set to null
                    CommunicationTemplate communicationTemplate =
                        communicationTemplates.get(comData.getCommunicationTemplateId());
                    if (communicationTemplate.getCommunicationType().equals("REMINDER")) {
                        variables.addAdditionalField("Ue_TypeRelance", comData.getReminderReason());
                    }

                    variables.addAdditionalField("Ue_MailAssistance", comData.getMailAssistance());
                    variables.addAdditionalField("Ue_TelAssistance", comData.getTelAssistance());

                    String barCode = generateBarCode(idEdition, comData.getCommunicationRequestId());
                    variables.setBarcode(barCode);

                    // Set InitAccuseReception based on some business logic
                    variables.setInitAccuseReception(template.isInitAccuseReception() ? "oui" : "non");


                    // handle metadata from template
                    // keep in mind merge with surveyUnit metadata coming later

                    comData.getTemplateMetadata().forEach(meta -> variables.addAdditionalField(meta.getKey(),
                        meta.getValue()));

                    communicationMetadataDao
                        .findMetadataByCampaignIdAndMeshuggahIdAndSurveyUnitId(surveyUnit.getCampaignId(), comData.getCommunicationTemplateId(), surveyUnitId)
                        .forEach(meta -> variables.addAdditionalField(meta.getKey(), meta.getValue()));


                    return courrier;
                }).toList();
                courriers.setCourriers(courrierList);
            });





            //print to XML file or die trying

            Path communicationPath;
            try {
                communicationPath = XmlUtils.printToXmlFile(courriers, FOLDER_OUT);
            } catch (PublicationException e) {
                // hard stop here
                return BatchErrorCode.KO_TECHNICAL_ERROR;
            }

            // try to publish communication
            boolean result = meshuggahService.postPublication(communicationPath.toFile(),
                    courriers.getCommunicationModel());

            // move file accordingly
            try {
                moveFileAfterPublish(result, communicationPath.toFile(), courriers.getCommunicationModel());
            } catch (PublicationException e) {
                batchResult = BatchErrorCode.KO_TECHNICAL_ERROR;
            }

            // add SEND status to commRequestStatus
            long msDate = nowDate.getTime();
            String newStatus = result ? "SUBMITTED" : "FAILED";
            filteredComData.forEach(communicationData ->
                    communicationRequestStatusDao.addStatus(
                            communicationData.getCommunicationRequestId(),
                            newStatus,
                            msDate,
                            communicationData.getCommunicationTemplateId()
                    )
            );

            LOGGER.info("Communication {} => {}", courriers.getEditionId(), result ? "OK" : "KO");

        }


        return batchResult;
    }

    private void dispatchAddressData(SurveyUnitType su, CommunicationData data) {
        // privilegedContact or firstFound
        List<PersonType> persons =
                personDao.getPersonsBySurveyUnitId(su.getId()).stream().map(Map.Entry::getValue).toList();

        PersonType privilegedPerson =
                persons.stream().filter(PersonType::isPrivileged).findFirst().orElse(persons.getFirst());

        String recipient = generateRecipientName(privilegedPerson);
        data.setBddL1(recipient);

        //surveyUnit address for remapping
        InseeAddressType address = addressDao.getAddressBySurveyUnitId(su.getId());


        String additionalAddress = Stream.of(address.getL2(), address.getL3())
                .filter(Objects::nonNull)
                .collect(Collectors.joining(" "))
                .trim();
        if (additionalAddress.length() > 38) {
            int splittingSpace = additionalAddress.substring(0, 38).lastIndexOf(" ");
            data.setBddL2(additionalAddress.substring(0, splittingSpace));
            data.setBddL3(keep38FirstChars(additionalAddress.substring(splittingSpace).trim()));
        } else {
            data.setBddL2(additionalAddress);
            data.setBddL3("");
        }

        data.setBddL4(keep38FirstChars(address.getL4()));
        data.setBddL5(keep38FirstChars(address.getL5()));
        data.setBddL6(keep38FirstChars(address.getL6()));
        data.setBddL7(keep38FirstChars(address.getL7()));

        // specific field for postCode
        data.setRecipientPostCode(address.getL6().split(" ")[0]);
    }

    private String keep38FirstChars(String strToTruncate) {
        return strToTruncate.substring(0, Math.min(strToTruncate.length(), 38));
    }

    private void dispatchOuData(SurveyUnitType su, CommunicationData data) {
        String ouId = su.getOrganizationalUnitId();
        String campaignId = su.getCampaignId();
        OrganizationalUnitType ou = visibilityDao.getVisibilityByCampaignIdAndOrganizationUnitId(campaignId, ouId);

        data.setMailAssistance(ou.getMailCourrier());
        data.setTelAssistance(ou.getTelephoneCourrier());
    }

    private void dispatchInterviewerData(SurveyUnitType su, CommunicationData data) {
        // interviewer data
        InterviewerType interviewer = interviewerTypeDao.findById(su.getInterviewerId());

        String title = interviewer.getTitle().equals("MISS") ? "Madame" : "Monsieur";
        data.setInterviewerTitle(title);

        data.setInterviewerFirstName(interviewer.getFirstName());
        data.setInterviewerLastName(interviewer.getLastName());
        data.setInterviewerEmail(interviewer.getEmail());
        data.setInterviewerTel(interviewer.getPhoneNumber());
    }

    @Override
    public CommunicationTemplate getCommunicationTemplate(String communicationTemplateId) throws MissingCommunicationException, SynchronizationException {
        return meshuggahService.getCommunicationTemplate(communicationTemplateId);
    }


    private String generateRecipientName(PersonType person) {
        String title = person.getTitle().equals("MISS") ? "MME" : "M";

        String firstName = person.getFirstName();
        List<String> composedFirstName = Arrays.stream(firstName.split(" ")).toList();
        String lastName = person.getLastName();

        String recipientCompleteName = String.join(" ", title, firstName, lastName);
        if (recipientCompleteName.length() <= 38) return recipientCompleteName;

        String recipientShortName = String.join(" ", title, composedFirstName.getFirst(), lastName);
        if (recipientShortName.length() <= 38) return recipientShortName;

        String firstNameAcronym =
                composedFirstName.stream().map(str -> str.substring(0, 1)).collect(Collectors.joining(" ")).toUpperCase();
        String recipientShorterName = String.join(" ", title, firstNameAcronym, lastName);
        if (recipientShorterName.length() <= 38) return recipientShorterName;

        return String.join(" ", title, lastName).substring(0, 38);
    }

    public String generateBarCode(String idEdition, String communicationRequestId) {
        // Segment 1: Code identifiant de l'Insee
        StringBuilder barcode = new StringBuilder("IS");

        // Segment 2: Date de dépôt du courrier
        int dayOfYear = LocalDate.now().getDayOfYear();
        barcode.append(String.format("%03d", dayOfYear));  // Ensure it's 3 digits long

        // Segment 3: Édition (id_edition with the first two characters removed, up to 11 characters)
        String truncatedEdition = idEdition.substring(2);
        barcode.append(truncatedEdition);

        // Segment 4: Destinataire courrier (communication request ID, fiiled up to 14 chars with spaces)
        String truncatedCommunicationId = communicationRequestId.length() > 14 ? communicationRequestId.substring(0,
                14) : String.format("%-14s", communicationRequestId);
        barcode.append(truncatedCommunicationId);

        // Segment 5: Ventilation (fixed 2 spaces)
        barcode.append("  ");

        // Segment 6: Code application (fixed 3 spaces)
        barcode.append("   ");

        return barcode.toString();
    }

    public void moveFileAfterPublish(boolean publishResult, File fileToPublish, String communicationModele) throws PublicationException {
        Path sourcePath = fileToPublish.toPath();
        String subFolder = publishResult ? "/success/" : "/fail/";

        Path destinationDir = Path.of(fileToPublish.getParent(), subFolder, communicationModele);

        try {
            Files.createDirectories(destinationDir);
            Path destinationPath = destinationDir.resolve(fileToPublish.getName());
            Files.move(sourcePath, destinationPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            String warnMessage = String.format("Can't move %s to %s after publication", sourcePath, destinationDir);
            LOGGER.warn(warnMessage, e);
            throw new PublicationException(warnMessage, e);
        }

    }

}
