package com.AlbertPrograms.listingReporter;

import java.util.UUID;

public class Location {
  private UUID id;
  private String manager_name;
  private String phone;
  private String address_primary;
  private String address_secondary;
  private String country;
  private String town;
  private String postal_code;

  public Location(UUID id, String manager_name, String phone, String address_primary, String address_secondary,
                  String country, String town, String postal_code) {
    this.id = id;
    this.manager_name = manager_name;
    this.phone = phone;
    this.address_primary = address_primary;
    this.address_secondary = address_secondary;
    this.country = country;
    this.town = town;
    this.postal_code = postal_code;
  }

  public UUID getId() {
    return id;
  }

  public String getManager_name() {
    return manager_name;
  }

  public String getPhone() {
    return phone;
  }

  public String getAddress_primary() {
    return address_primary;
  }

  public String getAddress_secondary() {
    return address_secondary;
  }

  public String getCountry() {
    return country;
  }

  public String getTown() {
    return town;
  }

  public String getPostal_code() {
    return postal_code;
  }
}
