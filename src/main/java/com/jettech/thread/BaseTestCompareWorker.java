package com.jettech.thread;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.jettech.domain.CompareResult;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryFileModel;
import com.jettech.domain.QueryModel;
import com.jettech.entity.TestResultItem;

public class BaseTestCompareWorker extends BaseTestWorker {

	public BaseTestCompareWorker(BlockingQueue<TestResultItem> itemQueue) {
		super(itemQueue);
	}
	protected CompareResult doCompareToFile(Map<String, List<Object>> sourceMap, QueryModel sourceQuery,
			Map<String, List<Object>> targetMap, QueryFileModel targetQuery, BlockingQueue<TestResultItem> itemQueue) {

		// Map<String, List<Object>> sourceMap = sourceData.getMap();
		// TestQueryService testQueryService =
		// SpringUtils.getBean(TestQueryServiceImpl.class);
		// TestQuery sourceQuery =
		// testQueryService.getOneById(sourceData.getTestQueryId());
		// QueryModel sourceQuery = sourceData.getTestQuery();
		// Map<String, List<Object>> targetMap = targetData.getMap();
		// TestQuery targetQuery =
		// testQueryService.getOneById(targetData.getTestQueryId());
		// QueryModel targetQuery = targetData.getTestQuery();

		Integer maxItemRows = null;
		Integer itemCount = 0;
		if (testCase.getMaxResultRows() != null && testCase.getMaxResultRows() > 0) {
			maxItemRows = testCase.getMaxResultRows();
		}

		long sameRow = 0;// 相同行数
		long notSameData = 0;// 不同的数据值的统计，每个行和列的不同的合计
		int sameDataInRow = 0;// 行内数据相同的统计
		long notSameRow = 0;// 不同行数
		// long notExistDataInDest = 0;
		// long notExistInSource = 0;
		// long notExistKeyInDest = 0;
		// long currentRow = 0;

		// 循环数据页面（或者全部）中的源数据的所有keyValue,判断目标是否有相同的kyeValue
		// 不包含，写结果明细
		// 包含，判断是否每一列的数据相同
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
					String fieldName = sourceField.getName();
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
								boolean cpDecimal = compareDecimal(keyValue, fieldName, sourceValue, targetValue);
								if (cpDecimal) {
									sameDataInRow++;
								} else {
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
				
				//一行数据行比较后，行内字段值相同数量和字段数量相同==>相同行数+1
				if (sameDataInRow == targetQuery.getQueryColumns().size()) {
					sameRow++;
				} else {
					notSameRow++;
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
	protected CompareResult doCompare(Map<String, List<Object>> sourceMap, QueryModel sourceQuery,
			Map<String, List<Object>> targetMap, QueryModel targetQuery, BlockingQueue<TestResultItem> itemQueue) {

		// Map<String, List<Object>> sourceMap = sourceData.getMap();
		// TestQueryService testQueryService =
		// SpringUtils.getBean(TestQueryServiceImpl.class);
		// TestQuery sourceQuery =
		// testQueryService.getOneById(sourceData.getTestQueryId());
		// QueryModel sourceQuery = sourceData.getTestQuery();
		// Map<String, List<Object>> targetMap = targetData.getMap();
		// TestQuery targetQuery =
		// testQueryService.getOneById(targetData.getTestQueryId());
		// QueryModel targetQuery = targetData.getTestQuery();

		Integer maxItemRows = null;
		Integer itemCount = 0;
		if (testCase.getMaxResultRows() != null && testCase.getMaxResultRows() > 0) {
			maxItemRows = testCase.getMaxResultRows();
		}

		long sameRow = 0;// 相同行数
		long notSameData = 0;// 不同的数据值的统计，每个行和列的不同的合计
		int sameDataInRow = 0;// 行内数据相同的统计
		long notSameRow = 0;// 不同行数
		// long notExistDataInDest = 0;
		// long notExistInSource = 0;
		// long notExistKeyInDest = 0;
		// long currentRow = 0;

		// 循环数据页面（或者全部）中的源数据的所有keyValue,判断目标是否有相同的kyeValue
		// 不包含，写结果明细
		// 包含，判断是否每一列的数据相同
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
					String fieldName = sourceField.getName();
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
								boolean cpDecimal = compareDecimal(keyValue, fieldName, sourceValue, targetValue);
								if (cpDecimal) {
									sameDataInRow++;
								} else {
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
				// 行内数量和字段数量相同==>相同行数+1
				if (sameDataInRow == targetQuery.getTestFields().size()) {
					sameRow++;
				} else {
					notSameRow++;
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

	// 从右到左比较
	protected CompareResult doRightCompare(Map<String, List<Object>> sourceMap, QueryModel sourceQuery,
			Map<String, List<Object>> targetMap, QueryModel targetQuery, BlockingQueue<TestResultItem> itemQueue) {
		Integer maxItemRows = null;
		Integer itemCount = 0;
		if (testCase.getMaxResultRows() != null && testCase.getMaxResultRows() > 0) {
			maxItemRows = testCase.getMaxResultRows();
		}

		long sameRow = 0;// 相同行数
		long notSameData = 0;// 不同的数据值的统计，每个行和列的不同的合计
		int sameDataInRow = 0;// 行内数据相同的统计
		long notSameRow = 0;// 不同行数
		// long notExistDataInDest = 0;
		// long notExistInSource = 0;
		// long notExistKeyInDest = 0;
		// long currentRow = 0;

		// 循环数据页面（或者全部）中的源数据的所有keyValue,判断目标是否有相同的kyeValue
		// 不包含，写结果明细
		// 包含，判断是否每一列的数据相同
		// Iterator<Map.Entry<String, List<Object>>> sourceEntries =
		// sourceMap.entrySet().iterator();
		for (String keyValue : targetMap.keySet()) {
			sameDataInRow = 0;
			if (sourceMap.containsKey(keyValue)) {
				List<Object> sourceValues = sourceMap.get(keyValue);
				List<Object> targetValues = targetMap.get(keyValue);
				// int sameDataInRow = 0;
				// 当使用简易模式(不设置比较的字段)时,以下需要特殊处理

				// 对于每个列目标是是否具有相同的值
				for (int i = 0; i < targetQuery.getTestFields().size(); i++) {
					FieldModel targetField = targetQuery.getTestFields().get(i);
					String fieldName = targetField.getName();
					Object targetValue = targetValues.get(i);
					// 判断目标的列数量是否足够
					if (sourceValues.size() > i) {
						Object sourceValue = sourceValues.get(i);
						String dataType = targetField.getDataType();

						if (sourceValue != null && targetValue != null) {
							if (numberType.containsKey(dataType.toUpperCase())) {
								// 如果时候数值类型,转换为decimal比较
								boolean cpDecimal = compareDecimal(keyValue, fieldName, sourceValue, targetValue);
								if (cpDecimal) {
									sameDataInRow++;
								} else {
									notSameData++;
								}
							} else {
								// 其它类型转换为字符串比较
								if (!sourceValue.toString().trim().equals(targetValue.toString().trim())) {
									itemQueue.add(createItem(keyValue, targetField.getName(), sourceValue, targetValue,
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
							itemQueue.add(createItem(keyValue, targetField.getName(), sourceValue, targetValue,
									"sourceValue is null"));
							notSameData++;
						} else if (targetValue == null && sourceValue != null) {
							// 源数据非null,目标数据null
							itemQueue.add(createItem(keyValue, targetField.getName(), sourceValue, targetValue,
									"targetValue is null"));
							notSameData++;
						}
					} else {
						// 目标表的列数量不与源相同
						itemQueue.add(createItem(keyValue, targetField.getName(), targetValue, "source not column"));
					}
				}
				// 行内数量和字段数量相同==>相同行数+1
				if (sameDataInRow == targetQuery.getTestFields().size()) {
					sameRow++;
				} else {
					notSameRow++;
				}
			} else {
				// 目标表无此KeyValue
				itemQueue.add(createItem(keyValue, "source not keyvalue"));
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

	// 双向比较
	protected CompareResult doTwoWayCompare(Map<String, List<Object>> sourceMap, QueryModel sourceQuery,
			Map<String, List<Object>> targetMap, QueryModel targetQuery, BlockingQueue<TestResultItem> itemQueue) {
		// Map<String, List<Object>> sourceMap = sourceData.getMap();
		// TestQueryService testQueryService =
		// SpringUtils.getBean(TestQueryServiceImpl.class);
		// TestQuery sourceQuery =
		// testQueryService.getOneById(sourceData.getTestQueryId());
		// QueryModel sourceQuery = sourceData.getTestQuery();
		// Map<String, List<Object>> targetMap = targetData.getMap();
		// TestQuery targetQuery =
		// testQueryService.getOneById(targetData.getTestQueryId());
		// QueryModel targetQuery = targetData.getTestQuery();

		Integer maxItemRows = null;
		Integer itemCount = 0;
		if (testCase.getMaxResultRows() != null && testCase.getMaxResultRows() > 0) {
			maxItemRows = testCase.getMaxResultRows();
		}

		long sameRow = 0;// 相同行数
		long notSameData = 0;// 不同的数据值的统计，每个行和列的不同的合计
		int sameDataInRow = 0;// 行内数据相同的统计
		long notSameRow = 0;// 不同行数
		//先从左到右进行比较,将比较过key的放入集合中
		Map<String,Object> compared=new HashMap<>();//存放已经比较过的key
		for (String keyValue : sourceMap.keySet()) {
			sameDataInRow = 0;
			if (targetMap.containsKey(keyValue)) {
				compared.put(keyValue, true);
				List<Object> sourceValues = sourceMap.get(keyValue);
				List<Object> targetValues = targetMap.get(keyValue);
				// int sameDataInRow = 0;
				// 当使用简易模式(不设置比较的字段)时,以下需要特殊处理

				// 对于每个列目标是是否具有相同的值
				for (int i = 0; i < sourceQuery.getTestFields().size(); i++) {
					FieldModel sourceField = sourceQuery.getTestFields().get(i);
					String fieldName = sourceField.getName();
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
								boolean cpDecimal = compareDecimal(keyValue, fieldName, sourceValue, targetValue);
								if (cpDecimal) {
									sameDataInRow++;
								} else {
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
				// 行内数量和字段数量相同==>相同行数+1
				if (sameDataInRow == targetQuery.getTestFields().size()) {
					sameRow++;
				} else {
					notSameRow++;
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
		//从右到左进行比较
		for (String keyValue : targetMap.keySet()) {
			sameDataInRow = 0;
			if(compared.containsKey(keyValue)) {
				//已经比较过跳过循环
				continue;
			}
			if (sourceMap.containsKey(keyValue)) {
				List<Object> sourceValues = sourceMap.get(keyValue);
				List<Object> targetValues = targetMap.get(keyValue);
				// int sameDataInRow = 0;
				// 当使用简易模式(不设置比较的字段)时,以下需要特殊处理

				// 对于每个列目标是是否具有相同的值
				for (int i = 0; i < targetQuery.getTestFields().size(); i++) {
					FieldModel targetField = targetQuery.getTestFields().get(i);
					String fieldName = targetField.getName();
					Object targetValue = targetValues.get(i);
					// 判断目标的列数量是否足够
					if (sourceValues.size() > i) {
						Object sourceValue = sourceValues.get(i);
						String dataType = targetField.getDataType();

						if (sourceValue != null && targetValue != null) {
							if (numberType.containsKey(dataType.toUpperCase())) {
								// 如果时候数值类型,转换为decimal比较
								boolean cpDecimal = compareDecimal(keyValue, fieldName, sourceValue, targetValue);
								if (cpDecimal) {
									sameDataInRow++;
								} else {
									notSameData++;
								}
							} else {
								// 其它类型转换为字符串比较
								if (!sourceValue.toString().trim().equals(targetValue.toString().trim())) {
									itemQueue.add(createItem(keyValue, targetField.getName(), sourceValue, targetValue,
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
							itemQueue.add(createItem(keyValue, targetField.getName(), sourceValue, targetValue,
									"sourceValue is null"));
							notSameData++;
						} else if (targetValue == null && sourceValue != null) {
							// 源数据非null,目标数据null
							itemQueue.add(createItem(keyValue, targetField.getName(), sourceValue, targetValue,
									"targetValue is null"));
							notSameData++;
						}
					} else {
						// 目标表的列数量不与源相同
						itemQueue.add(createItem(keyValue, targetField.getName(), targetValue, "source not column"));
					}
				}
				// 行内数量和字段数量相同==>相同行数+1
				if (sameDataInRow == targetQuery.getTestFields().size()) {
					sameRow++;
				} else {
					notSameRow++;
				}
			} else {
				// 目标表无此KeyValue
				itemQueue.add(createItem(keyValue, "source not keyvalue"));
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

	private boolean compareDecimal(String keyValue, String fileName, Object sourceValue, Object targetValue) {
		BigDecimal srcDec = null;
		String info_convert_error = " convert to BigDecimal error";
		try {
			srcDec = new BigDecimal(sourceValue.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(caseName + "keyValue:" + keyValue + "sourceValue:" + sourceValue + info_convert_error, e);
			itemQueue.add(createItem(keyValue, fileName, sourceValue, targetValue, "Convert to Decimal Error"));
			return false;
		}
		BigDecimal destDec = null;
		try {
			destDec = new BigDecimal(targetValue.toString());
		} catch (Exception e) {
			e.printStackTrace();
			logger.warn(caseName + " keyValue:" + keyValue + "targetValue:" + targetValue + info_convert_error, e);
			itemQueue.add(createItem(keyValue, fileName, sourceValue, targetValue, "Convert to Decimal Error"));
			return false;
		}

		if (srcDec.compareTo(destDec) == 0) {
			return true;
		} else {
			itemQueue.add(createItem(keyValue, fileName, sourceValue, targetValue, "not same value"));
			return false;
		}
	}

}
