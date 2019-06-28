package com.AlbertPrograms.listingReporter;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class Locations extends DBSyncedItems<Location> {
  /**
   * @param dbManager - the dbManager instance to access the database from within the item
   * @throws SQLException - if DB can't be accessed or returns invalid values
   * @throws ConnectException - if API can't be accessed or returns invalid values
   */
  public Locations(DBManager dbManager) throws SQLException, ConnectException {
    super(dbManager);
    sync();
  }

  protected void initItemSpecifics() {
    itemName = "location";
    itemClass = Location.class;
    itemArrayClass = Location[].class;
    insertFields = "(id, manager_name, phone, address_primary, address_secondary, country, town, postal_code)";
    insertValues = "VALUES(CAST(? AS UUID), ?, ?, ?, ?, ?, ?, ?)";
  }

  public void mapResultSet(ResultSet resultSet) throws SQLException {
    if (resultSet == null) throw new SQLException("ResultSet is null");

    items = new ArrayList<>();

    while (resultSet.next()) {
      UUID id = UUID.fromString(resultSet.getString("id"));
      String manager_name = resultSet.getString("manager_name");
      String phone = resultSet.getString("phone");
      String address_primary = resultSet.getString("address_primary");
      String address_secondary = resultSet.getString("address_secondary");
      String country = resultSet.getString("country");
      String town = resultSet.getString("town");
      String postal_code = resultSet.getString("postal_code");
      items.add(new Location(id, manager_name, phone, address_primary, address_secondary, country, town, postal_code));
    }
  }

  public List<UUID> getLookupList() {
    return items.stream().map(Location::getId).collect(Collectors.toList());
  }

  protected List<String> createDBSerializableItem(Location location) {
    return Arrays.asList(
      location.getId().toString(),
      location.getManager_name(),
      location.getPhone(),
      location.getAddress_primary(),
      location.getAddress_secondary(),
      location.getCountry(),
      location.getTown(),
      location.getPostal_code()
    );
  }
}