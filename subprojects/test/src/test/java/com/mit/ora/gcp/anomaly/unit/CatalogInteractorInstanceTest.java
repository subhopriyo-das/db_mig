/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.anomaly.unit;

import static org.junit.Assert.assertTrue;

import org.junit.BeforeClass;
import org.junit.Test;
import com.mit.ora.gcp.common.utils.InsightsConfiguration;
import com.mit.ora.gcp.common.utils.InsightsConfiguration.CatalogInteractorType;


/**
 * This will test if instance of catalog interactor is created or not and its
 * type.
 */
public class CatalogInteractorInstanceTest {

	CatalogInteractorType catalogInteractorType = InsightsConfiguration.getInstance().getCatalogInteractorType();

	@BeforeClass
	public static void testSetup() {
	}

	/**
	 * This method will check if Catalog Interactor Instance is created or not and
	 * will also check type of Interactor(IGC or WKC)
	 */
	@Test
	public void checkInteractorType() {
		assertTrue("Interactor type should be wkc or igc", (catalogInteractorType == CatalogInteractorType.IGC
				|| catalogInteractorType == CatalogInteractorType.WKC));
	}
}
