package com.jettech.service.Impl;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.EnumDatabaseType;
import com.jettech.EnumOptType;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.DB2Adapter;
import com.jettech.db.adapter.InformixAdapter;
import com.jettech.db.adapter.MySqlAdapter;
import com.jettech.db.adapter.OracleAdapter;
import com.jettech.db.adapter.SyBaseAdapter;
import com.jettech.domain.DbModel;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.MainSqlRecord;
import com.jettech.entity.MetaHistory;
import com.jettech.entity.MetaHistoryItem;
import com.jettech.entity.TestCase;
import com.jettech.entity.DataField;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.entity.DataTable;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataSchemaRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.DataTableRepository;
import com.jettech.repostory.MainSqlRecordRepository;
import com.jettech.repostory.MetaHistoryItemRepository;
import com.jettech.repostory.MetaHistoryRepository;
import com.jettech.repostory.TestCaseRepository;
import com.jettech.repostory.TestResultItemRepository;
import com.jettech.repostory.TestResultRepository;
import com.jettech.service.IMetaDataManageService;
import com.jettech.sqlbuilder.ISqlBuilerAdapter;
import com.jettech.sqlbuilder.MysqlBuilderAdapter;
import com.jettech.sqlbuilder.OracleBuilderAdapter;
import com.jettech.util.DateUtil;
import com.jettech.util.ExcelUtil;
import com.jettech.util.SplitData;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.sql.Fromlist;
import com.jettech.vo.sql.Grouplist;
import com.jettech.vo.sql.Havinglist;
import com.jettech.vo.sql.Joinlist;
import com.jettech.vo.sql.Leftjoinlist;
import com.jettech.vo.sql.Orderbylist;
import com.jettech.vo.sql.Selectlist;
import com.jettech.vo.sql.Sourceselectsql;
import com.jettech.vo.sql.SqlVo;
import com.jettech.vo.sql.Wherelist;

@Service
public class MetaDataManageServiceImpl implements IMetaDataManageService {

	private static Logger logger = LoggerFactory
			.getLogger(MetaDataManageServiceImpl.class);

	@Autowired
	private DataSourceRepository dataSourceRepository;
	@Autowired
	private DataSchemaRepository testDatabaseRepository;
	@Autowired
	private DataTableRepository testTableRepository;
	@Autowired
	private DataFieldRepository testFieldRepository;
	@Autowired
	private MainSqlRecordRepository mainSqlRecordRepository;

	@Autowired
	private MetaHistoryRepository metaHistoryRepository;
	@Autowired
	private MetaHistoryItemRepository metaHistoryItemRepository;
	@Autowired
	private TestCaseRepository testCaseRepository;
	@Autowired
	private TestResultRepository testResultRepository;
	@Autowired
	private TestResultItemRepository testResultItemRepository;

	private boolean updateDB = false;
	private boolean updateTable = false;

	
	

	@Override
	public void addOneDatasource(DataSource ds) throws SQLException {
		dataSourceRepository.save(ds);
	}

	@Override
	public void updateOneDatasource(DataSource ds) throws SQLException {
		dataSourceRepository.update(ds.getName(), ds.getDatabaseType(),
				ds.getDatabaseVersion(), ds.getHost(), ds.getUserName(),
				ds.getPassword(), ds.getCharacterSet(), ds.getDefaultSchema(),
				ds.getDriver(), ds.getUrl(), ds.getPort(), new Date(),
				ds.getEditUser(), ds.getSid(), ds.getId());
	}


	public void syncSchemaMetaExistTable(AbstractAdapter adapter,
			Connection conn, DataSource ds, DataSchema testDatabase) {
		// String dbName = ds.getDefaultSchema();
		testDatabaseRepository.save(testDatabase);
		// TestDatabase testDatabase =
		// testDatabaseRepository.findByName(dbName);

		List<DataTable> testtableList = testDatabase.getDataTables();
		testTableRepository.saveAll(testtableList);
		syncFields(adapter, conn, testDatabase.getName(), testtableList);
	}
	private void syncFields(AbstractAdapter adapter, Connection conn,
			String dbName, List<DataTable> testtableList) {
		DataSchema db = testDatabaseRepository.findByName(dbName);
		for (int j = 0; j < testtableList.size(); j++) {
			long start = new Date().getTime();
			String tableName = testtableList.get(j).getName();
			List<DataField> testfieldList = adapter.getField(dbName, tableName,
					conn);
			List<DataField> existfList = testFieldRepository.findByTBName(
					tableName, dbName);
			List<DataField> existfieldList = new ArrayList<>();
			// 过滤掉已删除的记录，保存未被删除的记录
			if (existfList != null) {
				for (int i = 0; i < existfList.size(); i++) {
					if (existfList.get(i).getDeleted() == null
							|| (existfList.get(i).getDeleted() != null && existfList
									.get(i).getDeleted() != true)) {
						existfieldList.add(existfList.get(i));
					}
				}
			}

			// 新增表，新增字段
			if (existfieldList == null || existfieldList.size() == 0) {
				updateTable = true;
				DataTable testTable = testTableRepository.findByNameAndDBName(
						testtableList.get(j).getName(), dbName);
				List<DataField> saveFieldList = new ArrayList<>();
				List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
				for (int m = 0; m < testfieldList.size(); m++) {
					DataField tf = testfieldList.get(m);
					tf.setDataTable(testTable);
					tf.setEditTime(new Date());
					saveFieldList.add(tf);
					MetaHistoryItem metaHistoryItem = new MetaHistoryItem();
					metaHistoryItem.setCreateTime(new Date());
					metaHistoryItem.setTableName(testTable.getName());
					metaHistoryItem.setOrgFiledName("");
					metaHistoryItem.setRefFieldName(tf.getName());
					metaHistoryItem.setOptFieldType(EnumOptType.Add);
					metaHistoryItem.setOptTableType(EnumOptType.Add);
					metaHistoryItem.setInfo("新增表新增字段");
					metaHistoryItemList.add(metaHistoryItem);

				}
				testFieldRepository.saveAll(saveFieldList);

				logger.info("save table' fields,table:" + testTable.getName()
						+ " use " + DateUtil.getEclapsedTimesStr(start));
				MetaHistory metaHistory = new MetaHistory();
				metaHistory.setCreateTime(new Date());
				metaHistory.setDataSchema(db);
				metaHistoryRepository.save(metaHistory);
				for (int q = 0; q < metaHistoryItemList.size(); q++) {
					MetaHistoryItem mi = metaHistoryItemList.get(q);
					mi.setMetaHistory(metaHistory);
					metaHistoryItemRepository.save(mi);
				}

			} else {
				List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
				updateTable = false;
				for (int i = 0; i < testfieldList.size(); i++) {
					DataField tb1 = testfieldList.get(i);
					String name1 = tb1.getName();
					int data_len1 = tb1.getDataLength();
					int data_pre1 = tb1.getDataPrecision();
					String data_type1 = tb1.getDataType();
					Boolean isForeignKey1 = tb1.getIsForeignKey();
					Boolean isIndex1 = tb1.getIsIndex();
					Boolean isNullable1 = tb1.getIsNullable();
					Boolean isPrimaryKey1 = tb1.getIsPrimaryKey();
					int flag = 0;
					int flag1 = 0;
					int flag2 = 0;
					int flag3 = 0;
					int flag4 = 0;
					int flag5 = 0;
					int flag6 = 0;
					int flag7 = 0;
					String fieldTypeChangeDesc = "";
					for (int p = 0; p < existfieldList.size(); p++) {
						DataField tb2 = existfieldList.get(p);
						String name2 = tb2.getName();
						int data_len2 = tb2.getDataLength();
						int data_pre2 = tb2.getDataPrecision();
						String data_type2 = tb2.getDataType();
						Boolean isForeignKey2 = tb2.getIsForeignKey();
						Boolean isIndex2 = tb2.getIsIndex();
						Boolean isNullable2 = tb2.getIsNullable();
						Boolean isPrimaryKey2 = tb2.getIsPrimaryKey();
						// 判断数据的长度，数据的精度，数据的类型
						if (name1.equals(name2)) {
							flag = 1;
							if (data_len1 != data_len2
									|| data_pre1 != data_pre2
									|| !data_type1.equals(data_type2)
									|| isForeignKey1 != isForeignKey2
									|| isIndex1 != isIndex2
									|| isNullable1 != isNullable2
									|| isPrimaryKey1 != isPrimaryKey2) {
								flag = 2;
								if (data_len1 != data_len2) {
									flag1 = 1;
									fieldTypeChangeDesc = fieldTypeChangeDesc
											+ "数据长度，原来是" + data_len1 + "现在是"
											+ data_len2 + ",";
								}
								if (data_pre1 != data_pre2) {
									flag2 = 1;
									fieldTypeChangeDesc = fieldTypeChangeDesc
											+ "数据精度，原来是" + data_pre1 + "现在是"
											+ data_pre2 + ",";
								}
								if (!data_type1.equals(data_type2)) {
									flag3 = 1;
									fieldTypeChangeDesc = fieldTypeChangeDesc
											+ "数据类型，原来是" + data_type1 + "现在是"
											+ data_type2 + ",";
								}
								if (isForeignKey1 != isForeignKey2) {
									flag4 = 1;
									fieldTypeChangeDesc = fieldTypeChangeDesc
											+ "数据是否为外键，原来是" + isForeignKey1
											+ "现在是" + isForeignKey2 + ",";
								}
								if (isIndex1 != isIndex2) {
									flag5 = 1;
									fieldTypeChangeDesc = fieldTypeChangeDesc
											+ "数据是否为索引，原来是" + isIndex1 + "现在是"
											+ isIndex2 + ",";
								}
								if (isNullable1 != isNullable2) {
									flag6 = 1;
									fieldTypeChangeDesc = fieldTypeChangeDesc
											+ "数据是否为索引，原来是" + isNullable1
											+ "现在是" + isNullable2 + ",";
								}
								if (isPrimaryKey1 != isPrimaryKey2) {
									flag7 = 1;
									fieldTypeChangeDesc = fieldTypeChangeDesc
											+ "数据是否为索引，原来是" + isPrimaryKey1
											+ "现在是" + isPrimaryKey2 + ",";
								}
							}
						}
					}
					// 字段增加
					if (flag == 0) {
						DataTable testTable = testTableRepository
								.findByNameAndDBName(testtableList.get(j)
										.getName(), dbName);
						tb1.setDataTable(testTable);
						tb1.setEditTime(new Date());
						testFieldRepository.save(tb1);
						if (tb1.getVersion() != null) {
							int version = tb1.getVersion() + 1;
							testFieldRepository.update(version, tb1.getId());
						} else {
							int version = 1;
							testTableRepository.update(version, tb1.getId());
						}
						updateTable = true;
						MetaHistoryItem metaHistoryItem = new MetaHistoryItem();
						metaHistoryItem.setCreateTime(new Date());
						metaHistoryItem.setTableName(testTable.getName());
						metaHistoryItem.setOrgFiledName("");
						metaHistoryItem.setRefFieldName(tb1.getName());
						metaHistoryItem.setOptFieldType(EnumOptType.Add);
						metaHistoryItem.setOptTableType(EnumOptType.Update);
						metaHistoryItem.setInfo("更新表增加字段");
						metaHistoryItemList.add(metaHistoryItem);
					}
					// 字段修改
					if (flag == 2) {
						DataTable testTable1 = testTableRepository
								.findByNameAndDBName(testtableList.get(j)
										.getName(), dbName);
						tb1.setDataTable(testTable1);
						DataField tfchange = testFieldRepository
								.findByNameAndTableName(tb1.getName(),
										tb1.getTalbeName());
						int id = tfchange.getId();
						Date createTime = tfchange.getCreateTime();
						String createUser = tfchange.getCreateUser();
						Date editTime = new Date();
						String editUser = tb1.getEditUser();
						int dataLength = tb1.getDataLength();
						int dataPrecision = tb1.getDataPrecision();
						String dataType = tb1.getDataType();
						String des = tb1.getDes();
						boolean isForeignKey = false;
						if (tb1.getIsForeignKey() != null) {
							isForeignKey = tb1.getIsForeignKey();
						}
						boolean isIndex = false;
						if (tb1.getIsIndex() != null) {
							isIndex = tb1.getIsIndex();
						}
						boolean isNullable = false;
						if (tb1.getIsNullable() != null) {
							isNullable = tb1.getIsNullable();
						}
						boolean isPrimaryKey = false;
						if (tb1.getIsPrimaryKey() != null) {
							isPrimaryKey = tb1.getIsPrimaryKey();
						}
						String name = tb1.getName();
						String talbeName = tb1.getTalbeName();
						boolean deleted = false;

						if (tb1.getDeleted() != null) {
							deleted = tb1.getDeleted();
						}

						int version0 = 1;
						if (tfchange.getVersion() != null) {
							version0 = tfchange.getVersion() + 1;
						}

						testFieldRepository.updateOneByID(createTime,
								createUser, editTime, editUser, dataLength,
								dataPrecision, dataType, des, isForeignKey,
								isIndex, isNullable, isPrimaryKey, name,
								talbeName, deleted, version0, id);
						// testFieldRepository.deleteById(tfchange.getId());
						// testFieldRepository.save(tb1);

						// if(tb1.getVersion()!=null) {
						// int version=tb1.getVersion()+1;
						// testFieldRepository.update(version, tb1.getId());
						// }
						// else {
						// int version=1;
						// testFieldRepository.update(version, tb1.getId());
						// }

						updateTable = true;
						MetaHistoryItem metaHistoryItem = new MetaHistoryItem();
						metaHistoryItem.setCreateTime(new Date());
						metaHistoryItem.setTableName(testtableList.get(j)
								.getName());
						metaHistoryItem.setOrgFiledName(name1);
						metaHistoryItem.setRefFieldName(name1);
						metaHistoryItem.setOptFieldType(EnumOptType.Update);
						metaHistoryItem.setOptTableType(EnumOptType.Update);
						metaHistoryItem.setInfo("更新字段属性");
						metaHistoryItem
								.setFieldTypeChangeDesc(fieldTypeChangeDesc);
						metaHistoryItemList.add(metaHistoryItem);

					}

				}

				for (int i = 0; i < existfieldList.size(); i++) {
					DataField tb1 = existfieldList.get(i);
					String name1 = tb1.getName();
					int flag = 0;

					for (int p = 0; p < testfieldList.size(); p++) {
						DataField tb2 = testfieldList.get(p);
						String name2 = tb2.getName();
						if (name1.equals(name2)) {
							flag = 1;
						}
					}
					// 删除字段
					if (flag == 0) {
						DataTable testTable = testTableRepository
								.findByNameAndDBName(testtableList.get(j)
										.getName(), dbName);
						tb1.setDataTable(testTable);
						tb1.setDeleted(true);
						// testFieldRepository.delete(tb1);
						testFieldRepository.deletedByID(true, tb1.getId());
						updateTable = true;
						MetaHistoryItem metaHistoryItem = new MetaHistoryItem();
						metaHistoryItem.setCreateTime(new Date());
						metaHistoryItem.setTableName(testTable.getName());
						metaHistoryItem.setOrgFiledName(name1);
						metaHistoryItem.setRefFieldName("");
						metaHistoryItem.setOptFieldType(EnumOptType.Delete);
						metaHistoryItem.setOptTableType(EnumOptType.Update);
						metaHistoryItem.setInfo("更新表删除字段");
						metaHistoryItemList.add(metaHistoryItem);
					}// if5

				}// for
				if (updateTable) {
					MetaHistory metaHistory = new MetaHistory();
					metaHistory.setCreateTime(new Date());
					metaHistory.setDataSchema(db);
					metaHistoryRepository.save(metaHistory);
					for (int q = 0; q < metaHistoryItemList.size(); q++) {
						MetaHistoryItem mi = metaHistoryItemList.get(q);
						mi.setMetaHistory(metaHistory);
						metaHistoryItemRepository.save(mi);
					}

					DataTable testTable = testTableRepository
							.findByNameAndDBName(
									testtableList.get(j).getName(), dbName);
					if (testTable.getVersion() != null) {
						int version = testTable.getVersion() + 1;
						testTableRepository.update(version, testTable.getId());
					} else {
						int version = 1;
						testTableRepository.update(version, testTable.getId());
					}
					updateDB = true;

				}

			}// else

		}

		if (updateDB) {
			if (db.getVersion() != null) {
				int version = db.getVersion() + 1;
				testDatabaseRepository.update(version, db.getId());
			} else {
				int version = 1;
				testDatabaseRepository.update(version, db.getId());
			}

		}

	}

	/**
	 * 把传过来的参数拼接成sql，并执行返回结果
	 */
	public void GetSql(SqlVo sqlvo) throws SQLException {

		ISqlBuilerAdapter adapter = null;
		ISqlBuilerAdapter targetadapter = null;
		String caseId = sqlvo.getCaseid();
		String createUser = sqlvo.getCreateUser();
		String editUser = sqlvo.getEditUser();
		String editTime = sqlvo.getEditTime();
		String sourcedbtype = sqlvo.getSourcedbType();
		sourcedbtype = sourcedbtype.toLowerCase();
		String targetdbtype = sqlvo.getTargetdbType();
		targetdbtype = targetdbtype.toLowerCase();
		if (sourcedbtype.equals("mysql")) {
			adapter = new MysqlBuilderAdapter();
		}
		if (sourcedbtype.equals("oracle")) {
			adapter = new OracleBuilderAdapter();
		}

		List<Sourceselectsql> sourceselectsql = sqlvo.getSourceselectsql();
		String ssql = adapter.selectBuilder(sourceselectsql);

		if (targetdbtype.equals("mysql")) {
			targetadapter = new MysqlBuilderAdapter();
		}
		if (targetdbtype.equals("oracle")) {
			targetadapter = new OracleBuilderAdapter();
		}
		List<Sourceselectsql> targetselectsql = sqlvo.getTargetselectsql();
		String tsql = targetadapter.selectBuilder(targetselectsql);

		MainSqlRecord record = new MainSqlRecord();
		String TIME_PATTREN = "yyyy-MM-dd HH:mm:ss";
		record.setCaseId(caseId);
		record.setCreateUser(createUser);
		record.setEditUser(editUser);
		record.setSourceData(sourcedbtype);
		record.setTargetData(targetdbtype);
		record.setSourceSql(ssql);
		record.setTargetSql(tsql);
		record.setCreateTime(new Date());
		Date editdate = DateUtil.getDate(editTime, TIME_PATTREN);
		record.setEditTime(editdate);
		mainSqlRecordRepository.save(record);

		System.out.println("source_sql=" + ssql + ",targetsql=" + tsql);
		
	}

	@Override
	public List<DataSource> getAllDatasource() throws SQLException {
		List<DataSource> datasourceList = dataSourceRepository.findAll();
		return datasourceList;
	}

	@Override
	public Page<DataSource> findAllPage(int pageNum, int pageSize) {
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
		return dataSourceRepository.findAll(pageRequest);
	}

	@Override
	public Page<DataSource> getAllDSByPage(String dataSourceName,
			Pageable pageable) {
		return dataSourceRepository.findAllDSNameByPage(dataSourceName,
				pageable);
	}

	@Override
	public List<DataSchema> getAllDb(int data_source_id) throws SQLException {
		List<DataSchema> dbList = testDatabaseRepository
				.findByForeignKey(data_source_id);
		return dbList;
	}

	@Override
	public Page<DataSchema> getAllDbByPage(int data_source_id,
			Pageable pageable) throws SQLException {
		return testDatabaseRepository.findByForeignKeyByPage(data_source_id,
				pageable);

	}

	@Override
	public Page<DataSchema> getAllDatabaseByPage(String DBName,
			Pageable pageable) throws SQLException {
		Page<DataSchema> tt = testDatabaseRepository.findAllDbByPage(DBName,
				pageable);
		return tt;
	}

	@Override
	public List<DataTable> getAllTable(int test_database_id){
		List<DataTable> tableList = testTableRepository
				.findByForeignKey(test_database_id);
		return tableList;
	}

	@Override
	public Page<DataTable> getAllTableByPage(int test_database_id,
			Pageable pageable) throws SQLException {
		return testTableRepository.findByForeignKeyByPage(test_database_id,
				pageable);
	}

	@Override
	public Page<DataTable> getSelTableByPage(int test_database_id,
			String tableName, Pageable pageable) throws SQLException {
		return testTableRepository.findByForeignKeyAndTableByPage(
				test_database_id, tableName, pageable);
	}

	@Override
	public List<DataField> getAllField(int test_table_id) throws SQLException {
		List<DataField> fieldList = testFieldRepository
				.findByForeignKey(test_table_id);
		return fieldList;
	}

	@Override
	public Page<DataField> getAllFieldByPage(int test_table_id,
			Pageable pageable) throws SQLException {
		return testFieldRepository.findByForeignKeyByPage(test_table_id,
				pageable);
	}

	public Sourceselectsql parseEachSql(String sql) {
		Sourceselectsql selectsql = new Sourceselectsql();
		sql = sql.toLowerCase();
		String selectStr = SplitData.subString(sql, "select", "from");
		String[] selectArr = selectStr.split(",");
		List<Selectlist> selectlist = new ArrayList<Selectlist>();
		for (int i = 0; i < selectArr.length; i++) {
			Selectlist s = new Selectlist();
			s.setItem(selectArr[i]);
			selectlist.add(s);
		}
		selectsql.setSelectlist(selectlist);
		int joinindex = sql.indexOf("join");
		int leftjoinindex = sql.indexOf("leftjoin");
		int whereindex = sql.indexOf("where");
		int groupindex = sql.indexOf("group by");
		int havingindex = sql.indexOf("having");
		int orderindex = sql.indexOf("order by");
		String fromStr = "";
		String joinStr = "";
		String leftjoinStr = "";
		String whereStr = "";
		String groupbyStr = "";
		String havingStr = "";
		String orderStr = "";
		if (joinindex > 0) {
			fromStr = SplitData.subString(sql, "from", "join");
			if (whereindex > 0) {
				joinStr = SplitData.subString(sql, "join", "where");
			}
		}
		if (leftjoinindex > 0) {
			fromStr = SplitData.subString(sql, "from", "leftjoin");
			if (whereindex > 0) {
				leftjoinStr = SplitData.subString(sql, "leftjoin", "where");
			}
		}
		if (whereindex > 0) {
			if (joinindex < 0) {
				fromStr = SplitData.subString(sql, "from", "where");
			}
			if (groupindex > 0) {
				whereStr = SplitData.subString(sql, "where", "group by");
			} else {
				if (orderindex > 0) {
					whereStr = SplitData.subString(sql, "where", "order by");
					orderStr = SplitData.subString(sql, "order by", "end");
				} else {
					whereStr = SplitData.subString(sql, "where", "end");
				}
			}
		} else {
			if (groupindex > 0) {
				if (joinindex > 0) {
					joinStr = SplitData.subString(sql, "join", "group by");
				}
				if (leftjoinindex > 0) {
					leftjoinStr = SplitData.subString(sql, "leftjoin",
							"group by");
				}
				if (joinindex < 0) {
					fromStr = SplitData.subString(sql, "from", "group by");
				}
				if (havingindex > 0) {
					groupbyStr = SplitData.subString(sql, "group by", "having");
					if (orderindex > 0) {
						if (joinindex > 0) {
							joinStr = SplitData.subString(sql, "join",
									"order by");
						}
						if (leftjoinindex > 0) {
							leftjoinStr = SplitData.subString(sql, "leftjoin",
									"order by");
						}
						havingStr = SplitData.subString(sql, "having",
								"order by");
						orderStr = SplitData.subString(sql, "order by", "end");
					} else {
						havingStr = SplitData.subString(sql, "having", "end");
					}
				} else {
					if (orderindex > 0) {
						if (joinindex > 0) {
							joinStr = SplitData.subString(sql, "join",
									"order by");
						}
						if (leftjoinindex > 0) {
							leftjoinStr = SplitData.subString(sql, "leftjoin",
									"order by");
						}
						groupbyStr = SplitData.subString(sql, "group by",
								"order by");
						orderStr = SplitData.subString(sql, "order by", "end");
					} else {
						groupbyStr = SplitData
								.subString(sql, "group by", "end");
					}
				}
			} else {
				fromStr = SplitData.subString(sql, "from", "end");
				if (joinindex > 0) {
					joinStr = SplitData.subString(sql, "join", "end");
				}
				if (leftjoinindex > 0) {
					leftjoinStr = SplitData.subString(sql, "leftjoin", "end");
				}
			}
		}

		String[] fromArr = fromStr.split(",");
		List<Fromlist> fromlist = new ArrayList<Fromlist>();
		for (int i = 0; i < fromArr.length; i++) {
			Fromlist s = new Fromlist();
			s.setItem(fromArr[i]);
			fromlist.add(s);
		}
		selectsql.setFromlist(fromlist);

		String[] whereArr = whereStr.split("and");
		List<Wherelist> wherelist = new ArrayList<Wherelist>();
		for (int i = 0; i < whereArr.length; i++) {
			Wherelist s = new Wherelist();
			s.setItem(whereArr[i]);
			wherelist.add(s);
		}
		selectsql.setWherelist(wherelist);

		String[] groupbyArr = groupbyStr.split(",");
		List<Grouplist> grouplist = new ArrayList<Grouplist>();
		for (int i = 0; i < groupbyArr.length; i++) {
			Grouplist s = new Grouplist();
			s.setItem(groupbyArr[i]);
			grouplist.add(s);
		}
		selectsql.setGrouplist(grouplist);

		String[] havingArr = havingStr.split(",");
		List<Havinglist> havinglist = new ArrayList<Havinglist>();
		for (int i = 0; i < havingArr.length; i++) {
			Havinglist s = new Havinglist();
			s.setItem(havingArr[i]);
			havinglist.add(s);
		}
		selectsql.setHavinglist(havinglist);

		String[] orderArr = orderStr.split(",");
		List<Orderbylist> orderlist = new ArrayList<Orderbylist>();
		for (int i = 0; i < orderArr.length; i++) {
			Orderbylist s = new Orderbylist();
			s.setItem(orderArr[i]);
			orderlist.add(s);
		}
		selectsql.setOrderbylist(orderlist);

		String[] joinArr = joinStr.split("join");
		List<Joinlist> joinlist = new ArrayList<Joinlist>();
		for (int i = 0; i < joinArr.length; i++) {
			Joinlist s = new Joinlist();
			s.setItem(joinArr[i]);
			joinlist.add(s);
		}
		selectsql.setJoinlist(joinlist);

		String[] leftjoinArr = leftjoinStr.split("leftjoin");
		List<Leftjoinlist> leftjoinlist = new ArrayList<Leftjoinlist>();
		for (int i = 0; i < leftjoinArr.length; i++) {
			Leftjoinlist s = new Leftjoinlist();
			s.setItem(leftjoinArr[i]);
			leftjoinlist.add(s);
		}
		selectsql.setLeftjoinlist(leftjoinlist);
		return selectsql;
	}

	@Override
	public SqlVo parseSql(MainSqlRecord m) throws SQLException {
		MainSqlRecord mm = mainSqlRecordRepository.getOne(m.getId());
		SqlVo sqlvo = new SqlVo();
		try {
			String sourcesqlStr = mm.getSourceSql();// "select a.id,b.name from
													// sss as a,bbb as b where
													// a.id=b.id and a.id>1
													// order by a.id,b.name";
			System.out.println("sourcesqlStr=" + sourcesqlStr);
			String sourcedbType = mm.getSourceData();
			List<Sourceselectsql> selectsql = new ArrayList<Sourceselectsql>();

			String[] sqlarr = sourcesqlStr.split("union");
			for (int i = 0; i < sqlarr.length; i++) {
				Sourceselectsql sq = parseEachSql(sqlarr[i]);
				selectsql.add(sq);
			}

			sqlvo.setSourceselectsql(selectsql);
			sqlvo.setSourcedbType(sourcedbType);

			String tagetsqlStr = mm.getTargetSql();// "select a.id,b.name from
													// sss as a,bbb as b where
													// a.id=b.id and a.id>1
													// order
													// by a.id,b.name";
			System.out.println("tagetsqlStr=" + tagetsqlStr);
			String targetdbType = mm.getTargetData();
			List<Sourceselectsql> targetselectsql = new ArrayList<Sourceselectsql>();

			String[] tagetsqlarr = tagetsqlStr.split("union");
			for (int i = 0; i < tagetsqlarr.length; i++) {
				Sourceselectsql sq = parseEachSql(tagetsqlarr[i]);
				targetselectsql.add(sq);
			}
			sqlvo.setTargetdbType(targetdbType);
			sqlvo.setTargetselectsql(targetselectsql);

		} catch (Exception e) {
			System.out.println(e.getLocalizedMessage());
		}
		return sqlvo;
	}

	
	@Override
	//@Transactional
	public void uploadDictExcel(String filePath){
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DataSchema db = new DataSchema();
		int lastIndex = filePath.lastIndexOf("\\");
		String name = filePath.substring(lastIndex + 1);
		String dbName = name.split("\\.")[0];
		db.setName(dbName);
		Boolean flag = new Boolean("true");
		db.setIsDict(flag);
		testDatabaseRepository.save(db);
		DataSchema tdb = testDatabaseRepository.findByName(dbName);

		Workbook workbook = ExcelUtil.getWorkBook(filePath);
		
		if (workbook != null) {
			for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
				//System.out.println(workbook.getNumberOfSheets());
				// 获得当前sheet工作表
				Sheet sheet = workbook.getSheetAt(sheetNum);
				// 获取sheet表名
				String tableName = sheet.getSheetName();
				DataTable table = new DataTable();
				table.setName(tableName);
				table.setIsDict(flag);
				table.setDataSchema(tdb);
				testTableRepository.save(table);

				/*TestTable tt = testTableRepository.findByNameAndDBName(
						tableName, dbName);*/
				// 获得当前sheet的开始行
				int firstRowNum = sheet.getFirstRowNum();
				// 获得当前sheet的结束行
				int lastRowNum = sheet.getLastRowNum();
				try {
				for (int rowNum = firstRowNum + 1; rowNum <= lastRowNum; rowNum++) {
					// 获得当前行
					Row row = sheet.getRow(rowNum);
					
					// 获得当前行的开始列
					//int firstCellNum = row.getFirstCellNum();
					// 获得当前行的列数
					//int lastCellNum = row.getPhysicalNumberOfCells();
					String l1 = ExcelUtil.getCellValue(row.getCell(0));
					String l2 = ExcelUtil.getCellValue(row.getCell(1));
					String l3 = ExcelUtil.getCellValue(row.getCell(2));
					String l4 = ExcelUtil.getCellValue(row.getCell(3));
					String l5 = ExcelUtil.getCellValue(row.getCell(4));
					String l6 = ExcelUtil.getCellValue(row.getCell(5));
					String l7 = ExcelUtil.getCellValue(row.getCell(6));
					String l8 = ExcelUtil.getCellValue(row.getCell(7));
					String l9 = ExcelUtil.getCellValue(row.getCell(8));
					String l10 = ExcelUtil.getCellValue(row.getCell(9));
					String l11 = ExcelUtil.getCellValue(row.getCell(10));
					String l12 = ExcelUtil.getCellValue(row.getCell(11));
					String l13 = ExcelUtil.getCellValue(row.getCell(12));
					String l14 = ExcelUtil.getCellValue(row.getCell(13));
					String l15 = ExcelUtil.getCellValue(row.getCell(14));
					String l16 = ExcelUtil.getCellValue(row.getCell(15));
					String l17 = ExcelUtil.getCellValue(row.getCell(16));
					String l18 = ExcelUtil.getCellValue(row.getCell(17));

					/*
					 * for(int cellNum = firstCellNum; cellNum
					 * <lastCellNum;cellNum++){ String
					 * cellN=ExcelUtil.getCellValue(row.getCell(cellNum)); }
					 */

					
						DataField tf = new DataField();
						tf.setId(Integer.valueOf(l1));
						tf.setCreateTime(sdf.parse(l2));
						tf.setCreateUser(l3);
						tf.setEditTime(sdf.parse(l4));
						tf.setEditUser(l5);
						tf.setDataLength(Integer.valueOf(l6));
						tf.setDataPrecision(Integer.valueOf(l7));
						tf.setDataType(l8);
						tf.setDeleted(Boolean.getBoolean(l9));
						tf.setDes(l10);
						tf.setIsForeignKey(Boolean.getBoolean(l11));
						tf.setIsIndex(Boolean.getBoolean(l12));
						tf.setIsNullable(Boolean.getBoolean(l13));
						tf.setIsPrimaryKey(Boolean.getBoolean(l14));
						tf.setName(l15);
						tf.setTalbeName(l16);
						tf.setVersion(Integer.valueOf(l17));
						tf.setDataTable(table);
						testFieldRepository.save(tf);
					

				}
				} catch (ParseException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void compareDictAndModel(int db1Id, int db2Id) throws SQLException {
		List<DataTable> table = testTableRepository.findByForeignKey(db1Id);
		List<DataTable> tableDict = testTableRepository.findByForeignKey(db2Id);
		// System.out.println("-----table size="+table.size());
		// System.out.println("------tableDict size="+tableDict.size());

		int flag1 = 0;
		int flag2 = 0;
		int flag3 = 0;
		int flag4 = 0;
		int flag0 = 0;
		boolean tableflag = true;
		boolean fieldflag = true;
		for (int i = 0; i < table.size(); i++) {
			DataTable tb1 = table.get(i);
			String name1 = tb1.getName();
			// System.out.println("name1="+name1);
			flag1 = 0;
			for (int j = 0; j < tableDict.size(); j++) {
				DataTable tb2 = tableDict.get(j);
				String name2 = tb2.getName();
				// System.out.println("name2="+name2);
				if (name1.equals(name2)) {
					flag1 = 1;
					break;
				}
			}

			if (flag1 == 0) {
				// System.out.println("1.table not same ");
				tableflag = false;
				TestCase tcase = new TestCase();
//				tcase.setExpertValue("Dict And Table");
				tcase.setName("Dict And Table");
				TestCase tc = testCaseRepository.save(tcase);
				TestResult tresult = new TestResult();
				tresult.setCaseId(String.valueOf(tc.getId()));
				tresult.setResult("Dict table not same source table");
				tresult.setSecordaryTable(name1);
				testResultRepository.save(tresult);
			}
		}
		// System.out.println("tableDict size="+tableDict.size());
		for (int i = 0; i < tableDict.size(); i++) {
			DataTable tb1 = tableDict.get(i);
			String name1 = tb1.getName();
			// System.out.println("2----name1="+name1);
			flag2 = 0;
			for (int j = 0; j < table.size(); j++) {
				DataTable tb2 = table.get(j);
				String name2 = tb2.getName();
				// System.out.println("2----name2="+name2);
				if (name1.equals(name2)) {
					flag2 = 1;
					break;
				}
			}

			if (flag2 == 0) {
				// System.out.println("2.table not same ");
				tableflag = false;
				TestCase tcase = new TestCase();
//				tcase.setExpertValue("Dict And Table");
				tcase.setName("Dict And Table");
				TestCase tc = testCaseRepository.save(tcase);
				TestResult tresult = new TestResult();
				tresult.setCaseId(String.valueOf(tc.getId()));
				tresult.setResult("Dict table not same source table");
				tresult.setSecordaryTable(name1);
				testResultRepository.save(tresult);
			}
		}
		// System.out.println("------------tableflag="+tableflag);
		if (tableflag) {
			for (int i = 0; i < tableDict.size(); i++) {
				DataTable tb1 = tableDict.get(i);
				int tableId = tb1.getId();
				List<DataField> tfdictList = testFieldRepository
						.findByForeignKey(tableId);
				String name2 = "";
				for (int j = 0; j < tfdictList.size(); j++) {
					DataField tf1 = tfdictList.get(j);
					String name1 = tf1.getName();
					// System.out.println("3----name1="+name1);
					String tb1_tablename = tf1.getTalbeName();
					int data_len1 = tf1.getDataLength();
					int data_pre1 = tf1.getDataPrecision();
					String data_type1 = tf1.getDataType();
					flag3 = 0;
					for (int p = 0; p < table.size(); p++) {
						DataTable tb2 = table.get(p);
						int tb2_tableid = tb2.getId();
						String tb2_tablename = tb2.getName();
						List<DataField> tfList = testFieldRepository
								.findByForeignKey(tb2_tableid);
						if (tb1_tablename.equals(tb2_tablename)) {
							for (int q = 0; q < tfList.size(); q++) {
								DataField tf2 = tfList.get(q);
								name2 = tf2.getName();
								// System.out.println("3----name2="+name2);
								int data_len2 = tf2.getDataLength();
								int data_pre2 = tf2.getDataPrecision();
								String data_type2 = tf2.getDataType();
								if (name1.equals(name2)) {
									flag3 = 1;
									if (data_len1 != data_len2
											|| data_pre1 != data_pre2
											|| !data_type1.equals(data_type2)) {
										flag3 = 2;
										if (data_len1 != data_len2) {
											flag0 = 1;
										}
										if (data_pre1 != data_pre2) {
											flag0 = 2;
										}
										if (!data_type1.equals(data_type2)) {
											flag0 = 3;
										}
										break;
									}
								}
							}
						}
					}
					if (flag3 == 0) {
						fieldflag = false;
						// System.out.println("3.field not same ");
						TestCase tcase = new TestCase();
//						tcase.setExpertValue("Dict field And field");
						tcase.setName("Dict And field");
						TestCase tc = testCaseRepository.save(tcase);
						TestResult tresult = new TestResult();
						tresult.setCaseId(String.valueOf(tc.getId()));
						tresult.setResult("Dict field not same source field");
						TestResult tr = testResultRepository.save(tresult);
						TestResultItem tri = new TestResultItem();
						tri.setTestResultId(tr.getId());
						tri.setSourceValue(name1);
						testResultItemRepository.save(tri);
					}
					if (flag3 == 2) {
						fieldflag = false;
						// System.out.println("3.1 field property not same ");
						TestCase tcase = new TestCase();
//						tcase.setExpertValue("Dict field And field");
						tcase.setName("Dict And field");
						TestCase tc = testCaseRepository.save(tcase);
						TestResult tresult = new TestResult();
						tresult.setCaseId(String.valueOf(tc.getId()));
						tresult.setResult("Dict field not same source field property");
						TestResult tr = testResultRepository.save(tresult);
						TestResultItem tri = new TestResultItem();
						tri.setTestResultId(tr.getId());
						tri.setSourceValue(name1);
						if (flag0 == 1) {
							tri.setResult("data_len not same");
						}
						if (flag0 == 2) {
							tri.setResult("data_precision not same");
						}
						if (flag0 == 3) {
							tri.setResult("data_type not same");
						}
						testResultItemRepository.save(tri);
					}

				}
			}

			for (int i = 0; i < table.size(); i++) {
				DataTable tb1 = table.get(i);
				int tableId = tb1.getId();
				List<DataField> tfdictList = testFieldRepository
						.findByForeignKey(tableId);
				String name2 = "";
				for (int j = 0; j < tfdictList.size(); j++) {
					DataField tf1 = tfdictList.get(j);
					String name1 = tf1.getName();
					// System.out.println("4----name1="+name1);
					String tb1_tablename = tf1.getTalbeName();
					int data_len1 = tf1.getDataLength();
					int data_pre1 = tf1.getDataPrecision();
					String data_type1 = tf1.getDataType();
					flag4 = 0;
					for (int p = 0; p < tableDict.size(); p++) {
						DataTable tb2 = tableDict.get(p);
						int tb2_tableid = tb2.getId();
						String tb2_tablename = tb2.getName();
						List<DataField> tfList = testFieldRepository
								.findByForeignKey(tb2_tableid);
						if (tb1_tablename.equals(tb2_tablename)) {
							for (int q = 0; q < tfList.size(); q++) {
								DataField tf2 = tfList.get(q);
								name2 = tf2.getName();
								// System.out.println("4----name2="+name2);
								int data_len2 = tf2.getDataLength();
								int data_pre2 = tf2.getDataPrecision();
								String data_type2 = tf2.getDataType();
								if (name1.equals(name2)) {
									flag4 = 1;
									if (data_len1 != data_len2
											|| data_pre1 != data_pre2
											|| !data_type1.equals(data_type2)) {
										flag4 = 2;
										break;
									}
								}
							}
						}
					}
					if (flag4 == 0) {
						fieldflag = false;
						// System.out.println("4.field not same ");
						TestCase tcase = new TestCase();
//						tcase.setExpertValue("field And Dict field");
						tcase.setName("field And Dict field");
						TestCase tc = testCaseRepository.save(tcase);
						TestResult tresult = new TestResult();
						tresult.setCaseId(String.valueOf(tc.getId()));
						tresult.setResult("Dict field not same source field");
						TestResult tr = testResultRepository.save(tresult);
						TestResultItem tri = new TestResultItem();
						tri.setTestResultId(tr.getId());
						tri.setSourceValue(name1);
						testResultItemRepository.save(tri);
					}
					// if(flag4==2) {
					// fieldflag=false;
					// System.out.println("4.1 field property not same ");
					// TestCase tcase= new TestCase();
					// tcase.setExpertValue("field And Dict field");
					// tcase.setName("field And Dict field");
					// TestCase tc=testCaseRepository.save(tcase);
					// TestResult tresult=new TestResult();
					// tresult.setCaseId(String.valueOf(tc.getId()));
					// tresult.setResult(" field not same source Dict field property");
					// TestResult tr=testResultRepository.save(tresult);
					// TestResultItem tri=new TestResultItem();
					// tri.setTestResultId(tr.getId());
					// tri.setSoruceValue(name1);
					// tri.setTragetValue(name2);
					// testResultItemRepository.save(tri);
					// }
				}

			}

			if (fieldflag) {
				// System.out.println("5 field property  same ");
				TestCase tcase = new TestCase();
//				tcase.setExpertValue("field And Dict field");
				tcase.setName("field And Dict field");
				TestCase tc = testCaseRepository.save(tcase);
				TestResult tresult = new TestResult();
				tresult.setCaseId(String.valueOf(tc.getId()));
				tresult.setResult("field same source Dict field property");
				TestResult tr = testResultRepository.save(tresult);
				TestResultItem tri = new TestResultItem();
				tri.setTestResultId(tr.getId());
				tri.setResult("field property same");
				testResultItemRepository.save(tri);
			}
		}
	}
	@Override
	public ResultVO GetSqlDBLink(Integer dataSourceId,String sqlText) throws SQLException{
		ResultVO result=new ResultVO(true, StatusCode.OK, "测试成功");
		AbstractAdapter adapter = null;
		DataSource ds = dataSourceRepository.findById(dataSourceId).get();
		String schema = ds.getDefaultSchema();
		String username = ds.getUserName();
		String pwd = ds.getPassword();
		String name = ds.getName();
		EnumDatabaseType dbType = ds.getDatabaseType();
		String host = ds.getHost();

		String port = ds.getPort();
		String url = ds.getUrl();
		String driver = ds.getDriver();
		String sid = ds.getSid();
		System.out.println("schema=" + schema);
		System.out.println("dbType=" + dbType);

		String explain="";
		
		Connection conn = null;
		try {

			DbModel db = new DbModel();
			if (dbType.equals(EnumDatabaseType.Mysql)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("com.mysql.cj.jdbc.Driver");
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {
					if (host.equals("localhost")) {
						db.setUrl("jdbc:mysql://"
								+ host
								+ "/information_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
					} else {
						if (port == null || port.equals("")) {
							port = "3306";
						}
						db.setUrl("jdbc:mysql://"
								+ host
								+ ":"
								+ port
								+ "/information_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
					}
				}

				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("mysql");
				db.setDbtype(EnumDatabaseType.Mysql);

				adapter = new MySqlAdapter();
				conn = ((MySqlAdapter) adapter).getConnection(db);
				explain="explain ";
			} else if (dbType.equals(EnumDatabaseType.Oracle)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("oracle.jdbc.driver.OracleDriver");
				}
				if (port == null || port.equals("")) {
					port = "1521";
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {

					db.setUrl("jdbc:oracle:thin:@//" + host + ":" + port + "/"
							+ sid);
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("oracle");
				db.setDbtype(EnumDatabaseType.Oracle);

				adapter = new OracleAdapter();
				conn = ((OracleAdapter) adapter).getConnection(db);
				explain="explain plan for ";
			} else if (dbType.equals(EnumDatabaseType.DB2)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("com.ibm.db2.jcc.DB2Driver");
				}
				if (port == null || port.equals("")) {
					port = "50000";
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {

					db.setUrl("jdbc:db2://" + host + ":" + port + "/datatest");
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("db2");
				db.setDbtype(EnumDatabaseType.DB2);

				adapter = new DB2Adapter();
				conn = ((DB2Adapter) adapter).getConnection(db);
				explain="";
			} else if (dbType.equals(EnumDatabaseType.Informix)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("com.informix.jdbc.IfxDriver");
				}
				if (port == null || port.equals("")) {
					port = "9088";
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {

					db.setUrl("jdbc:informix-sqli://" + host + ":" + port
							+ "/xydb:INFORMIXSERVER=ol_informix1170");
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("informix");
				db.setDbtype(EnumDatabaseType.Informix);

				adapter = new InformixAdapter();
				conn = ((InformixAdapter) adapter).getConnection(db);
			}else if (dbType.equals(EnumDatabaseType.SyBase)) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("com.sybase.jdbc3.jdbc.SybDriver");
				}
				if (port == null || port.equals("")) {
					port = "5000";
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {

					db.setUrl("jdbc:sybase:Tds:" + host + ":" + port
							+ "?charset=cp936");
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("sybase");
				db.setDbtype(EnumDatabaseType.SyBase);

				adapter = new SyBaseAdapter();
				conn = ((SyBaseAdapter) adapter).getConnection(db);
			}
			if (conn != null) {
				Statement statement = conn.createStatement();
				ResultSet rs =  statement.executeQuery(explain +sqlText);
				return result;
			}else {
				return new ResultVO(false, StatusCode.ERROR, "测试失败,数据源连接失败,请重新配置数据源");
			}

		} catch (Exception e) {
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "测试失败"+ e.getLocalizedMessage());
		}
	}
	
}
