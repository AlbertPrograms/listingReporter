package com.AlbertPrograms.listingReporter;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public class ListingsTest {
  static ListingsStub getListingsStub() {
    CurrenciesStub currenciesStub = CurrenciesTest.getCurrenciesStub();
    LocationsStub locationsStub = LocationsTest.getLocationsStub();
    MarketplacesStub marketplacesStub = MarketplacesTest.getMarketplacesStub();
    ListingStatusesStub listingStatusesStub = ListingStatusesTest.getListingStatusesStub();

    ListingsStub listingsStub = new ListingsStub(
      // Create lookups based on stub data for easy listing validation
      currenciesStub.items.stream().map(currency -> currency.currency_name).collect(Collectors.toList()),
      locationsStub.items.stream().map(location -> location.id).collect(Collectors.toList()),
      marketplacesStub.items.stream().map(marketplace -> marketplace.id).collect(Collectors.toList()),
      listingStatusesStub.items.stream().map(listingStatus -> listingStatus.id).collect(Collectors.toList())
    );
    listingsStub.getFromAPI();
    return listingsStub;
  }

  @Test
  public void checkMarketplaces() {
    ListingsStub listingsStub = getListingsStub();

    // avoid getItems() so no DB connection is attempted
    Assert.assertEquals(16, listingsStub.items.size()); // From the 28 demo data only 16 are valid
    for (Listing listing : listingsStub.items) {
      Assert.assertNotNull(listing);
      Assert.assertNotNull(listing.id);
      Assert.assertTrue(listing.title.length() > 3);
      Assert.assertTrue(listing.description.length() > 5);
      Assert.assertNotNull(listing.location_id);
      Assert.assertTrue(listingsStub.locationLookup.contains(listing.location_id));
      Assert.assertTrue(listing.listing_price > 0);
      Assert.assertEquals(3, listing.currency.length());
      Assert.assertTrue(listingsStub.currencyLookup.contains(listing.currency));
      Assert.assertTrue(listing.quantity > 0);
      Assert.assertTrue(listingsStub.listingStatusLookup.contains(listing.listing_status));
      Assert.assertTrue(listingsStub.marketplaceLookup.contains(listing.marketplace));
      Assert.assertNotNull(listing.upload_time);
      String emailRegex = "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$";
      Assert.assertTrue(listing.owner_email_address.matches(emailRegex));
    }
  }
}

class ListingsStub extends Listings {
  ListingsStub(List<String> currencyLookup, List<UUID> locationLookup, List<Integer> marketplaceLookup, List<Integer> listingStatusLookup) {
    super(currencyLookup, locationLookup, marketplaceLookup, listingStatusLookup);
    logger = new ListingImportLoggerStub("");
  }

  @Override
  protected boolean getFromAPI() {
    ApiDataFetcherStub apiDataFetcherStub = new ApiDataFetcherStub();
    items = dataValidator(apiDataFetcherStub.fetchFromMockaroo("listing", Listing.class, Listing[].class));
    return items != null && items.size() > 0;
  }

  @Override
  protected List<Listing> dataValidator(List<Listing> apiData) {
    List<Listing> validatedData = new ArrayList<>();
    for (Listing listing : apiData)
      if (validateListing(listing)) validatedData.add(listing);
    return validatedData;
  }
}

// Preventing error logs from being created during testing
class ListingImportLoggerStub extends ListingImportLogger {
  ListingImportLoggerStub(String filename) {
    super(filename);
  }

  @Override
  void logErrorToCSV(String error) {
  }
}