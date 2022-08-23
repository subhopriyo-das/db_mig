/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.integration.test;

import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.ParseException;
import org.apache.http.entity.StringEntity;
import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mit.ora.gcp.common.utils.Constants;
import com.mit.ora.gcp.common.utils.HttpUtils;
import com.mit.ora.gcp.common.utils.InsightsConfiguration;
import com.mit.ora.gcp.common.utils.InsightsException;
import com.mit.ora.gcp.test.standalone.TestConstants;

/**
 * @author ashwinjain This test will delete drd that were previously created by
 *         this test, then make training request for drd and check get drd, get
 *         status, get list of drd and delete endpoints and hence will verify
 *         the created drd.
 */
public class MlDrdEndToEndTest {

	private final static Logger LOGGER = LoggerFactory.getLogger(MlDrdEndToEndTest.class);
	// data for request body
	private static JSONObject reqBodyTraining = new JSONObject();
	private static JSONArray colsJsonTrain;
	private static JSONObject response = new JSONObject();
	private static String drdName = TestConstants.DRD_NAME_TEST;
	private static String assetID = null;
	private static String assetName = TestConstants.BANK_MARKET;
	private static String description = "drd_description_test";
	private static String TEST_SUFFIX = InsightsConfiguration.getInstance()
			.getTestProperty("insights.db2oncloud.test.suffix");
	private static String workspaceName = TestConstants.ML_DRD_WS + "_" + TEST_SUFFIX;
	private static String assetIDURL;
	private static String drdID = null;
	private static String workspaceID = null;

	// http request related data
	private static StringEntity postReqEntity;
	private static HttpResponse res;
	private static String getUGURL = InsightsConfiguration.getInstance().getUGBaseUrl();
	private static String basicAuth = InsightsConfiguration.getInstance().getIISBasicAuthToken();
	private static String addIsAdminUser = TestConstants.ADD_ISADMIN_USER;
	private static String getInsightsURLV3 = InsightsConfiguration.getInstance()
			.getTestProperty("insights.base.url.v3");
	private static String drdUrl = getInsightsURLV3 + "/insights/data_rule_definitions";
	private static String workspace_overview = TestConstants.WORKSPACE_OVERVIEW;
	private static Map<String, String> headerBasicAuth = new HashMap<String, String>();

	/**
	 * Test setup will: Fetch workspace id of the verified workspace. add isadmin
	 * user to the workspace. Fetch the asset id with respect to the asset name. set
	 * up request body for training. Fetch and delete drd which were previously
	 * created by this test. Just a fail safe to ensure if test aborted, rerun will
	 * not face any issue. create a drd. wait for completion.
	 */
	@BeforeClass
	public static void testSetup() {
		try {
			// Setup test data.
// 			IntegrationTestDataSetup testDataSetupInst = new IntegrationTestDataSetup();
// 			assertTrue("Unable to setup the test data", testDataSetupInst.setUpTestData());

			// Set up basic auth for insights and workspace/asset details endpoint
			headerBasicAuth.put("Content-Type", "application/json");
			headerBasicAuth.put("Authorization", "Basic " + basicAuth);

			// appending current time on drd name.
			drdName += System.currentTimeMillis();

			// fetch Ml drd Workspace_id
			String fetch_workspace_url = getUGURL + workspace_overview;
			LOGGER.info("fetch workspace url=" + fetch_workspace_url);
			res = HttpUtils.executeRestAPI(fetch_workspace_url, "GET", headerBasicAuth, null);
			int status = res.getStatusLine().getStatusCode();
			assertTrue("failed to get workspace_overview, status=" + status, status == 200);
			JSONObject fetch_workspace_response = HttpUtils.parseResponseAsJSONObject(res);
			JSONArray rows = fetch_workspace_response.getJSONArray("rows");

			for (int i = 0; i < rows.size(); i++) {
				if (rows.getJSONObject(i).getString("WORKSPACE").trim().equalsIgnoreCase(workspaceName)) {
					workspaceID = rows.getJSONObject(i).getString("WORKSPACERID");
					LOGGER.info("Got Workspace ID: " + workspaceID + " For Workspace: " + workspaceName);
				}
			}
			if (workspaceID == null) {
				LOGGER.error("Data setup is incomplete : unable to fetch ML_DRD_WS workspace");
				assertTrue("Data setup is incomplete : unable to fetch ML_DRD_WS workspace", false);
			}
		} catch (InsightsException | JSONException e) {
			LOGGER.error("workspace for data rule definition is not present" + workspaceName + "StackTrace: " + e);
			fail("Data setup is incomplete : unable to fetch ML_DRD_WS workspace-" + e.getMessage());
		}

		// add isadmin to workspace
		try {
			LOGGER.info("Request body for adding isAdmin ot workspace: " + addIsAdminUser);
			postReqEntity = new StringEntity(addIsAdminUser);
			String addUserToWskUrl = getUGURL + "/dq/da/rest/v1/workspaces/" + workspaceID + "/user/update";
			LOGGER.info("create drd url= " + addUserToWskUrl);
			res = HttpUtils.executeRestAPI(addUserToWskUrl, "PUT", headerBasicAuth, postReqEntity);
			LOGGER.info("got response for adding isadmin user to workspace, Status: " + res.getStatusLine());
			assertTrue("Unable to add is admin to workspace: " + workspaceName + "WorkspaceID: " + workspaceID,
					res.getStatusLine().getStatusCode() == 200);
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for Delete drd, Response: " + response);
		} catch (ParseException | IOException | InsightsException e) {
			String message = "Error while Creating drd";
			LOGGER.error(message, e);
			fail(message);
		}

		//

		// get asset id from igc
		try {
			assetIDURL = getUGURL;
			assetIDURL += TestConstants.IGC_SEARCH_REST_SUFFIX + "/?" + TestConstants.TYPES + "="
					+ TestConstants.DATABASE_TABLE + "&text=" + assetName + "&search-properties=name";
			LOGGER.info("Retrieving Asset ID from IGC, URL: " + assetIDURL);
			res = HttpUtils.executeRestAPI(assetIDURL, "GET", headerBasicAuth, null);
			LOGGER.info("got response from igc for getting asset id, status: " + res.getStatusLine());
			assertTrue("Unable to get information of asset: " + assetName, res.getStatusLine().getStatusCode() == 200);
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response from igc for getting asset id, response: " + response);
			JSONArray items = response.getJSONArray(Constants.ITEMS);

			for (int itemNo = 0; itemNo < items.size(); itemNo++) {
				JSONObject item = items.getJSONObject(itemNo);
				if (item.getString(Constants._NAME).contentEquals(assetName)) {
					assetID = item.getString(Constants._ID);
					LOGGER.info("Got asset ID: " + assetID + " For asset: " + assetName);
				}
			}
			if (assetID == null) {
				String message = "Test Data Setup Required, Could not find asset: " + assetName;
				LOGGER.error(message);
				fail(message);
			}
		} catch (InsightsException | JSONException e) {
			LOGGER.error("Error while making req body JSON object", e);
			fail("Error while making req body JSON object");
		}

		try {
			// setting request Body for training.
			colsJsonTrain = new JSONArray().put("AGE").put("EDUCATION");
			// .put("DAY")
			// .put("DURATION").put("CAMPAIGN").put("PDAYS");
			JSONObject dataSetJSON = new JSONObject();
			dataSetJSON.put(Constants.COLUMNS, colsJsonTrain);
			dataSetJSON.put(Constants.ASSET_ID, assetID);
			reqBodyTraining.put(Constants.DRD_NAME, drdName);
			reqBodyTraining.put(Constants.DRD_DESCRIPTION, description);
			reqBodyTraining.put("workspace_rid", workspaceID);
			reqBodyTraining.put("data_asset", dataSetJSON);
			LOGGER.info("RequestBody for training: " + reqBodyTraining);
		} catch (JSONException e) {
			LOGGER.error("Error while making req body JSON object", e);
			fail("Error while making req body JSON object");
		}

		try {
			// get list of drd
			LOGGER.info("get list of drd url=" + drdUrl);
			res = HttpUtils.executeRestAPI(drdUrl, "GET", headerBasicAuth, null);
			LOGGER.info("got response from get list of drd, Status: " + res.getStatusLine());
			assertTrue("Could not get list of drd", res.getStatusLine().getStatusCode() == 200);
			// change to use from http utils.
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for get list of drd, Response: " + response);
		} catch (ParseException | InsightsException e) {
			String message = "Error while getting response for get list of drd";
			LOGGER.error(message, e);
			fail(message);
		}

		// delete if test drd present from previous run
		try {
			JSONArray resources;
			// This response is from get list of drd
			resources = response.getJSONArray(Constants.RESOURCES);
			for (int i = 0; i < resources.size(); i++) {
				if ((resources.getJSONObject(i).getString(Constants.DRD_NAME).contains("drd_name_test"))) {
					String drdIDForDeletion = resources.getJSONObject(i).getString(Constants.DRD_ID);
					String deleteDrd = drdUrl + "/" + drdIDForDeletion;
					LOGGER.info("delete drd url= " + deleteDrd);
					res = HttpUtils.executeRestAPI(deleteDrd, "DELETE", headerBasicAuth, null);
					LOGGER.info("got response for Delete drd, Status: " + res.getStatusLine());
					// change to http Utils
					JSONObject deleteResponse = HttpUtils.parseResponseAsJSONObject(res);
					LOGGER.info("got response for Delete drd, Response: " + deleteResponse);
					assertTrue("response from delete drd should contain drd id",
							deleteResponse.containsKey(Constants.DRD_ID));
					assertTrue(
							"drd id did not match for deletion Expected drd ID: " + drdIDForDeletion + " got: "
									+ deleteResponse.getString(Constants.DRD_ID),
							deleteResponse.getString(Constants.DRD_ID).contentEquals(drdIDForDeletion));
				}
			}
		} catch (JSONException | InsightsException | ParseException e) {
			String message = "Error while Delete drd";
			LOGGER.error(message, e);
			fail(message);
		}

		// Start Training
		try {
			String postBody = reqBodyTraining.toString();
			LOGGER.info("Request body for training: " + postBody);
			postReqEntity = new StringEntity(postBody);
			LOGGER.info("create drd url= " + drdUrl);
			res = HttpUtils.executeRestAPI(drdUrl, "POST", headerBasicAuth, postReqEntity);
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for Create drd, Status: " + res.getStatusLine());
			LOGGER.info("got response for Create drd, Message: " + response);
			assertTrue("Should get 200 response from training api, got: " + res.getStatusLine(),
					res.getStatusLine().getStatusCode() == 200);
			drdID = response.getString(Constants.DRD_ID);
			LOGGER.info("drd Registered for training, drd ID: " + drdID);
		} catch (ParseException | IOException | JSONException | InsightsException e) {
			String message = "Error while Creating drd";
			LOGGER.error(message, e);
			fail(message);
		}

		// get status
		try {
			int time = 0;
			Boolean trainingCompleted = false;
			// wait for maximum 200 sec for completion of training
			while (time < 200) {
				int timeToSleep = 10000;
				Thread.sleep(timeToSleep);
				time = time + timeToSleep / 1000;
				String getStatusUrl = drdUrl + "/status/" + drdID;
				LOGGER.info("get status of drd url= " + getStatusUrl);
				res = HttpUtils.executeRestAPI(getStatusUrl, "GET", headerBasicAuth, null);
				assertTrue("Should get 200 response from get status api, got: " + res.getStatusLine(),
						res.getStatusLine().getStatusCode() == 200);
				LOGGER.info("got response for drd status, Status: " + res.getStatusLine());
				response = HttpUtils.parseResponseAsJSONObject(res);
				LOGGER.info("got response for drd status, Response: " + response);
				if (response.getString(Constants.STATUS).contentEquals("SUCCESS")) {
					LOGGER.info("Training completed Successfully");
					trainingCompleted = true;
					break;
				}
			}
			assertTrue("Training Completed Successfully", trainingCompleted);
		} catch (InsightsException | InterruptedException | JSONException e) {
			String message = "Error while Checking drd Status";
			LOGGER.error(message, e);
			fail(message);
		}
	}

	/**
	 * This will fetch the drd details and verify the metadata and entity associated
	 * with the drd.
	 */
	@Test
	public void testGetDRD() {
		try {
			String getDrd = drdUrl + "/" + drdID;
			LOGGER.info("get drd url= " + getDrd);
			HttpResponse res = HttpUtils.executeRestAPI(getDrd, "GET", headerBasicAuth, null);
			LOGGER.info("got response for GET drd, Status: " + res.getStatusLine());
			assertTrue("Should get 200 response from get drd api, got: " + res.getStatusLine(),
					res.getStatusLine().getStatusCode() == 200);
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for GET drd, Response: " + response);
			assertTrue("drd data should contain metadata key", response.containsKey(Constants.METADATA));
			assertTrue("drd data should contain entity key", response.containsKey(Constants.ENTITY));

			JSONObject metadata = response.getJSONObject(Constants.METADATA);
			assertTrue("metadata should contain: " + Constants.CREATED_BY + " key",
					metadata.containsKey(Constants.CREATED_BY));
			assertTrue("metadata should contain: " + Constants.DRD_ID + " key", metadata.containsKey(Constants.DRD_ID));
			assertTrue("value of drd ID should be: " + drdID,
					metadata.getString(Constants.DRD_ID).contentEquals(drdID));
			assertTrue("metadata should contain: " + Constants.CREATED_AT + " key",
					metadata.containsKey(Constants.CREATED_AT));
			assertTrue("created at should not be null", metadata.getString(Constants.CREATED_AT) != null);

			JSONObject entity = response.getJSONObject(Constants.ENTITY);
			assertTrue("entity should contain: " + Constants.DRD_NAME + " key", entity.containsKey(Constants.DRD_NAME));
			assertTrue("value of drd name should be: " + drdName,
					entity.getString(Constants.DRD_NAME).contentEquals(drdName));
			assertTrue("entity should contain: " + Constants.MODEL_ID + " key", entity.containsKey(Constants.MODEL_ID));

		} catch (JSONException | InsightsException e) {
			String message = "Error while Get DRD test";
			LOGGER.error(message, e);
			fail(message);
		}
	}

	/**
	 * This will fetch the list of drd and check if the previouly created drd is
	 * present in the list.
	 */
	@Test
	public void getListOfDrd() {
		try {

			Boolean drdFound = false;
			String getList = drdUrl + "?limit=1000";
			LOGGER.info("get list of drd url= " + getList);
			HttpResponse res = HttpUtils.executeRestAPI(getList, "GET", headerBasicAuth, null);
			LOGGER.info("got response for GET list of drd, Status: " + res.getStatusLine());
			assertTrue("Should get 200 response from get list of drd api, got: " + res.getStatusLine(),
					res.getStatusLine().getStatusCode() == 200);
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for GET drd, Response: " + response);
			JSONArray resources = response.getJSONArray(Constants.RESOURCES);
			JSONObject resource = new JSONObject();
			for (int i = 0; i < resources.size(); i++) {
				resource = resources.getJSONObject(i);
				if (resource.getString(Constants.DRD_NAME).contentEquals(drdName)) {
					drdFound = true;
					break;
				}
			}
			assertTrue("resource should drd name as a key", resource.containsKey(Constants.DRD_NAME));
			assertTrue("Unable to find drd", drdFound);
		} catch (JSONException | InsightsException e) {
			String message = "Error while Get list of DRD test";
			LOGGER.error(message, e);
			fail(message);
		}
	}

	/**
	 * This will send request for training with same drd name. Endpoint is expected
	 * to give 400 as response with message drd with same name is present.
	 */
	@Test
	public void createDRDWithSameName() {
		try {
			String postBody = reqBodyTraining.toString();
			LOGGER.info("Post Body for training for drd with same name present test: " + postBody);
			StringEntity postReqEntity = new StringEntity(postBody);
			LOGGER.info("Post train url= " + drdUrl);
			HttpResponse res = HttpUtils.executeRestAPI(drdUrl, "POST", headerBasicAuth, postReqEntity);
			assertTrue("Should get 400 response from post train with same drd name, got: " + res.getStatusLine(),
					res.getStatusLine().getStatusCode() == 400);
			LOGGER.info("got response for post train with same drd name, Status: " + res.getStatusLine());
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for post train with same drd name, Response: " + response);
			assertTrue("response should contain status_message as key", response.containsKey(Constants.STATUS_MESSAGE));
			assertTrue(
					"resposne should have: " + Constants.DRD_WITH_SAME_NAME_EXISITS + " as status message, but got: "
							+ response.getString(Constants.STATUS_MESSAGE),
					response.getString(Constants.STATUS_MESSAGE).contentEquals(Constants.DRD_WITH_SAME_NAME_EXISITS));
		} catch (IOException | JSONException | InsightsException e) {
			String message = "Error while create drd with same name test";
			LOGGER.error(message, e);
			fail(message);
		}
	}

	/**
	 * This will send request for training with same asset and column combination.
	 * Endpoint is expected to give 400 as response with message drd with same asset
	 * and column combination present.
	 */
	@Test
	public void createDRDWithSameAssetAndColumnCombination() {
		try {
			// change name to get asset and column check to work
			reqBodyTraining.put(Constants.DRD_NAME, "abc");
			String postBody = reqBodyTraining.toString();
			LOGGER.info("Post Body for training " + postBody);
			StringEntity postReqEntity = new StringEntity(postBody);
			LOGGER.info("Post train url= " + drdUrl);
			HttpResponse res = HttpUtils.executeRestAPI(drdUrl, "POST", headerBasicAuth, postReqEntity);
			assertTrue("Should get 400 response from get list of drd api, got: " + res.getStatusLine(),
					res.getStatusLine().getStatusCode() == 400);
			LOGGER.info("got response for Post method to create drd, Status: " + res.getStatusLine());
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for GET drd, Response: " + response);
			assertTrue("response should contain status_message as key", response.containsKey(Constants.STATUS_MESSAGE));
			assertTrue(
					"resposne should have: " + Constants.DRD_WITH_SAME_ASSET_AND_COLS_PRESENT
							+ " as status message, but got: " + response.getString(Constants.STATUS_MESSAGE),
					response.getString(Constants.STATUS_MESSAGE)
							.contentEquals(Constants.DRD_WITH_SAME_ASSET_AND_COLS_PRESENT));

			// reverting back name change
			reqBodyTraining.put(Constants.DRD_NAME, drdName);
		} catch (IOException | JSONException | InsightsException e) {
			String message = "Error while create drd with same name asset and column combination";
			LOGGER.error(message, e);
			fail(message);
		}
	}

	/**
	 * This will send request for training with invalid workspace id. Endpoint is
	 * expected to give 404 as response.
	 */
	@Test
	public void createDRDWithIncorrectWorkspaceID() {
		try {
			// inserting random workspace rid
			reqBodyTraining.put(Constants.WORKSPACE_RID, "abc");
			String postBody = reqBodyTraining.toString();
			LOGGER.info("Post Body for training with incorrect workspace id: " + postBody);
			postReqEntity = new StringEntity(postBody);
			LOGGER.info("Post train url= " + drdUrl);
			res = HttpUtils.executeRestAPI(drdUrl, "POST", headerBasicAuth, postReqEntity);
			LOGGER.info("got response for Post method to create drd with incorrect workspcae id, Status: "
					+ res.getStatusLine());
			assertTrue("Should get 404 response Post method to create drd with incorrect workspcae id, got: "
					+ res.getStatusLine(), res.getStatusLine().getStatusCode() == 404);
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for Post train request with incorrect workspcae id, Response: " + response);

			// reverting back to original workspace id
			reqBodyTraining.put(Constants.WORKSPACE_RID, workspaceID);
		} catch (IOException | JSONException | InsightsException e) {
			String message = "Error while create drd with Incorrect Workspace ID";
			LOGGER.error(message, e);
			fail(message);
		}
	}

	/**
	 * This will send request for training with invalid asset id. Endpoint is
	 * expected to give 404 as response with message Asset Not Found.
	 */
	@Test
	public void createDRDWithIncorrectAssetID() {
		try {
			reqBodyTraining.getJSONObject("data_asset").put(Constants.ASSET_ID, "abc");
			// change in name to avoid drd with same name exists response
			reqBodyTraining.put(Constants.DRD_NAME, "abc");
			String postBody = reqBodyTraining.toString();
			LOGGER.info("Post Body for training with incorrect asset id: " + postBody);
			StringEntity postReqEntity = new StringEntity(postBody);
			LOGGER.info("Post train url= " + drdUrl);
			HttpResponse res = HttpUtils.executeRestAPI(drdUrl, "POST", headerBasicAuth, postReqEntity);
			LOGGER.info("got response for Post method to create drd with incorrect asset id, Status: "
					+ res.getStatusLine());
			assertTrue("Should get 404 response from Post method to create drd with incorrect asset id, got: "
					+ res.getStatusLine(), res.getStatusLine().getStatusCode() == 404);
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for  Post method to create drd with incorrect asset id, Response: " + response);
			assertTrue("response should contain status_message as key", response.containsKey(Constants.STATUS_MESSAGE));
			assertTrue(
					"resposne should have: Asset not found as status message, but got: "
							+ response.getString(Constants.STATUS_MESSAGE),
					response.getString(Constants.STATUS_MESSAGE).contentEquals("Asset not found"));
			// reverting back changes in name and asset id
			reqBodyTraining.getJSONObject("data_asset").put(Constants.ASSET_ID, assetID);
			reqBodyTraining.put(Constants.DRD_NAME, drdName);
		} catch (IOException | JSONException | InsightsException e) {
			String message = "Error while create drd with invalid asset id";
			LOGGER.error(message, e);
			fail(message);
		}
	}

	/**
	 * This will send BLOB type column for training, Marital in this case. Endpoint
	 * is expected to give 400 as response with message saying blob type not
	 * supported.
	 */
	@Test
	public void trainWithBlobColumn() {
		try {
			reqBodyTraining.put(Constants.DRD_NAME, drdName + System.currentTimeMillis());
			JSONArray colsJsonTrainNew = new JSONArray().put("JOB").put("AGE").put("MARITAL");
			reqBodyTraining.getJSONObject("data_asset").put(Constants.COLUMNS, colsJsonTrainNew);
			String postBody = reqBodyTraining.toString();
			LOGGER.info("Post Body for training with Marital(BLOB) column" + postBody);
			StringEntity postReqEntity = new StringEntity(postBody);
			LOGGER.info("Post train url= " + drdUrl);
			HttpResponse res = HttpUtils.executeRestAPI(drdUrl, "POST", headerBasicAuth, postReqEntity);
			LOGGER.info("got response for Post method to create drd, Status: " + res.getStatusLine());
			assertTrue("Should get 400 response from Post train request endpoint, got: " + res.getStatusLine(),
					res.getStatusLine().getStatusCode() == 400);
			response = HttpUtils.parseResponseAsJSONObject(res);
			LOGGER.info("got response for Post Train, Response: " + response);
			// TODO: message should be replace with status_message in rest layer
			assertTrue("response should contain message as key", response.containsKey(Constants.MESSAGE));
			assertTrue(
					"resposne should have: BLOB type column not supported, but got: "
							+ response.getString(Constants.MESSAGE),
					response.getString(Constants.MESSAGE).contains("BLOB"));
			assertTrue(
					"resposne should have: Marital column is not supported, but got: "
							+ response.getString(Constants.MESSAGE),
					response.getString(Constants.MESSAGE).contains("MARITAL"));
			// reverting back changes in columns and drd name
			reqBodyTraining.getJSONObject("data_asset").put(Constants.COLUMNS, colsJsonTrain);
			reqBodyTraining.put(Constants.DRD_NAME, drdName);
		} catch (IOException | JSONException | InsightsException e) {
			String message = "Error while create drd with same name asset and column combination";
			LOGGER.error(message, e);
			fail(message);
		}
	}

	/**
	 * This will delete the drd which was created during this test.
	 */
	@AfterClass
	public static void testCleanup() {
		try {
			if (drdID != null) {
				String deleteDrd = drdUrl + "/" + drdID;
				LOGGER.info("delete drd url= " + deleteDrd);
				res = HttpUtils.executeRestAPI(deleteDrd, "DELETE", headerBasicAuth, null);
				LOGGER.info("got response for Delete drd, Status: " + res.getStatusLine());
				// change to http Utils
				JSONObject deleteResponse = HttpUtils.parseResponseAsJSONObject(res);
				LOGGER.info("got response for Delete drd, Response: " + deleteResponse);
			}
		} catch (InsightsException e) {
			String message = "Error while deleting drd";
			LOGGER.error(message, e);
			fail(message);
		}
	}

}
