package com.quantexits.opportunity;

import com.quantexits.dao.OpportunityDAO;
import com.quantexits.dao.OpportunityCloseStatusDAO;
import com.quantexits.dao.TradingDayDAO;
import com.quantexits.dao.ScreenDAO;
import com.quantexits.dao.exceptions.DAOException;
import com.quantexits.dao.model.Opportunity;
import com.quantexits.dao.model.OpportunityCloseStatus;
import com.quantexits.dao.model.Symbol;
import com.quantexits.opportunity.taker.OpportunityTaker;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
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
    public void run(final int original_start_day, final int original_end_day) throws RunnerException {
        int startDay, endDay, stepSize, jobArrayIndex = 0, numNodes = 1;
        boolean isJobArray = false;

        // see if we are running in an AWS Batch job array or not
        final String jobArrayIndexString = System.getenv("AWS_BATCH_JOB_ARRAY_INDEX");
        final String numNodesString = System.getenv("AWS_BATCH_JOB_NUM_NODES");
        if(jobArrayIndexString != null && !jobArrayIndexString.isEmpty()) {
            jobArrayIndex = Integer.parseInt(jobArrayIndexString);
            numNodes = Integer.parseInt(numNodesString);
            isJobArray = true;

        }

        try {
            // See if we are just starting or resuming from an interruption.
            final OpportunityCloseStatus status = oppCloseStatusDAO.getOpportunityCloseStatus(jobArrayIndex);
            if (!status.isProcessing()) {
                // this is first time the program has been called
                if (isJobArray) {
                    // divide up the work and take our portion
                    stepSize = (original_end_day - original_start_day + 1) / numNodes;
                    startDay = original_start_day + jobArrayIndex * stepSize;
                    if (jobArrayIndex != (numNodes - 1)) {
                        endDay = original_start_day + (jobArrayIndex + 1) * stepSize - 1;
                    } else {
                        // we are last node so just take what's left
                        endDay = original_end_day;
                    }

                } else {
                    startDay = original_start_day;
                    endDay = original_end_day;
                }
                // record original inputs
                oppCloseStatusDAO.setOpportunityCloseStatus(
                        new OpportunityCloseStatus(true, startDay, endDay, startDay,0)
                );
            }
            else {
                // the program is resuming after being interrupted.
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
            oppCloseStatusDAO.setOpportunityCloseStatus(
                    new OpportunityCloseStatus(false,0,0,0, jobArrayIndex)
            );
        } catch (final DAOException e) {
            throw new RunnerException(e);
        }

    }

}
