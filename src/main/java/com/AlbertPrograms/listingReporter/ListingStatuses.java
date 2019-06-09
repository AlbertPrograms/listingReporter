package com.AlbertPrograms.listingReporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

class ListingStatuses extends DBSyncedItems<ListingStatus> {
  protected void initClassSpecifics() {
    itemName = "listingStatus";
    itemClass = ListingStatus.class;
    itemArrayClass = ListingStatus[].class;
    insertFields = "(id, status_name)";
    insertValues = "VALUES(CAST(? AS INT), ?)";
  }

  protected ListingStatus readResult(ResultSet rs) throws SQLException {
    ListingStatus listingStatus = new ListingStatus();
    listingStatus.id = rs.getInt("id");
    listingStatus.status_name = rs.getString("status_name");
    return listingStatus;
  }

  protected List<String> createItemStringList(ListingStatus listingStatus) {
    return Arrays.asList(
      Integer.toString(listingStatus.id),
      listingStatus.status_name
    );
  }
}

class ListingStatus {
  int id;
  String status_name;
}
