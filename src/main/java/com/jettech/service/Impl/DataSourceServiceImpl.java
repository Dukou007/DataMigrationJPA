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
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataSchemaRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.DataTableRepository;
import com.jettech.repostory.MetaHistoryItemRepository;
import com.jettech.repostory.MetaHistoryRepository;
import com.jettech.service.DataSchemaService;
import com.jettech.service.IDataSourceService;
import com.jettech.util.DateUtil;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.SycData;

@Service
public class DataSourceServiceImpl implements IDataSourceService {
	private static Logger logger = LoggerFactory
			.getLogger(DataSourceServiceImpl.class);
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
		 String message="";
		 if(dataSourceId==null){
				message="源id不能为空，不能进行同步";
				return message;
			}
		AbstractAdapter adapter = null;
	//	DataSource ds = repository.getOne(dataSourceId);
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
		SycData sycData = getAdapterAndConnection(dbType, driver, url, port,
				host, username, pwd, sid);
		conn = sycData.getConn();
		adapter = sycData.getAdapter();
		// 尝试读取该用户访问权限下的所有schema(默认的)
		List<DataSchema> testdatabaseList = adapter.getDatabase(schema, conn);
		DataSource dataSource = repository.findByName(name);
		for (int p = 0; p < testdatabaseList.size(); p++) {
			DataSchema tb = (DataSchema) testdatabaseList.get(p);
			tb.setDataSource(dataSource);
			tb.setIsDict(false);
			String testDataBaseName = testdatabaseList.get(p).getName();
			DataSchema dataSchema = testDatabaseRepository
					.findByNameAndDataSourceId(testDataBaseName, dataSourceId);
			if (dataSchema != null) {
				syncTableAndFiled(adapter, conn, dataSchema);
			} else if(dataSchema == null){
				DataSchema savedataSchema = testDatabaseRepository.save(tb);// 保存库
				// 同步表和字段
				syncTableAndFiled(adapter, conn, savedataSchema);
			}/*else if(dataSchema != null && dataSchema.getDataSource().getId()==dataSourceId
					&& dataSchema.getName()==schema){
				logger.info("名为"+dataSchema.getName()+"的数据库"+"名为"+dataSourceId+"的数据源已经存在，不能同步");
			    message="名为"+dataSchema.getName()+"的数据库"+"名为"+dataSourceId+"的数据源已经存在，不能同步";
			}*/
			
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
	public SycData getAdapterAndConnection(EnumDatabaseType dbType,
			String driver, String url, String port, String host,
			String username, String pwd, String sid) {
		SycData sycData = new SycData();
		DbModel db = new DbModel();
		AbstractAdapter adapter = null;
		Connection conn = null;
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
		} else if (dbType.equals(EnumDatabaseType.SyBase)) {
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
		sycData.setAdapter(adapter);
		sycData.setConn(conn);
		return sycData;
	}

	@Override
	public String syncTableAndFiled(AbstractAdapter adapter, Connection conn,
			DataSchema dataSchema) {
		String result = "success";
		long start = new Date().getTime();
		Map<String, Map<String, DataField>> localModelMap = new HashMap<String, Map<String, DataField>>();
		String dbName = dataSchema.getName();
		List<DataTable> eList = testTableRepository.findByForeignKey(dataSchema.getId());
		List<DataTable> existtableList = new ArrayList<DataTable>();
		for (DataTable tb : eList) {
			// 查出来的过滤删除的表，和数据字典的表
			if (tb.getDeleted() == null
					|| (tb.getDeleted() != null && tb.getDeleted() != true)
					|| tb.getIsDict() != null
					|| (tb.getIsDict() != null && tb.getIsDict() != true)) {
				existtableList.add(tb);
			}
		}
		for (DataTable tb : existtableList) {
			Map<String, DataField> fmap = new HashMap<String, DataField>();
			for (DataField tf : tb.getDataFields()) {
				// 过滤掉已删除的字段记录，保存未被删除的记录
				if (tf.getDeleted() == null
						|| (tf.getDeleted() != null && tf.getDeleted() != true)) {
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
			if (tb.getDeleted() == null
					|| (tb.getDeleted() != null && tb.getDeleted() != true)
					|| tb.getIsDict() != null
					|| (tb.getIsDict() != null && tb.getIsDict() != true)) {
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
		result = compareTwoMap(dataSchema, result, localModelMap, dbName,
				existtableList, remoteDBMap, guolvTableList);
		return result;
	}

	private String compareTwoMap(DataSchema dataSchema, String result,
			Map<String, Map<String, DataField>> localModelMap, String dbName,
			List<DataTable> existtableList,
			Map<String, Map<String, DataField>> remoteDBMap,
			List<DataTable> guolvTableList) {
		for (String tableNameKey : remoteDBMap.keySet()) {
			if (localModelMap.containsKey(tableNameKey)) {
				Map<String, DataField> flocal = localModelMap.get(tableNameKey);
				Map<String, DataField> ftongbu = remoteDBMap.get(tableNameKey);
				DataTable DataModel1 = null;
				for (DataTable tb : existtableList) {
					if (tb.getName().equals(tableNameKey)) {
						DataModel1 = tb;
					}
				}
				for (String fieldName : ftongbu.keySet()) {
					if (flocal.containsKey(fieldName)) {
						// 本地有这个字段,名称相同，比较属性
						DataField dataFieldLocal = flocal.get(fieldName);
						DataField dataFieldTongbu = ftongbu.get(fieldName);
						// 字段名称相同 判断数据的长度，数据的精度，数据的类型//属性发生变化已测通过
						List<MetaHistoryItem> item = sameNameCompare(
								dataFieldLocal, dataFieldTongbu, dbName,
								DataModel1);

						if (item != null) {
							upTbAndDb(item, dataSchema, dbName, DataModel1);
						}

					} else {// 名称不同，增加字段到这个表,更新表
						DataField tongbuField = ftongbu.get(fieldName);
						tongbuField.setDataTable(DataModel1);
						testFieldRepository.save(tongbuField);
						updateTable = true;
						String info = "更新表增加字段";
						List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
						MetaHistoryItem metaHistoryItem = czmetaHistory(
								tableNameKey, "", fieldName, EnumOptType.Add,
								EnumOptType.Update, info);
						metaHistoryItemList.add(metaHistoryItem);
						upTbAndDb(metaHistoryItemList, dataSchema, dbName,
								DataModel1);
					}
				}
				List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
				// 遍历同步的字段名称，同步过来的字段本地没有则把本地原来的字段删除
				for (String fieldName : flocal.keySet()) {
					if (!ftongbu.containsKey(fieldName)) {
						DataField localfield = flocal.get(fieldName);
						metaHistoryItemList = deleteFeild(localfield,
								tableNameKey);
						upTbAndDb(metaHistoryItemList, dataSchema,
								dataSchema.getName(), DataModel1);
					}
				}
			} else {// 表名不同，本地没有这张表，把这张表添加到本地
					// 找到要同步的表,从list中找
				DataTable needTongbuTable = null;
				for (DataTable t : guolvTableList) {
					if (t.getName().equals(tableNameKey)) {
						needTongbuTable = t;
					}
				}
				if (needTongbuTable != null) {
					result = sycAll(needTongbuTable, remoteDBMap, dbName,
							dataSchema);
				}
			}
		}
		// 遍历本地的库，同步的包不包含remoteDBMap
		for (String tableNameKey : localModelMap.keySet()) {
			if (!remoteDBMap.containsKey(tableNameKey)) {
				// 删除本地表
				deletemeta(tableNameKey, dataSchema, existtableList,
						localModelMap, dbName);
			}
		}
		return result;
	}

	public List<MetaHistoryItem> upfiled(DataField localfield, String dbName,
			String fieldTypeChangeDesc, DataTable dataTable, DataField removetf) {
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
		int dataLength = localfield.getDataLength();
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

		testFieldRepository.updateOneByID(createTime, createUser, editTime,
				editUser, dataLength, dataPrecision, dataType, des,
				isForeignKey, isIndex, isNullable, isPrimaryKey, name,
				talbeName, deleted, version0, id);

		updateTable = true;
		String info = "更新字段属性";
		MetaHistoryItem metaHistoryItem = czmetaHistory(tableName, name1,
				name2, EnumOptType.Update, EnumOptType.Update, info);
		metaHistoryItem.setFieldTypeChangeDesc(fieldTypeChangeDesc);
		metaHistoryItemList.add(metaHistoryItem);
		return metaHistoryItemList;

	}

	public void deletemeta(String tableNameKey, DataSchema dataSchema,
			List<DataTable> existtableList,
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
			MetaHistoryItem metaHistoryItem = czmetaHistory(tableNameKey, "",
					filedNameKey, EnumOptType.Delete, EnumOptType.Delete, info);
			metaHistoryItemList.add(metaHistoryItem);
		}
		// 删除表
		existDataModel.setDeleted(true);
		testTableRepository.deletedByID(true, existDataModel.getId());
		upTbAndDb(metaHistoryItemList, dataSchema, dbName, existDataModel);
	}

	public List<MetaHistoryItem> deleteFeild(DataField localfield,
			String tableNameKey) {
		List<MetaHistoryItem> metaHistoryItemList = new ArrayList<>();
		testFieldRepository.deletedByID(true, localfield.getId());
		updateTable = true;
		String info = "更新表删除字段";
		MetaHistoryItem metaHistoryItem = czmetaHistory(tableNameKey, "",
				localfield.getName(), EnumOptType.Delete, EnumOptType.Delete,
				info);
		metaHistoryItemList.add(metaHistoryItem);
		return metaHistoryItemList;
	}

	public MetaHistoryItem czmetaHistory(String tableName, String otfName,
			String tfName, EnumOptType t1, EnumOptType t2, String info) {
		MetaHistoryItem metaHistoryItem = new MetaHistoryItem();
		metaHistoryItem.setCreateTime(new Date());
		metaHistoryItem.setTableName(tableName);
		metaHistoryItem.setOrgFiledName(otfName);
		metaHistoryItem.setRefFieldName(tfName);
		metaHistoryItem.setOptFieldType(t1);
		metaHistoryItem.setOptTableType(t2);
		metaHistoryItem.setInfo(info);
		return metaHistoryItem;
	}

	public void upTbAndDb(List<MetaHistoryItem> metaHistoryItemList,
			DataSchema dataSchema, String dbName, DataTable dataTable) {
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

	public String sycAll(DataTable guolvTable,
			Map<String, Map<String, DataField>> tmap, String dbName,
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
			MetaHistoryItem metaHistoryItem = czmetaHistory(
					guolvTable.getName(), "", tf1.getName(), EnumOptType.Add,
					EnumOptType.Add, info);
			metaHistoryItemList.add(metaHistoryItem);
		}
		testFieldRepository.saveAll(saveFieldList);
		logger.info("save table' fields,table:" + guolvTable.getName()
				+ " use " + DateUtil.getEclapsedTimesStr(start));
		upTbAndDb(metaHistoryItemList, dataSchema, dbName, guolvTable);
		return "success";
	}

	public List<MetaHistoryItem> sameNameCompare(DataField localfield,
			DataField removetf, String dbName, DataTable dataModel) {
		List<MetaHistoryItem> metaHistoryItemList = null;
		// 字段名称相同的话比较下面的属性
		int data_len1 = localfield.getDataLength();// 本地
		String name1 = localfield.getName();
		int data_pre1 = localfield.getDataPrecision();
		String data_type1 = localfield.getDataType();
		Boolean isForeignKey1 = localfield.getIsForeignKey();
		Boolean isIndex1 = localfield.getIsIndex();
		Boolean isNullable1 = localfield.getIsNullable();
		Boolean isPrimaryKey1 = localfield.getIsPrimaryKey();
		int flag = 0;
		String fieldTypeChangeDesc = "";
		int data_len2 = removetf.getDataLength();// 同步的
		String name2 = removetf.getName();
		int data_pre2 = removetf.getDataPrecision();
		String data_type2 = removetf.getDataType();
		Boolean isForeignKey2 = removetf.getIsForeignKey();
		Boolean isIndex2 = removetf.getIsIndex();
		Boolean isNullable2 = removetf.getIsNullable();
		Boolean isPrimaryKey2 = removetf.getIsPrimaryKey();
		String re = compare(flag, data_len1, data_pre1, data_type1,
				isForeignKey1, isIndex1, isNullable1, isPrimaryKey1, data_len2,
				data_pre2, data_type2, isForeignKey2, isIndex2, isNullable2,
				isPrimaryKey2);
		if (re.contains(":")) {
			fieldTypeChangeDesc = re.split(":")[0];
			flag = Integer.parseInt(re.split(":")[1]);
		} else {
			flag = Integer.parseInt(re);
		}

		if (flag == 2) {
			// 字段修改
			metaHistoryItemList = upfiled(localfield, dbName,
					fieldTypeChangeDesc, dataModel, removetf);
		}
		if (flag == 1) {
			// 这些比较的属性没有变化，不做处理
			logger.info("这张表" + localfield.getTalbeName() + "的" + name1
					+ "字段在其他比较的属性中没有变化");

		}
		return metaHistoryItemList;
	}

	public String compare(int flag, int data_len1, int data_pre1,
			String data_type1, Boolean isForeignKey1, Boolean isIndex1,
			Boolean isNullable1, Boolean isPrimaryKey1, int data_len2,
			int data_pre2, String data_type2, Boolean isForeignKey2,
			Boolean isIndex2, Boolean isNullable2, Boolean isPrimaryKey2) {
		String fieldTypeChangeDesc = "";
		flag = 1;
		int flag1 = 0;
		int flag2 = 0;
		int flag3 = 0;
		int flag4 = 0;
		int flag5 = 0;
		int flag6 = 0;
		int flag7 = 0;
		if (data_len1 != data_len2 || data_pre1 != data_pre2
				|| !data_type1.equals(data_type2)
				|| isForeignKey1 != isForeignKey2 || isIndex1 != isIndex2
				|| isNullable1 != isNullable2 || isPrimaryKey1 != isPrimaryKey2) {
			flag = 2;
			if (data_len1 != data_len2) {
				flag1 = 1;
				fieldTypeChangeDesc = fieldTypeChangeDesc + "数据长度，原来是"
						+ data_len1 + "现在是" + data_len2 + "，";
			}
			if (data_pre1 != data_pre2) {
				flag2 = 1;
				fieldTypeChangeDesc = fieldTypeChangeDesc + "数据精度，原来是"
						+ data_pre1 + "现在是" + data_pre2 + "，";
			}
			if (!data_type1.equals(data_type2)) {
				flag3 = 1;
				fieldTypeChangeDesc = fieldTypeChangeDesc + "数据类型，原来是"
						+ data_type1 + "现在是" + data_type2 + "，";
			}
			if (isForeignKey1 != isForeignKey2) {
				flag4 = 1;
				fieldTypeChangeDesc = fieldTypeChangeDesc + "数据是否为外键，原来是"
						+ isForeignKey1 + "现在是" + isForeignKey2 + "，";
			}
			if (isIndex1 != isIndex2) {
				flag5 = 1;
				fieldTypeChangeDesc = fieldTypeChangeDesc + "数据是否为索引，原来是"
						+ isIndex1 + "现在是" + isIndex2 + "，";
			}
			if (isNullable1 != isNullable2) {
				flag6 = 1;
				fieldTypeChangeDesc = fieldTypeChangeDesc + "数据是否为索引，原来是"
						+ isNullable1 + "现在是" + isNullable2 + "，";
			}
			if (isPrimaryKey1 != isPrimaryKey2) {
				flag7 = 1;
				fieldTypeChangeDesc = fieldTypeChangeDesc + "数据是否为索引，原来是"
						+ isPrimaryKey1 + "现在是" + isPrimaryKey2 + "，";
			}
		}
		StringBuffer sb = new StringBuffer();
		sb.append(fieldTypeChangeDesc);
		sb.append(":");
		sb.append(flag);
		return sb.toString();
	}

	@Override
	public String syncOneTableAndFiled(AbstractAdapter adapter,
			Connection conn, DataTable exitsDataTable) {
		String result = "success";
		long start = new Date().getTime();
		Map<String, Map<String, DataField>> localModelMap = new HashMap<String, Map<String, DataField>>();

		// 查出来的过滤删除的表，和数据字典的表
		if ((exitsDataTable.getDeleted() != null && exitsDataTable
						.getDeleted() == true)
				||(exitsDataTable.getIsDict() != null && exitsDataTable
						.getIsDict() == true)) {
			exitsDataTable = null;
		}
		List<DataTable> existtableList=new ArrayList<DataTable>();
		existtableList.add(exitsDataTable);
		Map<String, DataField> fmap = new HashMap<String, DataField>();
		for (DataField tf : exitsDataTable.getDataFields()) {
			// 过滤掉已删除的字段记录，保存未被删除的记录
			if (tf.getDeleted() == null
					|| (tf.getDeleted() != null && tf.getDeleted() != true)) {
				fmap.put(tf.getName(), tf);
			}
		}
		localModelMap.put(exitsDataTable.getName(), fmap);
		Map<String, Map<String, DataField>> remoteDBMap = new HashMap<String, Map<String, DataField>>();
		Map<String, DataField> ftmap = null;
		// 找到数据源同步过来的表，字段
		DataSchema dataSchema =exitsDataTable.getDataSchema();
		String dbName = dataSchema.getName();
		DataTable removeDataTable = adapter.getTable(dbName,
				exitsDataTable.getName(), conn);
		// get all fields of this table
		List<DataField> dataFieldList = adapter.getField(dbName,
				exitsDataTable.getName(), conn);
		List<DataTable> guolvTableList=new ArrayList<DataTable>();

		// 查出来的过滤删除的表，和数据字典的表,数据字典不同步
		if ((removeDataTable.getDeleted() != null && removeDataTable.getDeleted() == true)
				|| (removeDataTable.getIsDict() != null && removeDataTable.getIsDict() == true)) {
			removeDataTable=null;
		}else{
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
		 result=compareTwoMap(dataSchema, result, localModelMap, dbName,
				existtableList, remoteDBMap, guolvTableList);
		return result;
	}
	
	@Override
	public void delOneDatasource(int data_source_id) throws SQLException {
		repository.deleteById(data_source_id);
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
			SycData sycData=getAdapterAndConnection(dbType, driver, url, port, host, username, pwd, sid);
			adapter=sycData.getAdapter();
			conn=sycData.getConn();
			if (conn != null) {
				return true;
			} else {
				return false;
			}


		} catch (Exception e) {
			System.out.println("e=" + e.getLocalizedMessage());
			return false;
		}

	}
}