package com.jettech.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import javax.transaction.Transactional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;

import com.jettech.EnumExecuteStatus;
import com.jettech.EnumFieldType;
import com.jettech.EnumTestCaseType;
import com.jettech.domain.CaseModel;
import com.jettech.domain.CompareCaseModel;
import com.jettech.domain.DataModel;
import com.jettech.domain.FieldModel;
import com.jettech.domain.ModelCaseModel;
import com.jettech.domain.QualityResultModel;
import com.jettech.domain.QualityTestCaseModel;
import com.jettech.domain.QueryModel;
import com.jettech.domain.ResultModel;
import com.jettech.entity.QualityTestResult;
import com.jettech.entity.QualityTestResultItem;
import com.jettech.entity.DataField;
import com.jettech.entity.ModelTestResult;
import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestQueryField;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.service.IQualityTestReusltService;
import com.jettech.service.ITestReusltService;
import com.jettech.service.ModelTestResultService;
import com.jettech.service.Impl.ModelTestResultServiceImpl;
import com.jettech.service.Impl.QualityTestResultServiceImpl;
import com.jettech.service.Impl.TestCaseServiceImpl;
import com.jettech.service.Impl.TestResultServiceImpl;
import com.jettech.util.DateUtil;
import com.jettech.util.SpringUtils;

import javafx.util.Pair;

/**
 * 实现一个案例的比较操作
 * 
 * @author tan
 *
 */
// @Component
public class JobWorker implements Runnable {

	private static Logger logger = LoggerFactory.getLogger(JobWorker.class);
	// @Autowired
	// private ITestReusltService testResultService;
	private CaseModel testCase;
	private EnumTestCaseType testCaseType = EnumTestCaseType.None;

	public JobWorker(CaseModel testCase) {
		this.testCase = testCase;
		testCaseType = testCase.getTestCaseType();
	}

	Integer _defaultMaxPagesInQueue = 5;
	Integer maxPagesInQueue = _defaultMaxPagesInQueue;

	ITestReusltService testResultService = null;
	ModelTestResultService modelTestResultService = null;
	TestResult testResult = null;
	ModelTestResult modeltestResult = null;

	QualityTestResult qualityTestResult = null;

	@Override
	public void run() {
		logger.info("begin doTestCase:" + testCase.getName());
		switch (testCaseType) {
		case DataCompare:
			CompareCaseModel cpCase = (CompareCaseModel) testCase;
			doDataCompareWork(cpCase);
			break;
		case QualityCheck:
			QualityTestCaseModel qaCase = (QualityTestCaseModel) testCase;
			doQualityWork(qaCase);
			break;
		case AccountingEntry:
			break;
		case RepaymentSchedule:
			break;
		case DataModel:
			ModelCaseModel modelCase = (ModelCaseModel) testCase;
			doModelTestWork(modelCase);
			break;
		case None:
		default:
		}

	}

	IQualityTestReusltService qualityTestResultService = null;

	private void doQualityWork(QualityTestCaseModel testCase) {
		String info = "do testCase:" + testCase.getId() + "_" + testCase.getName();
		try {
			logger.info(info);
			qualityTestResultService = (IQualityTestReusltService) SpringUtils
			        .getBean(QualityTestResultServiceImpl.class);
			qualityTestResult = createTestReuslt(testCase, qualityTestResultService);

			testCase.setTestQualityResult(new QualityResultModel(qualityTestResult));
			if (testCase.getUsePage() && testCase.getPageSize() > 0) {
				logger.info(info + " use page mode.");
				doQualityUnionWork(testCase);
			} else {
				logger.info(info + " use common mode.");
				doQualityCommonWork(testCase);
			}
		} catch (Exception e) {
			logger.error(info + " error", e);
		}
	}

	private void doQualityCommonWork(QualityTestCaseModel testCase) throws Exception {
		long start = DateUtil.getNow().getTime();
		// 更新结果状态
		this.qualityTestResult.setExecState(EnumExecuteStatus.Executing);
		this.qualityTestResult.setStartTime(new Date());
		this.qualityTestResultService.save(qualityTestResult);

		// 结果明细队列
		BlockingQueue<QualityTestResultItem> itemQueue = new LinkedBlockingQueue<>();

		// 启动获取数据的进程
		// 创建存储源数据的队列，默认大小为5
		BlockingQueue<QualityBaseData> dataQueue = new LinkedBlockingQueue<>(maxPagesInQueue);

		// 全量读取到队列单个对象中
		QualityCommonDataWorker dataWorker = new QualityCommonDataWorker(dataQueue, testCase.getTargetQuery());
		Thread dataThread = new Thread(dataWorker);
		dataThread.setName("DT:" + testCase.getName());
		dataThread.start();
		dataThread.join();

		// 执行操作
		CommonCheckWorker worker = new CommonCheckWorker(dataQueue, itemQueue, testCase);
		Thread testThread = new Thread(worker);
		testThread.setName("Do:" + testCase.getName());
		testThread.start();

		// 启动写主结果记录进程
		QualityTestResultWorker resultWorker = new QualityTestResultWorker(itemQueue);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Item:" + testCase.getName());
		resultThread.start();

		testThread.join();

		this.qualityTestResult.setExecState(EnumExecuteStatus.Finish);
		this.qualityTestResult.setEndTime(new Date());
		this.qualityTestResult.setDataCount(testCase.getTestQualityResult().getDataCount());
		this.qualityTestResult.setItemCount(testCase.getTestQualityResult().getItemCount());
		this.qualityTestResult.setResult(testCase.getTestQualityResult().getResult());
		this.qualityTestResultService.save(qualityTestResult);

		logger.info("Job is end." + testCase.getName() + " use " + DateUtil.getEclapsedTimesStr(start));

	}

	private void doQualityUnionWork(QualityTestCaseModel testCase) throws Exception {
		long start = DateUtil.getNow().getTime();

		// 更新结果状态
		this.qualityTestResult.setExecState(EnumExecuteStatus.Executing);
		this.qualityTestResult.setStartTime(new Date());
		this.qualityTestResultService.save(qualityTestResult);

		// 结果明细队列
		BlockingQueue<QualityTestResultItem> itemQueue = new LinkedBlockingQueue<>();

		// 启动写主结果记录进程
		// 启动写主结果记录进程
		QualityTestResultWorker resultWorker = new QualityTestResultWorker(itemQueue);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Result:" + testCase.getName());
		resultThread.start();

		// 启动获取源数据的进程
		// 创建存储源数据的队列，默认大小为5
		BlockingQueue<QualityBaseData> dataQueue = new LinkedBlockingQueue<>(maxPagesInQueue);
		// TestCaseService testCaseService =
		// SpringUtils.getBean(TestCaseService.class);
		// TestCase testCase=testCaseService.getOneById(testCaseId);
		// Integer pageSize = testCase.getPageSize();
		// 全量读取到队列单个对象中
		QualityPageDataWorker dataWorker = new QualityPageDataWorker(dataQueue, testCase.getTargetQuery(),
		        testCase.getPageSize());
		Thread dataThread = new Thread(dataWorker);
		dataThread.setName("DT:" + testCase.getName());
		dataThread.start();

		// 执行操作
		PageCheckWorker worker = new PageCheckWorker(dataQueue, itemQueue, testCase);
		Thread testThread = new Thread(worker);
		testThread.setName("Do:" + testCase.getName());
		testThread.start();

		dataThread.join();
		testThread.join();
		resultWorker.stop();

		// 比较操作
		// testCase.get
		// CompareWorker worker = new CompareWorker(sourceQueue, targetQueue,
		// itemQueue);
		// Thread compareThread = new Thread(worker);
		// compareThread.setName("compare");
		// compareThread.start();

		dataThread.join();
		// compareThread.join();
		resultWorker.stop();
	}

	private void doQualityConcurrentWork(QualityTestCaseModel testCase) throws Exception {
		ExecutorService pool = Executors.newFixedThreadPool(maxPagesInQueue);
		long start = DateUtil.getNow().getTime();
		// 更新结果状态
		this.qualityTestResult.setExecState(EnumExecuteStatus.Executing);
		this.qualityTestResult.setStartTime(new Date());
		this.qualityTestResultService.save(qualityTestResult);

		// 结果明细队列
		BlockingQueue<QualityTestResultItem> itemQueue = new LinkedBlockingQueue<>();

		// 创建存储源数据的队列，默认大小为5
		BlockingQueue<QualityBaseData> dataQueue = new LinkedBlockingQueue<>(maxPagesInQueue);

		// 全量读取到队列单个对象中
		QualityPageDataWorker dataWorker = new QualityPageDataWorker(dataQueue, testCase.getTargetQuery(),
		        testCase.getPageSize());
		Thread dataThread = new Thread(dataWorker);
		dataThread.setName("DT:" + testCase.getName());

		// 执行操作
		CommonCheckWorker worker = new CommonCheckWorker(dataQueue, itemQueue, testCase);

		Thread testThread = new Thread(worker);
		testThread.setName("Do:" + testCase.getName());

		// 启动写主结果记录进程
		QualityTestResultWorker resultWorker = new QualityTestResultWorker(itemQueue);

		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Item:" + testCase.getName());
		// 线程池执行
		Future<?> future1 = pool.submit(dataThread);
		if (future1.isDone()) {
			Future<?> future2 = pool.submit(testThread);
			if (future2.isDone()) {
				pool.execute(resultThread);
			}
		}

		// testThread.join();

		this.qualityTestResult.setExecState(EnumExecuteStatus.Finish);
		this.qualityTestResult.setEndTime(new Date());
		this.qualityTestResult.setItemCount(testCase.getTestQualityResult().getItemCount());
		this.qualityTestResult.setResult(testCase.getTestQualityResult().getResult());
		this.qualityTestResult.setDataCount(testCase.getTestQualityResult().getDataCount());
		this.qualityTestResultService.save(qualityTestResult);

		logger.info("Job is end." + testCase.getName() + " use " + DateUtil.getEclapsedTimesStr(start));
	}

	private void doModelTestWork(ModelCaseModel modelCase) {
		List<Pair<DataModel, DataModel>> modelPairList = modelCase.getModelPairList();
		String pairSize = modelPairList != null ? String.valueOf(modelPairList.size()) : "empty";
		String info = "do modelTestCase:" + testCase.getId() + "_" + testCase.getName() + " pairSize:" + pairSize;

		logger.info(info);
		modelTestResultService = (ModelTestResultService) SpringUtils.getBean(ModelTestResultServiceImpl.class);
		testResult = createModelTestReuslt(testCase);
		testCase.setTestResult(new ResultModel(testResult));

		// 结果明细队列
		BlockingQueue<ModelTestResultItem> itemQueue = new LinkedBlockingQueue<>();

		DataModelTestWorker worker = new DataModelTestWorker(itemQueue, modelCase);
		Thread thread = new Thread(worker);
		thread.start();

		// 启动写主结果记录进程
		ModelTestResultWorker resultWorker = new ModelTestResultWorker(itemQueue);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Item:" + testCase.getName());
		resultThread.start();
	}

	private void doDataCompareWork(CompareCaseModel testCase) {
		String info = "do testCase:" + testCase.getId() + "_" + testCase.getName();
		try {
			logger.info(info);
			testResultService = (ITestReusltService) SpringUtils.getBean(TestResultServiceImpl.class);
			testResult = createTestReuslt(testCase);
			testCase.setTestResult(new ResultModel(testResult));
			if (testCase.getUsePage() && testCase.getPageSize() > 0) {
				logger.info(info + " use page mode.");
				doUnionWork(testCase);
			} else {
				logger.info(info + " use common mode.");
				doCommonWork(testCase);
			}

		} catch (InterruptedException e1) {
			logger.error(info + " error", e1);
		} catch (Exception e) {
			logger.error(info + " error", e);
		}
	}

	private void doCommonWork(CompareCaseModel testCase) throws Exception, InterruptedException {
		long start = DateUtil.getNow().getTime();
		final CountDownLatch latch = new CountDownLatch(3);
		// QualityTestResult testReulst =null;
		// 更新结果状态
		this.testResult.setExecState(EnumExecuteStatus.Executing);
		this.testResult.setStartTime(new Date());
		this.testResultService.save(testResult);

		// 结果明细队列
		BlockingQueue<TestResultItem> itemQueue = new LinkedBlockingQueue<>();

		// 启动获取源数据的进程
		// 创建存储源数据的队列，默认大小为5
		BlockingQueue<BaseData> sourceQueue = new LinkedBlockingQueue<>(maxPagesInQueue);
		// 全量读取到队列单个对象中
		CommonDataWorker sourceDataWorker = new CommonDataWorker(sourceQueue, testCase.getSourceQuery());
		Thread sourceThread = new Thread(sourceDataWorker);
		sourceThread.setName("S.DT:" + testCase.getName());
		sourceThread.start();

		// 启动获取目标数据的进程
		BlockingQueue<BaseData> targetQueue = new LinkedBlockingQueue<>(maxPagesInQueue);
		CommonDataWorker targetDataWorker = new CommonDataWorker(targetQueue, testCase.getTargetQuery());
		Thread targetThread = new Thread(targetDataWorker);
		targetThread.setName("T.DT:" + testCase.getName());
		targetThread.start();

		sourceThread.join();
		targetThread.join();

		// 启动写主结果记录进程
		TestResultWorker resultWorker = new TestResultWorker(itemQueue);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Item:" + testCase.getName());
		resultThread.start();

		// 比较操作
		CommonTestWorker worker = new CommonTestWorker(sourceQueue, targetQueue, itemQueue, testCase);
		// worker.compare();//使用当前线程进行执行，不新开子线程
		Thread compareThread = new Thread(worker);
		compareThread.setName("Do:" + testCase.getName());
		compareThread.start();

		compareThread.join();
		resultThread.join();
		// 等待结果详情写完
		// while (!itemQueue.isEmpty()) {
		// try {
		// Thread.sleep(100);
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }

		updateTestResult(testCase.getTestResult());

		logger.info("Job is end.结果:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
		        + DateUtil.getEclapsedTimesStr(start));
	}

	private void updateTestResult(ResultModel resultModel) {
		this.testResult.setExecState(EnumExecuteStatus.Finish);
		this.testResult.setEndTime(new Date());
		this.testResult.setSourceCount(resultModel.getSourceCount());
		this.testResult.setTargetCount(resultModel.getTargetCount());
		this.testResult.setSameRow(resultModel.getSameRow());
		this.testResult.setNotSameData(resultModel.getNotSameData());
		this.testResult.setNotSameRow(resultModel.getNotSameRow());
		if (resultModel.getSameRow() > 0 && (resultModel.getNotSameData() == null || resultModel.getNotSameData() == 0)
		        && (resultModel.getNotSameRow() == null || resultModel.getNotSameRow() == 0)) {
			this.testResult.setResult(String.valueOf(Boolean.TRUE));
		} else {
			this.testResult.setResult(String.valueOf(Boolean.FALSE));
		}
		this.testResultService.save(testResult);
	}

	private void doUnionWork(CompareCaseModel testCase) throws Exception {
		long start = DateUtil.getNow().getTime();

		// 更新结果状态
		this.testResult.setExecState(EnumExecuteStatus.Executing);
		this.testResult.setStartTime(new Date());
		this.testResultService.save(testResult);

		// 结果明细队列
		BlockingQueue<TestResultItem> itemQueue = new LinkedBlockingQueue<>();

		// 启动写主结果记录进程
		// 启动写主结果记录进程
		TestResultWorker resultWorker = new TestResultWorker(itemQueue);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Result:" + testCase.getName());
		resultThread.start();

		// 启动获取数据的进程(读取源数据和对应的目标数据)
		// 创建存储源数据的队列，默认大小为5
		BlockingQueue<BaseData> dataQueue = new LinkedBlockingQueue<>(maxPagesInQueue);
		UnionDataWorker unionDataWorker = new UnionDataWorker(dataQueue, testCase, testCase.getPageSize(),
		        testCase.getTargetQuery());
		Thread dataThread = new Thread(unionDataWorker);
		dataThread.setName("UData:" + testCase.getName());
		dataThread.start();

		// 比较操作
		// testCase.get
		UnionTestWorker worker = new UnionTestWorker(dataQueue, itemQueue, testCase);
		Thread compareThread = new Thread(worker);
		compareThread.setName("compare." + testCase.getName());
		compareThread.start();

		dataThread.join();
		compareThread.join();
		resultThread.join();

		updateTestResult(testCase.getTestResult());

		logger.info("Job is end.结果:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
		        + DateUtil.getEclapsedTimesStr(start));
	}

	private QualityTestResult createTestReuslt(QualityTestCaseModel testCase,
	        IQualityTestReusltService testResultService) {
		QualityTestResult result = new QualityTestResult();
		if (testCase.getId() == null) {
			logger.error("########### testCase's Id is null #############");
		} else {
			result.setTestCaseId(testCase.getId());
		}

		String secordaryTable = null;// 明细表的名称
		result.setSecordaryTable(secordaryTable);

		result.setExecState(EnumExecuteStatus.Ready);
		result.setStartTime(new Date());
		result = testResultService.saveOne(result);
		return result;
	}

	private TestResult createTestReuslt(CaseModel testCase) {
		TestResult result = new TestResult();
		if (testCase.getId() == null) {
			logger.error("########### testCase's Id is null #############");
		} else {
			result.setCaseId(testCase.getId().toString());
		}

		String secordaryTable = null;// 明细表的名称
		result.setSecordaryTable(secordaryTable);

		result.setExecState(EnumExecuteStatus.Ready);
		result.setStartTime(new Date());
		result = testResultService.saveOne(result);
		return result;
	}
	private TestResult createModelTestReuslt(CaseModel testCase) {
		ModelTestResult result = new ModelTestResult();
		if (testCase.getId() == null) {
			logger.error("########### testCase's Id is null #############");
		} else {
			result.setCaseId(testCase.getId().toString());
		}

		String secordaryTable = null;// 明细表的名称
		result.setSecordaryTable(secordaryTable);

		result.setExecState(EnumExecuteStatus.Ready);
		result.setStartTime(new Date());
		result = modelTestResultService.saveOne(result);
		TestResult testResult=new TestResult();
		BeanUtils.copyProperties(result, testResult);
		return testResult;
	}
	private TestResult createTestReuslt(CompareCaseModel testCase) {
		TestResult result = new TestResult();
		if (testCase.getId() == null) {
			logger.error("########### testCase's Id is null #############");
		} else {
			result.setCaseId(testCase.getId().toString());
		}

		String secordaryTable = null;// 明细表的名称
		result.setSecordaryTable(secordaryTable);

		// source
		List<String> sourceCols = new ArrayList<>();
		QueryModel sourceQuery = testCase.getSourceQuery();
		for (FieldModel field : sourceQuery.getTestFields()) {
			if (field.getFieldType() == EnumFieldType.TestField) {
				sourceCols.add(field.getName());
			} else {
				sourceCols.add(field.getExpression());
			}
		}
		result.setSourceCol(sourceCols.toString());
		if (sourceQuery.getKeyFields() != null && sourceQuery.getKeyFields().size() > 0) {
			List<String> sourceKeys = new ArrayList<>();
			for (FieldModel field : sourceQuery.getKeyFields()) {
				if (field.getFieldType() == EnumFieldType.TestField) {
					sourceKeys.add(field.getName());
				} else {
					sourceKeys.add(field.getExpression());
				}
			}
			result.setSourceKey(sourceKeys.toString());
		} else {
			result.setSourceKey(testCase.getSourceQuery().getKeyText());
		}

		result.setSourceData(sourceQuery.getDataSource().getPrintInfo());
		result.setSourceSql(sourceQuery.getSqlText());

		// target
		List<String> targetCols = new ArrayList<>();
		QueryModel targetQuery = testCase.getTargetQuery();
		for (FieldModel field : targetQuery.getTestFields()) {
			targetCols.add(field.getName());
		}
		result.setTargetCol(targetCols.toString());
		if (targetQuery.getKeyFields() != null && targetQuery.getKeyFields().size() > 0) {
			List<String> targetKeys = new ArrayList<>();
			for (FieldModel field : targetQuery.getKeyFields()) {
				if (field.getFieldType() == EnumFieldType.TestField) {
					targetKeys.add(field.getName());
				} else {
					targetKeys.add(field.getExpression());
				}
			}
			result.setTargetKey(targetKeys.toString());
		} else {
			result.setTargetKey(testCase.getTargetQuery().getKeyText());
		}
		result.setTargetData(targetQuery.getDataSource().getPrintInfo());
		result.setTargetSql(targetQuery.getSqlText());

		result.setExecState(EnumExecuteStatus.Ready);
		result.setStartTime(new Date());
		result = testResultService.saveOne(result);
		return result;
	}

	/**
	 * 更新主结果信息
	 */
	public void updateTestResult() {

	}

	// public static void main(String[] args) {
	// try {
	// DataSource dataSource = new DataSource();
	// dataSource.setId(1);
	// dataSource.setHost("127.0.0.1");
	// dataSource.setDriver("com.mysql.jdbc.Driver");
	// dataSource.setName("localhost");
	// dataSource.setPassword("root");
	// dataSource.setUserName("root");
	// dataSource.setDatabaseType("mysql");
	// dataSource.setDefaultSchema("dbquality");
	// dataSource.setCharacterSet("utf8");
	// dataSource.setUrl(
	// "jdbc:mysql://127.0.0.1:3306/dbquality?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true&serverTimezone=UTC");
	// //
	// dataSource.setURL("jdbc:mysql://127.0.0.1:3306/database?useUnicode=true&characterEncoding=utf8&allowMultiQueries=true");
	// TestQuery sourceQuery = new TestQuery();
	// sourceQuery.setSqlText("select * from t1");
	// sourceQuery.setDataSource(dataSource);
	// List<TestQueryField> keys1 = new ArrayList<TestQueryField>();
	// keys1.add(new TestQueryField("col1", "varchar"));
	// sourceQuery.setKeyFields(keys1);
	// List<TestField> cols1 = new ArrayList<TestField>();
	// cols1.add(new TestField("col2", "decimal"));
	// cols1.add(new TestField("col3", "timestamp"));
	// sourceQuery.setTestFields(cols1);
	//
	// TestQuery targetQuery = new TestQuery();
	// targetQuery.setSqlText("select * from t1");
	// targetQuery.setDataSource(dataSource);
	// List<TestField> keys2 = new ArrayList<TestField>();
	// keys1.add(new TestField("col1", "varchar"));
	// targetQuery.setKeyFields(keys2);
	// List<TestField> cols2 = new ArrayList<TestField>();
	// cols1.add(new TestField("col2", "decimal"));
	// cols1.add(new TestField("col3", "timestamp"));
	// targetQuery.setTestFields(cols2);
	//
	// TestCase testCase = new TestCase();
	// // testCase.setId(1);
	// testCase.setIsSQLCase(true);
	// testCase.setMaxResultRows(100);
	// testCase.setName("demo");
	// testCase.setPageSize(1000);
	// testCase.setSourceQuery(sourceQuery);
	// testCase.setTargetQuery(targetQuery);
	//
	// Thread thread = new Thread(new JobWorker(testCase.getId()));
	// thread.start();
	// } catch (Exception e) {
	// e.printStackTrace();
	// }
	// }

}
