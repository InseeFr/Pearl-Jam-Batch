package fr.insee.pearljam.batch.utils;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.TestComponent;

import javax.sql.DataSource;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.Statement;

/**
 * This class implements the two classes of test It also initialize the testing
 * part.
 *
 * @author scorcaud
 *
 */

@TestComponent
public class DBResetHelper {

	private static final Logger logger = LogManager.getLogger(DBResetHelper.class);

	@Autowired
	@Qualifier("pilotageDataSource")
	private DataSource pilotageDataSource;

	@Autowired
	@Qualifier("dataCollectionDataSource")
	private DataSource dataCollectionDataSource;

	@Autowired
	@Qualifier("pilotageConnection")
	private Connection pilotageConnection;

	@Autowired
	@Qualifier("dataCollectionConnection")
	private Connection dataCollectionConnection;

	/**
	 * This method initialize the data for testing
	 *
	 * @throws Exception
	 */
	public void reinitData() throws Exception {
		executeSql("src/test/resources/sql/pilotage/reinit-data.sql", pilotageConnection);
		logger.info("DB pilotage resetted");
		executeSql("src/test/resources/sql/datacollection/reinit-data.sql", dataCollectionConnection);
		logger.info("DB data collection resetted");
	}

	private void executeSql(String sqlFilePath, Connection connection) throws Exception {
		connection.setAutoCommit(false);
		Statement stmt = connection.createStatement();
		String sql = new String(Files.readAllBytes(Paths.get(sqlFilePath)));
		stmt.execute(sql);
		connection.commit();
	}
}