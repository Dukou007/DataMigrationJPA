package com.jettech.thread;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
import com.jettech.entity.TestResultItem;
import com.jettech.util.StringUtil;
import com.jettech.domain.CaseModel;
import com.jettech.domain.DataField;

public abstract class BaseDataWorker extends BaseWorker {
	protected Logger logger = null;
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
	BlockingQueue<TestResultItem> itemQueue;

	public BaseDataWorker() {
		logger = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 
	 * @param queue
	 *            结果消息队列
	 * @param testQuery
	 *            查询模型对象
	 * @throws Exception
	 */
	public BaseDataWorker(BlockingQueue<BaseData> queue, QueryModel testQuery) throws Exception {
		this();
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

	public BaseDataWorker(BlockingQueue<BaseData> queue, BlockingQueue<TestResultItem> itemQueue, QueryModel testQuery,
	        CaseModel testCase) throws Exception {
		this(queue, testQuery);
		this.itemQueue = itemQueue;
		this.testResultId = testCase.getTestResult().getId();
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
		if (StringUtil.isNumeric(list)) {
			// 当key的值全是数值时用下面方法比较
			Collections.sort(list, new Comparator<String>() {
				public int compare(String left, String right) {
					if (left == null)
						return -1;
					if (right == null)
						return 1;
					if (left.length() < right.length())
						return -1;
					if (left.length() > right.length())
						return 1;
					return left.compareTo(right);
				}
			});
		} else {
			// 当key的值不全是数值时按字符类型比较
			Collections.sort(list);
		}
		page.setMinKey(list.get(0));
		page.setMaxKey(list.get(list.size() - 1));

	}

	/**
	 * key有可能是多个值，按“|”把每个key分割开，根据key在String[]中的位置放在不同的list中，
	 * 对每个list中的元素排序，后续在targetSQL中按条件查询时，分别添加每个key的最大值和最小值
	 * 
	 * @param page
	 * @param ArrayList
	 */
	protected void getMixMaxKey2(PageData page, ArrayList<List<String>> arrayList, int k) {
		// 因为key既可以是数值又可以是字符类型，单纯转换成字符串类型排序结果会有问题，所以要重写比较规则

		List<String> list = arrayList.get(k);

		if (StringUtil.isNumeric(list)) {
			// 当key的值全是数值时用下面方法比较
			/*
			 * 当数据库中被选做key的那列(数值)有空值(NULL)的时候查询SQL会报错
			 */
			Collections.sort(list, new Comparator<String>() {
				public int compare(String left, String right) {
					if (left == null)
						return -1;
					if (right == null)
						return 1;
					if (left.length() < right.length())
						return -1;
					if (left.length() > right.length())
						return 1;
					return left.compareTo(right);
				}
			});
		} else {
			// 当key的值不全是数值时按字符类型比较
			Collections.sort(list);
		}
		page.setMinKey(list.get(0));
		page.setMaxKey(list.get(list.size() - 1));

	}

	/**
	 * 对多个key生成的keySet进行重新组合，使每个list中的元素都是相同类型的内容
	 * 
	 * @param keySets
	 * @return
	 */
	protected ArrayList<List<String>> generateKeyList(Set<String> keySets) {
		ArrayList<List<String>> arrayList = new ArrayList<>();
		for (String keySet : keySets) {
			String[] keys = keySet.split("\\|");
			int len = keys.length;
			if (arrayList.isEmpty()) {
				for (int i = 0; i < len; i++) {
					List<String> list = new ArrayList<>();
					arrayList.add(list);
				}
			}
			for (int i = 0; i < len; i++) {
				arrayList.get(i).add(keys[i]);
			}
		}
		System.out.println("新的key值集合：" + arrayList);
		return arrayList;
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
		logger.info(String.format("Query:[%s],获取数据SQL:[%s]", this.testQuery.getName(), sql));
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
			col.setDataLength(rsmd.getPrecision(i));// 长度？数据类型不同可能不同
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
