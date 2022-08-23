/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.common.utils;

import java.io.PrintWriter;
import java.io.StringWriter;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.Response.Status;

import org.apache.wink.json4j.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CommonUtils {
    
    private final static Logger LOGGER = LoggerFactory.getLogger(CommonUtils.class);
    
    public enum AuthenticationScheme {
        AUTH_SCHEME_BASIC,
        AUTH_SCHEME_JWT
    }
    
    public static Response secureResponse(JSONObject result, Status status) {
        return secureResponse(AuthenticationScheme.AUTH_SCHEME_BASIC, result, status);
    }
    
    public static Response secureResponse(AuthenticationScheme authScheme, JSONObject result, Status status) {
        ResponseBuilder rsb = Response.status(status)
                .header("X-Content-Type-Options", "nosniff")
                .header("X-XSS-Protection", "1; mode=block")
                .header(HttpHeaders.CACHE_CONTROL, "no-store")
                .header("Pragma", "no-cache");
        if (result!=null && !result.isEmpty()) {
            rsb = rsb.entity(result);
        }
        if (status.equals(Status.UNAUTHORIZED)) {
            if (authScheme.equals(AuthenticationScheme.AUTH_SCHEME_BASIC)) {
                rsb.header("WWW-Authenticate", "Basic realm=\"IBM Information Server\"");
            } else {
                rsb.header("WWW-Authenticate", "Bearer realm=\"IBM Information Server\"");
            }
        }
        return rsb.build();
    
    }
  
    public static String getTraceAsString(Throwable throwable)
    {
        final StringWriter sWriter = new StringWriter();
        final PrintWriter pWriter = new PrintWriter(sWriter);
        try {
            throwable.printStackTrace(pWriter);
            return sWriter.toString();
        }
        catch (final Exception e) {
            // Don't fail if we're unable to get a stack trace
            return "[<Failed to get Throwable stack trace: " + e.getMessage() + ">]";
        }
        finally {
            try {
                pWriter.close();
                sWriter.close();
            }
            catch (final Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
    }
    
}
