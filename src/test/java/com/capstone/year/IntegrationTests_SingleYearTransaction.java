package com.capstone.year;


import com.capstone.year.Classifiers.YearClassifier;
import com.capstone.year.Configurations.BatchConfigSingleYear;
import com.capstone.year.Models.YearModel;
import com.capstone.year.Processors.SingleYearProcessor;
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

// ********************************************************************************
//                          Test Single Year Operations
// ********************************************************************************

@SpringBatchTest
@SpringJUnitConfig(classes = {
        BatchConfigSingleYear.class,
        YearClassifier.class,
        YearModel.class,
        YearReaderCSV.class,
        SingleYearProcessor.class,
        YearCompositeWriter.class,
        TaskExecutor.class
})
@EnableAutoConfiguration

public class IntegrationTests_SingleYearTransaction {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    @Autowired
    private JobLauncherTestUtils jobLauncherTestUtils;

    @Autowired
    private JobRepositoryTestUtils jobRepositoryTestUtils;

    // Set year to test for single year operations & export
    private String year = "2002";
    private String INPUT = "src/test/resources/input/test_input.csv";
    private String EXPECTED_OUTPUT = "src/test/resources/output/expected_output_SingleYearTransaction.xml";
    private String ACTUAL_OUTPUT = "src/test/resources/output/year_" + year;

//    @BeforeEach
//    public void setup(@Autowired Job job_singleUser) {
//        jobLauncherTestUtils.setJob(job_singleUser);
//    }

    @AfterEach
    public void cleanUp() {
        jobRepositoryTestUtils.removeJobExecutions();
    }

    private JobParameters testJobParameters_SingleYear() {

        return new JobParametersBuilder()
                .addString("year_param", year)
                .addString("file.input", INPUT)
                .addString("outputPath_param", ACTUAL_OUTPUT)
                .toJobParameters();
    }


    // ----------------------------------------------------------------------------------
    // --                                 TESTS                                        --
    // ----------------------------------------------------------------------------------

    @Test
    public void testBatchProcessFor_SingleYear() throws Exception {

        // Load job parameters and launch job through test suite
        JobExecution jobExecution = jobLauncherTestUtils.launchJob(testJobParameters_SingleYear());
        JobInstance actualJobInstance = jobExecution.getJobInstance();
        ExitStatus actualJobExitStatus = jobExecution.getExitStatus();

        // ----- Assertions -----
        File testInputFile = new File(INPUT);
        File testOutputFileExpected = new File(EXPECTED_OUTPUT);
        File testOutputFileActual = new File(ACTUAL_OUTPUT + "/year_" + year + "_transactions.xml");

        // Match job names
        Assertions.assertEquals("exportSingleYearJob", actualJobInstance.getJobName());

        // Match job exit status to "COMPLETED"
        Assertions.assertEquals("COMPLETED", actualJobExitStatus.getExitCode());

        // Verify input file is valid and can be read
        Assertions.assertTrue(FileUtil.canReadFile(testInputFile));

        // Verify output (expected) file is valid and can be read
        Assertions.assertTrue(FileUtil.canReadFile(testOutputFileExpected));

        // Verify output (actual) file is valid and can be read
        Assertions.assertTrue(FileUtil.canReadFile(testOutputFileActual));

        // Verify expected and actual output files match
        Assertions.assertEquals(
                FileUtils.readFileToString(testOutputFileExpected, "utf-8"),
                FileUtils.readFileToString(testOutputFileActual, "utf-8"),
                "============================== FILE MISMATCH ==============================");

    }
}

