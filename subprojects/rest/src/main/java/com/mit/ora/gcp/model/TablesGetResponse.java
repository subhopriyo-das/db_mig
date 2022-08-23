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

@Schema(description = "TablesGetResponse")
public class TablesGetResponse {

    public TablesGetResponse(TablesGetMetadataResponse metadata, TablesGetEntityResponse entity) {
        super();
        this.metadata = metadata;
        this.entity = entity;
    }

    @Schema(required = true, description = "metadata")
    private TablesGetMetadataResponse metadata;
    
    @Schema(required = true, description = "entity")
    private TablesGetEntityResponse entity;

    public TablesGetMetadataResponse getMetadata() {
        return metadata;
    }

    public void setMetadata(TablesGetMetadataResponse metadata) {
        this.metadata = metadata;
    }

    public TablesGetEntityResponse getEntity() {
        return entity;
    }

    public void setEntity(TablesGetEntityResponse entity) {
        this.entity = entity;
    }

}
