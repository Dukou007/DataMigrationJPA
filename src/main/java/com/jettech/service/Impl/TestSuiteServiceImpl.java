package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.BizException;
import com.jettech.domain.CaseModel;
import com.jettech.domain.QualityTestCaseModel;
import com.jettech.entity.Product;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestRound;
import com.jettech.entity.TestRule;
import com.jettech.entity.TestSuite;
import com.jettech.entity.TestSuiteCase;
import com.jettech.repostory.ProductRepository;
import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.TestCaseRepository;
import com.jettech.repostory.TestRoundRepository;
import com.jettech.repostory.TestSuiteCaseRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.ITestCaseService;
import com.jettech.service.TestSuiteService;
import com.jettech.thread.JobWorker;
import com.jettech.vo.TestCaseVO;
import com.jettech.vo.TestQueryVO;
import com.jettech.vo.TestRuleVO;
import com.jettech.vo.TestSuiteVO;

@Service
public class TestSuiteServiceImpl implements TestSuiteService {

	Logger logger = LoggerFactory.getLogger(TestSuiteServiceImpl.class);
	@Autowired
	private TestSuiteRepository repository;
	@Autowired
	private ProductRepository productRepository;
	@Autowired
	private TestCaseRepository testCaseRepository;
	@Autowired
	private ITestCaseService testCaseService;
	@Autowired
	TestRoundRepository testRoundRepository;
	@Autowired
	private TestSuiteCaseRepository testSuiteCaseRepository;
	@Autowired
	QualityTestCaseRepository qualityTestCaseRepository;

	@Override
	public List<TestSuite> findAll() {
		return repository.findAll();
	}

	@Override
	public List<TestSuite> saveAll(List<TestSuite> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(TestSuite entity) {
		repository.save(entity);
	}

	@Override
	@Transactional
	public void delete(String ids) {
		String[] testSuiteIds = ids.split(",");
		for (String testSuiteId : testSuiteIds) {
			// 找到对象---删除关系---删除对象
			int suiteId = Integer.parseInt(testSuiteId);
			TestSuite testSuite = repository.getOne(suiteId);
			//迁移&&质量
			List<TestSuiteCase> list = testSuiteCaseRepository.findBySuiteId(suiteId);
			for (TestSuiteCase tsc : list) {
				testSuiteCaseRepository.delete(tsc);
			}
			//质量
			/*List<QualityTestCase>qualityTestCaseList=qualityTestCaseRepository.findByTestSuiteId(suiteId);
			for (QualityTestCase qualityTestCase : qualityTestCaseList) {
				qualityTestCase.getTestSuites().remove(testSuite);
				qualityTestCaseRepository.save(qualityTestCase);
			}*/
			repository.delete(testSuite);
		}

	}

	@Override
	public TestSuite findById(Integer id) {
		TestSuite testSuite = repository.getOne(id);
		return testSuite;
	}

	@Override
	@Transactional
	public String doTestSuite(Integer testSuiteId) {
		TestSuite testSuite = repository.getOne(testSuiteId);
		if (testSuite == null) {
			return "not found testSuite id:" + testSuiteId;
		}
		List<CaseModel> caseModelList = new ArrayList<>();
		ArrayList<TestCase> caseList = new ArrayList<TestCase>();
		Integer[] caseIds = testSuiteCaseRepository.findCaseIdsBysuiteId(testSuiteId);
		for (Integer caseId : caseIds) {
			TestCase testCase = testCaseRepository.getOne(caseId);
			caseList.add(testCase);
		}
		for (TestCase testCase : caseList) {
			CaseModel caseModel;
			try {
				caseModel = new CaseModel(testCase);
				caseModel.setId(testCase.getId());
				caseModel.setName(testCase.getName());
				caseModelList.add(caseModel);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("cast testCase to model error." + testCase.getId() + "_" + testCase.getName());
			}
			// 特殊的：以下属性不能复制,手工复制一次(暂未查明原因)
		}
		ExecutorService executor = null;
		executor = Executors.newFixedThreadPool(5);// 线程数量
		for (CaseModel caseModel : caseModelList) {
			JobWorker job = new JobWorker(caseModel);
			executor.execute(job);
		}
		executor.shutdown();

		return null;
	}

	@Override
	public TestSuite getByName(String name) {
		List<TestSuite> list = repository.findByName(name);
		TestSuite entity = null;
		if (list == null || list.size() == 0) {
			entity = new TestSuite();
			entity = repository.save(entity);
		} else {
			entity = list.get(0);
		}
		return entity;
	}

	@Override
	public TestSuite getByName(String testSuiteName, String productName) {
		List<TestSuite> list = repository.findByName(testSuiteName, productName);
		TestSuite entity = null;
		if (list == null || list.size() == 0) {
			entity = new TestSuite();
			entity.setName(testSuiteName);
			Product product = null;
			List<Product> productList = productRepository.findByName(productName);
			if (productList != null && productList.size() > 0) {
				product = productList.get(0);
			} else {
				product = new Product();
				product.setName(productName);
				product = productRepository.save(product);
			}
			entity.setProduct(product);
			entity = repository.save(entity);
		} else {
			entity = list.get(0);
		}
		return entity;
	}

	@Override
	public Page<TestSuite> getByNameLikeList(String name, Pageable pageable) {
		return repository.findByNameLike(name, pageable);
	}

	@Override
	public Page<TestSuite> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	public List<TestSuite> findByProductIdI(Integer productId) {
		List<TestSuite> list = new ArrayList<TestSuite>();
		Product product = productRepository.getOne(productId);
		if (product == null) {
			logger.warn("not found product,id:" + productId);
			return list;
		} else {
			addProductSuites(list, product);
		}
		return list;
	}

	public List<TestSuiteVO> findByProductId(Integer productId) {
		List<TestSuite> findByProductId = repository.findByProductId(productId);
		List<TestSuiteVO> listVO = new ArrayList<TestSuiteVO>();
		for (TestSuite testSuite : findByProductId) {
			TestSuiteVO vo = new TestSuiteVO();
			BeanUtils.copyProperties(testSuite, vo);
			listVO.add(vo);
		}

		return listVO;
	}

	private void addProductSuites(List<TestSuite> list, Product product) {
		list.addAll(product.getTestSuites());
		for (Product sub : product.getSubProducts()) {
			addProductSuites(list, sub);
		}
	}

	@Override
	public List<TestSuite> findTestSuiteByName(String name) {
		return repository.finTestSuiteByName(name);
	}

	@Override
	public Page<TestSuite> findByNameLike(String name, Pageable pageable, int type) {
		Page<TestSuite> list = repository.findByNameAndTypeByPage("%" + name + "%", type, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return new PageImpl<TestSuite>(new ArrayList<TestSuite>(), pageable, 0);
		}

	}

	@Override
	public Page<TestSuite> getAllTestSuiteByProductID(Integer productID, Pageable pageable) {

		Page<TestSuite> list = repository.getAllTestSuiteByProductID(productID, pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestSuite>(new ArrayList<TestSuite>(), pageable, 0);
		}

	}

	@Override
	public Page<TestSuite> getTestSuiteList(Pageable pageable) {
		Page<TestSuite> list = repository.getTestSuiteList(pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestSuite>(new ArrayList<TestSuite>(), pageable, 0);
		}

	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@SuppressWarnings("unused")
	@Override
	public String doTest(Integer testSuiteID) {
		TestSuite testSuite = repository.getOne(testSuiteID);
		if (testSuiteID != null) {
			List<TestCase> testCaseList = testCaseRepository.findAllTestCase(testSuiteID);
			for (TestCase testCase : testCaseList) {
				Integer testCaseId = testCase.getId();
				doTest(testCaseId);
			}
			return "开始执行";
		} else {
			return "请输入合法参数";
		}

	}

	@Override
	public void copyTestSuite(Integer testSuiteId) throws BizException {
		TestSuite testSuite = repository.findById(testSuiteId).get();
		ArrayList<TestCase> caseList = new ArrayList<TestCase>();
		Integer[] caseIds = testSuiteCaseRepository.findCaseIdsBysuiteId(testSuiteId);
		for (Integer caseId : caseIds) {
			TestCase testCase = testCaseRepository.getOne(caseId);
			caseList.add(testCase);
		}
		TestSuite newtestSuite = new TestSuite();
		Product product = new Product();
		product.setId(testSuite.getProduct().getId());
		newtestSuite.setName(testSuite.getName());
		newtestSuite.setProduct(product);
		repository.save(newtestSuite);
		for (TestCase testCase : caseList) {
			// 复制案例
			TestCaseVO newtestCase = new TestCaseVO();
			newtestCase.setName(testCase.getName());
			newtestCase.setVersion(testCase.getVersion());
			newtestCase.setIsSQLCase(testCase.getIsSQLCase());
			newtestCase.setMaxResultRows(testCase.getMaxResultRows());
			newtestCase.setUsePage(testCase.getUsePage());
			newtestCase.setPageSize(testCase.getPageSize());
			TestQuery sourceQuery = testCase.getSourceQuery();
			TestQueryVO sourceQueryVO = new TestQueryVO();
			sourceQueryVO.setName(sourceQuery.getName());
			sourceQueryVO.setSqlText(sourceQuery.getSelectText());
			sourceQueryVO.setKeyText(sourceQuery.getKeyText());
			sourceQueryVO.setPageText(sourceQuery.getPageText());
			sourceQueryVO.setDataSourceId(sourceQuery.getDataSource().getId());
			List<TestRule> testRules = sourceQuery.getTestRules();
			List<TestRuleVO> sourceRuleVO = new ArrayList<TestRuleVO>();
			for (TestRule testRule : testRules) {
				TestRuleVO vo = new TestRuleVO();
				vo.setCodeMapId(testRule.getCodeMap().getId());
				vo.setDateFormat(testRule.getDateFormat());
				vo.setRuleValue(testRule.getRuleValue());
				sourceRuleVO.add(vo);
			}
			sourceQueryVO.setTestRules(sourceRuleVO);
			TestQuery targetQuery = testCase.getTargetQuery();
			TestQueryVO targetQueryVO = new TestQueryVO();
			targetQueryVO.setName(targetQuery.getName());
			targetQueryVO.setSqlText(targetQuery.getSelectText());
			targetQueryVO.setKeyText(targetQuery.getKeyText());
			targetQueryVO.setPageText(targetQuery.getPageText());
			targetQueryVO.setDataSourceId(targetQuery.getDataSource().getId());
			List<TestRule> tartestRules = targetQuery.getTestRules();
			List<TestRuleVO> targetRuleVO = new ArrayList<TestRuleVO>();
			for (TestRule testRule : tartestRules) {
				TestRuleVO vo = new TestRuleVO();
				vo.setCodeMapId(testRule.getCodeMap().getId());
				vo.setDateFormat(testRule.getDateFormat());
				vo.setRuleValue(testRule.getRuleValue());
				targetRuleVO.add(vo);
			}
			targetQueryVO.setTestRules(targetRuleVO);
			newtestCase.setSourceQuery(sourceQueryVO);
			newtestCase.setTargetQuery(targetQueryVO);
			newtestCase.setTestSuiteID(newtestSuite.getId());
			testCaseService.saveTestCaseVo(newtestCase);
		}

	}

	@Override
	@Transactional
	public String doQualityTestSuite(Integer testSuiteId) {
		TestSuite testSuite = repository.getOne(testSuiteId);
		if (testSuite == null) {
			return "not found testSuite id:" + testSuiteId;
		}
		List<QualityTestCaseModel> qualityCaseModelList = new ArrayList<>();
		if (testSuite.getQualityTestCases().size() == 0) {
			return "is not qualityTestCases:" + testSuiteId;
		}
		for (QualityTestCase qualityTestCase : testSuite.getQualityTestCases()) {
			QualityTestCaseModel qualityCaseModel;
			try {
				qualityCaseModel = new QualityTestCaseModel(qualityTestCase);
				qualityCaseModel.setId(qualityTestCase.getId());
				qualityCaseModel.setName(qualityTestCase.getName());
				qualityCaseModelList.add(qualityCaseModel);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("cast qualityTestCase to model error." + qualityTestCase.getId() + "_"
						+ qualityTestCase.getName());
			}
		}
		// 添加保存轮次的方法 20190415
		TestRound tr = new TestRound();
		tr.setTestSuite(testSuite);
		tr.setCaseCount(testSuite.getQualityTestCases().size());
		tr.setStartTime(new Date());
		tr.setSuccessCount(0);
		testRoundRepository.save(tr);

		ExecutorService executor = null;
		executor = Executors.newFixedThreadPool(5);// 线程数量
		for (QualityTestCaseModel qualityCaseModel : qualityCaseModelList) {
			// 添加轮次id 20190416
			qualityCaseModel.setTestRoundId(tr.getId());
			JobWorker job = new JobWorker(qualityCaseModel);
			executor.execute(job);
		}
		executor.shutdown();

		/*
		 * while(true){ if(executor.isTerminated()){ System.out.println("所有的子线程都结束了！");
		 * break; } try { Thread.sleep(1000); } catch (InterruptedException e) {
		 * e.printStackTrace(); } }
		 */
		return null;
	}

	@Override
	public List<TestSuite> getBySuiteNameAndProductId(String name, Integer productID) {

		return repository.findByNameAndProductId(name, productID);
	}

	@Override
	@Transactional
	public String doFalseQualityTestSuite(List<QualityTestCase> qualityTestCases, TestSuite testSuite) {
		List<QualityTestCaseModel> qualityCaseModelList = new ArrayList<>();
		int test_case_id = qualityTestCases.get(0).getId();
		for (QualityTestCase qualityTestCase : qualityTestCases) {
			QualityTestCaseModel qualityCaseModel;
			try {
				qualityCaseModel = new QualityTestCaseModel(qualityTestCase);
				qualityCaseModel.setId(qualityTestCase.getId());
				qualityCaseModel.setName(qualityTestCase.getName());
				qualityCaseModelList.add(qualityCaseModel);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("cast qualityTestCase to model error." + qualityTestCase.getId() + "_"
						+ qualityTestCase.getName());
			}
		}
		// 添加保存轮次的方法 20190415
		TestRound tr = new TestRound();
		tr.setTestSuite(testSuite);
		tr.setCaseCount(testSuite.getQualityTestCases().size());
		tr.setStartTime(new Date());
		tr.setSuccessCount(0);
		testRoundRepository.save(tr);

		ExecutorService executor = null;
		executor = Executors.newFixedThreadPool(5);// 线程数量
		for (QualityTestCaseModel qualityCaseModel : qualityCaseModelList) {
			// 添加轮次id 20190416
			qualityCaseModel.setTestRoundId(tr.getId());
			JobWorker job = new JobWorker(qualityCaseModel);
			executor.execute(job);
		}
		executor.shutdown();
		return null;
	}

	@Override
	@Transactional
	public void save(TestSuiteVO testSuiteVO) {
		Product p = productRepository.findById(testSuiteVO.getProductID()).get();
		TestSuite testSuite = new TestSuite();
		BeanUtils.copyProperties(testSuiteVO, testSuite);
		testSuite.setProduct(p);
		repository.save(testSuite);

	}

	@Override
	@Transactional
	public String doTaskQualityTestSuite(Integer testSuiteId, int threadNum) {
		TestSuite testSuite = repository.getOne(testSuiteId);
		if (testSuite == null) {
			return "not found testSuite id:" + testSuiteId;
		}
		List<QualityTestCaseModel> qualityCaseModelList = new ArrayList<>();
		if (testSuite.getQualityTestCases().size() == 0) {
			return "is not qualityTestCases:" + testSuiteId;
		}
		for (QualityTestCase qualityTestCase : testSuite.getQualityTestCases()) {
			QualityTestCaseModel qualityCaseModel;
			try {
				qualityCaseModel = new QualityTestCaseModel(qualityTestCase);
				qualityCaseModel.setId(qualityTestCase.getId());
				qualityCaseModel.setName(qualityTestCase.getName());
				qualityCaseModelList.add(qualityCaseModel);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("cast qualityTestCase to model error." + qualityTestCase.getId() + "_"
						+ qualityTestCase.getName());
			}
		}
		TestRound tr = new TestRound();
		tr.setTestSuite(testSuite);
		tr.setCaseCount(testSuite.getQualityTestCases().size());
		tr.setStartTime(new Date());
		tr.setSuccessCount(0);
		testRoundRepository.save(tr);

		ExecutorService executor = null;
		if (threadNum > 0) {
			executor = Executors.newFixedThreadPool(threadNum);// 线程数量
		} else {
			executor = Executors.newFixedThreadPool(1);// 线程数量
		}
		for (QualityTestCaseModel qualityCaseModel : qualityCaseModelList) {
			// 添加轮次id 20190416
			qualityCaseModel.setTestRoundId(tr.getId());
			JobWorker job = new JobWorker(qualityCaseModel);
			executor.execute(job);
		}
		executor.shutdown();
		return null;
	}

	@Override
	public List<TestSuiteVO> findByProductIdAndType(Integer productId, Integer type) {
		List<TestSuite> findByProductId = repository.findByProductIdAndType(productId, type);
		List<TestSuiteVO> listVO = new ArrayList<TestSuiteVO>();
		for (TestSuite testSuite : findByProductId) {
			TestSuiteVO vo = new TestSuiteVO();
			BeanUtils.copyProperties(testSuite, vo);
			listVO.add(vo);
		}
		return listVO;
	}

}
