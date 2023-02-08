package com.capstone.year.Writers;

import com.capstone.year.Classifiers.YearClassifier;
import com.capstone.year.Models.YearModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.support.ClassifierCompositeItemWriter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class YearCompositeWriter {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    @Autowired
    YearClassifier yearClassifier;



    // ----------------------------------------------------------------------------------
    // --                                METHODS                                       --
    // ----------------------------------------------------------------------------------

    @Bean("writer_Year")
    public ClassifierCompositeItemWriter<YearModel> classifierCompositeItemWriter() {

        ClassifierCompositeItemWriter<YearModel> writer = new ClassifierCompositeItemWriter<>();
        writer.setClassifier(yearClassifier);

        return writer;
    }
}