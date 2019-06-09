package com.AlbertPrograms.listingReporter;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ListingReporter {
  private static boolean allSynced = true;
  private static List<String> unsynced = new ArrayList<>();

  private static void syncDBItems(DBSyncedItems ...items) {
    System.out.println("Syncing all items");
    for (DBSyncedItems item : items) {
      boolean synced = item.syncData();
      if (!synced) unsynced.add(item.itemName);
      allSynced = allSynced && synced;
    }
    System.out.println("Syncing finished");
  }

  public static void main(String[] args) {
    Currencies currencies = new Currencies();
    Marketplaces marketplaces = new Marketplaces();
    ListingStatuses listingStatuses = new ListingStatuses();
    Locations locations = new Locations();
    Listings listings = new Listings(
      // Create simple lookups for easy listing validation
      currencies.getItems().stream().map(currency -> currency.currency_name).collect(Collectors.toList()),
      locations.getItems().stream().map(location -> location.id).collect(Collectors.toList()),
      marketplaces.getItems().stream().map(marketplace -> marketplace.id).collect(Collectors.toList()),
      listingStatuses.getItems().stream().map(listingStatus -> listingStatus.id).collect(Collectors.toList())
    );

    syncDBItems(currencies, marketplaces, listingStatuses, locations, listings);

    if (!allSynced) {
      System.err.println("Error: not all data is synced:");
      System.err.println(unsynced);
      DBConnection.close();
      return;
    }

    Reporter reporter = new Reporter(currencies, marketplaces, listings);
    if (!reporter.saveReport()) System.err.println("Failed to save report");
    else System.out.println("Saved report to FTP server successfully, exiting");

    DBConnection.close();
  }
}