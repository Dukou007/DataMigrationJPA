package com.jettech.thread;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.jettech.EnumExecuteStatus;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.AdapterFactory;
import com.jettech.domain.CaseModel;
import com.jettech.domain.DataField;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryModel;
import com.jettech.entity.TestResultItem;

/**
 * 实现获取数据的生产者(一次性获取所有数据,不分页)
 * 
 * @author tan
 *
 */
public class CommonDataWorker extends BaseDataWorker implements Runnable {

	public CommonDataWorker(BlockingQueue<BaseData> queue, QueryModel testQuery) throws Exception {
		super(queue, testQuery);
	}
 
	public CommonDataWorker(BlockingQueue<BaseData> queue, BlockingQueue<TestResultItem> itemQueue,
	        QueryModel testQuery, CaseModel testCase) throws Exception {
		super(queue,itemQueue, testQuery,testCase);
//		this.itemQueue = itemQueue;
//		this.testResultId = testCase.getTestResult().getId();
	}

	// static final Logger logger = LoggerFactory.getLogger(DataWorker.class);


	public void stop() {
		_isRunning = false;
	}

	@Override
	public void run() {
		Connection conn = null;
		AbstractAdapter adapter = null;
		String dbInfo = getDbInfo(testQuery.getDataSource());
		testQuery.setExecState(EnumExecuteStatus.Executing);
		try {
			// 获取数据库连接
			adapter = AdapterFactory.create(dataSource.getDatabaseType());
			if (adapter == null) {
				logger.error("creat adapter failed. " + dbInfo);
				return;
			} else {
				logger.info("creat adapter success. " + dbInfo);
			}
		} catch (Exception e1) {
			logger.error("creat adapter error", e1);
		}

		try {
			// 获得数据库连接
			conn = adapter.getConnection(dataSource);
			if (conn == null || conn.isClosed()) {
				logger.error("create connection failed." + dbInfo);
				return;
			} else {
				logger.info("creat connection success. " + dbInfo);
			}

			// 获得准备执行的SQL
			String sql = getQuerySQL();

			PreparedStatement pStmt = null;
			pStmt = conn.prepareStatement(sql);
			logger.info("load sourceSql start=========================:" + new Date());
			ResultSet rs = pStmt.executeQuery();
			logger.info("load sourceSql end=========================:" + new Date());
			// 获得查询的列情况,并赋值给Query的queyrColumns属性,用于为定义查询的列详情时
			List<DataField> colsList = getQueryColumns(this.testQuery, rs);

			logger.info(String.format("当前有数据:[%d]组,开始获取数据", queue.size()));

			// 获取数据到Map
			Map<String, List<Object>> map = null;
			map = getDataRows(rs, keyMap);
			if (map.size() == 0) {
				logger.info(this.testQuery.getName() + "未读取到有效数据");
			} else {
				logger.info(this.testQuery.getName() + "有效数据:" + map.size());
			}

			// 将数据存储到页对象中
			CommonData data = new CommonData();
			data.setTestQuery(testQuery);
			data.setTestQueryId(testQuery.getId());
			data.setMap(map);
			// getMixMaxKey(page, map.keySet());

			// 将数据页存入队列
			// 如果BlockQueue没有空间,则调用此方法的线程被阻断直到BlockingQueue里面有空间再继续.
			queue.put(data);

			logger.info(String.format("[%s]获取数据行数:[%d]", this.testQuery.getDataSource().getName(), map.size()));
			testQuery.setExecState(EnumExecuteStatus.Finish);
		} catch (InterruptedException e) {
			testQuery.setExecState(EnumExecuteStatus.Interrupt);
			e.printStackTrace();
			logger.error("查询[" + testQuery.getName() + "] thread interrupted error.", e);
		} catch (java.sql.SQLException e) {
			testQuery.setExecState(EnumExecuteStatus.Interrupt);
			logger.error("查询[" + testQuery.getName() + "] get data error.", e);
		} catch (Exception e) {
			testQuery.setExecState(EnumExecuteStatus.Interrupt);
			logger.error("查询[" + testQuery.getName() + "] get data error.", e);
		} finally {
			logger.info("退出生产者线程！查询[" + testQuery.getName() + "]");
			adapter.closeConnection(conn);
			_isRunning = false;
		}
	}

	/**
	 * 从指定的数据集中获取数据
	 * 
	 * @param rs
	 * @param keyMap
	 * @return
	 * @throws SQLException
	 */
	protected Map<String, List<Object>> getDataRows(ResultSet rs, Map<String, FieldModel> keyMap) throws SQLException {
		Map<String, List<Object>> map = new HashMap<>();

		Integer maxNullKeyCount = testQuery.getMaxNullKeyCount();
		Integer maxDuplKeyCount = testQuery.getMaxDuplicatedKeyCount();
		String info = "dataSource[" + dataSource.getName() + "],query[" + testQuery.getName() + "]";
		long rowNum = 0;
		int errCount = 0;
		int MaxErrCount = 100000;
		List<Object> data = null;
		while (rs.next()) {
			rowNum++;
			data = new ArrayList<Object>();
			String keyValue = "";
			StringBuffer buffer = new StringBuffer();
			String columnLabel = null;
			try {
				// 处理每个数据列
				for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
					// 列名称统一转换为大写[这里根据不同数据库可能需要调整]
					columnLabel = rs.getMetaData().getColumnLabel(i).toUpperCase().trim();
					String colType = rs.getMetaData().getColumnTypeName(i).trim().toUpperCase();
					// rs.getMetaData().getColumnName(i);
					// rs.getMetaData().getColumnClassName(i);

					data.add(rs.getObject(i));
					if (keyMap.containsKey(columnLabel)) {
						if (rs.getObject(i) != null) {
							Object keyData = rs.getObject(i);
							String dataStr = keyData.toString().trim();
							if (NumberUtil.getNumberType().containsKey(colType)) {
								String valueStr = dataStr.trim();
								if (valueStr.contains(".")) {
									valueStr = valueStr.replaceAll("0+?$", "");// 去掉多余的0
									valueStr = valueStr.replaceAll("[.]$", "");// 如最后一位是.则去掉
								}
								keyValue += "|" + valueStr;
								if (!valueStr.equals(dataStr)) {
									buffer.append("[" + dataStr + "]to[" + valueStr + "],");
								}
							} else {
								keyValue += "|" + dataStr.trim();
							}
						} else {
							// ###上饶对Decimal的特殊处理，迁移时Null值设置为0
							if (colType.equals(NumberUtil.DECIMAL)) {
								keyValue += "|0";
							} else {
								keyValue += "|[NULL]";
							}
						}
					}
				}
			} catch (SQLException e) {
				TestResultItem item = this.createItem("query:" + testQuery.getName() + ",rowNum:" + rowNum, columnLabel,
				        data.toString(), e.getMessage());
				itemQueue.add(item);
				logger.info("查询[" + testQuery.getName() + "]读取数据第[" + rowNum + "]行出现错误:" + e.getMessage() + ",数据["
				        + data.toString() + "]");
				errCount++;
				// if (errCount > MaxErrCount) {
				// logger.info("读取数据出现[" + errCount + "]此错误,读取终止");
				// testQuery.setExecState(EnumExecuteStatus.Interrupt);
				// break;
				// }
				continue;// 处理下一条
			}

			// if (buffer.length() > 0)
			// logger.debug("convert:" + buffer.toString());
			if (keyValue.length() > 0 && keyValue.substring(0, 1).equals("|"))
				keyValue = keyValue.substring(1);
			if (keyValue == null || keyValue.trim().length() == 0 || keyValue.equals("[NULL]")) {
				// logger.warn(String.format("key'value is null or empty.
				// key:[%s] data:[%s]", keyMap.keySet(),
				// data.toString()));
				nullOrEmptyKeyCount = nullOrEmptyKeyCount + 1;
				// continue;
			} else {
				if (map.containsKey(keyValue)) {
					// logger.warn(String.format("key'value is Duplicated.
					// key:[%s] keyValue:[%s] data:[%s]",
					// keyMap.keySet(), keyValue, data.toString(),
					// map.get(keyValue).toString()));
					duplicatedKeyCount = duplicatedKeyCount + 1;
					// continue;
				} else {
					map.put(keyValue, data);
					if (map.size() % 10000 == 0) {
						logger.info(info + ",已读取数据[" + map.size() + "]");
					}
				}
			}

			if (maxNullKeyCount != null && maxNullKeyCount > 0 && nullOrEmptyKeyCount > maxNullKeyCount) {
				logger.info(
				        String.format("读数中断,空键值列数量超过允许:current:[%d] max:[%d]", nullOrEmptyKeyCount, maxNullKeyCount));
				break;
			}

			// 如果有超过[#100]的重复键值,退出获取数据过程
			if (maxDuplKeyCount != null && maxDuplKeyCount > 0 && duplicatedKeyCount > maxDuplKeyCount) {
				logger.info(
				        String.format("读数中断,重复键值列数量超过允许:current:[%d] max:[%d]", duplicatedKeyCount, maxDuplKeyCount));
				break;
			}

		} // 循环结束
		if (errCount > 0) {
			logger.info("######查询[" + testQuery.getName() + "]读取数据出现[" + errCount + "]次数据错误");
			testQuery.setQueryErrCount(testQuery.getQueryErrCount() + errCount);
		}
		return map;
	}
}
