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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.AdapterFactory;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryModel;
import com.jettech.entity.TestQuery;

/**
 * 实现获取分页数据的生产者
 * 
 * @author tan
 *
 */
public class PageDataWorker extends BaseDataWorker implements Runnable {

	protected static final int _DEFAULT_PAGE_SIZE = 1000;// 默认分页大小
	protected Integer pageSize = _DEFAULT_PAGE_SIZE;
	int pageIndex = 1;

	public PageDataWorker(BlockingQueue<BaseData> queueSource, QueryModel querySource, Integer pageSize)
	        throws Exception {
		super(queueSource, querySource);
		if (pageSize == null || pageSize < 0) {
			this.pageSize = _DEFAULT_PAGE_SIZE;
		} else {
			this.pageSize = pageSize;
		}
	}

	public PageDataWorker() {
		super();
	}


	TestQuery sourceQuery;

	public PageDataWorker(BlockingQueue<BaseData> queue, QueryModel testQuery, Integer pageSize,
	        TestQuery sourceQuery) throws Exception {
		this(queue, testQuery, pageSize);
		this.sourceQuery = sourceQuery;
	}
	// static final Logger logger = LoggerFactory.getLogger(DataWorker.class);

	public void stop() {
		_isRunning = false;
	}

	@Override
	public void run() {
		logger.info("启动生产者线程！");
		Connection conn = null;
		AbstractAdapter adapter = null;
		String dbInfo = getDbInfo(null);
		try {
			// 获取数据库连接
			adapter = AdapterFactory.create(dataSource.getDatabaseType());
			if (adapter == null) {
				System.out.print("creat adapter failed. " + dbInfo);
				return;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			conn = adapter.getConnection(dataSource);
			if (conn == null || conn.isClosed()) {
				logger.error("create connection failed." + dbInfo);
				return;
			}

			String sql = getQuerySQL();

			PreparedStatement pStmt = null;
			pStmt = conn.prepareStatement(sql);
			ResultSet rs = pStmt.executeQuery();

			Integer maxNullKeyCount = testQuery.getMaxNullKeyCount();
			Integer maxDuplKeyCount = testQuery.getMaxDuplicatedKeyCount();
			Map<String, List<Object>> map = null;
			while (true) {

				if (maxNullKeyCount != null && maxNullKeyCount > 0 && nullOrEmptyKeyCount > maxNullKeyCount) {
					logger.info(String.format("读数中断,空键值列数量超过允许:current:[%d] max:[%d]", nullOrEmptyKeyCount,
					        maxNullKeyCount));
					break;
				}

				// 如果有超过[#100]的重复键值,退出获取数据过程
				if (maxDuplKeyCount != null && maxDuplKeyCount > 0 && nullOrEmptyKeyCount > maxDuplKeyCount) {
					logger.info(String.format("读数中断,重复键值列数量超过允许:current:[%d] max:[%d]", duplicatedKeyCount,
					        maxDuplKeyCount));
					break;
				}

				logger.info(String.format("当前有数据:[%d]组,开始获取数据", queue.size()));

				map = getDataRows(rs, keyMap);
				logger.info(String.format("获取数据行数:[%d]", map.size()));
				if (map.size() == 0) {
					logger.info("数据已读取完毕");
					break;
				} else {
					PageData data = new PageData();
					data.setMap(map);
					data.setTestQuery(testQuery);
					data.setPageIndex(pageIndex);
					getMixMaxKey(data, map.keySet());
					// 如果BlockQueue没有空间,则调用此方法的线程被阻断直到BlockingQueue里面有空间再继续.
					queue.put(data);
					logger.info(String.format("当前有数据:[%d]组, pageIndex:[%d] pageSize:[%d]", queue.size(), pageIndex,
					        pageSize));
					pageIndex += 1;// 分页序号+1
				}
				if (_isRunning == false) {
					logger.info("收到终止信号");
				}
			}

			if (map.size() > 0) {
				// 最后一组map
				PageData page = new PageData();
				page.setMap(map);
				getMixMaxKey(page, map.keySet());
				queue.put(page);
			}

		} catch (InterruptedException e) {
			e.printStackTrace();
			Thread.currentThread().interrupt();
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
	 * 根据查询，分页索引，分页大小 返回分页查询SQL
	 * 
	 * @param query
	 *            查询模型
	 * @param pageIndex
	 *            分页索引 ,从1开始
	 * @param pageSize
	 *            分页大小，没有数据行数
	 * @return SQL
	 * @throws Exception
	 */
	protected String getPageSQL(QueryModel query, int pageIndex, int pageSize) throws Exception {
//		SelectBuilder builder =new SelectBuilder();
	
		String sql = query.getSqlText();
//		net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(sql);
		// 如果PageText为空，设置分页规则
		if (query.getPageText() == null || query.getPageText().trim().isEmpty()) {
			// 通过配置的pageFields配置
			StringBuilder builder = new StringBuilder();
			if (query.getPageFields() != null && query.getPageFields().size() > 0) {
				for (FieldModel field : query.getPageFields()) {
					if (builder.length() > 0)
						builder.append(",");
					builder.append(field.getName());
				}
			}
			if (builder.length() == 0) {
				// 未找到自定义分页排序配置,使用key作为分页排序标志
				query.setPageText(query.getKeyText());
			} else {
				query.setPageText(builder.toString());
			}
		}
		// 如果沒有指定分页排序属性,则使用Key作为分页排序字段
		String pageText = query.getPageText();
		if (pageText == null || pageText.trim().isEmpty())
			pageText = query.getKeyText();

		String regEx = "{1,}order {1,}by {1,}";
		Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql.toLowerCase());
		// 查找字符串中是否有匹配正则表达式的字符/字符串
		if (!matcher.find()) {
			// 未找到排序子句,增加排序子句
			switch (query.getDataSource().getDbtype()) {
			case Oracle:
				StringBuilder pageTex=new StringBuilder();
				if(pageText.contains(",")){//如果多个key给每个key增加双引号
					String[] pageTs=pageText.split(",");
					for(int i=0;i<pageTs.length;i++){
						pageTex.append("\"" + pageTs[i]+"\"");
						if(i<pageTs.length-1){
							pageTex.append(",");
						}
					}
					sql += " order by " + pageTex.toString();
					break;
				}
				sql += " order by \"" + pageText+"\"";
				break;
			default:
				sql += " order by " + pageText;
			}
			
		}

		switch (query.getDataSource().getDbtype()) {
		case Mysql:
			// https://www.cnblogs.com/youyoui/p/7851007.html
			// SELECT * FROM table LIMIT [offset,] rows | rows OFFSET offset
			sql += " limit " + (pageIndex - 1) * pageSize + "," + pageSize;
			break;
		case Oracle:
			// https://blog.csdn.net/nalw2012/article/details/79033145
			// 可以被索引优化的方式，不能用于有多表关联的情况
			// SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (SELECT * FROM
			// TABLE_NAME) A WHERE ROWNUM <= 40 ) WHERE RN >= 21
			sql = "SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (" + sql + ") A WHERE ROWNUM <= "
			        + pageIndex * pageSize + " ) WHERE RN >= " + ((pageIndex - 1) * pageSize + 1);

			// SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (SELECT * FROM
			// TABLE_NAME) A ) WHERE RN BETWEEN 21 AND 40
			// sql="SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (" +sql+") A )
			// WHERE RN BETWEEN "+((pageIndex-1) * pageSize +1)+" AND "
			// +pageIndex * pageSize;
			break;
		case DB2:
			break;
		case Informix:
			//select skip 2 first 10 * from Table(multiset(select * FROM AA10TEMP order by AA10BKNO)) t;
			sql ="select skip "+ ((pageIndex - 1) * pageSize + 1)+" first "+ pageSize+ " * from Table(Multiset("+sql +")) t";
			break;
		default:
			throw new Exception("not support databasetype:" + this.testQuery.getDataSource().getDbtype().getName());
		}

		return sql;
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
			StringBuffer buffer = new StringBuffer();
			for (int i = 1; i <= rs.getMetaData().getColumnCount(); i++) {
				data.add(rs.getObject(i));
				//System.out.println("===================:"+ rs.getObject(i).toString().trim());
				String columnName = rs.getMetaData().getColumnLabel(i).toUpperCase();
				String colType = rs.getMetaData().getColumnTypeName(i).trim().toUpperCase();
				if (keyMap.containsKey(columnName)) {
					if (rs.getObject(i) != null) {
						Object keyData = rs.getObject(i);
						String dataStr = keyData.toString();
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
						keyValue += "|[NULL]";
					}
				}
			}
//			if (buffer.length() > 0)
//				logger.debug("convert:" + buffer.toString());
			if(keyValue==null || keyValue.trim().length()==0)
			{
				logger.info("keyVaue is null or emtpy"+data.toString());
				continue;
			}
			if (keyValue.substring(0, 1).equals("|"))
				keyValue = keyValue.substring(1);
			if (keyValue == null || keyValue.trim().length() == 0 || keyValue.equals("[NULL]")) {
				logger.warn(String.format("key'value is null or empty. key:[%s] data:[%s]", keyMap.keySet(),
				        data.toString()));
				nullOrEmptyKeyCount = nullOrEmptyKeyCount + 1;
				continue;
			} else {
				if (map.containsKey(keyValue)) {
					logger.warn(String.format("key'value is Duplicated. key:[%s] keyValue:[%s] data:[%s]",
					        keyMap.keySet(), keyValue, data.toString(), map.get(keyValue).toString()));
					duplicatedKeyCount = duplicatedKeyCount + 1;
					continue;
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
//		System.out.println("map=============="+map);
		return map;
	}
}
