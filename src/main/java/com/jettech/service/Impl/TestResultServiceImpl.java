package com.jettech.service.Impl;

import java.io.IOException;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Predicate;
import javax.persistence.criteria.Root;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang3.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import com.jettech.EnumExecuteStatus;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.repostory.TestCaseRepository;
import com.jettech.repostory.TestResultRepository;
import com.jettech.service.ITestReusltService;
import com.jettech.vo.TestResultVO;

@Service
public class TestResultServiceImpl implements ITestReusltService {

	@Autowired
	private TestResultRepository repository;

	@Autowired
	private TestCaseRepository testCaseRepository;

	@Override
	public TestResult saveOne(TestResult entity) {
		return repository.save(entity);
	}

	@Override
	public List<TestResult> findAll() {

		return repository.findAll();
	}

	@Override
	public List<TestResult> saveAll(List<TestResult> list) {

		return repository.saveAll(list);
	}

	@Override
	public void save(TestResult entity) {

		repository.save(entity);
	}

	@Override
	public void delete(Integer id) {
		// int i = 1 / 0;
		repository.deleteById(id);
	}

	@Override
	public TestResult findById(Integer id) {
		Optional<TestResult> optional = repository.findById(id);
		if (optional.isPresent())
			return optional.get();
		return null;
	}

	@Override
	public Page<TestResult> findPage(TestResult testResult, int pageNum, int pageSize) {
		PageRequest pageRequest = PageRequest.of(pageNum - 1, pageSize);
		return repository.findAll(pageRequest);
	}

	@Override
	public Page<TestResult> findResultListByCaseId(String caseId, Pageable pageable) {
		// return repository.findTestResultListByCaseId(caseId,pageable);
		return repository.findByCaseId(caseId, pageable);
		// return null;findByCaseId
	}

	@Override
	public Page<TestResult> findAllByPage(Pageable pageable) {
		Page<TestResult> list = repository.findAll(pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public Page<TestResult> findTestResultByIdOrderStartTime(Integer testRoundID, Pageable pageable) {
		Page<TestResult> list = repository.findTestResultByIdOrderStartTime(testRoundID, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return new PageImpl<TestResult>(new ArrayList<TestResult>(), pageable, 0);
		}
	}

	@Override
	public Page<TestResult> findTestResultByTestRoundId(Integer testRoundId, Pageable pageable) {
		return null;
	}

	@Override
	public Page<TestResultItem> findByKeyValue(Integer keyValue, Pageable pageable) {
		Page<TestResultItem> list = repository.findByKeyValue(keyValue, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;
		} else {
			return null;
		}
	}

	@Override
	public Page<TestResult> findTestResultByTestCaseID(Integer caseID, Pageable pageable) {
		Page<TestResult> list = repository.findTestResultByTestCaseID(caseID, pageable);
		if (list.getSize() > 0 && list != null) {
			return list;

		} else {
			return new PageImpl<>(new ArrayList<TestResult>(), pageable, 0);

		}
	}

	@Override
	public Page<TestResult> findAll(Specification<TestResult> specification, Pageable pageable) {
		return repository.findAll(specification, pageable);
	}

	@Override
	public Page<TestResult> findTestResultByCaseName(String caseName, Pageable pageable) {
		Page<TestResult> list = null;
		if (caseName.equals("") || caseName == null) {
			list = repository.findAll(pageable);
		} else {
			list = repository.findCaseByNameLike(caseName, pageable);
//			System.out.println("dddddddddddddddddddddddd");
		}
		if (list != null) {
			return list;
		} else {
			return new PageImpl<TestResult>(new ArrayList<>(), pageable, 0);
		}
	}

	@Override
	public Page<TestResult> findAllByExecState(String state, Pageable pageable) {
		Page<TestResult> list = null;
		if (state == null || state.equals("")) {
			list = repository.findAll(pageable);
		} else {
			list = repository.findAllByExecState(state, pageable);
		}
		return list;
	}

	@Override
	public Page<TestResult> findByCaseIdAndSourceDataSource(String caseId,String sourceData, Pageable pageable) {
		Page<TestResult> list = null;
		if(sourceData==null ||sourceData.equals("") ) {
			list=repository.findByCaseId(caseId, pageable);
		}else {
			list=repository.findByCaseIdAndSourceDataSource(caseId,"%"+sourceData+"%",pageable);
		}
		if (list != null && list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestResult>(new ArrayList<TestResult>(), pageable, 0);
		}
	}

	public Page<TestResult> findByCaseIdAndTargetDataSource(String caseId, String targetData, Pageable pageable) {
		Page<TestResult> list = null;
		if(targetData==null ||targetData.equals("") ) {
			list=repository.findByCaseId(caseId, pageable);
		}else {
			list=repository.findByCaseIdAndTargetDataSource(caseId,"%"+targetData+"%",pageable);
		}
		if (list != null && list.getSize() > 0) {
			return list;
		} else {
			return new PageImpl<TestResult>(new ArrayList<TestResult>(), pageable, 0);
		}
	}

	@Override
	public Page<TestResult> findTestResultByCaseAndStartTimeAndEndTime(String caseId, String startTime, String endTime,
			EnumExecuteStatus execState, String testRoundId, String targetData, String sourceData, Pageable pageable) {
		Page<TestResult> testResultList = null;
		Specification<TestResult> specification = new Specification<TestResult>() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public Predicate toPredicate(Root<TestResult> root, CriteriaQuery<?> query,
					CriteriaBuilder criteriaBuilder) {
				ArrayList<Predicate> predicateList = new ArrayList<Predicate>();
				if (StringUtils.isNotBlank(startTime)) {
					predicateList.add(
							criteriaBuilder.greaterThanOrEqualTo(root.get("startTime").as(String.class), startTime));
				}
				if (StringUtils.isNotBlank(endTime)) {
					predicateList.add(criteriaBuilder.lessThanOrEqualTo(root.get("endTime").as(String.class), endTime));
				}
				if (StringUtils.isNotBlank(caseId)) {
					predicateList.add(criteriaBuilder.equal(root.get("caseId").as(String.class), caseId));
				}
				if (execState != null) {
					predicateList
							.add(criteriaBuilder.equal(root.get("execState").as(EnumExecuteStatus.class), execState));
				}
				if (StringUtils.isNotBlank(testRoundId)) {
					predicateList.add(criteriaBuilder.equal(root.get("testRoundId").as(String.class), testRoundId));
				}
				if (StringUtils.isNotBlank(targetData)) {
					predicateList.add(criteriaBuilder.like(root.get("targetData").as(String.class), targetData));
				}
				if (StringUtils.isNotBlank(sourceData)) {
					predicateList.add(criteriaBuilder.like(root.get("sourceData").as(String.class), sourceData));
				}
				query.where(criteriaBuilder.and(predicateList.toArray(new Predicate[predicateList.size()])));
				query.orderBy(criteriaBuilder.desc(root.get("startTime").as(String.class)));
				return query.getRestriction();
			}
		};
		testResultList = this.repository.findAll(specification, pageable);
		return testResultList;
	}

	@Override
	public List<TestResult> findByTestRoundId(Integer testRoundId) {
		return repository.findByTestRoundId(testRoundId);
	}

	public TestResult findEndTimeByCaseId(Integer caseId) {
		TestResult testResult = repository.findEndTimeByCaseId(caseId);
		return testResult;
	}

	@Override
	public List<Map<String, Object>> findTestCaseStatus(Integer caseId) {
		return repository.findTestCaseStatus(caseId);
	}

	@Override
	public void exportMigrationResult(String testResultIds, HttpServletResponse res) {
		// 查找数据
		List<TestResult> list = findByTestRoundIds(testResultIds);
		// 封装数据
		List<TestResultVO> voList = convertToVoList(list);
		// 创建表格并写入
		HSSFWorkbook workbook = new HSSFWorkbook();
		String sheetName = "testResult";
		HSSFSheet sheet = workbook.createSheet(sheetName);
		// 设置表头内容
		setHeaderContent(sheet);
		// 设置表格内容
		createTableContent(sheet, voList);
		// 导出
		export(workbook, res);
		// 导出

	}

	private void export(HSSFWorkbook workbook, HttpServletResponse res) {
		String fileName = "testResult" + ".xls";
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

	private void createTableContent(HSSFSheet sheet, List<TestResultVO> voList) {
		SimpleDateFormat sdf=new SimpleDateFormat("yyyy-mm-dd  HH:mm:ss");
		int rowIndex = 1;
		for (TestResultVO vo : voList) {
			HSSFRow contentRow = sheet.createRow(rowIndex++);
			if (StringUtils.isNotBlank(vo.getId().toString())) {
				contentRow.createCell(0).setCellValue(vo.getId().toString());
			} else {
				contentRow.createCell(0).setCellValue("null");
			}
			if (vo.getTestRoundId() != null && vo.getTestRoundId() > 0) {
				contentRow.createCell(1).setCellValue(vo.getTestRoundId());
			} else {
				contentRow.createCell(1).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getTargetCount().toString())) {
				contentRow.createCell(2).setCellValue(vo.getTargetCount());
			} else {
				contentRow.createCell(2).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getSourceCount().toString())) {
				contentRow.createCell(3).setCellValue(vo.getSourceCount());
			} else {
				contentRow.createCell(3).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getExecState().toString())) {
				contentRow.createCell(4).setCellValue(vo.getExecState().toString());
			} else {
				contentRow.createCell(4).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getEndTime().toString())) {
				System.out.println();
				contentRow.createCell(5).setCellValue(vo.getEndTime().toString().substring(0, vo.getEndTime().toString().length()-2));
			} else {
				contentRow.createCell(5).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getStartTime().toString())) {
				contentRow.createCell(6).setCellValue(vo.getStartTime().toString().substring(0, vo.getEndTime().toString().length()-2));
			} else {
				contentRow.createCell(6).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getResult().toString())) {
				contentRow.createCell(7).setCellValue(vo.getResult().toString());
			} else {
				contentRow.createCell(7).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getSameRow().toString())) {
				contentRow.createCell(8).setCellValue(vo.getSameRow().toString());
			} else {
				contentRow.createCell(8).setCellValue("null");
			}
			if (vo.getNotSameRow() != null && vo.getNotSameRow() > 0) {
				contentRow.createCell(9).setCellValue(vo.getNotSameRow().toString());
			} else {
				contentRow.createCell(9).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getNotSameData().toString())) {
				contentRow.createCell(10).setCellValue(vo.getNotSameData().toString());
			} else {
				contentRow.createCell(10).setCellValue("null");
			}
			if (vo.getTargetSql() != null && !vo.getTargetSql().equals("")) {
				contentRow.createCell(11).setCellValue(vo.getTargetSql());
			} else {
				contentRow.createCell(11).setCellValue("null");
			}
			if (vo.getSourceSql() != null && !vo.getSourceSql().equals("")) {
				contentRow.createCell(12).setCellValue(vo.getSourceSql().toString());
			} else {
				contentRow.createCell(12).setCellValue("null");
			}
//			if (StringUtils.isNotBlank(vo.getSourceCol().toString())) {
//				contentRow.createCell(13).setCellValue(vo.getSourceCol().toString());
//			} else {
//				contentRow.createCell(13).setCellValue("null");
//			}
//			if (StringUtils.isNotBlank(vo.getTargetCol().toString())) {
//				contentRow.createCell(14).setCellValue(vo.getTargetCol().toString());
//			} else {
//				contentRow.createCell(14).setCellValue("null");
//			}
			if (StringUtils.isNotBlank(vo.getSourceKey().toString())) {
				contentRow.createCell(13).setCellValue(vo.getSourceKey().toString());
			} else {
				contentRow.createCell(13).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getTargetKey().toString())) {
				contentRow.createCell(14).setCellValue(vo.getTargetKey().toString());
			} else {
				contentRow.createCell(14).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getTargetData().toString())) {
				contentRow.createCell(15).setCellValue(vo.getTargetData().toString());
			} else {
				contentRow.createCell(15).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getSourceData().toString())) {
				contentRow.createCell(16).setCellValue(vo.getSourceData().toString());
			} else {
				contentRow.createCell(16).setCellValue("null");
			}
			if (vo != null && !vo.equals("")) {
				Integer id = vo.getId();
				TestResult testResult = repository.findById(id).get();
				String caseId = testResult.getCaseId();
				TestCase testCase = testCaseRepository.getOne(Integer.parseInt(caseId));
				contentRow.createCell(17).setCellValue(testCase.getName());
			} else {
				contentRow.createCell(17).setCellValue("null");
			}
			if (vo.getCreateUser() != null) {
				contentRow.createCell(18).setCellValue(vo.getCreateUser());
			} else {
				contentRow.createCell(18).setCellValue("null");

			}
			if (vo.getEditUser() != null) {
				contentRow.createCell(19).setCellValue(vo.getEditUser());
			} else {
				contentRow.createCell(19).setCellValue("null");

			}
			if (StringUtils.isNotBlank(vo.getCreateTime().toString())) {
				contentRow.createCell(20).setCellValue(vo.getCreateTime().toString().substring(0, vo.getEndTime().toString().length()-2));
			} else {
				contentRow.createCell(20).setCellValue("null");
			}
			if (StringUtils.isNotBlank(vo.getEditTime().toString())) {
				contentRow.createCell(21).setCellValue(vo.getEditTime().toString().substring(0, vo.getEndTime().toString().length()-2));
			} else {
				contentRow.createCell(21).setCellValue("null");
			}
		}
	}

	private void setHeaderContent(HSSFSheet sheet) {
		HSSFRow header = sheet.createRow(0);
		header.createCell(0).setCellValue("id");
		header.createCell(1).setCellValue("轮次的id");
		header.createCell(2).setCellValue("目标数据总量");
		header.createCell(3).setCellValue("源数据总量");
		header.createCell(4).setCellValue("执行状态");
		header.createCell(5).setCellValue("结束时间");
		header.createCell(6).setCellValue("开始时间");
		header.createCell(7).setCellValue("结果");
		header.createCell(8).setCellValue("相同数据行数");
		header.createCell(9).setCellValue("不同数据行数");
		header.createCell(10).setCellValue("不同数据值数量");
		header.createCell(11).setCellValue("目标查询语句");
		header.createCell(12).setCellValue("源查询语句");
		header.createCell(13).setCellValue("源key");
		header.createCell(14).setCellValue("目标key");
		header.createCell(15).setCellValue("目标数据源");
		header.createCell(16).setCellValue("源数据源");
		header.createCell(17).setCellValue("案例名称");
		header.createCell(18).setCellValue("创建人");
		header.createCell(19).setCellValue("修改人");
		header.createCell(20).setCellValue("创建时间");
		header.createCell(21).setCellValue("修改时间");
	}

	private List<TestResultVO> convertToVoList(List<TestResult> list) {
		List<TestResultVO> voList = new ArrayList<TestResultVO>();
		for (TestResult testResult : list) {
			TestResultVO vo = new TestResultVO(testResult);
			voList.add(vo);
		}
		return voList;
	}

	private List<TestResult> findByTestRoundIds(String testResultIds) {
		List<TestResult> list = new ArrayList<TestResult>();
		if (StringUtils.isNotBlank(testResultIds)) {
			String[] ids = testResultIds.split(",");
			for (String testResultId : ids) {
				int id = Integer.parseInt(testResultId);
				TestResult testResult = repository.getOne(id);
				list.add(testResult);
			}
		} else {
			list = repository.findAll();
		}
		return list;
	}

}
