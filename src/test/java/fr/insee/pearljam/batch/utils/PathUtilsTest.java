package fr.insee.pearljam.batch.utils;


import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PathUtilsTest extends FileHelper {
    @BeforeEach
    void setUp() throws Exception {
        copyFiles("sampleprocessing");
    }

    /* Tests for PathUtils.java */

    @Test
    void directoryShouldExist() {
        assertTrue(PathUtils.isDirectoryExist("src/test/resources/in"));
    }

    @Test
    void directoryShouldntExist() {
        assertFalse(PathUtils.isDirectoryExist("src/test/resources/test"));
    }

    @Test
    void directoryShouldContainsExtension() {
        assertTrue(PathUtils.isDirContainsFileExtension(Path.of("src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5"), "sampleProcessing.xml"));
    }

    @Test
    void fileShouldExist() {
        assertTrue(PathUtils.isFileExist("src/test/resources/in/sampleprocessing/testScenarios/sampleprocessingScenario5/sampleProcessing.xml"));
    }
}
