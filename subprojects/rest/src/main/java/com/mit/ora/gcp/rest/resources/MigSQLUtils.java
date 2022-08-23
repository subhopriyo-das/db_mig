/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.rest.resources;

import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.mit.ora.gcp.common.utils.CommonUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterStyle;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import net.sf.jsqlparser.JSQLParserException;

import com.mit.ora.gcp.store.db.sql.QueryParser;

@Path("/sql")
@Produces(MediaType.TEXT_PLAIN)
public class MigSQLUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(MigSQLUtils.class);

	@POST
	@Produces(MediaType.APPLICATION_JSON)
	@Operation(summary = "Retrieve the table names from given SQL query.", description = "")
	public Response processSQLStatement(
			@Parameter(description = "SQL Script to process", required = true, name = "file", style = ParameterStyle.FORM) @RequestBody(ref = "sql_query") String sql)
			throws JSONException {

		JSONObject result = new JSONObject();
		JSONObject errorResp = new JSONObject();
		try {
			QueryParser parser = new QueryParser();
			result = parser.ProcessSQL(sql);
		} catch (JSONException | JSQLParserException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			try {
				errorResp.put("Error", e);
			} catch (JSONException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			return CommonUtils.secureResponse(errorResp, Status.INTERNAL_SERVER_ERROR);
		}
		return CommonUtils.secureResponse(result, Status.OK);
	}

}
