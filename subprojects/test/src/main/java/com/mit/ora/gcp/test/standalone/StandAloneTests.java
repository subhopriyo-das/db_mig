/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.test.standalone;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Date;
import java.util.concurrent.TimeUnit;

import javax.sql.DataSource;

import org.apache.wink.json4j.JSONArray;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mit.ora.gcp.common.utils.InsightsConfiguration;


public class StandAloneTests {

    private final static Logger LOGGER = LoggerFactory.getLogger(StandAloneTests.class);
    private static final InsightsConfiguration config = InsightsConfiguration.getInstance();
    private final static String tenant_id = "999";

    public StandAloneTests() {

    }


}
