package com.jettech.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CellValue;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

/**
 * XSSF处理xlsx格式(Excel2007),HSSF处理xls格式(Excel2003)
 * 
 * @author tan
 *
 */
public class ExcelUtil {
	private final static String xls = "xls";
	private final static String xlsx = "xlsx";

	/**
	 * 
	 * @描述：是否是2003的excel，返回true是2003
	 * 
	 * @参数：@param filePath 文件完整路径
	 * 
	 * @参数：@return
	 * 
	 * @返回值：boolean
	 */

	public static boolean isExcel2003(String filePath) {

		return filePath.matches("^.+\\.(?i)(xls)$");

	}

	/**
	 * 
	 * @描述：是否是2007的excel，返回true是2007
	 * 
	 * @参数：@param filePath 文件完整路径
	 * 
	 * @参数：@return
	 * 
	 * @返回值：boolean
	 */

	public static boolean isExcel2007(String filePath) {

		return filePath.matches("^.+\\.(?i)(xlsx)$");

	}

	public Map<String, Object[][]> readExcelFile(String fileName) {
		return readExcelFile(fileName, ExcelUtil.isExcel2007(fileName));
	}

	public Map<String, Object[][]> readExcelFile(String fileName, Boolean isExcel2007) {
		Map<String, Object[][]> map = new HashMap<>();
		Workbook xwb = null;
		InputStream is = null;
		try {
			is = new FileInputStream(fileName);
			if (!isExcel2007) {
				HSSFWorkbook hssfBook = new HSSFWorkbook(is);
				xwb = hssfBook;
				// hssfBook.getNumberOfSheets();
			} else {
				XSSFWorkbook xxxfBook = new XSSFWorkbook(is);
				xwb = xxxfBook;
			}
			for (int i = 0; i < xwb.getNumberOfSheets(); i++) {
				Sheet sheet = xwb.getSheetAt(i);
				Object[][] arr = readExcelSheet(sheet);
				map.put(sheet.getSheetName(), arr);
			}
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			try {
//				if (xwb != null)
//					xwb.close();
				if (is != null)
					is.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return map;
	}

	public Object[][] readExcelSheet(Sheet sheet) {
		int rowCount = sheet.getLastRowNum();
		if (rowCount == 0)
			return null;
		int colCount = sheet.getRow(0) != null ? colCount = sheet.getRow(0).getPhysicalNumberOfCells() : 0;
		if (colCount == 0)
			return null;
		Object[][] arr = new Object[rowCount + 1][colCount];
		for (int i = 0; i < rowCount; i++) {
			Object[] row = arr[i];
			for (int j = 0; j < colCount; j++) {
				row[j] = sheet.getRow(i) != null ? sheet.getRow(i).getCell(j) : null;
			}
		}
		return arr;
	}

	public Object[][] readExcel2003Sheet(HSSFSheet sheet) {
		int rowCount = sheet.getLastRowNum();
		if (rowCount == 0)
			return null;
		int colCount = sheet.getRow(0) != null ? colCount = sheet.getRow(0).getPhysicalNumberOfCells() : 0;
		if (colCount == 0)
			return null;

		Object[][] arr = new Object[rowCount + 1][colCount];
		// HSSFRow row5 = sheet.getRow(4); // 获得工作薄的第五行
		// HSSFCell cell54 = row5.getCell(3);// 获得第五行的第四个单元格
		// cell54.setCellValue("测试纳税人名称");// 给单元格赋值
		for (int i = 0; i < rowCount; i++) {
			Object[] row = arr[i];
			for (int j = 0; j < colCount; j++) {
				row[j] = sheet.getRow(i) != null ? sheet.getRow(i).getCell(j) : null;
			}
		}

		return arr;
	}

	// 读取excel表格中的数据，path代表excel路径
	public Object[][] readExecl2007(String path) {
		try {
			// 读取的时候可以使用流，也可以直接使用文件名
			XSSFWorkbook xwb = new XSSFWorkbook(path);

			// 循环工作表sheet
			for (int numSheet = 0; numSheet < xwb.getNumberOfSheets(); numSheet++) {
				XSSFSheet xSheet = xwb.getSheetAt(numSheet);
				if (xSheet == null) {
					continue;
				}
				// 循环行row
				for (int numRow = 0; numRow <= xSheet.getLastRowNum(); numRow++) {
					XSSFRow xRow = xSheet.getRow(numRow);
					if (xRow == null) {
						continue;
					}
					// 循环列cell
					for (int numCell = 0; numCell <= xRow.getLastCellNum(); numCell++) {
						XSSFCell xCell = xRow.getCell(numCell);
						if (xCell == null) {
							continue;
						}
						// 输出值
						System.out.println("excel表格中取出的数据" + getValue(xCell));
					}
				}
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * 取出每列的值
	 *
	 * @param xCell
	 *            列
	 * @return
	 */
	private String getValue(XSSFCell xCell) {
		if (xCell.getCellType() == XSSFCell.CELL_TYPE_BOOLEAN) {
			return String.valueOf(xCell.getBooleanCellValue());
		} else if (xCell.getCellType() == XSSFCell.CELL_TYPE_NUMERIC) {
			return String.valueOf(xCell.getNumericCellValue());
		} else {
			return String.valueOf(xCell.getStringCellValue());
		}
	}

	public void createExcel() {
		// try {
		// String path = "D:/test.xlsx";
		// // 创建新的Excel 工作簿
		// XSSFWorkbook workbook = new XSSFWorkbook();
		// // 在Excel工作簿中建一工作表，其名为缺省值
		// // 如要新建一名为"用户表"的工作表，其语句为：
		// XSSFSheet sheet = workbook.createSheet("用户表");
		// // 在索引0的位置创建行（最顶端的行）
		// XSSFRow row = sheet.createRow((short) 0);
		// //在索引0的位置创建单元格（左上端）
		// XSSFCell cell = row.createCell((short) 0);
		// //创建单元格样式
		// CellStyle cellStyle = workbook.createCellStyle();
		// // 设置这些样式
		//// cellStyle.setFillForegroundColor(HSSFColor.SKY_BLUE.index);
		//// cellStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		//// cellStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		//// cellStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		//// cellStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		//// cellStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		//// cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		//
		// // 定义单元格为字符串类型
		// cell.setCellType(HSSFCell.CELL_TYPE_STRING);
		// // 在单元格中输入一些内容
		// cell = row.createCell((short) 0);
		// cell.setCellValue("用户id");
		// cell.setCellStyle(cellStyle);
		//
		// cell = row.createCell((short) 1);
		// cell.setCellValue("姓名");
		// cell.setCellStyle(cellStyle);
		//
		// cell = row.createCell((short) 2);
		// cell.setCellValue("别名");
		// cell.setCellStyle(cellStyle);
		//
		// cell = row.createCell((short) 3);
		// cell.setCellValue("密码");
		// cell.setCellStyle(cellStyle);
		//
		// cell = row.createCell((short) 4);
		// cell.setCellValue("外来id");
		// cell.setCellStyle(cellStyle);
		//
		// //查询数据库中所有的数据
		// VtUserMapper mapper = getMapper(VtUserMapper.class);
		// VtUserCriteria cri = new VtUserCriteria();
		// cri.createCriteria().andUserEnabledEqualTo(1);
		// List<VtUser> list = mapper.selectByExample(cri);
		// /*//第一个sheet第一行为标题
		// XSSFRow rowFirst = sheet.createRow(0);
		// rowFirst.setHeightInPoints(21.75f);*/
		// for (int i = 0; i < list.size(); i++) {
		// row = sheet.createRow((int) i + 1);
		// VtUser stu = (VtUser) list.get(i);
		// // 第四步，创建单元格，并设置值
		// row.createCell((short) 0).setCellValue(stu.getUserId());
		// row.createCell((short) 1).setCellValue(stu.getUserName());
		// row.createCell((short) 2).setCellValue(stu.getUserNameZn());
		// row.createCell((short) 3).setCellValue(stu.getUserPassword());
		// row.createCell((short) 4).setCellValue(stu.getUserForeignId());
		// sheet.autoSizeColumn((short) 0); //调整第一列宽度（自适应），只识别数字、字母
		// sheet.autoSizeColumn((short) 1); //调整第二列宽度
		// //调整第三列宽度,有中文，先判断这一列的最长字符串
		// int length = stu.getUserNameZn().getBytes().length;
		// sheet.setColumnWidth((short)2,(short)(length*2*256));
		// sheet.autoSizeColumn((short) 3); //调整第四列宽度
		// sheet.autoSizeColumn((short) 4); //调整第五列宽度
		//
		// /*Font font = workbook.createFont();
		// font.setFontHeightInPoints((short)18); //字体大小
		// sheet.setDefaultRowHeightInPoints(21.75f);
		// font.setFontName("楷体");
		// font.setBoldweight(Font.BOLDWEIGHT_BOLD); //粗体
		// font.setColor(HSSFColor.GREEN.index); //绿字- 字体颜色*/
		// }
		// // 新建一输出文件流
		// FileOutputStream fOut = new FileOutputStream(path);
		// // 把相应的Excel 工作簿存盘
		// workbook.write(fOut);
		// //清空缓冲区数据
		// fOut.flush();
		// // 操作结束，关闭文件
		// fOut.close();
		// System.out.println("文件生成...");
		// } catch (Exception e) {
		// System.out.println("已运行 xlCreate() : " + e);
		// }
	}

	// 修改excel表格，path为excel修改前路径（D:\\test.xlsx）
	public void writeExcel3(String path) {
		try {
			// 传入的文件
			FileInputStream fileInput = new FileInputStream(path);
			// poi包下的类读取excel文件

			// 创建一个webbook，对应一个Excel文件
			XSSFWorkbook workbook = new XSSFWorkbook(fileInput);
			// 对应Excel文件中的sheet，0代表第一个
			XSSFSheet sh = workbook.getSheetAt(0);
			// 修改excle表的第5行，从第三列开始的数据
			for (int i = 2; i < 4; i++) {
				// 对第五行的数据修改
				sh.getRow(4).getCell((short) i).setCellValue(100210 + i);
			}
			// 将修改后的文件写出到D:\\excel目录下
			FileOutputStream os = new FileOutputStream("D:\\修改后test.xlsx");
			// FileOutputStream os = new
			// FileOutputStream("D:\\test.xlsx");//此路径也可写修改前的路径，相当于在原来excel文档上修改
			os.flush();
			// 将Excel写出
			workbook.write(os);
			// 关闭流
			fileInput.close();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static Workbook getWorkBook(String filePath) {
		// 获得文件名

		File file = new File(filePath);

		// 创建Workbook工作薄对象，表示整个excel
		Workbook workbook = null;
		try {
			// 获取excel文件的io流
			InputStream is = new FileInputStream(file);
			// 根据文件后缀名不同(xls和xlsx)获得不同的Workbook实现类对象
			if (filePath.endsWith(xls)) {
				// 2003
				workbook = new HSSFWorkbook(is);
			} else if (filePath.endsWith(xlsx)) {
				// 2007
				try {
					workbook = WorkbookFactory.create(is);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			e.getMessage();
		}
		return workbook;
	}

	public static String getCellValue(Cell cell) {

		String value = "";
		if (cell == null) {
			return value;
		}
		// 把数字当成String来读，避免出现1读成1.0的情况
		if (cell.getCellType() == Cell.CELL_TYPE_NUMERIC || cell.getCellType() == Cell.CELL_TYPE_FORMULA) {
			cell.setCellType(Cell.CELL_TYPE_STRING);
		}
		// 判断数据的类型
		switch (cell.getCellType()) {
		case Cell.CELL_TYPE_NUMERIC: // 数字
			value = String.valueOf(cell.getNumericCellValue());
			break;
		case Cell.CELL_TYPE_STRING: // 字符串
			value = String.valueOf(cell.getRichStringCellValue());
			break;
		case Cell.CELL_TYPE_BOOLEAN: // Boolean
			// Value = String.valueOf(cell.getBooleanCellValue());
			value = String.valueOf(cell.getBooleanCellValue());
			break;
		case Cell.CELL_TYPE_FORMULA: // 公式
			// Value = String.valueOf(cell.getCellFormula());
			// break;
			try {
				value = String.valueOf(cell.getNumericCellValue());
			} catch (IllegalStateException e) {
				value = String.valueOf(cell.getRichStringCellValue());
			}
			break;
		case Cell.CELL_TYPE_BLANK: // 空值
			value = "";
			break;
		case Cell.CELL_TYPE_ERROR: // 故障
			value = "非法字符";
			break;
		default:
			value = "未知类型";
			break;
		}

		return value;
	}
}
