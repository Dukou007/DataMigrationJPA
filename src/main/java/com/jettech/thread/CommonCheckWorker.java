package com.jettech.thread;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.domain.FieldModel;
import com.jettech.domain.QualityQueryModel;
import com.jettech.domain.QualityResultModel;
import com.jettech.domain.QualityTestCaseModel;
import com.jettech.domain.QueryModel;
import com.jettech.entity.QualityTestResultItem;
import com.jettech.entity.TestResultItem;

public class CommonCheckWorker implements Runnable {

	private Logger logger = LoggerFactory.getLogger(CommonCheckWorker.class);
	static private Map<String, String> numberType = new HashMap<String, String>();
	private BlockingQueue<QualityBaseData> queue;

	private BlockingQueue<QualityTestResultItem> itemQueue;
	private Integer qualityTestResultId;
	private Integer MaxWaitTime = 600;
	private QualityTestCaseModel testCase;

	public CommonCheckWorker(BlockingQueue<QualityBaseData> queue, BlockingQueue<QualityTestResultItem> itemQueue,
			QualityTestCaseModel testCase) {
		this.queue = queue;
		this.itemQueue = itemQueue;
		this.testCase = testCase;
		this.qualityTestResultId = testCase.getTestResult().getId();

		numberType.clear();
		numberType.put("SMALLINT", "SMALLINT");
		numberType.put("INTEGER", "INTEGER");
		numberType.put("FLOAT", "FLOAT");
		numberType.put("SMALLFLOAT", "SMALLFLOAT");
		numberType.put("DECIMAL", "DECIMAL");
		numberType.put("MONEY", "MONEY");
		numberType.put("NUMBER", "NUMBER");
	}

	@Override
	public void run() {
		logger.info("启动消费者线程！");
		int waitCount = 0;
		QualityCommonData data = null;
		try {
			QualityResultModel testResult = testCase.getTestQualityResult();
			while (true) {
				logger.info("正从队列获取数据...");
				if (data == null)
					data = (QualityCommonData) queue.poll(100, TimeUnit.MICROSECONDS);

				if (null != data && null != data) {// 有数据时直接从队列的队首取走，无数据时阻塞，在2s内有数据取走
					logger.info("拿到数据：" + data.getMap().size());
					testResult.setDataCount(data.getMap().size());
					// this.dataCompare(sourceData, targetData, itemQueue);//
					// 数据比对
					// doCompare(sourceData, targetData, itemQueue);
					doCheck(data, itemQueue);
					queue.remove(data);// 获取到队首数据后从队列中删掉

					logger.info("队首源数据被消费Data：" + data.getMap().size());
					logger.info("对比结束");
					break;
				} else {// 超过2s还没数据，认为所有生产线程都已经退出，自动退出消费线程。
					Thread.sleep(100);
					waitCount = waitCount + 1;
					if (waitCount > MaxWaitTime) {
						break;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("数据比对异常！！！");
			Thread.currentThread().interrupt();
		} finally {
			logger.info("退出消费者线程！");
			Thread.currentThread().interrupt();
		}
	}

	private void doCheck(QualityCommonData data, BlockingQueue<QualityTestResultItem> itemQueue) {
		Map<String, List<Object>> dataMap = data.getMap();
		QualityQueryModel query = data.getTestQuery();
		Integer maxItemRows = null;
		Integer itemCount = 0;
		if (testCase.getMaxResultRows() != null && testCase.getMaxResultRows() > 0) {
			maxItemRows = testCase.getMaxResultRows();
		}
		for (String keyValue : dataMap.keySet()) {
			List<Object> values = dataMap.get(keyValue);
			for (int i = 0; i < query.getTestFields().size(); i++) {
				FieldModel field = query.getTestFields().get(i);
				String dataType = field.getDataType();
				if (numberType.containsKey(dataType.toUpperCase())) {

				} else {

				}
				// 无其它监查规则的情况下，直接将查询的结果写如结果明细中
				itemCount++;
				itemQueue.add(createItem(keyValue, field.getName(), values.get(i), null, "failed value"));
			}
			if (maxItemRows != null && itemCount > maxItemRows) {
				logger.info("测试被中断,结果明细的数量:" + itemCount + " 超过了最大结果数量限制:" + maxItemRows);
				break;
			}
		}
	}

	/*private void doCompare(CommonData sourceData, CommonData targetData, BlockingQueue<TestResultItem> itemQueue) {
		Map<String, List<Object>> sourceMap = sourceData.getMap();
		// TestQueryService testQueryService =
		// SpringUtils.getBean(TestQueryServiceImpl.class);
		// TestQuery sourceQuery =
		// testQueryService.getOneById(sourceData.getTestQueryId());
		QueryModel sourceQuery = sourceData.getTestQuery();
		Map<String, List<Object>> targetMap = targetData.getMap();
		// TestQuery targetQuery =
		// testQueryService.getOneById(targetData.getTestQueryId());
		QueryModel targetQuery = targetData.getTestQuery();
		long sameRow = 0;// 相同行数
		long notSameData = 0;
		// long notExistDataInDest = 0;
		// long notExistInSource = 0;
		// long notExistKeyInDest = 0;
		// long currentRow = 0;

		// Iterator<Map.Entry<String, List<Object>>> sourceEntries =
		// sourceMap.entrySet().iterator();
		for (String keyValue : sourceMap.keySet()) {
			if (targetMap.containsKey(keyValue)) {
				List<Object> sourceValues = sourceMap.get(keyValue);
				List<Object> targetValues = targetMap.get(keyValue);
				int sameDataInRow = 0;
				// 当使用简易模式(不设置比较的字段)时,以下需要特殊处理

				// 对于每个列目标是是否具有相同的值
				for (int i = 0; i < sourceQuery.getTestFields().size(); i++) {
					FieldModel sourceField = sourceQuery.getTestFields().get(i);
					Object sourceValue = sourceValues.get(i);
					// 判断目标的列数量是否足够
					if (targetValues.size() > i) {
						Object targetValue = targetValues.get(i);
						// String dataType =
						// targetQuery.getTestFields().get(i).getDataType();
						String dataType = sourceField.getDataType();

						if (sourceValue != null && targetValue != null) {
							if (numberType.containsKey(dataType.toUpperCase())) {
								// 如果时候数值类型,转换为decimal比较
								BigDecimal srcDec = new BigDecimal(sourceValue.toString());
								BigDecimal destDec = new BigDecimal(targetValue.toString());
								if (srcDec.compareTo(destDec) == 0) {
									sameDataInRow++;
								} else {
									itemQueue.add(createItem(keyValue, sourceField.getName(), sourceValue, targetValue,
									        "not same value"));
									// this.append(keyValue,
									// col.getColumnName(),
									// "not same data,source:" + srcData +
									// ",dest:" + destData);
									notSameData++;
								}
							} else {
								// 其它类型转换为字符串比较
								if (!sourceValue.toString().trim().equals(targetValue.toString().trim())) {
									itemQueue.add(createItem(keyValue, sourceField.getName(), sourceValue, targetValue,
									        "not same value"));
									notSameData++;
								} else {
									sameDataInRow++;
								}
							}
						} else if (sourceValue == null && targetValue == null) {
							// 目标数据和源数据都null
							sameDataInRow++;
						} else if (sourceValue == null && targetValue != null) {
							// 源数据null,目标数据非null
							itemQueue.add(createItem(keyValue, sourceField.getName(), sourceValue, targetValue,
							        "sourceValue is null"));
							notSameData++;
						} else if (targetValue == null && sourceValue != null) {
							// 源数据非null,目标数据null
							itemQueue.add(createItem(keyValue, sourceField.getName(), sourceValue, targetValue,
							        "targetValue is null"));
							notSameData++;
						}
					} else {
						itemQueue.add(createItem(keyValue, sourceField.getName(), sourceValue, "target not data"));
					}
				}
				if (sameDataInRow == targetQuery.getTestFields().size()) {
					sameRow++;
				}
			} else {
				itemQueue.add(createNewResultItem(keyValue, "target not keyvalue"));
			}
		}
		logger.info("compare end sameRow:" + sameRow + " notSameData:" + notSameData);
	}*/

	private QualityTestResultItem createItem(String selectValue, String colName, Object sourceValue, Object targetValue,
	        String result) {
		QualityTestResultItem item = new QualityTestResultItem();
		item.setSelectValue(selectValue);
		item.setResult(result);
		item.setColumnName(colName);
/*		item.setSoruceValue(sourceValue == null ? "null" : sourceValue.toString());
		item.setTragetValue(targetValue == null ? "null" : targetValue.toString());
*/		item.setTestResultId(qualityTestResultId);
		return item;
	}

	private QualityTestResultItem createItem(String keyValue, String colName, Object sourceValue, String result) {
		return createItem(keyValue, colName, sourceValue, null, result);
	}

	private QualityTestResultItem createNewResultItem(String keyValue, String result) {
		return createItem(keyValue, null, null, result);
	}

	/**
	 * 数据比对
	 * 
	 * @param data
	 */
	private void dataCompare(CommonData sourceData, CommonData targetData, BlockingQueue<TestResultItem> itemQueue) {
		Map<String, List<Object>> sourceMap = sourceData.getMap();
		Map<String, List<Object>> targetMap = targetData.getMap();
		Iterator<Map.Entry<String, List<Object>>> sourceEntries = sourceMap.entrySet().iterator();
		Iterator<Map.Entry<String, List<Object>>> targetEntries = targetMap.entrySet().iterator();

		while (sourceEntries.hasNext()) {
			Map.Entry<String, List<Object>> sourceEntry = sourceEntries.next();
			boolean keyFlag = false;
			boolean comFlag = false;

			TestResultItem tri = new TestResultItem();
			tri.setCreateUser("");
			tri.setEditUser("");
			tri.setTestResultId(qualityTestResultId);

			while (targetEntries.hasNext()) {
				Map.Entry<String, List<Object>> targetEntry = sourceEntries.next();
				tri.setColumnName(sourceEntry.getKey());
				tri.setKeyValue(sourceEntry.getKey());
				if (sourceEntry.getKey().equals(targetEntry.getKey())) {// key值相等
					keyFlag = true;
					List<Object> sourceList = sourceEntry.getValue();
					List<Object> targetList = targetEntry.getValue();
					out: for (Object sourceObj : sourceList) {
						for (Object targetObj : targetList) {
							if (sourceObj != null && targetObj != null
							        && sourceObj.toString().equals(targetObj.toString())) {
								tri.setSoruceValue(sourceObj.toString());
								tri.setTragetValue(targetObj.toString());
								comFlag = true;
								break out;
							}
						}
					}
				}
			}
			if (!keyFlag || !comFlag) {// 如果key值或者value值对不上 则返回失败
				logger.info("数据比对结果：失败");
				tri.setResult("数据比对结果：失败");
			} else if (keyFlag && comFlag) {// 如果key值和value值全部相等 则返回成功
				logger.info("数据比对结果：成功");
				tri.setResult("数据比对结果：成功");
			}
			itemQueue.add(tri);// 比对结果加入到结果队列
		}
	}
}
