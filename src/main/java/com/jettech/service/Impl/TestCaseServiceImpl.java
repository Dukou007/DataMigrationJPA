package com.jettech.service.Impl;

import java.io.IOException;
import java.io.OutputStream;
import java.sql.Connection;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.BizException;
import com.jettech.EnumCompareDirection;
import com.jettech.EnumDatabaseType;
import com.jettech.EnumExecuteStatus;
import com.jettech.EnumPageType;
import com.jettech.EnumTestCaseType;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.db.adapter.DB2Adapter;
import com.jettech.db.adapter.InformixAdapter;
import com.jettech.db.adapter.MySqlAdapter;
import com.jettech.db.adapter.OracleAdapter;
import com.jettech.db.adapter.SyBaseAdapter;
import com.jettech.domain.CaseModel;
import com.jettech.domain.CompareCaseModel;
import com.jettech.domain.CompareaToFileCaseModel;
import com.jettech.domain.DbModel;
import com.jettech.entity.CodeMap;
import com.jettech.entity.DataField;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.entity.Product;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.QualityTestQuery;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.entity.TestRule;
import com.jettech.entity.TestSuite;
import com.jettech.entity.TestSuiteCase;
import com.jettech.repostory.CodeMapRepository;
import com.jettech.repostory.CompareTestCaseRepository;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.ProductRepository;
import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.QualityTestQueryRepository;
import com.jettech.repostory.TestCaseRepository;
import com.jettech.repostory.TestPointRepository;
import com.jettech.repostory.TestQueryFieldRepository;
import com.jettech.repostory.TestQueryRepository;
import com.jettech.repostory.TestResultItemRepository;
import com.jettech.repostory.TestResultRepository;
import com.jettech.repostory.TestRuleRepository;
import com.jettech.repostory.TestSuiteCaseRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.ITestCaseService;
import com.jettech.service.ITestReusltService;
import com.jettech.thread.JobWorker;
import com.jettech.util.DateUtil;
import com.jettech.util.ExcelUtil;
import com.jettech.util.FileUtil;
import com.jettech.vo.QualityTestCaseVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.SycData;
import com.jettech.vo.TestCaseVO;
import com.jettech.vo.TestFieldVO;
import com.jettech.vo.TestQueryVO;
import com.jettech.vo.TestRuleVO;

@Service
public class TestCaseServiceImpl implements ITestCaseService {
	private static final String TARGET_KEY = "targetKey";

	private static final String TARGET_SQL = "targetSQL";

	private static final String TARGET_DB = "targetDB";

	private static final String SOURCE_KEY = "sourceKey";

	private static final String SOURCE_SQL = "sourceSQL";

	private static final String SOURCE_DB = "sourceDB";

	private static final String TEST_CASE_NAME = "testCaseName";

	Logger logger = LoggerFactory.getLogger(TestCaseServiceImpl.class);

	@Autowired
	TestCaseRepository caseRepository;
	@Autowired
	TestSuiteRepository testSuiteRepository;
	@Autowired
	ProductRepository productRepository;
	@Autowired
	DataSourceRepository dataSourceRepository;
	@Autowired
	TestQueryRepository testQueryRepository;
	@Autowired
	CodeMapRepository codeMapRepository;
	@Autowired
	TestRuleRepository testRuleRepository;
	@Autowired
	CompareTestCaseRepository compareTestCaseRepository;
	@Autowired
	QualityTestCaseRepository qualityTestCaseRepository;
	@Autowired
	QualityTestQueryRepository qualityTestQueryRepository;
	@Autowired
	TestPointRepository testPointRepository;
	@Autowired
	DataFieldRepository testFieldRepository;
	@Autowired
	TestQueryFieldRepository testQueryFieldRepository;
	@Autowired
	TestSuiteCaseRepository testSuiteCaseRepository;
	@Autowired
	private TestResultRepository testResultRepository;
	@Autowired
	private TestCaseRepository testCaseRepository;
	@Autowired
	private ITestReusltService testRusltService;
	@Autowired
	private DataSourceRepository repository;
	@Autowired
	private TestResultItemRepository testResultItemRepository;

	@Override
	public List<TestCase> findAll() {
		return caseRepository.findAll();
	}

	@Override
	public List<TestCase> saveAll(List<TestCase> list) {
		return caseRepository.saveAll(list);
	}

	@Override
	public void save(TestCase entity) {
		caseRepository.save(entity);
	}

	@Override
	@Transactional
	public void delete(Integer id) {
		Date start = new Date();
		TestCase testCase = this.findById(id);
		String name = testCase.getName();
		int sourceQueryId = testCase.getSourceQuery().getId();
		int targetQueryId = testCase.getTargetQuery().getId();
		testCase.setSourceQuery(null);
		testCase.setTargetQuery(null);
		caseRepository.delete(testCase);
		// 查询test_result删除结果及结果明细
		List<Integer> resultIds = testResultRepository.findByCaseId(id);
		for (Integer resultId : resultIds) {
			// 查询明细表并删除
			List<TestResultItem> items = testResultItemRepository
					.findByTestResultID(resultId.toString());
			if (items.size() != 0) {
				testResultItemRepository.deleteByResultId(resultId);
			}
			testResultRepository.deleteById(resultId);
		}
		if (sourceQueryId != 0)
			testQueryRepository.deleteById(sourceQueryId);
		if (targetQueryId != 0)
			testQueryRepository.deleteById(targetQueryId);

		logger.info("删除案例成功：" + id + "_" + name + " 耗时:"
				+ DateUtil.getEclapsedTimesStr(start.getTime()));
	}

	@Override
	public TestCase findById(Integer id) {
		Optional<TestCase> optional = caseRepository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	// @Transactional
	public String doTest(Integer testCaseId) {
		TestCase testCase = caseRepository.getOne(testCaseId);
		// CompareTestCase testCase=
		// compareTestCaseRepository.getOne(testCaseId);

		if (testCase == null)
			return "not found testCase id:" + testCaseId;
		logger.info("ready doTestCase:" + testCase.getId() + "_"
				+ testCase.getName());
		CaseModel caseModel = null;
		try {
			if (testCase.getTargetQuery() != null) {
				testCase.setCaseType(EnumTestCaseType.DataCompare);
				caseModel = new CompareCaseModel(testCase);
			} else {
				testCase.setCaseType(EnumTestCaseType.DataCompareToFile);
				caseModel = new CompareaToFileCaseModel(testCase);
			}

		} catch (Exception e) {
			String info = "cast testCase to model error." + testCase.getId()
					+ "_" + testCase.getName();
			logger.error(info, e);
			return info + e.getLocalizedMessage();
		}
		JobWorker job = new JobWorker(caseModel);
		Thread thread = new Thread(job);
		thread.setName("Case:" + caseModel.getName());
		thread.start();
		return null;
	}

	@Override
	public ResultVO readSQLCase(Map<String, String> map) {
		ResultVO result = new ResultVO();
		result.setFlag(true);

		if (map == null || map.size() == 0)
			throw new IllegalArgumentException(
					"readSQLCase argument is null or empty.");
		if (!map.containsKey("filePath")) {
			result.setFlag(false);
			result.setMessage("not input argument:filePath");
		}
		String filePath = map.get("filePath"); // 文件路径
		if (!map.containsKey("testSuiteId")) {
			result.setFlag(false);
			result.setMessage("not input argument:testSuiteId");
		}
		String testSuiteId = map.get("testSuiteId"); // 案例Id
		Integer id = null;
		try {
			id = Integer.valueOf(testSuiteId);
		} catch (NumberFormatException e) {
			result.setFlag(false);
			result.setMessage("testSuiteId NumberFormat error." + testSuiteId);
		}
		TestSuite testSuite = testSuiteRepository.findById(id).get();
		List<String> list = FileUtil.readFileToList(filePath, "UTF-8");
		result = createCaseWithQueue(list, testSuite);

		return result;

	}

	private ResultVO createCaseWithQueue(List<String> list, TestSuite testSuite) {
		ResultVO result = new ResultVO();

		Queue<String> queue = new ArrayBlockingQueue<String>(list.size());
		for (String line : list) {
			if (line != null && !line.trim().isEmpty())
				queue.add(line);
		}

		String testCaseName = null;
		String sourceDB = null;
		String sourceSQL = null;
		String sourceKey = null;
		String targetDB = null;
		String targetSQL = null;
		String targetKey = null;
		String info = null;
		int successCount = 0;
		int updateCaseCount = 0;

		while (true) {
			if (queue.isEmpty())
				break;
			String line = queue.poll();
			if (line == null || line.trim().isEmpty())
				continue;
			logger.debug("读取到:[" + line + "]");

			if (containTag(line, TEST_CASE_NAME)) {
				testCaseName = getTagValue(line, TEST_CASE_NAME);
				List<TestCase> findAllByNameLike = caseRepository
						.findAllByNameLike(testCaseName);
				if (findAllByNameLike.size() > 0) {
					info = testCaseName + "案例名称重复 ";
					logger.info(info);
					result.setFlag(false);
					result.setMessage(info);
					break;
				}
				logger.info("######## 开始导入案例:" + testCaseName);
			}
			if (testCaseName == null || testCaseName.trim().isEmpty())
				continue;

			if (containTag(line, SOURCE_DB)) {
				sourceDB = getTagValue(line, SOURCE_DB);
				logger.info("源查询数据源:" + sourceDB);
			}
			if (sourceDB == null || sourceDB.trim().isEmpty())
				continue;

			if (containTag(line, SOURCE_SQL)) {
				sourceSQL = getTagValue(line, SOURCE_SQL);
				// 获取SQL语句的内容(多行的SQL语句)
				while (true) {
					line = queue.peek();
					if (line != null && !line.trim().isEmpty()) {
						if (!containTag(line)) {
							sourceSQL = sourceSQL + " " + line;
							queue.remove();
						} else {
							break;
						}
					} else {
						break;
					}
				}
			}
			if (sourceSQL == null || sourceSQL.trim().isEmpty()) {
				continue;
			}

			if (containTag(line, SOURCE_KEY)) {
				sourceKey = getTagValue(line, SOURCE_KEY);
				logger.info("源查询Key:" + sourceKey);
			}
			if (sourceKey == null || sourceKey.trim().isEmpty()) {
				continue;
			}

			if (containTag(line, TARGET_DB)) {
				targetDB = getTagValue(line, TARGET_DB);
				logger.info("目标查询数据源:" + targetDB);
			}
			if (targetDB == null || targetDB.trim().isEmpty())
				continue;

			if (containTag(line, TARGET_SQL)) {
				targetSQL = getTagValue(line, TARGET_SQL);
				// 贪婪获取SQL语句的内容(多行的SQL语句)
				while (true) {
					line = queue.peek();
					if (line != null && !line.trim().isEmpty()) {
						if (!containTag(line)) {
							targetSQL = targetSQL + " " + line;
							queue.remove();
						} else {
							break;
						}
					} else {
						break;
					}
				}
			}
			if (targetSQL == null || targetSQL.trim().isEmpty())
				continue;

			if (containTag(line, TARGET_KEY)) {
				targetKey = getTagValue(line, TARGET_KEY);
				logger.info("目标查询Key:" + targetKey);
			}
			if (targetKey == null || targetKey.trim().isEmpty())
				continue;

			DataSource dsSource = dataSourceRepository.findByName(sourceDB);
			if (dsSource == null) {
				info = testCaseName + " can not found DataSource name is  "
						+ sourceDB;
				logger.info(info);
				result.setFlag(false);
				result.setMessage(info);
				break;
			}
			DataSource dsTarget = dataSourceRepository.findByName(targetDB);
			if (dsTarget == null) {
				info = testCaseName + " can not found DataSource name is "
						+ targetDB;
				logger.info(info);
				result.setFlag(false);
				result.setMessage(info);
				break;
			}

			TestCase testCase = null;
			List<TestCase> caseList = this.caseRepository.findByNameAndSuite(
					testCaseName, testSuite.getName());
			if (caseList != null && caseList.size() > 0) {
				logger.info("测试集 [" + testSuite.getName() + "] 的案例 ["
						+ testCaseName + "] 将被更新");
				updateCaseCount = updateCaseCount + 1;
				testCase = caseList.get(0);
			} else {
				logger.info("测试集 [" + testSuite.getName() + "] 新建案例 ["
						+ testCaseName + "]");
				testCase = new TestCase();
			}
			testCase.setName(testCaseName);
			// testCase.setTestSuite(testSuite);
			testCase.setUsePage(false);
			testCase.setIsSQLCase(true);

			TestQuery sourceQuery = null;
			if (testCase.getId() != null && testCase.getSourceQuery() != null) {
				sourceQuery = testCase.getSourceQuery();
			} else {
				sourceQuery = new TestQuery();
			}
			sourceQuery.setDataSource(dsSource);
			sourceQuery.setSqlText(sourceSQL);
			sourceQuery.setKeyText(sourceKey);
			// sourceQuery.setTestCase(testCase);
			testCase.setSourceQuery(sourceQuery);

			TestQuery targetQuery = null;
			if (testCase.getId() != null && testCase.getTargetQuery() != null) {
				targetQuery = testCase.getTargetQuery();
			} else {
				targetQuery = new TestQuery();
			}
			targetQuery.setDataSource(dsTarget);
			targetQuery.setSqlText(targetSQL);
			targetQuery.setKeyText(targetKey);
			// targetQuery.setTestCase(testCase);
			testCase.setTargetQuery(targetQuery);
			testCase.setEnumCompareDirection(EnumCompareDirection.LeftToRight);
			testQueryRepository.save(sourceQuery);
			testQueryRepository.save(targetQuery);
			if (testCase.getCaseType() == null) {
				testCase.setCaseType(EnumTestCaseType.DataCompare);
			}
			caseRepository.save(testCase);

			successCount++;
			logger.info("######## 保存第[" + successCount + "] 案例成功:["
					+ testCase.getName() + "]");

			testCaseName = null;
			sourceDB = null;
			sourceSQL = null;
			sourceKey = null;
			targetDB = null;
			targetSQL = null;
			targetKey = null;

		}

		info = "导入案例数量:" + successCount + " 其中更新数量:" + updateCaseCount;
		logger.info(info);
		result.setMessage(result.getMessage() + " " + info);

		return result;
	}

	@SuppressWarnings("unused")
	private ResultVO createCase(List<String> list, TestSuite testSuite) {
		ResultVO result = new ResultVO();

		String testCaseName = null;
		String sourceDB = null;
		String sourceSQL = null;
		String sourceKey = null;
		String targetDB = null;
		String targetSQL = null;
		String targetKey = null;
		String info = null;
		int successCount = 0;

		// String caseText = content;
		// while (content.contains("testCaseName")) {
		// caseText = caseText.substring(caseText.indexOf("testCaseName"));
		// }
		for (int i = 0; i < list.size(); i++) {

			String line = list.get(i);

			if (line == null || line.trim().isEmpty())
				continue;

			if (testCaseName == null)
				testCaseName = getTagValue(i, line, TEST_CASE_NAME);
			if (testCaseName == null || testCaseName.trim().isEmpty())
				continue;

			if (sourceDB == null)
				sourceDB = getTagValue(i, line, SOURCE_DB);
			if (sourceDB == null || sourceDB.trim().isEmpty())
				continue;

			if (sourceSQL == null)
				sourceSQL = getTagValue(i, line, SOURCE_SQL);
			if (sourceSQL == null || sourceSQL.trim().isEmpty())
				continue;

			if (sourceKey == null)
				sourceKey = getTagValue(i, line, SOURCE_KEY);
			if (sourceKey == null || sourceKey.trim().isEmpty()) {
				continue;
			}

			if (targetDB == null)
				targetDB = getTagValue(i, line, TARGET_DB);
			if (targetDB == null || targetDB.trim().isEmpty())
				continue;

			if (targetSQL == null)
				targetSQL = getTagValue(i, line, TARGET_SQL);
			if (targetSQL == null || targetSQL.trim().isEmpty())
				continue;

			if (targetKey == null)
				targetKey = getTagValue(i, line, TARGET_KEY);
			if (targetKey == null || targetKey.trim().isEmpty())
				continue;

			DataSource dsSource = dataSourceRepository.findByName(sourceDB);
			if (dsSource == null) {
				info = "can not found DataSource name is  " + sourceDB;
				logger.info(info);
				result.setMessage(info);
				break;
			}
			DataSource dsTarget = dataSourceRepository.findByName(targetDB);
			if (dsTarget == null) {
				info = "can not found DataSource name is " + targetDB;
				logger.info(info);
				result.setMessage(info);
				break;
			}

			TestCase testCase = null;
			List<TestCase> caseList = this.caseRepository.findByNameAndSuite(
					testCaseName, testSuite.getName());
			if (caseList != null && caseList.size() > 0) {
				testCase = caseList.get(0);
			} else {
				testCase = new TestCase();
			}
			testCase.setName(testCaseName);
			// testCase.setTestSuite(testSuite);
			testCase.setUsePage(false);
			testCase.setIsSQLCase(true);

			TestQuery sourceQuery = new TestQuery();
			sourceQuery.setDataSource(dsSource);
			sourceQuery.setSqlText(sourceSQL);
			sourceQuery.setKeyText(sourceKey);
			testCase.setSourceQuery(sourceQuery);

			TestQuery targetQuery = new TestQuery();
			targetQuery.setDataSource(dsTarget);
			targetQuery.setSqlText(targetSQL);
			targetQuery.setKeyText(targetKey);
			testCase.setTargetQuery(targetQuery);

			caseRepository.save(testCase);
			testQueryRepository.save(sourceQuery);
			testQueryRepository.save(targetQuery);
			successCount++;
			logger.info("保存第[" + successCount + "] 案例成功:[" + testCase.getName()
					+ "]");

			testCaseName = null;
			sourceDB = null;
			sourceSQL = null;
			sourceKey = null;
			targetDB = null;
			targetSQL = null;
			targetKey = null;

		}
		info = "导入案例数量:" + successCount;
		logger.info(info);
		result.setMessage(result.getMessage() + " " + info);

		return result;
	}

	private TestSuite getTestSuite(String testSuiteName, String productName) {
		Product product = null;
		List<Product> productList = productRepository.findByName(productName);
		if (productList != null && productList.size() > 0) {
			product = productList.get(0);
		} else {
			product = new Product();
			product.setName(productName);
			product = productRepository.save(product);
		}
		TestSuite testSuite = null;
		List<TestSuite> testSuiteList = testSuiteRepository.findByName(
				testSuiteName, productName);
		if (testSuiteList != null && testSuiteList.size() > 0) {
			testSuite = testSuiteList.get(0);
		} else {
			testSuite = new TestSuite();
			testSuite.setProduct(product);
			testSuite.setName(testSuiteName);
			testSuite = testSuiteRepository.save(testSuite);
		}
		return testSuite;
	}

	private boolean containTag(String line, String tagName) {
		return line.toUpperCase().startsWith(tagName.toUpperCase());
	}

	private boolean containTag(String line) {
		return containTag(line, TEST_CASE_NAME) || containTag(line, SOURCE_DB)
				|| containTag(line, SOURCE_SQL) || containTag(line, SOURCE_KEY)
				|| containTag(line, TARGET_DB) || containTag(line, TARGET_SQL)
				|| containTag(line, TARGET_KEY);
	}

	private String getTagValue(String line, String tagName) {
		try {
			if (line.toUpperCase().startsWith(tagName.toUpperCase())) {
				String tagValue = null;
				String[] arr = null;
				if (line.contains("@@@")) {
					arr = line.split("@@@");
				} else if (line.contains("@@@")) {
					arr = line.split("@@@");
				} else {
					logger.error(tagName + " error style.");
				}

				tagValue = arr[1].trim();
				if (tagValue.isEmpty()) {
					logger.error(tagName + " is empty.line");
				}
				return tagValue;
			} else
				return null;
		} catch (Exception e) {
			logger.warn("导入案例出现错误,lineContent:[" + line + "],tagname:["
					+ tagName + "]," + e.getLocalizedMessage());
			return null;
		}
	}

	private String getTagValue(int i, String line, String tagName) {
		try {
			if (line.toUpperCase().startsWith(tagName.toUpperCase())) {
				String tagValue = null;
				String[] arr = null;
				if (line.contains(":")) {
					arr = line.split(":");
				} else if (line.contains("：")) {
					arr = line.split("：");
				} else {
					logger.error(tagName + " error style.line：" + i + " ["
							+ line + "]");
				}

				tagValue = arr[1].trim();
				if (tagValue.isEmpty()) {
					logger.error(tagName + " is empty.line：" + i + " [" + line
							+ "]");
				}
				return tagValue;
			} else
				return null;
		} catch (Exception e) {
			logger.warn("导入案例出现错误,lineNum:[" + i + "],lineContent:[" + line
					+ "],tagname:[" + tagName + "]," + e.getLocalizedMessage());
		}
		return null;
	}

	// @SuppressWarnings("unused")
	// private String getTagValue(Queue<String> queue, String tagName) {
	// while (true) {
	// // String line = list.get(i);
	// String line = queue.poll();
	// if (line == null || line.trim().isEmpty()) {
	// // i++;
	// continue;
	// }
	// String tagValue = getTagValue(line, tagName);
	// }
	// }

	@Override
	public TestCase findByNameAndSuite(String testCaseName, String testSuiteName) {
		List<TestCase> list = caseRepository.findByNameAndSuite(testCaseName,
				testSuiteName);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}

	public List<TestCase> findByName(String name) {
		List<TestCase> list = caseRepository.findByName(name);
		if (list.size() > 0) {
			return list;
		} else {
			return null;
		}

	}

	/*
	 * @Override public Page<TestCase> findBySuiteId(Integer testSuiteID, String
	 * name, Pageable pageable) { Page<TestCase> list =
	 * caseRepository.findByTestSuiteIdAndNameContaining(testSuiteID, "%" + name
	 * + "%", pageable); if (list.getSize() > 0) { return list; } else { return
	 * new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0); }
	 * 
	 * }
	 */

	/*
	 * @Override public Page<TestCase> findBySuiteId(Integer testSuiteID,
	 * Pageable pageable) { Page<TestCase> list; if (testSuiteID != null &&
	 * testSuiteID > 0) { list = caseRepository.findByTestSuiteId(testSuiteID,
	 * pageable); } else { list =
	 * caseRepository.findByTestSuiteIsNull(pageable); } if (list.getSize() > 0)
	 * { return list; } else { return new PageImpl<TestCase>(new
	 * ArrayList<TestCase>(), pageable, 0); }
	 * 
	 * }
	 */

	/**
	 * 根据测试集名称查询所有案例 20190121
	 * 
	 * @param suiteName
	 * @return
	 */
	@Override
	public Page<TestCase> findBySuiteName(String suiteName, Pageable pageable) {
		// 新的方法 20190123
		return caseRepository.findBysuiteName("%" + suiteName + "%", pageable);
	}

	@Override
	public Page<TestCase> findAllByPage(Pageable pageable) {
		return caseRepository.findAll(pageable);
	}

	@Override
	public StringBuffer exportTestCase(Integer testCaseId) {

		return null;
	}

	@Override
	public TestCaseVO getTestCaseDetail(Integer testCaseID) {
		// 查询案例详情
		TestCase testCase = caseRepository.findById(testCaseID).get();
		TestCaseVO testCaseVO = new TestCaseVO();
		BeanUtils.copyProperties(testCase, testCaseVO);
		// 查询源查询
		TestQuery sourceQuery = testCase.getSourceQuery();
		TestQueryVO sourceQueryVO = new TestQueryVO();
		BeanUtils.copyProperties(sourceQuery, sourceQueryVO);
		List<TestRule> testRules = sourceQuery.getTestRules();
		List<TestRuleVO> testRulesVO = new ArrayList<>();
		for (TestRule testRule : testRules) {
			TestRuleVO t = new TestRuleVO();
			BeanUtils.copyProperties(testRule, t);
			t.setCodeMapId(testRule.getCodeMap().getId());
			testRulesVO.add(t);
		}
		sourceQueryVO.setDataSourceId(sourceQuery.getDataSource().getId());
		sourceQueryVO.setTestRules(testRulesVO);
		testCaseVO.setSourceQuery(sourceQueryVO);
		// 查询目标查询
		TestQuery targetQuery = testCase.getTargetQuery();
		TestQueryVO targetQueryVO = new TestQueryVO();
		BeanUtils.copyProperties(targetQuery, targetQueryVO);
		List<TestRule> tarTestRules = targetQuery.getTestRules();
		List<TestRuleVO> tarTestRulesVO = new ArrayList<>();
		for (TestRule testRule : tarTestRules) {
			TestRuleVO t = new TestRuleVO();
			BeanUtils.copyProperties(testRule, t);
			t.setCodeMapId(testRule.getCodeMap().getId());
			tarTestRulesVO.add(t);
		}
		targetQueryVO.setDataSourceId(targetQuery.getDataSource().getId());
		targetQueryVO.setTestRules(tarTestRulesVO);
		testCaseVO.setTargetQuery(targetQueryVO);
		return testCaseVO;
	}

	@Override
	public List<TestCase> findAllTestCase(Integer testSuiteID) {
		return caseRepository.findAllTestCase(testSuiteID);
	}

	@Override
	public Page<TestCase> getAllTestCaseByPage(String name,
			EnumCompareDirection enumCompareDirection, Integer suiteId,
			Pageable pageable) {
		Page<TestCase> pageList = null;
		if (name.equals("") && suiteId == null) {
			if (enumCompareDirection != null
					&& !"".equals(enumCompareDirection)) {
				pageList = caseRepository
						.findAllByEnumCompareDirectionOrderByIdDesc(
								enumCompareDirection, pageable);
			} else {
				pageList = caseRepository.findAllOrderByIdDesc(pageable);
			}

		} else if ((!name.equals("")) && suiteId == null) {
			if (enumCompareDirection != null) {
				pageList = caseRepository
						.findByNameLikeAndEnumCompareDirectionLikeOrderByIdDesc(
								"%" + name + "%", enumCompareDirection,
								pageable);
			} else if (enumCompareDirection == null) {
				pageList = caseRepository.findByNameLikeOrderByIdDesc("%"
						+ name + "%", pageable);
			}
		} else if ((!name.equals("")) && suiteId != null) {
			if (enumCompareDirection != null) {
				pageList = caseRepository.getAllTestCByPage(name,
						enumCompareDirection, suiteId, pageable);
			} else if (enumCompareDirection == null) {
				pageList = caseRepository.getAllTCByPage(name, suiteId,
						pageable);
			}

		} else if (name.equals("") && suiteId != null) {
			if (enumCompareDirection != null) {
				pageList = caseRepository.getAllTByPage(enumCompareDirection,
						suiteId, pageable);
			} else if (enumCompareDirection == null) {
				pageList = caseRepository.findByTestSuiteId(suiteId, pageable);
			}
		}

		if (pageList.getSize() > 0) {
			return pageList;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable,
					0);
		}
	}

	public Page<TestCase> getAllTestCByPage(String name,
			EnumCompareDirection enumCompareDirection,
			EnumExecuteStatus caseStatus, Integer suiteId, Pageable pageable) {
		Page<TestCase> pageList = null;
		if ((!name.equals("")) && suiteId != null) {
			if (enumCompareDirection != null) {
				if (caseStatus.equals(EnumExecuteStatus.Init)) {
					pageList = caseRepository.getAllNESRI(name,
							enumCompareDirection, suiteId, pageable);
				} else {
					pageList = caseRepository
							.getAllNESR(name, enumCompareDirection, suiteId,
									caseStatus, pageable);
				}
			} else if (enumCompareDirection == null) {
				if (caseStatus.equals(EnumExecuteStatus.Init)) {
					pageList = caseRepository.getAllNDRI(name, suiteId,
							pageable);
				} else {
					pageList = caseRepository.getAllNDR(name, suiteId,
							caseStatus, pageable);
				}

			}

		} else if (name.equals("") && suiteId != null) {
			if (enumCompareDirection != null) {
				if (caseStatus.equals(EnumExecuteStatus.Init)) {
					pageList = caseRepository.getAllEDRI(enumCompareDirection,
							suiteId, pageable);
				} else {
					pageList = caseRepository.getAllEDR(enumCompareDirection,
							suiteId, caseStatus, pageable);
				}

			} else if (enumCompareDirection == null) {
				if (caseStatus.equals(EnumExecuteStatus.Init)) {
					pageList = caseRepository.getAllDRI(suiteId, pageable);
				} else {
					pageList = caseRepository.getAllDR(suiteId, caseStatus,
							pageable);
				}
			}
		} else if (name.equals("") && suiteId == null) {
			if (enumCompareDirection != null
					&& !"".equals(enumCompareDirection)) {
				if (caseStatus.equals(EnumExecuteStatus.Init)) {
					pageList = caseRepository.getAllERIU(enumCompareDirection,
							pageable);
				} else {
					pageList = caseRepository.getAllER(enumCompareDirection,
							caseStatus, pageable);
				}
			} else {
				if (caseStatus.equals(EnumExecuteStatus.Init)) {
					pageList = caseRepository.getAllRI(pageable);
				} else {
					pageList = caseRepository.getAllR(caseStatus, pageable);
				}
			}

		} else if ((!name.equals("")) && suiteId == null) {
			if (enumCompareDirection != null) {
				if (caseStatus.equals(EnumExecuteStatus.Init)) {
					pageList = caseRepository.getAllERI(name,
							enumCompareDirection, pageable);
				} else {
					pageList = caseRepository.getAllNER(name,
							enumCompareDirection, caseStatus, pageable);
				}
			} else if (enumCompareDirection == null) {
				if (caseStatus.equals(EnumExecuteStatus.Init)) {
					pageList = caseRepository.getAllNRI(name, pageable);
				} else {
					pageList = caseRepository.getAllNR(name, caseStatus,
							pageable);
				}
			}
		}
		if (pageList.getSize() > 0) {
			return pageList;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable,
					0);
		}
	}

	@Override
	@Transactional
	public ResultVO getAllTestCaseByPage(String name, String caseStatus,
			Integer pageNum, Integer pageSize, Integer testSuiteId,
			EnumCompareDirection enumCompareDirection) {
		Map<String, Object> resultmap = new HashMap<String, Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
		long beginTime = (new Date()).getTime();
		if (testSuiteId != null) {
			// 查询这个测试集是不是迁移的，如果不是迁移的给出提示
			Integer[] caseIds = testSuiteCaseRepository
					.findCaseIdsBysuiteId(testSuiteId);
			if (caseIds.length == 0) {
				return new ResultVO(false, StatusCode.ERROR, "查询失败",
						"这个测试集下没有案例");
			}
		}
		Page<TestCase> testCaseList = null;
		List<TestCaseVO> testCaseVOList = new ArrayList<TestCaseVO>();
		if (caseStatus.equals("")) {
			testCaseList = getAllTestCaseByPage(name, enumCompareDirection,
					testSuiteId, pageable);// findAllPage(pageNum,pageSize);
			for (TestCase testCase : testCaseList) {
				TestCaseVO testCaseVO = new TestCaseVO(testCase);
				// 取最新一条查案例结果
				TestResult testResult = testRusltService
						.findEndTimeByCaseId(testCase.getId());
				if (testResult != null) {
					if (testResult.getExecState().equals(
							EnumExecuteStatus.Executing)) {
						testCaseVO.setCaseStatus("执行中");
					} else if (testResult.getExecState().equals(
							EnumExecuteStatus.Finish)) {
						testCaseVO.setCaseStatus("执行完成");
					} else if (testResult.getExecState().equals(
							EnumExecuteStatus.Interrupt)) {
						testCaseVO.setCaseStatus("执行中断");
					} else if (testResult.getExecState().equals(
							EnumExecuteStatus.Ready)) {
						testCaseVO.setCaseStatus("准备中");
					}
					testCaseVO.setResult(testResult.getResult());
					testCaseVO.setEndTime(testResult.getStartTime());
				}
				if (testCaseVO.getCaseStatus() == null) {
					testCaseVO.setCaseStatus("初始状态");
				}
				testCaseVOList.add(testCaseVO);

			}
		} else {
			EnumExecuteStatus status = null;
			if (caseStatus.equals("Finish")) {
				status = EnumExecuteStatus.Finish;
			} else if (caseStatus.equals("Executing")) {
				status = EnumExecuteStatus.Executing;
			} else if (caseStatus.equals("Interrupt")) {
				status = EnumExecuteStatus.Interrupt;
			} else if (caseStatus.equals("Ready")) {
				status = EnumExecuteStatus.Ready;
			} else if (caseStatus.equals("Init")) {
				status = EnumExecuteStatus.Init;
			}
			testCaseList = getAllTestCByPage(name, enumCompareDirection,
					status, testSuiteId, pageable);
			for (TestCase testCase : testCaseList) {
				TestCaseVO testCaseVO = new TestCaseVO(testCase);
				TestResult testResult = testRusltService
						.findEndTimeByCaseId(testCase.getId());
				if (caseStatus.equals("Finish")) {
					caseStatus = "执行完成";
				} else if (caseStatus.equals("Executing")) {
					caseStatus = "执行中";
				} else if (caseStatus.equals("Interrupt")) {
					caseStatus = "执行中断";
				} else if (caseStatus.equals("Ready")) {
					caseStatus = "准备中";
				} else if (caseStatus.equals("Init")) {
					caseStatus = "初始状态";
				}
				testCaseVO.setCaseStatus(caseStatus);
				if (testResult != null
						&& testResult.getExecState().equals(status)) {
					testCaseVO.setEndTime(testResult.getStartTime());
					testCaseVO.setResult(testResult.getResult());
				}
				if (testCaseVO.getCaseStatus() == null) {
					testCaseVO.setCaseStatus("初始状态");
				}
				testCaseVOList.add(testCaseVO);
			}

		}

		resultmap.put("totalElements", testCaseList.getTotalElements());
		resultmap.put("totalPages", testCaseList.getTotalPages());
		resultmap.put("list", testCaseVOList);
		logger.info("getAllTestCaseByPage use:"
				+ DateUtil.getEclapsedTimesStr(beginTime) + " name:" + name
				+ " pageNum:" + pageNum + " pageSize:" + pageSize);
		return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	}

	@Override
	@Transactional
	public void changeTestCasePosition(Integer testSuiteId, String testCaseIdS)
			throws Exception {
		String[] ids = testCaseIdS.split(",");
		for (String testCaseId : ids) {
			Integer caseId = Integer.valueOf(testCaseId);
			TestSuiteCase tsc = testSuiteCaseRepository.findByCaseIdAndSuiteId(
					caseId, testSuiteId);
			if (tsc != null) {
				throw new Exception("案例已经存在此库中");
			} else {
				testSuiteCaseRepository.changeTestCasePosition(testSuiteId,
						caseId);
			}
		}
	}

	@Override
	@Transactional
	public void backDisorder(String testCaseIDS, Integer suiteId) {
		String[] ids = testCaseIDS.split(",");
		for (String testCaseID : ids) {
			Integer caseId = Integer.valueOf(testCaseID);
			TestSuiteCase tsc = testSuiteCaseRepository.findByCaseIdAndSuiteId(
					caseId, suiteId);
			testSuiteCaseRepository.delete(tsc);
		}

	}

	@Override
	@Transactional
	public TestCase saveTestCaseVo(TestCaseVO testCaseVO) throws BizException {
		testCaseVO.setTargetSqlIntro("222");
		// 检测参数的合法性
		if (testCaseVO.getSourceQuery() == null) {
			throw new BizException("源查询对象为空");
		}
		if (testCaseVO.getTargetQuery() == null) {
			throw new BizException("目标查询对象为空");
		}
		Integer srcDataSourceId = testCaseVO.getSourceQuery().getDataSourceId();
		if (srcDataSourceId == null || srcDataSourceId <= 0) {
			throw new BizException("源查询数据源ID为空或非法:" + srcDataSourceId);
		}
		Integer dstDataSourceId = testCaseVO.getTargetQuery().getDataSourceId();
		if (srcDataSourceId == null || srcDataSourceId <= 0) {
			throw new BizException("目标查询数据源ID为空或非法:" + srcDataSourceId);
		}
		String testCaseName = testCaseVO.getName();
		if (testCaseName == null || testCaseName.trim().length() == 0) {
			throw new BizException("案例的名称不能为空或空串:" + testCaseName);
		}
		// 处理新增名称不能重复
		List<TestCase> qtc = caseRepository.findAllByNameLike(testCaseVO
				.getName());
		if (qtc != null && qtc.size() > 0) {
			throw new BizException("案例名称已存在");
		}
		// 当查询没有命名时，使用默认的规则进行命名(SrcQry_案例名称/DstQry_案例名称)
		if (testCaseVO.getSourceQuery().getName() == null
				|| testCaseVO.getSourceQuery().getName().trim().length() == 0) {
			testCaseVO.getSourceQuery().setName(testCaseName + "_SrcQry");
		}
		if (testCaseVO.getTargetQuery().getName() == null
				|| testCaseVO.getTargetQuery().getName().trim().length() == 0) {
			testCaseVO.getTargetQuery().setName(testCaseName + "_DstQry");
		}

		// 处理源查询
		DataSource srcDataSource = dataSourceRepository.findById(
				srcDataSourceId).get();
		TestQuery sourceQuery = new TestQuery();
		TestQueryVO sourceQueryVO = testCaseVO.getSourceQuery();
		BeanUtils.copyProperties(sourceQueryVO, sourceQuery);
		srcDataSource.setId(srcDataSourceId);
		sourceQuery.setDataSource(srcDataSource);
		sourceQuery.setTestRules(null);
		testQueryRepository.save(sourceQuery);

		// 处理目标查询
		DataSource dstDatasource = dataSourceRepository.findById(
				dstDataSourceId).get();
		TestQuery targetQuery = new TestQuery();
		TestQueryVO targetQueryVO = testCaseVO.getTargetQuery();
		BeanUtils.copyProperties(targetQueryVO, targetQuery);
		dstDatasource.setId(dstDataSourceId);
		targetQuery.setDataSource(dstDatasource);
		targetQuery.setTestRules(null);
		testQueryRepository.save(targetQuery);

		// 保存案例
		TestCase testCase = new TestCase();
		BeanUtils.copyProperties(testCaseVO, testCase);
		fillTestCaseDefault(testCase);
		testCase.setSourceQuery(sourceQuery);
		testCase.setTargetQuery(targetQuery);
		if (testCaseVO.getTestSuiteID() != null) {
			TestSuite suite = new TestSuite();
			suite.setId(testCaseVO.getTestSuiteID());
			// testCase.setTestSuite(suite);
		}
		TestCase dd = caseRepository.save(testCase);
		return dd;
	}

	private void fillTestCaseDefault(TestCase testCase) {
		if (testCase.getUsePage() == null)
			testCase.setUsePage(false);
		if (testCase.getCaseType() == null)
			testCase.setCaseType(EnumTestCaseType.DataCompare);// 当前的案例类型为数据比较
		if (testCase.getPageType() == null)
			testCase.setPageType(EnumPageType.None);
		if (testCase.getEnumCompareDirection() == null)
			testCase.setEnumCompareDirection(EnumCompareDirection.LeftToRight);
	}

	@Override
	public void updateTestCase(TestCaseVO testCaseVO) throws BizException {
		// 检测参数的合法性
		if (testCaseVO.getSourceQuery() == null) {
			throw new BizException("源查询对象为空");
		}
		if (testCaseVO.getTargetQuery() == null) {
			throw new BizException("目标查询对象为空");
		}

		Integer srcDataSourceId = testCaseVO.getSourceQuery().getDataSourceId();
		if (srcDataSourceId == null || srcDataSourceId <= 0) {
			throw new BizException("源查询数据源ID为空或非法:" + srcDataSourceId);
		}
		Integer dstDataSourceId = testCaseVO.getTargetQuery().getDataSourceId();
		if (srcDataSourceId == null || srcDataSourceId <= 0) {
			throw new BizException("目标查询数据源ID为空或非法:" + srcDataSourceId);
		}

		String testCaseName = testCaseVO.getName();
		if (testCaseName == null || testCaseName.trim().length() == 0) {
			throw new BizException("案例的名称不能为空或空串:" + testCaseName);
		}
		// // 处理新增名称不能重复
		// List<TestCase> qtc =
		// caseRepository.findAllByNameLike(testCaseVO.getName());
		// for (TestCase testCase : qtc) {
		// if (testCase.getId().equals(testCaseVO.getId()) &&
		// testCase.getName().equals(testCaseName)) {
		// break;
		// } else {
		// throw new BizException("案例名称已存在");
		// }
		// }
		// 当查询没有命名时，使用默认的规则进行命名(SrcQry_案例名称/DstQry_案例名称)
		if (testCaseVO.getSourceQuery().getName() == null
				|| testCaseVO.getSourceQuery().getName().trim().length() == 0) {
			testCaseVO.getSourceQuery().setName("SrcQry_" + testCaseName);
		}
		if (testCaseVO.getTargetQuery().getName() == null
				|| testCaseVO.getTargetQuery().getName().trim().length() == 0) {
			testCaseVO.getTargetQuery().setName("DstQry_" + testCaseName);
		}
		// 处理源查询
		Optional<DataSource> srcDataSourceOp = dataSourceRepository
				.findById(srcDataSourceId);
		if (srcDataSourceOp == null || !srcDataSourceOp.isPresent())
			throw new BizException("源查询数据源ID未找到对应的数据源对象:" + srcDataSourceId);
		DataSource srcDataSource = srcDataSourceOp.get();
		TestQuery sourceQuery = new TestQuery();
		TestQueryVO sourceQueryVO = testCaseVO.getSourceQuery();
		BeanUtils.copyProperties(sourceQueryVO, sourceQuery);
		sourceQuery.setDataSource(srcDataSource);
		sourceQuery.setTestRules(null);
		testQueryRepository.save(sourceQuery);
		// 保存源查询的质量规则
		int sourcei = 0;
		List<TestRuleVO> testRules = testCaseVO.getSourceQuery().getTestRules();
		for (TestRuleVO testRuleVO : testRules) {
			CodeMap codeMap = codeMapRepository.findById(
					testRuleVO.getCodeMapId()).get();
			TestRule testRule = new TestRule();
			testRule.setCodeMap(codeMap);
			testRule.setTestQuery(sourceQuery);
			BeanUtils.copyProperties(testRuleVO, testRule);
			if (testRule.getId() == null) {
				testRule.setPosition(sourcei);
			}
			sourcei++;
			testRuleRepository.save(testRule);
		}
		// 处理目标查询
		Optional<DataSource> dstDataSourceOp = dataSourceRepository
				.findById(dstDataSourceId);
		if (dstDataSourceOp == null || !dstDataSourceOp.isPresent())
			throw new BizException("源查询数据源ID未找到对应的数据源对象:" + dstDataSourceId);
		DataSource dstDataSource = dstDataSourceOp.get();
		TestQuery targetQuery = new TestQuery();
		TestQueryVO targetQueryVO = testCaseVO.getTargetQuery();
		BeanUtils.copyProperties(targetQueryVO, targetQuery);
		targetQuery.setDataSource(dstDataSource);
		targetQuery.setTestRules(null);
		testQueryRepository.save(targetQuery);
		// 保存目标查询的质量规则
		List<TestRuleVO> tartestRules = testCaseVO.getTargetQuery()
				.getTestRules();
		int tartesti = 0;
		for (TestRuleVO testRuleVO : tartestRules) {
			CodeMap codeMap = codeMapRepository.findById(
					testRuleVO.getCodeMapId()).get();
			TestRule testRule = new TestRule();
			testRule.setCodeMap(codeMap);
			testRule.setTestQuery(targetQuery);
			BeanUtils.copyProperties(testRuleVO, testRule);
			if (testRule.getId() == null) {
				testRule.setPosition(tartesti);
			}
			tartesti++;
			testRuleRepository.save(testRule);
		}
		// 保存案例
		TestCase tc = testCaseRepository.getOne(testCaseVO.getId());
		TestCase testCase = new TestCase();
		BeanUtils.copyProperties(testCaseVO, testCase);
		fillTestCaseDefault(testCase);
		testCase.setSourceQuery(sourceQuery);
		testCase.setTargetQuery(targetQuery);
		testCase.setEditTime(new Date());
		testCase.setCreateTime(tc.getCreateTime());
		caseRepository.save(testCase);

	}

	@Override
	public List<TestCase> findByCaseIDs(String ids) {
		ArrayList<TestCase> list = new ArrayList<TestCase>();
		if (StringUtils.isNotBlank(ids)) {
			String[] testCaseIDs = ids.split(",");
			for (String id : testCaseIDs) {
				TestCase testCase = caseRepository.getOne(Integer.valueOf(id));
				list.add(testCase);
			}
			return list;
		} else {
			List<TestCase> all = caseRepository.findAll();
			return all;
		}
	}

	@Override
	public TestCase findByCaseName(String name) {
		TestCase testCase = caseRepository.findByCaseName(name);
		return testCase;
	}

	@Override
	public Page<QualityTestCase> findAllTestCaseByPage(String name,
			Pageable pageable) {
		Page<QualityTestCase> list = qualityTestCaseRepository
				.findAllTestCaseByPage(name, pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public void deleteQuanlityTestCaseBatch(String testCaseIDS) {
		if (StringUtils.isNotBlank(testCaseIDS)) {
			String[] ids = testCaseIDS.split(",");
			for (String id : ids) {
				qualityTestCaseRepository.deleteById(Integer.parseInt(id));
			}
		}

	}

	/*
	 * (non-Javadoc) 新增
	 * 
	 * @see
	 * com.jettech.service.TestCaseService#saveQualityTestCaseVo(com.jettech.vo.
	 * QualityTestCaseVO)
	 */
	@Override
	public void saveQualityTestCaseVo(QualityTestCaseVO testCaseVO)
			throws BizException {
		if (testCaseVO.getQualityTestQueryVo().getDataSourceId() == null) {
			throw new BizException("数据源为空");
		}
		/*
		 * if (testCaseVO.getQualityTestQueryVo().getTestFieldNames() == null) {
		 * throw new BizException("测试字段为空"); }
		 */
		if (testCaseVO.getQualityTestQueryVo().getName() == null) {
			throw new BizException("testQuery名称为空");
		}
		QualityTestCase qualityTestCase = new QualityTestCase();
		QualityTestQuery qualityTestQuery = new QualityTestQuery();
		qualityTestQuery.setName(testCaseVO.getQualityTestQueryVo().getName());
		// testfield
		ArrayList<DataField> list = new ArrayList<DataField>();
		/*
		 * String testfieldNames =
		 * testCaseVO.getQualityTestQueryVo().getTestFieldNames(); if
		 * (StringUtils.isNotBlank(testfieldNames)) { String[] names =
		 * testfieldNames.split(","); for (String name : names) { DataField
		 * testField = new DataField(); testField.setName(name);
		 * testFieldRepository.save(testField); list.add(testField); } }
		 * qualityTestQuery.setDataFields(list);
		 */
		DataSource dataSource = dataSourceRepository.getOne(testCaseVO
				.getQualityTestQueryVo().getDataSourceId());
		qualityTestQuery.setDataSource(dataSource);
		qualityTestQueryRepository.save(qualityTestQuery);
		qualityTestCaseRepository.save(qualityTestCase);

	}

	@Override
	public void updateTestQualityCase(QualityTestCaseVO testCaseVO,
			Integer testCaseId) {
		QualityTestCase testCase = qualityTestCaseRepository.getOne(testCaseId);
		BeanUtils.copyProperties(testCaseVO, testCase);
		QualityTestQuery testQuery = testCase.getQualityTestQuery();
		testQuery.setName(testCaseVO.getQualityTestQueryVo().getName());
		qualityTestQueryRepository.save(testQuery);
		qualityTestCaseRepository.save(testCase);

	}

	@Override
	public List<QualityTestCase> findByQualityCaseIDs(String ids) {
		ArrayList<QualityTestCase> list = new ArrayList<QualityTestCase>();
		if (StringUtils.isNotBlank(ids)) {
			String[] testCaseIDs = ids.split(",");
			for (String id : testCaseIDs) {
				QualityTestCase testCase = qualityTestCaseRepository
						.getOne(Integer.valueOf(id));
				list.add(testCase);
			}
			return list;
		} else {
			List<QualityTestCase> all = qualityTestCaseRepository.findAll();
			return all;
		}
	}

	@Override
	public List<TestCase> findAllById(Integer id) throws BizException {
		if (id == null) {
			throw new BizException("测试集ID为空");
		}
		List<TestCase> list = caseRepository.findAllById(id);
		if (list != null && list.size() > 0) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public Integer countBySuiteId(Integer suiteId) {
		Integer count = caseRepository.countBySuiteId(suiteId);
		return count;
	}

	/*
	 * @Override public Page<TestCase>
	 * findByTestSuiteIdNotInAndNameContaining(Integer testSuiteID, String name,
	 * Pageable pageable) { return
	 * caseRepository.findByTestSuiteIdNotInAndNameLike(testSuiteID, "%" + name
	 * + "%", pageable); }
	 */

	@Override
	public List<TestCase> findByTestSuiteId(Integer testSuiteID) {

		return caseRepository.findByTestSuiteId(testSuiteID);
	}

	/*
	 * @Override public Page<TestCase>
	 * findByTestSuiteIdNotInAndNameContaining(Integer testSuiteID, String name,
	 * Pageable pageable) { Page<TestCase> list; list =
	 * caseRepository.findByTestSuiteIdNotInAndNameContaining(testSuiteID, "%" +
	 * name + "%", pageable); System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^");
	 * return list; }
	 */

	/*
	 * @Override public Page<TestCase> findBySuiteId(Integer testSuiteID, String
	 * name, Pageable pageable) { Page<TestCase> list; if (testSuiteID != null
	 * && testSuiteID > 0) { if ("".equals(name)) { list =
	 * caseRepository.findByTestSuiteId(testSuiteID, pageable); } else { list =
	 * caseRepository.findByTestSuiteIdAndNameContaining(testSuiteID, name,
	 * pageable); }
	 * 
	 * } else { if ("".equals(name)) { list =
	 * caseRepository.findByTestSuiteIsNull(pageable); } else { list =
	 * caseRepository.findByTestSuiteIsNullAndNameContaining(name, pageable); }
	 * 
	 * } if (list.getSize() > 0) { return list; } else { return new
	 * PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0); }
	 * 
	 * }
	 */

	/*
	 * 右侧案例
	 * 
	 * @see
	 * com.jettech.service.ITestCaseService#findBySuiteId(java.lang.Integer,
	 * java.lang.String, org.springframework.data.domain.Pageable)
	 */
	/*
	 * @Override public Page<TestCase> findBySuiteId(Integer testSuiteID, String
	 * name, Pageable pageable) { Page<TestCase> list; if
	 * (name==null||name.equals("")) { // 名称为空时右侧的所有案例 list =
	 * caseRepository.findByTestSuiteId(testSuiteID, pageable); } else { //
	 * 右侧根据id，名称查询所有 list =
	 * caseRepository.findByTestSuiteIdAndNameContaining(testSuiteID, name,
	 * pageable); } if (list != null && list.getSize() > 0) { return list; }
	 * else { return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable,
	 * 0); } }
	 */

	/**
	 * 左侧的案例
	 */
	/*
	 * @Override public Page<TestCase> findALLBySuiteId(Integer testSuiteID,
	 * String name, Pageable pageable) { Page<TestCase> list; if
	 * (StringUtils.isBlank(name)) { // 名称为空，根据案例ID查询所有 list =
	 * caseRepository.findByTestSuiteNotContain(testSuiteID, pageable); } else {
	 * // 名称不为空，根据三个条件查询所有 list =
	 * caseRepository.findByTestSuiteIsNullAndNameContaining(testSuiteID,
	 * "%"+name+"%", pageable); // list =
	 * caseRepository.findByTestSuiteIsNullAndNameContaining(testSuiteID, name,
	 * pageable); }
	 * 
	 * if (list != null && list.getSize() > 0) { return list; } else { return
	 * new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0); } }
	 */
	/**
	 * 左侧的案例
	 */
	@Override
	public Page<TestCase> findALLBySuiteId(Integer testSuiteID, String name,
			EnumExecuteStatus exeState, Pageable pageable) {
		Page<TestCase> list;
		if (StringUtils.isBlank(name)
				&& (exeState == null || exeState.equals(""))) {
			// 名称为空，根据案例ID查询所有
			list = caseRepository.findByTestSuiteNotContain(testSuiteID,
					pageable);
		} else if ((exeState == null || exeState.equals(""))
				&& !StringUtils.isBlank(name)) {
			// 名称不为空，根据三个条件查询所有
			list = caseRepository.findByTestSuiteIsNullAndNameContaining(
					testSuiteID, "%" + name + "%", pageable);
			// list =
			// caseRepository.findByTestSuiteIsNullAndNameContaining(testSuiteID,
			// name, pageable);
		} else if (StringUtils.isBlank(name)
				&& (exeState != null || !exeState.equals(""))
				&& !exeState.equals(EnumExecuteStatus.Init)) {
			list = caseRepository.findByTestSuiteIsNullAndState(testSuiteID,
					exeState, pageable);
		} else if (!exeState.equals(EnumExecuteStatus.Init)
				&& (exeState != null || !exeState.equals(""))
				&& !StringUtils.isBlank(name)) {
			list = caseRepository.findByTestSuiteIsNullAndNameState(
					testSuiteID, exeState, name, pageable);
		} else if (StringUtils.isBlank(name)
				&& exeState.equals(EnumExecuteStatus.Init)) {
			list = caseRepository.findByCaseIsInit(testSuiteID, pageable);
		} else {
			list = caseRepository.findByCaseIsInitName(testSuiteID, name,
					pageable);
		}

		if (list != null && list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable,
					0);
		}
	}

	/*
	 * 右侧案例
	 * 
	 * @see
	 * com.jettech.service.ITestCaseService#findBySuiteId(java.lang.Integer,
	 * java.lang.String, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<TestCase> findBySuiteId(Integer testSuiteID, String name,
			Pageable pageable) {
		Page<TestCase> list;
		if (StringUtils.isBlank(name)) {
			// 名称为空时右侧的所有案例
			list = caseRepository.findByTestSuiteId(testSuiteID, pageable);
		} else {
			// 右侧根据id，名称查询所有
			list = caseRepository.findByTestSuiteIdAndNameContaining(
					testSuiteID, "%" + name + "%", pageable);
		}
		if (list != null && list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable,
					0);
		}
	}

	@Override
	public Page<TestCase> findBySuiteId(Integer testSuiteID, Pageable pageable) {
		Page<TestCase> list;
		if (testSuiteID != null && testSuiteID > 0) {
			list = null/*
						 * caseRepository.findByTestSuiteId(testSuiteID,
						 * pageable)
						 */;
		} else {
			list = null/* caseRepository.findByTestSuiteIsNull(pageable) */;
		}
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable,
					0);
		}

	}

	@Override
	public void exportCheckedCase(String ids, HttpServletResponse res) {
		List<TestCaseVO> voList = null;
		// 找到导出的数据、封装
		if (ids == null || ids.length() == 0) {
			List<TestCase> list = caseRepository.findAll();
			List<TestResult> resultList = testResultRepository.findAll();
			List<Product> productList = productRepository.findAll();
			voList = convertToVoList(list, resultList, productList);
		} else {
			List<TestCase> list = findByCaseIds(ids);
			List<TestResult> resultList = findResultByCaseIds(ids);
			List<Product> productList = findProductByCaseIds(ids);
			voList = convertToVoList(list, resultList, productList);
		}

		// 创建表格等到处环境
		HSSFWorkbook workbook = new HSSFWorkbook();
		String sheetName = "testCase";
		HSSFSheet sheet = workbook.createSheet(sheetName);
		// 设置表头内容
		setHeaderContent(sheet);
		// 设置表格内容
		createTableContent(sheet, voList);
		// 导出
		export(workbook, res);
	}

	private void export(HSSFWorkbook workbook, HttpServletResponse res) {
		String fileName = "testCase" + ".xls";
		res.reset();
		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		res.setHeader("Access-Control-Allow-Origin", "*");// 允许跨域请求

		try {
			OutputStream os = res.getOutputStream();
			res.addHeader("Content-Disposition", "attachment;fileName="
					+ java.net.URLEncoder.encode(fileName, "UTF-8"));
			workbook.write(os);
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createTableContent(HSSFSheet sheet, List<TestCaseVO> voList) {
		int rowIndex = 1;
		for (TestCaseVO vo : voList) {
			HSSFRow rowItem = sheet.createRow(rowIndex++);
			if (StringUtils.isNotBlank(vo.getId().toString())) {
				// 设置单元格的值
				rowItem.createCell(0).setCellValue(vo.getId().toString());
			} else {
				rowItem.createCell(0).setCellValue("null");
			}
			rowItem.createCell(1).setCellValue("数据迁移");
			// if (vo.getCaseType() != null) {
			// rowItem.createCell(2).setCellValue(vo.getCaseType().toString());
			// }
			rowItem.createCell(2).setCellValue(vo.getTestSuiteName());
			rowItem.createCell(3).setCellValue(vo.getTransactionName());
			rowItem.createCell(4).setCellValue(vo.getSceneName());
			rowItem.createCell(5).setCellValue(vo.getName());
			rowItem.createCell(6).setCellValue(vo.getTestIntent());
			rowItem.createCell(7).setCellValue(vo.getSqlText()); // 源表SQL
			rowItem.createCell(8).setCellValue(vo.getSourceSqlIntro());
			rowItem.createCell(9).setCellValue(vo.getSourceDataSourceName());
			rowItem.createCell(10).setCellValue(vo.getKeyText());

			rowItem.createCell(11).setCellValue(vo.getTargetSqlText()); // 目标表SQL
			rowItem.createCell(12).setCellValue(vo.getTargetSqlIntro());
			rowItem.createCell(13).setCellValue(vo.getTargetDataSourceName());
			rowItem.createCell(14).setCellValue(vo.getTargetkeyText());
			rowItem.createCell(15).setCellValue(vo.getResult());
			rowItem.createCell(16).setCellValue("备注");
			// if (vo.getMaxResultRows() != null) {
			// } else {
			// // rowItem.createCell(3).setCellValue("null");
			// }
			// if (StringUtils.isNotBlank(vo.getSourceDataSourceName())) {
			// } else {
			// // rowItem.createCell(4).setCellValue("null");
			// }
			// if (StringUtils.isNotBlank(vo.getTargetDataSourceName())) {
			// } else {
			// rowItem.createCell(5).setCellValue("null");
			// }
		}
	}

	private void setHeaderContent(HSSFSheet sheet) {
		HSSFRow header = sheet.createRow(0);
		header.createCell(0).setCellValue("id");
		header.createCell(1).setCellValue("系统名称");
		header.createCell(2).setCellValue("业务大类");
		header.createCell(3).setCellValue("交易名称");
		header.createCell(4).setCellValue("场景名称");
		header.createCell(5).setCellValue("案例编号");
		header.createCell(6).setCellValue("测试意图");
		header.createCell(7).setCellValue("源表SQL");
		header.createCell(8).setCellValue("源表SQL注释");
		header.createCell(9).setCellValue("sourceDB");
		header.createCell(10).setCellValue("sourceKey");
		header.createCell(11).setCellValue("目标表SQL");
		header.createCell(12).setCellValue("目标表SQL注释");
		header.createCell(13).setCellValue("targetDB");
		header.createCell(14).setCellValue("targetKey");
		header.createCell(15).setCellValue("是否通过");
		header.createCell(16).setCellValue("备注");
	}

	private List<TestCaseVO> convertToVoList(List<TestCase> list,
			List<TestResult> resultList, List<Product> productList) {
		ArrayList<TestCaseVO> voList = new ArrayList<TestCaseVO>();

		if (resultList.size() > 0 && productList.size() > 0) {
			for (int i = 0; i < list.size(); i++) {
				TestCaseVO vo = new TestCaseVO(list.get(i));
				vo.setResult(resultList.get(i).getResult());
				vo.setTestSuiteName(productList.get(i).getName());
				voList.add(vo);
			}
		} else {
			for (int i = 0; i < list.size(); i++) {
				TestCaseVO vo = new TestCaseVO(list.get(i));
				voList.add(vo);
			}
		}
		return voList;
	}

	private List<TestCase> findByCaseIds(String ids) {
		List<TestCase> list = new ArrayList<TestCase>();
		if (StringUtils.isNotBlank(ids)) {
			String[] testCaseIds = ids.split(",");
			for (String testCaseId : testCaseIds) {
				int caseId = Integer.parseInt(testCaseId);
				TestCase testCase = caseRepository.findById(caseId).get();
				list.add(testCase);
			}
		} else {
			list = caseRepository.findAll();
		}
		return list;
	}

	public List<TestResult> findResultByCaseIds(String ids) {
		List<TestResult> resultList = new ArrayList<TestResult>();
		if (StringUtils.isNotEmpty(ids)) {
			String[] testCaseIds = ids.split(",");
			for (String id : testCaseIds) {
				int caseId = Integer.parseInt(id);
				List<TestResult> testResult = testResultRepository
						.findResultByCaseIds(caseId);
				resultList.addAll(testResult);
			}
		}
		return resultList;
	}

	public List<Product> findProductByCaseIds(String ids) {
		List<Product> productList = new ArrayList<Product>();
		if (StringUtils.isNotEmpty(ids)) {
			String[] testCaseIds = ids.split(",");
			for (String id : testCaseIds) {
				int caseId = Integer.parseInt(id);
				List<Product> product = productRepository
						.findProductByCaseId(caseId);
				productList.addAll(product);
			}
		}
		return productList;
	}

	public ResultVO uploadTestCase(Map<String, String> map) {

		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		ResultVO result = new ResultVO();

		String filePath = String.valueOf(map.get("filePath"));
		int lastIndex = filePath.lastIndexOf("\\");
		String name = filePath.substring(lastIndex + 1);
		System.out.println(lastIndex + " " + name);

		// 获取测试集id
		String testSuiteId = map.get("testSuiteId");
		int suiteId = Integer.parseInt(testSuiteId);

		Workbook workbook = ExcelUtil.getWorkBook(filePath);

		if (workbook != null) {
			for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
				// System.out.println(workbook.getNumberOfSheets());
				// 获得当前sheet工作表
				Sheet sheet = workbook.getSheetAt(sheetNum);
				// 获取sheet表名
				String tableName = sheet.getSheetName();

				// 获得当前sheet的开始行
				int firstRowNum = sheet.getFirstRowNum();
				// 获得当前sheet的结束行
				int lastRowNum = sheet.getLastRowNum();
				try {
					for (int rowNum = firstRowNum + 1; rowNum <= lastRowNum; rowNum++) {
						// 获得当前行
						Row row = sheet.getRow(rowNum);

						// 获得当前行的开始列
						// int firstCellNum = row.getFirstCellNum();
						// 获得当前行的列数
						// int lastCellNum = row.getPhysicalNumberOfCells();
						String l1 = ExcelUtil.getCellValue(row.getCell(0)); // 编号
						String l2 = ExcelUtil.getCellValue(row.getCell(1)); // 系统名称（应案例类型：数据迁移/数据质量）
						String l3 = ExcelUtil.getCellValue(row.getCell(2)); // 业务大类(被测系统)
						String l4 = ExcelUtil.getCellValue(row.getCell(3)); // 交易名称
						String l5 = ExcelUtil.getCellValue(row.getCell(4)); // 场景名称
						String l6 = ExcelUtil.getCellValue(row.getCell(5)); // 案例编号(案例名称)
						String l7 = ExcelUtil.getCellValue(row.getCell(6)); // 测试意图(案例说明)
						String l8 = ExcelUtil.getCellValue(row.getCell(7)); // 源表SQL
						String l9 = ExcelUtil.getCellValue(row.getCell(8)); // 源表SQL注释
						String l10 = ExcelUtil.getCellValue(row.getCell(9)); // sourceDB
						String l11 = ExcelUtil.getCellValue(row.getCell(10)); // sourceKey
						String l12 = ExcelUtil.getCellValue(row.getCell(11)); // 目标表SQL
						String l13 = ExcelUtil.getCellValue(row.getCell(12)); // 目标表SQL注释
						String l14 = ExcelUtil.getCellValue(row.getCell(13)); // targetDB
						String l15 = ExcelUtil.getCellValue(row.getCell(14)); // targetKey
						String l16 = ExcelUtil.getCellValue(row.getCell(15)); // 是否通过
						String l17 = ExcelUtil.getCellValue(row.getCell(16)); // 备注

						// 判断案例名称是否重复
						List<TestCase> testCase = testCaseRepository
								.findByName(l6);
						if (!testCase.isEmpty()) {
							result.setMessage("案例名称和序号不能重复");
							result.setFlag(false);
							return result;
						}

						// 封装数据
						TestCaseVO testCaseVO = new TestCaseVO();
						// Integer inte = Integer.parseInt(l1 + 1);
						// testCaseVO.setId(Integer.valueOf(inte));
						testCaseVO.setTestSuiteName(l3);
						testCaseVO.setTransactionName(l4);
						testCaseVO.setSceneName(l5);
						testCaseVO.setName(l6);
						testCaseVO.setTestIntent(l7);
						testCaseVO.setSqlText(l8);
						testCaseVO.setSourceSqlIntro(l9);
						testCaseVO.setSourceDataSourceName(l10);
						testCaseVO.setKeyText(l11);
						testCaseVO.setTargetSqlText(l12);
						testCaseVO.setTargetSqlIntro(l13);
						testCaseVO.setTargetDataSourceName(l14);
						testCaseVO.setTargetkeyText(l15);
						testCaseVO.setIsSQLCase(true);
						testCaseVO
								.setEnumCompareDirection(EnumCompareDirection.TwoWay);
						// 判断数据源是否存在
						DataSource sourceDataSource = dataSourceRepository
								.findByName(l10);
						DataSource targetDataSource = dataSourceRepository
								.findByName(l14);
						if (sourceDataSource != null
								&& targetDataSource != null) {
							// 获取数据源id
							// int sourceDataSourceID =
							// sourceDataSource.getId();
							// testCaseVO.setSourceQueryID(dataSourceId);
							// 源数据
							// TestQuery sourceQuery =
							// testQueryRepository.findbyName(testCaseVO.getSourceDataSourceName());
							TestQueryVO testQueryVO = new TestQueryVO();
							testQueryVO.setSqlText(l8);
							testQueryVO.setKeyText(l11);
							testQueryVO.setSqlIntro(l9);
							testQueryVO.setDataSourceId(sourceDataSource
									.getId());
							// 目标数据
							// TestQuery targetQuery =
							// testQueryRepository.findbyName(testCaseVO.getTargetDataSourceName());
							TestQueryVO targetQueryVO = new TestQueryVO();
							targetQueryVO.setSqlText(l12);
							targetQueryVO.setKeyText(l15);
							targetQueryVO.setSqlIntro(l13);
							targetQueryVO.setDataSourceId(targetDataSource
									.getId());

							testCaseVO.setSourceQuery(testQueryVO);
							testCaseVO.setTargetQuery(targetQueryVO);
							testCaseVO.setTestSuiteID(suiteId);
							TestCase t = saveTestCaseVo(testCaseVO);
							// 把保存的案例添加到测试集上
							testSuiteCaseRepository.changeTestCasePosition(
									suiteId, t.getId());
							result.setFlag(true);
							result.setMessage("上传案例成功");
						} else {
							result.setMessage("没有该数据源不可以上传");
							result.setFlag(false);
							return result;
						}

					}
				} catch (Exception e) {
					result.setMessage("案例名称和序号不能重复");
					result.setFlag(false);
					e.printStackTrace();
				}
			}
		}

		return result;
	}

	@Override
	public Integer getTableCount(Integer dataSourceId, String tableName)
			throws Exception {
		if (dataSourceId == null) {
			throw new Exception("数据源为空，不能进行操作");
		}
		if (tableName == null) {
			throw new Exception("表名为空，不能进行操作");
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
		SycData sycData = getAdapterAndConnection(dbType, driver, url, port,
				host, username, pwd, sid);
		conn = sycData.getConn();
		adapter = sycData.getAdapter();

		Integer count = adapter.getTableCount(tableName, conn, schema);

		return count;
	}

	@Override
	public ResultVO autoCreateCase(DataSource sourceDataSource,
			DataSource targetDataSource, String sourceTableName,
			String targetTableName) {

		TestCaseVO testCaseVo = new TestCaseVO();
		// 处理源查询
		DataSource srcDataSource = dataSourceRepository.findById(
				sourceDataSource.getId()).get();
		TestQuery sourceQuery = new TestQuery();
		sourceQuery.setCreateTime(new Date());
		sourceQuery.setEditTime(new Date());
		sourceQuery.setName("自动生成案例源");
		sourceQuery.setSqlText("select * from " + sourceTableName);

		srcDataSource.setId(sourceDataSource.getId());
		sourceQuery.setDataSource(srcDataSource);
		sourceQuery.setTestRules(null);
		testQueryRepository.save(sourceQuery);

		// 处理目标查询
		DataSource dstDatasource = dataSourceRepository.findById(
				targetDataSource.getId()).get();
		TestQuery targetQuery = new TestQuery();
		targetQuery.setCreateTime(new Date());
		targetQuery.setEditTime(new Date());
		targetQuery.setName("自动生成案例目标");
		targetQuery.setSqlText("select * from " + targetTableName);

		dstDatasource.setId(targetDataSource.getId());
		targetQuery.setDataSource(dstDatasource);
		targetQuery.setTestRules(null);
		testQueryRepository.save(targetQuery);

		// 保存案例
		TestCase testCase = new TestCase();
		testCase.setCaseType(EnumTestCaseType.DataCompare);
		testCase.setCreateTime(new Date());
		testCase.setEditTime(new Date());
		testCase.setName("测试自动生成案例");
		fillTestCaseDefault(testCase);
		testCase.setSourceQuery(sourceQuery);
		testCase.setTargetQuery(targetQuery);
		// if (testCaseVO.getTestSuiteID() != null) {
		// TestSuite suite = new TestSuite();
		// suite.setId(testCaseVO.getTestSuiteID());
		// }
		TestCase dd = caseRepository.save(testCase);
		// testSuiteCaseRepository.changeTestCasePosition(testCaseVO.getTestSuiteID(),dd.getId());
		if (dd != null) {
			return new ResultVO(true, StatusCode.OK, "生成案例成功", dd);
		}
		return new ResultVO(false, StatusCode.ERROR, "生成案例失败", dd);
	}

	/**
	 * <h1>比较两个表数量
	 * 
	 * @param sourceDataSourceId
	 * @param sourceTableName
	 * @param tardetDataSourceId
	 * @param targetTableName
	 * @return
	 */
	public Boolean compareTwoTable(Integer sourceDataSourceId,
			String sourceTableName, Integer tardetDataSourceId,
			String targetTableName) {
		Boolean flag = false;
		AbstractAdapter adapter = null;
		AbstractAdapter tarAdapter = null;
		// 源数据处理
		DataSource ds = repository.findById(sourceDataSourceId).get();
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
		System.out.println("source schema=" + schema);
		System.out.println("source dbType=" + dbType);
		Connection conn = null;
		SycData sycData = getAdapterAndConnection(dbType, driver, url, port,
				host, username, pwd, sid);
		conn = sycData.getConn();
		adapter = sycData.getAdapter();

		// 目标数据处理
		DataSource tr = repository.findById(tardetDataSourceId).get();
		String tarSchema = tr.getDefaultSchema();
		String tarUsername = tr.getUserName();
		String tarPwd = tr.getPassword();
		String tarName = tr.getName();
		EnumDatabaseType tarDbType = tr.getDatabaseType();
		String tarHost = tr.getHost();
		String tarPort = tr.getPort();
		String tarUrl = tr.getUrl();
		String tarDriver = tr.getDriver();
		String tarSid = tr.getSid();
		System.out.println("target schema=" + tarSchema);
		System.out.println("target dbType=" + tarDbType);
		Connection tarConn = null;
		SycData tarSycData = getAdapterAndConnection(tarDbType, tarDriver,
				tarUrl, tarPort, tarHost, tarUsername, tarPwd, tarSid);
		tarConn = tarSycData.getConn();
		tarAdapter = tarSycData.getAdapter();

		Integer sourceCount = adapter.getTableCount(sourceTableName, conn,
				schema);
		Integer targetCount = tarAdapter.getTableCount(sourceTableName,
				tarConn, tarSchema);
		if (sourceCount > targetCount) {
			return true;
		} else {
			return false;
		}
	}

	/**
	 * 目前先不使用
	 * 
	 * @param sourceTableName
	 * @param targetTableName
	 * @param sourceSchemaName
	 * @param targetSchemaName
	 * @return
	 */
	public Boolean compareTwoTableField(String sourceTableName,
			String targetTableName, String sourceSchemaName,
			String targetSchemaName) {
		List<TestFieldVO> arrvolist = new ArrayList<>();
		List<DataField> dataLs = testFieldRepository.findByTBName(
				targetTableName, sourceSchemaName);
		for (DataField df : dataLs) {
			TestFieldVO tf = new TestFieldVO(df);

		}

		return null;
	}

	/**
	 * 根据数据源自动生成案例
	 * 
	 * @throws BizException
	 * 
	 * @throws Exception
	 */
	public String createCaseByDataSource(String productName,
			String testSuiteName, DataSchema sourceDataSchema,
			DataSchema targetDataSchema) throws BizException {
		List<DataTable> sourceDataTables = sourceDataSchema.getDataTables();
		if (sourceDataTables == null || sourceDataTables.size() == 0) {
			throw new BizException("源库模型[" + sourceDataSchema.getName()
					+ "]下无表");
		}
		List<DataTable> targetDataTables = targetDataSchema.getDataTables();
		if (targetDataTables == null || targetDataTables.size() == 0) {
			throw new BizException("目标库模型[" + targetDataSchema.getName()
					+ "]下无表");
		}
		String sourceDBSchema = sourceDataSchema.getSchemaName();
		if (sourceDBSchema != null)
			sourceDBSchema = sourceDBSchema.trim();
		String targetDBSchema = targetDataSchema.getSchemaName();
		if (targetDBSchema != null)
			targetDBSchema = targetDBSchema.trim();

		long start1 = (new Date()).getTime();
		// 处理源查询
		DataSource srcDataSource = sourceDataSchema.getDataSource();
		DataSource dstDatasource = targetDataSchema.getDataSource();

		logger.info("源库[" + sourceDataSchema.getName() + "],dbSchema["
				+ sourceDBSchema + "],dataSource[" + srcDataSource.getName()
				+ "]");
		logger.info("目标库[" + targetDataSchema.getName() + "],dbSchema["
				+ targetDataSchema + "],dataSource[" + dstDatasource.getName()
				+ "]");

		Map<String, DataTable> sourceTableMap = new ConcurrentHashMap<>();
		for (int i = 0; i < sourceDataTables.size(); i++) {
			sourceTableMap.put(sourceDataTables.get(i).getName().toUpperCase()
					.trim(), sourceDataTables.get(i));
		}
		Map<String, DataTable> targetTableMap = new ConcurrentHashMap<>();
		for (int i = 0; i < targetDataTables.size(); i++) {
			targetTableMap.put(targetDataTables.get(i).getName().toUpperCase()
					.trim(), targetDataTables.get(i));
		}

		Map<String, DataField> sourceFieldMap = new ConcurrentHashMap<>();
		Map<String, DataField> targetFieldMap = new ConcurrentHashMap<>();

		StringBuilder fieldBuilder = new StringBuilder();
		StringBuilder notFoundTargetTable = new StringBuilder();
		List<TestQuery> queryList = new ArrayList<>();
		List<TestCase> caseList = new ArrayList<>();
		String _HEAD = "GZ2_";
		// for (int i = 0; i < sourDataTable.size(); i++) {
		TestCase tc = null;
		int i = 0;
		for (DataTable sourceTable : sourceTableMap.values()) {
			i++;
			long tableStart = (new Date()).getTime();
			String tableName = sourceTable.getName().toUpperCase();
			logger.info("处理表：[" + i + "/" + sourceTableMap.size() + "],["
					+ tableName + "]");
			if (!targetTableMap.containsKey(tableName)) {
				logger.warn("目标库模型中未找到表[" + tableName + "]");
				// 目标表在目标库中不存在(只是在模型列表中不存在,物理数据库需要再次核查/同步在测)
				notFoundTargetTable.append("," + tableName);
				continue;
			}
			DataTable targetTable = targetTableMap.get(tableName);
			fieldBuilder.delete(0, fieldBuilder.length());

			// :组装SQL需要考虑数据库表的字段名称使用数据库关键字的情况,并且不同的数据库的关键字不同
			sourceFieldMap.clear();
			for (DataField field : sourceTable.getDataFields()) {
				sourceFieldMap.put(field.getName(), field);
			}

			targetFieldMap.clear();
			for (DataField field : targetTable.getDataFields()) {
				targetFieldMap.put(field.getName(), field);
			}

			for (String fieldName : sourceFieldMap.keySet()) {
				if (targetFieldMap.containsKey(fieldName)) {
					fieldBuilder.append("," + fieldName);
				} else {
					// 目标表不包含来源表的字段
					logger.warn("目标库模型中表[" + tableName + "]下不包含字段[" + fieldName
							+ "]");
					continue;
				}
			}
			String allFiledNameStr = null;
			if (fieldBuilder.length() > 0) {
				allFiledNameStr = fieldBuilder.toString().substring(1);
			} else {
				logger.warn("源库模型中表[" + tableName + "]下无字段");
				continue;
			}

			TestQuery sourceQuery = new TestQuery();
			sourceQuery.setCreateTime(new Date());
			sourceQuery.setEditTime(new Date());
			sourceQuery.setName(_HEAD + "S_" + tableName);

			if (sourceDBSchema != null && !sourceDBSchema.trim().isEmpty()) {
				if (srcDataSource.getDatabaseType() == EnumDatabaseType.Oracle) {
					sourceQuery.setSqlText("select " + allFiledNameStr
							+ " from " + sourceDBSchema + ".\"" + tableName
							+ "\"");
				} else {
					sourceQuery.setSqlText("select " + allFiledNameStr
							+ " from " + sourceDBSchema + "." + tableName);
				}
			} else {
				sourceQuery.setSqlText("select " + allFiledNameStr + " from "
						+ tableName);
			}
			sourceQuery.setKeyText(allFiledNameStr);
			sourceQuery.setDataSource(srcDataSource);
			sourceQuery.setTestRules(null);
			// testQueryRepository.save(sourceQuery);
			queryList.add(sourceQuery);

			// 处理目标查询
			TestQuery targetQuery = new TestQuery();
			targetQuery.setCreateTime(new Date());
			targetQuery.setEditTime(new Date());
			targetQuery.setName(_HEAD + "T_" + tableName);
			if (targetDBSchema != null && !targetDBSchema.isEmpty()) {
				if (srcDataSource.getDatabaseType() == EnumDatabaseType.Oracle) {
					targetQuery.setSqlText("select " + allFiledNameStr
							+ " from " + targetDBSchema + ".\"" + tableName
							+ "\"");
				} else {
					targetQuery.setSqlText("select " + allFiledNameStr
							+ " from " + targetDBSchema + "." + tableName);
				}
			} else {
				targetQuery.setSqlText("select " + allFiledNameStr + " from "
						+ tableName);
			}
			targetQuery.setKeyText(allFiledNameStr);
			targetQuery.setDataSource(dstDatasource);
			targetQuery.setTestRules(null);
			// testQueryRepository.save(targetQuery);
			queryList.add(targetQuery);

			// 保存案例
			TestCase testCase = new TestCase();
			testCase.setCaseType(EnumTestCaseType.DataCompare);
			testCase.setCreateTime(new Date());
			testCase.setEditTime(new Date());

			testCase.setName(_HEAD + tableName);
			fillTestCaseDefault(testCase);
			testCase.setSourceQuery(sourceQuery);
			testCase.setTargetQuery(targetQuery);

			caseList.add(testCase);
			logger.info("处理表：[" + i + "/" + sourceTableMap.size() + "],["
					+ tableName + "],耗时"
					+ DateUtil.getEclapsedTimesStr(tableStart));
			// tc = caseRepository.save(testCase);
			// if (tc != null) {
			// caseList.add(tc);
			// }
		}
		// 批量保存查询
		long start2 = (new Date()).getTime();
		testQueryRepository.saveAll(queryList);
		logger.info("保存查询" + caseList.size() + "个,耗时:"
				+ DateUtil.getEclapsedTimesStr(start2));

		// 批量保存案例
		start2 = (new Date()).getTime();
		caseRepository.saveAll(caseList);
		logger.info("保存案例" + caseList.size() + "个,耗时:"
				+ DateUtil.getEclapsedTimesStr(start2));

		TestSuite testSuite = this.getTestSuite(testSuiteName, productName);
		if (testSuite == null || testSuite.getId() == null) {
			logger.warn("未找到/创建测试集,产品名称[" + productName + "],测试集名称["
					+ testSuiteName + "]");
		} else {
			logger.info("testSuiteId:" + testSuite.getId());
		}
		List<TestSuiteCase> existSuiteCase = testSuiteCaseRepository
				.findBySuiteId(testSuite.getId());
		if (existSuiteCase != null && existSuiteCase.size() > 0) {
			testSuiteCaseRepository.deleteInBatch(existSuiteCase);
		}

		List<TestSuiteCase> suiteCaseList = new ArrayList<>();
		for (TestCase testCase : caseList) {
			// testSuiteCaseRepository.changeTestCasePosition(testSuite.getId(),
			// testCase.getId());
			TestSuiteCase sc = new TestSuiteCase();
			sc.setCaseId(testCase.getId());
			sc.setSuiteId(testSuite.getId());
			suiteCaseList.add(sc);
		}
		start2 = (new Date()).getTime();
		testSuiteCaseRepository.saveAll(suiteCaseList);
		logger.info("保存集合案例关系" + suiteCaseList.size() + "个,耗时:"
				+ DateUtil.getEclapsedTimesStr(start2));

		if (caseList.size() > 0) {
			logger.info("保存测试集案例数量：[" + caseList.size() + "]");
		}

		logger.info("自动创建案例,成功/表总数:[" + caseList.size() + "/"
				+ sourceTableMap.size() + "],耗时:"
				+ DateUtil.getEclapsedTimesStr(start1));

		if (caseList != null && caseList.size() > 0) {
			return "添加成功:" + caseList.size();
		}
		return "添加失败";
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
				db.setDriver(OracleAdapter.DEFAULT_DRIVER);
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
			conn = ((DB2Adapter) adapter).getConnection(db);
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
				db.setUrl("jdbc:informix-sqli://" + host + ":" + port
						+ "/testdb:INFORMIXSERVER=ol_informix1170;");
			}
			// jdbc:informix-sqli://localhost:9090/testdb:INFORMIXSERVER=ol_demo1
			db.setUsername(username);
			db.setPassword(pwd);
			db.setName(EnumDatabaseType.Informix.name());
			db.setDbtype(EnumDatabaseType.Informix);

			adapter = new InformixAdapter();
			// Informix的连接中,需要将用户和密码与原URL组合为FullURL
			String fullUrl = url + ";user=" + username + ";password=" + pwd;
			conn = ((InformixAdapter) adapter)
					.createConnection(driver, fullUrl);
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
			db.setName(EnumDatabaseType.SyBase.name());
			db.setDbtype(EnumDatabaseType.SyBase);

			adapter = new SyBaseAdapter();
			conn = ((SyBaseAdapter) adapter).getConnection(db);
		}
		sycData.setAdapter(adapter);
		sycData.setConn(conn);
		return sycData;
	}

	@Override
	public String getDataTableName(String dataSourceName) {
		if (dataSourceName == null) {
			return "数据源名为空,不能进行下一步操作";
		}
		DataSource ds = repository.findByName(dataSourceName);

		// 尝试读取该用户访问权限下的所有schema(默认的)
		List<DataSchema> testdatabaseList = ds.getDataSchemas();
		List<DataTable> dataTables = testdatabaseList.get(0).getDataTables();
		if (testdatabaseList.size() == 0) {
			return "这个源下面没有默认的这个库，不能进行同步";
		}
		String tbn = new String();
		for (int i = 0; i < dataTables.size(); i++) {
			String tableName = dataTables.get(i).getName();
			tbn += tableName + ",";
		}
		return tbn;
	}
}
