package com.AlbertPrograms.listingReporter;

import java.net.ConnectException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class Currencies extends DBSyncedItems<Currency> {
  /**
   * @param dbManager - the dbManager instance to access the database from within the item
   * @throws SQLException - if DB can't be accessed or returns invalid values
   * @throws ConnectException - if API can't be accessed or returns invalid values
   */
  public Currencies(DBManager dbManager) throws SQLException, ConnectException {
    super(dbManager);
    sync();
  }

  protected void initItemSpecifics() {
    itemName = "currency";
    itemClass = Currency.class;
    itemArrayClass = Currency[].class;
    insertFields = "(currency_name, currency_value)";
    insertValues = "VALUES(?, CAST(? AS DOUBLE PRECISION))";

    changing = true; // Currencies should be updated every time, but fallback can be used
    onConflict = "(currency_name) DO UPDATE SET currency_value = CAST(? AS DOUBLE PRECISION)";
  }

  public void mapResultSet(ResultSet resultSet) throws SQLException {
    if (resultSet == null) throw new SQLException("ResultSet is null");

    items = new ArrayList<>();

    while (resultSet.next()) {
      String currency_name = resultSet.getString("currency_name");
      double currency_value = resultSet.getDouble("currency_value");
      items.add(new Currency(currency_name, currency_value));
    }
  }

  public List<String> getLookupList() {
    return items.stream().map(Currency::getCurrency_name).collect(Collectors.toList());
  }

  protected List<String> createDBSerializableItem(Currency currency) {
    return Arrays.asList(
      currency.getCurrency_name(),
      Double.toString(currency.getCurrency_value()),
      Double.toString(currency.getCurrency_value()) // for onConflict
    );
  }

  private List<CurrencyWrapper> getCurrencyWrapperFromAPI() {
    ApiDataFetcher apiDataFetcher = new ApiDataFetcher();

    return apiDataFetcher.fetchDataFromURL(
      "https://api.exchangeratesapi.io/latest",
      CurrencyWrapper.class,
      CurrencyWrapper[].class
    );
  }

  // Overriding default getFromAPI because this one returns rates in an object rather than an array
  protected void getFromAPI() throws ConnectException, IllegalArgumentException {
    List<CurrencyWrapper> wrapper = getCurrencyWrapperFromAPI();

    // Bad response or no connection - no API data
    if (wrapper == null || wrapper.size() == 0) {
      throw new ConnectException();
    }

    // The important part of received wrapper object (which is a single one) is the rates field
    CurrencyTable rates = wrapper.get(0).rates;

    items = new ArrayList<>();

    // Add the 6 currency values we need from either the API or the DB (EUR is the base, so it's always 1)
    // Since we only have 5 fixed currencies plus base it's cleaner to hard-wire them rather than iterate the fields
    items.add(new Currency("EUR", 1));
    items.add(new Currency("HUF", rates.getHUF()));
    items.add(new Currency("USD", rates.getUSD()));
    items.add(new Currency("GBP", rates.getGBP()));
    items.add(new Currency("AUD", rates.getAUD()));
    items.add(new Currency("JPY", rates.getJPY()));
  }
}
