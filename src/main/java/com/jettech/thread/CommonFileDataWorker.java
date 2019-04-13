package com.jettech.thread;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;

import com.ibm.db2.jcc.am.br;
import com.jettech.domain.CompareCaseModel;
import com.jettech.domain.FieldModel;
import com.jettech.domain.FileModel;
import com.jettech.domain.ModelCaseModel;
import com.jettech.domain.QualityTestCaseModel;
import com.jettech.entity.TestFile;
import com.jettech.util.DateUtil;
import com.jettech.util.SnowFlake;

/**
 * 从文本文件中获取数据的生产者(一次性获取所有数据,不分页)
 * 
 * @author tan
 *
 */
public class CommonFileDataWorker extends QualityBaseDataWorker implements Runnable {

	public CommonFileDataWorker(BlockingQueue<QualityBaseData> queue, FileModel fileModel) throws Exception {
		super(queue, fileModel);
	}

	SnowFlake snowFlake = new SnowFlake(1, 1);
	// static final Logger logger = LoggerFactory.getLogger(DataWorker.class);

	public void stop() {
		_isRunning = false;
	}

	public void run() {
		switch (fileModel.getDataSoruce().getFileType()) {
		case TXT:

			break;
		case CSV:
			doCSVCommonDataWork(fileModel);
			break;
		case DOC:

			break;
		case DBF:

			break;
		case XML:

			break;
		case None:
		default:
		}
	}

	private void doCSVCommonDataWork(FileModel fileModel) {
		try

		{
			long beginTime = new Date().getTime();
			// 获取数据到Map
			Map<String, List<Object>> map = null;

			// 获取文件绝对路径
			String path = fileModel.getDataSoruce().getFilePath();

			// 构建文件对象
			File file = new File(path);

			// 使用文件对象创建FileInputStream对象
			FileInputStream inputStream = null;

			// 使用文件输入流对象构造Reader对象
			InputStreamReader reader = null;

			// 使用Reader对像构建BufferedReader对象
			BufferedReader bf = null;

			// 获取数据列
			this.filedColumns = fileModel.getColumnList();

			try {
				List<String[]> dataList = new ArrayList<>();

				reader = new InputStreamReader(new FileInputStream(file), fileModel.getDataSoruce().getCharacterSet());

				bf = new BufferedReader(reader, 20 * 1024 * 1024);// 用20M的缓冲读取文本文件
				// 按行读取字符串
				String line;
				while ((line = bf.readLine()) != null) {
					// 以逗号分割，不包括双引号中的逗号。
					String[] lines = line.trim().split(
							// ,(?=([^\"]*\"[^\"]*\")*[^\"]*$)
							fileModel.getDataSoruce().getColumnSpeater() + "(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)",
							-1);

					// 处理文本限定符
					for (int i = 0; i < lines.length; i++) {
						String column = lines[i];
						column.replace(fileModel.getDataSoruce().getTextQualifier(), "");// 处理文本中的"
					}

					// 把一行表数据放入集合
					dataList.add(lines);

					// 把数据读到内存
					map = getDataRows(dataList, keyMap);
				}
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				if (inputStream != null) {
					inputStream.close();
				}
				if (reader != null) {
					reader.close();
				}
			}
			if (map.size() == 0) {
				logger.info("未读取到有效数据");
			}
			logger.info("读取数据" + map.size() + "耗时:" + DateUtil.getEclapsedTimesStr(beginTime));

			// 将数据存储到页对象中
			QualityCommonData data = new QualityCommonData();
			data.setTestQuery(testQuery);
			data.setTestQueryId(testQuery.getId());
			data.setMap(map);
			// getMixMaxKey(page, map.keySet());

			// 将数据页存入队列
			// 如果BlockQueue没有空间,则调用此方法的线程被阻断直到BlockingQueue里面有空间再继续.
			queue.put(data);

			logger.info(String.format("[%s]获取数据行数:[%d]", this.testQuery.getDataSource().getName(), map.size()));
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("thread interrupted error.", e);
		} catch (Exception e) {
			logger.error("get data error.", e);
		} finally {
			logger.info("退出生产者线程！");
			_isRunning = false;
		}

	}

	/**
	 * 从指定的数据集中获取数据
	 * 
	 * @param rs
	 * @param keyMap
	 * @return
	 * @throws SQLException
	 */
	protected Map<String, List<Object>> getDataRows(List<String[]> dataList, Map<String, FieldModel> keyMap)
			throws SQLException {
		Map<String, List<Object>> map = new HashMap<>();

		Integer maxNullKeyCount = testQuery.getMaxNullKeyCount();
		Integer maxDuplKeyCount = testQuery.getMaxDuplicatedKeyCount();
		List<Object> data = new ArrayList<Object>();
		for (int row = 0; row < dataList.size(); row++) {
//		while (rs.next()) {
			String keyValue = "";
			data.clear();
			String[] strList = dataList.get(row);
			if (keyMap == null || keyMap.size() == 0) {
				// 当没有设置key字段时,使用雪花算法产生唯一的Key(仅限用于数据质量监查中)
				keyValue = String.valueOf(snowFlake.nextId());
				for (int i = 1; i <= this.filedColumns.size(); i++) {
					data.add(strList[i]);
				}
			} else {
				for (int i = 1; i <= this.filedColumns.size(); i++) {
					data.add(strList[i]);
					String columnName = this.filedColumns.get(i).getName();
					if (keyMap.containsKey(columnName)) {
						if (strList[i] != null) {
							keyValue += "|" + strList[i].toString().trim();
						} else {
							keyValue += "|[NULL]";
						}
					}
				}
			}

			if (keyValue.length() > 0 && keyValue.substring(0, 1).equals("|"))
				keyValue = keyValue.substring(1);
			if (keyValue == null || keyValue.trim().length() == 0 || keyValue.equals("[NULL]")) {
				// logger.warn(String.format("key'value is null or empty.
				// key:[%s] data:[%s]", keyMap.keySet(),
				// data.toString()));
				nullOrEmptyKeyCount = nullOrEmptyKeyCount + 1;
//				continue;
			} else {
				if (map.containsKey(keyValue)) {
					// logger.warn(String.format("key'value is Duplicated.
					// key:[%s] keyValue:[%s] data:[%s]",
					// keyMap.keySet(), keyValue, data.toString(),
					// map.get(keyValue).toString()));
					duplicatedKeyCount = duplicatedKeyCount + 1;
//					continue;
				} else {
					map.put(keyValue, data);
				}
			}

			if (maxNullKeyCount != null && maxNullKeyCount > 0 && nullOrEmptyKeyCount > maxNullKeyCount) {
				logger.info(
						String.format("读数中断,空键值列数量超过允许:current:[%d] max:[%d]", nullOrEmptyKeyCount, maxNullKeyCount));
				break;
			}

			// 如果有超过[#100]的重复键值,退出获取数据过程
			if (maxDuplKeyCount != null && maxDuplKeyCount > 0 && duplicatedKeyCount > maxDuplKeyCount) {
				logger.info(
						String.format("读数中断,重复键值列数量超过允许:current:[%d] max:[%d]", duplicatedKeyCount, maxDuplKeyCount));
				break;
			}
		}
		return map;
	}
}
