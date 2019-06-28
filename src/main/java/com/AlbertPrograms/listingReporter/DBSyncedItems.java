package com.AlbertPrograms.listingReporter;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Generic abstract class for storing items that are pulled from an API and synced in the DB
 *
 * @param <T> Contained item type
 *
 * @author Albert Kelemen
 */
public abstract class DBSyncedItems<T> {
  // These have to be specified in subclasses' initItemSpecifics methods
  protected String itemName;
  protected Class<T> itemClass;
  protected Class<T[]> itemArrayClass;
  protected String insertFields;
  protected String insertValues; // All non-string values must be CAST within the query
  // These can optionally be specified in subclasses
  protected boolean changing = false; // Set this to true if the data in this class needs to be updated after storage
  protected boolean canUseFallback = true; // Set this to false if
  protected String onConflict = "DO NOTHING"; // To upsert, override this and use the same fields and order as insertFields
  // onConflict string format: "([conflict (id) field] DO UPDATE SET [updated_field1] = ?, [updated_field1] = ?[, ...])"
  // Make sure to add wilcard substitute string generation at the end of the 'createDBSerializableItem' method's string list

  protected List<T> items;
  private DBManager dbManager;

  /**
   * Calls the initItemSpecifics method where the item-specific fields are set.
   * The method sync() should be called at the end of each constructor manually, since putting it here
   * would prevent from setting extra fields via the constructor before syncing.
   *
   * @param dbManager - the dbManager instance to access the database from within the item
   */
  public DBSyncedItems(DBManager dbManager) {
    initItemSpecifics();
    this.dbManager = dbManager;
  }

  /**
   * Set item specific fields in this
   */
  protected abstract void initItemSpecifics();

  /**
   * Creates a serializable object from the item to store it in the DB
   *
   * @param item - an item from the "items" list
   * @return a list of strings (the elements are the item's columns) that can be used in the
   *   INSERT prepared statement to serialize the item into the DB
   */
  protected abstract List<String> createDBSerializableItem(T item);

  /**
   * Maps the resultSet received from the DB query into the "items" list of the instance
   *
   * @param resultSet - the resultSet from the DB query
   * @throws SQLException - in case the query fails
   */
  public abstract void mapResultSet(ResultSet resultSet) throws SQLException;

  /**
   * Validates the list of data by returning another list containing only valid items.
   * Doesn't validate by default, overload to add filtering function.
   *
   * @param apiData - the list of items to be validated
   * @return the (filtered) list of items
   */
  protected List<T> dataValidator(List<T> apiData) {
    return apiData;
  }

  protected void getFromAPI() throws ConnectException, IllegalArgumentException {
    ApiDataFetcher apiDataFetcher = new ApiDataFetcher();
    items = dataValidator(apiDataFetcher.fetchFromMockaroo(itemName, itemClass, itemArrayClass));
    if (items == null || items.size() == 0) {
      // The APIs should always return non-null values; otherwise it's a connection error
      throw new ConnectException();
    }
  }

  private void loadFromDB() throws SQLException {
    dbManager.getItemsWithSelect("SELECT * FROM " + itemName, this);
  }

  private void saveToDB() throws SQLException {
    List<List<String>> values = new ArrayList<>();

    for (T item : items) {
      // Invalid data will return null here
      List<String> value = createDBSerializableItem(item);
      if (value != null) values.add(value);
    }

    dbManager.insertItems(
      "INSERT INTO " + itemName + " " + insertFields + " " + insertValues + " ON CONFLICT " + onConflict, values
    );
  }

  protected void sync() throws SQLException, ConnectException {
    if (changing) {
      // If the data is constantly new, we always need to get it from the API then load
      // everything back from the DB to cache everything
      try {
        getFromAPI();
        saveToDB();
        loadFromDB();
      } catch (ConnectException e){
        e.printStackTrace();
        // If API data fails, try callback if allowed, or throw ConnectException
        if (canUseFallback) {
          // If fallback DB data available, notify user, otherwise throw SQLException
          loadFromDB();
        } else throw new ConnectException("Error while fetching " + itemName + " data from the API - " +
          "fallback cannot be used with this type");
      }
    } else {
      // With not constantly changing data query the DB first to see if it's already stored
      if (dbManager.getCount("SELECT COUNT(*) FROM " + itemName) == 0) {
        // Empty table, pulling data from the API and saving it to the DB
        try {
          getFromAPI();
          saveToDB();
        } catch (ConnectException e) {
          throw new ConnectException("Error while fetching " + itemName + " data from the API, DB has no relevant data");
        }
      } else {
        loadFromDB();
      }
    }
  }

  public List<T> getItems() {
    return new ArrayList<>(items);
  }

  public abstract List getLookupList();
}