package com.jettech.db.adapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.EnumDatabaseType;
import com.jettech.db.DBConnectionManager;
import com.jettech.domain.DbModel;
import com.jettech.domain.DataField;
import com.jettech.entity.DataTable;
import com.jettech.entity.DataSchema;
import com.mchange.v2.c3p0.ComboPooledDataSource;

public abstract class AbstractAdapter {

	protected final Logger logger;
	{
		logger = LoggerFactory.getLogger(this.getClass());
	}

	/**
	 * 获取数据库连接
	 * 
	 * @param db
	 *            数据库类型
	 * @return
	 */
	public Connection getConnection(DbModel db) {
		if (db.getAutoCommit() != null) {
			return getConnectoin2(db, db.getAutoCommit());
		} else {
			return getConnectoin2(db, false);
		}
	}

	/**
	 * 关闭数据库连接
	 * 
	 * @param conn
	 */
	public void closeConnection(Connection conn) {
		destoryConnection(conn);
	}

	/**
	 * 创建连接(不自動提交)
	 * 
	 * @param dbModel
	 * @return
	 */

	/**
	 * 创建连接,带是否自动提交事务参数
	 * 
	 * @param dbModel
	 * @param autoCommit
	 * @return
	 */
	public Connection getConnection(DbModel dbModel, Boolean autoCommit) {
		Connection conn = null;
		try {
			// DbModel判空
			if (dbModel == null) {
				// 打印警告
				logger.warn("DbModel is null.");
				return null;
			}

			// 创建一个数据库连接(从连接池中获取)
			ComboPooledDataSource comboPooledDataSource = DBConnectionManager.getInstance().createConnection(dbModel);
			// 获取数据库连接
			conn = comboPooledDataSource.getConnection();
			// 判断数据库类型
			if (!dbModel.getDbtype().equals(EnumDatabaseType.Mysql) && autoCommit != null) {
				// 设置自动提交事务
				conn.setAutoCommit(autoCommit);
			}
		} catch (Exception e) {
			// 打印erro信息
			logger.error("createConnection error." + dbModel.getPrintInfo(), e);
		} finally {
		}
		return conn;
	}

	public Connection getConnectoin2(DbModel db, Boolean autoCommit) {
		try {
			if (db == null) {
				logger.warn("dbinfo is null.");
				return null;
			}
			Class.forName(db.getDriver());
			Connection conn = DriverManager.getConnection(db.getUrl(), db.getUsername(), db.getPassword());
			if (autoCommit != null)
				conn.setAutoCommit(autoCommit);
			return conn;
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("createConnection error." + db.getPrintInfo(), e);
		}
		return null;
	}

	// 销毁连接
	protected void destoryConnection(Connection conn) {
		try {
			if (conn != null) {
				conn.close();
				this.logger.info("数据库连接关闭");
			}
		} catch (SQLException e) {
			logger.error("close connection error.", e);
		}
	}

	/**
	 * 获取一个查询的字段
	 * 
	 * @param dbModel
	 * @param query
	 * @return
	 */
	public List<DataField> getQueryColumns(DbModel dbModel, String query) {
		List<DataField> list = new ArrayList<DataField>();
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		String sql = null;
		if (dbModel.getDbtype().equals(EnumDatabaseType.Mysql)
		        || dbModel.getDbtype().equals(EnumDatabaseType.Informix)) {
			sql = "select * from (" + query + ") as temp_table where 1=2";
		} else if (dbModel.getDbtype().equals(EnumDatabaseType.Oracle)) {
			sql = "select * from (" + query + ")  where 1=2";
		}

		try {
			conn = this.getConnection(dbModel);
			if (conn == null)
				return null;
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int ColNum = rsmd.getColumnCount();// 取得列的数量
			for (int i = 1; i <= ColNum; i++) {
				DataField col = new DataField();
//				col.setColumnName(rsmd.getColumnName(i));
				col.setColumnName(rsmd.getColumnLabel(i));
				col.setDataType(rsmd.getColumnTypeName(i));
				col.setDataLength(rsmd.getPrecision(i));
				list.add(col);
			}
			stmt.close();
		} catch (SQLException e) {
			logger.error("getQueryColumns sql error." + sql, e);
		} catch (Exception e) {
			logger.error("getQueryColumns error.", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	protected boolean doTestConnection(DbModel db, Boolean autoCommit) {
		Connection conn = null;
		try {
			conn = this.getConnection(db, autoCommit);
			if (conn != null && !conn.isClosed()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e1) {
				logger.error("close connection error", e);
			}
			return false;
		}
	}

	public boolean doTestConnection(DbModel db) {
		Connection conn = null;
		try {
			conn = this.getConnection(db);
			if (conn != null && !conn.isClosed()) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			try {
				if (conn != null)
					conn.close();
			} catch (SQLException e1) {
				logger.error("close connection error", e);
			}
			return false;
		}
	}

	/**
	 * 通过一个表名称获取该表的结构定义
	 * 
	 * @param dbModel
	 * @param tableName
	 * @return
	 */
	protected List<com.jettech.entity.DataField> getTableFields(DbModel dbModel, String tableName) {

		List<com.jettech.entity.DataField> list = new ArrayList<com.jettech.entity.DataField>();
		// Map<String, List<TableColumn>> map = new HashMap<String,
		// List<TableColumn>>();
		ResultSet rs = null;
		Statement stmt = null;
		Connection conn = null;
		String sql = null;
		try {
			conn = this.getConnection(dbModel);
			if (conn == null) {
				return null;
			}
			stmt = conn.createStatement();
			sql = "select * from " + tableName + " where 1=2";// 获取0条数据
			rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData(); // 获取元数据
			int ColNum = rsmd.getColumnCount();// 取得列的数量
			for (int i = 1; i <= ColNum; i++) {
				com.jettech.entity.DataField col = new com.jettech.entity.DataField();
				// if (tableName.contains(".")) {
				// col.setName(name);(tableName.substring(0,
				// tableName.indexOf(".")));
				// col.setTableName(tableName.substring(tableName.indexOf(".") +
				// 1));
				// } else {
				// col.setTableName(tableName);
				// }
//				col.setName(rsmd.getColumnName(i));
				col.setName(rsmd.getColumnLabel(i));
				col.setDataType(rsmd.getColumnTypeName(i));
				col.setDataLength(rsmd.getPrecision(i));
				list.add(col);
			}
			// map.put(tableName, list);
			stmt.close();

		} catch (SQLException e) {
			logger.error("getTableColumns sql error." + sql, e);
		} catch (Exception e) {
			logger.error("getTableColumns error.", e);
		} finally {
			try {
				if (rs != null) {
					rs.close();
				}
				if (stmt != null) {
					stmt.close();
				}
				if (conn != null) {
					conn.close();
				}
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return list;
	}

	public ResultSet query(Connection conn, String sql) {
		Statement stmt = null;
		ResultSet rs = null;
		try {
			stmt = conn.createStatement();
			rs = stmt.executeQuery(sql);
		} catch (SQLException e) {
			logger.error("query error.sql:" + sql, e);
		}
		return rs;

	}

	/**
	 * 获取数据连接下的数据库列表
	 * 
	 * @param databaseName
	 * @param conn
	 * @return
	 */
	abstract public List<DataSchema> getDatabase(String databaseName, Connection conn);

	/**
	 * 获取数据库中的表列表
	 * 
	 * @param databaseName
	 * @param conn
	 * @return
	 */
	abstract public List<DataTable> getTable(String databaseName, Connection conn);

	/**
	 * 获取表的字段列表
	 * 
	 * @param databaseName
	 * @param tableName
	 * @param conn
	 * @return
	 */
	abstract public List<com.jettech.entity.DataField> getField(String databaseName, String tableName, Connection conn);
	/*
	 * 获取一个数据库的所有字段
	 */
	abstract public List<com.jettech.entity.DataField> getAllField(String dbName, Connection conn); 
	/**
	 * 获取一个库中的某张表
	 * @param databaseName
	 * @param conn
	 * @return
	 */
	abstract public DataTable getTable(String databaseName,String tableName, Connection conn);
}
