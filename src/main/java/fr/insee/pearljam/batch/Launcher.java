package fr.insee.pearljam.batch;

import java.io.IOException;
import java.sql.SQLException;

import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.exception.*;
import fr.insee.pearljam.batch.service.CommunicationService;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.ExitCodeGenerator;

import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.service.PilotageDBService;
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import fr.insee.pearljam.batch.service.TriggerService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.xml.stream.XMLStreamException;

/**
 * Launcher : Pearl Jam Batch main class
 *
 * @author Claudel Benjamin
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
public class Launcher implements CommandLineRunner, ExitCodeGenerator {
    private final PilotageDBService pilotageDBService;
    private final PilotageLauncherService pilotageLauncherService;
    private final TriggerService triggerService;
    private final CommunicationService communicationService;
    private final ApplicationConfig applicationConfig;

    private BatchErrorCode exitCode = BatchErrorCode.OK;

    private static final Logger logger = LogManager.getLogger(Launcher.class);

    public void run(String[] args) throws SQLException {
        try {
            initBatch();
            checkFolderTree();
            exitCode = runBatch(args);
        } catch (BatchException | XMLStreamException | ValidateException fe) {
            logger.error(fe.getMessage(), fe);
            exitCode = BatchErrorCode.KO_FONCTIONAL_ERROR;
        } catch (Exception te) {
            logger.error(te.getMessage(), te);
            exitCode = BatchErrorCode.KO_TECHNICAL_ERROR;
        } finally {
            logger.info(Constants.MSG_RETURN_CODE, exitCode);
            pilotageDBService.closeConnection();
        }
    }

    /**
     * Init Batch check all prerequisities before run batch : folder properties and
     * database structure
     *
     * @throws FolderException   e
     * @throws SQLException      e
     * @throws DataBaseException e
     */
    public void initBatch() throws FolderException, DataBaseException, SQLException {
        String folderIn = applicationConfig.folderIn();
        String folderOut = applicationConfig.folderOut();
        // Check folder properties
        if (StringUtils.isBlank(folderIn) || "${fr.insee.pearljam.folder.in}".equals(folderIn)) {
            throw new FolderException("property fr.insee.pearljam.batch.folder.in is not defined in properties");
        }
        if (StringUtils.isBlank(folderOut) || "${fr.insee.pearljam.folder.out}".equals(folderOut)) {
            throw new FolderException("property fr.insee.pearljam.batch.folder.out is not defined in properties");
        }
        logger.log(Level.INFO, "Folder properties are OK");

        // Check database

        pilotageDBService.checkDatabaseAccess();
        logger.log(Level.INFO, "Database is OK");
    }

    /**
     * Check folder tree : folders defined in properties exist or not. If folder not
     * exist, folder is created
     *
     * @throws FolderException when I/O exception is thrown
     */
    public void checkFolderTree() throws FolderException {
        String folderIn = applicationConfig.folderIn();
        String folderOut = applicationConfig.folderOut();
        PathUtils.createMissingFolder(folderIn);
        PathUtils.createMissingFolder(folderIn + "/processing");
        PathUtils.createMissingFolder(folderIn + "/sample");
        PathUtils.createMissingFolder(folderIn + "/campaign");
        PathUtils.createMissingFolder(folderOut);
        PathUtils.createMissingFolder(folderOut + "/sample");
        PathUtils.createMissingFolder(folderOut + "/campaign");
        PathUtils.createMissingFolder(folderOut + "/synchro");
        PathUtils.createMissingFolder(folderOut + "/communication");
        PathUtils.createMissingFolder(folderOut + "/communication/success");
        PathUtils.createMissingFolder(folderOut + "/communication/fail");
    }


    /**
     * run batch : Check if argument is well fielded ant start to run the batch
     *
     * @param options arguments define on cmd execution
     * @return BatchErrorCode of batch execution
     * @throws ArgumentException  e
     * @throws ValidateException  e
     * @throws BatchException     e
     * @throws IOException        e
     * @throws SQLException       e
     * @throws XMLStreamException e
     */
    public BatchErrorCode runBatch(String[] options)
            throws ArgumentException, ValidateException, BatchException, IOException, SQLException,
            XMLStreamException {
        String folderIn = applicationConfig.folderIn();
        String folderOut = applicationConfig.folderOut();
        if (options.length == 0) {
            throw new ArgumentException(
                    "No batch type found in parameter, you must choose between [DELETECAMPAIGN] || [EXTRACT] || " +
                            "[LOADCONTEXT] || [DAILYUPDATE] || [SYNCHRONIZE] || [SAMPLEPROCESSING] || [COMMUNICATION]");
        }
        BatchOption batchOption;
        try {
            batchOption = BatchOption.valueOf(options[0].trim());
        } catch (Exception e) {
            throw new ArgumentException("Batch type [" + options[0].trim()
                    + "] does not exist, you must choose between [DELETECAMPAIGN] || [EXTRACT] || [LOADCONTEXT] || " +
                    "[DAILYUPDATE] || [SYNCHRONIZE] || [SAMPLEPROCESSING] || [COMMUNICATION]");
        }
        logger.log(Level.INFO, "Batch is running with option {}", batchOption.getLabel());

        return switch (batchOption) {
            // Update states of survey units based on the visibility dates
            case DAILYUPDATE -> triggerService.updateStates();
            // synchronize interviewers and survey unit affectations for the interviewers
            case SYNCHRONIZE -> triggerService.synchronizeWithOpale(folderOut);
            // send communications
            case COMMUNICATION -> {
                try {
                    yield communicationService.handleCommunications();
                } catch (SynchronizationException | MissingCommunicationException e) {
                    yield BatchErrorCode.KO_TECHNICAL_ERROR;
                }
            }
            // use pilotage launcher
            default -> pilotageLauncherService.validateLoadClean(batchOption, folderIn, folderOut);
        };


    }

    @Override
    public int getExitCode() {
        return exitCode.getCode();
    }
}
