package com.jettech.service.Impl;

import com.jettech.EnumDatabaseType;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.MySqlAdapter;
import com.jettech.db.adapter.OracleAdapter;
import com.jettech.domain.DbModel;
import com.jettech.entity.*;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.QualityTestQueryRepository;
import com.jettech.repostory.QualityTestResultItemRepository;
import com.jettech.repostory.QualityTestResultRepository;
import com.jettech.service.IQualityTestQueryService;
/*import com.jettech.service.ITestRoundService;*/
import com.jettech.service.TestRoundService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.*;

@Service
public class QualityTestQueryServiceImpl implements IQualityTestQueryService {
	@Autowired
    QualityTestQueryRepository qualityTestQueryRepository;

	@Autowired
    DataSourceRepository dataSourceRepository;

	@Autowired
	TestRoundService testRoundService;

	@Autowired
    QualityTestResultRepository qualityTestResultRepository;

	@Autowired
	QualityTestResultItemRepository qualityTestResultItemRepository;

	@Override
	public List<QualityTestQuery> findAll() {
		return qualityTestQueryRepository.findAll();
	}

	@Override
	public List<QualityTestQuery> saveAll(List<QualityTestQuery> list) {
		List<QualityTestQuery> testQueries = qualityTestQueryRepository.saveAll(list);
		return testQueries;
	}

	@Override
	public void save(QualityTestQuery entity) {
		qualityTestQueryRepository.save(entity);

	}

	@Override
	public void delete(Integer id) {
		qualityTestQueryRepository.deleteById(id);
	}

	@Override
	public QualityTestQuery findById(Integer id) {
		return qualityTestQueryRepository.getOne(id);
	}

	@Override
	public Page<QualityTestQuery> findAllByPage(Pageable pageable) {
		return qualityTestQueryRepository.findAll(pageable);
	}

	@Override
	public QualityTestQuery getOneById(Integer id) {
		QualityTestQuery qualityTestQuery = qualityTestQueryRepository.findById(id).get();
		return qualityTestQuery;
	}

	@Override
	public ResultVO findSourceByQueryId(Integer testQueryId) {
		QualityTestQuery qualityTestQuery = qualityTestQueryRepository.getOne(testQueryId);
		AbstractAdapter adapter = null;
		DataSource ds = new DataSource();
		if(qualityTestQuery.getDataSource()!=null){
			ds = dataSourceRepository.getOne(qualityTestQuery.getDataSource().getId());
		}
		String sql = qualityTestQuery.getSqlText();
		String schema = ds.getDefaultSchema();
		String username = ds.getUserName();
		String pwd = ds.getPassword();
		String name = ds.getName();
		String dbType = ds.getDatabaseType().name();
		String host = ds.getHost();
		String port = ds.getPort();
		String url = ds.getUrl();
		String driver = ds.getDriver();
		System.out.println("schema=" + schema);
		System.out.println("dbType=" + dbType);
		Connection conn = null;
		try {
			DbModel db = new DbModel();
			if (dbType.toLowerCase().equals("mysql")) {
				if (driver != null && !driver.equals("")) {
					db.setDriver(driver);
				} else {
					db.setDriver("com.mysql.cj.jdbc.Driver");
				}
				if (url != null && !url.equals("")) {
					db.setUrl(url);
				} else {
					// jdbc:mysql://localhost/dm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC&useUnicode=true&characterEncoding=UTF-8
					db.setUrl("jdbc:mysql://" + host
							+ "/dm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC");
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("mysql");
				db.setDbtype(EnumDatabaseType.Oracle);
				adapter = new MySqlAdapter();
				conn = ((MySqlAdapter) adapter).getConnection(db);
			} else if (dbType.toLowerCase().equals("oracle")) {
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
					db.setUrl("jdbc:oracle:thin:@//" + host + ":" + port + "/xe");
				}
				db.setUsername(username);
				db.setPassword(pwd);
				db.setName("oracle");
				db.setDbtype(EnumDatabaseType.Oracle);
				adapter = new OracleAdapter();
				conn = ((OracleAdapter) adapter).getConnection(db);
			}
			//======= 上面为创建连接   20190129 ==========================================================================
			Long start = System.currentTimeMillis();
			List<DataSchema> databaselist = new ArrayList<DataSchema>();
			if(conn != null && !sql.equals("")){
				ResultSet set = adapter.query(conn, sql);
				ResultSetMetaData md = set.getMetaData(); //得到结果集(rs)的结构信息，比如字段数、字段名等
				int countAll = set.getRow();
				int columnCount = md.getColumnCount(); //返回此 ResultSet 对象中的列数
				List list = new ArrayList();
				Map rowData = new HashMap();
				List<QualityTestResultItem> qualityTestResultItems = new ArrayList<QualityTestResultItem>();
				while (set.next()) {
				//	Map rowDataOne = new HashMap();
					QualityTestResultItem qualityTestResultItem = new QualityTestResultItem();
					StringBuilder columnName = new StringBuilder();
					StringBuilder soruceValue = new StringBuilder();
					rowData = new HashMap(columnCount);
					for (int i = 1; i <= columnCount; i++) {
						rowData.put(md.getColumnLabel(i), set.getObject(i));
						columnName.append(md.getColumnLabel(i));
						soruceValue.append(set.getObject(i));
						if(i != columnCount){
							columnName.append("&");
							soruceValue.append("&");
						}
					}
					list.add(rowData);
//					testResultItem.setColumnName(columnName.toString());
//					testResultItem.setSoruceValue(soruceValue.toString());
					qualityTestResultItem.setResult(rowData.toString());
					qualityTestResultItems.add(qualityTestResultItem);
				}
				Long end = System.currentTimeMillis();
				Date startTime = new Date(start);
				Date endTime = new Date(end);
				QualityTestResult qualityTestResult = new QualityTestResult();
				QualityTestCase qualityTestCase = qualityTestQuery.getQualityTestCase();
				TestSuite testSuite = new TestSuite();
				TestRound testRound = new TestRound();
				if(qualityTestCase != null){
//					testSuite = qualityTestCase.getTestSuite();
					testRound = testRoundService.selectTestRoundByTestSuiteId(testSuite.getId());
				}
				if(qualityTestCase != null){
					qualityTestResult.setTestCaseName(qualityTestCase.getName());
				}
				qualityTestResult.setStartTime(startTime);
				qualityTestResult.setEndTime(endTime);
				qualityTestResult.setDataCount(countAll);
				if(qualityTestCase != null){
					qualityTestResult.setTestCaseId(qualityTestCase.getId());
				}
				qualityTestResult.setResult(true);
				qualityTestResult.setDataSource(ds.getName());
				qualityTestResult.setSqlText(qualityTestQuery.getSqlText());
//				testResult.setKeyText(list.toString());
				//保存主表信息
				QualityTestResult qualityTestResultid = qualityTestResultRepository.save(qualityTestResult);
				//保存子表信息testResultTestRepository   testResultItems.
				for (QualityTestResultItem s : qualityTestResultItems){
					s.setTestResultId(qualityTestResultid.getId());
				}
				List<QualityTestResultItem> saveQualityTestResultItem = qualityTestResultItemRepository.saveAll(qualityTestResultItems);
				Map<String,Object> rs = new HashMap<String,Object>();
				rs.put("testResult", qualityTestResultid);
				rs.put("testResultItem", saveQualityTestResultItem);
				return new ResultVO(true, StatusCode.OK, "查询成功", rs);
			}
			return new ResultVO(true, StatusCode.OK, "查询成功", "查询条件为空");

		}catch (Exception e){
			e.getLocalizedMessage();
		}finally {
			try {
				conn.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		}
		return new ResultVO(true, StatusCode.ERROR, "查询失败", "查询失败");
	}





}
