package com.jettech.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.domain.CaseModel;
import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResultItem;

public class BaseTestWorker {
	protected Logger logger = LoggerFactory.getLogger(BaseTestWorker.class);
	static protected Map<String, String> numberType = new HashMap<String, String>();
	protected BlockingQueue<TestResultItem> itemQueue;
	protected BlockingQueue<ModelTestResultItem> modelItemQueue;
	protected CaseModel testCase;
	protected String caseName = null;
	protected Integer testResultId;
	public BaseTestWorker() {
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

	public BaseTestWorker(BlockingQueue<TestResultItem> itemQueue) {
		this();
		this.itemQueue = itemQueue;
	}
	public BaseTestWorker(String name,BlockingQueue<ModelTestResultItem> modelItemQueue) {
		this();
		this.modelItemQueue = modelItemQueue;
	}

	protected TestResultItem createItem(String keyValue, String colName, Object sourceValue, Object targetValue,
	        String result) {
		TestResultItem item = new TestResultItem();
		item.setKeyValue(keyValue);
		item.setResult(result);
		item.setColumnName(colName);
		item.setSourceValue(sourceValue == null ? "null" : sourceValue.toString());
		item.setTargetValue(targetValue == null ? "null" : targetValue.toString());
		item.setTestResultId(testResultId);
		return item;
	}

	protected TestResultItem createItem(String keyValue, String colName, Object sourceValue, String result) {
		return createItem(keyValue, colName, sourceValue, null, result);
	}

	protected TestResultItem createItem(String keyValue, String result) {
		return createItem(keyValue, null, null, result);
	}
	protected ModelTestResultItem createModelItem(String keyValue, String colName, Object sourceValue, Object targetValue,
			String result) {
		ModelTestResultItem item = new ModelTestResultItem();
		item.setKeyValue(keyValue);
		item.setResult(result);
		item.setColumnName(colName);
		item.setSoruceValue(sourceValue == null ? "null" : sourceValue.toString());
		item.setTragetValue(targetValue == null ? "null" : targetValue.toString());
		item.setTestResultId(testResultId);
		return item;
	}
	
	protected ModelTestResultItem createModelItem(String keyValue, String colName, Object sourceValue, String result) {
		return createModelItem(keyValue, colName, sourceValue, null, result);
	}
	
	protected ModelTestResultItem createModelItem(String keyValue, String result) {
		return createModelItem(keyValue, null, null, result);
	}
}
