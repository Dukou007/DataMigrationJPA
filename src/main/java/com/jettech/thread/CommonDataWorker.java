package com.jettech.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;

import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.AdapterFactory;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryModel;
import com.jettech.domain.DataField;

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

	// static final Logger logger = LoggerFactory.getLogger(DataWorker.class);

	public void stop() {
		_isRunning = false;
	}

	@Override
	public void run() {
		Connection conn = null;
		AbstractAdapter adapter = null;
		String dbInfo = getDbInfo(testQuery.getDataSource());
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
			ResultSet rs = pStmt.executeQuery();

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
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("thread interrupted error.", e);
		} catch (Exception e) {
			logger.error("get data error.", e);
		} finally {
			logger.info("退出生产者线程！");
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

		while (rs.next()) {
			List<Object> data = new ArrayList<Object>();
			String keyValue = "";
			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
				data.add(rs.getObject(i));
				// 列名称统一转换为大写[这里根据不同数据库可能需要调整]
				String columnLabel = rs.getMetaData().getColumnLabel(i).toUpperCase().trim();
				// rs.getMetaData().getColumnName(i);
				// rs.getMetaData().getColumnClassName(i);
				if (keyMap.containsKey(columnLabel)) {
					if (rs.getObject(i) != null) {
						keyValue += "|" + rs.getObject(i).toString().trim();
					} else {
						keyValue += "|[NULL]";
					}
				}
			}

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
		}
		return map;
	}
}
