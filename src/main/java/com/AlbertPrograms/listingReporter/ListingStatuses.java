package com.AlbertPrograms.listingReporter;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ListingStatuses extends DBSyncedItems<ListingStatus> {
  /**
   * @param dbManager - the dbManager instance to access the database from within the item
   * @throws SQLException - if DB can't be accessed or returns invalid values
   * @throws ConnectException - if API can't be accessed or returns invalid values
   */
  public ListingStatuses(DBManager dbManager) throws SQLException, ConnectException {
    super(dbManager);
    sync();
  }

  protected void initItemSpecifics() {
    itemName = "listingStatus";
    itemClass = ListingStatus.class;
    itemArrayClass = ListingStatus[].class;
    insertFields = "(id, status_name)";
    insertValues = "VALUES(CAST(? AS INT), ?)";
  }

  public void mapResultSet(ResultSet resultSet) throws SQLException {
    if (resultSet == null) throw new SQLException("ResultSet is null");

    items = new ArrayList<>();

    while (resultSet.next()) {
      int id = resultSet.getInt("id");
      String status_name = resultSet.getString("status_name");
      items.add(new ListingStatus(id, status_name));
    }
  }

  public List<Integer> getLookupList() {
    return items.stream().map(ListingStatus::getId).collect(Collectors.toList());
  }

  protected List<String> createDBSerializableItem(ListingStatus listingStatus) {
    return Arrays.asList(
      Integer.toString(listingStatus.getId()),
      listingStatus.getStatus_name()
    );
  }
}