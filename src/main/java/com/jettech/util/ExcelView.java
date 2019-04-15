package com.jettech.util;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.view.document.AbstractXlsView;

import com.jettech.entity.DataField;
import com.jettech.service.DataFieldService;
import com.jettech.vo.TestFieldVO;


@Component
public class ExcelView extends AbstractXlsView{
	
	public static ExcelView excelView;
	 
    @PostConstruct
    public void init() {
    	excelView = this;
    }
	
	private DataFieldService testFieldService;
	@Override
    protected void buildExcelDocument(Map<String, Object> map,
                                      Workbook workbook,
                                      HttpServletRequest request,
                                      HttpServletResponse response) throws Exception {
        String excelName = map.get("name").toString() + ".xls";
        response.setHeader("content-disposition", "attachment;filename=" + URLEncoder.encode(excelName,"utf-8"));
        response.setContentType("application/ms-excel; charset=UTF-8");
        response.setCharacterEncoding("UTF-8");
        @SuppressWarnings("unchecked")
        List<DataField> testTables=(List<DataField>)map.get("members");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        for (DataField testTable : testTables) {
		//根据表名查询所有的字段
		List<DataField> TestFields=excelView.testFieldService.findByTableName(testTable.getName());
		
		Sheet sheet = workbook.createSheet(testTable.getName());
        sheet.setDefaultColumnWidth(30);
        CellStyle style = workbook.createCellStyle();
        org.apache.poi.ss.usermodel.Font font = workbook.createFont();
        font.setFontName("Arial");
        style.setFillForegroundColor(HSSFColor.BLUE.index);
//        font.setBold(true);
//        font.setBoldweight(Font.BOLDWEIGHT_NORMAL);
        font.setColor(HSSFColor.BLACK.index);
        style.setFont(font);
        Row header = sheet.createRow(0);
        header.createCell(0).setCellValue("编号");
        header.getCell(0).setCellStyle(style);
        header.createCell(1).setCellValue("创建时间");
        header.getCell(1).setCellStyle(style);
        header.createCell(2).setCellValue("创建人");
        header.getCell(2).setCellStyle(style);
        header.createCell(3).setCellValue("编辑时间");
        header.getCell(3).setCellStyle(style);
        header.createCell(4).setCellValue("编辑人");
        header.getCell(4).setCellStyle(style);
        header.createCell(5).setCellValue("数据长度");
        header.getCell(5).setCellStyle(style);
        header.createCell(6).setCellValue("数据精度");
        header.getCell(6).setCellStyle(style);
        header.createCell(7).setCellValue("数据类型");
        header.getCell(7).setCellStyle(style);
        header.createCell(8).setCellValue("删除");
        header.getCell(8).setCellStyle(style);
        header.createCell(9).setCellValue("描述");
        header.getCell(9).setCellStyle(style);
        header.createCell(10).setCellValue("是否为外键");
        header.getCell(10).setCellStyle(style);
        header.createCell(11).setCellValue("是否为索引");
        header.getCell(11).setCellStyle(style);
        header.createCell(12).setCellValue("是否为空");
        header.getCell(12).setCellStyle(style);
        header.createCell(13).setCellValue("是否为主键");
        header.getCell(13).setCellStyle(style);
        header.createCell(14).setCellValue("名称");
        header.getCell(14).setCellStyle(style);
        header.createCell(15).setCellValue("表名");
        header.getCell(15).setCellStyle(style);
        header.createCell(16).setCellValue("版本号");
        header.getCell(16).setCellStyle(style);
        header.createCell(17).setCellValue("测试数据表id");
        header.getCell(17).setCellStyle(style);
        int rowCount = 1;
        List<TestFieldVO> TestFieldVOs = new ArrayList<TestFieldVO>();
        for(DataField testField:TestFields){
			TestFieldVO testFieldVO = new TestFieldVO(testField);
			TestFieldVOs.add(testFieldVO);
		}
        for (TestFieldVO testfield : TestFieldVOs) {
            Row testfieldRow = sheet.createRow(rowCount++);
            testfieldRow.createCell(0).setCellValue(testfield.getId());
            testfieldRow.createCell(1).setCellValue(sdf.format(testfield.getCreateTime()));
            testfieldRow.createCell(2).setCellValue(testfield.getCreateUser());
            testfieldRow.createCell(3).setCellValue(sdf.format(testfield.getEditTime()));
            testfieldRow.createCell(4).setCellValue(testfield.getEditUser());
            if(testfield.getDataLength()!=null){
            	testfieldRow.createCell(5).setCellValue(testfield.getDataLength());
            }
            if(testfield.getDataPrecision()!=null){
                testfieldRow.createCell(6).setCellValue(testfield.getDataPrecision());
            }
            if(testfield.getDataType()!=null){
            	testfieldRow.createCell(7).setCellValue(testfield.getDataType());
            }
            if(testfield.getDeleted()!=null){
            	testfieldRow.createCell(8).setCellValue(testfield.getDeleted());
            }
            if(testfield.getDes()!=null){
                testfieldRow.createCell(9).setCellValue(testfield.getDes());
            }
            if(testfield.getIsForeignKey()!=null){
                testfieldRow.createCell(10).setCellValue(testfield.getIsForeignKey());
            }
            if(testfield.getIsIndex()!=null){
                testfieldRow.createCell(11).setCellValue(testfield.getIsIndex());
            }
            if(testfield.getIsNullable()!=null){
                testfieldRow.createCell(12).setCellValue(testfield.getIsNullable());
            }
            if(testfield.getIsPrimaryKey()!=null){
                testfieldRow.createCell(13).setCellValue(testfield.getIsPrimaryKey());
            }
            testfieldRow.createCell(14).setCellValue(testfield.getName());
            testfieldRow.createCell(15).setCellValue(testfield.getTalbeName());
            testfieldRow.createCell(16).setCellValue(testfield.getVersion());
            testfieldRow.createCell(17).setCellValue(testfield.getTestTableId());

            
	    }
        
        
        }
    }

}
