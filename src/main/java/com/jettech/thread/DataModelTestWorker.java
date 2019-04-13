package com.jettech.thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.domain.DataField;
import com.jettech.domain.DataModel;
import com.jettech.domain.ModelCaseModel;
import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResultItem;

import javafx.util.Pair;

public class DataModelTestWorker extends BaseTestWorker implements Runnable {

	private Logger logger = LoggerFactory.getLogger(BaseTestWorker.class);
	ModelCaseModel testCase;

	public DataModelTestWorker(BlockingQueue<ModelTestResultItem> itemQueue, ModelCaseModel modelCase) {
		super("",itemQueue);
		this.testCase = modelCase;
		this.testResultId = testCase.getTestResult().getId();
	}

	@Override
	public void run() {
		List<Pair<DataModel, DataModel>> modelPairList = testCase.getModelPairList();
		for (int i = 0; i < modelPairList.size(); i++) {
			Pair<DataModel, DataModel> pair = modelPairList.get(i);
			String leftModelName = pair.getKey().getName();
			String rightModelName = pair.getValue().getName();
			logger.info("doModelTest,left:" + leftModelName + " right:" + rightModelName);
			try {
				compareModel(pair.getKey(), pair.getValue());
			} catch (InterruptedException e) {
				logger.error("doModelTest error,left:" + leftModelName + " right:" + rightModelName, e);
			}
		}
	}

	private void compareModel(DataModel left, DataModel right) throws InterruptedException {
		if (!left.getName().equals(right.getName())) {
			modelItemQueue.put(createModelItem(left.getName(), "TableName", left.getName(), right.getName(), "TableNameNotSame"));
		}
		Map<String, DataField> leftFields = new HashMap<>();
		for (DataField field : left.getColumns()) {
			leftFields.put(field.getColumnName(), field);
		}
		Map<String, DataField> rightFields = new HashMap<>();
		for (DataField field : right.getColumns()) {
			rightFields.put(field.getColumnName(), field);
		}

		for (String fieldName : leftFields.keySet()) {
			// 左表中存在右表没有的字段
			if (!rightFields.containsKey(fieldName)) {
				itemQueue.put(this.createItem(left.getName(), fieldName, fieldName, null, "RightModelNotContianField"));
				continue;
			}
			DataField leftField = leftFields.get(fieldName);
			DataField rightField = rightFields.get(fieldName);
			// 数据类型
			if (leftField.getDataType() == null && rightField.getDataType() == null) {
			} else if (leftField.getDataType() == null || rightField.getDataType() == null) {
				itemQueue.put(createItem(left.getName(), fieldName, leftField.getDataType(), rightField.getDataType(),
						"DataFieldDataTypeNull"));
			} else if (!leftField.getDataType().equals(rightField.getDataType())) {
				itemQueue.put(createItem(left.getName(), fieldName, leftField.getDataType(), rightField.getDataType(),
						"DataFieldDataTypeNotSame"));
			}
			// 数据精度
			if (leftField.getScale() == null && rightField.getScale() == null) {
			} else if (leftField.getScale() == null || rightField.getScale() == null) {
				itemQueue.put(createItem(left.getName(), fieldName, leftField.getScale(), rightField.getScale(),
						"DataFieldDataScaleNull"));
			} else if (!leftField.getScale().equals(rightField.getScale())) {
				itemQueue.put(createItem(left.getName(), fieldName, leftField.getScale(), rightField.getScale(),
						"DataFieldDataScaleNotSame"));
			}
			// 数据长度
			if (leftField.getDataLength() == null && rightField.getDataLength() == null) {
			} else if (leftField.getDataLength() == null || rightField.getDataLength() == null) {
				itemQueue.put(createItem(left.getName(), fieldName, leftField.getDataLength(),
						rightField.getDataLength(), "DataFieldDataLengthNull"));
			} else if (!leftField.getDataLength().equals(rightField.getDataLength())) {
				itemQueue.put(createItem(left.getName(), fieldName, leftField.getDataType(), rightField.getDataType(),
						"DataFieldDataLengthNotSame"));
			}

		}

		// 右表中存在左表没有的字段
		for (String fieldName : rightFields.keySet()) {
			if (!leftFields.containsKey(fieldName)) {
				itemQueue.put(createItem(right.getName(), fieldName, fieldName, null, "LeftModelNotContianField"));
				continue;
			}
		}

	}

}
