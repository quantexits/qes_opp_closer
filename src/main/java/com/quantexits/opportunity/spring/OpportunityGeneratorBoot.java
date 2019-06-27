package com.quantexits.opportunity.spring;

import com.quantexits.dao.model.Symbol;
import com.quantexits.opportunity.OpportunityRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import java.util.*;
/**
 * Generates and sends notifications for stock orders that should be placed on the current day.
 */
@SpringBootApplication
public class OpportunityGeneratorBoot implements CommandLineRunner {

    @Autowired
    private OpportunityRunner opportunityRunner;

    /**
     * Main method.
     *
     * @param args Command line args.
     * @throws Exception Failed to generate orders.
     */
    public static void main(final String[] args) throws Exception {
        SpringApplication.run(OpportunityGeneratorBoot.class, args);
    }

    /**
     * Generates both entry and exit orders.
     *
     * @param args Ignored command line args.
     */
    @Override
    public void run(final String... args) throws Exception {
        if (args.length != 2) {
            System.out.println("Error: You must provide two numbers, a start day number and an end day number");
        }
        else {
            try {
                final int start_day = Integer.parseInt(args[0]);
                final int end_day = Integer.parseInt(args[1]);
                System.out.println("Start Day: " + start_day + " End Day: " + end_day);
                opportunityRunner.run(start_day, end_day);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
