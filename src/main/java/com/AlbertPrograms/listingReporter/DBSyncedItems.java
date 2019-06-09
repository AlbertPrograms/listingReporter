package com.AlbertPrograms.listingReporter;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

// Generic class for all the items we need to get from the API and store in the DB
abstract class DBSyncedItems<T> {
  // These have to be specified in subclasses
  String itemName;
  Class<T> itemClass;
  Class<T[]> itemArrayClass;
  String insertFields;
  String insertValues; // All non-string values must be CAST within the query

  // These can optionally be specified in subclasses
  boolean changing = false; // Set this to true if the data in this class needs to be updated after storage
  boolean canUseFallback = true; // Set this to false if
  String onConflict = "DO NOTHING"; // To upsert, override this and use the same fields and order as insertFields
  // onConflict string format: "([conflict (id) field] DO UPDATE SET [updated_field1] = ?, [updated_field1] = ?[, ...])"
  // Make sure to add wilcard substitute string generation at the end of the 'createItemStringList' method's string list

  List<T> items;
  private boolean synced = false;
  private boolean outdated = true;

  DBSyncedItems() {
    initClassSpecifics();
    items = new ArrayList<>();
  }

  protected abstract void initClassSpecifics();

  protected abstract T readResult(ResultSet rs) throws SQLException;

  protected abstract List<String> createItemStringList(T item);

  // No validation by default, can be overridden if API data needs validation
  protected List<T> dataValidator(List<T> apiData) {
    return apiData;
  }

  protected boolean getFromAPI() {
    ApiDataFetcher apiDataFetcher = new ApiDataFetcher();
    items = dataValidator(apiDataFetcher.fetchFromMockaroo(itemName, itemClass, itemArrayClass));
    return items != null && items.size() > 0;
  }

  private boolean loadFromDB() {
    ResultSet rs = DBConnection.doQuery("SELECT * FROM " + itemName);
    try {
      if (rs == null) throw new SQLException("Select from " + itemName + " query returned invalid result");

      while (rs.next()) {
        T result = readResult(rs);
        items.add(result);
      }

      return true;
    } catch (SQLException e) {
      System.err.println("Error while loading " + itemName + " data from the DB");
      e.printStackTrace();
      return false;
    } catch (Exception e) {
      // In case anything goes wrong while loading the data
      e.printStackTrace();
      return false;
    }
  }

  private boolean saveToDB() {
    List<List<String>> values = new ArrayList<>();

    for (T item : items) {
      // Invalid data will return null here
      List<String> value = createItemStringList(item);
      if (value != null) values.add(value);
    }

    ResultSet rs = DBConnection.doBatchQuery(
      "INSERT INTO " + itemName + " " + insertFields + " " + insertValues + " ON CONFLICT " + onConflict, values
    );

    return rs != null;
  }

  private boolean syncFromAPI() {
    if (getFromAPI()) {
      synced = saveToDB();
      outdated = !synced;
      return synced;
    } else return false;
  }

  private boolean syncFromDB() {
    if (loadFromDB()) {
      synced = true;
      return true;
    } else return false;
  }

  // Setting the 'synced' and 'outdated' fields is done in the two above methods unless there's an exception
  boolean syncData() {
    // Only need to sync once, but only if data's not changing with each API fetch
    if ((changing && !outdated) || (!changing && synced)) return true;

    try {
      if (changing) {
        // If the data is constantly new, we always need to get it from the API then load
        // everything back from the DB to cache everything
        if (syncFromAPI()) {
          return true;
        } else {
          // If API data fails, try callback if allowed, or throw ConnectException
          if (canUseFallback) {
            // If fallback DB data available, notify user, otherwise throw SQLException
            if (syncFromDB()) {
              System.out.println("Error while fetching " + itemName + " data from the API - " +
                "using fallback data from the DB");
              return true;
            } else {
              throw new SQLException("Error while fetching " + itemName + " data from the DB");
            }
          } else throw new ConnectException("Error while fetching " + itemName + " data from the API - " +
            "fallback cannot be used with this type");
        }
      } else {
        // With not constantly changing data query the DB first to see if it's already stored
        ResultSet rs = DBConnection.doQuery("SELECT COUNT(*) FROM " + itemName);
        // Sync failed if the result is empty or null (DB error)
        if (rs == null || !rs.next()) throw new SQLException("Error while fetching " + itemName + " data from the DB");

        if (rs.getInt("count") == 0) {
          // Empty table, pulling data from the API
          if (syncFromAPI()) {
            return true;
          } else throw new ConnectException("Error while fetching " + itemName + " data from the API");
        } else {
          if (syncFromDB()) {
            return true;
          } else throw new SQLException("Error while fetching " + itemName + " data from the DB");
        }
      }
    } catch (SQLException | ConnectException e) {
      e.printStackTrace();
      synced = false;
      return false;
    }
  }

  List<T> getItems() {
    syncData();
    return items;
  }
}