package com.AlbertPrograms.listingReporter;

import java.net.ConnectException;
import java.sql.*;
import java.util.List;

/**
 * Handles PostgreSQL database connection and execution of single and batch queries
 * Manual connection and disconnection is required before and after the desired queries!
 *
 * @author Albert Kelemen
 */
public class DBManager {
  private Connection db = null;

  private String url = null;
  private String username = null;
  private String password = null;

  /**
   * Create new DBManager with the given connection details
   *
   * @param url - database url
   * @param username - database login name
   * @param password - database password
   */
  public DBManager(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  /**
   * Check if DB connection is open
   *
   * @return whether the DB connection is currently established
   */
  public boolean isOpen() throws SQLException {
    return db != null && !db.isClosed();
  }

  /**
   * Attempts to connect the DB
   *
   * @throws ClassNotFoundException - if Postgres JDBC driver is missing
   * @throws ConnectException - if connection to the DB failed
   */
  public void connect() throws ClassNotFoundException, ConnectException {
    try {
      if (isOpen()) {
        return;
      }

      // Load postgres driver
      Class.forName("org.postgresql.Driver");
      db = DriverManager.getConnection("jdbc:postgresql://" + url, username, password);

      System.out.println("Opened database successfully");
    } catch (ClassNotFoundException e) {
      throw new ClassNotFoundException("Can't find PostgreSQL JDBC driver");
    } catch (SQLException e) {
      throw new ConnectException("Failed to connect to the PostgreSQL database");
    }
  }

  /**
   * Attempts to close the live DB connection if there's one
   */
  public void close() {
    try {
      if (isOpen()) {
        db.close();
        db = null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

  /**
   *
   * @param query - checks
   * @return boolean
   */
  private static boolean isQuerySafe(String query) {
    return query != null && query.indexOf('\'') == -1 && query.indexOf('-') == -1;
  }

  private static void checkQueryString(String query) throws IllegalArgumentException {
    if (query == null) {
      throw new IllegalArgumentException("Query string is null");
    } else if (!isQuerySafe(query)) {
      throw new IllegalArgumentException("Query is unsafe: " + query);
    }
  }

  public void getItemsWithSelect(String query, DBSyncedItems itemContainer) throws IllegalArgumentException, SQLException {
    if (!query.substring(0, 6).equalsIgnoreCase("select")) {
      throw new IllegalArgumentException("Query should be a select");
    }
    checkQueryString(query);

    PreparedStatement stmt = null;
    try {
      stmt = db.prepareStatement(query);

      ResultSet resultSet = null;

      // If the query was a select, there are results - return null otherwise
      resultSet = stmt.executeQuery();

      itemContainer.mapResultSet(resultSet);
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public int getCount(String query) throws IllegalArgumentException, SQLException {
    if (!query.substring(0, 6).equalsIgnoreCase("select")) {
      throw new IllegalArgumentException("Query should be a select");
    } else if (!query.toLowerCase().contains("count")) {
      throw new IllegalArgumentException("Query should contain the 'count' keyword");
    } else
    checkQueryString(query);

    PreparedStatement stmt = null;
    try {
      stmt = db.prepareStatement(query);

      ResultSet resultSet = null;

      // If the query was a select, there are results - return null otherwise
      resultSet = stmt.executeQuery();

      if (resultSet.next()) {
        return resultSet.getInt("count");
      } else {
        return 0;
      }
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }

  public void insertItems(String query, List<List<String>> valueStringsList) throws IllegalArgumentException, SQLException {
    if (!query.substring(0, 6).equalsIgnoreCase("insert")) {
      throw new IllegalArgumentException("Query should be an insert");
    }
    checkQueryString(query);

    PreparedStatement stmt = null;
    try {
      // Prepared statement with sanitized input to prevent unintended behavior
      // Since all the substitutes for the '?'-s are strings, the SQL query has to
      // explicitly cast the received variables to the appropriate types.
      stmt = db.prepareStatement(query);

      for (List<String> valueStrings : valueStringsList) {
        for (int i = 0; i < valueStrings.size(); i++) {
          stmt.setString(i + 1, valueStrings.get(i));
        }
        stmt.addBatch();
      }

      stmt.executeBatch();
    } finally {
      try {
        if (stmt != null) {
          stmt.close();
        }
      } catch (SQLException e) {
        e.printStackTrace();
      }
    }
  }
}
