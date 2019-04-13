package com.jettech.thread;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.jettech.EnumDatabaseType;
import com.jettech.EnumPageType;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.AdapterFactory;
import com.jettech.domain.CaseModel;
import com.jettech.domain.CompareCaseModel;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryModel;
import com.jettech.domain.DataField;

import ca.krasnay.sqlbuilder.SelectBuilder;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;

/**
 * 实现源数据和目标数据一致的分页数据获取
 * 
 * @author tan
 *
 */
public class UnionDataWorker extends PageDataWorker implements Runnable {

	public UnionDataWorker(BlockingQueue<BaseData> queue, QueryModel querySource, Integer pageSize) throws Exception {
		super(queue, querySource, pageSize);
	}

	QueryModel targetQuery;
	protected Map<String, FieldModel> targetKeyMap;
	private CaseModel testCase;
	private EnumPageType pageType;

	/**
	 * 构造一个通过源数据的队列来获取目标数据的执行器
	 * 
	 * @param queueTarget
	 * @param queryTarget1
	 * @param pageSize
	 * @param queueSource
	 * @throws Exception
	 */
	public UnionDataWorker(BlockingQueue<BaseData> queue, QueryModel querySource, Integer pageSize,
	        QueryModel targetQuery) throws Exception {
		this(queue, querySource, pageSize);
		this.targetQuery = targetQuery;
		this.targetKeyMap = this.getKeyMap(targetQuery);
	}

	public UnionDataWorker(BlockingQueue<BaseData> queue, CompareCaseModel testCase, Integer pageSize,
	        QueryModel targetQuery) throws Exception {
		this(queue, testCase.getSourceQuery(), testCase.getPageSize(), testCase.getTargetQuery());
		this.testCase = testCase;
	}

	@Override
	public void run() {
		logger.info("启动生产者线程！");
		Connection sourceConn = null;
		AbstractAdapter sourceAdapter = null;
		String sourceDbInfo = getDbInfo(dataSource);
		try {
			// 获取数据库连接
			sourceAdapter = AdapterFactory.create(dataSource.getDatabaseType());
			if (sourceAdapter == null) {
				System.out.print("creat adapter failed. " + sourceDbInfo);
				return;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		Connection targetConn = null;
		AbstractAdapter targetAdapter = null;
		String targetDbInfo = getDbInfo(targetQuery.getDataSource());

		try {
			// 获取数据库连接
			targetAdapter = AdapterFactory.create(targetQuery.getDataSource().getDatabaseType());
			if (targetAdapter == null) {
				System.out.print("creat adapter failed. " + targetDbInfo);
				return;
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}

		try {
			sourceConn = sourceAdapter.getConnection(dataSource);
			if (sourceConn == null || sourceConn.isClosed()) {
				logger.error("create connection failed." + sourceDbInfo);
				return;
			}
			targetConn = targetAdapter.getConnection(targetQuery.getDataSource());
			if (targetConn == null || targetConn.isClosed()) {
				logger.error("create connection failed." + targetDbInfo);
				return;
			}
			// 校验分页参数
			if (pageIndex <= 0) {
				pageIndex = 1;
			}

			Integer maxNullKeyCount = testQuery.getMaxNullKeyCount();
			Integer maxDuplKeyCount = testQuery.getMaxDuplicatedKeyCount();
			// Map<String, List<Object>> map = null;
			int count = 0;
			while (true) {
				// UnionPageData unionPage = (UnionPageData) queue.peek();
				// if (unionPage == null) {
				// Thread.sleep(100);
				// continue;
				// }

				// PreparedStatement pStmt = null;

				// 设置最大查询到第几条记录
				int maxIndex = pageIndex * pageSize;
				// 游标移动到要输出的第一条记录,此处为获取数据的前一行
				int startIndex = (pageIndex - 1) * pageSize;
				logger.info("########### pageIndex:" + pageIndex + " pageSize:" + pageSize + " startIndex:" + startIndex
				        + " maxIndex:" + maxIndex);

				// String soruceSQL = getQuerySQL();
				// soruceSQL = getSqlWithOrderBy(soruceSQL);
				// 判断使用的分页类型
				String sourceSQL = "";
				switch (testCase.getPageType()) {
				case None:
					sourceSQL = getPageSQLNatural(testQuery, pageIndex, pageSize);
					break;
				case PrimaryKey:
				case UniqueIndex:
					break;
				case QueryKey:
				default:
					sourceSQL = getPageSQL(testQuery, pageIndex, pageSize);
				}
				logger.info("Source SQL:" + sourceSQL);
				// pStmt = sourceConn.prepareStatement(soruceSQL);
				Statement pStmt = sourceConn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
				        ResultSet.CONCUR_READ_ONLY);

				// 使用setMaxRows,absolute进行分页存在大数据量表时，内存占用过大的问题
				// ORACLE JDBC对于翻页支持非常不友好，
				// 因为它会在rs.absoulte之前就会把setMaxRow后的查询结果集全部载入应用服务器内存，在数据量较大时会导致内存溢出。
				// pStmt.setMaxRows(maxIndex);
				ResultSet rs = pStmt.executeQuery(sourceSQL);
				// ResultSet rs = pStmt.executeQuery();
				if (rs == null) {
					logger.info("查询返回空ResultSet对象,终止数据生产");
					break;
				} else {
					// 获得查询的列情况,并赋值给Query的queyrColumns属性,用于为定义查询的列详情时
					List<DataField> colsList = getQueryColumns(this.testQuery, rs);
				}
				// rs.relative(startIndex);
				// 游标移动到要输出的第一条记录
				// if
				// (testQuery.getDbModel().getDatabaseType().equals("oracle")) {
				// rs.absolute(startIndex + 1);// oralce需要从序号1开始
				// } else if
				// (testQuery.getDbModel().getDatabaseType().equals("mysql")) {
				// rs.absolute(startIndex);// mysql需要从序号0开始
				// } else {
				// rs.absolute(startIndex);
				// }

				if (maxNullKeyCount != null && maxNullKeyCount > 0 && nullOrEmptyKeyCount > maxNullKeyCount) {
					logger.info(String.format("读数中断,空键值列数量超过允许:current:[%d] max:[%d]", nullOrEmptyKeyCount,
					        maxNullKeyCount));
					break;
				}

				// 如果有超过[#100]的重复键值,退出获取数据过程
				if (maxDuplKeyCount != null && maxDuplKeyCount > 0 && duplicatedKeyCount > maxDuplKeyCount) {
					logger.info(String.format("读数中断,重复键值列数量超过允许:current:[%d] max:[%d]", duplicatedKeyCount,
					        maxDuplKeyCount));
					break;
				}

				logger.info(String.format("当前有数据:[%d]组,开始获取数据", queue.size()));
				// count++;C
				Map<String, List<Object>> map = getDataRows(rs, keyMap);
				logger.info("获取源数据行数::" + map.size());
				if (map.size() == 0) {
					logger.info("数据已读取完毕");
					break;
				} else {
					UnionPageData page = new UnionPageData();
					page.setTestQuery(testQuery);
					page.setMap(map);
					page.setPageIndex(pageIndex);
					getMixMaxKey(page, map.keySet());

					// 读取目标数据
					Map<String, FieldModel> targetKeyMap = getKeyMap(targetQuery);
					String targetSQL = getTargetSQL2(page);

					PreparedStatement pStmtTarget = null;
					pStmtTarget = targetConn.prepareStatement(targetSQL);
					ResultSet rsTarget = pStmtTarget.executeQuery();
					// 获得查询的列情况,并赋值给Query的queyrColumns属性,用于为定义查询的列详情时
					List<DataField> targetColsList = getQueryColumns(this.targetQuery, rsTarget);

					Map<String, List<Object>> mapTarget = getDataRows(rsTarget, targetKeyMap);
					logger.info(String.format("获取目标数据行数:[%d]", mapTarget.size()));
					page.setMapTarget(mapTarget);
					page.setQueryTarget(targetQuery);

					// 如果BlockQueue没有空间,则调用此方法的线程被阻断直到BlockingQueue里面有空间再继续.
					queue.put(page);
					logger.info(String.format("当前有数据:[%d]组, pageIndex:[%d] pageSize:[%d]", queue.size(), pageIndex,
					        pageSize));
					pageIndex += 1;// 分页序号+1
				}
				if (_isRunning == false) {
					logger.info("#########收到终止信号");
					break;
				}
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			// Thread.currentThread().interrupt();
			logger.error("thread interrupted error.", e);
		} catch (Exception e) {
			logger.error("get data error.", e);
		} finally {
			logger.info("退出生产者线程！");
			sourceAdapter.closeConnection(sourceConn);
			targetAdapter.closeConnection(targetConn);
			_isRunning = false;
		}
	}

	private String getTargetSQL2(UnionPageData page) {
		// 暂未处理查询的pageType定义
		// 原始的SQL
		// QueryModel queryTarget = page.getQueryTarget();
		String targetSQL = targetQuery.getSqlText();
		String pageText = targetQuery.getPageText();
		// 未定义分页字段/分页字段无效,使用key进行分页
		if (pageText == null || pageText.trim().length() == 0) {
			pageText = targetQuery.getKeyText();
		}

		EnumDatabaseType databaseType = targetQuery.getDataSource().getDatabaseType();
		String whereCol = "";
		// SyBase,SqlServer字符串的连接方式不同
		if (pageText.contains(",")) {
			// 存在多个分页的列
			switch (databaseType) {
			case SyBase:
				whereCol = pageText.replace(",", "+");
				break;
			case Mysql:
			case Oracle:
			case DB2:
			default:
				whereCol = "CONCAT(" + pageText + ")";
			}
		} else {
			whereCol = pageText;
		}

		StringBuilder sqlBuilder = new StringBuilder();
		sqlBuilder.append("SELECT * FROM (");
		sqlBuilder.append(targetSQL);
		sqlBuilder.append(")");
		// 不同的数据库,子查询的别名方式不同
		switch (databaseType) {
		case Mysql:
			sqlBuilder.append(" AS T");
			break;
		case SyBase:
		case Oracle:
		case DB2:
		default:
			sqlBuilder.append(" T");
		}

		sqlBuilder.append(" WHERE " + whereCol + " BETWEEN '" + page.getMinKey() + "' AND '" + page.getMaxKey() + "'");

		String result = sqlBuilder.toString();
		return result;
	}

	private String getTargetSQL(UnionPageData page) {
		String targetSQL = targetQuery.getSqlText();
		// 增加补充查询条件
		StringBuilder builder = new StringBuilder();
		String[] destKeys = targetQuery.getKeyText().split(",");
		// TODO:需要根据不同的数据库，不同的数据类型来构造到目标数据库的分页查询SQL
		// 如果是简易模式,这里还不能通过查询获取到key字段的完整定义
		for (String key : targetKeyMap.keySet()) {
			FieldModel field = targetKeyMap.get(key);
			// if (field.getDataType().equals("")) {
			//
			// } else {
			builder.append(" and " + key + " >='" + page.getMinKey() + "'");
			builder.append(" and " + key + " <='" + page.getMaxKey() + "'");
			// }
		}

		// 简易模式
		builder = new StringBuilder();
		for (int i = 0; i < destKeys.length; i++) {
			String key = destKeys[i];
			builder.append(
			        " and " + key + ">='" + page.getMinKey() + "' and " + key + " <= '" + page.getMaxKey() + "'");
		}
		String result = "";
		if (targetSQL.toLowerCase().contains(" where ")) {
			// sql中已经包含where条件，增加条件
			result = targetSQL + builder.toString();
		} else {
			// sql中不包含where条件，增加where子句
			result = builder.toString();
			result = result.substring(4);// 去掉"and"
			result = targetSQL + " where " + result;
		}
		logger.info("获取目标数据的SQL:" + result);
		return result;
	}

	private String getQuerySQL(String minKey, String maxKey) {
		String sql = getQuerySQL();
		sql = getBetweenSQL(sql, minKey, maxKey);
		return sql;
	}

	private String getSqlWithOrderBy(String sql) {
		// String sql = getQuerySQL();
		String sql2 = sql;
		// 忽略大小写的写法
		String regEx = "{1,}order {1,}by {1,}";
		Pattern pattern = Pattern.compile(regEx, Pattern.CASE_INSENSITIVE);
		Matcher matcher = pattern.matcher(sql.toLowerCase());
		// 查找字符串中是否有匹配正则表达式的字符/字符串
		if (matcher.find()) {
			logger.info("find order by ");
		} else {
			logger.info("not find order by ");
			sql2 += " order by " + testQuery.getKeyText();
		}

		return sql2;
	}

	/*
	 * 不排序下的分页
	 */
	private String getPageSQLNatural(QueryModel query, int pageIndex, int pageSize) throws Exception {

		String sql = query.getSqlText();

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
			sql = "SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (" + sql + ") A WHERE ROWNUM <= " + pageIndex * pageSize
			        + " ) WHERE RN >= " + ((pageIndex - 1) * pageSize + 1);

			// SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (SELECT * FROM
			// TABLE_NAME) A ) WHERE RN BETWEEN 21 AND 40
			// sql="SELECT * FROM ( SELECT A.*, ROWNUM RN FROM (" +sql+") A )
			// WHERE RN BETWEEN "+((pageIndex-1) * pageSize +1)+" AND "
			// +pageIndex * pageSize;
			break;
		case DB2:
			break;
		case Informix:
			break;
		default:
			throw new Exception("not support databasetype:" + this.testQuery.getDataSource().getDbtype().getName());
		}

		return sql;

	}

	/**
	 * 
	 * @param orgSql
	 * @param minKey
	 * @param maxKey
	 * @return
	 */
	private String getBetweenSQL(String orgSql, String minKey, String maxKey) {
		StringBuilder builder = new StringBuilder();
		// String[] destKeys = testQuery.getKeys().split(",");
		// for (int i = 0; i < destKeys.length; i++) {
		// String key = destKeys[i];
		// if (i > 0)
		// builder.append(" and ");
		// builder.append("(" + key + " between " + minKey + " and " + maxKey +
		// ")");
		// }
		String sql = orgSql;
		if (orgSql.toLowerCase().contains("where")) {
			sql = sql + " and " + builder.toString();
		} else {
			sql = sql + " where " + builder.toString();
		}
		return sql;
	}
}
