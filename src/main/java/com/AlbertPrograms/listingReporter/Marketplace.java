package com.AlbertPrograms.listingReporter;

public class Marketplace {
  private int id;
  private String marketplace_name;

  public Marketplace(int id, String marketplace_name) {
    this.id = id;
    this.marketplace_name = marketplace_name;
  }

  public int getId() {
    return id;
  }

  public String getMarketplace_name() {
    return marketplace_name;
  }
}
