package com.jettech.service.Impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.util.CellRangeAddress;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jettech.entity.TestResult;
import com.jettech.entity.TestRound;
import com.jettech.repostory.TestResultRepository;
import com.jettech.repostory.TestRoundRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.TestRoundService;
import com.jettech.vo.ExportVO;
import com.jettech.vo.TestResultVO;

@Service
public class TestRoundServiceImpl implements TestRoundService {

	@Autowired
	TestRoundRepository repository;
	@Autowired
	TestResultRepository testResultRepository;

	@Autowired
	TestSuiteRepository testSuiteRepository;

	@Override
	public List<TestRound> findAll() {
		return repository.findAll();
	}

	@Override
	public List<TestRound> saveAll(List<TestRound> list) {
		return repository.saveAll(list);
	}

	@Override
	public void save(TestRound entity) {
		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		repository.deleteById(id);
	}

	@Override
	public TestRound findById(Integer id) {
		Optional<TestRound> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Page<TestRound> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public List<TestRound> findBySuiteId(int SuiteId) {
		return repository.getAmountBySuiteId(SuiteId);
	}

	@Override
	public Page<TestRound> findByTestSuiteName(String suiteName, int pageNum, int pageSize) {
		return null;
	}

	@Override
	public Page<TestRound> findAllRoundBytestResultID(Integer testResultID, PageRequest pageable) {
		return null;
	}

	@Override
	public Page<TestRound> findAllRoundBytestSuiteID(Integer testSuiteID, Pageable pageable) {
		Page<TestRound> list = repository.findAllRoundBytestSuiteID(testSuiteID, pageable);
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestRound>(new ArrayList<TestRound>(), pageable, 0);
		}
	}

	@Override
	public TestRound selectTestRoundByTestSuiteId(Integer testSuiteId) {
		List<TestRound> test = repository.findByTestSuiteId(testSuiteId);
		TestRound testRound = new TestRound();
		if (test.size() > 0) {
			testRound = test.get(0);
		}
		return testRound;
	}

	@Override
	public Page<TestRound> findTestRoundBySuiteName(String suiteName, Pageable pageable) {
		Page<TestRound> list = null;
		if (suiteName.equals("") || suiteName == null) {
			list = repository.findAll(pageable);
		} else {
			Specification<TestRound> specification = new Specification<TestRound>() {

				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Predicate toPredicate(Root<TestRound> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
					ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
					if (StringUtils.isNotBlank(suiteName)) {
						predicateList.add(cb.like(root.get("testSuite").get("name"), "%" + suiteName + "%"));
					}
					return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
				}
			};
			list = this.repository.findAll(specification, pageable);
		}
		if (list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestRound>(new ArrayList<TestRound>(), pageable, 0);
		}
	}

	@Override
	public int updateWithVersion(int id, int successCount, Date endTime, int version) {
		return repository.updateWithVersion(id, successCount, endTime, version);
	}

	@Override
	public Page<TestRound> findBySuiteIdAndStartTimeAndEndTime(String testSuiteID, String startTime, String endTime,
			Pageable pageable) {
		Page<TestRound> testRoundList = null;
		if(StringUtils.isNotBlank(startTime)&&StringUtils.isNotBlank(endTime)) {
			Specification<TestRound> querySpecifi = new Specification<TestRound>() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				@Override
				public Predicate toPredicate(Root<TestRound> root, CriteriaQuery<?> query,
						CriteriaBuilder criteriaBuilder) {
					ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
					if (StringUtils.isNotBlank(testSuiteID)) {
						predicateList.add(criteriaBuilder.equal(root.get("testSuite").get("id"), testSuiteID));
					}
					if (StringUtils.isNotBlank(startTime)) {
						predicateList.add(
								criteriaBuilder.greaterThanOrEqualTo(root.get("startTime").as(String.class), startTime));
					}
					if (StringUtils.isNotBlank(endTime)) {
						String endTime1=endTime + " 23:59:59";
						predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime").as(String.class), endTime1));
					}
					return criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()]));
				}
			};
			testRoundList = this.repository.findAll(querySpecifi, pageable);
		}else {
			testRoundList=this.repository.findAllRoundBytestSuiteID(Integer.parseInt(testSuiteID),pageable);
		}
		return testRoundList;
	}

	@Override
	public String exportQualityReport(Integer testRoundId, Integer testSuiteId,HttpServletResponse response) {
		// 1 查找数据
		//查出这个测试集这个轮次下案例所属的表名
		List<ExportVO> exportVO=new ArrayList<ExportVO>();
		List<String> tableNameList = repository.findBysuiteIdAndRoundId(testSuiteId,testRoundId);
		int sumCase=0;
		int failSumCase=0;
		int successSumCase=0;
		for(String tableName:tableNameList){
			ExportVO ex=new ExportVO();
			ex.setTableName(tableName);
			//查询这个表名的案例数量
		  int caseCount=repository.findBysIdAndRIdAndTName(testSuiteId, testRoundId, tableName);
		  ex.setCaseCount(caseCount);
		  sumCase=sumCase+caseCount;
		  //查询这个表对应的失败案例
		  int failCount=repository.getCountBysIdAndRIdAndTName(testSuiteId, testRoundId, tableName);
		  ex.setFailCase(failCount);
		  failSumCase=failSumCase+failCount;
		  //成功数
		  int successCount=caseCount-failCount;
		  ex.setSuccessCase(successCount);
		  successSumCase=successSumCase+successCount;
		  //通过率
		  String passPersent=(float)(successCount)/caseCount*100+"%";
		  ex.setPassPercent(passPersent);
		  //未通过率
		  String falsePercent=(float)(failCount)/caseCount*100+"%";
		  ex.setFailPassPercent(falsePercent);
		  exportVO.add(ex);
		}
		//总通过率
		String totalPassPercent=(float)(successSumCase)/sumCase*100+"%";
		//总未通过率
		String totalFailpercent=(float)(failSumCase)/sumCase*100+"%";
		
		// 3 创建表格
		// 创建表格并写入
		HSSFWorkbook workbook = new HSSFWorkbook();
		String sheetName = "第"+testRoundId+"轮执行情况说明";
		HSSFSheet sheet = workbook.createSheet(sheetName);
		// 设置表格内容
		createTableContent(sheet,exportVO,testRoundId,sumCase,failSumCase,successSumCase,
				totalPassPercent,totalFailpercent);

		// 4 导出excel
		exportToExcel(workbook, response);
		return "download excel";
	}

	private void exportToExcel(HSSFWorkbook workbook, HttpServletResponse res) {
		String fileName = "数据质量测试报告" + ".xls";
		res.reset();
		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		// 允许跨域请求
		res.setHeader("Access-Control-Allow-Origin", "*");
		try {
			OutputStream os = res.getOutputStream();
			res.addHeader("Content-Disposition",
					"attachment;fileName=" + java.net.URLEncoder.encode(fileName, "UTF-8"));
			workbook.write(os);
			os.flush();
			os.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	private void createTableContent(HSSFSheet sheet,List<ExportVO> exportVo,Integer testRoundId,
			Integer sumCase,Integer failSumCase,Integer successSumCase,
			String totalPassPercent,String totalFailpercent){
		// 设置表头内容
		setHeaderContent(sheet,testRoundId);
		 int rowIndex = 2;
		 int index=1;
	        for (ExportVO e : exportVo) {
	            HSSFRow contentRow = sheet.createRow(rowIndex++);
	            contentRow.createCell(0).setCellValue(index++);
	            contentRow.createCell(1).setCellValue(e.getTableName());
	            contentRow.createCell(2).setCellValue(e.getCaseCount());
	            contentRow.createCell(3).setCellValue(e.getCaseCount());
	            contentRow.createCell(4).setCellValue("100%");
	            contentRow.createCell(5).setCellValue(e.getSuccessCase());
	            contentRow.createCell(6).setCellValue(e.getPassPercent());
	            contentRow.createCell(7).setCellValue(e.getFailCase());
	            contentRow.createCell(8).setCellValue(e.getFailPassPercent());
	        }
	        HSSFRow contentRow = sheet.createRow(rowIndex++);
	        contentRow.createCell(0).setCellValue("合计");
            contentRow.createCell(1).setCellValue(exportVo.size());
            contentRow.createCell(2).setCellValue(sumCase);
            contentRow.createCell(3).setCellValue(sumCase);
            contentRow.createCell(4).setCellValue("100%");
            contentRow.createCell(5).setCellValue(successSumCase);
            contentRow.createCell(6).setCellValue(totalPassPercent);
            contentRow.createCell(7).setCellValue(failSumCase);
            contentRow.createCell(8).setCellValue(totalFailpercent);
	        
	}
	

	private void setHeaderContent(HSSFSheet sheet,int testRoundId) {
		HSSFRow firstHeaderRow = sheet.createRow(0);
		HSSFRow secondHeaderRow = sheet.createRow(1);
		CellRangeAddress rangeAddress0 = new CellRangeAddress(0, 0, 0, 13);
		sheet.addMergedRegion(rangeAddress0);
		firstHeaderRow.createCell(0).setCellValue("第"+testRoundId+"轮执行情况说明");
		secondHeaderRow.createCell(0).setCellValue("序号");
		secondHeaderRow.createCell(1).setCellValue("表名");
		secondHeaderRow.createCell(2).setCellValue("测试用例个数");
		secondHeaderRow.createCell(3).setCellValue("执行用例个数");
		secondHeaderRow.createCell(4).setCellValue("执行率");
		secondHeaderRow.createCell(5).setCellValue("通过数");
		secondHeaderRow.createCell(6).setCellValue("通过率");
		secondHeaderRow.createCell(7).setCellValue("未通过数");
		secondHeaderRow.createCell(8).setCellValue("未通过率");
		secondHeaderRow.createCell(9).setCellValue("未执行数");
		secondHeaderRow.createCell(10).setCellValue("未执行率");
		secondHeaderRow.createCell(11).setCellValue("缺陷数量");
		secondHeaderRow.createCell(12).setCellValue("缺陷占比");
		secondHeaderRow.createCell(13).setCellValue("备注");
		

	}

	private List<TestResultVO> convertToVoList(List<TestResult> list) {
		List<TestResultVO> voList = new ArrayList<TestResultVO>();
		for (TestResult testResult : list) {
			TestResultVO vo = new TestResultVO(testResult);
			voList.add(vo);
		}
		return voList;
	}

	/*private List<String> findByTestRoundIdAndTestSuiteId(Integer testRoundId,Integer testSuiteId) {
		List<TestResult> list = new ArrayList<TestResult>();
		if (testRoundId != null && testRoundId > 0) {
			list = testResultRepository.findByTestRoundId(testRoundId);
		} else {
			list = testResultRepository.findAll();
		}
		return list;
	}*/

}
