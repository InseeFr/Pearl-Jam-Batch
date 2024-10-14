package fr.insee.pearljam.batch;
import java.io.IOException;
import java.sql.SQLException;

import javax.xml.stream.XMLStreamException;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import fr.insee.pearljam.batch.config.ApplicationConfig;
import fr.insee.pearljam.batch.config.ApplicationContext;
import fr.insee.pearljam.batch.enums.BatchOption;
import fr.insee.pearljam.batch.exception.ArgumentException;
import fr.insee.pearljam.batch.exception.BatchException;
import fr.insee.pearljam.batch.exception.DataBaseException;
import fr.insee.pearljam.batch.exception.FolderException;
import fr.insee.pearljam.batch.exception.ValidateException;
import fr.insee.pearljam.batch.service.PilotageDBService;
import fr.insee.pearljam.batch.service.PilotageFolderService;
import fr.insee.pearljam.batch.service.PilotageLauncherService;
import fr.insee.pearljam.batch.service.TriggerService;
import fr.insee.pearljam.batch.utils.BatchErrorCode;
import fr.insee.pearljam.batch.utils.PathUtils;

/**
 * Launcher : Pearl Jam Batch main class
 * 
 * @author Claudel Benjamin
 * 
 */
public abstract class Launcher {
	/**
	 * The folder in use to insert datas
	 */
	public static String FOLDER_IN;
	/**
	 * The folder out use to store logs and file treated
	 */
	public static String FOLDER_OUT;
	
	/**
	 * The Application context
	 */
	static AnnotationConfigApplicationContext context;

	static PilotageDBService pilotageDBService;
	static PilotageFolderService pilotageFolderService;
	static PilotageLauncherService pilotageLauncherService;
	static TriggerService triggerService;
	
	/**
	 * The class logger
	 */
	private static final Logger logger = LogManager.getLogger(Launcher.class);

	public static void main(String[] args) throws IOException, ValidateException, SQLException, XMLStreamException {
		context = new AnnotationConfigApplicationContext(ApplicationContext.class);
		pilotageDBService = context.getBean(PilotageDBService.class);
		pilotageFolderService = context.getBean(PilotageFolderService.class);
		pilotageLauncherService = context.getBean(PilotageLauncherService.class);
		BatchErrorCode batchErrorCode = BatchErrorCode.OK;
		try {
			initBatch();
			checkFolderTree();
			batchErrorCode = runBatch(args);
		} catch (ArgumentException | FolderException | IOException | SQLException | DataBaseException te) {
			logger.log(Level.ERROR, te.getMessage(), te);
			batchErrorCode = BatchErrorCode.KO_TECHNICAL_ERROR;
		} catch (BatchException | XMLStreamException | ValidateException fe) {
			logger.log(Level.ERROR, fe.getMessage(), fe);
			batchErrorCode = BatchErrorCode.KO_FONCTIONAL_ERROR;
		} 
		finally {
			logger.log(Level.INFO, Constants.MSG_RETURN_CODE, batchErrorCode);
			pilotageDBService.closeConnection();
			context.close();
			System.exit(batchErrorCode.getCode());
		}
	}

	/**
	 * Init Batch check all prerequisities before run batch : folder properties and
	 * database structure
	 * 
	 * @throws FolderException
	 * @throws SQLException
	 * @throws DataBaseException
	 */
	public static void initBatch() throws FolderException, DataBaseException, SQLException {
		// Check folder properties
		FOLDER_IN = ApplicationConfig.FOLDER_IN;
		FOLDER_OUT = ApplicationConfig.FOLDER_OUT;
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
	 * @throws FolderException
	 */
	public static void checkFolderTree() throws FolderException {
		PathUtils.createMissingFolder(FOLDER_IN);
		PathUtils.createMissingFolder(FOLDER_IN + "/processing");
		PathUtils.createMissingFolder(FOLDER_IN + "/sample");
		PathUtils.createMissingFolder(FOLDER_IN + "/campaign");
		PathUtils.createMissingFolder(FOLDER_OUT);
		PathUtils.createMissingFolder(FOLDER_OUT + "/sample");
		PathUtils.createMissingFolder(FOLDER_OUT + "/campaign");
		PathUtils.createMissingFolder(FOLDER_OUT + "/synchro");
	}


	/**
	 * run batch : Check if argument is well fielded ant start to run the batch
	 * @param options arguments define on cmd execution
	 * @return BatchErrorCode of batch execution
	 * @throws ArgumentException
	 * @throws ValidateException
	 * @throws BatchException
	 * @throws IOException
	 * @throws SQLException 
	 * @throws XMLStreamException 
	 * @throws FolderException 
	 */
	public static BatchErrorCode runBatch(String[] options)
			throws ArgumentException, ValidateException, BatchException, IOException, SQLException, XMLStreamException, FolderException {
		if (options.length == 0) {
			throw new ArgumentException(
					"No batch type found in parameter, you must choose between [DELETECAMPAIGN] || [EXTRACT] || [LOADCONTEXT] || [DAILYUPDATE] || [SYNCHRONIZE] || [SAMPLEPROCESSING]");
		}
		BatchOption batchOption = null;
		try {
			batchOption = BatchOption.valueOf(options[0].trim());
		} catch (Exception e) {
			throw new ArgumentException("Batch type [" + options[0].trim()
					+ "] does not exist, you must choose between [DELETECAMPAIGN] || [EXTRACT] || [LOADCONTEXT] || [DAILYUPDATE] || [SYNCHRONIZE] || [SAMPLEPROCESSING]");
		}
		logger.log(Level.INFO, "Batch is running with option {}", batchOption.getLabel());
		switch(batchOption) {
			// Update states of survey units based on the visibility dates
			case DAILYUPDATE:
				triggerService = context.getBean(TriggerService.class);
				return triggerService.updateStates();
			// synchronize interviewers and survey unit affectations for the interviewers
			case SYNCHRONIZE:
				logger.log(Level.INFO, "Running synchronization with context referential");
				triggerService = context.getBean(TriggerService.class);
				return triggerService.synchronizeWithOpale(FOLDER_OUT);
			// use pilotage launcher
			default:
				return pilotageLauncherService.validateLoadClean(batchOption, FOLDER_IN, FOLDER_OUT);
		}
		

	}
}
