package com.capstone.year.Classifiers;

import com.capstone.year.Models.YearModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.support.SynchronizedItemStreamWriter;
import org.springframework.batch.item.support.builder.SynchronizedItemStreamWriterBuilder;
import org.springframework.batch.item.xml.StaxEventItemWriter;
import org.springframework.batch.item.xml.builder.StaxEventItemWriterBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.classify.Classifier;
import org.springframework.core.io.FileSystemResource;
import org.springframework.oxm.xstream.XStreamMarshaller;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@StepScope
@Component
@Slf4j
public class YearClassifier implements Classifier<YearModel, ItemWriter<? super YearModel>> {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    // Destination path for export file
    @Value("#{jobParameters['outputPath_param']}")
    private String outputPath;

    // Map for mapping each year to its own dedicated ItemWriter (for performance)
    private final Map<String, ItemWriter<? super YearModel>> writerMap;

    // Public constructor
    public YearClassifier() {
        this.writerMap = new HashMap<>();
    }



    // ----------------------------------------------------------------------------------
    // --                                METHODS                                       --
    // ----------------------------------------------------------------------------------

    // Classify method (contains XML writer and synchronized item stream writer)
    @Override
    public ItemWriter<? super YearModel> classify(YearModel transaction) {

        // Set filename to specific year from the Transaction model
        String fileName = transaction.getFileName();

        // Make entire process thead-safe
        synchronized (this) {

            // If year has already been accessed, use the same ItemWriter
            if (writerMap.containsKey(fileName)) {
                return writerMap.get(fileName);
            }
            // Create new ItemWriter for new year
            else {

                // Complete path for file export
                File file = new File(outputPath + "\\" + fileName);

                // XML writer
                XStreamMarshaller marshaller = new XStreamMarshaller();
                marshaller.setAliases(Collections.singletonMap("year", YearModel.class));

                StaxEventItemWriter<YearModel> writerXML = new StaxEventItemWriterBuilder<YearModel>()
                        .name("yearXmlWriter")
                        .resource(new FileSystemResource(file))
                        .marshaller(marshaller)
                        .rootTagName("transactions")
                        .transactional(false) // Keeps XML headers on all output files
                        .build();

                // Make XML writer thread-safe
                SynchronizedItemStreamWriter<YearModel> synchronizedItemStreamWriter =
                        new SynchronizedItemStreamWriterBuilder<YearModel>()
                                .delegate(writerXML)
                                .build();

                writerXML.open(new ExecutionContext());
                writerMap.put(fileName, synchronizedItemStreamWriter); // Pair year to unique ItemWriter
                return synchronizedItemStreamWriter;
            }
        }
    }

    public void closeAllwriters() {

        for (String key : writerMap.keySet()) {

            SynchronizedItemStreamWriter<YearModel> writer = (SynchronizedItemStreamWriter<YearModel>) writerMap.get(key);
            writer.close();
        }
        writerMap.clear();
    }


}
