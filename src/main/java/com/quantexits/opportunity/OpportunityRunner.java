package com.quantexits.opportunity;

import com.quantexits.dao.OpportunityCloseStatusDAO;
import com.quantexits.dao.exceptions.DAOException;
import com.quantexits.dao.model.OpportunityCloseStatus;
import com.quantexits.opportunity.taker.OpportunityTaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Runner to generate and store historical opportunities.
 * To generate daily opportunities use the main git branch.
 */
@Component
public class OpportunityRunner {

    private OpportunityTaker oppTaker;
    private OpportunityCloseStatusDAO oppCloseStatusDAO;

    /**
     * Create a new OpportunityRunner instance.
     *
     * @param oppTaker Opportunity taker.
     * @param oppCloseStatusDAO OpportunityCloseStatusDAO.
     */
    @Autowired
    public OpportunityRunner(
            final OpportunityTaker oppTaker,
            final OpportunityCloseStatusDAO oppCloseStatusDAO) {
        this.oppTaker = oppTaker;
        this.oppCloseStatusDAO = oppCloseStatusDAO;

    }

    /**
     * Run.
     *
     * @throws RunnerException Exception while running.
     */
    public void run(int original_start_day, int original_end_day) throws RunnerException {
        int startDay, endDay, jobArrayIndex = 0;

        System.out.println("**** Starting the Runner *****");

        // first see if we need to check environment variables for start and end days.
        if (original_start_day <= 0 || original_end_day <= 0) {
            final String envStartDay = System.getenv("START_DAY");
            final String envEndDay = System.getenv("END_DAY");
            if(envStartDay != null && !envStartDay.isEmpty()) {
                original_start_day = Integer.parseInt(envStartDay);
            }
            if(envEndDay != null && !envEndDay.isEmpty()) {
                original_end_day = Integer.parseInt(envEndDay);
            }
        }

        // end if we do not have a valid startDate and valid endDate
        if (original_start_day <= 0 || original_end_day <= 0) {
            System.out.println("**** ERROR: Invalid Start or End date. startDate: " + original_start_day +
                    " endDate: " + original_end_day + " *****");
            throw new RunnerException();
        }

        try {
            // See if we are just starting or resuming from an interruption.
            System.out.println("**** Getting OpportunityGenStatus. *****");
            final OpportunityCloseStatus status = oppCloseStatusDAO.getOpportunityCloseStatus(jobArrayIndex);
            System.out.println("**** Done getting OpportunityGenStatus. *****");
            if (!status.isProcessing()) {
                // this is first time the program has been called
                System.out.println("**** Frist time called for this program. *****");
                startDay = original_start_day;
                endDay = original_end_day;
                // record original inputs
                System.out.println("**** Setting Status. *****");
                oppCloseStatusDAO.setOpportunityCloseStatus(
                        new OpportunityCloseStatus(true, startDay, endDay, startDay,0)
                );
                System.out.println("**** Done Setting Status. *****");
            }
            else {
                // the program is resuming after being interrupted.
                System.out.println("**** The program is resuming after being interrupted. *****");
                startDay = status.getCurrentDay();
                endDay = status.getEndDay();
            }
        } catch (final DAOException e) {
            e.printStackTrace();
            throw new RunnerException(e);
        }

        // Find open opportunities and close them for the date range given
        try {

            for (int day = startDay; day <= endDay; day++) {
                oppCloseStatusDAO.updateOpportunityCloseStatus(day, jobArrayIndex);
                oppTaker.handleOpenOpportunities(day);
                System.out.println("Closed: " + day);

            }
            // reset opportunity generation status
            //oppCloseStatusDAO.setOpportunityCloseStatus(
            //        new OpportunityCloseStatus(false,0,0,0, jobArrayIndex)
            //);
        } catch (final DAOException e) {
            throw new RunnerException(e);
        }

    }

}
