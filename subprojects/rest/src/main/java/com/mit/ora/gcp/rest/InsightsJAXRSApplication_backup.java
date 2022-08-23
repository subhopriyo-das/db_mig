///* 
// * Author : Shaik Magdhum Nawaz
// * Email : shaik.nawaz@mastechinfotrellis.com
// * 
// * Mastech InfoTrellis Confidential
// * Copyright InfoTrellis India Pvt. Ltd. 2021
// * The source code for this program is not published.
// */
//
//package com.mit.ora.gcp.rest;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import javax.ws.rs.ApplicationPath;
//import javax.ws.rs.core.Application;
//
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import com.mit.ora.gcp.model.SwaggerConstants;
//import com.mit.ora.gcp.rest.resources.InsightsBuildVersion;
//import com.mit.ora.gcp.rest.resources.VersionResourceV3;
//
//import io.swagger.annotations.Info;
//import io.swagger.annotations.SwaggerDefinition;
//import io.swagger.annotations.Tag;
//
//@SwaggerDefinition(info = @Info(title = "Insights API v3", version = "0.0.3", description = "Provides insights on data quality"), tags = {
//    @Tag(name = SwaggerConstants.INSIGHTS_API_VERSION_V3, description = "APIs for Insights version."),
//    @Tag(name = SwaggerConstants.INSIGHTS_HEALTH_V3, description = "APIs for Insights Service Health Status."),
//    @Tag(name = SwaggerConstants.INSIGHTS_BUILD_VERSION, description = "APIs for Insights Build Number."),
//    @Tag(name = SwaggerConstants.INSIGHTS_SCA, description = "APIs for Similar Columns Analysis."),
//    @Tag(name = SwaggerConstants.INSIGHTS_MODEL, description = "APIs for Model Management."),
//    @Tag(name = SwaggerConstants.INSIGHTS_ANOMALY_DETECTION, description = "APIs for Anomaly Detection."),
//    @Tag(name = SwaggerConstants.INSIGHTS_DRD, description = "APIs for Data Rule Definitions."),
//    @Tag(name = SwaggerConstants.INSIGHTS_TASK, description = "APIs for Task Management.")
//})
//@ApplicationPath("/v3/insights")
//public class InsightsJAXRSApplication_backup extends Application {
//
//    private final static Logger LOGGER = LoggerFactory.getLogger(InsightsJAXRSApplication_backup.class);
//
//    
//    
//    public static Set<Class<?>> getResourceClasses() {
//        Set<Class<?>> resources = new HashSet<>();
//        resources.add(VersionResourceV3.class);
////        resources.add(InsightsHealthInterfaceV3.class);
//        resources.add(InsightsBuildVersion.class);
////        resources.add(SimilarColumnRestInterface.class);
////        resources.add(ModelManagementRestInterface.class);
////        resources.add(DataRuleDefinitionRestInterface.class);
////        resources.add(TaskManagementRestInterface.class);
////        
////        resources.add(AuthenticationFeature.class);
////        resources.add(AuthorizationFeature.class);
////        resources.add(ExceptionHandlersFeature.class);
////        resources.add(SerializationFeature.class);
//        return resources;
//    }
//
//}
