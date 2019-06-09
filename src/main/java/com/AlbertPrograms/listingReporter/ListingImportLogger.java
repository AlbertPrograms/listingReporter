package com.AlbertPrograms.listingReporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

// Logs import errors for Listings
class ListingImportLogger {
  private StringBuilder sb;
  private String filename;

  ListingImportLogger(String filename) {
    this.filename = filename;
    sb = new StringBuilder();
  }

  void startErrorLog() {
    try {
      // Create the importLog file if it doesn't yet exist
      File logFile = new File(filename);
      // This will only create it if it doesn't exist
      if (logFile.createNewFile()) {
        // Create the CSV header
        PrintWriter writer = new PrintWriter(logFile);
        writer.println("ListingId;MarketplaceName;InvalidField");
        writer.close();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void closeErrorLog() {
    if (sb.length() == 0) return;

    try {
      // Open writer in append mode
      PrintWriter writer = new PrintWriter(
        new FileOutputStream(new File(filename), true) // true for append on
      );
      writer.append(sb.toString());
      writer.close();
      System.out.println("Listing import errors logged to 'importLog.csv'");
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  void logErrorToCSV(String line) {
    sb.append(line);
    sb.append("\r\n");
  }
}
