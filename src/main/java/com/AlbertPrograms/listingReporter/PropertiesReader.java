package com.AlbertPrograms.listingReporter;

import javax.naming.ConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Reads database and FTP connection settings (url, user, pw) stored in the file "application.properties"
 * If location is omitted, defaults to ./config/application.properties
 *
 * @author Albert Kelemen
 */
public final class PropertiesReader {
  private String dbUrl;
  private String dbUsername;
  private String dbPassword;

  private String ftpUrl;
  private String ftpUsername;
  private String ftpPassword;

  /**
   * Calls the constructor with the default application.properties file location
   *
   * @throws IOException - in case the properties cannot be loaded, an IOException is thrown
   */
  public PropertiesReader() throws IOException, ConfigurationException {
    this("config/application.properties");
  }

  private static void checkPropertyValiditiesBasic(String... properties) throws ConfigurationException {
    for (String property : properties) {
      if (property == null || property.length() <= 3) {
        throw new ConfigurationException();
      }
    }
  }

  /**
   * Reads the connection settings from the file "application.properties"
   *
   * @param fileLocation - location of the file "application.properties" relative to root
   * @throws IOException - in case the properties cannot be loaded, an IOException is thrown
   * @throws ConfigurationException - if the loaded properties are missing or too short, a Conf. Exc. is thrown
   */
  public PropertiesReader(String fileLocation) throws IOException, ConfigurationException {
    try (InputStream is = new FileInputStream("config/application.properties")) {
      Properties properties = new Properties();
      properties.load(is);

      dbUrl = properties.getProperty("db.url");
      dbUsername = properties.getProperty("db.username");
      dbPassword = properties.getProperty("db.password");

      ftpUrl = properties.getProperty("ftp.url");
      ftpUsername = properties.getProperty("ftp.username");
      ftpPassword = properties.getProperty("ftp.password");

      checkPropertyValiditiesBasic(dbUrl, dbUsername, dbPassword, ftpUrl, ftpUsername, ftpPassword);
    } catch (IOException e) {
      // Pass the exception to the caller to handle
      throw new IOException("Error failed to load properties file at " + fileLocation + ": " + e.getMessage());
    } catch (ConfigurationException e) {
      // Pass the exception to the caller to handle
      throw new ConfigurationException("Invalid properties given in the properties file");
    }
  }

  public String getDbUrl() {
    return dbUrl;
  }

  public String getDbUsername() {
    return dbUsername;
  }

  public String getDbPassword() {
    return dbPassword;
  }

  public String getFtpUrl() {
    return ftpUrl;
  }

  public String getFtpUsername() {
    return ftpUsername;
  }

  public String getFtpPassword() {
    return ftpPassword;
  }
}
