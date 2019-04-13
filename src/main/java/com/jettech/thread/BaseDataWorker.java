package com.jettech.thread;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.AdapterFactory;
import com.jettech.domain.DbModel;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryModel;
import com.jettech.domain.DataField;

public abstract class BaseDataWorker {
	protected Logger logger = LoggerFactory.getLogger(BaseDataWorker.class);
	protected BlockingQueue<BaseData> queue = null;
	protected volatile boolean _isRunning = true;
	protected DbModel dataSource = null;
	// private static int _maxCount = 5;
	// private static int _threadHold = 100;
	AbstractAdapter adapter = null;
	// static Map<String, Object> duplicatedKeyMap = new HashMap<String,
	// Object>();
	// Map<String, Object> nullKeyMap = new HashMap<String, Object>();
	protected int nullOrEmptyKeyCount = 0;
	protected int duplicatedKeyCount = 0;
	protected QueryModel testQuery;
	protected Map<String, FieldModel> keyMap;

	/**
	 * 
	 * @param queue
	 *            结果消息队列
	 * @param testQuery
	 *            查询模型对象
	 * @throws Exception
	 */
	public BaseDataWorker(BlockingQueue<BaseData> queue, QueryModel testQuery) throws Exception {
		if (queue == null) {
			throw new Exception("queue is null,create DataWorker failed");
		} else {
			this.queue = queue;
		}

		if (testQuery == null) {
			throw new Exception("testQuery is null,create DataWorker failed");
		} else {
			this.testQuery = testQuery;
			this.dataSource = testQuery.getDataSource();
		}

		String testQueryName = testQuery.getName();
		if (testQuery.getDataSource() == null) {
			throw new Exception("testQuery[" + testQueryName + "]'s DataSource is null,create DataWorker failed");
		}

		this.keyMap = getKeyMap(this.testQuery);

		String dataSourceName = testQuery.getDataSource().getName();
		this.adapter = AdapterFactory.create(dataSource.getDatabaseType());
		if (this.adapter == null) {
			String info = "testQuery:[" + testQueryName + "] dataSource[" + dataSourceName + "] create adapter failed.";
			throw new Exception(info);
		}

		this.nullOrEmptyKeyCount = 0;
		this.duplicatedKeyCount = 0;
	}

	protected Map<String, FieldModel> getKeyMap(QueryModel testQuery) {
		Map<String, FieldModel> keyMap = new HashMap<>();
		List<FieldModel> list = testQuery.getKeyFields();
		for (FieldModel field : list) {
			// 列名称统一转换为大写[这里根据不同数据库可能需要调整]
			keyMap.put(field.getName().toUpperCase().trim(), field);
		}
		if (keyMap.size() == 0) {
			String keyText = testQuery.getKeyText();
			keyText = keyText.replaceAll("，", ",");// 可能存在误输入中文逗号
			String[] keys = keyText.split(",");
			for (String key : keys) {
				// 列名称统一转换为大写[这里根据不同数据库可能需要调整]
				keyMap.put(key.toUpperCase().trim(), new FieldModel(key));
			}
		}
		logger.info("query:[" + testQuery.getName() + "]keys:[" + keyMap.keySet() + "]");
		return keyMap;
	}

	protected void getMixMaxKey(PageData page, Set<String> keySet) {
		List<String> list = new ArrayList<>();
		list.addAll(keySet);
		Collections.sort(list);
		page.setMinKey(list.get(0));
		page.setMaxKey(list.get(list.size() - 1));
	}

	protected String getDbInfo(DbModel ds) {
		String dbInfo = "DatabaseType:" + ds.getDatabaseType().name();
		dbInfo += " url:" + ds.getUrl();
		dbInfo += " userName:" + ds.getUsername();
		dbInfo += " password:" + ds.getPassword();
		return dbInfo;

	}

	protected String getQuerySQL() {
		String sql = testQuery.getSqlText();
		logger.info(String.format("Query:[%s]\r\n\t\t\t获取数据SQL:[%s]", this.testQuery.getName(), sql));
		return sql;
	}

	protected List<DataField> getQueryColumns(QueryModel testQuery, ResultSet rs) throws SQLException {
		// 获取查询中的列定义
		// List<TableColumn> list = new ArrayList<TableColumn>();
		Map<String, DataField> map = new HashMap<>();
		List<DataField> list = new ArrayList<>();
		ResultSetMetaData rsmd = rs.getMetaData();
		int ColNum = rsmd.getColumnCount();// 取得列的数量
		for (int i = 1; i <= ColNum; i++) {
			DataField col = new DataField();
			col.setTableName(rsmd.getTableName(i));// 表名称
			col.setColumnName(rsmd.getColumnLabel(i));// 名称
			col.setLabel(rsmd.getColumnLabel(i));// 标签
			col.setDataType(rsmd.getColumnTypeName(i));// 数据类型名称
			col.setDataLength(rsmd.getPrecision(i));// 长度？
			col.setScale(rsmd.getScale(i));// 小数精度?
			// System.out.println("getSchemaName" + rsmd.getSchemaName(i));//数据库
			// System.out.println("getCatalogName" +
			// rsmd.getCatalogName(i));//数据库
			// System.out.println("getColumnType" +
			// rsmd.getColumnType(i));//数据类型(数值)
			// System.out.println("getColumnClassName" +
			// rsmd.getColumnClassName(i));//转到java后的数据类型
			// list.add(col);
			map.put(col.getColumnName().toUpperCase(), col);
			list.add(col);
		}
		if (testQuery != null) {
			testQuery.setQueryColumns(list);
		}

		// 如果查询列未定义,将SQL执行的列转换为查询列定义
		if (testQuery.getTestFields() == null || testQuery.getTestFields().size() == 0) {
			List<FieldModel> fields = new ArrayList<>();
			for (DataField col : list) {
				FieldModel field = new FieldModel(col);
				fields.add(field);
			}
			testQuery.setTestFields(fields);
		}

		// 如果Key列未定义,将查询的列定义转换为Key列定义
		if (testQuery.getKeyFields() == null || testQuery.getKeyFields().size() == 0) {
			List<FieldModel> fields = new ArrayList<>();
			String keys[] = testQuery.getKeyText().split(",");
			for (String key : keys) {
				DataField col = map.get(key.toUpperCase());
				if (col != null) {
					fields.add(new FieldModel(col));
				}
			}
			testQuery.setKeyFields(fields);
		}

		return list;
	}
}
