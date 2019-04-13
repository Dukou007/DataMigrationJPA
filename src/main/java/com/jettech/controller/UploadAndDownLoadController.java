package com.jettech.controller;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.jettech.entity.TestCase;
import com.jettech.entity.TestResultItem;
import com.jettech.service.ITestResultItemService;
import com.jettech.service.ITestCaseService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;

@Api(value = "Upload--And--DownLoad--Controller|测试结果的controller")
@RestController
@RequestMapping(value = "/uploadAndDownLoad")
public class UploadAndDownLoadController {

	private static Logger log = LoggerFactory.getLogger(UploadAndDownLoadController.class);

	@Autowired
	private ITestResultItemService testRestltItemService;

	@Autowired
	private ITestCaseService testCaseService;

	/**
	 * @Description: 上传文件，格式为文本文件
	 * @param request
	 * @param file
	 * @return
	 * @author: zhou_xiaolong in 2019年3月5日下午10:25:42
	 *
	 */
	@SuppressWarnings("unused")
	@ResponseBody
	@RequestMapping(value = "/uploadFile", method = RequestMethod.POST)
	@ApiOperation(value = "上传文件，格式为文本文件。", notes = "书写的格式要求高")
	@ApiImplicitParam(value = "要上传的文件", dataType = "String", name = "file", paramType = "query", required = true)
	public ResultVO uploadFile(HttpServletRequest request, @RequestParam(value = "file") MultipartFile file) {

		// 获得产品的名称与测试机名称
		String productName = request.getParameter("productName");
		System.out.println("产品：" + productName);
		String testSuiteName = request.getParameter("testSuiteName");
		System.out.println("测试集：" + testSuiteName);

		// 文件名称
		String filename = file.getOriginalFilename();
		// 文件后缀名
		String suffixName = filename.substring(filename.lastIndexOf("."));
		// 新的文件名（UUID）
		String newFileName = UUID.randomUUID().toString() + filename;
		// 新建文件对象
		File newFile = new File(newFileName);
		HashMap<String, Object> map = new HashMap<String, Object>();
		// 封装文件信息
		map.put("filePath", newFile.getAbsolutePath());
		map.put("productName", productName);
		map.put("testSuiteName", testSuiteName);
		// 获取文件在服务器中的位置
		ServletContext context = request.getServletContext();
		String path = context.getRealPath("upload");
		System.out.println(path);
		try {
			file.transferTo(newFile);
		} catch (IllegalStateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return new ResultVO(false, StatusCode.OK, "上传成功");

	}

	/**
	 * @Description: 导入案例的case,返回结果为200成功
	 * @tips: null;
	 * @author: zhou_xiaolong in 2019年3月6日上午12:09:22
	 * @param file
	 * @param request
	 * @return
	 * @throws Exception
	 *//*
		 * @SuppressWarnings("unused")
		 * 
		 * @ResponseBody
		 * 
		 * @RequestMapping(value="/importCaseExcel",method=RequestMethod.POST)
		 * 
		 * @ApiOperation(value="导入案例的case,返回结果为200成功=") public
		 * ResponseEntity<TestCaseVO> importCaseExcel(MultipartFile
		 * file,HttpServletRequest request) throws Exception { // fileName && filePath
		 * String excelFileName="D://logs//test.xlsx";
		 * List<TestCaseVO>testCaseList=ExcelReaderTool.readExcel(excelFileName); return
		 * new ResponseEntity<>(HttpStatus.OK); }
		 */

	/**
	 * 导出当前页 resultItem数据到Excel,依据得是resultitem的ID查询的所有的resultItem
	 * 
	 * @param request
	 * @param name
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "unused", "resource" })
	@ApiOperation(value = "导出当前页的 resultItem数据到Excel,依据的是resultItem的ID查询的所有的resultItem数据到Excel，可以根据生成的时间来判断")
	@ApiImplicitParam(paramType = "query", name = "id", value = "id的值", dataType = "String", required = false)
	@ResponseBody
	@RequestMapping(value = "/downloadCurrentPageConverToExcel", method = RequestMethod.GET)
	public ResultVO downloadCurrentPageConverToExcel(
			@RequestParam(value = "id", required = false, defaultValue = "") String ids, HttpServletResponse res)
			throws Exception {
		List<TestResultItem> list = testRestltItemService.findByTestResultItemIDs(ids);
		HashMap<String, Object> map = new HashMap<String, Object>();
		String fileName = "testResultItem" + ".xls";
		for (TestResultItem testResultItem : list) {
			map.put("id", testResultItem.getId());
			map.put("columnName", testResultItem.getColumnName());
			map.put("keyValue", testResultItem.getKeyValue());
			map.put("result", testResultItem.getResult());
			map.put("soruceValue", testResultItem.getSoruceValue());
			map.put("tragetValue", testResultItem.getTragetValue());
			map.put("createUser", testResultItem.getCreateUser());
			map.put("editUser", testResultItem.getEditUser());
			map.put("createTime", testResultItem.getCreateTime());
			map.put("editTime", testResultItem.getEditTime());

		}

		// 创建表格文件
		HSSFWorkbook workbook = new HSSFWorkbook();
		String sheetName = "TestResultItemDetial";
		HSSFSheet sheet = workbook.createSheet(sheetName);
		HSSFRow header = sheet.createRow(0);
		// 设置表头名称
		header.createCell(0).setCellValue("id");
		header.createCell(1).setCellValue("column_name");
		header.createCell(2).setCellValue("keyValue");
		header.createCell(3).setCellValue("result");
		header.createCell(4).setCellValue("soruceValue");
		header.createCell(5).setCellValue("tragetValue");
		header.createCell(6).setCellValue("createUser");
		header.createCell(7).setCellValue("editUser");
		header.createCell(8).setCellValue("createTime");
		header.createCell(9).setCellValue("editTime");
		// 设置表内容
		int rowIndex = 1;
		for (TestResultItem testResultItem : list) {
			HSSFRow rowItem = sheet.createRow(rowIndex++);
			// 设置单元格的值
			rowItem.createCell(0).setCellValue(testResultItem.getId().toString());
			rowItem.createCell(1).setCellValue(testResultItem.getColumnName());
			rowItem.createCell(2).setCellValue(testResultItem.getKeyValue());
			rowItem.createCell(3).setCellValue(testResultItem.getResult());
			rowItem.createCell(4).setCellValue(testResultItem.getSoruceValue());
			rowItem.createCell(5).setCellValue(testResultItem.getTragetValue());
			rowItem.createCell(6).setCellValue(testResultItem.getCreateUser());
			rowItem.createCell(7).setCellValue(testResultItem.getEditUser());
			rowItem.createCell(8).setCellValue(testResultItem.getCreateTime().toString());
			rowItem.createCell(9).setCellValue(testResultItem.getEditTime().toString());

		}
		String filename = sheetName;
		res.reset(); // 非常重要
		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

		try {
			OutputStream out = res.getOutputStream();
			res.addHeader("Content-Disposition",
					"attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
			workbook.write(out);
			out.flush();
			out.close();
			return new ResultVO(true, StatusCode.OK, "下载成功");
		} catch (Exception e) {
			e.printStackTrace();
			log.error("导出当前页 resultItem数据到Excel失败：", e);
			return new ResultVO(false, StatusCode.ERROR, "下载失败");
		}

	}

	/**
	 * 导出全部 resultItem数据到Excel,依据得是result的ID查询的所有的resultItem
	 * 
	 * @param request
	 * @param name
	 * @return
	 * @throws IOException
	 */
	@SuppressWarnings({ "unused", "resource" })
	@ApiOperation(value = "导出全部 resultItem数据到Excel,依据的是result的ID查询的所有的resultItem数据到Excel可以根据生成的时间来判断")
	@ApiImplicitParam(paramType = "query", name = "id", value = "id的值", dataType = "String", required = false)
	@ResponseBody
	@RequestMapping(value = "/downloadAllPageConverToExcel", method = RequestMethod.GET)
	public ResultVO downloadAllPageConverToExcel(
			@RequestParam(value = "id", required = false, defaultValue = "") String id, HttpServletResponse res)
			throws Exception {
		try {
			List<TestResultItem> list = testRestltItemService.findByTestResultID(id);
			HashMap<String, Object> map = new HashMap<String, Object>();
			String fileName = "testResultItem" + ".xls";
			for (TestResultItem testResultItem : list) {
				map.put("id", testResultItem.getId());
				map.put("columnName", testResultItem.getColumnName());
				map.put("keyValue", testResultItem.getKeyValue());
				map.put("result", testResultItem.getResult());
				map.put("soruceValue", testResultItem.getSoruceValue());
				map.put("tragetValue", testResultItem.getTragetValue());
				map.put("createUser", testResultItem.getCreateUser());
				map.put("editUser", testResultItem.getEditUser());
				map.put("createTime", testResultItem.getCreateTime());
				map.put("editTime", testResultItem.getEditTime());

			}

			// 创建表格文件
			HSSFWorkbook workbook = new HSSFWorkbook();
			String sheetName = "TestResultItemDetial";
			HSSFSheet sheet = workbook.createSheet(sheetName);
			HSSFRow header = sheet.createRow(0);
			// 设置表头名称
			header.createCell(0).setCellValue("id");
			header.createCell(1).setCellValue("column_name");
			header.createCell(2).setCellValue("keyValue");
			header.createCell(3).setCellValue("result");
			header.createCell(4).setCellValue("soruceValue");
			header.createCell(5).setCellValue("tragetValue");
			header.createCell(6).setCellValue("createUser");
			header.createCell(7).setCellValue("editUser");
			header.createCell(8).setCellValue("createTime");
			header.createCell(9).setCellValue("editTime");
			// 设置表内容
			int rowIndex = 1;
			for (TestResultItem testResultItem : list) {
				HSSFRow rowItem = sheet.createRow(rowIndex++);
				// 设置单元格的值
				rowItem.createCell(0).setCellValue(testResultItem.getId().toString());
				rowItem.createCell(1).setCellValue(testResultItem.getColumnName());
				rowItem.createCell(2).setCellValue(testResultItem.getKeyValue());
				rowItem.createCell(3).setCellValue(testResultItem.getResult());
				rowItem.createCell(4).setCellValue(testResultItem.getSoruceValue());
				rowItem.createCell(5).setCellValue(testResultItem.getTragetValue());
				rowItem.createCell(6).setCellValue(testResultItem.getCreateUser());
				rowItem.createCell(7).setCellValue(testResultItem.getEditUser());
				rowItem.createCell(8).setCellValue(testResultItem.getCreateTime().toString());
				rowItem.createCell(9).setCellValue(testResultItem.getEditTime().toString());

			}
			String filename = sheetName;
			res.reset(); // 非常重要
			res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

			try {
				OutputStream out = res.getOutputStream();
				res.addHeader("Content-Disposition",
						"attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
				workbook.write(out);
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
			System.out.println("……………………………………………………");
			return new ResultVO(true, StatusCode.OK, "下载成功", fileName);
		} catch (Exception e) {
			log.error("下载失败:", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "下载失败");
		}

	}

	/**
	 * @Description: 根据caseid导出当前的/全部testcase数据到Excel可以根据生成的时间来判断
	 * @tips:
	 * @author: zhou_xiaolong in 2019年3月5日下午10:27:26
	 * @param ids
	 * @return
	 * @throws Exception
	 */
	@SuppressWarnings({ "unused", "resource" })
	@ApiOperation(value = "根据caseid导出当前的/全部testcase数据到Excel可以根据生成的时间来判断")
	@ApiImplicitParam(paramType = "query", name = "ids", value = "id的值,String类型,可单可数组", dataType = "String", required = false)
	@ResponseBody
	@RequestMapping(value = "/downloadCheckedCaseConverToExcel", method = RequestMethod.GET)
	public ResultVO downloadCheckedCaseConverToExcel(
			@RequestParam(value = "ids", required = false, defaultValue = "") String ids, HttpServletResponse res)
			throws Exception {
		try {
			List<TestCase> list = testCaseService.findByCaseIDs(ids);
			HashMap<String, Object> map = new HashMap<String, Object>();
			String fileName = "testCase" + ".xls";
			for (TestCase testCase : list) {
				map.put("id", testCase.getId());
				map.put("name", testCase.getName());
				map.put("caseType", testCase.getCaseType());
				map.put("maxResultRows", testCase.getMaxResultRows());
				map.put("sourceQuery", testCase.getSourceQuery());
				map.put("targetQuery", testCase.getTargetQuery());
				map.put("version", testCase.getVersion());
				map.put("createUser", testCase.getCreateUser());
				map.put("createTime", testCase.getCreateTime());
				map.put("editUser", testCase.getEditUser());
				map.put("editTime", testCase.getEditTime());

			}

			// 创建表格文件
			HSSFWorkbook workbook = new HSSFWorkbook();
			String sheetName = "TestResultItemDetial";
			HSSFSheet sheet = workbook.createSheet(sheetName);
			HSSFRow header = sheet.createRow(0);
			// 设置表头名称
			header.createCell(0).setCellValue("id");
			header.createCell(1).setCellValue("名称");
			header.createCell(2).setCellValue("类型");
			header.createCell(3).setCellValue("最大结果数");
			header.createCell(4).setCellValue("源数据模型");
			header.createCell(5).setCellValue("目标数据模型");
			header.createCell(6).setCellValue("版本");
			header.createCell(7).setCellValue("创建人");
			header.createCell(8).setCellValue("修改人");
			header.createCell(9).setCellValue("创建时间");
			header.createCell(10).setCellValue("修改时间");
			// 设置表内容
			int rowIndex = 1;
			for (TestCase testCase : list) {
				HSSFRow rowItem = sheet.createRow(rowIndex++);
				if (StringUtils.isNotBlank(testCase.getId().toString())) {
					// 设置单元格的值
					rowItem.createCell(0).setCellValue(testCase.getId().toString());
				} else {
					rowItem.createCell(0).setCellValue("null");
				}
				rowItem.createCell(1).setCellValue(testCase.getName());
				if (testCase.getCaseType() != null) {
					rowItem.createCell(2).setCellValue(testCase.getCaseType().toString());
				}
				if (testCase.getMaxResultRows() != null) {
					rowItem.createCell(3).setCellValue(testCase.getMaxResultRows());
				} else {
					rowItem.createCell(3).setCellValue("null");

				}
				if (testCase.getSourceQuery() != null) {
					rowItem.createCell(4).setCellValue(testCase.getSourceQuery().toString());
				} else {
					rowItem.createCell(4).setCellValue("null");

				}
				if (testCase.getTargetQuery() != null) {
					rowItem.createCell(5).setCellValue(testCase.getTargetQuery().toString());
				} else {
					rowItem.createCell(5).setCellValue("null");

				}
				if (testCase.getVersion() != null) {
					rowItem.createCell(6).setCellValue(testCase.getVersion());
				} else {
					rowItem.createCell(6).setCellValue("null");

				}
				if (testCase.getCreateUser() != null) {
					rowItem.createCell(7).setCellValue(testCase.getCreateUser());
				} else {
					rowItem.createCell(7).setCellValue("null");

				}
				if (testCase.getEditUser() != null) {
					rowItem.createCell(8).setCellValue(testCase.getEditUser());
				} else {
					rowItem.createCell(8).setCellValue("null");

				}
				if (StringUtils.isNotBlank(testCase.getCreateTime().toString())) {
					rowItem.createCell(9).setCellValue(testCase.getCreateTime().toString());
				} else {
					rowItem.createCell(9).setCellValue("null");
				}
				if (StringUtils.isNotBlank(testCase.getEditTime().toString())) {
					rowItem.createCell(10).setCellValue(testCase.getEditTime().toString());
				} else {
					rowItem.createCell(10).setCellValue("null");
				}

			}
			String filename = sheetName;
			res.reset(); // 非常重要
			res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

			try {
				OutputStream out = res.getOutputStream();
				res.addHeader("Content-Disposition",
						"attachment;filename=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
				workbook.write(out);
				out.flush();
				out.close();
			} catch (Exception e) {
				e.printStackTrace();
			}

			System.out.println("……………………………………………………");
			return new ResultVO(true, StatusCode.OK, "下载成功");
		} catch (Exception e) {
			log.error("根据caseid导出当前下载失败原因：", e);
			e.printStackTrace();
			return new ResultVO(false, StatusCode.ERROR, "下载失败");
		}

	}

}
