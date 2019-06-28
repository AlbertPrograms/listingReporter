package com.AlbertPrograms.listingReporter;

/**
 * The exchangeratesapi.io API returns currency rates in the rates property of the returned object,
 * not an array, so we need a simple wrapper to match its data structure for JSON serialization
 *
 * @author Albert Kelemen
 */
public class CurrencyWrapper {
  CurrencyTable rates;
}
