package com.jettech.service.Impl;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;

import com.jettech.EnumExecuteStatus;
import com.jettech.repostory.TestResultRepository;
import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.CellStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;


import com.jettech.entity.QualitySuite;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.QualityTestQuery;
import com.jettech.entity.QualityTestResult;

import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.QualityTestResultRepository;
import com.jettech.service.IQualityTestResultService;
import com.jettech.vo.QualityTestResultVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;


@Service
public class QualityTestResultServiceImpl implements IQualityTestResultService {

	@Autowired
	private QualityTestResultRepository repository;

	@Autowired
	private QualityTestCaseRepository qualityTestCaseRepository;

	@Autowired
	private TestResultRepository testResultRepository;

	@Override
	public QualityTestResult saveOne(QualityTestResult entity) {
		return repository.save(entity);
	}

	@Override
	public List<QualityTestResult> findAll() {

		return repository.findAll();
	}

	@Override
	public List<QualityTestResult> saveAll(List<QualityTestResult> list) {

		return repository.saveAll(list);
	}

	@Override
	public void save(QualityTestResult entity) {

		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		// int i = 1 / 0;
		repository.deleteById(id);
	}

	@Override
	public QualityTestResult getOneById(Integer id) {

		return repository.findById(id).get();
	}

	@Override
	public Page<QualityTestResult> findPage(int pageNum, int pageSize) {
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
		return repository.findAll(pageRequest);
	}

	@Override
	public Page<QualityTestResult> findPage(
			QualityTestResult qualityTestResult, int pageNum, int pageSize) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public QualityTestResult findById(Integer id) {
		return repository.getOne(id);
	}

	@Override
	public Page<QualityTestResult> findAllByPage(Pageable pageable) {
		return repository.findAll(pageable);
	}

	@Override
	public Page<QualityTestResult> findByCaseIdAndResult(Integer testCaseId,
			Boolean result, Pageable pageable) {
		Page<QualityTestResult> list = null;
		Specification<QualityTestResult> specification = new Specification<QualityTestResult>() {

			@Override
			public Predicate toPredicate(Root<QualityTestResult> root,
					CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if (StringUtils.isNotBlank(testCaseId.toString())) {
					predicateList.add(cb.equal(
							root.get("testCaseId").as(Integer.class),
							testCaseId));
				}
				if (StringUtils.isNotBlank(result.toString())) {
					predicateList.add(cb.equal(
							root.get("result").as(Boolean.class), result));
				}

				return cb.and(predicateList.toArray(new Predicate[predicateList
						.size()]));
			}
		};
		list = this.repository.findAll(specification, pageable);
		return list;
	}

	@Override
	public void exportEvidence(String testResultIds, HttpServletResponse res) {
		// 1 找到数据
		List<QualityTestResult> list = findBytestResultIds(testResultIds);
		ArrayList<QualityTestResultVO> voList = new ArrayList<QualityTestResultVO>();
		for (QualityTestResult qualityTestResult : list) {
			QualityTestResultVO vo = new QualityTestResultVO(qualityTestResult);
			voList.add(vo);
		}
		// 3导出数据
		// 建表 设置表格式
		HSSFWorkbook workbook = new HSSFWorkbook();
		String sheetName = "证迹表";
		HSSFSheet sheet = workbook.createSheet(sheetName);
		sheet.autoSizeColumn(1, true);
		CellStyle headerStyle = workbook.createCellStyle();

		// 水平居中
		headerStyle.setAlignment(CellStyle.ALIGN_CENTER);
		// 垂直居中
		headerStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		// 设置边框
		headerStyle.setBorderTop(CellStyle.BORDER_THIN);
		headerStyle.setBorderRight(CellStyle.BORDER_THIN);
		headerStyle.setBorderBottom(CellStyle.BORDER_THIN);
		headerStyle.setBorderLeft(CellStyle.BORDER_THIN);

		CellStyle cellStyle = workbook.createCellStyle();
		cellStyle.setAlignment(CellStyle.ALIGN_CENTER);
		// 垂直居中
		cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
		// 设置边框
		cellStyle.setBorderTop(CellStyle.BORDER_THIN);
		cellStyle.setBorderRight(CellStyle.BORDER_THIN);
		cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
		cellStyle.setBorderLeft(CellStyle.BORDER_THIN);

		// 设置自动换行
		cellStyle.setWrapText(true);

		HSSFRow header = sheet.createRow(0);
		// 存放内容
		header.createCell(0).setCellValue("序号");
		header.createCell(1).setCellValue("测试用例编号");
		header.createCell(2).setCellValue("字段");
		header.createCell(3).setCellValue("规则集");
		header.createCell(4).setCellValue("预期结果");
		header.createCell(5).setCellValue("实际结果");
		header.createCell(6).setCellValue("是否通过");
		header.createCell(7).setCellValue("执行SQL");

		int rowIndex = 1;
		int i = 1;
		NumberFormat f = new DecimalFormat("0000");
		for (int e = 0; e < voList.size(); e++) {
			QualityTestResultVO qualityTestResultVO=voList.get(e);
			Integer caseId = qualityTestResultVO.getTestCaseId();
			QualityTestCase testCase = qualityTestCaseRepository.findById(
					caseId).get();
			QualityTestQuery qualityTestQuery = testCase.getQualityTestQuery();
			String fieldName = qualityTestQuery.getDataField().getName();
			HSSFRow rowItem = sheet.createRow(rowIndex++);
			rowItem.createCell(0).setCellValue(i++ + "");
			rowItem.createCell(1).setCellValue(testCase.getCaseCode());
			rowItem.createCell(2).setCellValue(fieldName);
			QualitySuite qualitySuite = qualityTestQuery.getQualitySuite();
			if (qualitySuite != null) {
				rowItem.createCell(3).setCellValue(qualitySuite.getName());
			} else {
				rowItem.createCell(3).setCellValue("");
			}
			rowItem.createCell(4).setCellValue("0");
			//实际结果
			int sqlCount=qualityTestResultVO.getFalseItemCount();//sql通过值
			if(sqlCount==0){//如果实际结果是0 是否通过应该是true,否则为false
				rowItem.createCell(5).setCellValue(0);
			}else{
				rowItem.createCell(5).setCellValue(sqlCount);
			}
			Boolean result=qualityTestResultVO.getResult();
			rowItem.createCell(6).setCellValue(result);
			rowItem.createCell(7).setCellValue(qualityTestResultVO.getSqlText());

		}

		String fileName = "证迹表" + ".xls";
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

	private List<QualityTestResult> findBytestResultIds(String testResultIds) {
		List<QualityTestResult> list = new ArrayList<QualityTestResult>();
		if (StringUtils.isNotBlank(testResultIds)) {
			String[] ids = testResultIds.split(",");
			for (String testResultId : ids) {
				int id = Integer.parseInt(testResultId);
				QualityTestResult testResult = repository.findById(id).get();
				list.add(testResult);
			}
		}
		return list;
	}

	@Override
	public ResultVO findByTestRIdAndName(Integer testRoundId,
			String name,int pageNum,int pageSize) {
		Map<String,Object> resultmap = new HashMap<String,Object>();
		PageRequest pageable = PageRequest.of(pageNum - 1, pageSize);
        try {     	
    		Page<QualityTestResult> qualityTestResults=repository.findByRidAndName(testRoundId, name, pageable);
    		ArrayList<QualityTestResultVO> voList = new ArrayList<QualityTestResultVO>();
    		for (QualityTestResult qualityTestResult : qualityTestResults) {
    			QualityTestResultVO vo = new QualityTestResultVO(qualityTestResult);
    			voList.add(vo);
    		}
	        resultmap.put("totalElements",qualityTestResults.getTotalElements());
	        resultmap.put("totalPages",qualityTestResults.getTotalPages());
	        resultmap.put("list",voList);
	    }catch(Exception e) {
	     e.getLocalizedMessage();
	    }
        return new ResultVO(true, StatusCode.OK, "查询成功", resultmap);
	}

	@Override
	public Page<QualityTestResult> findTestResultByQualityCaseIDAndStartTimeAndEndTime(Integer testCaseId,
			String startTime, String endTime, Pageable pageable) {
		Page<QualityTestResult>testResultList=null;
		Specification<QualityTestResult> specification = new Specification<QualityTestResult>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<QualityTestResult> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if(StringUtils.isNotBlank(startTime)) {
					predicateList.add(
							criteriaBuilder.greaterThanOrEqualTo(root.get("startTime").as(String.class),startTime));
				}
				if(StringUtils.isNotBlank(endTime)) {
					predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime").as(String.class), endTime));
				}
				if(StringUtils.isNotBlank(testCaseId.toString())) {
					predicateList.add(criteriaBuilder.equal(root.get("testCaseId").as(Integer.class), testCaseId));
				}
				query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()])));
				query.orderBy(criteriaBuilder.desc(root.get("startTime").as(String.class)));
				return query.getRestriction();
			}
		};
		testResultList = this.repository.findAll(specification, pageable);
		return testResultList;
	}




}
