package com.AlbertPrograms.listingReporter;

import org.junit.Assert;
import org.junit.Test;

public class ReporterTest {
  @Test
  public void checkReport() {
    Reporter reporter = new Reporter(
      CurrenciesTest.getCurrenciesStub(),
      MarketplacesTest.getMarketplacesStub(),
      ListingsTest.getListingsStub()
    );

    Report report = reporter.createReport();

    Assert.assertNotNull(report.date_of_report);
    Assert.assertNotNull(report.all_listings);
    Assert.assertNotNull(report.months);

    Assert.assertEquals(16, report.all_listings.total_listing_count);
    Assert.assertEquals(11, report.all_listings.total_ebay_listing_count);
    Assert.assertEquals(3465.34, report.all_listings.total_ebay_listing_price, 0.01);
    Assert.assertEquals(315.03, report.all_listings.average_ebay_listing_price, 0.01);
    Assert.assertEquals(5, report.all_listings.total_amazon_listing_count);
    Assert.assertEquals(2074.04, report.all_listings.total_amazon_listing_price, 0.01);
    Assert.assertEquals(414.81, report.all_listings.average_amazon_listing_price, 0.01);

    Assert.assertTrue(report.months.size() > 0);
    Assert.assertEquals("2017-02", report.months.get(0).month);
    Assert.assertEquals("N/A", report.months.get(2).report.best_lister_email);
    Assert.assertEquals("jipsen@hotmail.com", report.months.get(13).report.best_lister_email);
    Assert.assertEquals(418.82, report.months.get(22).report.total_ebay_listing_price, 0.01);
  }
}
