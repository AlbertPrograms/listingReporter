package com.AlbertPrograms.listingReporter;

import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class CurrenciesTest {
  static CurrenciesStub getCurrenciesStub() {
    CurrenciesStub currenciesStub = new CurrenciesStub();
    currenciesStub.getFromAPI();
    return currenciesStub;
  }

  @Test
  public void checkCurrencies() {
    CurrenciesStub currenciesStub = new CurrenciesStub();

    currenciesStub.getFromAPI();
    // avoid getItems() so no DB connection is attempted
    Assert.assertEquals(6, currenciesStub.items.size());
    for (Currency currency : currenciesStub.items) {
      Assert.assertNotNull(currency);
      Assert.assertEquals(3, currency.currency_name.length());
      Assert.assertNotEquals(0, currency.currency_value);
    }
  }
}

class CurrenciesStub extends Currencies {
  @Override
  protected List<CurrencyWrapper> getCurrencyWrapperFromAPI() {
    ApiDataFetcherStub apiDataFetcherStub = new ApiDataFetcherStub();
    return apiDataFetcherStub.fetchFromMockaroo("currency", CurrencyWrapper.class, CurrencyWrapper[].class);
  }
}
