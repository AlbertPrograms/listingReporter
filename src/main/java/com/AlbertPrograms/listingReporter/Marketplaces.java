package com.AlbertPrograms.listingReporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

class Marketplaces extends DBSyncedItems<Marketplace> {
  protected void initClassSpecifics() {
    itemName = "marketplace";
    itemClass = Marketplace.class;
    itemArrayClass = Marketplace[].class;
    insertFields = "(id, marketplace_name)";
    insertValues = "VALUES(CAST(? AS INT), ?)";
  }

  protected Marketplace readResult(ResultSet rs) throws SQLException {
    Marketplace marketplace = new Marketplace();
    marketplace.id = rs.getInt("id");
    marketplace.marketplace_name = rs.getString("marketplace_name");
    return marketplace;
  }

  protected List<String> createItemStringList(Marketplace marketplace) {
    return Arrays.asList(
      Integer.toString(marketplace.id),
      marketplace.marketplace_name
    );
  }
}

class Marketplace {
  int id;
  String marketplace_name;
}
