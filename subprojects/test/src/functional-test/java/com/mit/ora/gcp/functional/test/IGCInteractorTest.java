/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.functional.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

import javax.sql.DataSource;

import org.apache.http.HttpResponse;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import com.mit.ora.gcp.common.utils.Constants;
import com.mit.ora.gcp.common.utils.HttpUtils;
import com.mit.ora.gcp.common.utils.InsightsConfiguration;
import com.mit.ora.gcp.common.utils.InsightsException;
import com.mit.ora.gcp.test.standalone.IntegrationTestDataSetup;
import com.mit.ora.gcp.test.standalone.TestConstants;
import com.mit.ora.gcp.test.standalone.UnitTestUtils;

public class IGCInteractorTest {

	private static final Logger LOGGER = LoggerFactory.getLogger(IGCInteractorTest.class);
	private static String assetName = "BANK_MARKET";
	private static String assetIDURL = null;
	private static String assetID = null;
	private static String dbName = null;
	private static String schema = null;
	private static String nullTableRID = null; // to check if null is passed in place of tableID
	private static String dummyTableRID = "abc"; // to check if wrong tableID is passed
	private static DataSource ds;
	private static String tableIdQuery = Constants.IGCINTERACTOR_TEST_TABLERID_QUERY;
	private static JSONObject res = new JSONObject();

	@BeforeClass
	public static void test_setup() throws SQLException {
//		IntegrationTestDataSetup testDataSetupInst = new IntegrationTestDataSetup();
//		assertTrue("Unable to setup the test data", testDataSetupInst.setUpTestData());

		dbName = InsightsConfiguration.getInstance().getTestProperty("insights.db2oncloud.name");
		schema = InsightsConfiguration.getInstance().getTestProperty("insights.db2oncloud.schema");

		assetIDURL = InsightsConfiguration.getInstance().getIISBaseUrl();
		assetIDURL += TestConstants.IGC_SEARCH_REST_SUFFIX + "/?" + TestConstants.TYPES + "="
				+ TestConstants.DATABASE_TABLE + "&text=" + assetName + "&search-properties=name";
		LOGGER.info("Retrieving Asset ID from IGC, URL: " + assetIDURL);
		// get asset id from igc
		Map<String, String> header = new HashMap<String, String>();
		header.put("Authorization", "Basic " + InsightsConfiguration.getInstance().getIISBasicAuthToken());
		try {
			HttpResponse res = HttpUtils.executeRestAPI(assetIDURL, "GET", header, null);
			assertTrue("Error While retrieving asset ID", res.getStatusLine().getStatusCode() == 200);
			JSONObject jsonResponse = HttpUtils.parseResponseAsJSONObject(res);
			JSONArray items = jsonResponse.getJSONArray(Constants.ITEMS);

			for (int itemNo = 0; itemNo < items.size(); itemNo++) {
				JSONObject item = items.getJSONObject(itemNo);
				if (item.getString(Constants._NAME).contentEquals(assetName)) {
					assetID = item.getString(Constants._ID);
				}
			}
			if (assetID == null) {
				String message = "Test Data Setup Required, Could not find asset: " + assetName;
				LOGGER.error(message);
				fail(message);
			}
		} catch (InsightsException | JSONException e) {
			LOGGER.error("Error while making req body JSON object", e);
			assertTrue("Error while making req body JSON object", false);
		}
	}

	@Test
	public void negativeTestResponseCheck() {
		try {
			// passing tableRID as null
			assertTrue("Should return not found when given null as tableRID", res.getInt(Constants.CODE) != 200);
		} catch ( JSONException e) {
			LOGGER.error("JSONException : Error while getting connection detils" + e.getMessage());
			assertTrue("Should not throw exception when given null as tableRID", false);
		}

		try {
			// passing random tableRID
			assertTrue("Should return not found when given null as tableRID", res.getInt(Constants.CODE) != 200);
		} catch ( JSONException e) {
			LOGGER.error("JSONException : Error while getting connection detils" + e.getMessage());
			assertTrue("Should not throw exception when given null as tableRID", false);
		}
	}

	
	@AfterClass
	public static void test_cleanup() {
		assetName = null;
		assetID = null;
		ds = null;
		tableIdQuery = null;
		res = null;
	}

}