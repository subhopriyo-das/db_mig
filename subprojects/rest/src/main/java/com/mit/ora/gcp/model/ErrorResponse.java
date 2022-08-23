/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "ErrorResponse")
public class ErrorResponse {

    public ErrorResponse(String trace, String message) {
        super();
        this.trace = trace;
        this.message = message;
    }

    @Schema(required = true, description = "trace")
    private String trace;
    
    @Schema(required = true, description = "message")
    private String message;

    public String getTrace() {
        return trace;
    }

    public void setTrace(String trace) {
        this.trace = trace;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }


}
