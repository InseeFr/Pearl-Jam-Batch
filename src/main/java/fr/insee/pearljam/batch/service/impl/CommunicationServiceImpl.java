package fr.insee.pearljam.batch.service.impl;

import fr.insee.pearljam.batch.campaign.*;
import fr.insee.pearljam.batch.communication.*;
import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.context.InterviewerType;
import fr.insee.pearljam.batch.dao.*;
import fr.insee.pearljam.batch.exception.MissingAddressException;
import fr.insee.pearljam.batch.exception.MissingCommunicationException;
import fr.insee.pearljam.batch.exception.PublicationException;
import fr.insee.pearljam.batch.exception.SynchronizationException;
import fr.insee.pearljam.batch.service.CommunicationService;
import fr.insee.pearljam.batch.service.MeshuggahService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.XmlUtils;
import lombok.RequiredArgsConstructor;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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

	private final String outputFolder = ApplicationConfig.FOLDER_OUT;

	@Override
	public BatchErrorCode handleCommunications() throws SynchronizationException, MissingCommunicationException {
		BatchErrorCode batchResult = BatchErrorCode.OK;
		List<String> failedSurveyUnits = new ArrayList<>();

		List<CommunicationRequestType> communicationsToSend = getReadyCommunicationRequests();
		if (communicationsToSend.isEmpty()) return batchResult;

		Map<String, SurveyUnitType> surveyUnits = getSurveyUnits(communicationsToSend);
		Map<String, CommunicationTemplate> communicationTemplates = loadCommunicationTemplates(communicationsToSend);

		// Gather and merge data for each communicationRequest
		List<CommunicationData> communicationDataList = new ArrayList<>();

		for (CommunicationRequestType cr : communicationsToSend) {
			SurveyUnitType su = surveyUnits.get(cr.getSurveyUnitId());
			CommunicationTemplate template = communicationTemplates.get(cr.getMeshuggahId());
			if (template == null) continue;

			CommunicationData data = new CommunicationData();
			data.setEditionDate(new SimpleDateFormat("d MMMM yyyy", Locale.FRENCH).format(new Date()));
			data.setCommunicationRequestId(cr.getId());
			data.setSurveyUnitBusinessId(su.getDisplayName());
			data.setReminderReason("REFUSAL".equals(cr.getReason()) ? "REF" : "IAJ");
			data.setCommunicationTemplateId(cr.getMeshuggahId());

			try {
				dispatchAddressData(su, data);
				dispatchOuData(su, data);
				dispatchInterviewerData(su, data);
				data.setTemplateMetadata(mergeMetadata(su, template));
				communicationDataList.add(data);
			} catch (MissingAddressException | NullPointerException e) {
				LOGGER.warn("Skipping survey unit {} due to address or interviewer error: {}", su.getId(), e.getMessage());
				failedSurveyUnits.add(su.getId());
			}
		}

		// Write a Courriers file for each communicationTemplate
		// Send it via API then move it in matching result folder
		for (Map.Entry<String,CommunicationTemplate> entry : communicationTemplates.entrySet()) {
			String templateId = entry.getKey();
			CommunicationTemplate template = entry.getValue();
			if (template == null) continue;

			Courriers courriers = buildCourriers(template, communicationDataList, templateId);
			Path xmlPath;
			try {
				xmlPath = XmlUtils.printToXmlFile(courriers, outputFolder);
			} catch (PublicationException e) {
				return BatchErrorCode.KO_TECHNICAL_ERROR;
			}

			boolean published = meshuggahService.postPublication(xmlPath.toFile(), courriers.getCommunicationModel());
			try {
				moveFileAfterPublish(published, xmlPath.toFile(), courriers.getCommunicationModel());
			} catch (PublicationException e) {
				batchResult = BatchErrorCode.KO_TECHNICAL_ERROR;
			}

			updateStatus(communicationDataList, templateId, published);
			LOGGER.info("Communication {} => {}", courriers.getEditionId(), published ? "OK" : "KO");
		}

		if (!failedSurveyUnits.isEmpty()) {
			LOGGER.warn("Some survey units failed to process: {}", failedSurveyUnits);
			batchResult = BatchErrorCode.KO_FONCTIONAL_ERROR;
		}

		return batchResult;
	}

	private List<Metadata> mergeMetadata(SurveyUnitType su, CommunicationTemplate template) {
		if (template == null) return List.of();

		List<Metadata> base = template.getMetadatas();
		List<Metadata> fromDb = communicationMetadataDao
				.findMetadataByCampaignIdAndMeshuggahIdAndSurveyUnitId(
						su.getCampaignId(), template.getCommunicationId(), su.getId())
				.stream()
				.map(m -> new Metadata(m.getKey(), m.getValue()))
				.toList();
		// here we keep the second value : template default value should be replaced by SU metadata value
		return Stream.concat(base.stream(), fromDb.stream())
				.collect(Collectors.toMap(Metadata::getKey, m -> m, (m1, m2) -> m2))
				.values().stream().toList();
	}


	private Courriers buildCourriers(CommunicationTemplate template, List<CommunicationData> allData,
									 String templateId) throws SynchronizationException {
		List<CommunicationData> relevantData = allData.stream()
				.filter(Objects::nonNull)
				.filter(communicationData -> templateId.equals(communicationData.getCommunicationTemplateId()))
				.toList();

		String editionId = meshuggahService.getNewEditionNumber();
		List<Courrier> courriers = new ArrayList<>();
		AtomicInteger docIndex = new AtomicInteger(1);

		for (CommunicationData data : relevantData) {
			courriers.add(CourrierBuilder.buildFrom(data, template, editionId, docIndex.getAndIncrement()));
		}

		Courriers result = new Courriers();
		result.setCourriers(courriers);
		result.setEditionId(editionId);
		result.setIdOperation(template.getIdOperation());
		result.setCommunicationModel(template.getCommunicationModel());
		result.setTypeGenerateur("courrier_fo");
		result.setPartieNomFichierLibreZip(template.getPartieNomFichierLibreZip());
		return result;
	}


	private List<CommunicationRequestType> getReadyCommunicationRequests() {
		return communicationRequestDao.findAll().stream()
				.filter(req -> "READY".equals(req.getStatus()))
				.toList();
	}

	private Map<String, SurveyUnitType> getSurveyUnits(List<CommunicationRequestType> requests) {
		List<String> ids = requests.stream().map(CommunicationRequestType::getSurveyUnitId).toList();
		return surveyUnitDao.getSurveyUnitsById(ids).stream()
				.collect(Collectors.toMap(SurveyUnitType::getId, su -> su));
	}

	private Map<String, CommunicationTemplate> loadCommunicationTemplates(List<CommunicationRequestType> requests)
			throws MissingCommunicationException, SynchronizationException {
		Set<String> ids = requests.stream().map(CommunicationRequestType::getMeshuggahId).collect(Collectors.toSet());
		Map<String, CommunicationTemplate> result = new HashMap<>();
		for (String id : ids) {
			result.put(id, meshuggahService.getCommunicationTemplate(id));
		}
		return result;
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
		if (address == null || address.getL6() == null) {
			throw new MissingAddressException(su.getId());
		}

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

	private void updateStatus(List<CommunicationData> allData, String templateId, boolean success) {
		long timestamp = System.currentTimeMillis();
		String status = success ? "SUBMITTED" : "FAILED";
		allData.stream()
				.filter(d -> templateId.equals(d.getCommunicationTemplateId()))
				.forEach(data -> communicationRequestStatusDao.addStatus(data.getCommunicationRequestId(), status,
						timestamp));
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
