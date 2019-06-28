package com.AlbertPrograms.listingReporter;

public class Currency {
  private String currency_name;
  private double currency_value;

  Currency(String name, double value) {
    this.currency_name = name;
    this.currency_value = value;
  }

  public String getCurrency_name() {
    return currency_name;
  }

  public double getCurrency_value() {
    return currency_value;
  }
}
