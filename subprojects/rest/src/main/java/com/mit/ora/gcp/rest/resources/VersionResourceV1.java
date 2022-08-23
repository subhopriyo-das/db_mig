/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.rest.resources;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import com.mit.ora.gcp.model.SwaggerConstants;
import io.swagger.v3.oas.annotations.Operation;

@Path("/version1")
//@Api(value = SwaggerConstants.INSIGHTS_API_VERSION, hidden = true)
@Produces(MediaType.APPLICATION_JSON)
public class VersionResourceV1 {

    public static final String ID_V1 = "v1";

    @GET
    @Operation(summary = "Get API version information",
    		description = "Returns information on Insights API version provided by this service.")
    public int getVersionInfo() {
        int version = 1;
        return version;
    }

}
