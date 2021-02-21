package com.example.stockwatch;

import java.io.Serializable;

public class Stock implements Serializable {

    private String symbol, name;
    private double change, changePercent, latestPrice;

    public String getSymbol() {
        return symbol;
    }

    public void setSymbol(String symbol) {
        this.symbol = symbol;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getChange() {
        return change;
    }

    public void setChange(double change) {
        this.change = change;
    }

    public double getChangePercent() {
        return changePercent;
    }

    public void setChangePercent(double changePercent) {
        this.changePercent = changePercent;
    }

    public double getLatestPrice() {
        return latestPrice;
    }

    public void setLatestPrice(double latestPrice) {
        this.latestPrice = latestPrice;
    }

    public Stock(String symbol, String name, double change, double changePercent, double latestPrice) {
        this.symbol = symbol;
        this.name = name;
        this.change = change;
        this.changePercent = changePercent;
        this.latestPrice = latestPrice;
    }

    public Stock(String symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }
}
