package com.capstone.year.Configurations;

import com.capstone.year.Classifiers.YearClassifier;
import com.capstone.year.Models.YearModel;
import com.capstone.year.Processors.AllYearsProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.*;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamReader;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

@Configuration
@Slf4j
public class BatchConfigAllYears {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    @Qualifier("reader_Year")
    private SynchronizedItemStreamReader<YearModel> synchronizedItemStreamReader;

    @Autowired
    private AllYearsProcessor allYearsProcessor;

    @Autowired
    @Qualifier("writer_Year")
    private ClassifierCompositeItemWriter<YearModel> classifierCompositeItemWriter;

    @Autowired
    @Qualifier("taskExecutor_Year")
    private org.springframework.core.task.TaskExecutor asyncTaskExecutor;

    @Autowired
    private YearClassifier transactionTypeClassifier;

    // ----------------------------------------------------------------------------------
    // --                             STEPS & JOBS                                     --
    // ----------------------------------------------------------------------------------

    // Step - all years
    @Bean
    public Step step_exportAllYears() {

        return new StepBuilder("exportAllYearsStep", jobRepository)
                .<YearModel, YearModel> chunk(50000, transactionManager)
                .reader(synchronizedItemStreamReader)
                .processor(allYearsProcessor)
                .writer(classifierCompositeItemWriter)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        transactionTypeClassifier.closeAllwriters();
                        log.info("------------------------------------------------------------------");
                        log.info(stepExecution.getSummary());
                        log.info("------------------------------------------------------------------");

                        allYearsProcessor.clearAllTrackersAndCounters();

                        return StepExecutionListener.super.afterStep(stepExecution);
                    }
                })
                .taskExecutor(asyncTaskExecutor)
                .build();
    }

    // Job - all years
    @Bean
    public Job job_exportAllYears() {

        return new JobBuilder("exportAllYearsJob", jobRepository)
                .start(step_exportAllYears())
                .build();
    }
}
