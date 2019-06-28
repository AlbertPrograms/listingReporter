package com.AlbertPrograms.listingReporter;

import hirondelle.date4j.DateTime;

import javax.naming.ConfigurationException;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.TimeZone;

public class ListingReporter {
  public static void main(String[] args) {
    DBManager dbManager = null;
    try {
      PropertiesReader propertiesReader = new PropertiesReader();
      dbManager = new DBManager(
        propertiesReader.getDbUrl(),
        propertiesReader.getDbUsername(),
        propertiesReader.getDbPassword()
      );

      dbManager.connect();
      Currencies currencies = new Currencies(dbManager);
      Marketplaces marketplaces = new Marketplaces(dbManager);
      ListingStatuses listingStatuses = new ListingStatuses(dbManager);
      Locations locations = new Locations(dbManager);
      Listings listings = new Listings(
        dbManager,
        // Create simple lookups for easy listing validation
        currencies.getLookupList(),
        locations.getLookupList(),
        marketplaces.getLookupList(),
        listingStatuses.getLookupList()
      );
      dbManager.close();

      Reporter reporter = new Reporter(currencies, marketplaces, listings);
      InputStream reportJsonIs = reporter.getReportAsInputStream();
      FTPUploader ftpUploader = new FTPUploader(
        propertiesReader.getFtpUrl(),
        propertiesReader.getFtpUsername(),
        propertiesReader.getFtpPassword()
      );

      ftpUploader.uploadReport(reportJsonIs);
      System.out.println("Saved report to FTP server successfully, exiting");
    } catch (ConfigurationException | ClassNotFoundException | IOException | SQLException e) {
      e.printStackTrace();
    } finally {
      try {
        if (dbManager != null && dbManager.isOpen()) {
          dbManager.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}