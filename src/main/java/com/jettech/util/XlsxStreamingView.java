package com.jettech.util;

import java.text.DateFormat;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.web.servlet.view.document.AbstractXlsxStreamingView;

import com.jettech.entity.TestCase;

public class XlsxStreamingView extends AbstractXlsxStreamingView {

	private static final DateFormat DATE_FORMAT = DateFormat.getDateInstance(DateFormat.SHORT);

	@Override
	protected void buildExcelDocument(Map<String, Object> map, Workbook workbook, HttpServletRequest request,
			HttpServletResponse response) throws Exception {
		// change the file name
		response.reset();
		response.setHeader("Content-Disposition", "attachment; filename=\"my-xlsxStreaming-file.xlsx\"");

		@SuppressWarnings("unchecked")
		List<TestCase> list = (List<TestCase>) map.get("list");
		// create excel xls sheet
		Sheet sheet = workbook.createSheet("Spring MVC AbstractXlsxStreamingView");
		// create header
		Row header = sheet.createRow(0);
		header.createCell(0).setCellValue("id");
		header.createCell(1).setCellValue("name");
		header.createCell(2).setCellValue("caseType");
		header.createCell(3).setCellValue("maxResultRows");
		header.createCell(4).setCellValue("sourceQuery");
		header.createCell(5).setCellValue("targetQuery");
		header.createCell(6).setCellValue("version");
		header.createCell(7).setCellValue("createUser");
		header.createCell(8).setCellValue("createTime");
		header.createCell(9).setCellValue("editUser");
		header.createCell(10).setCellValue("editTime");

		// Create data cells
		int rowIndex = 1;
		for (TestCase testCase : list) {
			Row rowItem = sheet.createRow(rowIndex++);
			if (StringUtils.isNotBlank(testCase.getId().toString())) {
				// 设置单元格的值
				rowItem.createCell(0).setCellValue(testCase.getId().toString());
			} else {
				rowItem.createCell(0).setCellValue("null");
			}
			rowItem.createCell(1).setCellValue(testCase.getName());
			if (testCase.getCaseType() != null) {
				rowItem.createCell(2).setCellValue(testCase.getCaseType().toString());
			}else {
				rowItem.createCell(2).setCellValue("null");
			}
			if (testCase.getMaxResultRows()!=null) {
				rowItem.createCell(3).setCellValue(testCase.getMaxResultRows());
			}else {
				rowItem.createCell(3).setCellValue("null");
			}
			if (testCase.getSourceQuery()!=null) {
				rowItem.createCell(4).setCellValue(testCase.getSourceQuery().toString());
			}else {
				rowItem.createCell(4).setCellValue("null");
			}
			if (testCase.getTargetQuery()!=null) {
				rowItem.createCell(5).setCellValue(testCase.getTargetQuery().toString());
			}else {
				rowItem.createCell(5).setCellValue("null");
			}
			if (testCase.getVersion()!=null) {
				rowItem.createCell(6).setCellValue(testCase.getVersion());
			}else {
				rowItem.createCell(6).setCellValue("null");
			}
			if (testCase.getCreateUser()!=null) {
				rowItem.createCell(7).setCellValue(testCase.getCreateUser());
			}else {
				rowItem.createCell(7).setCellValue("null");
			}
			if (testCase.getEditUser()!=null) {
				rowItem.createCell(8).setCellValue(testCase.getEditUser());
			}else {
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
	}
}
