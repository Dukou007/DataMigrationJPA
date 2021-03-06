package com.jettech.thread;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;

import org.hibernate.StaleObjectStateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.TransactionSystemException;

import com.jettech.EnumExecuteStatus;
import com.jettech.EnumFieldType;
import com.jettech.EnumTestCaseType;
import com.jettech.domain.CaseModel;
import com.jettech.domain.CompareCaseModel;
import com.jettech.domain.DataField;
import com.jettech.domain.CompareaToFileCaseModel;
import com.jettech.domain.DataModel;
import com.jettech.domain.FieldModel;
import com.jettech.domain.ModelCaseModel;
import com.jettech.domain.QualityResultModel;
import com.jettech.domain.QualityTestCaseModel;
import com.jettech.domain.QueryModel;
import com.jettech.domain.ResultModel;
import com.jettech.entity.ModelTestResult;
import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.QualityTestResult;
import com.jettech.entity.QualityTestResultItem;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.entity.TestRound;
import com.jettech.service.IQualityTestResultService;
import com.jettech.service.ITestReusltService;
import com.jettech.service.ModelTestResultService;
import com.jettech.service.WebSocketService;
import com.jettech.service.Impl.ModelTestResultServiceImpl;
import com.jettech.service.Impl.QualityTestResultServiceImpl;
import com.jettech.service.Impl.TestResultServiceImpl;
import com.jettech.service.Impl.TestRoundServiceImpl;
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
	String secordaryTable = null;// 明细表的名称

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
		case DataCompareToFile:
			CompareaToFileCaseModel cpfCase = (CompareaToFileCaseModel) testCase;
			doDataCompareToFileWork(cpfCase);
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

	IQualityTestResultService qualityTestResultService = null;

	private void doQualityWork(QualityTestCaseModel testCase) {
		String info = "do qualityTestCase:" + testCase.getId() + "_" + testCase.getName();
		try {
			logger.info(info);
			qualityTestResultService = (IQualityTestResultService) SpringUtils
			        .getBean(QualityTestResultServiceImpl.class);
			qualityTestResult = createTestResult(testCase, qualityTestResultService);

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

		// 新加质量方法执行案例写入结果集 20190412
		testCase.getTargetQuery().setQualityTestResultId(qualityTestResult.getId());
		QualityCommonDataWorkerOne qulityDataWorker = new QualityCommonDataWorkerOne(itemQueue,
		        testCase.getTargetQuery());
		Thread dataThread = new Thread(qulityDataWorker);
		dataThread.setName("DT:" + testCase.getName());
		dataThread.start();

		// sql查询数据量

		// 取反结果集
		QualityCommonDataWorkerTwo qulityWorker = new QualityCommonDataWorkerTwo(itemQueue, testCase.getTargetQuery());
		Thread dataTwoThread = new Thread(qulityWorker);
		dataTwoThread.setName("DTT:" + testCase.getName());
		dataTwoThread.start();

		dataThread.join();
		dataTwoThread.join();

		// 启动写主结果记录进程
		QualityTestResultWorker resultWorker = new QualityTestResultWorker(itemQueue);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Item:" + testCase.getName());
		resultThread.start();

		resultThread.join();

		int allDataCount = qulityDataWorker.allDataCount;
		int dataCount = qulityDataWorker.dataCount;
		int dataCountSign = allDataCount - dataCount;

		Boolean bo = qulityDataWorker.state;

		if (!bo) {
			this.qualityTestResult.setExecState(EnumExecuteStatus.Interrupt);
		} else {
			this.qualityTestResult.setExecState(EnumExecuteStatus.Finish);
		}
		if (dataCountSign != 0) {
			bo = false;
		}

		// testThread.join();
		// 添加写结果集方法 20190409
		List<DataField> colsList = qulityDataWorker.testQuery.getQueryColumns();
		// List<DataField> colsList = dataWorker.testQuery.getQueryColumns();
		StringBuffer sb = new StringBuffer();
		if (colsList != null) {
			for (int i = 0; i < colsList.size(); i++) {
				if (i < colsList.size() - 1) {
					sb.append(colsList.get(i).getColumnName() + ",");
				} else {
					sb.append(colsList.get(i).getColumnName());
				}
			}
		}

		this.qualityTestResult.setSelectCols(sb.toString());
		// 质量代码添加结果集主表的部分信息 20190409 ==
		if (testCase.getTargetQuery() != null) {
			this.qualityTestResult.setSqlText(testCase.getTargetQuery().getSqlText());
			if (testCase.getTargetQuery().getDataSource() != null)
				this.qualityTestResult.setDataSource(testCase.getTargetQuery().getDataSource().getName());
		}
		this.qualityTestResult.setTestCaseName(testCase.getName());

		this.qualityTestResult.setEndTime(new Date());
		this.qualityTestResult.setDataCount(allDataCount);
		// 目标表数据量改成全量
		this.qualityTestResult.setItemCount(dataCount);// dataCount
		// 添加取反数量
		this.qualityTestResult.setFalseItemCount(dataCountSign);
		this.qualityTestResult.setResult(bo);
		// 添加测试意图 testCase
		// qualityTestResult.setTestPurpose(testCase.getTargetQuery()); 修改中
		// this.qualityTestResult.setResult(testCase.getTestQualityResult().getResult());
		this.qualityTestResultService.save(qualityTestResult);
		logger.info("Job is end." + testCase.getName() + " use " + DateUtil.getEclapsedTimesStr(start));

		/*
		 * //添加保存轮次的方法 20190416 TestRoundServiceImpl TestRoundServiceImpl
		 * testRoundService = (TestRoundServiceImpl) SpringUtils
		 * .getBean(TestRoundServiceImpl.class); //查询轮次 TestRound tr =
		 * testRoundService.findById(testCase.getTestRoundId()); if(bo){
		 * if(tr.getSuccessCount() == null){ tr.setSuccessCount(1); }else{ int
		 * successCount = tr.getSuccessCount().intValue();
		 * tr.setSuccessCount(successCount+1); } } tr.setEndTime(new Date());
		 * tr.setEditTime(new Date()); testRoundService.save(tr);
		 */
		if (testCase.getTestRoundId() != null && testCase.getTestRoundId() != 0) {
			updateTestRound(qulityDataWorker.state);
		}

	}

	private void updateTestRound(Boolean bo) {
		try {
			// 添加保存轮次的方法 20190416 TestRoundRepository TestRoundServiceImpl
			TestRoundServiceImpl testRoundService = (TestRoundServiceImpl) SpringUtils
			        .getBean(TestRoundServiceImpl.class);
			// 查询轮次
			TestRound tr = testRoundService.findById(testCase.getTestRoundId());
			if (bo) {
				if (tr.getSuccessCount() == null || tr.getSuccessCount() == 0) {
					tr.setSuccessCount(1);
				} else {
					tr.setSuccessCount(tr.getSuccessCount() + 1);
				}
			}
			tr.setEndTime(new Date());
			int ok = testRoundService.updateWithVersion(tr.getId(), tr.getSuccessCount(), new Date(), tr.getVersion());
			if (ok == 0) {
				updateTestRound(bo);
			}
		} catch (Exception e) {
			if (e instanceof ObjectOptimisticLockingFailureException || e instanceof StaleObjectStateException
			        || e instanceof TransactionSystemException) {
				logger.error("乐观锁循环" + e);
				updateTestRound(bo);
			} else {
				logger.error("保存失败！" + e);
			}
		}
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
			testResult = createTestResult(testCase);
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

	private void doDataCompareToFileWork(CompareaToFileCaseModel testCase) {
		String info = "do testCase:" + testCase.getId() + "_" + testCase.getName();
		try {
			logger.info(info);
			testResultService = (ITestReusltService) SpringUtils.getBean(TestResultServiceImpl.class);
			testResult = createTestResult(testCase);
			testCase.setTestResult(new ResultModel(testResult));
			if (testCase.getUsePage() && testCase.getPageSize() > 0) {
				logger.info(info + " use page mode.");
				// doUnionWork(testCase);
			} else {
				logger.info(info + " use common mode.");
				doFileCommonWork(testCase);
			}
		} catch (InterruptedException e1) {
			logger.error(info + " error", e1);
		} catch (Exception e) {
			logger.error(info + " error", e);
		}
	}

	private void doFileCommonWork(CompareaToFileCaseModel testCase) throws Exception, InterruptedException {
		long start = DateUtil.getNow().getTime();
		final CountDownLatch latch = new CountDownLatch(3);
		// QualityTestResult testReulst =null;
		// 更新结果状态
		this.testResult.setExecState(EnumExecuteStatus.Executing);
		this.testResult.setStartTime(new Date());
		this.testResultService.save(testResult);
		Integer maxResultRows = testCase.getMaxResultRows();
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
		CommonFileDataWorker targetDataWorker = new CommonFileDataWorker(targetQueue, testCase.getTargetQuery());
		Thread targetThread = new Thread(targetDataWorker);
		targetThread.setName("T.DT:" + testCase.getName());
		targetThread.start();

		sourceThread.join();
		targetThread.join();

		// 启动写主结果记录进程
		TestResultWorker resultWorker = new TestResultWorker(itemQueue, maxResultRows);
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

	private void doCommonWork(CompareCaseModel testCase) throws Exception, InterruptedException {
		long start = DateUtil.getNow().getTime();
		final CountDownLatch latch = new CountDownLatch(3);
		// QualityTestResult testReulst =null;
		// 更新结果状态

		this.testResult.setExecState(EnumExecuteStatus.Ready);
		this.testResult.setStartTime(new Date());
		this.testResultService.save(testResult);
		Integer maxResultRows = testCase.getMaxResultRows();

		WebSocketService.sendMsgToAll("开始进行比较:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
		        + DateUtil.getEclapsedTimesStr(start));

		// 结果明细队列
		BlockingQueue<TestResultItem> itemQueue = new LinkedBlockingQueue<>();
		WebSocketService.sendMsgToAll("启动写主结果记录进程:" + testResult.getId() + " 案例:" + testCase.getName());
		// 启动写主结果记录进程
		TestResultWorker resultWorker = new TestResultWorker(itemQueue, maxResultRows);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Item:" + testCase.getName());
		resultThread.start();

		// 启动获取源数据的进程
		// 创建存储源数据的队列，默认大小为5
		BlockingQueue<BaseData> sourceQueue = new LinkedBlockingQueue<>(maxPagesInQueue);
		// 全量读取到队列单个对象中
		CommonDataWorker sourceDataWorker = new CommonDataWorker(sourceQueue, itemQueue, testCase.getSourceQuery(),
		        testCase);
		Thread sourceThread = new Thread(sourceDataWorker);
		sourceThread.setName("S.DT:" + testCase.getName());
		sourceThread.start();
		WebSocketService.sendMsgToAll("获取目标数据中:" + testResult.getId() + " 案例:" + testCase.getName());

		// 启动获取目标数据的进程
		BlockingQueue<BaseData> targetQueue = new LinkedBlockingQueue<>(maxPagesInQueue);
		CommonDataWorker targetDataWorker = new CommonDataWorker(targetQueue, itemQueue, testCase.getTargetQuery(),
		        testCase);
		Thread targetThread = new Thread(targetDataWorker);
		targetThread.setName("T.DT:" + testCase.getName());
		targetThread.start();

		sourceThread.join();
		targetThread.join();

		updateTestResult(sourceQueue, targetQueue);

		if (testCase.getSourceQuery().getExecState() == EnumExecuteStatus.Finish
		        && testCase.getTargetQuery().getExecState() == EnumExecuteStatus.Finish) {
			// 比较操作
			WebSocketService.sendMsgToAll("比较线程执行中:" + testResult.getId() + " 案例:" + testCase.getName());
			CommonTestWorker worker = new CommonTestWorker(sourceQueue, targetQueue, itemQueue, testCase);
			// worker.compare();//使用当前线程进行执行，不新开子线程
			Thread compareThread = new Thread(worker);
			compareThread.setName("Do:" + testCase.getName());
			compareThread.start();
			compareThread.join();

			WebSocketService.sendMsgToAll("执行结果详情写入中:" + testResult.getId() + " 案例:" + testCase.getName());
			updateTestResult(testCase.getTestResult());

			resultWorker.stop();

			logger.info("###Job is end.结果:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
			        + DateUtil.getEclapsedTimesStr(start));
			WebSocketService.sendMsgToAll("Job is end.结果:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
			        + DateUtil.getEclapsedTimesStr(start));
		} else {
			resultWorker.stop();
			testCase.getTestResult().setExecState(EnumExecuteStatus.Interrupt);
			updateTestResult(testCase.getTestResult());
			String info = "";
			if (testCase.getSourceQuery().getExecState() != EnumExecuteStatus.Finish)
				info += testCase.getName() + "读取源数据错误";
			if (testCase.getSourceQuery().getExecState() != EnumExecuteStatus.Finish)
				info += testCase.getName() + "读取目标数据错误";
			logger.info("###Job is Interrupt." + info + " use:" + DateUtil.getEclapsedTimesStr(start));
		}
	}

	/**
	 * 非分表模式，在获取到数据后进行结果更新
	 * 
	 * @param sourceQueue
	 * @param targetQueue
	 * @throws InterruptedException
	 */
	private void updateTestResult(BlockingQueue<BaseData> sourceQueue, BlockingQueue<BaseData> targetQueue)
	        throws InterruptedException {
		String caseName = null;
		try {
			caseName = this.testCase.getName();
			this.testResult.setExecState(EnumExecuteStatus.Executing);
			this.testResult.setEditTime(new Date());
			// 从队列里面取顶部元素,不移除元素
			if (sourceQueue.size() > 0) {
				Map<String, List<Object>> sourceMap = sourceQueue.element().getMap();
				if (sourceMap != null)
					this.testResult.setSourceCount(sourceMap.size());
			}

			if (targetQueue.size() > 0) {
				Map<String, List<Object>> targetMap = targetQueue.element().getMap();
				if (targetMap != null)
					this.testResult.setSourceCount(targetMap.size());
			}

			this.testResultService.save(testResult);
		} catch (Exception e) {
			logger.error("updateTestResult error,case[" + caseName + "]", e);
		}
	}

	private void updateTestResult(ResultModel resultModel) {
		String caseName = null;
		try {
			caseName = this.testCase.getName();
			this.testResult.setExecState(EnumExecuteStatus.Finish);
			this.testResult.setEndTime(new Date());
			this.testResult.setSourceCount(resultModel.getSourceCount());
			this.testResult.setTargetCount(resultModel.getTargetCount());
			this.testResult.setSameRow(resultModel.getSameRow());
			this.testResult.setNotSameData(resultModel.getNotSameData());
			this.testResult.setNotSameRow(resultModel.getNotSameRow());
			if (resultModel.getSameRow() != null && resultModel.getSameRow() > 0
			        && (resultModel.getNotSameData() == null || resultModel.getNotSameData() == 0)
			        && (resultModel.getNotSameRow() == null || resultModel.getNotSameRow() == 0)) {
				this.testResult.setResult(String.valueOf(Boolean.TRUE));
			} else {
				this.testResult.setResult(String.valueOf(Boolean.FALSE));
			}
			this.testResultService.save(testResult);
		} catch (Exception e) {
			logger.error("updateTestResult error,case:[" + caseName + "]", e);
		}
	}
	private void updateUnionTestResult(ResultModel resultModel, UnionDataWorker unionDataWorker) {
		this.testResult.setExecState(EnumExecuteStatus.Finish);
		this.testResult.setEndTime(new Date());
		this.testResult.setSourceCount(resultModel.getSourceCount());
		this.testResult.setTargetCount(unionDataWorker.allTarDataCount);
		this.testResult.setSameRow(resultModel.getSameRow());
		this.testResult.setNotSameData(resultModel.getNotSameData());
		this.testResult.setNotSameRow(resultModel.getNotSameRow());
		if (this.testResult.getResult() == null) {
			this.testResult.setResult(String.valueOf(Boolean.TRUE));
		}
		if (!String.valueOf(Boolean.FALSE).equals(this.testResult.getResult())) {
			if (resultModel.getSameRow() != null && resultModel.getSameRow() > 0
			        && (resultModel.getNotSameData() == null || resultModel.getNotSameData() == 0)
			        && (resultModel.getNotSameRow() == null || resultModel.getNotSameRow() == 0)) {
				this.testResult.setResult(String.valueOf(Boolean.TRUE));
			} else {
				this.testResult.setResult(String.valueOf(Boolean.FALSE));
			}
		}

		this.testResultService.save(testResult);
	}

	private void doPageWork(CompareCaseModel testCase) throws Exception {
		long start = DateUtil.getNow().getTime();

		// 更新结果状态
		this.testResult.setExecState(EnumExecuteStatus.Executing);
		this.testResult.setStartTime(new Date());
		this.testResultService.save(testResult);
		Integer maxResultRows = testCase.getMaxResultRows();

		WebSocketService.sendMsgToAll("开始进行比较:" + testResult.getId() + " 案例:" + testCase.getName());
		// 结果明细队列
		BlockingQueue<TestResultItem> itemQueue = new LinkedBlockingQueue<>();

		// 启动写主结果记录进程
		WebSocketService.sendMsgToAll("启动写主结果记录进程:" + testResult.getId() + " 案例:" + testCase.getName());
		TestResultWorker resultWorker = new TestResultWorker(itemQueue, maxResultRows);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Result:" + testCase.getName());
		resultThread.start();	
		
		//读取源数据的Key放入缓存
		// 启动获取源数据的进程
		// 创建存储源数据的队列，默认大小为5
		BlockingQueue<BaseData> sourceQueue = new LinkedBlockingQueue<>(maxPagesInQueue);
		// 全量读取到队列单个对象中
//		PageDataWorker sourceDataWorker = new PageDataWorker(sourceQueue, itemQueue, testCase.getSourceQuery(),
//		        testCase);		
		
		//读取目标数据的Key放入缓存
		
		// 比较操作
		
		logger.info("Job is end.结果:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
		        + DateUtil.getEclapsedTimesStr(start));
		WebSocketService.sendMsgToAll("Job is end.结果:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
		        + DateUtil.getEclapsedTimesStr(start));
	}

	private void doUnionWork(CompareCaseModel testCase) throws Exception {
		long start = DateUtil.getNow().getTime();

		// 更新结果状态
		this.testResult.setExecState(EnumExecuteStatus.Executing);
		this.testResult.setStartTime(new Date());
		this.testResultService.save(testResult);
		Integer maxResultRows = testCase.getMaxResultRows();

		WebSocketService.sendMsgToAll("开始进行比较:" + testResult.getId() + " 案例:" + testCase.getName());
		// 结果明细队列
		BlockingQueue<TestResultItem> itemQueue = new LinkedBlockingQueue<>();

		// 启动写主结果记录进程
		WebSocketService.sendMsgToAll("启动写主结果记录进程:" + testResult.getId() + " 案例:" + testCase.getName());
		TestResultWorker resultWorker = new TestResultWorker(itemQueue, maxResultRows);
		Thread resultThread = new Thread(resultWorker);
		resultThread.setName("Result:" + testCase.getName());
		resultThread.start();

		// 启动获取数据的进程(读取源数据和对应的目标数据)
		// 创建存储源数据的队列，默认大小为5
		WebSocketService.sendMsgToAll("启动获取数据的进程:" + testResult.getId() + " 案例:" + testCase.getName());
		BlockingQueue<BaseData> dataQueue = new LinkedBlockingQueue<>(maxPagesInQueue);
		UnionDataWorker unionDataWorker = new UnionDataWorker(dataQueue, testCase, testCase.getPageSize(),
		        testCase.getTargetQuery());
		Thread dataThread = new Thread(unionDataWorker);
		dataThread.setName("UData:" + testCase.getName());
		dataThread.start();

		// 比较操作
		// testCase.get
		WebSocketService.sendMsgToAll("进行比较操作中:" + testResult.getId() + " 案例:" + testCase.getName());
		UnionTestWorker worker = new UnionTestWorker(dataQueue, itemQueue, testCase);
		Thread compareThread = new Thread(worker);
		compareThread.setName("compare." + testCase.getName());
		compareThread.start();

		dataThread.join();
		compareThread.join();
		resultThread.join();
		WebSocketService.sendMsgToAll("比较结果详情写入线程启动:" + testResult.getId() + " 案例:" + testCase.getName());
		updateUnionTestResult(testCase.getTestResult(), unionDataWorker);

		logger.info("Job is end.结果:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
		        + DateUtil.getEclapsedTimesStr(start));
		WebSocketService.sendMsgToAll("Job is end.结果:" + testResult.getId() + " 案例:" + testCase.getName() + " use "
		        + DateUtil.getEclapsedTimesStr(start));
	}

	private QualityTestResult createTestResult(QualityTestCaseModel testCase,
	        IQualityTestResultService testResultService) {
		QualityTestResult result = new QualityTestResult();
		if (testCase.getId() == null) {
			logger.error("########### testCase's Id is null #############");
		} else {
			result.setTestCaseId(testCase.getId());
		}

		result.setSecordaryTable(secordaryTable);

		result.setExecState(EnumExecuteStatus.Init);
		result.setStartTime(new Date());
		// 添加轮次id
		result.setTestRoundId(testCase.getTestRoundId());
		result = testResultService.saveOne(result);
		return result;
	}

	private TestResult createTestResult(CaseModel testCase) {
		TestResult result = new TestResult();
		if (testCase.getId() == null) {
			logger.error("########### testCase's Id is null #############");
		} else {
			result.setCaseId(testCase.getId().toString());
		}

		result.setSecordaryTable(secordaryTable);

		result.setExecState(EnumExecuteStatus.Init);
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

		result.setSecordaryTable(secordaryTable);

		result.setExecState(EnumExecuteStatus.Init);
		result.setStartTime(new Date());
		result = modelTestResultService.saveOne(result);
		TestResult testResult = new TestResult();
		BeanUtils.copyProperties(result, testResult);
		return testResult;
	}

	private TestResult createTestResult(CompareCaseModel testCase) {
		TestResult result = new TestResult();
		if (testCase.getId() == null) {
			logger.error("########### testCase's Id is null #############");
		} else {
			result.setCaseId(testCase.getId().toString());
		}

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
