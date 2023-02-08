package com.capstone.year.Models;

import lombok.Data;

@Data
public class YearModel {

    private long id;
    private long userID; // CSV - User (index 0)
    private long cardID; // CSV - Card (index 1)
    private String transactionYear; // CSV - Year (index 2)
    private String transactionMonth; // CSV - Month (index 3)
    private String transactionDay; // CSV - Day (index 4)
    private String transactionTime; // CSV - Time (index 5)
    private String transactionAmount; // CSV - Amount (index 6)
    private String transactionType; // CSV - Use Chip (index 7)
    private long merchantID; // CSV - Merchant Name (index 8) ----- STRIP NEGATIVE SIGN
    private String transactionCity; // CSV - Merchant City (index 9)
    private String transactionState; // CSV - Merchant State (index 10)
    private String transactionZip; // CSV - Merchant Zip (index 11) ----- STRIP FRACTIONAL PART
    private long merchantCatCode; // CSV - MCC (index 12)
    private String transactionErrorCheck; // CSV - Errors? (index 13)
    private String transactionFraudCheck; // CSV - Is Fraud? (index 14)

    public String getFileName() {
        return "year_" + transactionYear + "_transactions.xml";
    }
}
