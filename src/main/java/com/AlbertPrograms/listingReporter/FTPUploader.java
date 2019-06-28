package com.AlbertPrograms.listingReporter;

import hirondelle.date4j.DateTime;
import org.apache.commons.net.ftp.FTPClient;

import java.io.IOException;
import java.io.InputStream;
import java.util.TimeZone;

/**
 * Simple FTP uploader that uploads the file passed as InputStream to the target fileName.
 *
 * @author Albert Kelemen
 */
public final class FTPUploader {
  private String url = null;
  private String username = null;
  private String password = null;

  /**
   * Create new FTPUploader with the given connection details
   *
   * @param url - FTP url
   * @param username - FTP login name
   * @param password - FTP password
   */
  public FTPUploader(String url, String username, String password) {
    this.url = url;
    this.username = username;
    this.password = password;
  }

  /**
   * Attempts to upload a file passed as InputStream via the FTP connection to the destination file specified in fileName
   *
   * @param fileName - destination filename
   * @param fileInputStream - the InputStream of the file contents
   * @throws IOException - if the connection or upload failed
   */
  public void uploadFile(String fileName, InputStream fileInputStream) throws IOException {
    FTPClient client = new FTPClient();
    try {
      client.connect(url);
      client.login(username, password);
      client.storeFile(fileName, fileInputStream);
    } finally {
      try {
        if (client.isConnected()) {
          client.logout();
          client.disconnect();
        }
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  /**
   * Attempts to upload a report file passed as InputStream via the FTP connection
   * filename format: report_YYYY-MM-DD_hh.mm.ss
   *
   * @param reportJsonIs - the InputStream of the file contents
   * @throws IOException - if the connection or upload failed
   */
  public void uploadReport(InputStream reportJsonIs) throws IOException {
    String timeStamp = DateTime.now(TimeZone.getDefault())
      .format("YYYY-MM-DD_hh.mm.ss");

    try {
      uploadFile("report_" + timeStamp + ".json", reportJsonIs);
    } catch (IOException e) {
      System.err.println("Failed to save report");
      throw e;
    }
  }
}
