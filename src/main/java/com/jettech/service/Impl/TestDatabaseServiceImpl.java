package com.jettech.service.Impl;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFDataFormat;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.Logger;
import com.jettech.entity.CaseModelSetDetails;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataField;
import com.jettech.entity.MetaHistory;
import com.jettech.entity.TestQueryField;
import com.jettech.repostory.CaseModelSetDetailsRepository;
import com.jettech.repostory.CaseModelSetRepository;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataSchemaRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.DataTableRepository;
import com.jettech.repostory.MetaHistoryItemRepository;
import com.jettech.repostory.MetaHistoryRepository;
import com.jettech.repostory.TestQueryFieldRepository;
import com.jettech.service.DataSchemaService;
import com.jettech.service.IDataSourceService;
import com.jettech.service.IMetaDataManageService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.SycData;
import com.jettech.vo.TestDatabaseVO;
import com.jettech.vo.TestFieldVO;
@Service
public class TestDatabaseServiceImpl implements DataSchemaService {
	@Autowired
	private DataSchemaRepository testDatabaseRepository;
	@Autowired
	private DataTableRepository testTableRepository;
	@Autowired
	private DataFieldRepository testFieldRepository;
	@Autowired
	TestQueryFieldRepository testQueryFieldrepository;
	@Autowired
	private IMetaDataManageService metaDataManageService;
	@Autowired
	private IDataSourceService dataSourceService;
	@Autowired
	private MetaHistoryRepository metaHistoryRepository;
	@Autowired
	private MetaHistoryItemRepository metaHistoryItemRepository;
	@Autowired
	private DataSourceRepository dataSourcereRository;
	@Autowired
	CaseModelSetDetailsRepository caseModelSetDetailsRepository;
	@Autowired
	CaseModelSetRepository caseModelSetRepository;
	
	private static org.slf4j.Logger logger = LoggerFactory
			.getLogger(TestDatabaseServiceImpl.class);
	@Override
	public ResultVO add(TestDatabaseVO testDatabaseVO) {
		DataSchema testDatabase = new DataSchema();
		BeanUtils.copyProperties(testDatabaseVO, testDatabase);
		if (testDatabaseVO.getDataSourceId() != null) {
			DataSource dataSource = dataSourcereRository.findById(
					testDatabaseVO.getDataSourceId()).get();
			testDatabase.setDataSource(dataSource);
		}else{
			DataSchema exdataSchema =testDatabaseRepository.findByNameAndDataSourceIdIsNull(testDatabaseVO.getName());
		    if(exdataSchema!=null){
		    	return new ResultVO(false, StatusCode.ERROR, "新增失败,本名称的数据字典已经存在");
		    }
		}
		testDatabaseRepository.save(testDatabase);
		return new ResultVO(true, StatusCode.OK, "添加成功");
	}

	@Override
	public ResultVO update(TestDatabaseVO testDatabaseVO) {
		DataSchema dataSchema = testDatabaseRepository.findById(
				testDatabaseVO.getId()).get();
		String name=testDatabaseVO.getName();
		if(dataSchema.getVersion()==null){
			return new ResultVO(false, StatusCode.ERROR, "修改失败,数据异常版本号不允许为null");
		}else{
			dataSchema.setVersion(dataSchema.getVersion() + 1);
		}
		if (testDatabaseVO.getDataSourceId() == null) {
			DataSchema exdataSchema =testDatabaseRepository.findByNameAndDataSourceIdIsNull(testDatabaseVO.getName());
			if(exdataSchema!=null){
				return new ResultVO(false, StatusCode.ERROR, "修改失败,本名称的数据字典已经存在");
			}
			dataSchema.setDataSource(null);
			dataSchema.setId(testDatabaseVO.getId());
			dataSchema.setIsDict(testDatabaseVO.getIsDict());
			dataSchema.setName(testDatabaseVO.getName());
			testDatabaseRepository.save(dataSchema);
		}else{
			int data_source_id=testDatabaseVO.getDataSourceId() ;
			DataSchema exdataSchema =testDatabaseRepository.findByNameAndDataSourceId(name, data_source_id);
			if(exdataSchema!=null){
				return new ResultVO(false, StatusCode.ERROR, "修改失败,同名称同数据源的数据已经存在");
			}
			testDatabaseRepository.update(dataSchema.getVersion(),
					testDatabaseVO.getId(), testDatabaseVO.getIsDict(),
					testDatabaseVO.getName(), testDatabaseVO.getDataSourceId());
		}
		
		return new ResultVO(true, StatusCode.OK, "修改成功");
	}

	@Override
	@Transactional
	public ResultVO delete(Integer id) {
		// 根据testdatabaseId查询testtable
		List<DataTable> testTables = testTableRepository.findByForeignKey(id);
		if (testTables.size() != 0) {
			// 根据testtableid查询testfield
			for (DataTable testTable : testTables) {
				List<DataField> testFields = testFieldRepository
						.findByForeignKey(testTable.getId());
				for (DataField testField : testFields) {
					// 根据testfieldid查询testqueryfield
					List<TestQueryField> testQueryFields = testQueryFieldrepository
							.findByForeignKey(testField.getId());
					if (testQueryFields.size() != 0) {
						return new ResultVO(false, StatusCode.ERROR, "案例关联禁止删除");
					}
					for (TestQueryField testQueryField : testQueryFields) {
						testQueryFieldrepository.delete(testQueryField);
					}
					testFieldRepository.delById(testField.getId());
				}
				caseModelSetDetailsRepository.deleteBydatumTabOrTestTab(testTable.getId(), testTable.getId());
				testTableRepository.delById(testTable.getId());
			}

		}
		DataSchema dataSchema = testDatabaseRepository.findById(id).get();
		dataSchema.setDataSource(null);
		dataSchema.setDataTables(null);
		List<MetaHistory> metaHistorys = metaHistoryRepository
				.findByForeignKeyID(id);
		if (metaHistorys.size() != 0) {
			for (MetaHistory metaHistory : metaHistorys) {
				metaHistoryItemRepository.deletedByForeignKeyID(metaHistory
						.getId());
			}
			metaHistoryRepository.deletedByForeignKeyID(id);
		}
		caseModelSetRepository.deleteByTestOrDatumId(dataSchema.getId(), dataSchema.getId());
		testDatabaseRepository.delete(dataSchema);
		return new ResultVO(true, StatusCode.OK, "删除成功");
	}

	@Override
	public void buildExcelFile(String filename, HSSFWorkbook workbook)
			throws Exception {
		FileOutputStream fos = new FileOutputStream(filename);
		workbook.write(fos);
		fos.flush();
		fos.close();
	}

	@Override
	public void buildExcelDocument(String filename, HSSFWorkbook workbook,
			HttpServletResponse response) throws Exception {
		response.setContentType("application/vnd.ms-excel");
		response.setHeader("Content-Disposition", "attachment;filename="
				+ URLEncoder.encode(filename, "utf-8"));
		OutputStream outputStream = response.getOutputStream();
		workbook.write(outputStream);
		outputStream.flush();
		outputStream.close();

	}

	@Override
	public void insertRow(List<DataTable> dataModels, HSSFWorkbook workbook) {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		if (dataModels.isEmpty()) {
			HSSFSheet sheet = workbook.createSheet("sheet");
			createTitle(workbook, sheet);
		}

		for (DataTable testTable : dataModels) {
			HSSFSheet sheet = workbook.createSheet(testTable.getName());
			createTitle(workbook, sheet);
			// 根据表名查询所有的字段
			List<DataField> TestFields = testFieldRepository
					.findByForeignKey(testTable.getId());
			List<TestFieldVO> rows = new ArrayList<TestFieldVO>();
			for (DataField testField : TestFields) {
				TestFieldVO testFieldVO = new TestFieldVO(testField);
				rows.add(testFieldVO);
			}

			// 设置日期格式
			HSSFCellStyle style = workbook.createCellStyle();
			style.setDataFormat(HSSFDataFormat.getBuiltinFormat("m/d/yy h:mm"));

			// 新增数据行，并且设置单元格数据
			int rowNum = 1;
			for (TestFieldVO testfield : rows) {
				Row testfieldRow = sheet.createRow(rowNum++);
				testfieldRow.createCell(0).setCellValue(testfield.getId());
				testfieldRow.createCell(1).setCellValue(
						sdf.format(testfield.getCreateTime()));
				testfieldRow.createCell(2).setCellValue(
						testfield.getCreateUser());
				testfieldRow.createCell(3).setCellValue(
						sdf.format(testfield.getEditTime()));
				testfieldRow.createCell(4)
						.setCellValue(testfield.getEditUser());
				if (testfield.getDataLength() != null) {
					testfieldRow.createCell(5).setCellValue(
							testfield.getDataLength());
				}
				if (testfield.getDataPrecision() != null) {
					testfieldRow.createCell(6).setCellValue(
							testfield.getDataPrecision());
				}
				if (testfield.getDataType() != null) {
					testfieldRow.createCell(7).setCellValue(
							testfield.getDataType());
				}
				if (testfield.getDeleted() != null) {
					testfieldRow.createCell(8).setCellValue(
							testfield.getDeleted());
				}
				if (testfield.getDes() != null) {
					testfieldRow.createCell(9).setCellValue(testfield.getDes());
				}
				if (testfield.getIsForeignKey() != null) {
					testfieldRow.createCell(10).setCellValue(
							testfield.getIsForeignKey());
				}
				if (testfield.getIsIndex() != null) {
					testfieldRow.createCell(11).setCellValue(
							testfield.getIsIndex());
				}
				if (testfield.getIsNullable() != null) {
					testfieldRow.createCell(12).setCellValue(
							testfield.getIsNullable());
				}
				if (testfield.getIsPrimaryKey() != null) {
					testfieldRow.createCell(13).setCellValue(
							testfield.getIsPrimaryKey());
				}
				testfieldRow.createCell(14).setCellValue(testfield.getName());
				testfieldRow.createCell(15).setCellValue(
						testfield.getTalbeName());
				if (testfield.getVersion() != null) {
					testfieldRow.createCell(16).setCellValue(
							testfield.getVersion());
				}
				testfieldRow.createCell(17).setCellValue(
						testfield.getTestTableId());

			}
		}

	}

	@Override
	public void createTitle(HSSFWorkbook workbook, HSSFSheet sheet) {
		// 设置列宽，setColumnWidth的第二个参数要乘以256，这个参数的单位是1/256个字符宽度
		sheet.setColumnWidth(1, 12 * 256);
		sheet.setColumnWidth(3, 17 * 256);

		// 设置为居中加粗
		HSSFCellStyle style = workbook.createCellStyle();
		HSSFFont font = workbook.createFont();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
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

	}

	@Override
	public String getDatabase(HttpServletResponse response, int id)
			throws Exception {
		String name = testDatabaseRepository.findName(id);
		StringBuilder sb = new StringBuilder();
		String fileName = sb.append(name).append(".xls").toString();
		HSSFWorkbook workbook = new HSSFWorkbook();
		// 根据test_database_id查出所有的表
		List<DataTable> testTables = metaDataManageService.getAllTable(id);
		insertRow(testTables, workbook);
		// 生成excel文件
		buildExcelFile(fileName, workbook);
		// 浏览器下载excel
		buildExcelDocument(fileName, workbook, response);
		return "download excel";

	}

	@Override
	public String downloadSelect(HttpServletResponse response, List<Integer> ids)
			throws Exception {
		List<DataTable> testTables = new ArrayList<DataTable>();
		for (int id : ids) {
			DataTable testTable = testTableRepository.findById(id).get();
			testTables.add(testTable);
		}
		String name = testTables.get(0).getDataSchema().getName();
		HSSFWorkbook workbook = new HSSFWorkbook();
		insertRow(testTables, workbook);
		StringBuilder sb = new StringBuilder();
		String fileName = sb.append(name).append(".xls").toString();
		// 生成excel文件
		buildExcelFile(fileName, workbook);
		// 浏览器下载excel
		buildExcelDocument(fileName, workbook, response);
		return "download excel";
	}

	@Override
	public ResultVO copyDataSchema(int id, String name) {
		DataSchema dataSchema = testDatabaseRepository.findById(id).get();
		DataSchema exitsdataSchema = testDatabaseRepository.findByName(name);
		if (exitsdataSchema != null) {
			return new ResultVO(false, StatusCode.ERROR, "名称重复");
		}
		DataSchema copyDataSchema = new DataSchema();
		BeanUtils.copyProperties(dataSchema, copyDataSchema);
		copyDataSchema.setCreateTime(new Date());
		copyDataSchema.setCreateUser(null);
		copyDataSchema.setEditTime(new Date());
		copyDataSchema.setEditUser(null);
		copyDataSchema.setId(null);
		copyDataSchema.setName(name);
		testDatabaseRepository.save(copyDataSchema);
		List<DataTable> dataTables = dataSchema.getDataTables();
		if (!dataTables.isEmpty()) {
			DataSchema savedataSchema = testDatabaseRepository.findByName(name);
			List<DataTable> copydataTables = new ArrayList<DataTable>();
			for (DataTable dataTable : dataTables) {
				DataTable copyDataTable = new DataTable();
				BeanUtils.copyProperties(dataTable, copyDataTable);
				copyDataTable.setId(null);
				copyDataTable.setCreateTime(new Date());
				copyDataTable.setCreateUser(null);
				copyDataTable.setEditTime(new Date());
				copyDataTable.setEditUser(null);
				copyDataTable.setDataSchema(savedataSchema);
				copydataTables.add(copyDataTable);
			}
			testTableRepository.saveAll(copydataTables);
		}

		return new ResultVO(true, StatusCode.OK, "复制成功");

	}

	@Override
	@Transactional
	public void SetOneDataSchema(int id) {
		// 找到这个库
		DataSchema tongbuDataSchema = testDatabaseRepository.findById(id).get();
		DataSource dataSource = tongbuDataSchema.getDataSource();
		if (dataSource != null) {
			SycData sycData = dataSourceService.getAdapterAndConnection(
					dataSource.getDatabaseType(), dataSource.getDriver(),
					dataSource.getUrl(), dataSource.getPort(),
					dataSource.getHost(), dataSource.getUserName(),
					dataSource.getPassword(), dataSource.getSid());
			// 同步表和字段
			dataSourceService.syncTableAndFiled(sycData.getAdapter(),
					sycData.getConn(), tongbuDataSchema);

		} else {
			System.out.println("没有数据源不能进行同步");
		}
	}
	@Override
	public List<DataSchema> getAllDataSchema(){
		List<DataSchema> list=testDatabaseRepository.findAll();
		return list;
	}

	@Override
	public DataSchema findSchemaByID(int id) {
		return testDatabaseRepository.getOne(id);
	}

	@Override
	public List<DataSchema> getSchemasByDataSourceID(int dataSourceID, String schemaName) {
		List<DataSchema> list = testDatabaseRepository.findSchemasByDataSourceID(dataSourceID,schemaName);
		return list;
	}
}
