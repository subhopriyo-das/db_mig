/* 
 * Author : Shaik Magdhum Nawaz
 * Email : shaik.nawaz@mastechinfotrellis.com
 * 
 * Mastech InfoTrellis Confidential
 * Copyright InfoTrellis India Pvt. Ltd. 2021
 * The source code for this program is not published.
 */


package com.mit.ora.gcp.integration.test;
//
//import static org.junit.Assert.assertEquals;
//
//import java.io.IOException;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.apache.wink.json4j.JSONArray;
//import org.apache.wink.json4j.JSONException;
//import org.apache.wink.json4j.JSONObject;
//import org.junit.Test;
//
//
//public class ConstraintsGenTest {
//
//	@Test
//	public void testConstraintGenFromDB2() throws ConstraintAPIException {
//		OpenJDBCConnectionDetails openJDBCConnectionDetails = new OpenJDBCConnectionDetails("DB2");
//		openJDBCConnectionDetails.setHost("dashdb-txn-sbox-yp-dal09-03.services.dal.bluemix.net");
//		openJDBCConnectionDetails.setPort("50000");
//		openJDBCConnectionDetails.setUsername("fgt42286");
//		openJDBCConnectionDetails.setPassword("7hfs6cbr6m9+tztz");
//		openJDBCConnectionDetails.setDbName("BLUDB");
//		openJDBCConnectionDetails.setTableName("BANK_MARKET");
//		openJDBCConnectionDetails.setSchemaName("FGT42286");
//		ArrayList<Column> columns = ConstraintsGen.readFromDB(openJDBCConnectionDetails,null);
//
//		for (Column col : columns)
//			col.setDatatype(Datatype.getDatatype(col.getData()));
//		List<ColumnConstraints> colConstraints = ConstraintsGen.getColumnConstraints(columns);
//		List<MultiColumnConstraints> multiConstraints = Associations.getMultiColumnConstraints(columns);
//		Constraints cons = new Constraints();
//		cons.setFileID("BANK_MARKET");
//		cons.setNumColumns(columns.size());
//		cons.setNumRows(columns.get(0).getData().size());
//		cons.setColumns(colConstraints);
//		cons.setAssociations(multiConstraints);
//		assertEquals(18,colConstraints.size());
//		assertEquals(405, multiConstraints.size());
//
//	}
//
//	@Test
//	public void testConstraintAsJSON() throws ConstraintAPIException, IOException, JSONException, InsightsException {
//		OpenJDBCConnectionDetails openJDBCConnectionDetails = new OpenJDBCConnectionDetails("DB2");
//		openJDBCConnectionDetails.setHost("dashdb-txn-sbox-yp-dal09-03.services.dal.bluemix.net");
//		openJDBCConnectionDetails.setPort("50000");
//		openJDBCConnectionDetails.setUsername("fgt42286");
//		openJDBCConnectionDetails.setPassword("7hfs6cbr6m9+tztz");
//		openJDBCConnectionDetails.setDbName("BLUDB");
//		openJDBCConnectionDetails.setTableName("BANK_MARKET");
//		openJDBCConnectionDetails.setSchemaName("FGT42286");
//		String cons = ConstraintsGen.getConstraintsJson(openJDBCConnectionDetails, null,"BANK_MARKET", null);
//		JSONObject jo = new JSONObject(cons);
//		int nbrOfRows = jo.getInt("num_rows");
//		assertEquals(45211, nbrOfRows);
//		JSONArray ja = (JSONArray) jo.get("columns");
//		assertEquals(18,ja.size());
//		JSONObject col0 = (JSONObject) ja.get(0);
//		String datatype  = (String) col0.get("datatype");
//		assertEquals("numeric",datatype);
//		double mean = (Double) col0.get("mean");
//		assertEquals(22606,mean, 0.0);
//		String colName = (String) col0.get("col_name");
//		assertEquals("Cust_num",colName);
//		ja = (JSONArray) jo.get("associations");
//		assertEquals(405, ja.size());
//
//	}
//
//
//	@Test
//	public void testDB2Reader() throws ConstraintAPIException, SQLException {
//		DataReader dataReader = null;
//		try {
//			OpenJDBCConnectionDetails openJDBCConnectionDetails = new OpenJDBCConnectionDetails("DB2");
//			openJDBCConnectionDetails.setHost("dashdb-txn-sbox-yp-dal09-03.services.dal.bluemix.net");
//			openJDBCConnectionDetails.setPort("50000");
//			openJDBCConnectionDetails.setUsername("fgt42286");
//			openJDBCConnectionDetails.setPassword("7hfs6cbr6m9+tztz");
//			openJDBCConnectionDetails.setDbName("BLUDB");
//			openJDBCConnectionDetails.setTableName("BANK_MARKET");
//			openJDBCConnectionDetails.setSchemaName("FGT42286");
//			dataReader = DataReader.getInstance(openJDBCConnectionDetails);
//			assertEquals(18, dataReader.getNumberOfColumns());
//			assertEquals(45211, dataReader.getNumberOfRows());
//			List<String> row = dataReader.next();
//			assertEquals("1", row.get(0));
//			assertEquals("58", row.get(1));
//		} finally {
//			if(dataReader != null)
//				dataReader.close();
//		}
//		
//	}
//
//}