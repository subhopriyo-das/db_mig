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

@Schema(description = "TablesGetMetadataResponse")
public class TablesGetMetadataResponse {
    
    public TablesGetMetadataResponse(String workflow_name, String total_count) {
        super();
        this.workflow_name = workflow_name;
        this.total_count = total_count;
    }

    @Schema(required = true, description = "workflow_name")
    private String workflow_name;

    @Schema(required = true, description = "total_count")
    private String total_count;
    
    public String getWorkflow_name() {
        return workflow_name;
    }

    public void setWorkflow_name(String workflow_name) {
        this.workflow_name = workflow_name;
    }

    public String getTotal_count() {
        return total_count;
    }

    public void setTotal_count(String total_count) {
        this.total_count = total_count;
    }
    

}
