package com.jettech.thread;

import java.io.BufferedReader;
import java.io.DataInputStream;
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
import java.util.concurrent.BlockingQueue;
import com.jettech.domain.FieldModel;
import com.jettech.domain.QueryFileModel;
import com.jettech.service.IFileDataSourceService;
import com.jettech.util.DateUtil;

/**
 * 从文本文件中获取数据的生产者(一次性获取所有数据,不分页)
 * 
 * @author tan
 *
 */
public class CommonFileDataWorker extends FileBaseDataWorker implements Runnable {

	public CommonFileDataWorker(BlockingQueue<BaseData> queue, QueryFileModel testQuery) throws Exception {
		super(queue, testQuery);
	}

	IFileDataSourceService fileDataSourceService;

	public void stop() {
		_isRunning = false;
	}

	public void run() {
		switch (testQuery.getFileDataSource().getFileType()) {
		case TXT:
			doCSVCommonDataWork(testQuery);
			break;
		case CSV:
			doCSVCommonDataWork(testQuery);
			break;
		case Binary:
			doBinaryCommonDataWork(testQuery);
			break;
		case DBF:

			break;
		case XML:

			break;
		case None:
		default:
		}
	}

	private void doBinaryCommonDataWork(QueryFileModel targetQuery) {

		try {
			long beginTime = new Date().getTime();
			// 获取数据到Map
			Map<String, List<Object>> map = null;
			// 获取文件绝对路径
			String path = targetQuery.getFileDataSource().getFilePath();

			// 构建文件对象
			File file = new File(path);

			// 使用文件对象创建FileInputStream对象
			FileInputStream inputStream = null;

			// 使用文件输入流对象构造Reader对象
			InputStreamReader reader = null;

			// 使用Reader对像构建BufferedReader对象
			DataInputStream dis = null;

			// 获取数据列
			// this.filedColumns = targetQuery.getFileDataSource().gett

			try {
				List<List<Object>> dataList = new ArrayList<>();

				inputStream = new FileInputStream(file);

				dis = new DataInputStream(inputStream);// 用20M的缓冲读取文本文件
				// 按行读取字符串
				String line;
				int count = 1;
				String columnSpeater = targetQuery.getFileDataSource().getColumnSpeater();
				String textQualifier = targetQuery.getFileDataSource().getTextQualifier();
				if (",".equals(columnSpeater)) {
					byte[] b = new byte[1024];
					int len = b.length - 1;
					dis.read(b);
					String string = new String(b, 0, len, "GBK");
					String[] split = string.split(columnSpeater);

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
			CommonData data = new CommonData();
			data.setTestFileQuery(testQuery);
			data.setTestFileQueryId(testQuery.getId());
			data.setMap(map);
			// getMixMaxKey(page, map.keySet());

			// 将数据页存入队列
			// 如果BlockQueue没有空间,则调用此方法的线程被阻断直到BlockingQueue里面有空间再继续.
			queue.put(data);

			logger.info(String.format("[%s]获取数据行数:[%d]", this.testQuery.getFileDataSource().getName(), map.size()));
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

	private void doCSVCommonDataWork(QueryFileModel targetQuery) {
		/*
		 * fileDataSourceService = (IFileDataSourceService)
		 * SpringUtils.getBean(FileDataSourceServiceImpl.class); ResultVO testConnect =
		 * this.fileDataSourceService.testConnect(fileModel.getDataSoruce()); if
		 * (testConnect.isFlag()) {
		 */
		try {
			long beginTime = new Date().getTime();
			// 获取数据到Map
			Map<String, List<Object>> map = null;

			// 获取文件绝对路径
			String path = targetQuery.getFileDataSource().getFilePath();

			// 构建文件对象
			File file = new File(path);

			// 使用文件对象创建FileInputStream对象
			FileInputStream inputStream = null;

			// 使用文件输入流对象构造Reader对象
			InputStreamReader reader = null;

			// 使用Reader对像构建BufferedReader对象
			BufferedReader bf = null;

			// 获取数据列
			// this.filedColumns = targetQuery.getFileDataSource().gett

			try {
				List<List<Object>> dataList = new ArrayList<>();

				reader = new InputStreamReader(new FileInputStream(file),
						targetQuery.getFileDataSource().getCharacterSet());

				bf = new BufferedReader(reader, 20 * 1024 * 1024);// 用20M的缓冲读取文本文件
				// 按行读取字符串
				String line;
				int count = 1;
				String columnSpeater = targetQuery.getFileDataSource().getColumnSpeater();
				String textQualifier = targetQuery.getFileDataSource().getTextQualifier();
				if (",".equals(columnSpeater)) {
					while ((line = bf.readLine()) != null) {
						// 以逗号分割，不包括字段中的逗号。
						if (count > 1) {
							String[] lines = line.trim().split(
									// ,(?=([^\"]*\"[^\"]*\")*[^\"]*$)

									columnSpeater + "(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
							List<Object> list = new ArrayList<>();
							// 处理文本限定符，不包含字段中的文本限定符
							for (int i = 0; i < lines.length; i++) {
								/*
								 * int indexOf = lines[i].indexOf(textQualifier); int lastIndexOf =
								 * lines[i].lastIndexOf(textQualifier); String substring =
								 * lines[i].substring(indexOf + 1, lastIndexOf);
								 */
								Object substring = lines[i].replaceAll(textQualifier, "");
								// 处理文本中的"",添加到集合中
								list.add(substring);
							}

							// 把一行表数据放入集合
							dataList.add(list);

							// 把数据读到内存
							map = getDataRows(dataList, keyMap);
						}
						count++;
					}
				}
				if ("e".equals(columnSpeater.substring(1))) {
					while ((line = bf.readLine()) != null) {
						// 以逗号分割，不包括字段中的逗号。

						String[] lines = line.trim().split(columnSpeater, -1);
						List<Object> list = new ArrayList<>();
						// 处理文本限定符，不包含字段中的文本限定符
						for (int i = 0; i < lines.length; i++) {
							/*
							 * int indexOf = lines[i].indexOf(textQualifier); int lastIndexOf =
							 * lines[i].lastIndexOf(textQualifier); String substring =
							 * lines[i].substring(indexOf + 1, lastIndexOf);
							 */
							Object substring = lines[i];
							// 处理文本中的"",添加到集合中
							list.add(substring);
						}

						// 把一行表数据放入集合
						dataList.add(list);

						// 把数据读到内存
						map = getDataRows(dataList, keyMap);

					}
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
			CommonData data = new CommonData();
			data.setTestFileQuery(testQuery);
			data.setTestFileQueryId(testQuery.getId());
			data.setMap(map);
			// getMixMaxKey(page, map.keySet());

			// 将数据页存入队列
			// 如果BlockQueue没有空间,则调用此方法的线程被阻断直到BlockingQueue里面有空间再继续.
			queue.put(data);

			logger.info(String.format("[%s]获取数据行数:[%d]", this.testQuery.getFileDataSource().getName(), map.size()));
		} catch (InterruptedException e) {
			e.printStackTrace();
			logger.error("thread interrupted error.", e);
		} catch (Exception e) {
			logger.error("get data error.", e);
		} finally {
			logger.info("退出生产者线程！");
			_isRunning = false;
		}
	} /*
		 * else { logger.info("服务器连接失败"); } }
		 */

	/**
	 * 从指定的数据集中获取数据
	 * 
	 * @param rs
	 * @param keyMap
	 * @return
	 * @throws SQLException
	 */
	protected Map<String, List<Object>> getDataRows(List<List<Object>> dataList, Map<String, FieldModel> keyMap)
			throws SQLException {
		Map<String, List<Object>> map = new HashMap<>();

		Integer maxNullKeyCount = testQuery.getMaxNullKeyCount();
		Integer maxDuplKeyCount = testQuery.getMaxDuplicatedKeyCount();
		for (int row = 0; row < dataList.size(); row++) {
			List<Object> data = dataList.get(row);

			String keyValue = "";
			for (int i = 0; i < data.size(); i++) {

				String columnName = this.filedColumns.get(i).getName();
				if (keyMap.containsKey(columnName)) {
					if (data.get(i) != null) {
						keyValue += "|" + data.get(i).toString().trim();
					} else {
						keyValue += "|[NULL]";
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
