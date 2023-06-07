package com.santander.pricehandler.model;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@EqualsAndHashCode
public class Price {

    @Getter
    private final int id;
    @Getter
    private final String instrument;
    @Getter
    @Setter
    private BigDecimal bid;
    @Getter
    @Setter
    private BigDecimal ask;
    @Getter
    @EqualsAndHashCode.Exclude
    private final LocalDateTime timestamp;

    public Price(int id, String instrument, BigDecimal bid, BigDecimal ask, LocalDateTime timestamp) {
        this.id = id;
        this.instrument = instrument;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "Price{" +
                "id=" + id +
                ", instrument='" + instrument + '\'' +
                ", bid=" + bid +
                ", ask=" + ask +
                ", timestamp=" + timestamp +
                '}';
    }
}
