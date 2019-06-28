package com.AlbertPrograms.listingReporter;

import com.google.gson.Gson;
import hirondelle.date4j.DateTime;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

public final class Reporter {
  private List<Listing> listings;
  private int ebayId;
  private int amazonId;

  public Reporter(Currencies currencies, Marketplaces marketplaces, Listings listings) {
    getMarketplaceIds(marketplaces);
    cacheListings(currencies, listings);
  }

  public InputStream getReportAsInputStream() {
    Report report = createReport();

    Gson gson = new Gson();
    String reportJson = gson.toJson(report);
    return new ByteArrayInputStream(reportJson.getBytes());
  }

  // Extract Ebay and Amazon ids from marketplaces
  private void getMarketplaceIds(Marketplaces marketplaces) {
    List<Marketplace> marketplaceList = marketplaces.getItems();

    ebayId = marketplaceList.stream()
      .filter(marketplace -> marketplace.getMarketplace_name().equalsIgnoreCase("ebay"))
      .collect(Collectors.toList())
      .get(0)
      .getId();

    amazonId = marketplaceList.stream()
      .filter(marketplace -> marketplace.getMarketplace_name().equalsIgnoreCase("amazon"))
      .collect(Collectors.toList())
      .get(0)
      .getId();
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
            currency -> currency.getCurrency_name().equalsIgnoreCase(listing.getCurrency())
          )
          .collect(Collectors.toList())
          .get(0)
          .getCurrency_value();

        // Copy the listing so we don't modify the original
        Listing currencyNormalizedListing = new Listing(listing);
        // Exchange to EUR then round value to 2 decimals
        currencyNormalizedListing.setEurCurrency(
          Math.round(listing.getListing_price() / currencyFactor * 100.0) / 100.0
        );

        return currencyNormalizedListing;
      })
      .collect(Collectors.toList());
  }

  // Total report (from all time)
  private Report createReport() {
    ReportUnit total = createIntervalReport(new Date(0), new Date());
    List<ReportOfMonth> months = new ArrayList<>();

    TimeZone timeZone = TimeZone.getDefault();

    long earliestListingDate = listings.stream()
      .mapToLong(listing -> listing.getUpload_time().getTime())
      // go back 3 years if can't find minimum
      .min().orElse(new Date().getTime() - 3 * 365 * 24 * 3600);

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

      ReportOfMonth reportOfMonth = new ReportOfMonth(month, report);

      months.add(reportOfMonth);
    }

    String currentDate = DateTime.now(TimeZone.getDefault())
      .format("YYYY-MM-DD hh:mm:ss");

    return new Report(currentDate, total, months);
  }

  // Finds the lister with the most items for sale
  private String findBestListerMail(List<Listing> listings) {
    List<String> listerMails = listings.stream()
      .map(Listing::getOwner_email_address)
      .distinct()
      .collect(Collectors.toList());

    // Create a hash map for lister emails with all_listings item counts
    HashMap<String, Integer> listerMailMap = new HashMap<>();
    for (String listerMail : listerMails) {
      int listingAmount = listings.stream()
        .filter(listing -> listing.getOwner_email_address().equals(listerMail))
        .mapToInt(Listing::getQuantity)
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
        (listing.getUpload_time().after(from) || listing.getUpload_time().equals(from)) &&
          (listing.getUpload_time().before(to) || listing.getUpload_time().equals(to))
      )
      .collect(Collectors.toList());
    // Filter Ebay listings
    List<Listing> ebayListings = intervalListings.stream()
      .filter(listing -> listing.getMarketplace() == ebayId)
      .collect(Collectors.toList());
    // Filter Amazon listings
    List<Listing> amazonListings = intervalListings.stream()
      .filter(listing -> listing.getMarketplace() == amazonId)
      .collect(Collectors.toList());

    // Total listing counts
    int total_listing_count = intervalListings.size();
    int total_ebay_listing_count = ebayListings.size();
    int total_amazon_listing_count = amazonListings.size();

    // Total listing prices
    double total_ebay_listing_price = ebayListings.stream()
      .mapToDouble(Listing::getListing_price)
      .sum();
    double total_amazon_listing_price = amazonListings.stream()
      .mapToDouble(Listing::getListing_price)
      .sum();

    // Average listing prices
    double average_ebay_listing_price = ebayListings.stream()
      .mapToDouble(Listing::getListing_price)
      .average().orElse(0.0); // Return 0 if no elements
    double average_amazon_listing_price = amazonListings.stream()
      .mapToDouble(Listing::getListing_price)
      .average().orElse(0.0); // Return 0 if no elements

    return new ReportUnit(total_listing_count,
      total_ebay_listing_count,
      total_ebay_listing_price,
      average_ebay_listing_price,
      total_amazon_listing_count,
      total_amazon_listing_price,
      average_amazon_listing_price,
      findBestListerMail(intervalListings));
  }
}
