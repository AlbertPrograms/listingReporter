package com.AlbertPrograms.listingReporter;

import java.util.List;

public class Report {
  private String date_of_report;
  private String currency = "EUR";
  private ReportUnit all_listings;
  private List<ReportOfMonth> months;

  public Report(String date_of_report, ReportUnit total, List<ReportOfMonth> months) {
    this.date_of_report = date_of_report;
    this.all_listings = total;
    this.months = months;
  }
}
