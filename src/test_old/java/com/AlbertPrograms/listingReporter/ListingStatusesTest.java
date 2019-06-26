package com.AlbertPrograms.listingReporter;

import org.junit.Assert;
import org.junit.Test;

public class ListingStatusesTest {
  static ListingStatusesStub getListingStatusesStub() {
    ListingStatusesStub listingStatusesStub = new ListingStatusesStub();
    listingStatusesStub.getFromAPI();
    return listingStatusesStub;
  }

  @Test
  public void checkListingStatuses() {
    ListingStatusesStub listingStatusStub = getListingStatusesStub();
    // avoid getItems() so no DB connection is attempted
    Assert.assertEquals(3, listingStatusStub.items.size());
    for (ListingStatus listingStatus : listingStatusStub.items) {
      Assert.assertNotNull(listingStatus);
      Assert.assertTrue(listingStatus.id > 0);
      Assert.assertTrue(listingStatus.status_name.length() > 0);
    }
  }
}

class ListingStatusesStub extends ListingStatuses {
  @Override
  protected boolean getFromAPI() {
    ApiDataFetcherStub apiDataFetcherStub = new ApiDataFetcherStub();
    items = dataValidator(apiDataFetcherStub.fetchFromMockaroo("listingStatus", ListingStatus.class, ListingStatus[].class));
    return items != null && items.size() > 0;
  }
}