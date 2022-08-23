/**
 * 
 */
package com.mit.ora.gcp.store.db.sql;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.sf.jsqlparser.schema.Column;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.util.TablesNamesFinder;

/**
 * @author Shaik M Nawaz
 *
 */
public class CustomTableNamesFinder extends TablesNamesFinder {

	private Map<String, List<String>> tableColumnNames = new HashMap<String, List<String>>();

	private Map<String, String> tableAliases = new HashMap<String, String>();

	private Set<String> columns = new HashSet<String>();

	@Override
	public void visit(Column tableColumn) {

		Table table = tableColumn.getTable();
		if (table != null) {
			String tableName = table.toString();
			if (!tableColumnNames.containsKey(tableName)) {
				List<String> colNames = new ArrayList<String>();
				colNames.add(tableColumn.getColumnName());
				tableColumnNames.put(tableColumn.getTable().toString(), colNames);
			} else {
				tableColumnNames.get(tableName).add(tableColumn.getColumnName());
			}
		}

		columns.add(tableColumn.toString());
	}

	@Override
	public void visit(Table tableName) {
		super.visit(tableName);
		if (tableName.getAlias() != null && tableName.getName() != null) {
			tableAliases.put(tableName.getAlias().getName(), tableName.getName());
		}
	}

	public Map<String, List<String>> getTableColumnNames() {
		return this.tableColumnNames;
	}

	public Map<String, String> getTableAliases() {
		return this.tableAliases;
	}

	public Set<String> getColumns() {
		return this.columns;
	}
}
