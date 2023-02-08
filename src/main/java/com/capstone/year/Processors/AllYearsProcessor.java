package com.capstone.year.Processors;

import com.capstone.year.Models.YearModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@StepScope
@Component
@Slf4j
public class AllYearsProcessor implements ItemProcessor<YearModel, YearModel> {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    private static long transactionIdCounter = 0;

    // ----------------------------------------------------------------------------------
    // --                                METHODS                                       --
    // ----------------------------------------------------------------------------------

    public YearModel process(YearModel transaction) {

        synchronized (this) {

            // Strip negative sign from MerchantID
            long temp_MerchantID = Math.abs(transaction.getMerchantID());
            transaction.setMerchantID(temp_MerchantID);

            // Strip fractional part of TransactionZip if greater than 5 characters
            if (transaction.getTransactionZip().length() > 5) {
                String[] temp_TransactionZip = transaction.getTransactionZip().split("\\.", 0);
                transaction.setTransactionZip(temp_TransactionZip[0]);
            }

            // Print processed transaction and return
            transactionIdCounter++;
            transaction.setId(transactionIdCounter);
            log.info(transaction.toString());
            return transaction;

        }


    }
}
