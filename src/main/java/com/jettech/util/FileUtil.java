package com.jettech.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.commons.io.FileUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileUtil {
	final static Logger logger = LoggerFactory.getLogger(FileUtil.class);
	private static final String ENCODING = "GBK";
	private static final String NEW_LINE_CHAR = "\r\n";

	public static List<String> readFileToList(String fileName) {
		return readFileToList(fileName, ENCODING);
	}

	public static List<String> readFileToList(String fileName, String encoding) {
		List<String> list = new ArrayList<String>();

		File file = new File(fileName);
		// 判断文件是否存在
		if (file.isFile() && file.exists()) {
			// 考虑到编码格式
			FileInputStream stream = null;
			InputStreamReader read = null;
			try {
				stream = new FileInputStream(file);
				read = new InputStreamReader(stream, encoding);
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				while ((lineTxt = bufferedReader.readLine()) != null) {
					list.add(lineTxt);
				}
			} catch (IOException e) {
				logger.error("readTxtFile error.", e);
				e.printStackTrace();
			} finally {
				try {
					if (read != null)
						read.close();
					if (stream != null)
						stream.close();
				} catch (Exception e2) {
					logger.error("close file steam or reader error.", e2);
					e2.printStackTrace();
				}
			}
		}

		return list;
	}

	public static String readFileToString(String fileName, String encoding) {
		StringBuilder builder = new StringBuilder();
		File file = new File(fileName);
		if (file.isFile() && file.exists()) {
			try {
				List<String> list = FileUtil.readFileToList(fileName, encoding);
				for (String str : list) {
					builder.append(str);
				}
			} catch (Exception e) {
				logger.error("readFileToString error.file: " + fileName + " encoding: " + encoding, e);
			}
		}
		return builder.toString();
	}

	public static List<String> readTxtFile(File file, int readMaxLine) {

		List<String> list = new ArrayList<String>();
		if (file.isFile() && file.exists()) {
			// 考虑到编码格式
			FileInputStream stream = null;
			InputStreamReader read = null;
			try {
				stream = new FileInputStream(file);
				read = new InputStreamReader(stream, ENCODING);
				BufferedReader bufferedReader = new BufferedReader(read);
				String lineTxt = null;

				int i = 1;
				while ((lineTxt = bufferedReader.readLine()) != null && i <= readMaxLine) {
					list.add(lineTxt);
					i++;
				}
			} catch (IOException e) {
				logger.error("readTxtFile error.", e);
				e.printStackTrace();
			} finally {
				try {
					if (read != null)
						read.close();
					if (stream != null)
						stream.close();
				} catch (Exception e2) {
					logger.error("close file steam or reader error.", e2);
					e2.printStackTrace();
				}
			}
		}

		return list;
	}

	public static boolean canWrite(String fileNameString) {
		File file = new File(fileNameString);
		return file.canWrite();
	}

	public static void writeTxtFile(FileOutputStream stream, List<String> list) {
		OutputStreamWriter writer = null;
		try {
			writer = new OutputStreamWriter(stream, ENCODING);

			// BufferedWriter bufferedWriter = writer.
			for (String string : list) {

				writer.append(string + NEW_LINE_CHAR);

			}
			writer.close();
		} catch (UnsupportedEncodingException e) {
			logger.error("", e);
		} catch (IOException e) {
			logger.error("write file error.", e);
		} finally {
			try {
				if (writer != null)
					writer.close();
			} catch (IOException e) {
				logger.error("close OutputStreamWriter error.", e);
			}
		}
	}

	/**
	 * write file to disk with append
	 * 
	 * @param fileNameString
	 *            -- file name with full path
	 * @param list
	 *            <String> -- to be write contents
	 */
	public static void writeTxtFile(String fileNameString, List<String> list) {
		writeTxtFile(fileNameString, list, true);
	}

	public static void writeTxtFile(String fileNameString, List<String> list, boolean append) {
		writeTxtFile(fileNameString, list, append, ENCODING);
	}

	/**
	 * write file to disk
	 * 
	 * @param fileNameString
	 *            -- file name with full path
	 * @param list
	 *            <String> -- to be write contents
	 * @param append
	 *            -- append or replace the file
	 */
	public static void writeTxtFile(String fileNameString, List<String> list, boolean append, String encoding) {
		File file = new File(fileNameString);
		if (!file.exists()) {
			File parent = file.getParentFile();
			if (parent != null && !parent.exists()) {
				parent.mkdirs();
			}
		}

		FileOutputStream stream = null;
		OutputStreamWriter writer = null;
		// FileLock lock = null;
		try {
			stream = new FileOutputStream(fileNameString, append);

			writer = new OutputStreamWriter(stream, encoding);
			// BufferedWriter bufferedWriter = writer.
			for (String string : list) {
				writer.append(string + NEW_LINE_CHAR);
			}
		} catch (IOException e) {
			logger.error("writeTxtFile error.", e);
			e.printStackTrace();
		} finally {
			try {
				if (writer != null)
					writer.close();
				if (stream != null)
					stream.close();
			} catch (IOException e2) {
				logger.error("close file steam or reader error.", e2);
				e2.printStackTrace();
			}
		}

	}

	public static boolean deleteDir(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					return false;
				}
			}
		}

		// The directory is now empty so now it can be smoked
		return dir.delete();
	}

	public static boolean existFile(String fileName) {
		File file = new File(fileName);
		// 判断文件是否存在
		if (file.isFile() && file.exists()) {
			return true;
		} else
			return false;
	}

	/**
	 * get all file's first line in folder
	 * 
	 * @param parentFolderName
	 * @param folder
	 * @return Map<folderName,List<fileRirstLine content>>
	 * @throws Exception
	 */
	private static Map<String, List<String>> getFolderFilesFirstRow(String parentFolderName, File folder) {
		Map<String, List<String>> map = new HashMap<String, List<String>>();
		if (folder.listFiles() != null && folder.listFiles().length > 0) {
			// loop all file in the folder

			for (File f : folder.listFiles()) {
				if (f.isFile()) {
					List<String> strList = readTxtFile(f, 1);
					if (strList != null && strList.size() == 1) {
						List<String> table = null;
						String tableName = parentFolderName + "." + folder.getName();
						if (map.containsKey(tableName)) {
							table = map.get(tableName);
						} else {
							table = new ArrayList<String>();
							map.put(tableName, table);
						}
						table.add(strList.get(0));
					}
				}
				if (f.isDirectory()) {
					String key = parentFolderName + "." + f.getName();
					Map<String, List<String>> map2 = getFolderFilesFirstRow(parentFolderName, f);
					map.put(key, map2.get(key));
					// logger.error("data directory:" + folder.getPath() +
					// " contains sub directory");
				}
			}
		} else {
			logger.error("data directory " + folder.getPath() + " not contains files");
		}

		return map;
	}

	/**
	 * get dataTable's schema from dataFile first row
	 * 
	 * @param dataDir
	 * @return Map<String, List<String>>
	 * @throws Exception
	 */
	public static Map<String, List<String>> readFilesFirstRow(String dataDir) throws Exception {

		Map<String, List<String>> map = new HashMap<String, List<String>>();

		File rootDir = new File(dataDir);
		if (!rootDir.exists())
			return map;

		if (!rootDir.isDirectory())
			throw new Exception("dirParameter not is directory");
		if (rootDir.listFiles() != null && rootDir.listFiles().length > 0) {
			// fit database folder
			// current folder maybe is dataBaseName
			for (File folderFile : rootDir.listFiles()) {
				Map<String, List<String>> mapTableMap = getFolderFilesFirstRow(folderFile.getName(), folderFile);
				for (String tableName : (mapTableMap.keySet())) {
					if (map.containsKey(tableName)) {
						map.get(tableName).addAll(mapTableMap.get(tableName));
					} else {
						map.put(tableName, mapTableMap.get(tableName));
					}
				}
			}
		} else {
			logger.info("data directory: " + rootDir + " is empty");
		}

		return map;
	}

	/**
	 * the traditional io way
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray(String filename) throws IOException {

		File f = new File(filename);
		if (!f.exists()) {
			throw new FileNotFoundException(filename);
		}

		ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(f));
			int buf_size = 1024;
			byte[] buffer = new byte[buf_size];
			int len = 0;
			while (-1 != (len = in.read(buffer, 0, buf_size))) {
				bos.write(buffer, 0, len);
			}
			return bos.toByteArray();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			bos.close();
		}
	}

	/**
	 * NIO way
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray2(String filename) throws IOException {

		File f = new File(filename);
		if (!f.exists()) {
			throw new FileNotFoundException(filename);
		}

		FileChannel channel = null;
		FileInputStream fs = null;
		try {
			fs = new FileInputStream(f);
			channel = fs.getChannel();
			ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
			while ((channel.read(byteBuffer)) > 0) {
				// do nothing
				// System.out.println("reading");
			}
			return byteBuffer.array();
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				channel.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				fs.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Mapped File way MappedByteBuffer 可以在处理大文件时，提升性能
	 * 
	 * @param filename
	 * @return
	 * @throws IOException
	 */
	public static byte[] toByteArray3(String filename) throws IOException {

		FileChannel fc = null;
		try {
			fc = new RandomAccessFile(filename, "r").getChannel();
			MappedByteBuffer byteBuffer = fc.map(MapMode.READ_ONLY, 0, fc.size()).load();
			System.out.println(byteBuffer.isLoaded());
			byte[] result = new byte[(int) fc.size()];
			if (byteBuffer.remaining() > 0) {
				// System.out.println("remain");
				byteBuffer.get(result, 0, byteBuffer.remaining());
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			throw e;
		} finally {
			try {
				fc.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}