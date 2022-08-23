/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */

package com.mit.ora.gcp.rest;


import javax.ws.rs.ApplicationPath;
import javax.ws.rs.core.Application;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ApplicationPath("/")
public class MigInsightsJAXRSApplication extends Application {
    private final static Logger LOGGER = LoggerFactory.getLogger(MigInsightsJAXRSApplication.class);
//    public static Set<Class<?>> getResourceClasses() {
//      Set<Class<?>> resources = new HashSet<>();
//      resources.add(InsightsBuildVersion.class);
//      resources.add(VersionResourceV1.class);
//      return resources;
//  }
}
