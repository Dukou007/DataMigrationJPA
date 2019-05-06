package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.transaction.Transactional;

import com.jettech.domain.QualityTestCaseModel;
import com.jettech.thread.JobWorker;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONArray;
import com.jettech.BizException;
import com.jettech.domain.QualityCaseModel;
import com.jettech.entity.DataField;
import com.jettech.entity.DataSource;
import com.jettech.entity.Product;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.QualityTestQuery;
import com.jettech.entity.TestSuite;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.ProductRepository;
import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.QualityTestPointRepository;
import com.jettech.repostory.QualityTestQueryRepository;
import com.jettech.repostory.TestRuleRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.IQualityTestCaseService;
import com.jettech.util.FileUtil;
import com.jettech.vo.QualityTestCaseVO;
import com.jettech.vo.ResultVO;

@Service
public class QualityTestCaseServiceImpl implements IQualityTestCaseService {
	Logger logger = LoggerFactory.getLogger(QualityTestCaseServiceImpl.class);

	@Autowired
	QualityTestCaseRepository caseRepository;
	@Autowired
	TestSuiteRepository testSutieRepository;
	@Autowired
	ProductRepository productRepository;
	@Autowired
	DataSourceRepository dataSourceRepository;
	@Autowired
	QualityTestQueryRepository qualityTestQueryRepository;
	@Autowired
	QualityTestPointRepository qualityTestPointRepository;
	@Autowired
	TestRuleRepository testRuleRepository;
	@Autowired
	DataFieldRepository testFieldRepository;
	@Autowired
	TestSuiteRepository testSuiteRepository;

	@Override
	public List<QualityTestCase> findAll() {
		return caseRepository.findAll();
	}

	@Override
	public List<QualityTestCase> saveAll(List<QualityTestCase> list) {
		return caseRepository.saveAll(list);
	}

	@Override
	public void save(QualityTestCase entity) {
		caseRepository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		caseRepository.deleteById(id);
	}

	@Override
	public QualityTestCase getOneById(Integer id) {
		return caseRepository.getOne(id);
	}

	@Override
	@Transactional
	public String doTest(Integer testCaseId) {
		QualityTestCase qualityTestCase = caseRepository.getOne(testCaseId);
		if (qualityTestCase == null)
			return "not found qualityTestCase id:" + testCaseId;
		logger.info("ready doTestCase:" + qualityTestCase.getId() + "_" + qualityTestCase.getName());
		// QualityCaseModel caseModel = null;
		QualityTestCaseModel caseModel = null;
		try {
			caseModel = new QualityTestCaseModel(qualityTestCase);
			// 特殊的：以下属性不能复制,手工复制一次(暂未查明原因)
//			caseModel.parseEntity(qualityTestCase);

			// caseModel.setSourceQuery(new
			// QueryModel(qualityTestCase.getSourceQuery()));
			// List<FieldModel> sourceKeyFields = new ArrayList<FieldModel>();
			// for (TestQueryField queryField :
			// qualityTestCase.getSourceQuery().getKeyFields()) {
			// FieldModel fieldModel = new FieldModel(queryField);
			// sourceKeyFields.add(fieldModel);
			// }
			// QueryModel sourceQueryModel = caseModel.getSourceQuery();
			// sourceQueryModel.setKeyFields(sourceKeyFields);
			// List<FieldModel> sourceTestFields =new ArrayList<>();
			// for (TestQueryField queryField :
			// qualityTestCase.getSourceQuery().getTestFields()) {
			// FieldModel fieldModel = new FieldModel(queryField);
			// sourceTestFields.add(fieldModel);
			// }
			// sourceQueryModel.setTestFields(sourceTestFields);
			//
			// qualityTestCase.getSourceQuery().getTestRules();
			//
			//
			// caseModel.setTargetQuery(new
			// QueryModel(qualityTestCase.getTargetQuery()));
			// List<FieldModel> targetKeyFields = new ArrayList<FieldModel>();
			// for (TestQueryField queryField :
			// qualityTestCase.getTargetQuery().getKeyFields()) {
			// FieldModel fieldModel = new FieldModel(queryField);
			// targetKeyFields.add(fieldModel);
			// }
			// caseModel.getTargetQuery().setKeyFields(targetKeyFields);

		} catch (Throwable e) {
			logger.error(
					"cast qualityTestCase to model error." + qualityTestCase.getId() + "_" + qualityTestCase.getName());
			e.printStackTrace();
			return e.getMessage();
		}
		JobWorker job = new JobWorker(caseModel);
		Thread thread = new Thread(job);
		thread.start();
		return null;
	}

	@Override
	public ResultVO readSQLCase(Map<String, String> map) throws BizException {
		ResultVO result = new ResultVO();
		result.setFlag(true);

		if (map == null || map.size() == 0)
			throw new IllegalArgumentException("readSQLCase argument is null or empty.");
		String filePath = map.get("filePath");
		List<String> list = FileUtil.readFileToList(filePath, "UTF-8");
		StringBuffer str = new StringBuffer();

		for (String string : list) {
			str.append(string);
		}
		List<QualityTestCaseVO> json = JSONArray.parseArray(str.toString(), QualityTestCaseVO.class);
		for (QualityTestCaseVO testCaseVO : json) {
			this.saveQualityTestCaseVo(testCaseVO);
		}

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

	@Override
	public QualityTestCase findByNameAndSuite(String testCaseName, String testSuiteName) {
		List<QualityTestCase> list = caseRepository.findByNameAndSuite(testCaseName, testSuiteName);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}

	@Override
	public QualityTestCase findByName(String name) {
		List<QualityTestCase> list = caseRepository.findByName(name);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}

	@Override
	public Page<QualityTestCase> findTestCaseByName(String name, Pageable pageable) {
		Page<QualityTestCase> list = caseRepository.findTestCaseByName(name, pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<>(new ArrayList<QualityTestCase>(), pageable, 0);
		}
	}


	@Override
	public void batchDelete(String testCaseIDs) {
		if (StringUtils.isNotBlank(testCaseIDs)) {
			String[] ids = testCaseIDs.split(",");
			for (String id : ids) {
				caseRepository.deleteById(Integer.parseInt(id));
			}
		}

	}

	@Override
	public List<QualityTestCase> findByCaseIDs(String ids) {
		ArrayList<QualityTestCase> list = new ArrayList<QualityTestCase>();
		if (StringUtils.isNotBlank(ids)) {
			String[] testCaseIDs = ids.split(",");
			for (String id : testCaseIDs) {
				QualityTestCase qualityTestCase = caseRepository.getOne(Integer.valueOf(id));
				list.add(qualityTestCase);
			}
			return list;
		} else {
			List<QualityTestCase> all = caseRepository.findAll();
			return all;
		}
	}

	@Override
	public QualityTestCase findById(Integer id) {
		return caseRepository.getOne(id);
	}

	@Override
	public Page<QualityTestCase> findAllByPage(Pageable pageable) {
		return caseRepository.findAll(pageable);
	}

	@Override
	public Page<QualityTestCase> findAllTestCaseByPage(String name, Pageable pageable) {
		Page<QualityTestCase> list = null;
		if (name == null) {
			list = caseRepository.findAllByOrderByIdDesc(pageable);
		} else {

			list = caseRepository.findByNameLike("%" + name + "%", pageable);
		}
		if (list != null) {
			return list;
		} else {
			return new PageImpl<QualityTestCase>(new ArrayList<QualityTestCase>(), pageable, 0);
		}
	}

	@Override
	public QualityTestCase getOne(Integer id) {
		return caseRepository.getOne(id);
	}

	@Override
	public List<QualityTestCase> findByQualityCaseIDs(String ids) {
		ArrayList<QualityTestCase> list = new ArrayList<QualityTestCase>();
		if (StringUtils.isNotBlank(ids)) {
			String[] testCaseIDs = ids.split(",");
			for (String id : testCaseIDs) {
				QualityTestCase testCase = caseRepository.getOne(Integer.valueOf(id));
				list.add(testCase);
			}
			return list;
		} else {
			List<QualityTestCase> all = caseRepository.findAll();
			return all;
		}
	}

	@Override
	public void updateTestQualityCase(QualityTestCaseVO testCaseVO) throws BizException {
		QualityTestCase testCase = caseRepository.findById(testCaseVO.getId()).get();
		// 处理新增名称不能重复
		List<QualityTestCase> qtc = caseRepository.findByCaseName(testCaseVO.getName());
		for (QualityTestCase tc : qtc) {
			if (tc.getId().equals(testCaseVO.getId()) && tc.getName().equals(testCaseVO.getName())) {
				break;
			} else {
				throw new BizException("案例名称已存在");
			}
		}
		BeanUtils.copyProperties(testCaseVO, testCase);
		QualityTestQuery testQuery = qualityTestQueryRepository.findById(testCase.getQualityTestQuery().getId()).get();
		testQuery.setName(testCaseVO.getQualityTestQueryVo().getName());
		testQuery.setSqlText(testCaseVO.getQualityTestQueryVo().getSqlText());
		testQuery.setDataSource(dataSourceRepository.getOne(testCaseVO.getQualityTestQueryVo().getDataSourceId()));
		List<DataField> testFields = testFieldRepository.findAllByTestQueryId(testCase.getQualityTestQuery().getId());
		String names = testCaseVO.getQualityTestQueryVo().getTestFieldNames();
		String[] nameList = names.split(",");
		for (int i = 0; i < testFields.size(); i++) {
			testFields.get(i).setName(nameList[i]);
			testFieldRepository.save(testFields.get(i));
		}
		testQuery.setDataFields(testFields);
		qualityTestQueryRepository.save(testQuery);
		testCase.setQualityTestQuery(testQuery);
		caseRepository.save(testCase);

	}

	@Override
	@Transactional
	public void saveQualityTestCaseVo(QualityTestCaseVO testCaseVO) throws BizException {
		if (testCaseVO.getQualityTestQueryVo().getDataSourceId() == null) {
			throw new BizException("数据源为空");
		}
		if (testCaseVO.getQualityTestQueryVo().getTestFieldNames() == null) {
			throw new BizException("测试字段为空");
		}
		if (testCaseVO.getQualityTestQueryVo().getName() == null) {
			throw new BizException("testquery名称为空");
		}
		// 处理新增名称不能重复
		List<QualityTestCase> qtc = caseRepository.findByCaseName(testCaseVO.getName());
		if (qtc != null && qtc.size() > 0) {
			throw new BizException("案例名称已存在");
		}

		QualityTestCase qualityTestCase = new QualityTestCase();
		QualityTestQuery qualityTestQuery = new QualityTestQuery();
		ArrayList<DataField> list = new ArrayList<DataField>();
		String testfieldNames = testCaseVO.getQualityTestQueryVo().getTestFieldNames();
		if (StringUtils.isNotBlank(testfieldNames)) {
			String[] names = testfieldNames.split(",");
			for (String name : names) {
				DataField testField = new DataField();
				testField.setName(name);
				testField.setQualityTestQuery(qualityTestQuery);
				testFieldRepository.save(testField);
				list.add(testField);
			}
		}
		qualityTestQuery.setDataFields(list);
		DataSource dataSource = dataSourceRepository.getOne(testCaseVO.getQualityTestQueryVo().getDataSourceId());
		qualityTestQuery.setDataSource(dataSource);
		qualityTestQuery.setSqlText(testCaseVO.getQualityTestQueryVo().getSqlText());
		qualityTestQuery.setName(testCaseVO.getQualityTestQueryVo().getName());
		qualityTestQueryRepository.save(qualityTestQuery);
		// 处理新建和修改时间
		qualityTestCase.setCreateTime(new Date());
		qualityTestCase.setEditTime(new Date());

		qualityTestCase.setQualityTestQuery(qualityTestQuery);
		for (DataField dataField : list) {
			dataField.setQualityTestQuery(qualityTestQuery);
		}
		BeanUtils.copyProperties(testCaseVO, qualityTestCase);

		caseRepository.save(qualityTestCase);

	}

	@Override
	@Transactional
	//public void changeTestCasePosition(Integer caseId, Integer suiteId) {
	public void changeTestCasePosition(List<Integer> caseId, Integer suiteId) {
		TestSuite testSuite = testSutieRepository.getOne(suiteId);
		//QualityTestCase testCase = caseRepository.getOne(caseId);
	//	testCase.setTestSuite(testSuite);
		//修改为多对对关系liu
		for (Integer id : caseId){
			QualityTestCase testCase = caseRepository.getOne(id);
			testSuite.getQualityTestCases().remove(testCase);
			testSuite.getQualityTestCases().add(testCase);
		}
		testSuiteRepository.saveAndFlush(testSuite);

	/*	caseRepository.saveAndFlush(testCase);
		testSuiteRepository.saveAndFlush(testSuite);*/

	}

	@Override
	@Transactional
	public void backDisorder(List<Integer> caseId, Integer suiteId) {
	//	QualityTestCase testCase = caseRepository.findByCaseIdAndSuiteId(caseId, suiteId);
		//testCase.setTestSuite(null);
		//修改为多对对关系liu
		TestSuite testSuite = testSutieRepository.getOne(suiteId);
		for (Integer id : caseId){
			QualityTestCase testCase = caseRepository.getOne(id);
			testSuite.getQualityTestCases().remove(testCase);
		}
		/*List<TestSuite> testSuites = new ArrayList();
		testCase.setTestSuites(testSuites);*/
		testSuiteRepository.saveAndFlush(testSuite);

	//	caseRepository.saveAndFlush(testCase);
	}

	public List<QualityTestCase> findByTestSuitIdAndRoundId(Integer test_suit_id, Integer test_round_id) {
		return caseRepository.findByTestSuitIdAndRoundId(test_suit_id, test_round_id);
	}

	@Override
	public Page<QualityTestCase> findByNotSuiteId(Integer suiteId, Pageable pageable) {
		return caseRepository.findByNotSuiteId( suiteId, pageable);
	}

	@Override
	public Page<QualityTestCase> findByNotCaseName(Integer suiteId, String name, Pageable pageable) {
		return caseRepository.findByNotCaseName(suiteId,name,pageable);
	}

	@Override
	public Page<QualityTestCase> findCaseBySuiteId(Integer suiteId, Pageable pageable) {

		return caseRepository.findCaseBySuiteId(suiteId,pageable);
	}

	@Override
	public Page<QualityTestCase> findByCaseName(Integer suiteId, String name, Pageable pageable) {
		return caseRepository.findCaseByName(suiteId,name,pageable);
	}

}
