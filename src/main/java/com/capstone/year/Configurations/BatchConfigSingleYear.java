package com.capstone.year.Configurations;

import com.capstone.year.Classifiers.YearClassifier;
import com.capstone.year.Models.YearModel;
import com.capstone.year.Processors.SingleYearProcessor;
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
public class BatchConfigSingleYear {

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
    private SingleYearProcessor singleYearProcessor;

    @Autowired
    @Qualifier("writer_Year")
    private ClassifierCompositeItemWriter<YearModel> classifierCompositeItemWriter;

    @Autowired
    @Qualifier("taskExecutor_Year")
    private org.springframework.core.task.TaskExecutor asyncTaskExecutor;

    @Autowired
    private YearClassifier transactionYearClassifier;

    // ----------------------------------------------------------------------------------
    // --                             STEPS & JOBS                                     --
    // ----------------------------------------------------------------------------------

    // Step - Export single year transactions
    @Bean
    public Step step_exportSingleYear() {

        return new StepBuilder("exportSingleYearStep", jobRepository)
                .<YearModel, YearModel> chunk(50000, transactionManager)
                .reader(synchronizedItemStreamReader)
                .processor(singleYearProcessor)
                .writer(classifierCompositeItemWriter)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {
                        transactionYearClassifier.closeAllwriters();
                        log.info("------------------------------------------------------------------");
                        log.info(stepExecution.getSummary());
                        log.info("------------------------------------------------------------------");

                        singleYearProcessor.clearAllTrackersAndCounters();

                        return StepExecutionListener.super.afterStep(stepExecution);
                    }
                })
                .taskExecutor(asyncTaskExecutor)
                .build();
    }

    // Job - Export single year transactions
    @Bean
    public Job job_exportSingleYear() {

        return new JobBuilder("exportSingleYearJob", jobRepository)
                .start(step_exportSingleYear())
                .build();
    }
}
