package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.transaction.Transactional;

import org.apache.commons.lang.StringUtils;
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
import com.jettech.EnumPageType;
import com.jettech.EnumTestCaseType;
import com.jettech.domain.CaseModel;
import com.jettech.domain.CompareCaseModel;
import com.jettech.domain.CompareaToFileCaseModel;
import com.jettech.entity.CodeMap;
import com.jettech.entity.DataField;
import com.jettech.entity.DataSource;
import com.jettech.entity.Product;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.QualityTestQuery;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestQuery;
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
import com.jettech.repostory.TestRuleRepository;
import com.jettech.repostory.TestSuiteCaseRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.ITestCaseService;
import com.jettech.thread.JobWorker;
import com.jettech.util.FileUtil;
import com.jettech.vo.QualityTestCaseVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.TestCaseVO;
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
		TestCase testCase = this.findById(id);
		String name = testCase.getName();
		caseRepository.deleteById(id);
		if (testCase.getTargetQuery() != null)
			testQueryRepository.deleteById(testCase.getTargetQuery().getId());
		if (testCase.getSourceQuery() != null)
			testQueryRepository.deleteById(testCase.getSourceQuery().getId());
		logger.info("删除案例成功：" + id + "_" + name);
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
		logger.info("ready doTestCase:" + testCase.getId() + "_" + testCase.getName());
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
			String info = "cast testCase to model error." + testCase.getId() + "_" + testCase.getName();
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
			throw new IllegalArgumentException("readSQLCase argument is null or empty.");
		if (!map.containsKey("filePath")) {
			result.setFlag(false);
			result.setMessage("not input argument:filePath");
		}
		String filePath = map.get("filePath");
		if (!map.containsKey("testSuiteId")) {
			result.setFlag(false);
			result.setMessage("not input argument:testSuiteId");
		}
		String testSuiteId = map.get("testSuiteId");
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
				List<TestCase> findAllByNameLike = caseRepository.findAllByNameLike(testCaseName);
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
				// 贪婪获取SQL语句的内容(多行的SQL语句)
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
				info = testCaseName + " can not found DataSource name is  " + sourceDB;
				logger.info(info);
				result.setFlag(false);
				result.setMessage(info);
				break;
			}
			DataSource dsTarget = dataSourceRepository.findByName(targetDB);
			if (dsTarget == null) {
				info = testCaseName + " can not found DataSource name is " + targetDB;
				logger.info(info);
				result.setFlag(false);
				result.setMessage(info);
				break;
			}

			TestCase testCase = null;
			List<TestCase> caseList = this.caseRepository.findByNameAndSuite(testCaseName, testSuite.getName());
			if (caseList != null && caseList.size() > 0) {
				logger.info("测试集 [" + testSuite.getName() + "] 的案例 [" + testCaseName + "] 将被更新");
				updateCaseCount = updateCaseCount + 1;
				testCase = caseList.get(0);
			} else {
				logger.info("测试集 [" + testSuite.getName() + "] 新建案例 [" + testCaseName + "]");
				testCase = new TestCase();
			}
			testCase.setName(testCaseName);
//			testCase.setTestSuite(testSuite);
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

			testQueryRepository.save(sourceQuery);
			testQueryRepository.save(targetQuery);
			if (testCase.getCaseType() == null) {
				testCase.setCaseType(EnumTestCaseType.DataCompare);
			}
			caseRepository.save(testCase);

			successCount++;
			logger.info("######## 保存第[" + successCount + "] 案例成功:[" + testCase.getName() + "]");

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
			List<TestCase> caseList = this.caseRepository.findByNameAndSuite(testCaseName, testSuite.getName());
			if (caseList != null && caseList.size() > 0) {
				testCase = caseList.get(0);
			} else {
				testCase = new TestCase();
			}
			testCase.setName(testCaseName);
//			testCase.setTestSuite(testSuite);
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
			logger.info("保存第[" + successCount + "] 案例成功:[" + testCase.getName() + "]");

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
		List<TestSuite> testSuiteList = testSuiteRepository.findByName(testSuiteName, productName);
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
		return containTag(line, TEST_CASE_NAME) || containTag(line, SOURCE_DB) || containTag(line, SOURCE_SQL)
				|| containTag(line, SOURCE_KEY) || containTag(line, TARGET_DB) || containTag(line, TARGET_SQL)
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
			logger.warn("导入案例出现错误,lineContent:[" + line + "],tagname:[" + tagName + "]," + e.getLocalizedMessage());
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
					logger.error(tagName + " error style.line：" + i + " [" + line + "]");
				}

				tagValue = arr[1].trim();
				if (tagValue.isEmpty()) {
					logger.error(tagName + " is empty.line：" + i + " [" + line + "]");
				}
				return tagValue;
			} else
				return null;
		} catch (Exception e) {
			logger.warn("导入案例出现错误,lineNum:[" + i + "],lineContent:[" + line + "],tagname:[" + tagName + "],"
					+ e.getLocalizedMessage());
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
		List<TestCase> list = caseRepository.findByNameAndSuite(testCaseName, testSuiteName);
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
	 * caseRepository.findByTestSuiteIdAndNameContaining(testSuiteID, "%" + name +
	 * "%", pageable); if (list.getSize() > 0) { return list; } else { return new
	 * PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0); }
	 * 
	 * }
	 */

	/*@Override
	public Page<TestCase> findBySuiteId(Integer testSuiteID, Pageable pageable) {
		Page<TestCase> list;
		if (testSuiteID != null && testSuiteID > 0) {
			list = caseRepository.findByTestSuiteId(testSuiteID, pageable);
		} else {
			list = caseRepository.findByTestSuiteIsNull(pageable);
		}
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0);
		}

	}*/

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
	public Page<TestCase> getAllTestCaseByPage(String name, EnumCompareDirection enumCompareDirection,
			Pageable pageable) {
		Page<TestCase> pageList = null;
		if (name == null || name.trim().length() == 0) {
			if (enumCompareDirection != null && !"".equals(enumCompareDirection)) {
				pageList = caseRepository.findAllByEnumCompareDirectionOrderByIdDesc(enumCompareDirection, pageable);
			} else {
				pageList = caseRepository.findAllOrderByIdDesc(pageable);
			}

		} else {
			if (enumCompareDirection != null && !"".equals(enumCompareDirection)) {
				pageList = caseRepository.findByNameLikeAndEnumCompareDirectionLikeOrderByIdDesc("%" + name + "%",
						enumCompareDirection, pageable);
			} else {
				pageList = caseRepository.findByNameLikeOrderByIdDesc("%" + name + "%", pageable);
			}

		}
		if (pageList.getSize() > 0) {
			return pageList;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0);
		}
	}

	@Override
	@Transactional
	public void changeTestCasePosition(Integer testSuiteID, String testCaseIDS) {
		if (testCaseIDS != null) {
			String[] ids = testCaseIDS.split(",");
			for (String testCaseID : ids) {
				testSuiteCaseRepository.changeTestCasePosition(testSuiteID, Integer.valueOf(testCaseID));
			}
		} else {
			System.out.println("请输入合法参数");
		}
	}

	
	
	@Override
	@Transactional
	public void backDisorder(String testCaseIDS,Integer suiteId) {
		if (testCaseIDS != null) {
			String[] ids = testCaseIDS.split(",");
			for (String testCaseID : ids) {
				Integer caseId = Integer.valueOf(testCaseID);
				TestSuiteCase tsc=	testSuiteCaseRepository.findByCaseIdAndSuiteId(caseId,suiteId);
				testSuiteCaseRepository.delete(tsc);
			}
		} else {
			System.out.println("输入合法参数");
		}
	}

	@Override
	@Transactional
	public void saveTestCaseVo(TestCaseVO testCaseVO) throws BizException {
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
		List<TestCase> qtc = caseRepository.findAllByNameLike(testCaseVO.getName());
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
		DataSource srcDataSource = dataSourceRepository.findById(srcDataSourceId).get();
		TestQuery sourceQuery = new TestQuery();
		TestQueryVO sourceQueryVO = testCaseVO.getSourceQuery();
		BeanUtils.copyProperties(sourceQueryVO, sourceQuery);
		srcDataSource.setId(srcDataSourceId);
		sourceQuery.setDataSource(srcDataSource);
		sourceQuery.setTestRules(null);
		testQueryRepository.save(sourceQuery);
		// 保存源查询的质量规则
		int sourcei = 0;
		List<TestRuleVO> testRules = testCaseVO.getSourceQuery().getTestRules();
		for (TestRuleVO testRuleVO : testRules) {

			CodeMap codeMap = codeMapRepository.findById(testRuleVO.getCodeMapId()).get();
			TestRule testRule = new TestRule();
			testRule.setCodeMap(codeMap);
			testRule.setTestQuery(sourceQuery);
			BeanUtils.copyProperties(testRuleVO, testRule);
			testRule.setPosition(sourcei++);
			testRuleRepository.save(testRule);
		}
		// 处理目标查询
		DataSource dstDatasource = dataSourceRepository.findById(dstDataSourceId).get();
		TestQuery targetQuery = new TestQuery();
		TestQueryVO targetQueryVO = testCaseVO.getTargetQuery();
		BeanUtils.copyProperties(targetQueryVO, targetQuery);
//		tardatasource.setId(dstDataSourceId);
		targetQuery.setDataSource(dstDatasource);
		targetQuery.setTestRules(null);
		testQueryRepository.save(targetQuery);

		// 保存目标查询的质量规则
		int targeti = 0;
		List<TestRuleVO> tartestRules = testCaseVO.getTargetQuery().getTestRules();
		for (TestRuleVO testRuleVO : tartestRules) {
			CodeMap codeMap = codeMapRepository.findById(testRuleVO.getCodeMapId()).get();
			TestRule testRule = new TestRule();
			testRule.setCodeMap(codeMap);
			testRule.setTestQuery(targetQuery);
			BeanUtils.copyProperties(testRuleVO, testRule);
			testRule.setPosition(targeti++);
			testRuleRepository.save(testRule);
		}

		// 保存案例
		TestCase testCase = new TestCase();
		BeanUtils.copyProperties(testCaseVO, testCase);
		fillTestCaseDefault(testCase);
		testCase.setSourceQuery(sourceQuery);
		testCase.setTargetQuery(targetQuery);
		if (testCaseVO.getTestSuiteID() != null) {
			TestSuite suite = new TestSuite();
			suite.setId(testCaseVO.getTestSuiteID());
//			testCase.setTestSuite(suite);
		}
		caseRepository.save(testCase);
	}

	private void fillTestCaseDefault(TestCase testCase) {
		if (testCase.getUsePage() == null)
			testCase.setUsePage(false);
		if (testCase.getCaseType() == null)
			testCase.setCaseType(EnumTestCaseType.DataCompare);// 当前的案例类型为数据比较
		if (testCase.getPageType() == null)
			testCase.setPageType(EnumPageType.None);
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
		// 处理新增名称不能重复
		List<TestCase> qtc = caseRepository.findAllByNameLike(testCaseVO.getName());
		for (TestCase testCase : qtc) {
			if (testCase.getId().equals(testCaseVO.getId()) && testCase.getName().equals(testCaseName)) {
				break;
			} else {
				throw new BizException("案例名称已存在");
			}
		}
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
		Optional<DataSource> srcDataSourceOp = dataSourceRepository.findById(srcDataSourceId);
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
			CodeMap codeMap = codeMapRepository.findById(testRuleVO.getCodeMapId()).get();
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
		Optional<DataSource> dstDataSourceOp = dataSourceRepository.findById(dstDataSourceId);
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
		List<TestRuleVO> tartestRules = testCaseVO.getTargetQuery().getTestRules();
		int tartesti = 0;
		for (TestRuleVO testRuleVO : tartestRules) {
			CodeMap codeMap = codeMapRepository.findById(testRuleVO.getCodeMapId()).get();
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
		TestCase testCase = new TestCase();
		BeanUtils.copyProperties(testCaseVO, testCase);
		fillTestCaseDefault(testCase);
		testCase.setSourceQuery(sourceQuery);
		testCase.setTargetQuery(targetQuery);
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
	public Page<QualityTestCase> findAllTestCaseByPage(String name, Pageable pageable) {
		Page<QualityTestCase> list = qualityTestCaseRepository.findAllTestCaseByPage(name, pageable);
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
	public void saveQualityTestCaseVo(QualityTestCaseVO testCaseVO) throws BizException {
		if (testCaseVO.getQualityTestQueryVo().getDataSourceId() == null) {
			throw new BizException("数据源为空");
		}
		if (testCaseVO.getQualityTestQueryVo().getTestFieldNames() == null) {
			throw new BizException("测试字段为空");
		}
		if (testCaseVO.getQualityTestQueryVo().getName() == null) {
			throw new BizException("testQuery名称为空");
		}
		QualityTestCase qualityTestCase = new QualityTestCase();
		QualityTestQuery qualityTestQuery = new QualityTestQuery();
		qualityTestQuery.setName(testCaseVO.getQualityTestQueryVo().getName());
		// testfield
		ArrayList<DataField> list = new ArrayList<DataField>();
		String testfieldNames = testCaseVO.getQualityTestQueryVo().getTestFieldNames();
		if (StringUtils.isNotBlank(testfieldNames)) {
			String[] names = testfieldNames.split(",");
			for (String name : names) {
				DataField testField = new DataField();
				testField.setName(name);
				testFieldRepository.save(testField);
				list.add(testField);
			}
		}
		qualityTestQuery.setDataFields(list);
		DataSource dataSource = dataSourceRepository.getOne(testCaseVO.getQualityTestQueryVo().getDataSourceId());
		qualityTestQuery.setDataSource(dataSource);
		qualityTestQueryRepository.save(qualityTestQuery);
		qualityTestCaseRepository.save(qualityTestCase);

	}

	@Override
	public void updateTestQualityCase(QualityTestCaseVO testCaseVO, Integer testCaseId) {
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
				QualityTestCase testCase = qualityTestCaseRepository.getOne(Integer.valueOf(id));
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
	 * caseRepository.findByTestSuiteIdNotInAndNameLike(testSuiteID, "%" + name +
	 * "%", pageable); }
	 */

	@Override
	public List<TestCase> findByTestSuiteId(Integer testSuiteID) {
		// TODO Auto-generated method stub
		return caseRepository.findByTestSuiteId(testSuiteID);
	}

	/*@Override
	public Page<TestCase> findByTestSuiteIdNotInAndNameContaining(Integer testSuiteID, String name, Pageable pageable) {
		Page<TestCase> list;
		list = caseRepository.findByTestSuiteIdNotInAndNameContaining(testSuiteID, "%" + name + "%", pageable);
		System.out.println("^^^^^^^^^^^^^^^^^^^^^^^^^");
		return list;
	}*/

	/*@Override
	public Page<TestCase> findBySuiteId(Integer testSuiteID, String name, Pageable pageable) {
		Page<TestCase> list;
		if (testSuiteID != null && testSuiteID > 0) {
			if ("".equals(name)) {
				list = caseRepository.findByTestSuiteId(testSuiteID, pageable);
			} else {
				list = caseRepository.findByTestSuiteIdAndNameContaining(testSuiteID, name, pageable);
			}

		} else {
			if ("".equals(name)) {
				list = caseRepository.findByTestSuiteIsNull(pageable);
			} else {
				list = caseRepository.findByTestSuiteIsNullAndNameContaining(name, pageable);
			}

		}
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0);
		}

	}*/

	/* 右侧案例
	 * @see com.jettech.service.ITestCaseService#findBySuiteId(java.lang.Integer, java.lang.String, org.springframework.data.domain.Pageable)
	 */
	/*@Override
	public Page<TestCase> findBySuiteId(Integer testSuiteID, String name, Pageable pageable) {
		Page<TestCase> list;
		if (name==null||name.equals("")) {
			// 名称为空时右侧的所有案例
			list = caseRepository.findByTestSuiteId(testSuiteID, pageable);
		} else {
			// 右侧根据id，名称查询所有
			list = caseRepository.findByTestSuiteIdAndNameContaining(testSuiteID, name, pageable);
		}
		if (list != null && list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0);
		}
	}*/

	/**
	 * 左侧的案例
	 *//*
	@Override
	public Page<TestCase> findALLBySuiteId(Integer testSuiteID, String name, Pageable pageable) {
		Page<TestCase> list;
		if (StringUtils.isBlank(name)) {
			// 名称为空，根据案例ID查询所有
			list = caseRepository.findByTestSuiteNotContain(testSuiteID, pageable);
		} else {
			// 名称不为空，根据三个条件查询所有
		list = caseRepository.findByTestSuiteIsNullAndNameContaining(testSuiteID, "%"+name+"%", pageable);
//			list = caseRepository.findByTestSuiteIsNullAndNameContaining(testSuiteID, name, pageable);
		}

		if (list != null && list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0);
		}
	}*/
	/**
	 * 左侧的案例
	 */
	@Override
	public Page<TestCase> findALLBySuiteId(Integer testSuiteID, String name, Pageable pageable) {
		Page<TestCase> list;
		if (StringUtils.isBlank(name)) {
			// 名称为空，根据案例ID查询所有
			list = caseRepository.findByTestSuiteNotContain(testSuiteID, pageable);
		} else {
			// 名称不为空，根据三个条件查询所有
		list = caseRepository.findByTestSuiteIsNullAndNameContaining(testSuiteID, "%"+name+"%", pageable);
//			list = caseRepository.findByTestSuiteIsNullAndNameContaining(testSuiteID, name, pageable);
		}

		if (list != null && list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0);
		}
	}

	
	/* 右侧案例
	 * @see com.jettech.service.ITestCaseService#findBySuiteId(java.lang.Integer, java.lang.String, org.springframework.data.domain.Pageable)
	 */
	@Override
	public Page<TestCase> findBySuiteId(Integer testSuiteID, String name, Pageable pageable) {
		Page<TestCase> list;
		if (StringUtils.isBlank(name)) {
			// 名称为空时右侧的所有案例
			list = caseRepository.findByTestSuiteId(testSuiteID, pageable);
		} else {
			// 右侧根据id，名称查询所有
			list = caseRepository.findByTestSuiteIdAndNameContaining(testSuiteID, "%"+name+"%", pageable);
		}
		if (list != null && list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0);
		}
	}

	@Override
	public Page<TestCase> findBySuiteId(Integer testSuiteID, Pageable pageable) {
		Page<TestCase> list;
		if (testSuiteID != null && testSuiteID > 0) {
			list = null/* caseRepository.findByTestSuiteId(testSuiteID, pageable) */;
		} else {
			list = null/* caseRepository.findByTestSuiteIsNull(pageable) */;
		}
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestCase>(new ArrayList<TestCase>(), pageable, 0);
		}

	}
	
	
}
