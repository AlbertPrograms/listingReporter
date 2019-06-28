package com.AlbertPrograms.listingReporter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ApiDataFetcher {
  // Fetch JSON data in string line(s)
  private List<String> fetchJsonFromURL(String urlString) {
    ArrayList<String> outputList = new ArrayList<>();

    try {
      // Connect to the API
      URL url = new URL(urlString);
      HttpURLConnection conn = (HttpURLConnection) url.openConnection();
      conn.setRequestMethod("GET");
      conn.setRequestProperty("Accept", "application/json");

      // Throw an exception if response is not 200 (ok)
      if (conn.getResponseCode() != 200) {
        throw new RuntimeException("Failed : HTTP Error code : "
          + conn.getResponseCode());
      }

      // Init the reader and the temp string
      InputStreamReader in = new InputStreamReader(conn.getInputStream());
      BufferedReader br = new BufferedReader(in);

      String output;

      // Iterate the line(s) returned by API and store them in the output list
      while ((output = br.readLine()) != null) {
        outputList.add(output);
      }

      // Disconnect when everything's been read
      conn.disconnect();
    } catch (UnknownHostException e) {
      System.err.println("Can't reach API target host: " + e.getMessage());
    } catch (Exception e) {
      e.printStackTrace();
    }

    return outputList;
  }

  // Fetch JSON data into Class instances - make sure the class field types match the data in the JSON!
  // Parameters should be the URL string, YourClass.class, YourClass[].class
  public <T> List<T> fetchDataFromURL(String urlString, Class<T> typeClass, Class<T[]> typeArrayClass) {
    ArrayList<T> outputList = new ArrayList<>();
    List<String> jsonData = fetchJsonFromURL(urlString);

    // Don't start further processing on no data
    if (jsonData.size() == 0) {
      return outputList;
    }

    // Init JSON parser with no character escaping and the required MM/dd/yyyy format
    Gson gson = new GsonBuilder()
      .disableHtmlEscaping()
      .setDateFormat("MM/dd/yyyy")
      .create();

    // Init temp variables for both array and object storage
    T typeObject;
    T[] typeObjects;

    // Iterate the lines
    for (String jsonLine: jsonData) {
      try {
        if (jsonLine.trim().charAt(0) == '[') {
          // Array
          typeObjects = gson.fromJson(jsonLine, typeArrayClass);
          outputList.addAll(Arrays.asList(typeObjects));
        } else if (jsonLine.trim().charAt(0) == '{') {
          // Object
          typeObject = gson.fromJson(jsonLine, typeClass);
          outputList.add(typeObject);
        }
        // Malformed data is skipped
      } catch (JsonSyntaxException e) {
        // In case of wrong JSON syntax, the serialized data probably doesn't match the target class
        System.err.println("The JSON data doesn't match the serialization target class");
        e.printStackTrace();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }

    return outputList;
  }

  // Fetches from the Mockaroo API with the key made for this project
  public <T> List<T> fetchFromMockaroo(String type, Class<T> typeClass, Class<T[]> typeArrayClass) {
    return fetchDataFromURL("https://my.api.mockaroo.com/" + type + "?key=63304c70", typeClass, typeArrayClass);
  }
}
