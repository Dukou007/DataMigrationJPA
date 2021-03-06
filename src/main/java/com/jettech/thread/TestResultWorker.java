package com.jettech.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jettech.entity.TestResultItem;
import com.jettech.service.ITestResultItemService;
import com.jettech.util.SpringUtils;

/**
 * 实现测试结果的写库操作
 * 
 * @author tan
 *
 */
// @Component
public class TestResultWorker implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(TestResultWorker.class);

	private BlockingQueue<TestResultItem> _itemQueue = null;
	private volatile boolean isRunning = true;
	private Integer _maxResultRows;
	// @Autowired

	public TestResultWorker(BlockingQueue<TestResultItem> itemQueue, Integer maxResultRows) {
		_itemQueue = itemQueue;
		_maxResultRows = maxResultRows;
	}

	public void stop() {
		isRunning = false;
	}

	@Override
	public void run() {
		int rowCount = 0;
		ITestResultItemService resultItemService = SpringUtils.getBean(ITestResultItemService.class);
		// int freeTimes = 0;
		List<TestResultItem> itemList = new ArrayList<>();
		int waitCount = 0;
		int batch = 0;
		int batchSize = 1000;// 没批次插入的数量
		int oneWaitTime = 1000 * 60;// 一次插入等待的最大时间
		while (true) {
			try {
				Thread.sleep(1);
				waitCount++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//
			if (_maxResultRows != null && _maxResultRows > 0 && rowCount > _maxResultRows) {
				isRunning = true;
			}
			// if (!isRunning || waitCount > 1000 * 60)
			if (!isRunning) {
				logger.info("last items:" + _itemQueue.size());
				// 收到中断信号，将队列中的所有数据写数据库，并中断
				while (true) {
					// 在多线程情况下使用size获取会导致循环次数不够
					// for (int i = 0; i < _itemQueue.size(); i++) {
					TestResultItem itemModel = _itemQueue.poll();
					if (itemModel == null) {
						logger.info("已处理所有的消息:" + _itemQueue.size());
						break;
					}
					// 因为oracle分页会多出RN列，目标sql是按条件查询的没有RN列，所以保存结果明细的时候不记录RN列
					if (!"RN".equalsIgnoreCase(itemModel.getColumnName())) {
						itemList.add(itemModel);
					}

					if (itemList.size() >= batchSize) {
						resultItemService.addBatch(itemList);
						rowCount += itemList.size();
						batch++;
						logger.info("保存结果明细:" + batch + "*" + itemList.size());
						itemList.clear();
					}
					// System.out.println("1==========="+_itemQueue.size());
					// }
				}
				// 向数据库批量插入
				if (itemList.size() > 0) {
					resultItemService.addBatch(itemList);
					rowCount += itemList.size();
					logger.info("保存剩余的明细:" + itemList.size());
					itemList.clear();
				}
				// System.out.println("2=============="+_itemQueue.size());
				logger.info("退出结果明细处理进程");
				break;
			}
			// 每1000笔明细保存一次
			if (_itemQueue.size() > batchSize) {
				for (int i = 0; i < batchSize; i++) {
					TestResultItem itemModel = _itemQueue.poll();
					if (itemModel == null)
						continue;
					if (!"RN".equalsIgnoreCase(itemModel.getColumnName())) {
						itemList.add(itemModel);
					}
				}
				// 向数据库批量插入
				if (itemList.size() > 0) {
					resultItemService.addBatch(itemList);
					rowCount += itemList.size();
					batch++;
					logger.info("保存结果明细:" + batch + "*" + itemList.size());
					itemList.clear();
				}

				// System.out.println("3==============="+_itemQueue.size());
				waitCount = 0;
			} else {
				// 如果等待超过1分钟,向数据库批量插入全部数据
				if (waitCount > oneWaitTime) {
					while (true) {
						try {
							Thread.sleep(1);
						} catch (InterruptedException e) {
							e.printStackTrace();
						}
						TestResultItem itemModel = _itemQueue.poll();
						if (itemModel == null)
							break;
						if (!"RN".equalsIgnoreCase(itemModel.getColumnName())) {
							itemList.add(itemModel);
						}
					}
					if (itemList.size() > 0) {
						logger.info("写结果明细等待超过:" + oneWaitTime / 1000 + "秒,开始进行强制写入,明细数量:[" + itemList.size() + "]");
						if (itemList.size() > 0) {
							resultItemService.addBatch(itemList);
							rowCount += itemList.size();
							batch++;
							logger.info("保存结果明细:" + batch + "*" + itemList.size());
							itemList.clear();
						}
					}else {
						break;
					}
				}
			}

		}

	}

}
