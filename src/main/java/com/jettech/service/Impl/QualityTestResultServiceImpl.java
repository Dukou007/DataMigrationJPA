package com.jettech.service.Impl;

import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;

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

import com.jettech.entity.QualityTestCase;
import com.jettech.entity.QualityTestResult;
import com.jettech.entity.TestCase;
import com.jettech.repostory.QualityTestCaseRepository;
import com.jettech.repostory.QualityTestResultRepository;
import com.jettech.repostory.TestCaseRepository;
import com.jettech.service.IQualityTestResultService;
import com.jettech.vo.QualityTestResultVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

@Service
public class QualityTestResultServiceImpl implements IQualityTestResultService {

	@Autowired
	private QualityTestResultRepository repository;
	@Autowired
	private TestCaseRepository testCaseRepository;
	@Autowired
	private QualityTestCaseRepository qualityTestCaseRepository;

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
	public Page<QualityTestResult> findPage(QualityTestResult qualityTestResult, int pageNum, int pageSize) {
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
	public Page<QualityTestResult> findByCaseIdAndResult(Integer testCaseId, Boolean result, Pageable pageable) {
		Page<QualityTestResult> list = null;
		Specification<QualityTestResult> specification = new Specification<QualityTestResult>() {

			@Override
			public Predicate toPredicate(Root<QualityTestResult> root, CriteriaQuery<?> query, CriteriaBuilder cb) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if (StringUtils.isNotBlank(testCaseId.toString())) {
					predicateList.add(cb.equal(root.get("testCaseId").as(Integer.class), testCaseId));
				}
				if (StringUtils.isNotBlank(result.toString())) {
					predicateList.add(cb.equal(root.get("result").as(Boolean.class), result));
				}

				return cb.and(predicateList.toArray(new Predicate[predicateList.size()]));
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
		// 2 封装数据
		/*
		 * HashMap<String, Object> map = new HashMap<String, Object>(); for
		 * (QualityTestResultVO qtr : voList) { int i = 1; Integer caseId =
		 * qtr.getTestCaseId(); TestCase testCase =
		 * testCaseRepository.findById(caseId).get(); map.put("序号", qtr.getId());
		 * map.put("测试用例编号", "SIT-SJJX" + testCase.getName() + i++); map.put("测试意图",
		 * qtr.getTestPurpose()); map.put("预期结果", qtr.getPurposeValue());
		 * map.put("实际结果", qtr.getResult()); map.put("是否通过", qtr.getResult());
		 * map.put("执行SQL", qtr.getSqlText()); }
		 */
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
		header.createCell(2).setCellValue("测试意图");
		header.createCell(3).setCellValue("预期结果");
		header.createCell(4).setCellValue("实际结果");
		header.createCell(5).setCellValue("是否通过");
		header.createCell(6).setCellValue("执行SQL");
		
		
		int rowIndex = 1;
		int i=1;
		NumberFormat f=new DecimalFormat("0000");
		for (int e=0;e<voList.size();e++) {
		// daichuli 
			
			Integer caseId = voList.get(e).getTestCaseId();
			QualityTestCase testCase = qualityTestCaseRepository.findById(caseId).get();
			HSSFRow rowItem = sheet.createRow(rowIndex++);
			rowItem.createCell(0).setCellValue(i++ + "");
			rowItem.createCell(1).setCellValue("SIT-SJJX" + testCase.getName() + f.format(e));
			rowItem.createCell(2).setCellValue(voList.get(e).getTestPurpose());
			rowItem.createCell(3).setCellValue(voList.get(e).getPurposeValue());
			// to be finished
			rowItem.createCell(4).setCellValue(voList.get(e).getItemCount());
			rowItem.createCell(5).setCellValue(voList.get(e).getResult());
			rowItem.createCell(6).setCellValue(voList.get(e).getSqlText());

		}

		String fileName = "证迹表" + ".xls";
		res.reset(); // 非常重要
		res.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
		res.setHeader("Access-Control-Allow-Origin", "*");// 允许跨域请求
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
		} else {
			list = repository.findAll();
		}
		return list;
	}

}
