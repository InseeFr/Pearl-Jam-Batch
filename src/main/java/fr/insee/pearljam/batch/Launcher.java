package fr.insee.pearljam.batch;

import java.io.IOException;
import java.sql.SQLException;

import javax.xml.stream.XMLStreamException;

import fr.insee.pearljam.batch.exception.*;
import fr.insee.pearljam.batch.service.CommunicationService;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.service.PilotageDBService;
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import fr.insee.pearljam.batch.service.TriggerService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;
import org.springframework.stereotype.Component;

/**
 * Launcher : Pearl Jam Batch main class
 *
 * @author Claudel Benjamin
 */
@Component
public class Launcher {
    /**
     * The in folder used to input batch file
     */
    @Value("${fr.insee.pearljam.folder.in}")
    private String FOLDER_IN;
    /**
     * The out folder used to store logs, treated file and output files
     */
    @Value("${fr.insee.pearljam.folder.out}")
    private String FOLDER_OUT;


    @Autowired
    private PilotageDBService pilotageDBService;

    @Autowired
    private PilotageLauncherService pilotageLauncherService;

    @Autowired
    private TriggerService triggerService;

    @Autowired
    private CommunicationService communicationService;


    /**
     * The class logger
     */
    private static final Logger logger = LogManager.getLogger(Launcher.class);

    public static void main(String[] args) throws IOException, ValidateException, SQLException, XMLStreamException {
        // Spring context initialization
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(ApplicationContext.class);
        Launcher launcher = context.getBean(Launcher.class);
        launcher.run(args);
    }

    public void run(String[] args) throws  SQLException {
        BatchErrorCode batchErrorCode = BatchErrorCode.OK;
        try {
            initBatch();
            checkFolderTree();
            batchErrorCode = runBatch(args);
        } catch (ArgumentException | FolderException | IOException | SQLException | DataBaseException te) {
            logger.error(te.getMessage(), te);
            batchErrorCode = BatchErrorCode.KO_TECHNICAL_ERROR;
        } catch (BatchException | XMLStreamException | ValidateException fe) {
            logger.error(fe.getMessage(), fe);
            batchErrorCode = BatchErrorCode.KO_FONCTIONAL_ERROR;
        } finally {
            logger.info(Constants.MSG_RETURN_CODE, batchErrorCode);
            pilotageDBService.closeConnection();
        }
        System.exit(batchErrorCode.getCode());
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
        // Check folder properties
        if (StringUtils.isBlank(FOLDER_IN) || "${fr.insee.pearljam.folder.in}".equals(FOLDER_IN)) {
            throw new FolderException("property fr.insee.pearljam.batch.folder.in is not defined in properties");
        }
        if (StringUtils.isBlank(FOLDER_OUT) || "${fr.insee.pearljam.folder.out}".equals(FOLDER_OUT)) {
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
        PathUtils.createMissingFolder(FOLDER_IN);
        PathUtils.createMissingFolder(FOLDER_IN + "/processing");
        PathUtils.createMissingFolder(FOLDER_IN + "/sample");
        PathUtils.createMissingFolder(FOLDER_IN + "/campaign");
        PathUtils.createMissingFolder(FOLDER_OUT);
        PathUtils.createMissingFolder(FOLDER_OUT + "/sample");
        PathUtils.createMissingFolder(FOLDER_OUT + "/campaign");
        PathUtils.createMissingFolder(FOLDER_OUT + "/synchro");
        PathUtils.createMissingFolder(FOLDER_OUT + "/communication");
        PathUtils.createMissingFolder(FOLDER_OUT + "/communication/success");
        PathUtils.createMissingFolder(FOLDER_OUT + "/communication/fail");
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
     * @throws FolderException    e
     */
    public BatchErrorCode runBatch(String[] options)
            throws ArgumentException, ValidateException, BatchException, IOException, SQLException,
            XMLStreamException, FolderException {
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
            case SYNCHRONIZE -> triggerService.synchronizeWithOpale(FOLDER_OUT);
            // send communications
            case COMMUNICATION -> {
                try {
                    yield communicationService.handleCommunications();
                } catch (SynchronizationException | MissingCommunicationException e) {
                    yield BatchErrorCode.KO_TECHNICAL_ERROR;
                }
            }
            // use pilotage launcher
            default -> pilotageLauncherService.validateLoadClean(batchOption, FOLDER_IN, FOLDER_OUT);
        };


    }
}
