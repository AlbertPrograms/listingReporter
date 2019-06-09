package com.AlbertPrograms.listingReporter;

import org.junit.Assert;
import org.junit.Test;

import java.util.UUID;

public class LocationsTest {
  static LocationsStub getLocationsStub() {
    LocationsStub locationsStub = new LocationsStub();
    locationsStub.getFromAPI();
    return locationsStub;
  }

  @Test
  public void checkMarketplaces() {
    LocationsStub locationsStub = getLocationsStub();
    // avoid getItems() so no DB connection is attempted
    Assert.assertEquals(10, locationsStub.items.size());
    for (Location location : locationsStub.items) {
      Assert.assertNotNull(location);
      Assert.assertNotNull(location.id);
      Assert.assertTrue(location.manager_name.length() > 5);
      Assert.assertEquals(12, location.phone.length());
      Assert.assertTrue(location.address_primary.length() > 8);
      Assert.assertTrue(location.country.length() > 3);
      Assert.assertTrue(location.town.length() > 1);
    }
  }
}

class LocationsStub extends Locations {
  @Override
  protected boolean getFromAPI() {
    ApiDataFetcherStub apiDataFetcherStub = new ApiDataFetcherStub();
    items = dataValidator(apiDataFetcherStub.fetchFromMockaroo("location", Location.class, Location[].class));
    return items != null && items.size() > 0;
  }
}
