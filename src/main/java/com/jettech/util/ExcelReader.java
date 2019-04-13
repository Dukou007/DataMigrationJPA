package com.jettech.util;

import java.io.File;

import java.io.FileInputStream;

import java.io.IOException;

import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import org.apache.poi.ss.usermodel.Cell;

import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.ss.usermodel.Sheet;

import org.apache.poi.ss.usermodel.Workbook;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.jettech.entity.CompFields;
import com.jettech.entity.MapperEntity;
import com.jettech.util.ExcelUtil;
import com.jettech.util.StringUtil;

/**
 * 
 * @描述：测试excel读取
 * 
 * 				导入的jar包
 * 
 *               poi-3.8-beta3-20110606.jar
 * 
 *               poi-ooxml-3.8-beta3-20110606.jar
 * 
 *               poi-examples-3.8-beta3-20110606.jar
 * 
 *               poi-excelant-3.8-beta3-20110606.jar
 * 
 *               poi-ooxml-schemas-3.8-beta3-20110606.jar
 * 
 *               poi-scratchpad-3.8-beta3-20110606.jar
 * 
 *               xmlbeans-2.3.0.jar
 * 
 *               dom4j-1.6.1.jar
 * 
 *               jar包官网下载地址：http://poi.apache.org/download.html
 * 
 *               下载poi-bin-3.8-beta3-20110606.zipp
 * 
 * 
 */

@Component
public class ExcelReader {

	private static Logger logger = LoggerFactory.getLogger(ExcelReader.class);
	/** 总行数 */

	private int totalRows = 0;

	/** 总列数 */

	private int totalCells = 0;

	/** 错误信息 */

	private String errorInfo;

	/** 忽略的行数 */
	private int ignoreRows;

//	private Logger logger;
	/** 构造方法 */

	public ExcelReader() {

	}

	/**
	 * 
	 * @描述：得到总行数
	 * 
	 * @参数：@return
	 * 
	 * @返回值：int
	 */

	public int getTotalRows() {

		return totalRows;

	}

	/**
	 * 
	 * @描述：得到总列数
	 * 
	 * @参数：@return
	 * 
	 * @返回值：int
	 */

	public int getTotalCells() {

		return totalCells;

	}

	/**
	 * 
	 * @描述：得到错误信息
	 * 
	 * @参数：@return
	 * 
	 * @返回值：String
	 */

	public String getErrorInfo() {

		return errorInfo;

	}

	public int getIgnoreRows() {
		return ignoreRows;
	}

	/**
	 * 
	 * @描述：验证excel文件
	 * 
	 * @参数：@param filePath 文件完整路径
	 * 
	 * @参数：@return
	 * 
	 * @返回值：boolean
	 */

	public boolean validateExcel(String filePath) {

		/** 检查文件名是否为空或者是否是Excel格式的文件 */

		if (filePath == null || !(ExcelUtil.isExcel2003(filePath) || ExcelUtil.isExcel2007(filePath))) {

			errorInfo = "文件名不是excel格式";

			return false;

		}

		/** 检查文件是否存在 */

		File file = new File(filePath);

		if (file == null || !file.exists()) {

			errorInfo = "文件不存在";

			return false;

		}

		return true;

	}

	/**
	 * 
	 * @描述：根据文件名读取excel文件
	 * 
	 * @参数：@param filePath 文件完整路径
	 * 
	 * @参数：@return
	 * 
	 * @返回值：List
	 */

	public List<List<Map<String, Object>>> read(String filePath, Class clazz, int ignoreRows) {

		List<List<Map<String, Object>>> resultList = new ArrayList<List<Map<String, Object>>>();

		InputStream is = null;

		try {

			/** 验证文件是否合法 */

			if (!validateExcel(filePath)) {

				System.out.println(errorInfo);

				return null;

			}

			/** 判断文件的类型，是2003还是2007 */

			boolean isExcel2003 = true;

			if (ExcelUtil.isExcel2007(filePath)) {

				isExcel2003 = false;

			}

			/** 调用本类提供的根据流读取的方法 */

			File file = new File(filePath);

			is = new FileInputStream(file);

			resultList = read(is, isExcel2003, clazz, ignoreRows);

			is.close();

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			if (is != null) {

				try {

					is.close();

				} catch (IOException e) {

					is = null;

					e.printStackTrace();

				}

			}

		}

		/** 返回最后读取的结果 */

		return resultList;

	}

	/**
	 * 
	 * @描述：根据流读取Excel文件
	 * 
	 * @参数：@param inputStream
	 * 
	 * @参数：@param isExcel2003
	 * 
	 * @参数：@return
	 * 
	 * @返回值：List
	 */

	public List<List<Map<String, Object>>> read(InputStream inputStream, boolean isExcel2003, Class clazz,
			int ignoreRows) {

		List<List<Map<String, Object>>> dataLst = null;

		try {

			/** 根据版本选择创建Workbook的方式 */

			Workbook wb = null;

			if (isExcel2003) {
				wb = new HSSFWorkbook(inputStream);
			} else {
				wb = new XSSFWorkbook(inputStream);
			}
			dataLst = read(wb, clazz, ignoreRows);

		} catch (IOException e) {

			e.printStackTrace();

		}

		return dataLst;

	}

	/**
	 * 
	 * @描述：读取数据
	 * 
	 * @参数：@param Workbook
	 * 
	 * @参数：@return
	 * 
	 * @返回值：List<List<String>>
	 */

	private List<List<Map<String, Object>>> read(Workbook wb, Class clazz, int ignoreRows) {

		List<List<Map<String, Object>>> sheetList = new ArrayList<List<Map<String, Object>>>();
		for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
			Sheet sheet = wb.getSheetAt(sheetIndex);
			String sheetName = sheet.getSheetName();
			System.out.println("*******" + sheetName + "********");

			List<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();

			/** 得到Excel的行数 */

			this.totalRows = sheet.getPhysicalNumberOfRows();

			/** 得到Excel的列数 */

			if (this.totalRows >= 1 && sheet.getRow(0) != null) {

				this.totalCells = sheet.getRow(0).getPhysicalNumberOfCells();

			}

			String[] columName = new String[totalCells];// 相应的javabean类的属性名称数组

			Field[] fields = clazz.getDeclaredFields();

			/** 循环Excel的行 */

			for (int j = ignoreRows; j < totalRows; j++) {
				logger.info("row:" + j);
				Row row = (Row) sheet.getRow(j);
				if (row == null) {
					logger.info("rows end");
					break;
				}
				Map<String, Object> resultMap = new HashMap<String, Object>();
				for (int i = 0; i < totalCells; i++) {
					// 取出当前的cell的值和javabean类的属性 放入map中

//				String VVV = getStringCellValue(row.getCell(i));
					Cell cell = row.getCell(i);
					try {
/*						if (i == fields.length-1) {
							resultMap.put("newTableName", sheetName);

						}*/
						if (cell != null && !cell.toString().trim().equals("")
								&& !StringUtil.isAllMandarin(cell.toString().trim())) {

							String cellValue = getStringValue(cell);
							resultMap.put(fields[i].getName(), cellValue);

						} else {
							resultMap.put(fields[i].getName(), null);
						}

					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				mapList.add(resultMap);
			}
			sheetList.add(mapList);
		}
		return sheetList;

	}

	
	public String readExcelTitle(String filePath) {
		// TODO Auto-generated method stub

		InputStream is = null;
		String title = "";
		try {

			/** 验证文件是否合法 */

			if (!validateExcel(filePath)) {

				System.out.println(errorInfo);

				return null;

			}

			/** 判断文件的类型，是2003还是2007 */

			boolean isExcel2003 = true;

			if (ExcelUtil.isExcel2007(filePath)) {

				isExcel2003 = false;

			}

			/** 调用本类提供的根据流读取的方法 */

			File file = new File(filePath);

			is = new FileInputStream(file);

			title = readTitle(is, isExcel2003);

			is.close();

		} catch (Exception ex) {

			ex.printStackTrace();

		} finally {

			if (is != null) {

				try {

					is.close();

				} catch (IOException e) {

					is = null;

					e.printStackTrace();

				}

			}

		}

		/** 返回最后读取的结果 */

		return title;
	}

	public String readTitle(InputStream inputStream, boolean isExcel2003) {
		// TODO Auto-generated method stub
		String title = "";
		try {

			/** 根据版本选择创建Workbook的方式 */

			Workbook wb = null;

			if (isExcel2003) {
				wb = new HSSFWorkbook(inputStream);
			} else {
				wb = new XSSFWorkbook(inputStream);
			}
			title = readTitle(wb);

		} catch (IOException e) {

			e.printStackTrace();

		}

		return title;
	}

	public String readTitle(Workbook wb) {

		Cell cell = null;
		for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
			Sheet sheet = wb.getSheetAt(sheetIndex);
			Row row = sheet.getRow(0);
			cell = row.getCell(1);
			return cell.toString();
		}

		return null;
	}

	private String getStringValue(Cell cell) {
		String strCell = "";
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_STRING:
			strCell = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_NUMERIC:
			strCell = String.valueOf(cell.getNumericCellValue());
			// strCell = cell.getStringCellValue();
			break;
		case Cell.CELL_TYPE_BOOLEAN:
			strCell = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_BLANK:
			strCell = "";
			break;
		default:
			strCell = "";
			break;
		}
		if (strCell.equals("") || strCell == null) {
			return "";
		}
		if (cell == null) {
			return "";
		}
		return strCell;
	}

	/**
	 * 利用反射将Map中的数据生成相应的T对象
	 * 
	 * @param map
	 * @param clazz
	 * @return T对象
	 * @throws Exception
	 */
	public <T> T toObject(Map map, Class clazz) throws Exception {
		T obj = (T) clazz.newInstance();
		Method[] methods = clazz.getDeclaredMethods();
		for (Method m : methods) {
			if (m.getName().startsWith("set")) {// 找到set方法
				String str = m.getName().substring(3);// 该set方法对应的属性名 首字母为大写
				String attribute = StringUtil.toLowerFirstOne(str);
				String value = (String) map.get(attribute);
				if (value != null) {
					m.invoke(obj, value);
				}
			}
		}
		return obj;
	}

	public static void main(String[] args) {
		ExcelReader excelReader = new ExcelReader();
		//String title = excelReader.readExcelTitle("d:\\mapper\\dataMapping_v0.9.xls");
		// System.out.println("*********"+title);

		List<List<Map<String, Object>>> resultList = excelReader.read("d:\\tmp\\compareKey.xlsx", CompFields.class, 1);
		for (List<Map<String, Object>> list : resultList) {

			for (Map<String, Object> map : list) {
				CompFields compFields = new CompFields();
				
					String str = map.get("id").toString();

					compFields.setId(Integer.valueOf(str.substring(0,str.length()-2)));
					compFields.setNewTableName(map.get("newTableName").toString());
					compFields.setNewKeyField(map.get("newKeyField").toString());
					compFields.setOldTableName(map.get("oldTableName").toString());
					compFields.setOldKeyField(map.get("oldKeyField").toString());

					System.out.println(compFields);
				

			}

		}
	}

}
