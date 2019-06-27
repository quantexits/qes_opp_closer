package com.quantexits.opportunity.taker;

import com.quantexits.dao.EodDAO;
import com.quantexits.dao.OpportunityDAO;
import com.quantexits.dao.exceptions.DAOException;
import com.quantexits.dao.model.OpportunityTaken;
import com.quantexits.dao.model.Trade;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Component
public class OpportunityTaker {
    private EodDAO eodDAO;
    private OpportunityDAO opportunityDAO;

    /**
     *
     * @param eodDAO stuff.
     * @param opportunityDAO stuff.
     */
    @Autowired
    public OpportunityTaker(final EodDAO eodDAO, final OpportunityDAO opportunityDAO) {
        this.eodDAO = eodDAO;
        this.opportunityDAO = opportunityDAO;
    }

    /**
     *
     * @param tradingDay stuff.
     * @throws DAOException stuff.
     */
    public void handleOpenOpportunities(final int tradingDay) throws DAOException {
        final List<OpportunityTaken> openOpps = opportunityDAO.getOpenOpportunitesTaken(tradingDay);
        final Map<Integer, Trade> toClose = new HashMap<>();

        for (final OpportunityTaken opp : openOpps) {
            if (shouldClose(opp, tradingDay)) {
                final Trade trade = eodDAO.getTrade(opp.getSymbolId(), opp.getDirection(),
                        opp.getHoldPeriod(), opp.getEntryTradingDay(), tradingDay);
                if (trade != null) {
                    toClose.put(opp.getOpportunityId(), trade);
                }
            }
        }

        opportunityDAO.closeOpportunitiesTaken(toClose, tradingDay);
    }

    private boolean shouldClose(final OpportunityTaken opp, final int tradingDay) throws DAOException {
        if (opp.getHoldPeriod().getIntValue() == -1) {
            return shouldCloseFirstProfit(opp, tradingDay);
        } else {
            return (opp.getEntryTradingDay() + opp.getHoldPeriod().getIntValue() == tradingDay);
        }
    }

    private boolean shouldCloseFirstProfit(final OpportunityTaken opp, final int tradingDay) throws DAOException {
        if (opp.getEntryTradingDay() + 5 == tradingDay) {
            return true;
        } else {
            final Trade trade = eodDAO.getTrade(opp.getSymbolId(), opp.getDirection(),
                    opp.getHoldPeriod(), opp.getEntryTradingDay(), tradingDay);
            return trade != null && trade.isWinner();
        }
    }
}
