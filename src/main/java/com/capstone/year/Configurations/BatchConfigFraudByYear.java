package com.capstone.year.Configurations;

import com.capstone.year.Classifiers.YearClassifier;
import com.capstone.year.Controllers.YearController;
import com.capstone.year.Models.YearModel;
import com.capstone.year.Processors.FraudByYearProcessor;
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

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

@Configuration
@Slf4j
public class BatchConfigFraudByYear {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    // Sort a HashMap by keys
    public static Map<String, ArrayList<Long>> sortHashMapByKey(Map<String, ArrayList<Long>> hashMap)
    {
        // Create a list from elements of HashMap
        List<Map.Entry<String, ArrayList<Long>> > list = new LinkedList<>(hashMap.entrySet());

        // Sort the list using lambda expression
        Collections.sort(
                list,
                Comparator.comparing(Map.Entry::getKey));

        // put data from sorted list to hashmap

        HashMap<String, ArrayList<Long>> temp = new LinkedHashMap<>();

        for (Map.Entry<String, ArrayList<Long>> aa : list) {
            temp.put(aa.getKey(), new ArrayList<>());
            temp.get(aa.getKey()).add(aa.getValue().get(0));
            temp.get(aa.getKey()).add(aa.getValue().get(1));
        }
        return temp;
    }

    @Autowired
    private JobRepository jobRepository;

    @Autowired
    private PlatformTransactionManager transactionManager;

    @Autowired
    @Qualifier("reader_Year")
    private SynchronizedItemStreamReader<YearModel> synchronizedItemStreamReader;

    @Autowired
    private FraudByYearProcessor fraudByYearProcessor;

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

    // Step - fraud by year
    @Bean
    public Step step_exportFraudByYear() {

        return new StepBuilder("exportFraudByYearStep", jobRepository)
                .<YearModel, YearModel> chunk(50000, transactionManager)
                .reader(synchronizedItemStreamReader)
                .processor(fraudByYearProcessor)
                .writer(classifierCompositeItemWriter)
                .listener(new StepExecutionListener() {
                    @Override
                    public ExitStatus afterStep(StepExecution stepExecution) {

                        transactionTypeClassifier.closeAllwriters();

                        // Create reports file using reports file path from Controller API call
                        String filePath = YearController.getReportsPath();
                        File fraudByYearReport = new File(filePath);

                        // Make copy of HashMap from fraud by year processor (holds total transactions and total fraud transactions per year)
                        // Sort HashMap
                        Map<String, ArrayList<Long>> tempHashMap = sortHashMapByKey(fraudByYearProcessor.returnHashMap());
                        //HashMap<String, ArrayList<Long>> tempHashMap = fraudByYearProcessor.returnHashMap();

                        float tempTotalTransactions = 0;
                        float tempTotalFraud = 0;
                        float fraudPercentage = 0;
                        String adjFraudPercentage;

                        // Write relevant data to reports file
                        try {
                            BufferedWriter writer = new BufferedWriter(new FileWriter(fraudByYearReport));

                            // Reports file header
                            writer.write("Year" + "\t" + "Total Transactions" + "\t" + "Fraud Transactions" + "\t\t" + "Fraud %");
                            writer.newLine();
                            writer.newLine();

                            // Iterate over HashMap and print out year, year total transactions, year fraud transactions, and fraud %
                            for (String year : tempHashMap.keySet()) {

                                tempTotalTransactions = (float) tempHashMap.get(year).get(0); // total transactions for that year
                                tempTotalFraud = (float) tempHashMap.get(year).get(1); // total fraud transactions for that year
                                fraudPercentage = tempTotalFraud / tempTotalTransactions;
                                DecimalFormat df = new DecimalFormat("#");
                                df.setMaximumFractionDigits(8);
                                adjFraudPercentage = df.format(fraudPercentage);

                                writer.write("" +
                                        year + "\t" + // print the year
                                        tempHashMap.get(year).get(0) + "\t\t\t\t" + // print total transactions for that year
                                        tempHashMap.get(year).get(1) + "\t\t\t\t\t\t" + // print total fraud transactions for that year
                                        adjFraudPercentage
                                );
                                writer.newLine();
                            }

                            writer.close();

                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }

                        log.info("------------------------------------------------------------------");
                        log.info(stepExecution.getSummary());
                        log.info("------------------------------------------------------------------");

                        fraudByYearProcessor.clearAllTrackersAndCounters();

                        return StepExecutionListener.super.afterStep(stepExecution);
                    }
                })
                .taskExecutor(asyncTaskExecutor)
                .build();
    }

    // Job - fraud by year
    @Bean
    public Job job_exportFraudByYear() {

        return new JobBuilder("exportFraudByYearJob", jobRepository)
                .start(step_exportFraudByYear())
                .build();
    }
}
