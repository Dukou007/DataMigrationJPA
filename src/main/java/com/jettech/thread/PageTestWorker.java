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

import com.jettech.domain.CaseModel;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryModel;
import com.jettech.domain.ResultModel;
import com.jettech.entity.TestCase;
import com.jettech.entity.DataField;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestQueryField;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;

import com.jettech.service.TestQueryService;
import com.jettech.service.Impl.TestQueryServiceImpl;
import com.jettech.util.SpringUtils;

public class PageTestWorker extends BaseTestWorker implements Runnable {

	private Logger logger = LoggerFactory.getLogger(PageTestWorker.class);
	private BlockingQueue<BaseData> sourceQueue;

	private BlockingQueue<TestResultItem> itemQueue;
	private Integer testResultId;
	private Integer MaxWaitTime = 600;
	private CaseModel testCase = null;
	private String caseName = null;

	public PageTestWorker(BlockingQueue<BaseData> sourceQueue, BlockingQueue<TestResultItem> itemQueue,
	        CaseModel testCase) {
		super();
		this.sourceQueue = sourceQueue;
		this.itemQueue = itemQueue;
		this.testCase = testCase;
		this.testResultId = testCase.getTestResult().getId();
		this.caseName = testCase.getName();
	}

	@Override
	public void run() {
		logger.info(caseName + "启动消费者线程！");
		int waitCount = 0;
		CommonData sourceData = null;
		CommonData targetData = null;
		ResultModel resultModel = testCase.getTestResult();

		try {
			while (true) {
				if (sourceData == null)
					sourceData = (CommonData) sourceQueue.poll(100, TimeUnit.MICROSECONDS);
				if (null != sourceData) {// 有数据时直接从队列的队首取走，无数据时阻塞，在2s内有数据取走
					logger.info(caseName + "拿到队首源数据：" + sourceData.getMap().size());
					resultModel.addSourceCount(sourceData.getMap().size());
					
					
					// 数据比对
					doCompare(sourceData, targetData, itemQueue);
					sourceQueue.remove(sourceData);// 获取到队首数据后从队列中删掉
					logger.info(caseName + "队首源数据被消费sourceData：" + sourceData.getMap().size());
				} else {
					// 超过MaxWaitTime/10 秒还没数据，认为所有生产线程都已经退出，自动退出消费线程。
					Thread.sleep(100);
					waitCount = waitCount + 1;
					if (waitCount % 5000 == 0)
						logger.info(caseName + "等待队列数据...");
					if (waitCount > MaxWaitTime) {
						logger.info(caseName + "超过" + MaxWaitTime / 10 + "s未获取到新数据,终止处理进程");
						break;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(caseName + "数据比对异常！！！");
		} finally {
			logger.info(caseName + "退出消费者线程！");
		}
	}

	private void doCompare(CommonData sourceData, CommonData targetData, BlockingQueue<TestResultItem> itemQueue) {
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
		// long sameRow = 0;// 相同行数
		long notSameData = 0;
		int sameDataInRow = 0;
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
				// 对于每个列目标是是否具有相同的值
				for (int i = 0; i < sourceQuery.getTestFields().size(); i++) {
					FieldModel sourceField = sourceQuery.getTestFields().get(i);
					Object sourceValue = sourceValues.get(i);
					// 判断目标的列数量是否足够
					if (targetValues.size() > i) {
						Object targetValue = targetValues.get(i);
						String dataType = targetQuery.getTestFields().get(i).getDataType();

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
				// if (sameDataInRow == targetQuery.getTestFields().size()) {
				// sameRow++;
				// }
			} else {
				itemQueue.add(createItem(keyValue, "target not keyvalue"));
			}
		}
		// logger.info("compare end sameRow:" + sameRow + " notSameData:" +
		// notSameData);
		logger.info("sourceQuery dataCounts is:" + sourceQuery.getTestFields().size());
		logger.info("targetQuery dataCounts is:" + targetQuery.getTestFields().size());
		logger.info("compare end sameDataInRow:" + sameDataInRow + " notSameData:" + notSameData);
	}

//	private TestResultItem createItem(String keyValue, String colName, Object sourceValue, Object targetValue,
//	        String result) {
//		TestResultItem item = new TestResultItem();
//		item.setKeyValue(keyValue);
//		item.setResult(result);
//		item.setColumnName(colName);
//		item.setSoruceValue(sourceValue == null ? "null" : sourceValue.toString());
//		item.setTragetValue(targetValue == null ? "null" : targetValue.toString());
//		item.setTestResultId(testResultId);
//		return item;
//	}

//	private TestResultItem createItem(String keyValue, String colName, Object sourceValue, String result) {
//		return createItem(keyValue, colName, sourceValue, null, result);
//	}

//	private TestResultItem createNewResultItem(String keyValue, String result) {
//		return createItem(keyValue, null, null, result);
//	}

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
			tri.setTestResultId(testResultId);

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
