package com.AlbertPrograms.listingReporter;

public class ReportUnit {
  private long total_listing_count;
  private long total_ebay_listing_count;
  private double total_ebay_listing_price;
  private double average_ebay_listing_price;
  private long total_amazon_listing_count;
  private double total_amazon_listing_price;
  private double average_amazon_listing_price;
  private String best_lister_email;

  /**
   * Constructor that rounds double values to 2 decimals for readability
   */
  public ReportUnit(long total_listing_count, long total_ebay_listing_count, double total_ebay_listing_price,
                    double average_ebay_listing_price, long total_amazon_listing_count,
                    double total_amazon_listing_price, double average_amazon_listing_price, String best_lister_email) {
    this.total_listing_count = total_listing_count;
    this.total_ebay_listing_count = total_ebay_listing_count;
    this.total_ebay_listing_price = (double) Math.round(total_ebay_listing_price * 100) / 100;
    this.average_ebay_listing_price = (double) Math.round(average_ebay_listing_price * 100) / 100;
    this.total_amazon_listing_count = total_amazon_listing_count;
    this.total_amazon_listing_price = (double) Math.round(total_amazon_listing_price * 100) / 100;
    this.average_amazon_listing_price = (double) Math.round(average_amazon_listing_price * 100) / 100;
    this.best_lister_email = best_lister_email;
  }

  public String toString() {
    return "" + total_listing_count + '\n' + total_ebay_listing_count + '\n' + total_ebay_listing_price + '\n' +
      average_ebay_listing_price + '\n' + total_amazon_listing_count + '\n' + total_amazon_listing_price + '\n' +
      average_amazon_listing_price + '\n' + best_lister_email;
  }
}
