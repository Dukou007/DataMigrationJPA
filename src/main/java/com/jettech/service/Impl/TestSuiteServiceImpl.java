package com.jettech.service.Impl;

import com.jettech.BizException;
import com.jettech.domain.CaseModel;
import com.jettech.domain.QualityCaseModel;
import com.jettech.domain.QualityTestCaseModel;
import com.jettech.entity.*;
import com.jettech.repostory.ProductRepository;
import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.TestCaseRepository;
import com.jettech.repostory.TestSuiteCaseRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.ITestCaseService;
import com.jettech.service.TestSuiteCaseService;
import com.jettech.service.TestSuiteService;
import com.jettech.thread.JobWorker;
import com.jettech.vo.TestCaseVO;
import com.jettech.vo.TestQueryVO;
import com.jettech.vo.TestRuleVO;
import com.jettech.vo.TestSuiteVO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
	//添加质量方法 20190321
	@Autowired
	private QualityTestCaseRepository qualityTestCaseRepository;
	@Autowired
	private TestSuiteCaseRepository testSuiteCaseRepository;
	
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
	public void delete(String ids) {
		if(ids!=null) {
			String[] testSuites = ids.split(",");
			for (String id : testSuites) {
				repository.deleteById(Integer.valueOf(id));
			}
		}else {
			System.out.println("请输入合法参数");
		}
	
	}

	@Override
	public TestSuite findById(Integer id) {
		Optional<TestSuite> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
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
		 List<TestSuiteVO> listVO=new ArrayList<TestSuiteVO>();
		 for (TestSuite testSuite : findByProductId) {
			 TestSuiteVO vo=new TestSuiteVO();
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
	public Page<TestSuite> getAllTestSuiteByPage(String name, Pageable pageable) {
		
				 Page<TestSuite>list=repository.getAllTestSuiteByPage(name,pageable);
				 if(list.getSize()>0) {
					 return list;
				 }else {
					 return new PageImpl<TestSuite>(new ArrayList<TestSuite>(), pageable, 0);
				 }
				 
	}

	@Override
	public Page<TestSuite> getAllTestSuiteByProductID(Integer productID, Pageable pageable) {
		
	Page<TestSuite>list=repository.getAllTestSuiteByProductID(productID,pageable);
	if(list.getSize()>0) {
		return list;
	}else {
		return new PageImpl<TestSuite>(new ArrayList<TestSuite>(), pageable, 0);
	}
		
	}

	@Override
	public Page<TestSuite> getTestSuiteList(Pageable pageable) {
		Page<TestSuite>list=repository.getTestSuiteList(pageable);
		if(list.getSize()>0) {
			return list;
		}else {
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
		if(testSuiteID!=null) {
			List<TestCase> testCaseList =new ArrayList<TestCase>();
			Integer[] caseIds = testSuiteCaseRepository.findCaseIdsBysuiteId(testSuiteID);
			for (Integer caseId : caseIds) {
				TestCase testCase = testCaseRepository.getOne(caseId);
				testCaseList.add(testCase);
			}
			for (TestCase testCase : testCaseList) {
				Integer testCaseId = testCase.getId();
				doTest(testCaseId);
			}
			return "开始执行";
		}else {
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
//	    List<TestCase> testCaseList =caseList;
		TestSuite newtestSuite = new TestSuite();
		Product product =new Product();
		product.setId(testSuite.getProduct().getId());
		newtestSuite.setName(testSuite.getName());
		newtestSuite.setProduct(product);
	    repository.save(newtestSuite);
		for (TestCase testCase : caseList) {
			//复制案例
			TestCaseVO newtestCase=new TestCaseVO();
			newtestCase.setName(testCase.getName());
			newtestCase.setVersion(testCase.getVersion());
			newtestCase.setIsSQLCase(testCase.getIsSQLCase());
			newtestCase.setMaxResultRows(testCase.getMaxResultRows());
			newtestCase.setUsePage(testCase.getUsePage());
			newtestCase.setPageSize(testCase.getPageSize());
			TestQuery sourceQuery = testCase.getSourceQuery();
			TestQueryVO  sourceQueryVO=new TestQueryVO();
			sourceQueryVO.setName(sourceQuery.getName());
			sourceQueryVO.setSqlText(sourceQuery.getSelectText());
			sourceQueryVO.setKeyText(sourceQuery.getKeyText());
			sourceQueryVO.setPageText(sourceQuery.getPageText());
			sourceQueryVO.setDataSourceId(sourceQuery.getDataSource().getId());
			List<TestRule> testRules = sourceQuery.getTestRules();
			List<TestRuleVO> sourceRuleVO=new ArrayList<TestRuleVO>();
		    for (TestRule testRule : testRules) {
				TestRuleVO vo=new TestRuleVO();
				vo.setCodeMapId(testRule.getCodeMap().getId());
				vo.setDateFormat(testRule.getDateFormat());
				vo.setRuleValue(testRule.getRuleValue());
				sourceRuleVO.add(vo);
			}
		    sourceQueryVO.setTestRules(sourceRuleVO);
		    TestQuery targetQuery = testCase.getTargetQuery();
			TestQueryVO  targetQueryVO=new TestQueryVO();
			targetQueryVO.setName(targetQuery.getName());
			targetQueryVO.setSqlText(targetQuery.getSelectText());
			targetQueryVO.setKeyText(targetQuery.getKeyText());
			targetQueryVO.setPageText(targetQuery.getPageText());
			targetQueryVO.setDataSourceId(targetQuery.getDataSource().getId());
			List<TestRule> tartestRules = targetQuery.getTestRules();
			List<TestRuleVO> targetRuleVO=new ArrayList<TestRuleVO>();
		    for (TestRule testRule : tartestRules) {
				TestRuleVO vo=new TestRuleVO();
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

	//添加质量的方法    =====   20190321
	/*@Override
	@Transactional
	public String doQualityTestSuite(Integer testSuiteId) {
		TestSuite testSuite = repository.getOne(testSuiteId);
		if (testSuite == null) {
			return "not found testSuite id:" + testSuiteId;
		}
		List<CaseModel> qualityCaseModelList = new ArrayList<>();
		for (QualityTestCase qualityTestCase : testSuite.getQualityTestCases()) {
			CaseModel qualityCaseModel;
			try {
				qualityCaseModel = new CaseModel(qualityTestCase);
				qualityCaseModel.setId(qualityTestCase.getId());
				qualityCaseModel.setName(qualityTestCase.getName());
				qualityCaseModelList.add(qualityCaseModel);
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("cast qualityTestCase to model error." + qualityTestCase.getId() + "_" + qualityTestCase.getName());
			}
			// 特殊的：以下属性不能复制,手工复制一次(暂未查明原因)
		}
		ExecutorService executor = null;
		executor = Executors.newFixedThreadPool(5);// 线程数量
		for (CaseModel qualityCaseModel : qualityCaseModelList) {
			JobWorker job = new JobWorker(qualityCaseModel);
			executor.execute(job);
		}
		executor.shutdown();

		return null;
	}*/

	@Override
	@Transactional
	public String doQualityTestSuite(Integer testSuiteId) {
		TestSuite testSuite = repository.getOne(testSuiteId);
		if (testSuite == null) {
			return "not found testSuite id:" + testSuiteId;
		}
		//List<CaseModel> qualityCaseModelList = new ArrayList<>();
		List<QualityTestCaseModel> qualityCaseModelList = new ArrayList<>();
		for (QualityTestCase qualityTestCase : testSuite.getQualityTestCases()) {
			//	CaseModel qualityCaseModel;
			QualityTestCaseModel qualityCaseModel;
			try {
				qualityCaseModel = new QualityTestCaseModel(qualityTestCase);
				qualityCaseModel.setId(qualityTestCase.getId());
				qualityCaseModel.setName(qualityTestCase.getName());
				qualityCaseModelList.add(qualityCaseModel);
				/*qualityCaseModel = new CaseModel(qualityTestCase);
				qualityCaseModel.setId(qualityTestCase.getId());
				qualityCaseModel.setName(qualityTestCase.getName());
				qualityCaseModelList.add(qualityCaseModel);*/
			} catch (Exception e) {
				e.printStackTrace();
				logger.error("cast qualityTestCase to model error." + qualityTestCase.getId() + "_" + qualityTestCase.getName());
			}
			// 特殊的：以下属性不能复制,手工复制一次(暂未查明原因)
		}
		ExecutorService executor = null;
		executor = Executors.newFixedThreadPool(5);// 线程数量
		for (QualityTestCaseModel qualityCaseModel : qualityCaseModelList) {
			JobWorker job = new JobWorker(qualityCaseModel);
			executor.execute(job);
		}
		executor.shutdown();

		return null;
	}

	@Override
	public List<TestSuite> getBySuiteNameAndProductId(String name, Integer productID) {
		
		return repository.findByNameAndProductId(name,productID);
	}




	//质量方法添加  20190321
	/*@SuppressWarnings("unused")
	@Override
	public String doQualityTest(Integer testSuiteID) {
		TestSuite testSuite = repository.getOne(testSuiteID);
		if(testSuiteID!=null) {
			List<QualityTestCase> qualityTestCaseList = qualityTestCaseRepository.findAllTestCase(testSuiteID);
			for (QualityTestCase qualityTestCase : qualityTestCaseList) {
				Integer testCaseId = qualityTestCase.getId();
				doQualityTest(testCaseId);
			}
			return "开始执行";
		}else {
			return "请输入合法参数";
		}
	}*/



//======
	
}
