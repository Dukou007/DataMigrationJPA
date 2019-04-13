package com.jettech.controller;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskScheduler;
import org.springframework.scheduling.support.CronTrigger;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.PageHelper;
import com.jettech.entity.Product;
import com.jettech.entity.TestSuite;
import com.jettech.entity.TestTask;
import com.jettech.service.ITestTaskService;
import com.jettech.service.TestSuiteCaseService;
import com.jettech.service.TestSuiteService;
import com.jettech.util.DateUtil;
import com.jettech.vo.ProductVO;
import com.jettech.vo.TaskResultVO;
import com.jettech.vo.TestSuiteVO;
import com.jettech.vo.TestTaskVO;

@RestController
@Component
@RequestMapping("/testTask")
public class TestTaskController {

	private static Logger log = LoggerFactory.getLogger(TestTaskController.class);

	@Autowired
	private ThreadPoolTaskScheduler threadPoolTaskScheduler;

	@Autowired
	private ITestTaskService testTaskService;

	@Autowired
	private TestSuiteService testSuiteService;
	@Autowired
	private	TestSuiteCaseService testSuiteCaseService;
	
	private ScheduledFuture<?> future;

	private final int maxNo = 5;// 最大线程数

	private Map<Integer, ScheduledFuture<?>> futureMap = new HashMap<Integer, ScheduledFuture<?>>();

	@Bean
	public ThreadPoolTaskScheduler threadPoolTaskScheduler() {
		ThreadPoolTaskScheduler tps = new ThreadPoolTaskScheduler();
		tps.setPoolSize(maxNo);
		return tps;
	}

	/**
	 * 启动单一定时任务
	 */
	@ResponseBody
	@RequestMapping(value = "/startTask/{id}", produces = { "application/json;charset=UTF-8" })
	public String startTask(@PathVariable("id") int id) {
		JSONObject result = new JSONObject();
		try {
			TestTask task = testTaskService.findById(id);
			if (!task.isActived()) {// 未激活状态
				result.put("state", "0");
				result.put("msg", "定时任务【" + task.getName() + "】不是激活状态,请先激活");
			} else if (task.isStatus()) {// 执行状态
				result.put("state", "0");
				result.put("msg", "定时任务【" + task.getName() + "】已经启动,不要重复操作！");
			} else {// 激活未执行状态下 才能启动定时任务
				future = threadPoolTaskScheduler.schedule(new Task(task), new CronTrigger(task.getCron()));// 启动定时任务
				futureMap.put(task.getId(), future);
				System.out.println("testTask" + id + "================startCron()");

				task.setStatus(true);// 更新执行状态
				testTaskService.save(task);

				result.put("state", "1");
				result.put("msg", "定时任务【" + task.getName() + "】启动成功！");
			}
		} catch (Exception e) {
			result.put("state", "0");
			result.put("msg", "系统异常");
			log.error("系统异常:" + e);
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 关闭单一定时任务
	 */
	@ResponseBody
	@RequestMapping(value = "/stopTask/{id}", produces = { "application/json;charset=UTF-8" })
	public String stopTask(@PathVariable("id") int id) {
		JSONObject result = new JSONObject();
		try {
			TestTask task = testTaskService.findById(id);
			if (task.isStatus()) {// 执行状态下 才能关闭定时任务
				System.out.println("testTask" + id + "================stopCron()");
				task.setStatus(false);// 更新执行状态
				testTaskService.save(task);

				future = futureMap.get(id);
				if (future != null) {
					future.cancel(true);
				}
				result.put("state", "1");
				result.put("msg", "定时任务【" + task.getName() + "】关闭成功！");
			} else {
				result.put("state", "0");
				result.put("msg", "定时任务【" + task.getName() + "】未执行无需关闭！");
			}
		} catch (Exception e) {
			result.put("state", "0");
			log.error("系统异常:" + e);
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 动态修改一个定时任务的轮询时间
	 */
	@ResponseBody
	@RequestMapping(value = "/changeTaskCron", produces = { "application/json;charset=UTF-8" })
	public String changeTaskCron(TestTask task) {
		JSONObject result = new JSONObject();
		int taskID = task.getId();
		try {
			// TestTask task = testTaskService.getOneById(id);
			if (task.isStatus()) {// 执行状态下动态更新定时任务轮询时间
				stopTask(taskID);// 先停止，在开启

				// task.setCron(DateUtil.getCron(task.getCron()));
				testTaskService.save(task);

				startTask(taskID);// 启动任务
				result.put("state", "1");
				result.put("msg", "定时任务【" + task.getName() + "】修改成功！");
			} else {// 未执行状态下只更新数据库内容
				testTaskService.save(task);
				result.put("state", "1");
				result.put("msg", "定时任务【" + task.getName() + "】修改成功！");
			}
		} catch (Exception e) {
			result.put("state", "0");
			log.error("系统异常:" + e);
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 执行定时任务并处理相关业务逻辑
	 */
	private class Task implements Runnable {
		TestTask task = null;

		public Task() {
		}

		public Task(TestTask task) throws Exception {
			this();
			this.task = task;
			if (task.getTestSuite() == null)
				throw new Exception("can't doTask. TestTask:" + task.getName() + " not config Testsuite");
		}

		@Override
		public void run() {
			String threadName = Thread.currentThread().getName();
			System.out.println(threadName + "," + new Date());
			// System.out.println(id);
			testSuiteService.doTestSuite(task.getTestSuite().getId());
		}
	}

	// ==============================================增删改查=======================================================================================

	/**
	 * 查询任务列表
	 * 
	 * @param pageNumber
	 * @param pageSize
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "getTestTaskList/{pageNum}/{pageSize}", produces = { "application/json;charset=UTF-8" })
	public String getTestTaskList(@PathVariable("pageNum") int pageNum, @PathVariable("pageSize") int pageSize) {
		JSONObject result = new JSONObject();
		try {
			PageHelper.startPage(pageNum, pageSize);
			List<TestTask> pageInfo = testTaskService.findAll();// (pageNumber,pageSize,id);
			result.put("rows", pageInfo);
			result.put("total", pageInfo.size());
			result.put("state", "1");
		} catch (Exception e) {
			result.put("state", "0");
			e.printStackTrace();
			log.error("",e);
		}
		return result.toString();
	}

	/**
   * 查询任务（未分页）
   * @param pageNumber
   * @param pageSize
   * @return
   */
  @ResponseBody
  @RequestMapping(value="getTestTask",produces = { "application/json;charset=UTF-8" })
  public  String getTestTaskList(){
		JSONObject result = new JSONObject();
		try {
			List<TestTaskVO> list = new ArrayList<>();
			List<TestTask> pageInfo = testTaskService.findAll();
			for (TestTask testTask : pageInfo) {
				Integer suiteId = testTask.getTestSuite().getId();
				Integer[] caseIds = testSuiteCaseService.findCaseIdsBysuiteId(suiteId);
				TestTaskVO testTaskVO = new TestTaskVO(testTask,caseIds.length); 
				list.add(testTaskVO);
			}
			result.put("rows",list);
			result.put("total",list.size());
			result.put("state", "1");
		} catch (Exception e) {
			result.put("state", "0");
			e.printStackTrace();
			log.error("",e);
		}
		return result.toString();
	}
  
  /**
   * 查询任务相关数据
   * @param pageNumber
   * @param pageSize
   * @return
   */
  @ResponseBody
  @RequestMapping(value="getTestTaskById/{testTaskId}",produces = { "application/json;charset=UTF-8" })
  public  String getTestTaskById(@PathVariable("testTaskId") int testTaskId){
		JSONObject result = new JSONObject();
		try {
			TestTask testTask = testTaskService.findById(testTaskId);
			TestTaskVO testTaskVO = new TestTaskVO(testTask);
			result.put("rows",testTaskVO);
			result.put("total",1);
			result.put("state", "1");
		} catch (Exception e) {
			result.put("state", "0");
			e.printStackTrace();
			log.error("",e);
		}
		return result.toString();
	}

	/**
	 * 新增任务
	 * 
	 * @param testTask
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addTestTask", produces = { "application/json;charset=UTF-8" })
	public String addTestTask(@RequestBody TestTaskVO taskVO) {
		// System.out.println(TestTask.toString());
		TestTask tt = new TestTask();
		JSONObject result = new JSONObject();
		try {
			BeanUtils.copyProperties(taskVO, tt);
			TestSuite ts = new TestSuite();
			ts.setId(taskVO.getTestSuiteId());
			tt.setTestSuite(ts);
			testTaskService.save(tt);
			result.put("state", "1");
		} catch (Exception e) {
			result.put("state", "0");
			log.error("",e);
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 修改任务
	 * 
	 * @param testTask
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/updateTestTask", produces = { "application/json;charset=UTF-8" })
	public String updateTestTask(@RequestBody TestTaskVO taskVO) {
		JSONObject result = new JSONObject();
		TestTask task = new TestTask();
		try {
			BeanUtils.copyProperties(taskVO, task);
			TestSuite ts = new TestSuite();
			ts.setId(taskVO.getTestSuiteId());
			task.setTestSuite(ts);
			if (!task.isStatus()) {// 执行状态不能更新定时任务
				testTaskService.save(task);
				result.put("state", "1");
			} else {
				result.put("state", "0");
				result.put("msg", "定时任务【" + task.getName() + "】执行中,不能修改！");
			}
		} catch (Exception e) {
			result.put("state", "0");
			log.error("",e);
			e.printStackTrace();
		}
		return result.toString();
	}

	/**
	 * 删除任务
	 * 
	 * @param request
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/deleteTestTask/{ids}", produces = { "application/json;charset=UTF-8" })
	public String deleteTestTask(@PathVariable("ids") String ids) {
		String[] list = ids.split(",");
		JSONObject result = new JSONObject();
		try {
			for (int i = 0; i < list.length; i++) {
				int id = Integer.parseInt(list[i]);
				testTaskService.delete(id);
			}
			result.put("state", "1");
		} catch (Exception e) {
			result.put("state", "0");
			log.error("",e);
			e.printStackTrace();
		}
		return result.toString();
	}
}
