package com.AlbertPrograms.listingReporter;

/**
 * Describes what amount of the given currency make up the value of 1 EUR
 * In the format required for serialization of JSON data
 *
 * @author Albert Kelemen
 */
public class CurrencyTable {
  // EUR is the base currency so it's always 1
  private double HUF = 0;
  private double USD = 0;
  private double GBP = 0;
  private double AUD = 0;
  private double JPY = 0;

  /**
   * @param HUF - HUF in 1 EUR
   * @param USD - USD in 1 EUR
   * @param GBP - GBP in 1 EUR
   * @param AUD - AUD in 1 EUR
   * @param JPY - JPY in 1 EUR
   */
  public CurrencyTable(double HUF, double USD, double GBP, double AUD, double JPY) {
    this.HUF = HUF;
    this.USD = USD;
    this.GBP = GBP;
    this.AUD = AUD;
    this.JPY = JPY;
  }

  public double getHUF() {
    return HUF;
  }

  public double getUSD() {
    return USD;
  }

  public double getGBP() {
    return GBP;
  }

  public double getAUD() {
    return AUD;
  }

  public double getJPY() {
    return JPY;
  }

  public String toString() {
    return (
      "HUF: " + HUF + '\n' +
      "USD: " + USD + '\n' +
      "GBP: " + GBP + '\n' +
      "AUD: " + AUD + '\n' +
      "JPY: " + JPY + '\n'
    );
  }
}
