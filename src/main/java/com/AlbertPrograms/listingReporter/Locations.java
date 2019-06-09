package com.AlbertPrograms.listingReporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

class Locations extends DBSyncedItems<Location> {
  protected void initClassSpecifics() {
    itemName = "location";
    itemClass = Location.class;
    itemArrayClass = Location[].class;
    insertFields = "(id, manager_name, phone, address_primary, address_secondary, country, town, postal_code)";
    insertValues = "VALUES(CAST(? AS UUID), ?, ?, ?, ?, ?, ?, ?)";
  }

  protected Location readResult(ResultSet rs) throws SQLException {
    Location location = new Location();
    location.id = UUID.fromString(rs.getString("id"));
    location.manager_name = rs.getString("manager_name");
    location.phone = rs.getString("phone");
    location.address_primary = rs.getString("address_primary");
    location.address_secondary = rs.getString("address_secondary");
    location.country = rs.getString("country");
    location.town = rs.getString("town");
    location.postal_code = rs.getString("postal_code");
    return location;
  }

  protected List<String> createItemStringList(Location location) {
    return Arrays.asList(
      location.id.toString(),
      location.manager_name,
      location.phone,
      location.address_primary,
      location.address_secondary,
      location.country,
      location.town,
      location.postal_code
    );
  }
}

class Location {
  UUID id;
  String manager_name;
  String phone;
  String address_primary;
  String address_secondary;
  String country;
  String town;
  String postal_code;
}
