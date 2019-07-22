package com.jettech.service.Impl;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.Query;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import com.jettech.EnumExecuteStatus;
import com.jettech.db.adapter.AbstractAdapter;
import com.jettech.domain.QualityTestCaseModel;
import com.jettech.thread.JobWorker;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.streaming.SXSSFWorkbook;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jettech.BizException;
import com.jettech.EnumDatabaseType;
import com.jettech.entity.DataField;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.entity.Product;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.QualityTestQuery;
import com.jettech.entity.TestSuite;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataSchemaRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.DataTableRepository;
import com.jettech.repostory.ProductRepository;
import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.QualityTestPointRepository;
import com.jettech.repostory.QualityTestQueryRepository;
import com.jettech.repostory.TestRuleRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.IDataSourceService;
import com.jettech.service.IQualityTestCaseService;
import com.jettech.service.TestSuiteService;
import com.jettech.util.ExcelUtil;
import com.jettech.util.FileUtil;
import com.jettech.vo.QualityTestCaseVO;
import com.jettech.vo.QualityTestQueryVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;
import com.jettech.vo.SycData;

@Service
public class QualityTestCaseServiceImpl implements IQualityTestCaseService {
	Logger logger = LoggerFactory.getLogger(QualityTestCaseServiceImpl.class);
	/*@Value("${file.filePath}")
	private String filePath;*/
	@Autowired
	QualityTestCaseRepository caseRepository;
	@Autowired
	TestSuiteRepository testSutieRepository;
	@Autowired
	ProductRepository productRepository;
	@Autowired
	DataSourceRepository dataSourceRepository;
	@Autowired
	QualityTestQueryRepository qualityTestQueryRepository;
	@Autowired
	QualityTestPointRepository qualityTestPointRepository;
	@Autowired
	TestRuleRepository testRuleRepository;
	@Autowired
	DataFieldRepository testFieldRepository;
	@Autowired
	DataTableRepository dataTableRepository;
	@Autowired
	DataSchemaRepository dataSchemaRepository;
	@Autowired
	TestSuiteRepository testSuiteRepository;
	@Autowired
	TestSuiteService testSuiteService;
	@Autowired
	IDataSourceService dataSourceService;
	@PersistenceContext
	private EntityManager entityManager;


	@Override
	public List<QualityTestCase> findAll() {
		return caseRepository.findAll();
	}

	@Override
	public List<QualityTestCase> saveAll(List<QualityTestCase> list) {
		return caseRepository.saveAll(list);
	}

	@Override
	public void save(QualityTestCase entity) {
		caseRepository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		caseRepository.deleteById(id);
	}

	@Override
	public QualityTestCase getOneById(Integer id) {
		return caseRepository.getOne(id);
	}

	@Override
	@Transactional
	public String doTest(Integer testCaseId) {
		QualityTestCase qualityTestCase = caseRepository.getOne(testCaseId);
		if (qualityTestCase == null)
			return "not found qualityTestCase id:" + testCaseId;
		logger.info("ready doTestCase:" + qualityTestCase.getId() + "_"
				+ qualityTestCase.getName());
		QualityTestCaseModel caseModel = null;
		try {
			caseModel = new QualityTestCaseModel(qualityTestCase);
		} catch (Throwable e) {
			logger.error("cast qualityTestCase to model error."
					+ qualityTestCase.getId() + "_" + qualityTestCase.getName());
			e.printStackTrace();
			return e.getMessage();
		}
		JobWorker job = new JobWorker(caseModel);
		Thread thread = new Thread(job);
		thread.start();
		return null;
	}

	@Override
	public ResultVO readSQLCase(Map<String, String> map) throws BizException {
		ResultVO result = new ResultVO();
		result.setFlag(true);

		if (map == null || map.size() == 0)
			throw new IllegalArgumentException(
					"readSQLCase argument is null or empty.");
		String filePath = map.get("filePath");
		List<String> list = FileUtil.readFileToList(filePath, "UTF-8");
		StringBuffer str = new StringBuffer();

		for (String string : list) {
			str.append(string);
		}
		List<QualityTestCaseVO> json = JSONArray.parseArray(str.toString(),
				QualityTestCaseVO.class);
		List<Integer> caseIds = new ArrayList<Integer>();
		String testsuiteId = map.get("testSuiteId");
		int suiteId = Integer.parseInt(testsuiteId);
		for (QualityTestCaseVO testCaseVO : json) {
			testCaseVO.setTestSuiteId(suiteId);
			this.saveQualityTestCaseVo(testCaseVO);
		}

		// changeTestCasePosition(caseIds, suiteId);
		return result;

	}

	private TestSuite getTestSuite(String testSuiteName, String productName) {
		Product product = null;
		List<Product> productList = productRepository.findByName(productName);
		if (productList != null && productList.size() > 0) {
			product = productList.get(0);
		} else {
			product = new Product();
			product.setName(productName);
			product = productRepository.save(product);
		}
		TestSuite testSuite = null;
		List<TestSuite> testSuiteList = testSutieRepository.findByName(
				testSuiteName, productName);
		if (testSuiteList != null && testSuiteList.size() > 0) {
			testSuite = testSuiteList.get(0);
		} else {
			testSuite = new TestSuite();
			testSuite.setProduct(product);
			testSuite.setName(testSuiteName);
			testSuite = testSutieRepository.save(testSuite);
		}
		return testSuite;
	}

	private String getTagValue(int i, String line, String tagName) {
		if (line.toUpperCase().startsWith(tagName.toUpperCase())) {
			String tagValue = null;
			String[] arr = null;
			if (line.contains(":")) {
				arr = line.split(":");
			} else if (line.contains("：")) {
				arr = line.split("：");
			} else {
				logger.error(tagName + " error style.line：" + i + " [" + line
						+ "]");
			}

			tagValue = arr[1].trim();
			if (tagValue.isEmpty()) {
				logger.error(tagName + " is empty.line：" + i + " [" + line
						+ "]");
			}
			return tagValue;
		} else
			return null;
	}

	@Override
	public QualityTestCase findByNameAndSuite(String testCaseName,
			String testSuiteName) {
		List<QualityTestCase> list = caseRepository.findByNameAndSuite(
				testCaseName, testSuiteName);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}

	@Override
	public QualityTestCase findByName(String name) {
		List<QualityTestCase> list = caseRepository.findByName(name);
		if (list != null && list.size() > 0)
			return list.get(0);
		return null;
	}

	@Override
	public Page<QualityTestCase> findTestCaseByName(String name,
			Pageable pageable) {
		Page<QualityTestCase> list = caseRepository.findTestCaseByName(name,
				pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<>(new ArrayList<QualityTestCase>(), pageable, 0);
		}
	}

	@Override
	@Transactional
	public void batchDelete(String testCaseIDs) {
		if (StringUtils.isNotBlank(testCaseIDs)) {
			String[] ids = testCaseIDs.split(",");
			for (String id : ids) {
				QualityTestCase qualityTestCase = caseRepository.getOne(Integer
						.valueOf(id));
				QualityTestQuery qualityTestQuery = qualityTestCase
						.getQualityTestQuery();
				qualityTestCase.setQualityTestQuery(null);
				if (qualityTestQuery != null) {
					qualityTestQuery.setDataField(null);
					qualityTestQuery.setDataSchema(null);
					qualityTestQuery.setDataSource(null);
					qualityTestQuery.setDataTable(null);
					qualityTestQuery.setQualitySuite(null);
					qualityTestQueryRepository.save(qualityTestQuery);
					qualityTestQueryRepository.deleteById(qualityTestQuery
							.getId());
				}
				List<TestSuite> qualityTestSuites = qualityTestCase
						.getTestSuites();
				for (TestSuite testSuite : qualityTestSuites) {
					List<QualityTestCase> qualityTestCases = testSuite
							.getQualityTestCases();
					qualityTestCases.remove(qualityTestCase);
					testSutieRepository.save(testSuite);
				}
				caseRepository.delete(qualityTestCase);
			}
		}

	}

	@Override
	public List<QualityTestCase> findByCaseIDs(String ids) {
		ArrayList<QualityTestCase> list = new ArrayList<QualityTestCase>();
		if (StringUtils.isNotBlank(ids)) {
			String[] testCaseIDs = ids.split(",");
			for (String id : testCaseIDs) {
				QualityTestCase qualityTestCase = caseRepository.getOne(Integer
						.valueOf(id));
				list.add(qualityTestCase);
			}
			return list;
		} else {
			List<QualityTestCase> all = caseRepository.findAll();
			return all;
		}
	}

	@Override
	public QualityTestCase findById(Integer id) {
		return caseRepository.getOne(id);
	}

	@Override
	public Page<QualityTestCase> findAllByPage(Pageable pageable) {
		return caseRepository.findAll(pageable);
	}

	@Override
	public Page<QualityTestCase> findAllTestCaseByPage(String name,
			Pageable pageable) {
		Page<QualityTestCase> list = null;
		if (name == null) {
			list = caseRepository.findAllByOrderByIdDesc(pageable);
		} else {

			list = caseRepository.findByNameLike("%" + name + "%", pageable);
		}
		if (list != null) {
			return list;
		} else {
			return new PageImpl<QualityTestCase>(
					new ArrayList<QualityTestCase>(), pageable, 0);
		}
	}

	@Override
	public QualityTestCase getOne(Integer id) {
		return caseRepository.getOne(id);
	}

	@Override
	public List<QualityTestCase> findByQualityCaseIDs(String ids) {
		ArrayList<QualityTestCase> list = new ArrayList<QualityTestCase>();
		if (StringUtils.isNotBlank(ids)) {
			String[] testCaseIDs = ids.split(",");
			for (String id : testCaseIDs) {
				QualityTestCase testCase = caseRepository.getOne(Integer
						.valueOf(id));
				list.add(testCase);
			}
			return list;
		} else {
			List<QualityTestCase> all = caseRepository.findAll();
			return all;
		}
	}

	@Override
	public void updateTestQualityCase(QualityTestCaseVO testCaseVO)
			throws BizException {
		
		QualityTestCase testCase = caseRepository.findById(testCaseVO.getId())
				.get();
		Date ctime = testCase.getCreateTime();
		System.out.println(testCase.getCreateTime());
		BeanUtils.copyProperties(testCaseVO, testCase);
		QualityTestQuery testQuery = qualityTestQueryRepository.findById(
				testCase.getQualityTestQuery().getId()).get();
		testQuery.setSqlText(testCaseVO.getQualityTestQueryVo().getSqlText());
		DataSource dataSource = dataSourceRepository.getOne(testCaseVO
				.getQualityTestQueryVo().getDataSourceId());
		testQuery.setDataSource(dataSource);
		DataSchema dataSchema = dataSchemaRepository.findById(
				testCaseVO.getQualityTestQueryVo().getDataSchemaId()).get();
		testQuery.setDataSchema(dataSchema);
		DataTable dataTable = dataTableRepository.findById(
				testCaseVO.getQualityTestQueryVo().getDataTableId()).get();
		testQuery.setDataTable(dataTable);
		DataField dataField = testFieldRepository.findById(
				testCaseVO.getQualityTestQueryVo().getDataFieldId()).get();
		testQuery.setDataField(dataField);
		testQuery.setName("数据源:" + dataSource.getName() + "->schema:"
				+ dataSchema.getName() + "->表:" + dataTable.getName() + "->字段:"
				+ dataField.getName() + ",生成测试案例");
		qualityTestQueryRepository.save(testQuery);
		testCase.setQualityTestQuery(testQuery);
		testCase.setEditTime(new Date());
		testCase.setCreateTime(ctime);
		System.out.println(ctime);
		caseRepository.save(testCase);
	}

	@Override
	@Transactional
	public void saveQualityTestCaseVo(QualityTestCaseVO testCaseVO)
			throws BizException {
		QualityTestCase qualityTestCase = new QualityTestCase();
		QualityTestQuery qualityTestQuery = new QualityTestQuery();
		ArrayList<DataField> list = new ArrayList<DataField>();
		//添加qualityTestQuery
		QualityTestQueryVO qualityTestQueryVO = testCaseVO
				.getQualityTestQueryVo();
		DataSource dataSource = dataSourceRepository.getOne(testCaseVO
				.getQualityTestQueryVo().getDataSourceId());
		qualityTestQuery.setDataSource(dataSource);
		DataSchema dataSchema = dataSchemaRepository.findById(
				qualityTestQueryVO.getDataSchemaId()).get();
		qualityTestQuery.setDataSchema(dataSchema);
		DataTable dataTable = dataTableRepository.findById(
				qualityTestQueryVO.getDataTableId()).get();
		qualityTestQuery.setDataTable(dataTable);
		DataField dataField = testFieldRepository.findById(
				qualityTestQueryVO.getDataFieldId()).get();
		qualityTestQuery.setDataField(dataField);
		qualityTestQuery.setSqlText(testCaseVO.getQualityTestQueryVo()
				.getSqlText());
		qualityTestQuery.setName("数据源:" + dataSource.getName() + "->schema:"
				+ dataSchema.getName() + "->表:" + dataTable.getName() + "->字段:"
				+ dataField.getName() + "sql脚本生成");
		qualityTestQueryRepository.save(qualityTestQuery);
		// 处理新建和修改时间
		BeanUtils.copyProperties(testCaseVO, qualityTestCase);
		qualityTestCase.setCreateTime(new Date());
		qualityTestCase.setEditTime(new Date());
		qualityTestCase.setId(null);
		qualityTestCase.setName(testCaseVO.getName());
		// qualityTestCase.setName("数据源:"+dataSource.getName()+"->schema:"+dataSchema.getName()+"->表:"+dataTable.getName()+"->字段:"+dataField.getName()+"案例生成");
		qualityTestCase.setQualityTestQuery(qualityTestQuery);
		caseRepository.save(qualityTestCase);
		int qtcID = qualityTestCase.getId();
		String num = "";
		if (qtcID > 0) {
			if (String.valueOf(qtcID).length() == 1) {
				num = "000" + qtcID;
			} else if (String.valueOf(qtcID).length() == 2) {
				num = "00" + qtcID;
			} else if (String.valueOf(qtcID).length() == 3) {
				num = "0" + qtcID;
			} else if (String.valueOf(qtcID).length() == 4) {
				num = qtcID + "";
			}
		}
		// 更新案例编号
		qualityTestCase.setCaseCode("SIT-SJJS-" + dataSchema.getName() + "."
				+ dataTable.getName() + "-" + num);
		Integer suiteId = testCaseVO.getTestSuiteId();
		if (suiteId != null) {
			List<Integer> caseIds = new ArrayList<Integer>();
			caseIds.add(qtcID);
			changeTestCasePosition(caseIds, suiteId);
		}
		caseRepository.save(qualityTestCase);
	}

	@Override
	@Transactional
	// public void changeTestCasePosition(Integer caseId, Integer suiteId) {
	public void changeTestCasePosition(List<Integer> caseId, Integer suiteId) {
		TestSuite testSuite = testSutieRepository.getOne(suiteId);
		// QualityTestCase testCase = caseRepository.getOne(caseId);
		// testCase.setTestSuite(testSuite);
		// 修改为多对对关系liu
		for (Integer id : caseId) {
			QualityTestCase testCase = caseRepository.getOne(id);
			testSuite.getQualityTestCases().remove(testCase);
			testSuite.getQualityTestCases().add(testCase);
		}
		testSuiteRepository.saveAndFlush(testSuite);

		/*
		 * caseRepository.saveAndFlush(testCase);
		 * testSuiteRepository.saveAndFlush(testSuite);
		 */

	}

	@Override
	@Transactional
	public void backDisorder(List<Integer> caseId, Integer suiteId) {
		// QualityTestCase testCase =
		// caseRepository.findByCaseIdAndSuiteId(caseId, suiteId);
		// testCase.setTestSuite(null);
		// 修改为多对对关系liu
		TestSuite testSuite = testSutieRepository.getOne(suiteId);
		for (Integer id : caseId) {
			QualityTestCase testCase = caseRepository.getOne(id);
			testSuite.getQualityTestCases().remove(testCase);
		}
		/*
		 * List<TestSuite> testSuites = new ArrayList();
		 * testCase.setTestSuites(testSuites);
		 */
		testSuiteRepository.saveAndFlush(testSuite);

		// caseRepository.saveAndFlush(testCase);
	}

	public List<QualityTestCase> findByTestSuitIdAndRoundId(
			Integer test_suit_id, Integer test_round_id) {
		return caseRepository.findByTestSuitIdAndRoundId(test_suit_id,
				test_round_id);
	}

	@Override
	public Page<QualityTestCase> findByNotSuiteId(Integer suiteId,
			Pageable pageable) {
		return caseRepository.findByNotSuiteId(suiteId, pageable);
	}

	@Override
	public Page<QualityTestCase> findByNotCaseName(Integer suiteId,
			String name, Pageable pageable) {
		return caseRepository.findByNotCaseName(suiteId, name, pageable);
	}

	@Override
	public Page<QualityTestCase> findCaseBySuiteId(Integer suiteId,
			Pageable pageable) {

		return caseRepository.findCaseBySuiteId(suiteId, pageable);
	}

	@Override
	public Page<QualityTestCase> findCaseByTestRoundId(Integer suiteId,
			Pageable pageable) {
		return caseRepository.findCaseByTestRoundId(suiteId, pageable);
	}

	@Override
	public Page<QualityTestCase> findByCaseName(Integer suiteId, String name,
			Pageable pageable) {
		return caseRepository.findCaseByName(suiteId, name, pageable);
	}

	@Override
	@Transactional
	public ResultVO copyTestCase(String testCaseIds) {
		try {
			QualityTestCase qualityTestCase = caseRepository.findById(
					Integer.parseInt(testCaseIds)).get();
			QualityTestCase copyqualityTestCase = new QualityTestCase();
			BeanUtils.copyProperties(qualityTestCase, copyqualityTestCase);
			copyqualityTestCase.setId(null);
			copyqualityTestCase.setCreateTime(new Date());
			copyqualityTestCase.setEditTime(new Date());
			copyqualityTestCase.setName(qualityTestCase.getName() + "_copy");
			QualityTestQuery qualityTestQuery = qualityTestCase
					.getQualityTestQuery();
			QualityTestQuery copyQualityTestQuery = new QualityTestQuery();
			BeanUtils.copyProperties(qualityTestQuery, copyQualityTestQuery);
			copyQualityTestQuery.setId(null);
			copyQualityTestQuery.setCreateTime(new Date());
			copyQualityTestQuery.setEditTime(new Date());
			copyQualityTestQuery.setName(qualityTestQuery.getName() + "_copy");
			qualityTestQueryRepository.save(copyQualityTestQuery);
			copyqualityTestCase.setQualityTestQuery(copyQualityTestQuery);
			copyqualityTestCase.setTestSuites(null);
			caseRepository.save(copyqualityTestCase);
			return new ResultVO(true, StatusCode.OK, "复制成功");
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("复制失败报错为：", e);
			return new ResultVO(false, StatusCode.ERROR, "复制失败");
		}
	}

	@Override
	public void downloadQualityCheckedCaseConverToExcel(String ids,
			HttpServletResponse res) throws Exception {
		List<QualityTestCase> list = findByQualityCaseIDs(ids);
		ArrayList<QualityTestCaseVO> testCaseVOList = new ArrayList<QualityTestCaseVO>();
		for (QualityTestCase testCase : list) {
			QualityTestCaseVO testCaseVO = new QualityTestCaseVO(testCase);
			testCaseVOList.add(testCaseVO);
		}
		HashMap<String, Object> map = new HashMap<String, Object>();
		String fileName = "testCase" + ".xls";
		// 创建表格文件
		XSSFWorkbook workbook = new XSSFWorkbook();
		String sheetName = "TestResultItemDetial";
		XSSFSheet sheet = workbook.createSheet(sheetName);
		XSSFRow header = sheet.createRow(0);
		// 设置表头名称
		header.createCell(0).setCellValue("id");
		header.createCell(1).setCellValue("名称");
		header.createCell(2).setCellValue("是否SQL");
		header.createCell(3).setCellValue("最大结果数");
		header.createCell(4).setCellValue("版本");
		header.createCell(5).setCellValue("创建人");
		header.createCell(6).setCellValue("修改人");
		header.createCell(7).setCellValue("创建时间");
		header.createCell(8).setCellValue("修改时间");
		header.createCell(9).setCellValue("是否分页");
		header.createCell(10).setCellValue("sql");
		header.createCell(11).setCellValue("源id");
		header.createCell(12).setCellValue("库id");
		header.createCell(13).setCellValue("表id");
		header.createCell(14).setCellValue("字段id");
		// 设置表内容
		int rowIndex = 1;
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		for (QualityTestCaseVO testCaseVO : testCaseVOList) {
			XSSFRow rowItem = sheet.createRow(rowIndex++);
			rowItem.createCell(0).setCellValue(testCaseVO.getId());
			rowItem.createCell(1).setCellValue(testCaseVO.getName());
			if (testCaseVO.getIsSQLCase() != null) {
				rowItem.createCell(2).setCellValue(testCaseVO.getIsSQLCase());
			} else {
				rowItem.createCell(2).setCellValue("");
			}
			if (testCaseVO.getMaxResultRows() != null) {
				rowItem.createCell(3).setCellValue(
						testCaseVO.getMaxResultRows());
			} else {
				rowItem.createCell(3).setCellValue("");
			}
			rowItem.createCell(4).setCellValue(testCaseVO.getVersion());
			rowItem.createCell(5).setCellValue(testCaseVO.getCreateUser());
			rowItem.createCell(6).setCellValue(testCaseVO.getEditUser());
			rowItem.createCell(7).setCellValue(
					sdf.format(testCaseVO.getCreateTime()));
			rowItem.createCell(8).setCellValue(
					sdf.format(testCaseVO.getEditTime()));
			rowItem.createCell(9).setCellValue(testCaseVO.getUsePage());
			if (testCaseVO.getQualityTestQueryVo() != null) {
				rowItem.createCell(10).setCellValue(
						testCaseVO.getQualityTestQueryVo().getSqlText());
				rowItem.createCell(11).setCellValue(
						testCaseVO.getQualityTestQueryVo().getDataSourceId());
				rowItem.createCell(12).setCellValue(
						testCaseVO.getQualityTestQueryVo().getDataSchemaId());
				rowItem.createCell(13).setCellValue(
						testCaseVO.getQualityTestQueryVo().getDataTableId());
				rowItem.createCell(14).setCellValue(
						testCaseVO.getQualityTestQueryVo().getDataFieldId());

			}

		}
		String filename = sheetName;
		res.reset(); // 非常重要
		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		res.setHeader("Access-Control-Allow-Origin", "*");// 允许跨域请求
		try {
			OutputStream out = res.getOutputStream();
			res.addHeader("Content-Disposition", "attachment;filename="
					+ java.net.URLEncoder.encode(fileName, "UTF-8"));
			workbook.write(out);
			out.flush();
			out.close();
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@Override
	@Transactional
	public JSONObject uploadSQLCase(MultipartFile file,
			HttpServletRequest request) throws Exception {
		JSONObject result = new JSONObject();
		int testSuiteId = Integer.parseInt(request.getParameter("testSuiteId"));
		logger.info("测试集：" + testSuiteId);
		// 文件名
		String fileName = file.getOriginalFilename();
		logger.info("文件名： " + fileName);
		// 文件后缀
		String suffixName = fileName.substring(fileName.lastIndexOf(".") + 1);
		logger.info("文件后缀名： " + suffixName);
		// 重新生成唯一文件名，用于存储数据库
		String newFileName = UUID.randomUUID().toString() + suffixName;
		logger.info("新的文件名： " + newFileName);
		// 创建文件
		String filePath=System.getProperty("user.dir");
		File dest = new File(filePath,fileName);
		Map<String, String> map = new HashMap<>();
		map.put("filePath", dest.getAbsolutePath());
		map.put("testSuiteId", testSuiteId + "");
		try {
			// 将流写入本地文件
			file.transferTo(dest);
			if (dest.exists()) {
				logger.info("临时文件:" + dest.getAbsolutePath());
				result.put("success", true);
				result.put("data", map);
				if (suffixName.equals("txt")) {
					readSQLCase(map);
				} else if (suffixName.equals("xls")
						|| suffixName.equals("xlsx")) {
					uploadExcel(map);
				}

			} else {
				logger.info("临时文件不存在:" + dest.getAbsolutePath());
				result.put("success", false);
				result.put("data", map);
			}
			return result;
		} catch (IOException e) {
			e.printStackTrace();
			return (JSONObject) result.put("success", false);
		}

	}

	public void uploadExcel(Map<String, String> map) {
		String filePath = map.get("filePath");
		String testSuiteId = map.get("testSuiteId");
		int suiteId = Integer.parseInt(testSuiteId);
		Workbook workbook = ExcelUtil.getWorkBook(filePath);
		if (workbook != null) {
			for (int sheetNum = 0; sheetNum < workbook.getNumberOfSheets(); sheetNum++) {
				// 获得当前sheet工作表
				Sheet sheet = workbook.getSheetAt(sheetNum);
				// 获取sheet表名
				// 获得当前sheet的开始行
				int firstRowNum = sheet.getFirstRowNum();
				// 获得当前sheet的结束行
				int lastRowNum = sheet.getLastRowNum();
				try {
					for (int rowNum = firstRowNum + 1; rowNum <= lastRowNum; rowNum++) {
						// 获得当前行
						Row row = sheet.getRow(rowNum);
						String l1 = ExcelUtil.getCellValue(row.getCell(0));
						String l2 = ExcelUtil.getCellValue(row.getCell(1));
						String l3 = ExcelUtil.getCellValue(row.getCell(2));
						String l4 = ExcelUtil.getCellValue(row.getCell(3));
						String l5 = ExcelUtil.getCellValue(row.getCell(4));
						String l6 = ExcelUtil.getCellValue(row.getCell(5));
						String l7 = ExcelUtil.getCellValue(row.getCell(6));
						String l8 = ExcelUtil.getCellValue(row.getCell(7));
						String l9 = ExcelUtil.getCellValue(row.getCell(8));
						String l10 = ExcelUtil.getCellValue(row.getCell(9));
						String l11 = ExcelUtil.getCellValue(row.getCell(10));
						String l12 = ExcelUtil.getCellValue(row.getCell(11));
						String l13 = ExcelUtil.getCellValue(row.getCell(12));
						String l14 = ExcelUtil.getCellValue(row.getCell(13));
						String l15 = ExcelUtil.getCellValue(row.getCell(14));

						SimpleDateFormat sdf = new SimpleDateFormat(
								"yyyy-MM-dd HH:mm:ss");
						QualityTestCase qualityTestCase = new QualityTestCase();
						// qualityTestCase.setId(Integer.valueOf(l1));
						qualityTestCase.setName(l2);
						qualityTestCase.setIsSQLCase(Boolean.parseBoolean(l3));
						if(l4 != null && !l4.equals(""))
							qualityTestCase.setMaxResultRows(Integer.parseInt(l4));
						qualityTestCase.setVersion(l5);
						qualityTestCase.setCreateUser(l6);
						qualityTestCase.setEditUser(l7);
						qualityTestCase.setCreateTime(sdf.parse(l8));
						qualityTestCase.setEditTime(sdf.parse(l9));
						qualityTestCase.setUsePage(Boolean.parseBoolean(l10));
						QualityTestQuery qualityTestQuery = new QualityTestQuery();
						qualityTestQuery.setCreateTime(sdf.parse(l8));
						qualityTestQuery.setCreateUser(l6);
						qualityTestQuery.setEditTime(sdf.parse(l9));
						qualityTestQuery.setEditUser(l7);
						qualityTestQuery.setSqlText(l11);
						int dataSourceId = Integer.parseInt(l12);
						int dataSchemaId = Integer.parseInt(l13);
						int dataTableId = Integer.parseInt(l14);
						int dataFieldId = Integer.parseInt(l15);
						DataSource dataSource = dataSourceRepository.findById(
								dataSourceId).get();
						qualityTestQuery.setDataSource(dataSource);
						DataSchema dataSchema = dataSchemaRepository.findById(
								dataSchemaId).get();
						qualityTestQuery.setDataSchema(dataSchema);
						DataTable dataTable = dataTableRepository.findById(
								dataTableId).get();
						qualityTestQuery.setDataTable(dataTable);
						DataField dataField = testFieldRepository.findById(
								dataFieldId).get();
						qualityTestQuery.setDataField(dataField);
						qualityTestQuery.setDataSource(dataSource);
						qualityTestQuery.setDataSchema(dataSchema);
						qualityTestQuery.setDataTable(dataTable);
						qualityTestQuery.setDataField(dataField);
						qualityTestCase.setQualityTestQuery(qualityTestQuery);
						// QualityTestQueryVO qualityTestQueryVO=new
						// QualityTestQueryVO(qualityTestQuery);
						QualityTestCaseVO qualityTestCaseVO = new QualityTestCaseVO(
								qualityTestCase);
						qualityTestCaseVO.setTestSuiteId(suiteId);
						saveQualityTestCaseVo(qualityTestCaseVO);
					}
				} catch (Exception e) {
					logger.info("导入出现异常" + e);
					e.printStackTrace();
				}
			}
		}
	}

	public void exportFalseCase(Integer testCaseId, HttpServletResponse res) {
		// 获取所有字段名称
		QualityTestCase qualityTestCase = caseRepository.findById(testCaseId)
				.get();
		QualityTestQuery qualityTestQuery = qualityTestCase
				.getQualityTestQuery();
		List<DataField> DataFields = qualityTestQuery.getDataTable()
				.getDataFields();
		List<String> fieldNames = new ArrayList<String>();
		for (DataField df : DataFields) {
			fieldNames.add(df.getName());
		}
		fieldNames.add("表名");		
		DataSchema dataSchema = qualityTestQuery.getDataSchema();
		String schemaName = dataSchema.getName();
		DataTable dataTable = qualityTestQuery.getDataTable();
		String tableName = dataTable.getName();
		String sqlText = qualityTestQuery.getSqlText();
		// 执行sql，获取源取差集
		AbstractAdapter adapter = null;
		DataSource ds = qualityTestQuery.getDataSource();
		String username = ds.getUserName();
		String pwd = ds.getPassword();
		EnumDatabaseType dbType = ds.getDatabaseType();
		String host = ds.getHost();
		String port = ds.getPort();
		String url = ds.getUrl();
		String driver = ds.getDriver();
		String sid = ds.getSid();
		Connection conn = null;
		SycData sycData = dataSourceService.getAdapterAndConnection(dbType,
				driver, url, port, host, username, pwd, sid);
		conn = sycData.getConn();
		adapter = sycData.getAdapter();
		// 拼写sql
		StringBuilder sql = new StringBuilder();
		sql.append("select * from ")
				.append(schemaName + ".\"" + tableName + "\"")
				.append(" minus ");
		sql.append(sqlText);
		String newsql = sql.toString();
		logger.info("生成的新" + newsql);
		List<String> s = new ArrayList<String>();
		// 执行sql
		try {
			PreparedStatement pstmt = conn.prepareStatement(sql.toString());
			ResultSet rs = pstmt.executeQuery();
			//
			List<Object> ob = new ArrayList<Object>();
			
			while (rs.next()) {
				for (int i = 0; i < fieldNames.size()-1; i++) {
					String fi = rs.getString(fieldNames.get(i));
					s.add(fi);
				}
				s.add(tableName);
			}
			
			rs.close();
			pstmt.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		SXSSFWorkbook workbook = new SXSSFWorkbook();
		// 根据test_database_id查出所有的表
		insertRow(fieldNames, workbook,s);
		// 生成excel文件
		String fileName="不符合要求的案例明细";
		try {
			downLoadExcelToWebsite(workbook, res,fileName);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	
	public static void downLoadExcelToWebsite(SXSSFWorkbook wb, HttpServletResponse response, String fileName) throws IOException {

        response.setHeader("Content-disposition", "attachment; filename="
                + new String((fileName + ".xlsx").getBytes("utf-8"), "ISO8859-1"));//设置下载的文件名

        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            wb.write(outputStream);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (null != wb) {
                try {
                    wb.dispose();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (null != outputStream) {
                try {
                    outputStream.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
	
	private List<List<String>> splitList(List<String> list , int groupSize){
        int length = list.size();
        // 计算可以分成多少组
        int num = ( length + groupSize - 1 )/groupSize ;
        List<List<String>> newList = new ArrayList<>(num);
        for (int i = 0; i < num; i++) {
            // 开始位置
            int fromIndex = i * groupSize;
            // 结束位置
            int toIndex = (i+1) * groupSize < length ? ( i+1 ) * groupSize : length ;
            newList.add(list.subList(fromIndex,toIndex)) ;
        }
        return  newList ;
    }

	public void insertRow(List<String> fieldNames, SXSSFWorkbook workbook,List<String> s) {
			Sheet sheet = workbook.createSheet("sheet");
			createTitle(workbook, sheet,fieldNames);
			List<List<String>> str=splitList(s, fieldNames.size());
			createTitle(workbook,sheet,fieldNames);
			// 新增数据行，并且设置单元格数据
			int rowNum = 1;
			for(int j=0;j<str.size();j++){
				Row row = sheet.createRow(rowNum++);
				for(int o=0;o<str.get(j).size();o++){
					row .createCell(o).setCellValue(str.get(j).get(o));
				}
			}

	}

	public void createTitle(SXSSFWorkbook workbook, Sheet sheet,List<String> filedNames) {
		// 设置列宽，setColumnWidth的第二个参数要乘以256，这个参数的单位是1/256个字符宽度
		sheet.setColumnWidth(1, 12 * 256);
		sheet.setColumnWidth(3, 17 * 256);
		// 设置为居中加粗
		CellStyle style = workbook.createCellStyle();
		Font font = workbook.createFont();
		style.setAlignment(HSSFCellStyle.ALIGN_CENTER);
		style.setFont(font);
		Row header = sheet.createRow(0);
		for(int i=0;i<filedNames.size();i++){
			header.createCell(i).setCellValue(filedNames.get(i));
			header.getCell(i).setCellStyle(style);
		}
	}

	@Override
	public Page<QualityTestCase> findCaseNotSuit(Integer suiteId, String name, EnumExecuteStatus exeState, Pageable pageable) {

		StringBuilder dataSql = new StringBuilder("select c.* FROM quality_test_case c JOIN quality_test_result tr ON c.id = tr.test_case_id " +
				"WHERE tr.id IN ( SELECT max(id) AS id FROM quality_test_result WHERE test_case_id NOT IN (" +
				" SELECT quality_test_case_id FROM suite_quality_case WHERE test_suite_id = "+suiteId+" )" +
				" GROUP BY test_case_id ) AND tr.exec_state = '"+exeState+"' ");

		StringBuilder countSql = new StringBuilder("select count(1) FROM quality_test_case c JOIN quality_test_result tr ON c.id = tr.test_case_id " +
				"WHERE tr.id IN ( SELECT max(id) AS id FROM quality_test_result WHERE test_case_id NOT IN (" +
				" SELECT quality_test_case_id FROM suite_quality_case WHERE test_suite_id = "+suiteId+" )" +
				" GROUP BY test_case_id ) AND tr.exec_state = '"+exeState+"' ");

		Query dataQuery = entityManager.createNativeQuery(dataSql.toString(), QualityTestCase.class);
		Query countQuery = entityManager.createNativeQuery(countSql.toString());

		//设置参数
		//if (StringUtils.isNotEmpty(parameter.getCustomerId())) {
			/*dataQuery.setParameter("suiteId", suiteId);
			dataQuery.setParameter("exeState", exeState);
			countQuery.setParameter("suiteId", suiteId);
			countQuery.setParameter("exeState", exeState);*/
	//	}

		//设置分页
		dataQuery.setFirstResult((int)pageable.getOffset());
		dataQuery.setMaxResults(pageable.getPageSize());
		//BigInteger count = (BigInteger) countQuery.getSingleResult();
	//	Object count = countQuery.getResultList().get(0);
		Object count = countQuery.getSingleResult();
		Long total = Long.valueOf(count.toString());

	//	Long total = count.longValue();
		List<QualityTestCase> content2 = total > pageable.getOffset() ? dataQuery.getResultList() : Collections.<QualityTestCase> emptyList();
		return new PageImpl<>(content2, pageable, total);
	}



}
