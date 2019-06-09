package com.AlbertPrograms.listingReporter;

import org.apache.commons.net.ftp.FTPClient;

import javax.naming.ConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

final class FTPConnection {
  private static FTPClient client = new FTPClient();

  private static String url = null;
  private static String username = null;
  private static String password = null;

  private static boolean loadConfig() {
    // Skip loading if all the fields are known
    if (url != null && username != null && password != null) return true;

    try (InputStream is = new FileInputStream("config/application.properties")) {
      Properties properties = new Properties();
      properties.load(is);

      url = properties.getProperty("ftpconnection.url");
      username = properties.getProperty("ftpconnection.username");
      password = properties.getProperty("ftpconnection.password");
    } catch (Exception e) {
      e.printStackTrace();
    }

    // Config might not have loaded properly or have null value(s)
    return url != null && username != null && password != null;
  }

  static boolean uploadFile(String fileName, InputStream fis) {
    try {
      if (!loadConfig()) throw new ConfigurationException(
        "FTP connection configuration missing or invalid. Make sure you have " +
          "ftpconnection.url, ftpconnection.username and ftpconnection.password set in the file 'application.properties'."
      );

      client.connect(url);
      client.login(username, password);
      client.storeFile(fileName, fis);
      client.logout();
      return true;
    } catch (IOException | ConfigurationException e) {
      e.printStackTrace();
      return false;
    }
  }
}
