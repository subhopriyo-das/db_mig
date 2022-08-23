/**
 * 
 */
package com.mit.ora.gcp.store.db.sql;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.io.IOUtils;
import org.apache.wink.json4j.JSONException;
import org.apache.wink.json4j.JSONObject;

import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserManager;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.util.TablesNamesFinder;

/**
 * @author Shaik M Nawaz
 *
 */
public class QueryParser {

	public static Map<String, String> tableMap = new HashMap<String, String>();
	public static Map<String, String> colsMap = new HashMap<String, String>();
	public static Map<String, String> tableColsMap = new HashMap<String, String>();

	public static void main(String[] args) throws Exception {

		JSONObject resp = new JSONObject();

		FileInputStream is = new FileInputStream(args[0]);
		String sql = IOUtils.toString(is, StandardCharsets.UTF_8);
		System.out.println("input query : " + sql);

		List<String> tables = findTables(sql);
		resp.put("tables", tables);

		Set<String> columns = findColumns(sql);
		resp.put("columns", columns);

		Map<String, String> alias = findAlias(sql);
		resp.put("alias", alias);

		String path = "response.json";

		try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
			out.write(resp.toString());
		} catch (Exception e) {
			e.printStackTrace();
		}

	}
	
	public JSONObject ProcessSQL(String sql) throws JSONException, JSQLParserException {

		JSONObject resp = new JSONObject();

//		FileInputStream is = new FileInputStream(args[0]);
//		String sql = IOUtils.toString(is, StandardCharsets.UTF_8);
//		System.out.println("input query : " + sql);

		List<String> tables = findTables(sql);
		resp.put("tables", tables);

		Set<String> columns = findColumns(sql);
		resp.put("columns", columns);

		Map<String, String> alias = findAlias(sql);
		resp.put("alias", alias);

//		String path = "response.json";
//
//		try (PrintWriter out = new PrintWriter(new FileWriter(path))) {
//			out.write(resp.toString());
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
		return resp;

	}

	private static List<String> findTables(String inpSql) throws JSQLParserException {

		CCJSqlParserManager parseSql = new CCJSqlParserManager();
		Statement stmnt = parseSql.parse(new StringReader(inpSql));
		TablesNamesFinder tablesNamesFinder = new TablesNamesFinder();
		return tablesNamesFinder.getTableList(stmnt);

	}

	private static Map<String, String> findAlias(String inpSql) throws JSQLParserException {

		Statement statement = CCJSqlParserUtil.parse(inpSql);
		Select selectStatement = (Select) statement;
		// Replace table names
		CustomTableNamesFinder finder = new CustomTableNamesFinder();
		List<String> tableList = finder.getTableList(selectStatement);
		Map<String, String> tableAliases = finder.getTableAliases();
		return tableAliases;

	}

	private static Set<String> findColumns(String inpSql) throws JSQLParserException {

		Statement statement = CCJSqlParserUtil.parse(inpSql);
		Select selectStatement = (Select) statement;
		// Replace table names
		CustomTableNamesFinder finder = new CustomTableNamesFinder();
		finder.getTableList(selectStatement);
		Set<String> sourceColumns = finder.getColumns();
		return sourceColumns;

	}

//	private static void loadMappingFile(String fileName) {
//		try {
//			FileInputStream is = new FileInputStream(fileName);
//			XSSFWorkbook workbook = new XSSFWorkbook(is);
//			XSSFSheet sheet = workbook.getSheetAt(0);
//			Iterator<Row> rowIterator = sheet.iterator();
//			while (rowIterator.hasNext()) {
//				Row next = rowIterator.next();
//				if (next.getRowNum() == 0) {
//					// Skip the first row as header
//					continue;
//				}
//				String sourceTable = next.getCell(0).getStringCellValue().trim().toLowerCase();
//				String targetTable = next.getCell(2).getStringCellValue().trim().toLowerCase();
//				String sourceColumn = next.getCell(1).getStringCellValue().trim().toLowerCase();
//				String targetColumn = next.getCell(3).getStringCellValue().trim().toLowerCase();
//
//				tableMap.put(sourceTable, targetTable);
//				colsMap.put(sourceColumn, targetColumn);
//				tableColsMap.put(sourceTable + "." + sourceColumn, targetTable + "." + targetColumn);
//			}
//			is.close();
//			workbook.close();
//		} catch (FileNotFoundException e) {
//			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//	}

	private static Map<String, String> parseInputQuery(String inputQuery) {
		String outputQuery = inputQuery;
		StringBuilder errorMessage = new StringBuilder();

		Map<String, String> resultMap = new HashMap<String, String>();

		try {
			Statement statement = CCJSqlParserUtil.parse(inputQuery);
			Select selectStatement = (Select) statement;

			// Replace table names
			CustomTableNamesFinder finder = new CustomTableNamesFinder();
			List<String> tableList = finder.getTableList(selectStatement);
			for (String table : tableList) {
				String targetTable = tableMap.get(table);
				if (targetTable == null || targetTable.isEmpty()) {
					errorMessage.append("Target table is empty for source table : " + table + "\r\n");
				} else {
					String pattern = "(?<![a-zA-Z0-9_.])" + Pattern.quote(table) + "(?![a-zA-Z0-9_.])";
					outputQuery = outputQuery.replaceAll("\\b" + pattern + "\\b", targetTable);
				}
			}
			Map<String, String> tableAliases = finder.getTableAliases();

			// Replace column names
			Set<String> sourceColumns = finder.getColumns();
			for (String sourceColumn : sourceColumns) {
				String aliasColumn = "";
				String alias = getAlias(sourceColumn);
				if (tableAliases.containsKey(alias)) {
					aliasColumn = sourceColumn;
					String actualTable = tableAliases.get(alias);
					if (actualTable != null && !actualTable.isEmpty()) {
						sourceColumn = sourceColumn.replaceFirst(alias, actualTable);
					}
				}
				String targetCol = colsMap.get(sourceColumn);
				if (targetCol == null || targetCol.isEmpty()) {
					targetCol = tableColsMap.get(sourceColumn);
					if (targetCol == null || targetCol.isEmpty()) {
						errorMessage.append("Target column is empty for source column : " + sourceColumn + "\r\n");
					}
				}
				if (targetCol != null) {
					if (!aliasColumn.isEmpty()) {
						outputQuery = outputQuery.replaceAll("\\b" + aliasColumn + "\\b", targetCol);
					} else {
						outputQuery = outputQuery.replaceAll("\\b" + sourceColumn + "\\b", targetCol);
					}
				}
			}

		} catch (JSQLParserException e) {
			outputQuery = "";
			errorMessage.append("Error while parsing query: " + e.getLocalizedMessage());

		} finally {
			resultMap.put("OUTPUTQUERY", outputQuery);
			resultMap.put("ERRORMESSAGE", errorMessage.toString());
		}
		return resultMap;
	}

	private static String getAlias(String sourceColumn) {
		String[] split = sourceColumn.split("\\.");
		if (split != null && split.length > 0) {
			return split[0];
		}
		return null;
	}
}
