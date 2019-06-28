package com.AlbertPrograms.listingReporter;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Marketplaces extends DBSyncedItems<Marketplace> {
  /**
   * @param dbManager - the dbManager instance to access the database from within the item
   * @throws SQLException - if DB can't be accessed or returns invalid values
   * @throws ConnectException - if API can't be accessed or returns invalid values
   */
  public Marketplaces(DBManager dbManager) throws SQLException, ConnectException {
    super(dbManager);
    sync();
  }

  protected void initItemSpecifics() {
    itemName = "marketplace";
    itemClass = Marketplace.class;
    itemArrayClass = Marketplace[].class;
    insertFields = "(id, marketplace_name)";
    insertValues = "VALUES(CAST(? AS INT), ?)";
  }

  public void mapResultSet(ResultSet resultSet) throws SQLException {
    if (resultSet == null) throw new SQLException("ResultSet is null");

    items = new ArrayList<>();

    while (resultSet.next()) {
      int id = resultSet.getInt("id");
      String marketplace_name = resultSet.getString("marketplace_name");
      items.add(new Marketplace(id, marketplace_name));
    }
  }

  public List<Integer> getLookupList() {
    return items.stream().map(Marketplace::getId).collect(Collectors.toList());
  }

  protected List<String> createDBSerializableItem(Marketplace marketplace) {
    return Arrays.asList(
      Integer.toString(marketplace.getId()),
      marketplace.getMarketplace_name()
    );
  }
}
