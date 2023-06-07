package com.santander.pricehandler.model;

import java.math.BigDecimal;
import java.math.MathContext;

public class MarginCalculator {
    private static final MathContext mathContext = new MathContext(4);

    public BigDecimal adjustBidByMargin(BigDecimal currentBid, BigDecimal bidMargin) {
        return currentBid.add( (currentBid.multiply( bidMargin, mathContext)));
    }

    public BigDecimal adjustAskByMargin(BigDecimal curentAsk, BigDecimal askMargin) {
        return curentAsk.add (curentAsk.multiply(askMargin, mathContext) ) ;
    }
}
