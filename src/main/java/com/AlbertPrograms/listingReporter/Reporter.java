package com.AlbertPrograms.listingReporter;

import com.google.gson.Gson;
import hirondelle.date4j.DateTime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

final class Reporter {
  private List<Listing> listings;
  private int ebayId;
  private int amazonId;

  Reporter(Currencies currencies, Marketplaces marketplaces, Listings listings) {
    getMarketplaceIds(marketplaces);
    cacheListings(currencies, listings);
  }

  boolean saveReport() {
    Report report = createReport();

    Gson gson = new Gson();
    String reportJson = gson.toJson(report);
    InputStream reportJsonIs = new ByteArrayInputStream(reportJson.getBytes());

    String timeStamp = DateTime.now(TimeZone.getDefault())
      .format("YYYY-MM-DD_hh.mm.ss");

    return FTPConnection.uploadFile("report_" + timeStamp + ".json", reportJsonIs);
  }

  // Extract Ebay and Amazon ids from marketplaces
  private void getMarketplaceIds(Marketplaces marketplaces) {
    List<Marketplace> marketplaceList = marketplaces.getItems();

    ebayId = marketplaceList.stream()
      .filter(marketplace -> marketplace.marketplace_name.equalsIgnoreCase("ebay"))
      .collect(Collectors.toList())
      .get(0)
      .id;

    amazonId = marketplaceList.stream()
      .filter(marketplace -> marketplace.marketplace_name.equalsIgnoreCase("amazon"))
      .collect(Collectors.toList())
      .get(0)
      .id;
  }

  // Cache listings locally with the prices exchanged to EUR
  private void cacheListings(Currencies currencies, Listings listings) {
    List<Currency> currencyList = currencies.getItems();

    // Map all the listings to the currency of EUR
    this.listings = listings.getItems().stream()
      .map(listing -> {
        // Retrieve the value compared to EUR of the currency used in the listing
        // This will always find the correct value since currencies in listings
        // are validated against the currency collection
        double currencyFactor = currencyList.stream()
          .filter(
            currency -> currency.currency_name.equalsIgnoreCase(listing.currency)
          )
          .collect(Collectors.toList())
          .get(0)
          .currency_value;

        // Copy the listing so we don't modify the original
        Listing currencyNormalizedListing = new Listing(listing);
        // Exchange to EUR then round value to 2 decimals
        currencyNormalizedListing.listing_price =
          Math.round(listing.listing_price / currencyFactor * 100.0) / 100.0;
        currencyNormalizedListing.currency = "EUR";

        return currencyNormalizedListing;
      })
      .collect(Collectors.toList());
  }

  // Total report (from all time)
  Report createReport() {
    ReportUnit total = createIntervalReport(new Date(0), new Date());
    List<ReportMonth> months = new ArrayList<>();

    TimeZone timeZone = TimeZone.getDefault();

    long earliestListingDate = listings.stream()
      .mapToLong(listing -> listing.upload_time.getTime())
      // go back 3 years if can't find minimum
      .min().orElse(new Date().getTime() - 94694400000L);

    DateTime dt = DateTime.forInstant(earliestListingDate, timeZone);
    for (
      dt = dt.getStartOfMonth();
      dt.lteq(DateTime.now(timeZone));
      dt = dt.plusDays(32).getStartOfMonth()
    ) {
      // yyyy-MM format
      String month = dt.getYear() + "-" + String.format("%02d", dt.getMonth());
      ReportUnit report = createIntervalReport(
        new Date(dt.getMilliseconds(timeZone)),
        new Date(dt.getEndOfMonth().getMilliseconds(timeZone))
      );

      ReportMonth reportMonth = new ReportMonth(month, report);

      months.add(reportMonth);
    }

    String currentDate = DateTime.now(TimeZone.getDefault())
      .format("YYYY-MM-DD hh:mm:ss");

    return new Report(currentDate, total, months);
  }

  // Finds the lister with the most items for sale
  private String findBestListerMail(List<Listing> listings) {
    List<String> listerMails = listings.stream()
      .map(listing -> listing.owner_email_address)
      .distinct()
      .collect(Collectors.toList());

    // Create a hash map for lister emails with all_listings item counts
    HashMap<String, Integer> listerMailMap = new HashMap<>();
    for (String listerMail : listerMails) {
      int listingAmount = listings.stream()
        .filter(listing -> listing.owner_email_address.equals(listerMail))
        .mapToInt(listing -> listing.quantity)
        .sum();
      listerMailMap.put(listerMail, listingAmount);
    }

    try {
      return Collections.max(listerMailMap.entrySet(), HashMap.Entry.comparingByValue()).getKey();
    } catch (NoSuchElementException e) {
      // Skip printing stack trace and return N/A - this error occurrs when
      // a) there are no elements b) all values are equal
      // The former can happen regularly, the latter is an edge case
      return "N/A";
    }
  }

  // Creates a report for listings uploaded between the parameter dates
  private ReportUnit createIntervalReport(Date from, Date to) {
    // Filter the listings to the time period we're creating the report of
    List<Listing> intervalListings = listings.stream()
      .filter(listing ->
        (listing.upload_time.after(from) || listing.upload_time.equals(from)) &&
          (listing.upload_time.before(to) || listing.upload_time.equals(to))
      )
      .collect(Collectors.toList());
    // Filter Ebay listings
    List<Listing> ebayListings = intervalListings.stream()
      .filter(listing -> listing.marketplace == ebayId)
      .collect(Collectors.toList());
    // Filter Amazon listings
    List<Listing> amazonListings = intervalListings.stream()
      .filter(listing -> listing.marketplace == amazonId)
      .collect(Collectors.toList());

    ReportUnit reportUnit = new ReportUnit();

    // Total listing counts
    reportUnit.total_listing_count = intervalListings.size();
    reportUnit.total_ebay_listing_count = ebayListings.size();
    reportUnit.total_amazon_listing_count = amazonListings.size();

    // Total listing prices
    reportUnit.total_ebay_listing_price = ebayListings.stream()
      .mapToDouble(listing -> listing.listing_price)
      .sum();
    reportUnit.total_amazon_listing_price = amazonListings.stream()
      .mapToDouble(listing -> listing.listing_price)
      .sum();

    // Average listing prices
    reportUnit.average_ebay_listing_price = ebayListings.stream()
      .mapToDouble(listing -> listing.listing_price)
      .average().orElse(0.0); // Return 0 if no elements
    reportUnit.average_amazon_listing_price = amazonListings.stream()
      .mapToDouble(listing -> listing.listing_price)
      .average().orElse(0.0); // Return 0 if no elements

    reportUnit.roundValues();

    reportUnit.best_lister_email = findBestListerMail(intervalListings);

    return reportUnit;
  }
}

class Report {
  String date_of_report;
  String currency = "EUR";
  ReportUnit all_listings;
  List<ReportMonth> months;

  Report(String date_of_report, ReportUnit total, List<ReportMonth> months) {
    this.date_of_report = date_of_report;
    this.all_listings = total;
    this.months = months;
  }
}

class ReportMonth {
  String month;
  ReportUnit report;

  ReportMonth(String month, ReportUnit report) {
    this.month = month;
    this.report = report;
  }
}

class ReportUnit {
  long total_listing_count;
  long total_ebay_listing_count;
  double total_ebay_listing_price;
  double average_ebay_listing_price;
  long total_amazon_listing_count;
  double total_amazon_listing_price;
  double average_amazon_listing_price;
  String best_lister_email;

  public String toString() {
    return "" + total_listing_count + '\n' + total_ebay_listing_count + '\n' + total_ebay_listing_price + '\n' + average_ebay_listing_price + '\n' + total_amazon_listing_count + '\n' + total_amazon_listing_price + '\n' + average_amazon_listing_price + '\n' + best_lister_email;
  }

  // Round the double type values to 2 decimal places
  void roundValues() {
    total_ebay_listing_price = (double) Math.round(total_ebay_listing_price * 100) / 100;
    average_ebay_listing_price = (double) Math.round(average_ebay_listing_price * 100) / 100;
    total_amazon_listing_price = (double) Math.round(total_amazon_listing_price * 100) / 100;
    average_amazon_listing_price = (double) Math.round(average_amazon_listing_price * 100) / 100;
  }
}