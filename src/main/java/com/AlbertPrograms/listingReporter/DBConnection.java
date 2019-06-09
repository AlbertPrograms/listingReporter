package com.AlbertPrograms.listingReporter;

import java.io.FileInputStream;
import java.io.InputStream;
import java.net.ConnectException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import javax.naming.ConfigurationException;

// Handles postgres db connection
class DBConnection {
  private static Connection db = null;

  private static String url = null;
  private static String username = null;
  private static String password = null;

  // Attempts to load the db connection config file to the class fields
  private static boolean loadConfig() {
    // Skip loading if all the fields are known
    if (url != null && username != null && password != null) return true;

    try (InputStream is = new FileInputStream("config/application.properties")) {
      Properties properties = new Properties();
      properties.load(is);

      url = properties.getProperty("dbconnection.url");
      username = properties.getProperty("dbconnection.username");
      password = properties.getProperty("dbconnection.password");
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Config might not have loaded properly or have null value(s)
    return url != null && username != null && password != null;
  }

  private static boolean connect() {
    if (db != null) return true;

    try {
      if (!loadConfig()) throw new ConfigurationException(
        "DB connection configuration missing or invalid. Make sure you have " +
          "dbconnection.url, dbconnection.username and dbconnection.password set in the file 'application.properties'."
      );

      // Check for postgres driver availability
      Class.forName("org.postgresql.Driver");
      db = DriverManager.getConnection("jdbc:postgresql://" + url, username, password);

      System.out.println("Opened database successfully");
      return true;
    } catch (SQLException | ConfigurationException | ClassNotFoundException e) {
      e.printStackTrace();
      return false;
    }
  }

  static ResultSet doQuery(String query) {
    return doQuery(query, new ArrayList<>());
  }

  private static ResultSet doQuery(String query, List<String> valueStrings) {
    try {
      if (!connect()) throw new ConnectException("Failed to connect to the database");

      // Prepared statement with sanitized input to prevent unintended behavior
      // Since all the substitutes for the '?'-s are strings, the SQL query has to
      // explicitly cast the received variable to the appropriate type.
      PreparedStatement stmt = db.prepareStatement(query);
      for (int i = 0; i < valueStrings.size(); i++) {
        stmt.setString(i + 1, valueStrings.get(i));
      }

      // If the query was a select, there are results - return null otherwise
      if (query.substring(0, 6).equalsIgnoreCase("select")) {
        return stmt.executeQuery();
      } else {
        stmt.executeUpdate();
        return stmt.getGeneratedKeys();
      }
    } catch (SQLException | ConnectException e) {
      e.printStackTrace();
      return null;
    }
  }

    // Does a query with safe string setting (input sanitizing)
  static ResultSet doBatchQuery(String query, List<List<String>> valueStringsList) {
    try {
      if (!connect()) throw new ConnectException("Failed to connect to the database");

      // Prepared statement with sanitized input to prevent unintended behavior
      // Since all the substitutes for the '?'-s are strings, the SQL query has to
      // explicitly cast the received variable to the appropriate type.
      PreparedStatement stmt = db.prepareStatement(query);

      for (List<String> valueStrings : valueStringsList) {
        for (int i = 0; i < valueStrings.size(); i++) {
          stmt.setString(i + 1, valueStrings.get(i));
        }
        stmt.addBatch();
      }

      stmt.executeBatch();
      return stmt.getGeneratedKeys();
    } catch (SQLException | ConnectException e) {
      e.printStackTrace();
      return null;
    }
  }

  static void close() {
    try {
      db.close();
      db = null;
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }
}
