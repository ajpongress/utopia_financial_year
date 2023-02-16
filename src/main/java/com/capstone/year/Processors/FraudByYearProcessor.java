package com.capstone.year.Processors;

import com.capstone.year.Models.YearModel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;

@StepScope
@Component
@Slf4j
public class FraudByYearProcessor implements ItemProcessor<YearModel, YearModel> {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    // Track year (String) as the primary key
    // Includes a counter (long) for total transactions for that year (index 0 of array)
    // Includes a counter (long) for fraud transactions for that year (index 1 of array)
    private HashMap<String, ArrayList<Long>> transactionAndFraudTracker = new HashMap<>();

    // Keeps track of total transactions per year
    private static long tempTotalCounter = 0L;

    // Keeps track of fraud transactions per year
    private static long tempFraudCounter = 0L;

    // Used as temporary value to set model transaction ID
    private static long transactionIdCounter = 0L;

    public void clearAllTrackersAndCounters() {
        transactionAndFraudTracker.clear();
        tempTotalCounter = 0L;
        tempFraudCounter = 0L;
        transactionIdCounter = 0L;
    }



    // ----------------------------------------------------------------------------------
    // --                                METHODS                                       --
    // ----------------------------------------------------------------------------------

    public YearModel process(YearModel transaction) {

        synchronized (this) {

            // Year hasn't been accessed yet and isn't in HashMap
            if (!transactionAndFraudTracker.containsKey(transaction.getTransactionYear())) {

                // Add year to HashMap
                // Set index 0 of array (total year transactions) to 1
                // Set index 1 of array (total fraud transactions for year) to 0
                transactionAndFraudTracker.put(transaction.getTransactionYear(), new ArrayList<>());
                transactionAndFraudTracker.get(transaction.getTransactionYear()).add(1L); // index 0
                transactionAndFraudTracker.get(transaction.getTransactionYear()).add(0L); // index 1
            }

            // Year has already been accessed and is in the HashMap
            else {

                // Increment transaction tempTotalCounter in HashMap array (index 0)
                tempTotalCounter = transactionAndFraudTracker.get(transaction.getTransactionYear()).get(0);
                tempTotalCounter++;
                transactionAndFraudTracker.get(transaction.getTransactionYear()).set(0, tempTotalCounter);
            }

            // Check fraud field of model
            if (transaction.getTransactionFraudCheck().equals("Yes")) {

                // Get fraud tracker value from array
                tempFraudCounter = transactionAndFraudTracker.get(transaction.getTransactionYear()).get(1);
                tempFraudCounter++; // Increment by 1
                // Set fraud tracker value back to array
                transactionAndFraudTracker.get(transaction.getTransactionYear()).set(1, tempFraudCounter);

                // Strip negative sign from MerchantID
                long temp_MerchantID = Math.abs(transaction.getMerchantID());
                transaction.setMerchantID(temp_MerchantID);

                // Strip fractional part of TransactionZip if greater than 5 characters
                if (transaction.getTransactionZip().length() > 5) {
                    String[] temp_TransactionZip = transaction.getTransactionZip().split("\\.", 0);
                    transaction.setTransactionZip(temp_TransactionZip[0]);
                }

                // Set model transaction ID (unique to year) and return transaction to writer
                transactionIdCounter = transactionAndFraudTracker.get(transaction.getTransactionYear()).get(1);
                transaction.setId(transactionIdCounter);
                log.info(transaction.toString());
                return transaction;
            }

            else {return null;} // transaction isn't fraud. Don't return

        }

    }

    // Return HashMap for use in Listener (for report generation)
    public HashMap<String, ArrayList<Long>> returnHashMap() {
        return transactionAndFraudTracker;
    }

}
