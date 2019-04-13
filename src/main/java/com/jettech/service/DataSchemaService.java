package com.jettech.service;

import java.util.List;

import javax.servlet.http.HttpServletResponse;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.jettech.entity.DataSchema;
import com.jettech.entity.DataTable;
import com.jettech.vo.ResultVO;
import com.jettech.vo.TestDatabaseVO;


public interface DataSchemaService {
	public ResultVO add(TestDatabaseVO testDatabaseVO);
	public ResultVO update(TestDatabaseVO testDatabaseVO);
	public ResultVO delete(Integer id);
	public void buildExcelFile(String filename,HSSFWorkbook workbook) throws Exception;
	public void buildExcelDocument(String filename,HSSFWorkbook workbook,HttpServletResponse response) throws Exception;
	public void insertRow(List<DataTable> testTables,HSSFWorkbook workbook);
	public void createTitle(HSSFWorkbook workbook,HSSFSheet sheet);
	public String getDatabase(HttpServletResponse response,int id)throws Exception;
	public String downloadSelect(HttpServletResponse response,List<Integer> ids) throws Exception;
    public ResultVO copyDataSchema(int id,String name);
    public void SetOneDataSchema(int id);
    public List<DataSchema> getAllDataSchema();
}
