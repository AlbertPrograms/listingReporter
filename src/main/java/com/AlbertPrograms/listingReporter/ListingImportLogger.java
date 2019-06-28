package com.AlbertPrograms.listingReporter;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;

// Logs import errors for Listings

/**
 * Simple logger class that
 *
 * @author Albert Kelemen
 */
public class ListingImportLogger {
  private StringBuilder sb;
  private String filename;

  public ListingImportLogger(String filename) {
    this.filename = filename;
    sb = new StringBuilder();
  }

  public void writeErrorLog() {
    if (sb.length() == 0) {
      return; // No need to log if there are no errors
    }

    // Create the importLog file if it doesn't yet exist
    File logFile = new File(filename);
    boolean printHeader = false;
    try {
      // This will only create it if it doesn't exist
      if (logFile.createNewFile()) {
        printHeader = true;
      }

      try (PrintWriter writer = new PrintWriter(
        new FileOutputStream(new File(filename), true) // true: append mode on
      )) {
        if (printHeader) {
          writer.println("ListingId;MarketplaceName;InvalidField");
        }

        writer.append(sb.toString());
        writer.close();
        System.out.println("Listing import errors logged to 'importLog.csv'");
      }
    } catch (IOException e) {
      System.err.println("Can't access log file (" + filename + ")");
      e.printStackTrace();
    }
  }

  public void addToErrorLog(String line) {
    sb.append(line);
    sb.append("\r\n");
  }
}
