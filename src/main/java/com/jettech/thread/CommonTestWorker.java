package com.jettech.thread;

import java.math.BigDecimal;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.EnumCompareDirection;
import com.jettech.domain.CaseModel;
import com.jettech.domain.CompareResult;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryModel;
import com.jettech.domain.ResultModel;
import com.jettech.entity.TestResultItem;
import com.jettech.service.WebSocketService;

public class CommonTestWorker extends BaseTestCompareWorker implements Runnable {

	private Logger logger = LoggerFactory.getLogger(CommonTestWorker.class);

	private BlockingQueue<BaseData> sourceQueue;

	private BlockingQueue<BaseData> targetQueue;

	private Integer MaxWaitTime = 600;

	private EnumCompareDirection enumCompareDirection;

	public CommonTestWorker(BlockingQueue<BaseData> sourceQueue, BlockingQueue<BaseData> targetQueue,
	        BlockingQueue<TestResultItem> itemQueue, CaseModel testCase) {
		super(itemQueue);
		this.sourceQueue = sourceQueue;
		this.targetQueue = targetQueue;
		this.testCase = testCase;
		this.caseName = testCase.getName();
		this.testResultId = testCase.getTestResult().getId();
		this.enumCompareDirection = testCase.getEnumCompareDirection() == null ? EnumCompareDirection.LeftToRight
		        : testCase.getEnumCompareDirection();
		logger.info(caseName + ",testResultId:[" + testResultId + "]");
	}

	@Override
	public void run() {
		compare();
	}

	public void compare() {
		logger.info(caseName + " 启动消费者线程！");
		int waitCount = 0;
		CommonData sourceData = null;
		CommonData targetData = null;
		try {
			ResultModel testResult = testCase.getTestResult();
			CompareResult cpResult = new CompareResult(0L, 0L, 0L);
			;
			while (true) {
				if (sourceData == null) {
					sourceData = (CommonData) sourceQueue.poll(100, TimeUnit.MICROSECONDS);
					if (sourceData != null) {
						if (sourceData.getMap() != null && sourceData.getMap().size() > 0) {
							logger.info(caseName + " 获取到源数据结束,数量:" + sourceData.getMap().size());
							testResult.setSourceCount(sourceData.getMap().size());
						} else {
							logger.info(caseName + " 获取到源数据结束,返回空");
							cpResult.setSourceCount(0L);// 源数据返回的数据为空
							sourceQueue.remove(sourceData);// 获取到队首数据后从队列中删掉
							targetQueue.remove(targetData);// 获取到队首数据后从队列中删掉
							logger.info(caseName + " 对比终止");
							break;
						}
					} else {
						logger.info(caseName + " 等待源数据...");
					}
				}
				if (targetData == null) {
					targetData = (CommonData) targetQueue.poll(100, TimeUnit.MICROSECONDS);
					if (targetData != null) {
						if (targetData.getMap() != null && targetData.getMap().size() > 0) {
							logger.info(caseName + " 获取到目标数据结束,数量:" + targetData.getMap().size());
							testResult.setTargetCount(targetData.getMap().size());
						} else {
							WebSocketService.sendMsgToAll(caseName + " 获取到目标数据结束,返回空");
							logger.info(caseName + " 获取到目标数据结束,返回空");
							cpResult.setTargetCount(0L);// 目标数据返回的为空
							sourceQueue.remove(sourceData);// 获取到队首数据后从队列中删掉
							targetQueue.remove(targetData);// 获取到队首数据后从队列中删掉
							logger.info(caseName + " 对比终止");
							break;
						}
					} else {
						logger.info(caseName + " 等待目标数据...");
					}
				}
				if (null != sourceData && null != targetData) {// 有数据时直接从队列的队首取走，无数据时阻塞，在2s内有数据取走
					logger.info(caseName + "拿到队首源数据：" + sourceData.getMap().size());
					logger.info(caseName + "拿到队首目标数据：" + targetData.getMap().size());
					
					// this.dataCompare(sourceData, targetData, itemQueue);//
					// 数据比对

					if (this.enumCompareDirection == EnumCompareDirection.LeftToRight) {
						cpResult = doCompare(sourceData.getMap(), sourceData.getTestQuery(), targetData.getMap(),
						        targetData.getTestQuery(), itemQueue);
					} else if (this.enumCompareDirection == EnumCompareDirection.RightToLeft) {
						cpResult = doRightCompare(sourceData.getMap(), sourceData.getTestQuery(), targetData.getMap(),
						        targetData.getTestQuery(), itemQueue);
					} else if (this.enumCompareDirection == EnumCompareDirection.TwoWay) {
						cpResult = doTwoWayCompare(sourceData.getMap(), sourceData.getTestQuery(), targetData.getMap(),
						        targetData.getTestQuery(), itemQueue);
					} else if (this.enumCompareDirection == EnumCompareDirection.ToFile) {
						cpResult = doCompareToFile(sourceData.getMap(), sourceData.getTestQuery(), targetData.getMap(),
						        targetData.getTestFileQuery(), itemQueue);
					}

					logger.info(caseName + " 队首源数据被消费sourceData：" + sourceData.getMap().size());
					logger.info(caseName + " 队首目标数据被消费targetData：" + targetData.getMap().size());
					
					testResult.setSameRow(cpResult.getSameRow().intValue());// 暂时使用int值，存在溢出的可能
					testResult.setNotSameData(cpResult.getNotSameData().intValue());// 暂时使用int值，存在溢出的可能
					testResult.setNotSameRow(cpResult.getNotSameRow().intValue());
					
					sourceData.getMap().clear();
					targetData.getMap().clear();
					sourceQueue.remove(sourceData);// 获取到队首数据后从队列中删掉
					targetQueue.remove(targetData);// 获取到队首数据后从队列中删掉
				
					logger.info(caseName + " 对比结束");
					break;
				} else {// 超过2s还没数据，认为所有生产线程都已经退出，自动退出消费线程。
					Thread.sleep(100);
					waitCount = waitCount + 1;
					if (waitCount > MaxWaitTime) {
						testResult.setSameRow(cpResult.getSameRow().intValue());// 暂时使用int值，存在溢出的可能
						testResult.setNotSameData(cpResult.getNotSameData().intValue());// 暂时使用int值，存在溢出的可能
						testResult.setNotSameRow(cpResult.getNotSameRow().intValue());
						logger.info(caseName + " 等待数据超时：" + MaxWaitTime);
						break;
					}
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error(caseName + "数据比对异常！！！");
			WebSocketService.sendMsgToAll(caseName + "数据比对异常！！！");
			// Thread.currentThread().interrupt();
		} finally {
			logger.info(caseName + "退出消费者线程！");
			// Thread.currentThread().interrupt();
		}
	}

	private CompareResult doCompare2(CommonData sourceData, CommonData targetData,
	        BlockingQueue<TestResultItem> itemQueue) {

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

		Integer maxItemRows = null;
		Integer itemCount = 0;
		if (testCase.getMaxResultRows() != null && testCase.getMaxResultRows() > 0) {
			maxItemRows = testCase.getMaxResultRows();
		}

		long sameRow = 0;// 相同行数
		long notSameData = 0;
		int sameDataInRow = 0;
		long notSameRow = 0;// 不同行数
		// long notExistDataInDest = 0;
		// long notExistInSource = 0;
		// long notExistKeyInDest = 0;
		// long currentRow = 0;

		// Iterator<Map.Entry<String, List<Object>>> sourceEntries =
		// sourceMap.entrySet().iterator();
		for (String keyValue : sourceMap.keySet()) {
			sameDataInRow = 0;
			if (targetMap.containsKey(keyValue)) {
				List<Object> sourceValues = sourceMap.get(keyValue);
				List<Object> targetValues = targetMap.get(keyValue);
				// int sameDataInRow = 0;
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
								BigDecimal srcDec = null;
								try {
									srcDec = new BigDecimal(sourceValue.toString());
								} catch (Exception e) {
									e.printStackTrace();
									logger.warn(caseName + "keyValue:" + keyValue + "sourceValue:" + sourceValue
									        + " convert to BigDecimal error", e);
									itemQueue.add(createItem(keyValue, sourceField.getName(), sourceValue, targetValue,
									        "Convert to Decimal Error"));
									notSameData++;
									continue;
								}
								BigDecimal destDec = null;
								try {
									destDec = new BigDecimal(targetValue.toString());
								} catch (Exception e) {
									e.printStackTrace();
									logger.warn(caseName + " keyValue:" + keyValue + "targetValue:" + targetValue
									        + " convert to BigDecimal error", e);
									itemQueue.add(createItem(keyValue, sourceField.getName(), sourceValue, targetValue,
									        "Convert to Decimal Error"));
									notSameData++;
									continue;
								}

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
						// 目标表的列数量不与源相同
						itemQueue.add(createItem(keyValue, sourceField.getName(), sourceValue, "target not column"));
					}
				}
				// 行内数量和字段数量相同==>相关行数+1
				if (sameDataInRow == targetQuery.getTestFields().size()) {
					sameRow++;
				}
			} else {
				// 目标表无此KeyValue
				itemQueue.add(createItem(keyValue, "target not keyvalue"));
				notSameRow++;
			}

			if (maxItemRows != null && maxItemRows > 0 && notSameData >= maxItemRows) {
				logger.info(caseName + "测试被中断,结果明细的数量:" + notSameData + " 超过了最大结果数量限制:" + maxItemRows);
				break;
			}
		}
		logger.info(caseName + " compare end sameRow:" + sameRow + " notSameRow:" + notSameRow + " notSameData:"
		        + notSameData);
		CompareResult result = new CompareResult(sameRow, notSameData, notSameRow);
		return result;
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
								tri.setSourceValue(sourceObj.toString());
								tri.setTargetValue(targetObj.toString());
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
