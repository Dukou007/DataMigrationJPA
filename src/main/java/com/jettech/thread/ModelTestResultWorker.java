package com.jettech.thread;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.jettech.entity.ModelTestResultItem;
import com.jettech.entity.TestResultItem;
import com.jettech.service.ITestResultItemService;
import com.jettech.service.ModelTestResultItemService;
import com.jettech.util.SpringUtils;

/**
 * 实现测试结果的写库操作
 * 
 * @author tan
 *
 */
// @Component
public class ModelTestResultWorker implements Runnable {
	private static Logger logger = LoggerFactory.getLogger(ModelTestResultWorker.class);
			
	private BlockingQueue<ModelTestResultItem> _itemQueue = null;
	private volatile boolean isRunning = true;

	// @Autowired

	public ModelTestResultWorker(BlockingQueue<ModelTestResultItem> itemQueue) {
		_itemQueue = itemQueue;

	}

	public void stop() {
		isRunning = false;
	}

	@Override
	public void run() {
		ModelTestResultItemService resultItemService = SpringUtils.getBean(ModelTestResultItemService.class);
		// int freeTimes = 0;
		List<ModelTestResultItem> itemList = new ArrayList<>();
		int waitCount = 0;
		int batch=0;
		int batchSize=10;
		while (true) {
			try {
				Thread.sleep(1);
				waitCount++;
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			if (!isRunning || waitCount > 1000 * 60) {
				logger.info("last items:"+_itemQueue.size());
				// 收到中断信号，将队列中的所有数据写数据库，并中断
				while (true) {
				//在多线程情况下使用size获取会导致循环次数不够
				//for (int i = 0; i < _itemQueue.size(); i++) {
					ModelTestResultItem itemModel = _itemQueue.poll();
					if (itemModel == null){
						logger.info("已处理所有的消息:"+_itemQueue.size());
						break;
					}
					itemList.add(itemModel); 
					if (itemList.size() >= batchSize) {
						resultItemService.addBatch(itemList);
						batch++;
						logger.info("保存结果明细:"+batch+"*"+itemList.size());
						itemList.clear();
					}
//					System.out.println("1==========="+_itemQueue.size());
				//}
				}
				// 向数据库批量插入
				if (itemList.size() > 0) {
					resultItemService.addBatch(itemList);
					logger.info("保存剩余的明细:"+itemList.size());
					itemList.clear();
				}
//				System.out.println("2=============="+_itemQueue.size());
				logger.info("退出结果明细处理进程");
				break;
			}
			// 每1000笔明细保存一次
			if (_itemQueue.size() > batchSize) {
				for (int i = 0; i < batchSize; i++) {
					ModelTestResultItem itemModel = _itemQueue.poll();
					if (itemModel == null)
						continue;
					itemList.add(itemModel);
				}

				// 向数据库批量插入
				if (itemList.size() > 0) {
					resultItemService.addBatch(itemList);
					batch++;
					logger.info("保存结果明细:"+batch+"*"+itemList.size());
					itemList.clear();
				}

//				System.out.println("3==============="+_itemQueue.size());
				waitCount = 0;
			}

		}

	}

}
