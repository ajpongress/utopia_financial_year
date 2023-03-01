package com.capstone.year.Controllers;

import com.capstone.year.Services.YearService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class YearController {

    // ----------------------------------------------------------------------------------
    // --                                  SETUP                                       --
    // ----------------------------------------------------------------------------------

    private static String reportsPath;

    public static String getReportsPath() {
        return reportsPath;
    }


    @Autowired
    YearService yearService;



    // ----------------------------------------------------------------------------------
    // --                               MAPPINGS                                       --
    // ----------------------------------------------------------------------------------

    // all years
    @GetMapping("/years")
    public ResponseEntity<String> allYearsAPI(@RequestParam String source, @RequestParam String destination) {

        return yearService.exportAllYears(source, destination);
    }

    // specific year
    @GetMapping("/years/{year}")
    public ResponseEntity<String> oneYearAPI(@PathVariable String year, @RequestParam String source, @RequestParam String destination) {

        return yearService.exportSingleYear(year, source, destination);
    }

    // fraud by year
    @GetMapping("/getfraudbyyear")
    public ResponseEntity<String> fraudByYearAPI(@RequestParam String source, @RequestParam String destination, @RequestParam String reports_destination) {

        reportsPath = reports_destination;
        return yearService.exportFraudByYear(source, destination, reports_destination);
    }
}
