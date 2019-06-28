package com.AlbertPrograms.listingReporter;

import java.util.Date;
import java.util.UUID;

public class Listing {
  private UUID id;
  private String title;
  private String description;
  private UUID location_id;
  private double listing_price;
  private String currency;
  private int quantity;
  private int listing_status;
  private int marketplace;
  private Date upload_time;
  private String owner_email_address;

  public Listing(UUID id, String title, String description, UUID location_id, double listing_price, String currency,
                 int quantity, int listing_status, int marketplace, Date upload_time, String owner_email_address) {
    this.id = id;
    this.title = title;
    this.description = description;
    this.location_id = location_id;
    this.listing_price = listing_price;
    this.currency = currency;
    this.quantity = quantity;
    this.listing_status = listing_status;
    this.marketplace = marketplace;
    this.upload_time = upload_time;
    this.owner_email_address = owner_email_address;
  }

  /**
   * Copy constructor
   *
   * @param listing - the listing to copy from
   */
  public Listing(Listing listing) {
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

  public void setEurCurrency(double value) {
    currency = "EUR";
    listing_price = value;
  }

  public UUID getId() {
    return id;
  }

  public String getTitle() {
    return title;
  }

  public String getDescription() {
    return description;
  }

  public UUID getLocation_id() {
    return location_id;
  }

  public double getListing_price() {
    return listing_price;
  }

  public String getCurrency() {
    return currency;
  }

  public int getQuantity() {
    return quantity;
  }

  public int getListing_status() {
    return listing_status;
  }

  public int getMarketplace() {
    return marketplace;
  }

  public Date getUpload_time() {
    return upload_time;
  }

  public String getOwner_email_address() {
    return owner_email_address;
  }
}
