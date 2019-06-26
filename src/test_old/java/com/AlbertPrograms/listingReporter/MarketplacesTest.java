package com.AlbertPrograms.listingReporter;

import org.junit.Assert;
import org.junit.Test;

public class MarketplacesTest {
  static MarketplacesStub getMarketplacesStub() {
    MarketplacesStub marketplacesStub = new MarketplacesStub();
    marketplacesStub.getFromAPI();
    return marketplacesStub;
  }

  @Test
  public void checkMarketplaces() {
    MarketplacesStub marketplacesStub = getMarketplacesStub();
    // avoid getItems() so no DB connection is attempted
    Assert.assertEquals(2, marketplacesStub.items.size());
    for (Marketplace marketplace : marketplacesStub.items) {
      Assert.assertNotNull(marketplace);
      Assert.assertTrue(marketplace.id > 0);
      Assert.assertTrue(marketplace.marketplace_name.length() > 0);
    }
  }
}

class MarketplacesStub extends Marketplaces {
  @Override
  protected boolean getFromAPI() {
    ApiDataFetcherStub apiDataFetcherStub = new ApiDataFetcherStub();
    items = dataValidator(apiDataFetcherStub.fetchFromMockaroo("marketplace", Marketplace.class, Marketplace[].class));
    return items != null && items.size() > 0;
  }
}
