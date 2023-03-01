package com.capstone.year;

import com.capstone.year.Classifiers.YearClassifier;
import com.capstone.year.Configurations.BatchConfigFraudByYear;
import com.capstone.year.Models.YearModel;
import com.capstone.year.PathHandlers.ReportsPathHandler;
import com.capstone.year.Processors.FraudByYearProcessor;
import com.capstone.year.Readers.YearReaderCSV;
import com.capstone.year.TaskExecutors.TaskExecutor;
import com.capstone.year.Writers.YearCompositeWriter;
import org.apache.commons.io.FileUtils;
import org.aspectj.util.FileUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.batch.core.*;
import org.springframework.batch.test.JobLauncherTestUtils;
import org.springframework.batch.test.JobRepositoryTestUtils;
import org.springframework.batch.test.context.SpringBatchTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;

@SpringBatchTest
@SpringJUnitConfig(classes = {
        BatchConfigFraudByYear.class,
        YearClassifier.class,
        YearModel.class,
        YearReaderCSV.class,
        FraudByYearProcessor.class,
        YearCompositeWriter.class,
        TaskExecutor.class,
        ReportsPathHandler.class
})
@EnableAutoConfiguration

public class IntegrationTests_FraudByYear {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    // Hardcoded year - matches first year in test_input.csv source with fraud
    private String year = "2010";
    private String INPUT = "src/test/resources/input/test_input_fraud.csv";
    private String EXPECTED_REPORTS_OUTPUT = "src/test/resources/output/expected_output_fraudReports";
    private String REPORTS_OUTPUT = "src/test/resources/output/years_with_fraud/fraud_by_year_report";
    private String EXPECTED_OUTPUT = "src/test/resources/output/expected_output_FraudByYear.xml";
    private String ACTUAL_OUTPUT = "src/test/resources/output/years_with_fraud";

    @AfterEach
    public void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    private JobParameters testJobParameters_FraudByYear() {

        return new JobParametersBuilder()
                .addString("file.input", INPUT)
                .addString("outputPath_param", ACTUAL_OUTPUT)
                .addString("reportsPath_param", REPORTS_OUTPUT)
                .toJobParameters();
    }



    // ----------------------------------------------------------------------------------
    // --                                 TESTS                                        --
    // ----------------------------------------------------------------------------------

    @Test
    public void testBatchProcessFor_FraudByYear() throws Exception {

        // Load job parameters and launch job through test suite
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(testJobParameters_FraudByYear());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        // ----- Assertions -----
        File testInputFile = new File(INPUT);
        File testOutputFileExpected = new File(EXPECTED_OUTPUT);
        File testOutputReportsFileExpected = new File(EXPECTED_REPORTS_OUTPUT);
        File testOutputFileActual = new File(ACTUAL_OUTPUT + "/year_" + year + "_transactions.xml");
        File testOutputReportsFileActual = new File(REPORTS_OUTPUT);

        // Match job names
        Assertions.assertEquals("exportFraudByYearJob", actualJobInstance.getJobName());

        // Match job exit status to "COMPLETED"
        Assertions.assertEquals("COMPLETED", actualJobExitStatus.getExitCode());

        // Verify input file is valid and can be read
        Assertions.assertTrue(FileUtil.canReadFile(testInputFile));

        // Verify output (expected) file is valid and can be read
        Assertions.assertTrue(FileUtil.canReadFile(testOutputFileExpected));

        // Verify reports output (expected) file is valid and can be read
        Assertions.assertTrue(FileUtil.canReadFile(testOutputReportsFileExpected));

        // Verify output (actual) file is valid and can be read
        Assertions.assertTrue(FileUtil.canReadFile(testOutputFileActual));

        // Verify reports output (actual) file is valid and can be read
        Assertions.assertTrue(FileUtil.canReadFile(testOutputReportsFileActual));

        // Verify expected and actual output files match (file _1)
        Assertions.assertEquals(
                FileUtils.readFileToString(testOutputFileExpected, "utf-8"),
                FileUtils.readFileToString(testOutputFileActual, "utf-8"),
                "============================== FILE MISMATCH ==============================");

        // Verify expected and actual reports files match
        Assertions.assertEquals(
                FileUtils.readFileToString(testOutputReportsFileExpected, "utf-8"),
                FileUtils.readFileToString(testOutputReportsFileActual, "utf-8"),
                "============================== FILE MISMATCH ==============================");
    }
}
