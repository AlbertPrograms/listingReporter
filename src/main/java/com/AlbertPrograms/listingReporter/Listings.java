package com.AlbertPrograms.listingReporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import java.util.UUID;

class Listings extends DBSyncedItems<Listing> {
  ListingImportLogger logger = new ListingImportLogger("importLog.csv");
  List<String> currencyLookup;
  List<UUID> locationLookup;
  List<Integer> marketplaceLookup;
  List<Integer> listingStatusLookup;
  Listings(List<String> currencyLookup, List<UUID> locationLookup, List<Integer> marketplaceLookup, List<Integer> listingStatusLookup) {
    this.currencyLookup = currencyLookup;
    this.locationLookup = locationLookup;
    this.marketplaceLookup = marketplaceLookup;
    this.listingStatusLookup = listingStatusLookup;
  }

  protected void initClassSpecifics() {
    itemName = "listing";
    itemClass = Listing.class;
    itemArrayClass = Listing[].class;
    insertFields = "(id, title, description, location_id, listing_price, currency, quantity, " +
      "listing_status, marketplace, upload_time, owner_email_address)";
    insertValues = "VALUES(CAST(? AS UUID), ?, ?, CAST(? AS UUID), CAST(? AS DOUBLE PRECISION), " +
      "?, CAST(? AS INT), CAST(? AS INT), CAST(? AS INT), CAST(? AS DATE), ?)";

    changing = true; // Listings change every time
    canUseFallback = false; // They're not only different but also new, so no DB fallback allowed
  }

  protected Listing readResult(ResultSet rs) throws SQLException {
    Listing listing = new Listing();
    listing.id = UUID.fromString(rs.getString("id"));
    listing.title = rs.getString("title");
    listing.description = rs.getString("description");
    listing.location_id = UUID.fromString(rs.getString("location_id"));
    listing.listing_price = rs.getDouble("listing_price");
    listing.currency = rs.getString("currency");
    listing.quantity = rs.getInt("quantity");
    listing.listing_status = rs.getInt("listing_status");
    listing.marketplace = rs.getInt("marketplace");
    listing.upload_time = rs.getDate("upload_time");
    listing.owner_email_address = rs.getString("owner_email_address");
    return listing;
  }

  // Validates every field of a listing fetched from the API
  boolean validateListing(Listing listing) {
    List<String> invalidFields = new ArrayList<>();

    if (listing.id == null) invalidFields.add("id");
    if (listing.title == null) invalidFields.add("title");
    if (listing.description == null) invalidFields.add("description");
    if (listing.location_id == null || !locationLookup.contains(listing.location_id))
      invalidFields.add("location_id");
    if (listing.listing_price <= 0) invalidFields.add("listing_price");
    if (listing.currency == null || !currencyLookup.contains(listing.currency))
      invalidFields.add("currency");
    if (listing.quantity <= 0) invalidFields.add("quantity");
    if (listing.listing_status < 0 || !listingStatusLookup.contains(listing.listing_status))
      invalidFields.add("listing_status");
    if (listing.marketplace < 0 || !marketplaceLookup.contains(listing.marketplace))
      invalidFields.add("marketplace");
    if (listing.upload_time == null) invalidFields.add("upload_time");
    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    if (listing.owner_email_address == null || !listing.owner_email_address.matches(emailRegex))
      invalidFields.add("owner_email_address");

    if (invalidFields.size() > 0) {
      for (String invalidField : invalidFields)
        logger.logErrorToCSV(
          listing.id.toString() + ";" + listing.marketplace + ";" + invalidField
        );
      return false;
    }

    return true;
  }

  // Validates each API data entry and returns only the valid ones
  protected List<Listing> dataValidator(List<Listing> apiData) {
    List<Listing> validatedData = new ArrayList<>();
    logger.startErrorLog();
    for (Listing listing : apiData)
      if (validateListing(listing)) validatedData.add(listing);
    logger.closeErrorLog(); // Print error and close the logfile if applicable
    return validatedData;
  }

  protected List<String> createItemStringList(Listing listing) {
    return Arrays.asList(
      listing.id.toString(),
      listing.title,
      listing.description,
      listing.location_id.toString(),
      Double.toString(listing.listing_price),
      listing.currency,
      Integer.toString(listing.quantity),
      Integer.toString(listing.listing_status),
      Integer.toString(listing.marketplace),
      new SimpleDateFormat("yyyy-MM-dd").format(listing.upload_time),
      listing.owner_email_address
    );
  }
}

class Listing {
  UUID id;
  String title;
  String description;
  UUID location_id;
  double listing_price;
  String currency;
  int quantity;
  int listing_status;
  int marketplace;
  Date upload_time;
  String owner_email_address;

  Listing() {}

  // Copy constructor
  Listing(Listing listing) {
    this.id = listing.id;
    this.title = listing.title;
    this.description = listing.description;
    this.location_id = listing.location_id;
    this.listing_price = listing.listing_price;
    this.currency = listing.currency;
    this.quantity = listing.quantity;
    this.listing_status = listing.listing_status;
    this.marketplace = listing.marketplace;
    this.upload_time = listing.upload_time;
    this.owner_email_address = listing.owner_email_address;
  }
}