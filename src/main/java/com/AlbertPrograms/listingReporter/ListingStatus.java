package com.AlbertPrograms.listingReporter;

public class ListingStatus {
  private int id;
  private String status_name;

  public ListingStatus(int id, String status_name) {
    this.id = id;
    this.status_name = status_name;
  }

  public int getId() {
    return id;
  }

  public String getStatus_name() {
    return status_name;
  }
}
