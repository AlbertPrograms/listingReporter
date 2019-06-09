package com.AlbertPrograms.listingReporter;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

class Currencies extends DBSyncedItems<Currency> {
  protected void initClassSpecifics() {
    itemName = "currency";
    itemClass = Currency.class;
    itemArrayClass = Currency[].class;
    insertFields = "(currency_name, currency_value)";
    insertValues = "VALUES(?, CAST(? AS DOUBLE PRECISION))";

    changing = true; // Currencies should be updated every time, but fallback can be used
    onConflict = "(currency_name) DO UPDATE SET currency_value = CAST(? AS DOUBLE PRECISION)";
  }

  protected List<CurrencyWrapper> getCurrencyWrapperFromAPI() {
    ApiDataFetcher apiDataFetcher = new ApiDataFetcher();

    return apiDataFetcher.fetchDataFromURL(
      "https://api.exchangeratesapi.io/latest",
      CurrencyWrapper.class,
      CurrencyWrapper[].class
    );
  }

  // Overriding default getFromAPI because this one returns rates in an object rather than an array
  protected boolean getFromAPI() {
    List<CurrencyWrapper> wrapper = getCurrencyWrapperFromAPI();

    // Bad response or no connection - no API data
    if (wrapper == null || wrapper.size() == 0) return false;

    // The important part of received wrapper object (which is a single one) is the rates field
    CurrencyTable rates = wrapper.get(0).rates;

    // Add the 6 currency values we need from either the API or the DB (EUR is the base, so it's always 1)
    // Since we only have 5 fixed currencies plus base it's cleaner to hard-wire them rather than iterate the fields
    items.add(new Currency("EUR", 1));
    items.add(new Currency("HUF", rates.HUF));
    items.add(new Currency("USD", rates.USD));
    items.add(new Currency("GBP", rates.GBP));
    items.add(new Currency("AUD", rates.AUD));
    items.add(new Currency("JPY", rates.JPY));

    return true;
  }

  protected Currency readResult(ResultSet rs) throws SQLException {
    Currency currency = new Currency();
    currency.currency_name = rs.getString("currency_name");
    currency.currency_value = rs.getDouble("currency_value");
    return currency;
  }

  protected List<String> createItemStringList(Currency currency) {
    return Arrays.asList(
      currency.currency_name,
      Double.toString(currency.currency_value),
      Double.toString(currency.currency_value)
    );
  }
}

class Currency {
  String currency_name;
  double currency_value;

  Currency() {}

  Currency(String name, double value) {
    this.currency_name = name;
    this.currency_value = value;
  }
}

// The exchangeratesapi.io API returns currency rates in the rates property of the returned object,
// not an array, so we need a wrapper to match its data structure for serialization
class CurrencyWrapper {
  CurrencyTable rates;
}

// Describes what amount of the given currency make up the value of 1 EUR
// In the format required for serialization of JSON data
class CurrencyTable {
  // EUR is the base currency so it's always 1
  double HUF = 0;
  double USD = 0;
  double GBP = 0;
  double AUD = 0;
  double JPY = 0;

  public String toString() {
    return (
      "HUF: " + HUF + '\n' +
      "USD: " + USD + '\n' +
      "GBP: " + GBP + '\n' +
      "AUD: " + AUD + '\n' +
      "JPY: " + JPY + '\n'
    );
  }
}
