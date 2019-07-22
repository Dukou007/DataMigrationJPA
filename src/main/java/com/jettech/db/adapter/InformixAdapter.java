package com.jettech.db.adapter;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.jettech.EnumDatabaseType;
import com.jettech.domain.DbModel;
import com.jettech.domain.JobInfo;
import com.jettech.domain.DataField;
import com.jettech.entity.DataTable;
import com.jettech.entity.DataSchema;

/**
 * IBM Informix 数据库的适配器
 * 
 * @author tan
 *
 */
public class InformixAdapter extends AbstractAdapter {

	private static Map<Integer, String> _colTypes = new TreeMap<>();

	static {
		_colTypes.put(0, "CHAR");
		_colTypes.put(1, "SMALLINT");
		_colTypes.put(2, "INTEGER");
		_colTypes.put(3, "FLOAT");
		_colTypes.put(4, "SMALLFLOAT");
		_colTypes.put(5, "DECIMAL");
		_colTypes.put(6, "SERIAL*");
		_colTypes.put(7, "DATE");
		_colTypes.put(8, "MONEY");
		_colTypes.put(9, "NULL");
		_colTypes.put(10, "DATETIME");
		_colTypes.put(11, "BYTE");
		_colTypes.put(12, "TEXT");
		_colTypes.put(13, "VARCHAR");
		_colTypes.put(14, "INTERVAL");
		_colTypes.put(15, "NCHAR");
		_colTypes.put(16, "NVARCHAR");
		_colTypes.put(17, "INT8");
		_colTypes.put(18, "SERIAL8*");
		_colTypes.put(19, "SET");
		_colTypes.put(20, "MULTISET");
		_colTypes.put(21, "LIST");
		_colTypes.put(22, "Unnamed ROW");
		_colTypes.put(40, "Variable-length opaque type(LVARCHAR)");
		_colTypes.put(41, "Fixed-length opaque type 2");
		_colTypes.put(43, "LVARCHAR (client-side only)");
		_colTypes.put(45, "BOOLEAN");
		_colTypes.put(52, "BIGINT");
		_colTypes.put(53, "BIGSERIAL");
		_colTypes.put(53, "IDSSECURITYLABEL 2");
		_colTypes.put(4118, "NamedROW");
		// 0 = CHAR
		// 1 = SMALLINT
		// 2 = INTEGE R
		// 3 = FLOAT
		// 4 = SMALLFLOAT
		// 5 = DECIMAL
		// 6 = SERIAL *
		// 7 = DATE
		// 8 = MONEY
		// 9 = NULL
		// 10 = DATETIME
		// 11 = BYTE
		// 12 = TEXT
		// 13 = VARCHAR
		// 14 = INTERVAL
		// 15 = NCHAR
		// 16 = NVARCHAR
		// 17 = INT8
		// 18 = SERIAL8 *
		// 19 = SET
		// 20 = MULTISET
		// 21 = LIST
		// 22 = Unnamed ROW
		// 40 = Variable-length opaque type (LVARCHAR)
		// 41 = Fixed-length opaque type 2
		// 43 = LVARCHAR (client-side only)
		// 45 = BOOLEAN
		// 52 = BIGINT
		// 53 = BIGSERIAL
		// 2061 = IDSSECURITYLABEL 2
		// 4118 = Named ROW
		//
		// 如果，某字段要求非空，则在原数字上加256，
		// 如：某字段coltype=262,那么,262-256=6，该字段就应该为：SERIAL not NULL 类型！

	}

	private static String convertColType(Integer colType) {
		if (colType == null)
			return null;
		if (_colTypes.containsKey(colType)) {
			return _colTypes.get(colType).toString();
		} else if (colType >= 256 & _colTypes.containsKey(colType - 256)) {
			return _colTypes.get(colType - 256).toString();
		}
		return colType.toString();

	}

	public static final String DEFAULT_DRIVER = "com.informix.jdbc.IfxDriver";
	private static final String _DEFAULT_URL = "jdbc:informix-sqli://192.168.146.128:9090/testDB:INFORMIXSERVER=ol_demo1;user=informix;password=P@ssw0rd";
	//
	// @Override
	// public String assembleSQL(String paramString, Connection paramConnection,
	// JobInfo paramJobInfo) throws SQLException {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// @Override
	// public void executeSQL(String sql, Connection conn) throws SQLException {
	// // TODO Auto-generated method stub
	// PreparedStatement pst = conn.prepareStatement("");
	// String[] sqlList = sql.split(";");
	// for (int index = 0; index < sqlList.length; index++) {
	// pst.addBatch(sqlList[index]);
	// }
	// pst.executeBatch();
	// conn.commit();
	// pst.close();
	// }

	public static void main(String[] args) {
		testGetTableColumns();
		// testConnection1();
		// testConnect2();
		// testConnection();
		// testExecute();
		// testInsertData();
		// testGetTableColumns();
	}

	private static void testGetTableColumns() {
		DbModel db = new DbModel();
		db.setDbtype(EnumDatabaseType.Informix);
		db.setDriver(DEFAULT_DRIVER);
		db.setUrl("jdbc:informix-sqli://192.168.146.128:9090/testDB:INFORMIXSERVER=ol_demo1");
		db.setUsername("informix");
		db.setPassword("P@ssw0rd");
		db.setAutoCommit(null);// informix必须设置autocommit为null
		InformixAdapter informixAdapter = new InformixAdapter();
		List<com.jettech.entity.DataField> list = informixAdapter.getTableFields(db, "newtable");
		for (com.jettech.entity.DataField col : list) {
			System.out.println(
			        col.getName() + ":" + col.getDataType() + " " + col.getDataLength() + " " + col.getDataPrecision());
		}
	}

	private static void testInsertData() {
		StringBuilder builder = new StringBuilder();
		List<String> sqlList = new ArrayList<String>();
		// sqlList.add("delete from t1");
		// 999999
		for (int i = 1000001; i < 2000000; i++) {
			// if (builder.length() > 0) {
			// builder.append(";");
			// }
			String sql = "insert into t1(col1,col2,col3) values(" + i + ",'row" + i + "',current)";
			// builder.append(sql);
			sqlList.add(sql);
		}
		// String sqlStr = builder.toString();
		// System.out.println(sqlStr);
		Connection conn = null;

		// String url =
		// "jdbc:informix-sqli://192.168.146.128:9090/testDB:INFORMIXSERVER=ol_demo1;user=informix;password=P@ssw0rd";

		InformixAdapter adapter = new InformixAdapter();
		try {
			conn = (new InformixAdapter()).createConnection(DEFAULT_DRIVER, _DEFAULT_URL);
			long start = new Date().getTime();
			int i = 0;
			for (String sql : sqlList) {
				// System.out.println(sql);
				Statement stmt = conn.createStatement();
				stmt.executeUpdate(sql);
				stmt.close();
				i++;
				if (i / 10000 == 0) {
					// conn.commit();//Informx不支持 将连接的自动提交设置为false
					System.out.println("batch at:" + i);
				}
			}
			// conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
			// ResultSet.CONCUR_UPDATABLE);
			// adapter.executeSQL(sql, conn);
			System.out.println("execute sql escaped time: " + (new Date().getTime() - start) + "ms");
			// execute sql escaped time: 16958ms insert into database
			// 100000'rows
			// conn.commit();
		} catch (SQLException e) {
			e.printStackTrace();
			try {
				conn.rollback();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
	}

	private static void testConnection1() {
		DbModel db = new DbModel();
		db.setDbtype(EnumDatabaseType.Informix);
		db.setDriver(DEFAULT_DRIVER);
		db.setUrl("jdbc:informix-sqli://192.168.146.128:9090/testDB:INFORMIXSERVER=ol_demo1");
		db.setUsername("informix");
		db.setPassword("P@ssw0rd");
		db.setAutoCommit(null);// informix必须设置autocommit为nulls
		// Connection conn = (new InformixAdapter()).createConnection(db);
		// String url =
		// "jdbc:informix-sqli://192.168.48.150:9090/testDB:INFORMIXSERVER=ol_demo1;user=informix;password=P@ssw0rd";
		Connection conn = null;
		PreparedStatement ps = null;
		ResultSet rs = null;
		try {
			// jdbc:informix-sqli://192.168.146.128:9090/testDB:INFORMIXSERVER=ol_demo1;user=informix;password=P@ssw0rd";
			conn = (new InformixAdapter()).createConnection(DEFAULT_DRIVER, _DEFAULT_URL);
			String sql1 = "select * from t1";
			ps = conn.prepareStatement(sql1);
			// Statement stmt =
			// conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
			// ResultSet.CONCUR_UPDATABLE);
			// rs = stmt.executeQuery(sql1);
			rs = ps.executeQuery();
			// System.out.println(rs.first());
			int i = 0;
			while (rs.next()) {
				if (rs.getObject(1) != null) {
					// list.add(rs.getObject(1).toString());// + "," +
					// rs.getObject(2).toString());
					System.out.println(rs.getObject(1).toString() + "|" + rs.getObject(2).toString());
					i++;
					if (i > 10)
						break;
				}
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			// } catch (InstantiationException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (IllegalAccessException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
			// } catch (ClassNotFoundException e) {
			// // TODO Auto-generated catch block
			// e.printStackTrace();
		} finally {
			try {
				if (rs != null) {
					rs.close();
					rs = null;
				}
				if (ps != null) {
					ps.close();
					ps = null;
				}
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}

	}

	private static void testPage() {
		Connection conn = null;
		try {
			Class.forName(DEFAULT_DRIVER).newInstance();
			conn = DriverManager.getConnection(_DEFAULT_URL);
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
			String sql = "select * from t1 order by col1";
			System.out.println(sql);
			sql = sql.replaceFirst("select", "select skip 200 first 1000 ");
			System.out.println(sql);

			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int ColNum = rsmd.getColumnCount();// 取得列的数量
			System.out.println("ColNum:" + ColNum);
			for (int i = 1; i <= ColNum; i++) {
				System.out.println("col:" + rsmd.getColumnLabel(i) + " type:" + rsmd.getColumnTypeName(i) + " prec:"
				        + rsmd.getPrecision(i));
			}

			int i = 0;
			int first = 0;
			int last = 0;
			while (rs.next()) {
				if (i == 0) {
					first = rs.getInt(1);
				} else {
					last = rs.getInt(1);
				}
				i++;
			}
			System.out.println("page:" + i);
			System.out.println("first:" + first);
			System.out.println("last:" + last);

			rs.close();
			stmt.close();
			conn.close();

		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
				if (conn != null && !conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

	}

	private static void testConnect2() {
		String sql = "select * from t1";

		// 连接字符串，格式： "jdbc:数据库驱动名称:
		// 数据库服务器ip:端口号/数据库名称:INFORMIXSERVER=服务器名;
		// user=用户名;password=密码"

		// dbc:informix-sqli://[{ip-address|host-name}:{port-number|service-name}][/dbname]:
		// INFORMIXSERVER=servername[{;user=user;password=password]
		// |CSM=(SSO=database_server@realm,ENC=true)}
		// [;name=value[;name=value]...]
		// logger.info("testConnect to informix");
		try {
			Class.forName(DEFAULT_DRIVER).newInstance();

			Connection conn = DriverManager.getConnection(_DEFAULT_URL);
			Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);

			System.out.println(conn.getMetaData().getCatalogTerm());
			System.out.println(conn.getMetaData().getDriverName());
			System.out.println(conn.getMetaData().getDatabaseProductName());
			// System.out.println( conn.getSchema());

			ResultSet rs = stmt.executeQuery(sql);
			ResultSetMetaData rsmd = rs.getMetaData();
			int ColNum = rsmd.getColumnCount();// 取得列的数量
			System.out.println("ColNum:" + ColNum);
			for (int i = 1; i <= ColNum; i++) {
				System.out.println("col:" + rsmd.getColumnLabel(i) + " type:" + rsmd.getColumnTypeName(i) + " prec:"
				        + rsmd.getPrecision(i));
			}
			rs.close();
			stmt.close();
			conn.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void closeConnection(Connection conn) {
		this.destoryConnection(conn);
	}

	@Override
	public List<DataSchema> getDatabase(String databaseName, Connection conn) {
		List<DataSchema> databaselist = new ArrayList<DataSchema>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("SELECT name, owner FROM sysdatabases ");
			if (!databaseName.equals("")) {
				sql.append(" where name='").append(databaseName).append("' ");
			}
			System.out.println("sql=" + sql);
			String usedatabase = "DATABASE sysmaster ";
			query(usedatabase, conn);
			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称

			while (rs.next()) {
				DataSchema databaseobj = new DataSchema();
				databaseobj.setName(rs.getString("name"));
				databaselist.add(databaseobj);
				// System.out.println(rs.getString("TABLE_SCHEMA"));
			}
		} catch (SQLException e) {

		}
		return databaselist;
	}

	@Override
	public List<DataTable> getTable(String databaseName, Connection conn) {
		List<DataTable> tablelist = new ArrayList<DataTable>();
		try {
			Statement stmt = conn.createStatement();
			// StringBuffer sql=new StringBuffer("select tabname from
			// syscat.tables where tabschema = current schema");
			StringBuffer sql = new StringBuffer("SELECT tabname, owner, tabid FROM systables WHERE tabtype = 'T'  ");
			// sql.append(" where tabschema='").append(databaseName).append("'
			// ");

			String usedatabase = "DATABASE " + databaseName + " ";
			query(usedatabase, conn);
			// System.out.println("table--------------sql==="+sql);
			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称

			while (rs.next()) {
				DataTable tableobj = new DataTable();
				tableobj.setName(rs.getString("tabname"));
				// System.out.println("111table--------------tabname="+rs.getString("tabname"));
				// tableobj.setCreateTime(rs.getDate("CREATE_TIME"));
				// System.out.println("222table--------------CREATE_TIME");
				// tableobj.setEditTime(rs.getDate("ALTER_TIME"));
				// System.out.println("333table--------------ALTER_TIMe");
				tablelist.add(tableobj);
				// System.out.println(rs.getString("TABLE_SCHEMA"));
			}
		} catch (SQLException e) {

		}
		return tablelist;
	}

	@Override
	public List<com.jettech.entity.DataField> getField(String databaseName, String tableName, Connection conn) {
		return getTableFields(conn, tableName);

		// return getTableFileds(databaseName, tableName, conn);
	}

	private List<com.jettech.entity.DataField> getTableFileds(String databaseName, String tableName, Connection conn) {
		List<com.jettech.entity.DataField> fieldlist = new ArrayList<com.jettech.entity.DataField>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("SELECT t.tabname,c.colname,c.coltype,c.collength");
			sql.append(" FROM syscolumns c, systables t ");
			sql.append(" WHERE c.tabid = t.tabid AND t.tabname='").append(tableName.toLowerCase()).append("'");

			String usedatabase = "DATABASE " + databaseName + " ";
			query(usedatabase, conn);

			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称
			System.out.println("getField sql=[" + sql + "]");

			while (rs.next()) {
				com.jettech.entity.DataField fieldobj = new com.jettech.entity.DataField();
				fieldobj.setTalbeName(rs.getString("tabname"));
				fieldobj.setName(rs.getString("colname"));
				fieldobj.setDataType(convertColType(rs.getInt("coltype")));
				fieldobj.setDataLength(rs.getInt("collength"));
				fieldlist.add(fieldobj);
			}

		} catch (SQLException e) {

		}
		return fieldlist;
	}

	public void query(String sql, Connection conn) throws SQLException {
		PreparedStatement pstmt;
		pstmt = conn.prepareStatement(sql);
		pstmt.execute();
		pstmt.close();
	}
	// @Override
	// public ResultSet query(Connection conn, String sql) {
	// return this.doQuery(conn, sql);
	// }
	//
	// @Override
	// public List<TableColumn> getQueryColumns(DbInfo db, String query) {
	// return this.getQueryColumns(db, query);
	// }
	//
	// @Override
	// public boolean testConnection(DbInfo db) {
	// return doTestConnection(db, null);
	// }

	@Override
	public List<com.jettech.entity.DataField> getAllField(String dbName, Connection conn) {
		List<com.jettech.entity.DataField> fieldlist = new ArrayList<com.jettech.entity.DataField>();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer(
			        "SELECT t.tabname,c.colname,c.coltype,c.collength FROM syscolumns c, systables t  ");
			sql.append(" WHERE c.tabid = t.tabid ");

			String usedatabase = "DATABASE " + dbName + " ";
			query(usedatabase, conn);

			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称
			System.out.println("sql=" + sql);

			while (rs.next()) {
				com.jettech.entity.DataField fieldobj = new com.jettech.entity.DataField();
				fieldobj.setTalbeName(rs.getString("tabname"));
				fieldobj.setName(rs.getString("colname"));
				fieldobj.setDataType(convertColType(rs.getInt("coltype")));
				fieldobj.setDataLength(rs.getInt("collength"));
				fieldobj.setDataPrecision(Integer.valueOf(rs.getInt("collength")));
				fieldlist.add(fieldobj);
			}

		} catch (SQLException e) {

		}
		return fieldlist;
	}

	@Override
	public DataTable getTable(String databaseName, String tableName, Connection conn) {
		DataTable tableobj = new DataTable();
		try {
			Statement stmt = conn.createStatement();
			StringBuffer sql = new StringBuffer("SELECT tabname, owner, tabid FROM systables WHERE tabtype = 'T'  ");
			sql.append(" and tabname='").append(tableName.toLowerCase()).append("' ");
			String usedatabase = "DATABASE " + databaseName + " ";
			query(usedatabase, conn);
			ResultSet rs = stmt.executeQuery(sql.toString());
			// user 为你表的名称

			while (rs.next()) {
				tableobj.setName(rs.getString("tabname"));
			}
		} catch (SQLException e) {

		}
		return tableobj;
	}

	@Override
	public Integer getTableCount(String sourceTableName, Connection conn, String schema) {
		return null;
	}

}
