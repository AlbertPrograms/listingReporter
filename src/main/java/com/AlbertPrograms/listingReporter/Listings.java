package com.AlbertPrograms.listingReporter;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

public class Listings extends DBSyncedItems<Listing> {
  private ListingImportLogger logger;
  private List<String> currencyLookup;
  private List<UUID> locationLookup;
  private List<Integer> marketplaceLookup;
  private List<Integer> listingStatusLookup;

  /**
   * @param dbManager - the dbManager instance to access the database from within the item
   * @param currencyLookup - currency lookup list by currency_name (String)
   * @param locationLookup - location lookup list by id (UUID)
   * @param marketplaceLookup - marketplace lookup list by id (int)
   * @param listingStatusLookup - listingStatus lookup list by id (int)
   * @throws SQLException - if DB can't be accessed or returns invalid values
   * @throws ConnectException - if API can't be accessed or returns invalid values
   */
  public Listings(DBManager dbManager, List<String> currencyLookup, List<UUID> locationLookup, List<Integer> marketplaceLookup,
                  List<Integer> listingStatusLookup) throws SQLException, ConnectException {
    super(dbManager);
    this.currencyLookup = currencyLookup;
    this.locationLookup = locationLookup;
    this.marketplaceLookup = marketplaceLookup;
    this.listingStatusLookup = listingStatusLookup;
    sync();
  }

  protected void initItemSpecifics() {
    itemName = "listing";
    itemClass = Listing.class;
    itemArrayClass = Listing[].class;
    insertFields = "(id, title, description, location_id, listing_price, currency, quantity, " +
      "listing_status, marketplace, upload_time, owner_email_address)";
    insertValues = "VALUES(CAST(? AS UUID), ?, ?, CAST(? AS UUID), CAST(? AS DOUBLE PRECISION), " +
      "?, CAST(? AS INT), CAST(? AS INT), CAST(? AS INT), CAST(? AS DATE), ?)";

    changing = true; // Listings change every time
    canUseFallback = false; // They're not only different but also new, so no DB fallback allowed

    logger = new ListingImportLogger("importLog.csv");
  }

  public void mapResultSet(ResultSet resultSet) throws SQLException {
    if (resultSet == null) throw new SQLException("ResultSet is null");

    items = new ArrayList<>();

    while (resultSet.next()) {
      UUID id = UUID.fromString(resultSet.getString("id"));
      String title = resultSet.getString("title");
      String description = resultSet.getString("description");
      UUID location_id = UUID.fromString(resultSet.getString("location_id"));
      double listing_price = resultSet.getDouble("listing_price");
      String currency = resultSet.getString("currency");
      int quantity = resultSet.getInt("quantity");
      int listing_status = resultSet.getInt("listing_status");
      int marketplace = resultSet.getInt("marketplace");
      Date upload_time = resultSet.getDate("upload_time");
      String owner_email_address = resultSet.getString("owner_email_address");
      items.add(new Listing(id, title, description, location_id, listing_price, currency, quantity, listing_status,
        marketplace, upload_time, owner_email_address));
    }
  }

  public List<UUID> getLookupList() {
    return items.stream().map(Listing::getId).collect(Collectors.toList());
  }

  // Validates every field of a listing fetched from the API
  private boolean validateListing(Listing listing) {
    List<String> invalidFields = new ArrayList<>();

    if (listing.getId() == null) invalidFields.add("id");
    if (listing.getTitle() == null) invalidFields.add("title");
    if (listing.getDescription() == null) invalidFields.add("description");
    if (listing.getLocation_id() == null || !locationLookup.contains(listing.getLocation_id()))
      invalidFields.add("location_id");
    if (listing.getListing_price() <= 0) invalidFields.add("listing_price");
    if (listing.getCurrency() == null || !currencyLookup.contains(listing.getCurrency()))
      invalidFields.add("currency");
    if (listing.getQuantity() <= 0) invalidFields.add("quantity");
    if (listing.getListing_status() < 0 || !listingStatusLookup.contains(listing.getListing_status()))
      invalidFields.add("listing_status");
    if (listing.getMarketplace() < 0 || !marketplaceLookup.contains(listing.getMarketplace()))
      invalidFields.add("marketplace");
    if (listing.getUpload_time() == null) invalidFields.add("upload_time");
    String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
    if (listing.getOwner_email_address() == null || !listing.getOwner_email_address().matches(emailRegex))
      invalidFields.add("owner_email_address");

    if (invalidFields.size() > 0) {
      for (String invalidField : invalidFields)
        logger.addToErrorLog(
          listing.getId().toString() + ";" + listing.getMarketplace() + ";" + invalidField
        );
      return false;
    }

    return true;
  }

  // Validates each API data entry and returns only the valid ones
  protected List<Listing> dataValidator(List<Listing> apiData) {
    List<Listing> validatedData = new ArrayList<>();
    for (Listing listing : apiData)
      if (validateListing(listing)) validatedData.add(listing);
    logger.writeErrorLog(); // Print error and close the logfile if applicable
    return validatedData;
  }

  protected List<String> createDBSerializableItem(Listing listing) {
    return Arrays.asList(
      listing.getId().toString(),
      listing.getTitle(),
      listing.getDescription(),
      listing.getLocation_id().toString(),
      Double.toString(listing.getListing_price()),
      listing.getCurrency(),
      Integer.toString(listing.getQuantity()),
      Integer.toString(listing.getListing_status()),
      Integer.toString(listing.getMarketplace()),
      new SimpleDateFormat("yyyy-MM-dd").format(listing.getUpload_time()),
      listing.getOwner_email_address()
    );
  }
}
