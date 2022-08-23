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

@Schema(description = "TablesGetEntityResponse")
public class TablesGetEntityResponse {
    
    public TablesGetEntityResponse(String test) {
        
    }

    @Schema(required = true, description = "test")
    private String test;

	public String getTest() {
		return test;
	}

	public void setTest(String test) {
		this.test = test;
	}
    
    

}
