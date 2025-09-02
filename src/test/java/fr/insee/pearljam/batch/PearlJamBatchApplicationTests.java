package fr.insee.pearljam.batch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import fr.insee.pearljam.batch.service.ContextReferentialService;
import fr.insee.pearljam.batch.service.HabilitationService;

/**
 * This class implements the two classes of test It also initialize the testing
 * part.
 * 
 * @author scorcaud
 *
 */

abstract class PearlJamBatchApplicationTests {

	/**
	 * This method copy the necessary files that are used in the tests. This method
	 * is useful because of the clean and reset method which remove these two files.
	 * 
	 * @throws IOException
	 */
	static void copyFiles(String name) throws IOException {
		File initInDir = new File("src/test/resources/in/" + name + "/init");
		File testScenarioInDir = new File("src/test/resources/in/" + name + "/testScenarios");
		if (!testScenarioInDir.exists()) {
			testScenarioInDir.mkdir();
		}
		
		File testScenarioProcessingDir = new File("src/test/resources/in/" + name + "/testScenarios/processing");
		if (!testScenarioProcessingDir.exists()) {
			testScenarioProcessingDir.mkdir();
		}
		
		FileUtils.copyDirectory(initInDir, testScenarioInDir);
		List<Path> subfolder = Files.walk(new File("src/test/resources/in/" + name + "/testScenarios").toPath(), 1)
	            .filter(Files::isDirectory)
	            .collect(Collectors.toList());
		subfolder.remove(0);
		subfolder.parallelStream().forEach(path -> 
			new File("src/test/resources/in/" + name + "/testScenarios/" + path.getFileName().toString() + "/processing").mkdir());
		
		File outDir = new File("src/test/resources/out");
		if (!outDir.exists()) {
			outDir.mkdir();
		}
		File campaignOutDir = new File("src/test/resources/out/" + name);
		if (!campaignOutDir.exists()) {
			campaignOutDir.mkdir();
		}
		File testScenarioOutDir = new File("src/test/resources/out/" + name + "/testScenarios");
		if (!testScenarioOutDir.exists()) {
			testScenarioOutDir.mkdir();
		}
		File unitTestsOutDir = new File("src/test/resources/out/unitTests");
		if (!unitTestsOutDir.exists()) {
			unitTestsOutDir.mkdir();
		}
		
		File synchroTestsOutDir = new File("src/test/resources/out/contextReferentialSynchro");
		if (!synchroTestsOutDir.exists()) {
			synchroTestsOutDir.mkdir();
		}
		File synchroTestsOutDirSynchroFolder = new File("src/test/resources/out/contextReferentialSynchro/synchro");
		if (!synchroTestsOutDirSynchroFolder.exists()) {
			synchroTestsOutDirSynchroFolder.mkdir();
		}
		File processingFolder = new File("src/test/resources/processing");
		if (!processingFolder.exists()) {
			processingFolder.mkdir();
		}
		File dirInSample = new File("src/test/resources/in/sample");
		if (!dirInSample.exists()) {
			dirInSample.mkdir();
		}
		File dirOutSample = new File("src/test/resources/out/sample");
		if (!dirOutSample.exists()) {
			dirOutSample.mkdir();
		}
		File dirInCampaign = new File("src/test/resources/in/campaign");
		if (!dirInCampaign.exists()) {
			dirInCampaign.mkdir();
		}
		File dirOutCampaign = new File("src/test/resources/out/campaign");
		if (!dirOutCampaign.exists()) {
			dirOutCampaign.mkdir();
		}
	}

	void purgeDirectory(File dir) {
		if(dir == null || !dir.exists() || !dir.isDirectory() || dir.listFiles() == null) {
			return;
		}

		for (File file: dir.listFiles()) {
			if (file.isFile())
				file.delete();
		}
	}

	@Bean
	@Primary
	ContextReferentialService contextReferentialService() {
		return Mockito.mock(ContextReferentialService.class);
	}

	@Bean
	@Primary
	HabilitationService habilitationService() {
		return Mockito.mock(HabilitationService.class);
	}
}