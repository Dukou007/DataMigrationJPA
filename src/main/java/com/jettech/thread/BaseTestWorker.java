package com.jettech.thread;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.domain.CaseModel;
import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResultItem;

public abstract class BaseTestWorker extends BaseWorker {
	protected Logger logger = LoggerFactory.getLogger(BaseTestWorker.class);
	static protected Map<String, String> numberType = new HashMap<String, String>();
	protected BlockingQueue<TestResultItem> itemQueue;
	protected BlockingQueue<ModelTestResultItem> modelItemQueue;
	protected CaseModel testCase;
	protected String caseName = null;


	public BaseTestWorker() {
		logger = LoggerFactory.getLogger(this.getClass());
		// 对有精度的数据类型
		numberType = NumberUtil.getNumberType();
	}

	public BaseTestWorker(BlockingQueue<TestResultItem> itemQueue) {
		this();
		this.itemQueue = itemQueue;
	}

	public BaseTestWorker(String name, BlockingQueue<ModelTestResultItem> modelItemQueue) {
		this();
		this.modelItemQueue = modelItemQueue;
	}

	protected ModelTestResultItem createModelItem(String keyValue, String colName, Object sourceValue,
	        Object targetValue, String result) {
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
