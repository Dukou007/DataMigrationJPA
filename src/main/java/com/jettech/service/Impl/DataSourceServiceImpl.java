package com.jettech.service.Impl;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.transaction.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.jettech.EnumDatabaseType;
import com.jettech.EnumOptType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.DB2Adapter;
import com.jettech.db.adapter.InformixAdapter;
import com.jettech.db.adapter.MySqlAdapter;
import com.jettech.db.adapter.OracleAdapter;
import com.jettech.db.adapter.SyBaseAdapter;
import com.jettech.domain.DbModel;
import com.jettech.entity.DataField;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.entity.MetaHistory;
import com.jettech.entity.MetaHistoryItem;
import com.jettech.entity.QualityTestQuery;
import com.jettech.entity.TestQuery;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataSchemaRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.DataTableRepository;
import com.jettech.repostory.MetaHistoryItemRepository;
import com.jettech.repostory.MetaHistoryRepository;
import com.jettech.repostory.QualityTestQueryRepository;
import com.jettech.repostory.TestQueryRepository;
import com.jettech.service.DataSchemaService;
import com.jettech.service.IDataSourceService;
import com.jettech.util.CompareUtil;
import com.jettech.util.DateUtil;
import com.jettech.vo.DataSourceVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.SycData;

@Service
public class DataSourceServiceImpl implements IDataSourceService {
	private static Logger logger = LoggerFactory.getLogger(DataSourceServiceImpl.class);
	@Autowired
	private DataSchemaService testDatabaseService;
	@Autowired
	private DataSourceRepository repository;
	@Autowired
	private DataTableRepository testTableRepository;
	@Autowired
	private DataSchemaRepository testDatabaseRepository;
	@Autowired
	private DataFieldRepository testFieldRepository;
	@Autowired
	private MetaHistoryRepository metaHistoryRepository;
	@Autowired
	private MetaHistoryItemRepository metaHistoryItemRepository;
	@Autowired
	private QualityTestQueryRepository qualityTestQueryRepository;
	@Autowired
	private TestQueryRepository testQueryRepository;
	private boolean updateDB = false;
	private boolean updateTable = false;

	@Override
	public List<DataSource> findAll() {
		return repository.findAll();
	}

	@Override
	public List<DataSource> saveAll(List<DataSource> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(DataSource entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public DataSource findById(Integer id) {
		Optional<DataSource> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	// @Override
	// public DataSource getOneById(Integer id) {
	// return repository.getOne(id);
	// }

	@Override
	public Page<DataSource> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public List<DataSource> findByNameLike(String name) {
		return repository.findByNameLike(name);
	}

	@Override
	public DataSource findByName(String name) {
		return repository.findByName(name);
	}

	@Override
	public ResultVO copyDataSource(int id, String name) {
		DataSource dataSource = repository.findById(id).get();
		DataSource exitsDataSource = repository.findByName(name);
		if (exitsDataSource != null) {
			return new ResultVO(false, StatusCode.ERROR, "名称重复");
		}
		DataSource copyDataSource = new DataSource();
		BeanUtils.copyProperties(dataSource, copyDataSource);
		copyDataSource.setCreateTime(new Date());
		copyDataSource.setCreateUser(null);
		copyDataSource.setEditTime(new Date());
		copyDataSource.setEditUser(null);
		copyDataSource.setName(name);
		copyDataSource.setId(null);
		copyDataSource.setDataSchemas(null);
		copyDataSource.setQualityTestQuerys(null);
		copyDataSource.setTestQuerys(null);
		repository.save(copyDataSource);
		return new ResultVO(true, StatusCode.OK, "复制成功");
	}

	@Override
	public List<String> getDriver(String type) {
		List<String> driver = new ArrayList<String>();
		if (type.equals("Mysql")) {
			driver.add("com.mysql.cj.jdbc.Driver");
			driver.add("com.mysql.jdbc.Driver");
		} else if (type.equals("Oracle")) {
			driver.add("oracle.jdbc.OracleDriver");
			driver.add("oracle.jdbc.driver.OracleDriver");
		} else if (type.equals("SQLServer")) {
			driver.add("com.microsoft.sqlserver.jdbc.SQLServerDriver");
		} else if (type.equals("DB2")) {
			driver.add("com.ibm.db2.jcc.DB2Driver");
			driver.add("com.ibm.db2.jdbc.app.DB2Driver");
		} else if (type.equals("SyBase")) {
			driver.add("com.sybase.jdbc3.jdbc.SybDriver");
			driver.add("com.sybase.jdbc4.jdbc.SybDriver");
			driver.add("com.sysbase.jdbc.SybDriver");
			driver.add("net.sourceforge.jtds.jdbc.Driver");
		} else if (type.equals("Informix")) {
			driver.add("com.informix.jdbc.IfxDriver");
		}
		return driver;
	}

	/**
	 * 获取支持的数据库类型 20190326
	 * 
	 * @return
	 */
	@Override
	public List<String> selectDataSourceType() {
		List<String> type = new ArrayList<String>();
		for (EnumDatabaseType s : EnumDatabaseType.values()) {
			String name = s.getName();
			type.add(name);
		}
		return type;
	}

	@Override
	@Transactional
	public String GetMetaData(Integer dataSourceId) throws Exception {
		String message = "";
		if (dataSourceId == null) {
			message = "源id不能为空，不能进行同步";
			return message;
		}
		AbstractAdapter adapter = null;
		// DataSource ds = repository.getOne(dataSourceId);
		DataSource ds = repository.findById(dataSourceId).get();
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
		Connection conn = null;
		SycData sycData = getAdapterAndConnection(dbType, driver, url, port, host, username, pwd, sid);
		conn = sycData.getConn();
		adapter = sycData.getAdapter();
		// 尝试读取该用户访问权限下的所有schema(默认的)
		List<DataSchema> testdatabaseList = adapter.getDatabase(schema, conn);
		DataSource dataSource = repository.findByName(name);
		if (testdatabaseList.size() == 0) {
			message = "这个源下面没有默认的这个库，不能进行同步";
			return message;
		}
		for (int p = 0; p < testdatabaseList.size(); p++) {
			DataSchema tb = (DataSchema) testdatabaseList.get(p);
			tb.setDataSource(dataSource);
			tb.setIsDict(false);
			String testDataBaseName = testdatabaseList.get(p).getName();
			DataSchema dataSchema = testDatabaseRepository.findByNameAndDataSourceId(testDataBaseName, dataSourceId);
			if (dataSchema != null) {
				syncTableAndFiled(adapter, conn, dataSchema);
			} else if (dataSchema == null) {
				DataSchema savedataSchema = testDatabaseRepository.save(tb);// 保存库
				// 同步表和字段
				syncTableAndFiled(adapter, conn, savedataSchema);
			} /*
			   * else if(dataSchema != null &&
			   * dataSchema.getDataSource().getId()==dataSourceId &&
			   * dataSchema.getName()==schema){
			   * logger.info("名为"+dataSchema.getName()+"的数据库"+"名为"+dataSourceId+
			   * "的数据源已经存在，不能同步");
			   * message="名为"+dataSchema.getName()+"的数据库"+"名为"+dataSourceId+
			   * "的数据源已经存在，不能同步"; }
			   */

		}
		try {
			conn.close();
		} catch (SQLException e) {
			e.printStackTrace();
			logger.info("关闭连接报错");
		}
		return message;
	}

	@Override
	public SycData getAdapterAndConnection(EnumDatabaseType dbType, String driver, String url, String port, String host,
	        String username, String pwd, String sid) {
		SycData sycData = new SycData();
		DbModel db = new DbModel();
		AbstractAdapter adapter = null;
		Connection conn = null;
		if (dbType.equals(EnumDatabaseType.Mysql)) {
			if (driver != null && !driver.equals("")) {
				db.setDriver(driver);
			} else {
				db.setDriver(MySqlAdapter.DEFAULT_DRIVER);
			}
			if (url != null && !url.equals("")) {
				db.setUrl(url);
			} else {
				if (host.equals("localhost")) {
					db.setUrl("jdbc:mysql://" + host
					        + "/information_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
				} else {
					if (port == null || port.equals("")) {
						port = "3306";
					}
					db.setUrl("jdbc:mysql://" + host + ":" + port
					        + "/information_schema?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
				}
			}

			db.setUsername(username);
			db.setPassword(pwd);
			db.setName(EnumDatabaseType.Mysql.name());
			db.setDbtype(EnumDatabaseType.Mysql);

			adapter = new MySqlAdapter();
			conn = adapter.getConnection(db);
		} else if (dbType.equals(EnumDatabaseType.Oracle)) {
			if (driver != null && !driver.equals("")) {
				db.setDriver(driver);
			} else {
				db.setDriver(OracleAdapter.DEFAULT_DRIVER);
			}
			if (port == null || port.equals("")) {
				port = "1521";
			}
			if (url != null && !url.equals("")) {
				db.setUrl(url);
			} else {
				db.setUrl("jdbc:oracle:thin:@//" + host + ":" + port + "/" + sid);
			}
			db.setUsername(username);
			db.setPassword(pwd);
			db.setName("oracle");
			db.setDbtype(EnumDatabaseType.Oracle);

			adapter = new OracleAdapter();
			conn = adapter.getConnection(db);
		} else if (dbType.equals(EnumDatabaseType.DB2)) {
			if (driver != null && !driver.equals("")) {
				db.setDriver(driver);
			} else {
				db.setDriver(DB2Adapter.DEFAULT_DRIVER);
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
			conn = adapter.getConnection(db);
		} else if (dbType.equals(EnumDatabaseType.Informix)) {
			if (driver != null && !driver.equals("")) {
				db.setDriver(driver);
			} else {
				db.setDriver(InformixAdapter.DEFAULT_DRIVER);
			}
			if (port == null || port.equals("")) {
				port = "9088";
			}
			if (url != null && !url.equals("")) {
				db.setUrl(url);
			} else {
				db.setUrl("jdbc:informix-sqli://" + host + ":" + port + "/testdb:INFORMIXSERVER=ol_informix1170;");
			}
			// jdbc:informix-sqli://localhost:9090/testdb:INFORMIXSERVER=ol_demo1
			db.setUsername(username);
			db.setPassword(pwd);
			db.setName(EnumDatabaseType.Informix.name());
			db.setDbtype(EnumDatabaseType.Informix);

			adapter = new InformixAdapter();
			// Informix的连接中,需要将用户和密码与原URL组合为FullURL
			String fullURL = url + ";user=" + username + ";password=" + pwd;
			conn = adapter.createConnection(driver, fullURL);
		} else if (dbType.equals(EnumDatabaseType.SyBase)) {
			if (driver != null && !driver.equals("")) {
				db.setDriver(driver);
			} else {
				db.setDriver(SyBaseAdapter.DEFAULT_DRIVER);
			}
			if (port == null || port.equals("")) {
				port = "5000";
			}
			if (url != null && !url.equals("")) {
				db.setUrl(url);
			} else {

				db.setUrl("jdbc:sybase:Tds:" + host + ":" + port + "?charset=cp936");
			}
			db.setUsername(username);
			db.setPassword(pwd);
			db.setName(EnumDatabaseType.SyBase.name());
			db.setDbtype(EnumDatabaseType.SyBase);

			adapter = new SyBaseAdapter();
			conn = adapter.getConnection(db);
		}
		sycData.setAdapter(adapter);
		sycData.setConn(conn);
		return sycData;
	}

	@Override
	public String syncTableAndFiled(AbstractAdapter adapter, Connection conn, DataSchema dataSchema) {
		String result = "success";
		long start = new Date().getTime();
		Map<String, Map<String, DataField>> localModelMap = new HashMap<String, Map<String, DataField>>();
		String dbName = dataSchema.getName();
		List<DataTable> eList = testTableRepository.findByForeignKey(dataSchema.getId());
		List<DataTable> existtableList = new ArrayList<DataTable>();
		for (DataTable tb : eList) {
			// 查出来的过滤删除的表，和数据字典的表
			if (tb.getDeleted() == null || (tb.getDeleted() != null && tb.getDeleted() != true)
			        || tb.getIsDict() != null || (tb.getIsDict() != null && tb.getIsDict() != true)) {
				existtableList.add(tb);
			}
		}
		for (DataTable tb : existtableList) {
			Map<String, DataField> fmap = new HashMap<String, DataField>();
			for (DataField tf : tb.getDataFields()) {
				// 过滤掉已删除的字段记录，保存未被删除的记录
				if (tf.getDeleted() == null || (tf.getDeleted() != null && tf.getDeleted() != true)) {
					fmap.put(tf.getName(), tf);
				}
			}
			localModelMap.put(tb.getName(), fmap);
		}
		Map<String, Map<String, DataField>> remoteDBMap = new HashMap<String, Map<String, DataField>>();
		Map<String, DataField> ftmap = null;
		// 找到数据源同步过来的库，表，字段
		List<DataTable> dataModelList = adapter.getTable(dbName, conn);
		// get all fields of all table
		List<DataField> dataFieldList = adapter.getAllField(dbName, conn);

		List<DataTable> guolvTableList = new ArrayList<DataTable>();
		for (DataTable tb : dataModelList) {
			// 查出来的过滤删除的表，和数据字典的表,数据字典不同步
			if (tb.getDeleted() == null || (tb.getDeleted() != null && tb.getDeleted() != true)
			        || tb.getIsDict() != null || (tb.getIsDict() != null && tb.getIsDict() != true)) {
				tb.setDataSchema(dataSchema);
				guolvTableList.add(tb);
				remoteDBMap.put(tb.getName(), new HashMap<>());
			}
		}

		for (DataField field : dataFieldList) {// 遍历所有字段
			Map<String, DataField> fieldMap = null;
			String talbeName = field.getTalbeName();
			if (remoteDBMap.containsKey(talbeName)) {
				fieldMap = remoteDBMap.get(talbeName);
			} else {// tmap里面不包含这个表说明这个表不是数据字典的表不需要同步不用操作
				fieldMap = new HashMap<>();
				remoteDBMap.put(talbeName, fieldMap);
			}
			if (fieldMap.containsKey(field.getName())) {
				logger.info(talbeName + " 重复的 " + field.getName());
			} else {
				fieldMap.put(field.getName(), field);
			}

		}
		// 对两个map进行比较
		// 遍历要移动的map
		result = compareTwoMap(dataSchema, result, localModelMap, dbName, existtableList, remoteDBMap, guolvTableList);
		return result;
	}

	private String compareTwoMap(DataSchema dataSchema, String result,
	        Map<String, Map<String, DataField>> localModelMap, String dbName, List<DataTable> existtableList,
	        Map<String, Map<String, DataField>> remoteDBMap, List<DataTable> guolvTableList) {

		List<MetaHistory> metaList = new ArrayList<>();
		List<MetaHistoryItem> itemList = new ArrayList<>();
		List<DataField> saveFieldList = new ArrayList<>();// 需要保存的字段列表
		List<DataTable> saveTableList = new ArrayList<>();// 需要保存的表列表
		Map<String, DataTable> localTableMap = new HashMap<>();
		for (DataTable tb : existtableList) {
			localTableMap.put(tb.getName(), tb);
		}
		int i = 0;
		for (String tableNameKey : remoteDBMap.keySet()) {
			i++;
			logger.info("处理同步的表:[" + i + "/" + remoteDBMap.size() + "],remoteTable:" + tableNameKey);
			if (localModelMap.containsKey(tableNameKey)) {
				Map<String, DataField> localFields = localModelMap.get(tableNameKey);// 当前数据库中的模型定义
				Map<String, DataField> remoteFields = remoteDBMap.get(tableNameKey);// 从被测数据库读取的模型
				DataTable localTable = localTableMap.get(tableNameKey);
				List<DataField> tableFields = new ArrayList<>();
				// 处理表的字段
				for (String fieldName : remoteFields.keySet()) {
					if (localFields.containsKey(fieldName)) {
						// 本地有这个字段,名称相同，比较属性
						DataField localField = localFields.get(fieldName);
						DataField remoteField = remoteFields.get(fieldName);
						// 字段名称相同 判断数据的长度，数据的精度，数据的类型//属性发生变化已测通过
						MetaHistoryItem item = compareField(localField, remoteField, dbName, localTable);
						if (item != null) {
							itemList.add(item);
							tableFields.add(localField);// 更新字段
						}
					} else {
						// 名称不同，增加字段到这个表,更新表
						DataField newField = remoteFields.get(fieldName);
						newField.setDataTable(localTable);
						tableFields.add(newField);// 新增字段
						updateTable = true;
						String info = "更新表增加字段";
						// List<MetaHistoryItem> metaHistoryItemList = new
						// ArrayList<>();
						MetaHistoryItem item = czmetaHistory(tableNameKey, "", fieldName, EnumOptType.Add,
						        EnumOptType.Update, info);
						// metaHistoryItemList.add(metaHistoryItem);
						// upTbAndDb(metaHistoryItemList, dataSchema, dbName,
						// DataModel1);
						if (item != null)
							itemList.add(item);
					}
				}

				// 表的字段有修改，版本+1
				if (tableFields.size() > 0) {
					localTable.setVersion(localTable.getVersion() == null ? 1 : localTable.getVersion() + 1);
					saveTableList.add(localTable);
					saveFieldList.addAll(tableFields);
				}

				// 批量保存当前表的修改记录
				if (itemList.size() > 0) {
					MetaHistory metaHistory = createMetaHistory(itemList, dataSchema, dbName, localTable);
					metaList.add(metaHistory);
					// upTbAndDb(itemList, dataSchema, dbName, dataTable);
					// itemList.clear();
				}

				List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
				// 遍历同步的字段名称，同步过来的字段本地没有则把本地原来的字段删除
				for (String fieldName : localFields.keySet()) {
					if (!remoteFields.containsKey(fieldName)) {
						DataField localfield = localFields.get(fieldName);
						metaHistoryItemList = deleteFeild(localfield, tableNameKey);
						upTbAndDb(metaHistoryItemList, dataSchema, dataSchema.getName(), localTable);
					}
				}
			} else {
				// 表名不同，本地没有这张表，把这张表添加到本地
				// 找到要同步的表,从list中找
				DataTable needTongbuTable = null;
				for (DataTable t : guolvTableList) {
					if (t.getName().equals(tableNameKey)) {
						needTongbuTable = t;
					}
				}
				if (needTongbuTable != null) {
					result = sycAll(needTongbuTable, remoteDBMap, dbName, dataSchema);
				}
			}
		}

		// 批量保存新增字段
		if (saveFieldList.size() > 0) {
			long start = (new Date()).getTime();
			testFieldRepository.saveAll(saveFieldList);
			saveFieldList.clear();
			logger.info("数据库[" + dataSchema.getName() + "],保存字段[" + saveFieldList.size() + "]个,耗时:"
			        + DateUtil.getEclapsedTimesStr(start));
		}
		// 批量保存修改表
		if (saveTableList.size() > 0) {
			long start = (new Date()).getTime();
			testTableRepository.saveAll(saveTableList);
			saveFieldList.clear();
			logger.info("数据库[" + dataSchema.getName() + "],保存表[" + saveTableList.size() + "]个,耗时:"
			        + DateUtil.getEclapsedTimesStr(start));
		}
		// 批量保存更新记录
		if (metaList.size() > 0) {
			long start = (new Date()).getTime();
			metaHistoryRepository.saveAll(metaList);
			logger.info("数据库[" + dataSchema.getName() + "],保存更新记录[" + metaList.size() + "]个,耗时:"
			        + DateUtil.getEclapsedTimesStr(start));
		}
		// 批量保存更新记录明细
		if (itemList.size() > 0) {
			long start = (new Date()).getTime();
			metaHistoryItemRepository.saveAll(itemList);
			logger.info("数据库[" + dataSchema.getName() + "],保存更新记录明细[" + itemList.size() + "]个,耗时:"
			        + DateUtil.getEclapsedTimesStr(start));
		}

		// 遍历本地的库，同步的包不包含remoteDBMap
		for (String tableNameKey : localModelMap.keySet()) {
			if (!remoteDBMap.containsKey(tableNameKey)) {
				// 删除本地表
				deletemeta(tableNameKey, dataSchema, existtableList, localModelMap, dbName);
			}
		}
		return result;
	}

	/**
	 * 更新本地的字段属性
	 * 
	 * @param localField
	 *            本地字段
	 * @param remoteField
	 * @param fieldTypeChangeDesc
	 * @return
	 */
	private MetaHistoryItem upField(DataField localField, DataField remoteField, String fieldTypeChangeDesc) {
		// 名称不能作为被更新属性
		localField.setDataLength(remoteField.getDataLength());
		localField.setDataPrecision(remoteField.getDataPrecision());
		localField.setDataType(remoteField.getDataType());
		localField.setDes(remoteField.getDes());
		localField.setFieldId(remoteField.getFieldId());
		localField.setIsNullable(remoteField.getIsNullable());
		localField.setIsPrimaryKey(remoteField.getIsPrimaryKey());
		localField.setIsForeignKey(remoteField.getIsForeignKey());
		localField.setIsIndex(remoteField.getIsIndex());

		updateTable = true;
		String info = "更新字段属性";

		MetaHistoryItem metaHistoryItem = czmetaHistory(localField.getTalbeName(), localField.getName(),
		        remoteField.getName() + fieldTypeChangeDesc, EnumOptType.Update, EnumOptType.Update, info);
		metaHistoryItem.setFieldTypeChangeDesc(fieldTypeChangeDesc);
		return metaHistoryItem;
		// metaHistoryItemList.add(metaHistoryItem);
		// return metaHistoryItemList;
	}

	public List<MetaHistoryItem> upfiled(DataField localfield, String dbName, String fieldTypeChangeDesc,
	        DataTable dataTable, DataField removetf) {
		List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
		String name1 = localfield.getName();
		String name2 = removetf.getName();
		String tableName = dataTable.getName();
		localfield.setDataTable(dataTable);
		int id = localfield.getId();// 本地的字段tb1
		Date createTime = localfield.getCreateTime();
		String createUser = localfield.getCreateUser();
		Date editTime = new Date();
		String editUser = localfield.getEditUser();
		if (removetf.getEditUser() != null) {
			editUser = removetf.getEditUser();
		}
		Integer dataLength = localfield.getDataLength();
		if (removetf.getDataLength() != null) {
			dataLength = removetf.getDataLength();
		}
		int dataPrecision = localfield.getDataPrecision();
		if (removetf.getDataPrecision() != null) {
			dataPrecision = removetf.getDataPrecision();
		}
		String dataType = localfield.getDataType();
		if (removetf.getDataType() != null) {
			dataType = removetf.getDataType();
		}
		String des = localfield.getDes();
		if (removetf.getDes() != null) {
			des = removetf.getDes();
		}
		boolean isForeignKey = false;
		if (removetf.getIsForeignKey() != null) {
			isForeignKey = removetf.getIsForeignKey();
		}
		boolean isIndex = false;
		if (removetf.getIsIndex() != null) {
			isIndex = removetf.getIsIndex();
		}
		boolean isNullable = false;
		if (removetf.getIsNullable() != null) {
			isNullable = removetf.getIsNullable();
		}
		boolean isPrimaryKey = false;
		if (removetf.getIsPrimaryKey() != null) {
			isPrimaryKey = removetf.getIsPrimaryKey();
		}
		String name = localfield.getName();
		String talbeName = localfield.getTalbeName();
		boolean deleted = false;

		if (removetf.getDeleted() != null) {
			deleted = removetf.getDeleted();
		}

		int version0 = 1;
		if (localfield.getVersion() != null) {
			version0 = localfield.getVersion() + 1;
		}

		testFieldRepository.updateOneByID(createTime, createUser, editTime, editUser, dataLength, dataPrecision,
		        dataType, des, isForeignKey, isIndex, isNullable, isPrimaryKey, name, talbeName, deleted, version0, id);

		updateTable = true;
		String info = "更新字段属性";
		MetaHistoryItem metaHistoryItem = czmetaHistory(tableName, name1, name2 + fieldTypeChangeDesc,
		        EnumOptType.Update, EnumOptType.Update, info);
		metaHistoryItem.setFieldTypeChangeDesc(fieldTypeChangeDesc);
		metaHistoryItemList.add(metaHistoryItem);
		return metaHistoryItemList;

	}

	public void deletemeta(String tableNameKey, DataSchema dataSchema, List<DataTable> existtableList,
	        Map<String, Map<String, DataField>> localModelMap, String dbName) {

		// 找到本地要删除的表
		DataTable existDataModel = null;
		for (DataTable tb : existtableList) {
			if (tb.getName().equals(tableNameKey)) {
				existDataModel = tb;
			}
		}
		// 本地找到这张表的字段进行删除
		List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
		Map<String, DataField> flocal = localModelMap.get(tableNameKey);
		// 遍历字段进行删除
		for (String filedNameKey : flocal.keySet()) {
			DataField deleteField = flocal.get(filedNameKey);
			testFieldRepository.deletedByID(true, deleteField.getId());
			updateTable = true;
			String info = "删除表删除字段";
			MetaHistoryItem metaHistoryItem = czmetaHistory(tableNameKey, filedNameKey, "", EnumOptType.Delete,
			        EnumOptType.Delete, info);
			metaHistoryItemList.add(metaHistoryItem);
		}
		// 删除表
		existDataModel.setDeleted(true);
		testTableRepository.deletedByID(true, existDataModel.getId());
		upTbAndDb(metaHistoryItemList, dataSchema, dbName, existDataModel);
	}

	public List<MetaHistoryItem> deleteFeild(DataField localfield, String tableNameKey) {
		List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
		testFieldRepository.deletedByID(true, localfield.getId());
		updateTable = true;
		String info = "更新表删除字段";
		MetaHistoryItem metaHistoryItem = czmetaHistory(tableNameKey, localfield.getName(), "", EnumOptType.Delete,
		        EnumOptType.Delete, info);
		metaHistoryItemList.add(metaHistoryItem);
		return metaHistoryItemList;
	}

	public MetaHistoryItem czmetaHistory(String tableName, String orgFieldName, String refFieldName,
	        EnumOptType fieldOptType, EnumOptType tableOptType, String info) {
		MetaHistoryItem metaHistoryItem = new MetaHistoryItem();
		metaHistoryItem.setCreateTime(new Date());
		metaHistoryItem.setTableName(tableName);
		metaHistoryItem.setOrgFiledName(orgFieldName);
		metaHistoryItem.setRefFieldName(refFieldName);
		metaHistoryItem.setOptFieldType(fieldOptType);
		metaHistoryItem.setOptTableType(tableOptType);
		metaHistoryItem.setInfo(info);
		return metaHistoryItem;
	}

	/**
	 * 创建一个元数据更新记录
	 * 
	 * @param metaHistoryItemList
	 * @param dataSchema
	 * @param dbName
	 * @param dataTable
	 * @return
	 */
	private MetaHistory createMetaHistory(List<MetaHistoryItem> metaHistoryItemList, DataSchema dataSchema,
	        String dbName, DataTable dataTable) {
		MetaHistory metaHistory = new MetaHistory();
		metaHistory.setDataSchema(dataSchema);
		metaHistory.setStartTime(new Date());
		// metaHistory.setItems(metaHistoryItemList);
		for (MetaHistoryItem item : metaHistoryItemList) {
			item.setMetaHistory(metaHistory);
		}
		return metaHistory;
	}

	public void upTbAndDb(List<MetaHistoryItem> metaHistoryItemList, DataSchema dataSchema, String dbName,
	        DataTable dataTable) {
		updateTable = true;
		if (updateTable) {
			MetaHistory metaHistory = new MetaHistory();
			metaHistory.setCreateTime(new Date());
			metaHistory.setDataSchema(dataSchema);
			metaHistoryRepository.save(metaHistory);
			List<MetaHistoryItem> itemList = new ArrayList<MetaHistoryItem>();
			for (int q = 0; q < metaHistoryItemList.size(); q++) {
				MetaHistoryItem mi = metaHistoryItemList.get(q);
				mi.setMetaHistory(metaHistory);
				itemList.add(mi);
			}
			metaHistoryItemRepository.saveAll(itemList);
			if (dataTable.getVersion() != null) {
				int version = dataTable.getVersion() + 1;
				testTableRepository.update(version, dataTable.getId());
			} else {
				int version = 1;
				testTableRepository.update(version, dataTable.getId());
			}
			updateDB = true;

		}
		if (updateDB) {
			if (dataSchema.getVersion() != null) {
				int version = dataSchema.getVersion() + 1;
				testDatabaseRepository.update(version, dataSchema.getId());
			} else {
				int version = 1;
				testDatabaseRepository.update(version, dataSchema.getId());
			}
		}
	}

	public String sycAll(DataTable guolvTable, Map<String, Map<String, DataField>> tmap, String dbName,
	        DataSchema dataSchema) {
		long start = new Date().getTime();
		// 库里没有这个源的所有表
		// 第一次同步，全部添加表,全部同步字段
		guolvTable.setDeleted(false);
		testTableRepository.save(guolvTable);
		Map<String, DataField> removeFiled = tmap.get(guolvTable.getName());
		List<DataField> saveFieldList = new ArrayList<>();
		List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
		for (String filedName : removeFiled.keySet()) {
			DataField tf1 = removeFiled.get(filedName);
			tf1.setDataTable(guolvTable);
			saveFieldList.add(tf1);
			String info = "新增表新增字段";
			MetaHistoryItem metaHistoryItem = czmetaHistory(guolvTable.getName(), "", tf1.getName(), EnumOptType.Add,
			        EnumOptType.Add, info);
			metaHistoryItemList.add(metaHistoryItem);
		}
		testFieldRepository.saveAll(saveFieldList);
		logger.info("save table' fields,table:" + guolvTable.getName() + " use " + DateUtil.getEclapsedTimesStr(start));
		upTbAndDb(metaHistoryItemList, dataSchema, dbName, guolvTable);
		return "success";
	}

	public MetaHistoryItem compareField(DataField localfield, DataField removetf, String dbName, DataTable dataModel) {
		// List<MetaHistoryItem> metaHistoryItemList = null;
		// 字段名称相同的话比较下面的属性
		Integer data_len1 = localfield.getDataLength();// 本地
		String name1 = localfield.getName();
		int data_pre1 = localfield.getDataPrecision() == null ? localfield.getDataLength().intValue()
		        : localfield.getDataLength().intValue();
		String data_type1 = localfield.getDataType();
		Boolean isForeignKey1 = localfield.getIsForeignKey();
		Boolean isIndex1 = localfield.getIsIndex();
		Boolean isNullable1 = localfield.getIsNullable();
		Boolean isPrimaryKey1 = localfield.getIsPrimaryKey();
		int flag = 0;
		String fieldTypeChangeDesc = "";
		Integer data_len2 = removetf.getDataLength();// 同步的
		String name2 = removetf.getName();
		int data_pre2 = removetf.getDataPrecision() == null ? removetf.getDataLength().intValue()
		        : removetf.getDataLength().intValue();
		String data_type2 = removetf.getDataType();
		Boolean isForeignKey2 = removetf.getIsForeignKey();
		Boolean isIndex2 = removetf.getIsIndex();
		Boolean isNullable2 = removetf.getIsNullable();
		Boolean isPrimaryKey2 = removetf.getIsPrimaryKey();
		// 判断是字段属性是否改变,1：无变化,2：改变
		String re = compare(flag, data_len1, data_pre1, data_type1, isForeignKey1, isIndex1, isNullable1, isPrimaryKey1,
		        data_len2, data_pre2, data_type2, isForeignKey2, isIndex2, isNullable2, isPrimaryKey2);
		if (re.contains(":")) {
			fieldTypeChangeDesc = re.split(":")[0];
			flag = Integer.parseInt(re.split(":")[1]);
		} else {
			flag = Integer.parseInt(re);
		}
		MetaHistoryItem item = null;
		if (flag == 2) {
			// 字段修改
			// metaHistoryItemList = upfiled(localfield, dbName,
			// fieldTypeChangeDesc, dataModel, removetf);
			item = upField(localfield, removetf, fieldTypeChangeDesc);
			// metaHistoryItemList.add(item);
		}
		if (flag == 1) {
			// 这些比较的属性没有变化，不做处理
			logger.info("这张表" + localfield.getTalbeName() + "的" + name1 + "字段在其他比较的属性中没有变化");

		}
		// return metaHistoryItemList;
		return item;
	}

	private String compare(int flag, Integer data_len1, int data_pre1, String data_type1, Boolean isForeignKey1,
	        Boolean isIndex1, Boolean isNullable1, Boolean isPrimaryKey1, Integer data_len2, int data_pre2,
	        String data_type2, Boolean isForeignKey2, Boolean isIndex2, Boolean isNullable2, Boolean isPrimaryKey2) {
		String fieldTypeChangeDesc = "";
		flag = 1;
		int flag1 = 0;
		int flag2 = 0;
		int flag3 = 0;
		int flag4 = 0;
		int flag5 = 0;
		int flag6 = 0;
		int flag7 = 0;
		if (!CompareUtil.compareTo(data_len1, data_len2)) {
			flag1 = 1;
			fieldTypeChangeDesc = fieldTypeChangeDesc + "数据长度，原来是" + data_len1 + "现在是" + data_len2 + "，";
		}
		if (!CompareUtil.compareTo(data_pre1, data_pre2)) {
			flag2 = 1;
			fieldTypeChangeDesc = fieldTypeChangeDesc + "数据精度，原来是" + data_pre1 + "现在是" + data_pre2 + "，";
		}
		if (!CompareUtil.compareTo(data_type1, data_type2)) {
			flag3 = 1;
			fieldTypeChangeDesc = fieldTypeChangeDesc + "数据类型，原来是" + data_type1 + "现在是" + data_type2 + "，";
		}
		if (!CompareUtil.compareTo(isForeignKey1, isForeignKey2)) {
			flag4 = 1;
			fieldTypeChangeDesc = fieldTypeChangeDesc + "数据是否为外键，原来是" + isForeignKey1 + "现在是" + isForeignKey2 + "，";
		}
		if (!CompareUtil.compareTo(isIndex1, isIndex2)) {
			flag5 = 1;
			fieldTypeChangeDesc = fieldTypeChangeDesc + "数据是否为索引，原来是" + isIndex1 + "现在是" + isIndex2 + "，";
		}
		if (!CompareUtil.compareTo(isNullable1, isNullable2)) {
			flag6 = 1;
			fieldTypeChangeDesc = fieldTypeChangeDesc + "数据是否为索引，原来是" + isNullable1 + "现在是" + isNullable2 + "，";
		}
		if (!CompareUtil.compareTo(isPrimaryKey1, isPrimaryKey2)) {
			flag7 = 1;
			fieldTypeChangeDesc = fieldTypeChangeDesc + "数据是否为索引，原来是" + isPrimaryKey1 + "现在是" + isPrimaryKey2 + "，";
		}

		StringBuffer sb = new StringBuffer();
		sb.append(fieldTypeChangeDesc);
		sb.append(":");
		sb.append(fieldTypeChangeDesc.isEmpty() ? "1" : "2");
		if (fieldTypeChangeDesc == "") {
			return "1";
		}
		return sb.toString();
	}

	@Override
	public String syncOneTableAndFiled(AbstractAdapter adapter, Connection conn, DataTable exitsDataTable) {
		String result = "success";
		long start = new Date().getTime();
		Map<String, Map<String, DataField>> localModelMap = new HashMap<String, Map<String, DataField>>();

		// 查出来的过滤删除的表，和数据字典的表
		if ((exitsDataTable.getDeleted() != null && exitsDataTable.getDeleted() == true)
		        || (exitsDataTable.getIsDict() != null && exitsDataTable.getIsDict() == true)) {
			exitsDataTable = null;
		}
		List<DataTable> existtableList = new ArrayList<DataTable>();
		existtableList.add(exitsDataTable);
		Map<String, DataField> fmap = new HashMap<String, DataField>();
		for (DataField tf : exitsDataTable.getDataFields()) {
			// 过滤掉已删除的字段记录，保存未被删除的记录
			if (tf.getDeleted() == null || (tf.getDeleted() != null && tf.getDeleted() != true)) {
				fmap.put(tf.getName(), tf);
			}
		}
		localModelMap.put(exitsDataTable.getName(), fmap);
		Map<String, Map<String, DataField>> remoteDBMap = new HashMap<String, Map<String, DataField>>();
		Map<String, DataField> ftmap = null;
		// 找到数据源同步过来的表，字段
		DataSchema dataSchema = exitsDataTable.getDataSchema();
		String dbName = dataSchema.getName();
		DataTable removeDataTable = adapter.getTable(dbName, exitsDataTable.getName(), conn);
		// get all fields of this table
		List<DataField> dataFieldList = adapter.getField(dbName, exitsDataTable.getName(), conn);
		List<DataTable> guolvTableList = new ArrayList<DataTable>();

		// 查出来的过滤删除的表，和数据字典的表,数据字典不同步
		if ((removeDataTable.getDeleted() != null && removeDataTable.getDeleted() == true)
		        || (removeDataTable.getIsDict() != null && removeDataTable.getIsDict() == true)) {
			removeDataTable = null;
		} else {
			remoteDBMap.put(removeDataTable.getName(), new HashMap<>());
		}
		guolvTableList.add(removeDataTable);
		for (DataField field : dataFieldList) {// 遍历所有字段
			Map<String, DataField> fieldMap = null;
			String talbeName = field.getTalbeName();
			if (remoteDBMap.containsKey(talbeName)) {
				fieldMap = remoteDBMap.get(talbeName);
			} else {// tmap里面不包含这个表说明这个表不是数据字典的表不需要同步不用操作
				fieldMap = new HashMap<>();
				remoteDBMap.put(talbeName, fieldMap);
			}
			if (fieldMap.containsKey(field.getName())) {
				logger.info(talbeName + " 重复的 " + field.getName());
			} else {
				fieldMap.put(field.getName(), field);
			}

		}
		// 对两个map进行比较
		// 遍历要移动的map
		result = compareTwoMap(dataSchema, result, localModelMap, dbName, existtableList, remoteDBMap, guolvTableList);
		return result;
	}

	@Override
	public JSONObject delOneDatasource(int dataSourceId) throws SQLException {
		JSONObject result = new JSONObject();
		result.put("result", "删除成功");
		result.put("state", "1");
		List<DataSchema> dataSchemas = testDatabaseRepository.findByForeignKey(dataSourceId);
		for (DataSchema dataSchema : dataSchemas) {
			dataSchema.setDataSource(null);
			testDatabaseRepository.save(dataSchema);
		}
		List<QualityTestQuery> qualityTestQuerys = qualityTestQueryRepository.findByDataSourceId(dataSourceId);
		if (qualityTestQuerys.size() != 0) {
			result.put("state", "0");
			result.put("result", "这个数据源关联案例不能删除");
			return result;
		}
		List<TestQuery> testQuerys = testQueryRepository.findByDataSourceId(dataSourceId);
		if (testQuerys.size() != 0) {
			result.put("state", "0");
			result.put("result", "这个数据源关联案例不能删除");
			return result;
		}
		repository.deleteById(dataSourceId);
		return result;
	}

	@Override
	public boolean GetDBLink(DataSource ds) throws SQLException {
		AbstractAdapter adapter = null;

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

		Connection conn = null;
		try {
			SycData sycData = getAdapterAndConnection(dbType, driver, url, port, host, username, pwd, sid);
			adapter = sycData.getAdapter();
			conn = sycData.getConn();
			if (conn != null) {
				return true;
			} else {
				return false;
			}

		} catch (Exception e) {
			System.out.println("e=" + e.getLocalizedMessage());
			return false;
		} finally {
			conn.close();
		}

	}

	@Override
	public String getUrl(DataSourceVO vo) {
		String url = "";
		String dbType = vo.getDatabaseType();
		String host = vo.getHost();
		String port = vo.getPort();
		String schema = vo.getDefaultSchema();
		String characterSet = vo.getCharacterSet();
		if (dbType.equals("Mysql")) {
			url = "jdbc:mysql://" + host + ":" + port + "/" + schema
			        + "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=GMT%2b8&useUnicode=true&characterEncoding="
			        + characterSet;
		} else if (dbType.equals("Oracle")) {
			String sid = vo.getSid();
			url = "jdbc:oracle:thin:@" + host + ":" + port + ":" + sid;
		} else if (dbType.equals("DB2")) {
			url = "jdbc:db2://" + host + ":" + port + "/" + schema;
		} else if (dbType.equals("Informix")) {
			url = "jdbc:informix-sqli://" + host + ":" + port + "/testdb:INFORMIXSERVER=" + schema;
		} else if (dbType.equals("SyBase")) {
			url = "jdbc:sybase:Tds:" + host + ":" + port + "/" + schema;
		}
		return url;
	}

	@Override
	public Integer findDataSourceId(Integer id) {
		return repository.findDataSourceId(id);
	}
}