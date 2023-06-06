package com.santander.pricehandler.model;

import java.time.LocalDateTime;

public class Price {
    private int id;
    private String instrument;
    private double bid;
    private double ask;
    private LocalDateTime timestamp;

    public Price(int id, String instrument, double bid, double ask, LocalDateTime timestamp) {
        this.id = id;
        this.instrument = instrument;
        this.bid = bid;
        this.ask = ask;
        this.timestamp = timestamp;
    }

    public void adjustByMargin(double bidMargin, double askMargin) {
        bid += (bid * bidMargin);
        ask += (ask * askMargin);
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getInstrument() {
        return instrument;
    }

    public void setInstrument(String instrument) {
        this.instrument = instrument;
    }

    public double getBid() {
        return bid;
    }

    public void setBid(double bid) {
        this.bid = bid;
    }

    public double getAsk() {
        return ask;
    }

    public void setAsk(double ask) {
        this.ask = ask;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
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
