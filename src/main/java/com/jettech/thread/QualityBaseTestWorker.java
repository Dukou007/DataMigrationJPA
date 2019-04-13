package com.jettech.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.jettech.entity.QualityTestResultItem;
import com.jettech.entity.TestResultItem;

public class QualityBaseTestWorker {
	static protected Map<String, String> numberType = new HashMap<String, String>();
	protected BlockingQueue<QualityTestResultItem> itemQueue;
	protected Integer qualityTestResultId;
	public QualityBaseTestWorker() {
		// 对有精度的数据类型
		numberType.clear();
		// numberType.put("SMALLINT", "SMALLINT");
		// numberType.put("INTEGER", "INTEGER");
		numberType.put("BINARY_FLOAT", "BINARY_FLOAT");
		numberType.put("BINARY_DOUBLE", "BINARY_DOUBLE");
		numberType.put("FLOAT", "FLOAT");
		numberType.put("SMALLFLOAT", "SMALLFLOAT");
		numberType.put("DECIMAL", "DECIMAL");
		numberType.put("MONEY", "MONEY");
		numberType.put("NUMBER", "NUMBER");
		numberType.put("DOUBLE", "DOUBLE");
		numberType.put("REAL", "REAL");

	}

	public QualityBaseTestWorker(BlockingQueue<QualityTestResultItem> itemQueue) {
		this();
		this.itemQueue = itemQueue;
	}

	protected QualityTestResultItem createItem(String selectValue, String colName,
	        String result) {
		QualityTestResultItem item = new QualityTestResultItem();
		item.setSelectValue(selectValue);
		item.setResult(result);
		item.setColumnName(colName);
		item.setTestResultId(qualityTestResultId);
		return item;
	}

	/*protected QualityTestResultItem createItem(String selectValue, String colName, Object sourceValue, String result) {
		return createItem(selectValue, colName, sourceValue, null, result);
	}

	protected QualityTestResultItem createNewResultItem(String keyValue, String result) {
		return createItem(keyValue, null, null, result);
	}*/
}
