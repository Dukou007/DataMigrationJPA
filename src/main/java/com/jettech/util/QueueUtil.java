package com.jettech.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/*
 * 创建2个公共的队列，用于存、取SQL的查询结果
 */
public class QueueUtil {

	
	private static QueueUtil instance=null;
	
	private static BlockingQueue<Map<List<Object>,List<Object>>> SourceQueue = new LinkedBlockingQueue<>();
	private static BlockingQueue<Map<List<Object>,List<Object>>> targetQueue = new LinkedBlockingQueue<>();
	
	public static synchronized  QueueUtil  getInstance() {

		if(instance == null) {
			instance = new QueueUtil();
		}
		return instance;
	}

	public BlockingQueue<Map<List<Object>,List<Object>>> getSourceQueue() {
		return SourceQueue;
	}


	public BlockingQueue<Map<List<Object>,List<Object>>> getTargetQueue() {
		return targetQueue;
	}

	
	
	
}
