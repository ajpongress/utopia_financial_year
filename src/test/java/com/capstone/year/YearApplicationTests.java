package com.capstone.year;

import com.capstone.year.Models.YearModel;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
class YearApplicationTests {

    @Test
    void contextLoads() {
    }

    // ----------------------------------------------------------------------------------
    // --                             MODEL TESTING                                    --
    // ----------------------------------------------------------------------------------

    @Test
    public void creates_transaction_id_1_userid_99_cardid_88_year_2023_month_3_day_7_time_1130_amount_101_11_type_swipe_transaction_merchantid_777777777_city_Chicago_state_IL_zip_60602_merchantcode_5555_error_no_fraud_no () throws ClassNotFoundException {

        YearModel transaction = new YearModel();
        transaction.setId(1);
        transaction.setUserID(99);
        transaction.setCardID(88);
        transaction.setTransactionYear("2023");
        transaction.setTransactionMonth("3");
        transaction.setTransactionDay("7");
        transaction.setTransactionTime("11:30");
        transaction.setTransactionAmount("$101.11");
        transaction.setTransactionType("Swipe Transaction");
        transaction.setMerchantID(777777777);
        transaction.setTransactionCity("Chicago");
        transaction.setTransactionState("IL");
        transaction.setTransactionZip("60602");
        transaction.setMerchantCatCode(5555);
        transaction.setTransactionErrorCheck("Yes");
        transaction.setTransactionFraudCheck("No");

        assertEquals(1, transaction.getId());
        assertEquals(99, transaction.getUserID());
        assertEquals(88, transaction.getCardID());
        assertEquals("2023", transaction.getTransactionYear());
        assertEquals("3", transaction.getTransactionMonth());
        assertEquals("7", transaction.getTransactionDay());
        assertEquals("11:30", transaction.getTransactionTime());
        assertEquals("$101.11", transaction.getTransactionAmount());
        assertEquals("Swipe Transaction", transaction.getTransactionType());
        assertEquals(777777777, transaction.getMerchantID());
        assertEquals("Chicago", transaction.getTransactionCity());
        assertEquals("IL", transaction.getTransactionState());
        assertEquals("60602", transaction.getTransactionZip());
        assertEquals(5555, transaction.getMerchantCatCode());
        assertEquals("Yes", transaction.getTransactionErrorCheck());
        assertEquals("No", transaction.getTransactionFraudCheck());
    }
}
