package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jettech.domain.CaseModel;
import com.jettech.domain.ModelCaseModel;
import com.jettech.entity.CaseModelSet;
import com.jettech.entity.CaseModelSetDetails;
import com.jettech.entity.DataTable;
import com.jettech.entity.DataSource;
import com.jettech.entity.ModelTestCase;
import com.jettech.entity.Product;
import com.jettech.entity.DataSchema;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestSuite;
import com.jettech.repostory.CaseModelSetDetailsRepository;
import com.jettech.repostory.CaseModelSetRepository;
import com.jettech.repostory.CodeMapRepository;
import com.jettech.repostory.DataSchemaRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.DataTableRepository;
import com.jettech.repostory.ModelTestCaseRepository;
import com.jettech.repostory.ProductRepository;
import com.jettech.repostory.TestQueryRepository;
import com.jettech.repostory.TestRuleRepository;
import com.jettech.repostory.TestSuiteCaseRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.ModelTestCaseService;
import com.jettech.thread.JobWorker;
import com.jettech.util.FileUtil;
import com.jettech.vo.CaseModelSetDetailsVO;
import com.jettech.vo.CaseModelSetVO;
import com.jettech.vo.ModelTestCaseVO;
import com.jettech.vo.ResultVO;

@Service
public class ModelTestCaseServiceImpl implements ModelTestCaseService {
	private static final String TARGET_KEY = "targetKey";

	private static final String TARGET_SQL = "targetSQL";

	private static final String TARGET_DB = "targetDB";

	private static final String SOURCE_KEY = "sourceKey";

	private static final String SOURCE_SQL = "sourceSQL";

	private static final String SOURCE_DB = "sourceDB";

	private static final String TEST_CASE_NAME = "testCaseName";

	Logger logger = LoggerFactory.getLogger(ModelTestCaseServiceImpl.class);

	@Autowired
	ModelTestCaseRepository caseRepository;
	@Autowired
	TestSuiteRepository testSutieRepository;
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
	CaseModelSetRepository caseModelSetRepository;
	@Autowired
	DataTableRepository testTableRepository;
	@Autowired
	CaseModelSetDetailsRepository caseModelSetDetailsRepository;
	@Autowired
	DataSchemaRepository testDatabaseRepository;
	@Autowired
	TestSuiteCaseRepository testSuiteCaseRepository;
	@Override
	public List<ModelTestCase> findAll() {
		return caseRepository.findAll();
	}

	@Override
	public List<ModelTestCase> saveAll(List<ModelTestCase> list) {
		return caseRepository.saveAll(list);
	}

	@Override
	public void save(ModelTestCase entity) {
		caseRepository.save(entity);
	}

	@Override
	@Transactional
	public void delete(Integer id) {
		ModelTestCase testCase = this.findById(id);
		CaseModelSet findByModelTestCase = caseModelSetRepository.findByModelTestCase(testCase);
		List<CaseModelSetDetails> findByCaseModelSet = caseModelSetDetailsRepository.findByCaseModelSet(findByModelTestCase);
		if(findByCaseModelSet.size()>0) {
			caseModelSetDetailsRepository.deleteAll(findByCaseModelSet);
			caseModelSetRepository.delete(findByModelTestCase);
		}
		String name = testCase.getName();
		caseRepository.delete(testCase);
		logger.info("删除案例成功：" + id + "_" + name);
	}

	@Override
	public ModelTestCase findById(Integer id) {
		Optional<ModelTestCase> optional = caseRepository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public String doTest(Integer testCaseId) {
		ModelTestCase testCase = caseRepository.findById(testCaseId).get();
		CaseModelSet modeCase=caseModelSetRepository.findByModelTestCase(testCase);
		if (testCase == null)
			return "not found testCase id:" + testCaseId;
		logger.info("ready doTestCase:" + testCase.getId() + "_" + testCase.getName());
		ModelCaseModel caseModel = null;
		try {
			caseModel=new ModelCaseModel(modeCase);
		} catch (Throwable e) {
			logger.error("cast testCase to model error." + testCase.getId() + "_" + testCase.getName());
			e.printStackTrace();
			return e.getMessage();
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
		String filePath = map.get("filePath");
		List<String> list = FileUtil.readFileToList(filePath, "UTF-8");
		StringBuffer  str=new StringBuffer();

		for (String string : list) {
			str.append(string);
		}
		List<ModelTestCaseVO> json=JSONArray.parseArray(str.toString(), ModelTestCaseVO.class);
		for (ModelTestCaseVO modelTestCaseVO : json) {
			this.saveTestCaseVo(modelTestCaseVO);
		}
		// createCase(list, testSuite);
//		result = createCaseWithQueue(list, testSuite);

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

		while (true) {
			if (queue.isEmpty())
				break;
			String line = queue.poll();
			if (line == null || line.trim().isEmpty())
				continue;
			System.out.println(line);

			if (containTag(line, TEST_CASE_NAME))
				testCaseName = getTagValue(line, TEST_CASE_NAME);
			if (testCaseName == null || testCaseName.trim().isEmpty())
				continue;

			if (containTag(line, SOURCE_DB))
				sourceDB = getTagValue(line, SOURCE_DB);
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

			if (containTag(line, SOURCE_KEY))
				sourceKey = getTagValue(line, SOURCE_KEY);
			if (sourceKey == null || sourceKey.trim().isEmpty()) {
				continue;
			}

			if (containTag(line, TARGET_DB))
				targetDB = getTagValue(line, TARGET_DB);
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

			if (containTag(line, TARGET_KEY))
				targetKey = getTagValue(line, TARGET_KEY);
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

			ModelTestCase testCase = null;
			
			List<ModelTestCase> caseList = caseRepository.findByNameAndSuite(testCaseName, testSuite.getName());
			if (caseList != null && caseList.size() > 0) {
				testCase = caseList.get(0);
			} else {
				testCase = new ModelTestCase();
			}
			testCase.setName(testCaseName);
			testCase.setTestSuite(testSuite);
			testCase.setUsePage(false);
			testCase.setIsSQLCase(true);

			TestQuery sourceQuery = new TestQuery();
			sourceQuery.setDataSource(dsSource);
			sourceQuery.setSqlText(sourceSQL);
			sourceQuery.setKeyText(sourceKey);
			// sourceQuery.setTestCase(testCase);

			TestQuery targetQuery = new TestQuery();
			targetQuery.setDataSource(dsTarget);
			targetQuery.setSqlText(targetSQL);
			targetQuery.setKeyText(targetKey);
			// targetQuery.setTestCase(testCase);

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

			ModelTestCase testCase = null;
			List<ModelTestCase> caseList = caseRepository.findByNameAndSuite(testCaseName, testSuite.getName());
			if (caseList != null && caseList.size() > 0) {
				testCase = caseList.get(0);
			} else {
				testCase = new ModelTestCase();
			}
			testCase.setName(testCaseName);
			testCase.setTestSuite(testSuite);
			testCase.setUsePage(false);
			testCase.setIsSQLCase(true);

			TestQuery sourceQuery = new TestQuery();
			sourceQuery.setDataSource(dsSource);
			sourceQuery.setSqlText(sourceSQL);
			sourceQuery.setKeyText(sourceKey);

			TestQuery targetQuery = new TestQuery();
			targetQuery.setDataSource(dsTarget);
			targetQuery.setSqlText(targetSQL);
			targetQuery.setKeyText(targetKey);

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
		List<TestSuite> testSuiteList = testSutieRepository.findByName(testSuiteName, productName);
		if (testSuiteList != null && testSuiteList.size() > 0) {
			testSuite = testSuiteList.get(0);
		} else {
			testSuite = new TestSuite();
			testSuite.setProduct(product);
			testSuite.setName(testSuiteName);
			testSuite = testSutieRepository.save(testSuite);
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
	}

	private String getTagValue(int i, String line, String tagName) {
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
	}

	@SuppressWarnings("unused")
	private String getTagValue(Queue<String> queue, String tagName) {
		while (true) {
			// String line = list.get(i);
			String line = queue.poll();
			if (line == null || line.trim().isEmpty()) {
				// i++;
				continue;
			}
			String tagValue = getTagValue(line, tagName);
		}
	}

	@Override
	public ModelTestCase findByNameAndSuite(String testCaseName, String testSuiteName) {
		List<ModelTestCase> list = caseRepository.findByNameAndSuite(testCaseName, testSuiteName);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}

	public List<ModelTestCase> findByName(String name) {
		List<ModelTestCase> list = caseRepository.findByName(name);
		if (list.size() > 0) {
			return list;
		} else {
			return null;
		}

	}

	@Override
	public Page<ModelTestCase> findBySuiteId(Integer testSuiteID, Pageable pageable) {

		Page<ModelTestCase> list;
		if (testSuiteID != null && testSuiteID > 0) {
			list = caseRepository.findByTestSuiteId(testSuiteID, pageable);
		} else {
			list = caseRepository.findByTestSuiteIdIsNull(testSuiteID, pageable);
		}
		if (list.getSize() > 0) {
			return list;
		} else {
			return null;
		}

	}

	/**
	 * 根据测试集名称查询所有案例 20190121
	 * 
	 * @param suiteName
	 * @return
	 */
	@Override
	public Page<ModelTestCase> findBySuiteName(String suiteName, Pageable pageable) {
		// 新的方法 20190123
		return caseRepository.findBysuiteName("%" + suiteName + "%", pageable);
	}

	@Override
	public Page<ModelTestCase> findAllByPage(Pageable pageable) {
		return caseRepository.findAll(pageable);
	}

	@Override
	public String exportTestCase(String testCaseIds) {
		List<ModelTestCaseVO> voList=new ArrayList<>();
		if (testCaseIds != null) {
			String[] list = testCaseIds.split(",");
			System.out.println("list.size()");
			for (int i = 0; i < list.length; i++) {
				int id = Integer.parseInt(list[i]);
				ModelTestCaseVO testCaseDetail = this.getTestCaseDetail(id);
				voList.add(testCaseDetail);
			}
		} 
		String  json=JSONArray.toJSONString(voList);
		return json;
	}

	@Override
	public ModelTestCaseVO getTestCaseDetail(Integer testCaseID) {
		// TODO Auto-generated method stub
		// 查询案例详情
		ModelTestCase testCase = caseRepository.findById(testCaseID).get();
		ModelTestCaseVO testCaseVO = new ModelTestCaseVO();
		BeanUtils.copyProperties(testCase, testCaseVO);
		// 查询模型集
		CaseModelSet caseModelSet = caseModelSetRepository.findByModelTestCase(testCase);
		CaseModelSetVO caseModelSetVO = new CaseModelSetVO();
		BeanUtils.copyProperties(caseModelSet, caseModelSetVO);
		caseModelSetVO.setDatumModelSetId(caseModelSet.getDatumModelSet().getId());
		caseModelSetVO.setTestModelSetId(caseModelSet.getDataSchema().getId());
		// 查询表关系对应
		List<CaseModelSetDetailsVO> caseModelSetDetailsList = new ArrayList<>();
		List<CaseModelSetDetails> CaseModelSetDetailsList = caseModelSetDetailsRepository
				.findByCaseModelSet(caseModelSet);
		for (CaseModelSetDetails caseModelSetDetails : CaseModelSetDetailsList) {
			CaseModelSetDetailsVO vo = new CaseModelSetDetailsVO();
			BeanUtils.copyProperties(caseModelSetDetails, vo);
			vo.setDatumModelSetTableId(caseModelSetDetails.getDatumModelSetTable().getId());
			vo.setTestModelSetTableId(caseModelSetDetails.getTestModelSetTable().getId());
			vo.setDatumModelSetTableName(caseModelSetDetails.getDatumModelSetTable().getName());
			vo.setTestModelSetTableName(caseModelSetDetails.getTestModelSetTable().getName());
			caseModelSetDetailsList.add(vo);
		}
		caseModelSetVO.setDetailsList(caseModelSetDetailsList);
		testCaseVO.setCaseModelSetVO(caseModelSetVO);
		return testCaseVO;
	}

	@Override
	public List<ModelTestCase> findAllTestCase(Integer testSuiteID) {
		List<ModelTestCase>list=null;
		Integer[] caseIds = testSuiteCaseRepository.findCaseIdsBysuiteId(testSuiteID);
		for (Integer caseId : caseIds) {
			ModelTestCase testCase = caseRepository.findById(caseId).get();
			list.add(testCase);
		}
		return list;
	}

	@Override
	public Page<ModelTestCase> getAllTestCaseByPage(String name, Pageable pageable) {
		Page<ModelTestCase> pageList = caseRepository.getAllTestCaseByPage(name, pageable);
		if (pageList.getSize() > 0) {
			return pageList;
		} else {
			return null;
		}

	}

	@Override
	public void changeTestCasePosition(Integer testSuiteID, String testCaseIDS) {
		if (testCaseIDS != null) {
			String[] ids = testCaseIDS.split(",");
			for (String testCaseID : ids) {
				caseRepository.changeTestCasePosition(testSuiteID, Integer.valueOf(testCaseID));
			}
		} else {
			System.out.println("请输入合法参数");
		}
	}

	@Override
	public void backDisorder(String testCaseIDS) {
		if (testCaseIDS != null) {
			String[] ids = testCaseIDS.split(",");
			for (String testCaseID : ids) {
				caseRepository.backDisorder(Integer.valueOf(testCaseID));
			}
		} else {
			System.out.println("输入合法参数");
		}
	}

	@Override
	public void saveTestCaseVo(ModelTestCaseVO testCaseVO) {
		// 保存案例
		ModelTestCase testCase = new ModelTestCase();
		BeanUtils.copyProperties(testCaseVO, testCase);
		caseRepository.save(testCase);
		// 保存模型集
		CaseModelSet caseModelSet = new CaseModelSet();
		CaseModelSetVO caseModelSetVO = testCaseVO.getCaseModelSetVO();
		BeanUtils.copyProperties(caseModelSetVO, caseModelSet);
		if(caseModelSetVO.getDatumModelSetId()!=null && caseModelSetVO.getTestModelSetId()!=null) {
		DataSchema datumModel = testDatabaseRepository.findById(caseModelSetVO.getDatumModelSetId()).get();
		DataSchema testModel = testDatabaseRepository.findById(caseModelSetVO.getTestModelSetId()).get();
		caseModelSet.setDatumModelSet(datumModel);
		caseModelSet.setDataSchema(testModel);
		caseModelSet.setModelTestCase(testCase);
		caseModelSetRepository.save(caseModelSet);
		// 保存表对应关系
		List<CaseModelSetDetailsVO> detailsList = caseModelSetVO.getDetailsList();
			for (CaseModelSetDetailsVO caseModelSetDetailsVO : detailsList) {
				if (caseModelSetDetailsVO.getDatumModelSetTableId() == null
						|| caseModelSetDetailsVO.getTestModelSetTableId() == null) {
					continue;
				}
				DataTable datumModelSet = testTableRepository.getOne(caseModelSetDetailsVO.getDatumModelSetTableId());
				DataTable testModelSet = testTableRepository.getOne(caseModelSetDetailsVO.getTestModelSetTableId());
				CaseModelSetDetails caseModelSetDetails = new CaseModelSetDetails();
				BeanUtils.copyProperties(caseModelSetDetailsVO, caseModelSetDetails);
				caseModelSetDetails.setDatumModelSetTable(datumModelSet);
				caseModelSetDetails.setTestModelSetTable(testModelSet);
				caseModelSetDetails.setCaseModelSet(caseModelSet);
				caseModelSetDetailsRepository.save(caseModelSetDetails);
			}
		}
	}

	@Override
	public void updateTestCase(ModelTestCaseVO testCaseVO) {
		// 保存案例
		ModelTestCase testCase = new ModelTestCase();
		BeanUtils.copyProperties(testCaseVO, testCase);
		caseRepository.save(testCase);
		// 保存模型集
		CaseModelSet caseModelSet = new CaseModelSet();
		CaseModelSetVO caseModelSetVO = testCaseVO.getCaseModelSetVO();
		BeanUtils.copyProperties(caseModelSetVO, caseModelSet);
		DataSchema datumModel = testDatabaseRepository.findById(caseModelSetVO.getDatumModelSetId()).get();
		DataSchema testModel = testDatabaseRepository.findById(caseModelSetVO.getTestModelSetId()).get();

		caseModelSet.setDatumModelSet(datumModel);
		caseModelSet.setDataSchema(testModel);
		caseModelSet.setModelTestCase(testCase);
		caseModelSetRepository.save(caseModelSet);
		// 保存表对应关系
		List<CaseModelSetDetailsVO> detailsList = caseModelSetVO.getDetailsList();
		for (CaseModelSetDetailsVO caseModelSetDetailsVO : detailsList) {
			if(caseModelSetDetailsVO.getDatumModelSetTableId()==null|| caseModelSetDetailsVO.getTestModelSetTableId()==null) {
				continue;
			}
			DataTable datumModelSet = testTableRepository.getOne(caseModelSetDetailsVO.getDatumModelSetTableId());
			DataTable testModelSet = testTableRepository.getOne(caseModelSetDetailsVO.getTestModelSetTableId());
			CaseModelSetDetails caseModelSetDetails = new CaseModelSetDetails();
			BeanUtils.copyProperties(caseModelSetDetailsVO, caseModelSetDetails);
			caseModelSetDetails.setDatumModelSetTable(datumModelSet);
			caseModelSetDetails.setTestModelSetTable(testModelSet);
			caseModelSetDetails.setCaseModelSet(caseModelSet);
			caseModelSetDetailsRepository.save(caseModelSetDetails);
		}
	}

}
