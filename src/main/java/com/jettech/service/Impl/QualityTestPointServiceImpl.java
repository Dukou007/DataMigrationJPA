package com.jettech.service.Impl;

import com.jettech.controller.QualityTestPointController;
import com.jettech.entity.*;
import com.jettech.repostory.QualitySuiteRepository;
import com.jettech.repostory.QualityTestPointRepository;
import com.jettech.service.*;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

@Service
public class QualityTestPointServiceImpl implements IQualityTestPointService {
	
	private static Logger log = LoggerFactory.getLogger(QualityTestPointServiceImpl.class);
	
	@Autowired
	QualityTestPointRepository qualityTestPointRepository;
	
	@Autowired
	private IQualityTestQueryService qualityTestQueryService;

	@Autowired
	private ITestFieldService testFieldService;
	
	@Autowired
	private IQualityTestCaseService IQualityTestCaseService;
	
	@Autowired
	private TestSuiteService testSuiteService;
	
	@Autowired
	QualitySuiteRepository qualitySuiteRepository;
	
	@Autowired
	private DataSchemaService testDatabaseService;

	private final static String CHECK_NULL = "CHECK_NULL";// 检查空值
	private final static String CHECK_RANGE = "CHECK_RANGE";// 检查范围
	private final static String CHECK_LENGTH = "CHECK_LENGTH";// 检查长度
	private final static String CHECK_UNIQUE = "CHECK_UNIQUE";// 检查唯一性
	private final static String CHECK_NOTIN = "CHECK_NOTIN";// 检查码值范围
	private final static String CHECK_NOTFLOAT = "CHECK_NOTFLOAT";// 检查非浮点数
	private final static String CHECK_NON_AMOUNT_RANGE = "CHECK_NON_AMOUNT_RANGE";// 检查非金额范围
	private final static String CHECK_POINT_TIME_AND_DATE = "CHECK_POINT_TIME_AND_DATE";// 检查日期或者时间格式
	private final static String CHECK_SUM = "CHECK_SUM";// 检查字段是否为两个字段的和
	private final static String CHECK_FOREIGN_KEY = "CHECK_FOREIGN_KEY";// 检查外键是否是另一张表的主键

	@Override
	public void createCase(Object obj, QualityTestQuery qualityTestQuery,
			String dbName, String talbeName, String fieldName,
			QualitySuite qualitySuite) {
//		Integer testSuiteId = Integer.parseInt(obj + "");
		/* TestSuite testSuite = testSuiteService.getOneById(testSuiteId); */
//		TestSuite testSuite = testSuiteService.findById(testSuiteId);
		QualityTestCase qualityTestCase = new QualityTestCase();
		qualityTestCase.setQualityTestQuery(qualityTestQuery);
		// qualityTestCase.setTestSuite(testSuite);
		qualityTestCase.setName(dbName + "数据库" + talbeName + "表" + fieldName
				+ "字段" + qualitySuite.getName() + "的测试案例");
		// 添加保存测试集 20190411
//		qualityTestCase.setTestSuite(testSuite);
		IQualityTestCaseService.save(qualityTestCase);
	}

	/**
	 * 此检查点为空值检测检查点，起名checkNull 作用：检测一个字段是否为空值包含is null,''
	 * 
	 * @return
	 */

	public String checkNull(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		String rule1 = qualityRules.get(0).getName();// is null
		String rule2 = qualityRules.get(1).getName();// ''
		String andOr = qualitySuite.getAndOr();// or
		sb.append("select ");
		// 选中的要查询的字段名称的集合

		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}
		// 组织被测表
		sb.append(" from " + dbName).append(".").append(talbeName + " ");
		// 组织过滤条件
		sb.append("where").append(" " + fieldName).append(" " + rule1)
				.append(" " + andOr).append(" " + fieldName)
				.append(" " + rule2);
		// 参考sql:select * from bdm.tablename where check_field is null or
		// chek_field = ''

		return sb.toString();
	}

	/**
	 * 新增/配置检查点(根据多个字段，一个规则集，产生多个检查点) 并自动生成sql脚本， 自动生成测试案例，测试任务，此检查点为值域检查，有[
	 * ],(),[),(]四种情况，起名checkRange
	 *
	 */
	public String checkRange(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields, String dataType) {
		StringBuilder sb = new StringBuilder();
		String rule1 = qualityRules.get(0).getName();//is null
		String rule2 = qualityRules.get(1).getName();//>,此为可能的一种看选择的规则不同得到的结果不同
		String rule3 = qualityRules.get(2).getName();//<=，,此为可能的一种看选择的规则不同得到的结果不同
		String andOr = qualitySuite.getAndOr();
		Integer leftValue = qualitySuite.getLeftValue();
		Integer rightValue = qualitySuite.getRightValue();
		sb.append("select ");
		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}
		if (dataType.equals("vachar") || dataType.equals("char")) {
			sb.append(" from " + dbName).append(".").append(talbeName + " ")
					.append("where").append(" " + fieldName)
					.append(" " + rule1).append(" " + andOr)
					.append(" " + fieldName).append(" " + rule2)
					.append(" '" + rightValue).append("' " + andOr)
					.append(" " + fieldName).append(" " + rule3)
					.append(" '" + leftValue).append("'");
		} else {
			sb.append(" from " + dbName).append(".").append(talbeName + " ")
					.append("where").append(" " + fieldName)
					.append(" " + rule1).append(" " + andOr)
					.append(" " + fieldName).append(" " + rule2)
					.append(" " + rightValue).append(" " + andOr)
					.append(" " + fieldName).append(" " + rule3)
					.append(" " + leftValue);
		}

		return sb.toString();

	}

	public String checkLength(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		String rule1 = qualityRules.get(0).getName();//is null
		String rule2 = qualityRules.get(1).getName();//>
		String rule3 = qualityRules.get(2).getName();//<

		String andOr = qualitySuite.getAndOr();
		Integer leftValue = qualitySuite.getLeftValue();
		Integer rightValue = qualitySuite.getRightValue();
		sb.append("select ");
		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}

		sb.append(" from " + dbName).append(".").append(talbeName + " ")
				.append("where").append(" " + fieldName).append(" " + rule1)
				.append(" " + andOr).append(" length(" + fieldName + ")")
				.append(" " + rule2).append(" " + rightValue)
				.append(" " + andOr).append(" length(" + fieldName + ")")
				.append(" " + rule3).append(" " + leftValue);
		// sql实例：select * from bdm.tablename where check_field is null or
		// (length(chek_field) > 8 and length(chek_field) < 12)
		return sb.toString();
	}

	public String checkUnique(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		String rule = qualityRules.get(0).getName();//having count(*) > 1
		sb.append("select ");
		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}

		sb.append(" from " + dbName).append(".").append(talbeName + " ")
				.append("group by").append(" concat(" + fieldName + ")")
				.append(" " + rule);
//select check_field1,check_field2 from bdm.tablenam group by concat(check_field1,check_field2) having count(*) > 1
		return sb.toString();
	}

	public String checkNotIn(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		String rule1 = qualityRules.get(0).getName();// is null
		String rule2 = qualityRules.get(1).getName();// not in
		// 不在码值范围内 not in暂时写死
		String andOr = qualitySuite.getAndOr();// or
		sb.append("select ");
		// 选中的要查询的字段名称的集合

		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}
		// 组织被测表
		sb.append(" from " + dbName).append(".").append(talbeName + " ");
		// 组织过滤条件
		sb.append("where").append(" " + fieldName).append(" " + rule1)
				.append(" " + andOr).append(" " + fieldName)
				.append(" " + rule2).append(" (")
				.append(32 + "," + "fdsh" + "," + 56).append(")");
		// select * from bdm.tablename where check_field is null or check_field
		// not in (12,3)
		return sb.toString();
	}

	public String checkNotFloat(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		String rule1 = qualityRules.get(0).getName();// is null
		String rule2 = qualityRules.get(1).getName();// as decimal
		String andOr = qualitySuite.getAndOr();// or
		sb.append("select ");
		// 选中的要查询的字段名称的集合

		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}
		// 组织被测表
		sb.append(" from " + dbName).append(".").append(talbeName + " ");
		// 组织过滤条件
		sb.append("where").append(" " + fieldName).append(" " + rule1)
				.append(" " + andOr).append(" CAST(" + fieldName)
				.append(" " + rule2 + ")").append(" " + rule1);
		// 参考sql:select * from bdm.tablename where check_field is null or
		// cast(check_field as float) is null
		return sb.toString();
	}

	public String checkNonAmountRange(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		List<String> rules = new ArrayList<String>();
		for (int i = 0; i < qualityRules.size(); i++) {
			rules.add(qualityRules.get(i).getName());
		}
		String andOr = qualitySuite.getAndOr();
		Integer leftValue = qualitySuite.getLeftValue();
		Integer rightValue = qualitySuite.getRightValue();
		sb.append("select ");
		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}//rules规则顺序：is null ,>=,<=,as decimal,is not null
		sb.append(" from " + dbName)
				.append(".")
				.append(talbeName + " ")
				.append("where")
				.append(" " + fieldName)
				.append(" " + rules.get(0))//is null
				.append(" " + andOr + "(")//or
				.append(" CAST(" + fieldName)
				.append(" " + rules.get(3) + ")" + rules.get(4))
				.append(" and CAST(" + fieldName + " " + rules.get(3) + ")"
						+ rules.get(2) + leftValue).append(" " )
				.append("and CAST(" + fieldName + " " + rules.get(3) + ")")
				.append(" " + rules.get(1)).append(" " + rightValue + ")");
//sql:select * from bdm.tablename where check_field is null or (cast(check_field as float) is not null and cast(check_field as float) > 8.0 and cast(check_field as float) < 12.0)
		return sb.toString();
	}

	public String checkPointTimeAndDate(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		String rule1 = qualityRules.get(0).getName();//is null
		String rule2 = qualityRules.get(1).getName();//=''
		String rule3 = qualityRules.get(2).getName();//<>
		String rule4 = qualityRules.get(3).getName();//时间格式%Y-%m-%d %H:%i:%S,日期格式%Y-%m-%d
		String rule5 = qualityRules.get(4).getName();//%Y %m %d %H:%i:%S，%Y %m %d
		String rule6 = qualityRules.get(5).getName();//%Y/%m/%d %H:%i:%S，%Y/%m/%d
		String rule7 = qualityRules.get(6).getName();//%m/%d/%Y %H:%i:%S，%m/%d/%Y
		String andOr = qualitySuite.getAndOr();
		Integer leftValue = qualitySuite.getLeftValue();
		sb.append("select ");
		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}
		sb.append(" from " + dbName).append(".").append(talbeName + " ")
				.append("where").append(" " + fieldName).append(" " + rule1)
				.append(" " + andOr).append(" " + fieldName)
				.append(" " + rule2).append(" " + andOr)
				.append(" length(" + fieldName).append(") ")
				.append(" " + rule3).append(" " + leftValue)
				.append(" " + andOr).append("( from_unixtime(unix_timestamp(")
				.append(fieldName).append("),").append("'").append(rule4)
				.append("') <> ").append(fieldName).append(" and ")
				.append(" from_unixtime(unix_timestamp(").append(fieldName)
				.append("),").append("'").append(rule5).append("') <> ")
				.append(fieldName).append(" and ")
				.append(" from_unixtime(unix_timestamp(").append(fieldName)
				.append("),").append("'").append(rule6).append("') <> ")
				.append(fieldName).append(" and ")
				.append(" from_unixtime(unix_timestamp(").append(fieldName)
				.append("),").append("'").append(rule7).append("') <> ")
				.append(fieldName).append(")");
		return sb.toString();
	}

	/**
	 * 空值检查1 20190219 liu1
	 * 
	 * @param map
	 * @return ResultVO
	 */
	@Override
	public ResultVO tableCheckNull(Map<Object, Object> map) {
		Integer tableId = Integer.parseInt(map.get("tableId") + "");
		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
				+ "");
		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteId).get();
		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
		String rule1 = qualityRules.get(0).getName();
		String rule2 = qualityRules.get(1).getName();
		String andOr = qualitySuite.getAndOr();
		List<String> selectFields = (List<String>) map.get("selectFields");
		// DataSource dataSource =
		// testField.getTestTable().getTestDatabase().getDataSource();
		// String dbName = testField.getTestTable().getTestDatabase().getName();
		DataSource dataSource = testFields.get(0).getDataTable()
				.getDataSchema().getDataSource();
		String dbName = testFields.get(0).getDataTable().getDataSchema()
				.getName();
		for (DataField testField : testFields) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();
			StringBuilder sb = new StringBuilder();
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			String talbeName = testField.getTalbeName();
			sb.append("select ");
			// 选中的要查询的字段名称的集合
			for (int i = 0; i < selectFields.size(); i++) {
				// 当循环到最后一个的时候 就不添加逗号,
				if (i == selectFields.size() - 1) {
					sb.append(selectFields.get(i));
				} else {
					sb.append(selectFields.get(i));
					sb.append(",");
				}
			}
			sb.append(" from " + dbName).append(".").append(talbeName + " ")
					.append("where").append(" " + fieldName)
					.append(" " + rule1).append(" " + andOr)
					.append(" " + fieldName).append(" " + rule2);
			qualityTestQuery.setSqlText(sb.toString());
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setDataSource(dataSource);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			Object testSuiteIdobj = map.get("testSuiteId");
			this.createCase(testSuiteIdobj, qualityTestQuery, dbName,
					talbeName, fieldName, qualitySuite);
		}
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}

	/**
	 * 值域检查2 20190219
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public ResultVO tableCheckRange(Map<Object, Object> map) {
		Integer tableId = Integer.parseInt(map.get("tableId") + "");
		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
				+ "");
		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteId).get();
		;
		DataSource dataSource = testFields.get(0).getDataTable()
				.getDataSchema().getDataSource();
		String dbName = testFields.get(0).getDataTable().getDataSchema()
				.getName();
		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
		String rule1 = qualityRules.get(0).getName();
		String rule2 = qualityRules.get(1).getName();
		String rule3 = qualityRules.get(2).getName();
		String andOr = qualitySuite.getAndOr();
		Integer leftValue = qualitySuite.getLeftValue();
		Integer rightValue = qualitySuite.getRightValue();
		// 选中的要查询的字段名称的集合
		List<String> selectFields = (List<String>) map.get("selectFields");
		StringBuilder select = new StringBuilder();
		for (int i = 0; i < selectFields.size(); i++) {
			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				select.append(selectFields.get(i));
			} else {
				select.append(selectFields.get(i));
				select.append(",");
			}
		}
		for (DataField testField : testFields) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();
			StringBuilder sb = new StringBuilder();
			String dataType = testField.getDataType();
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			// 配置检查点
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			String talbeName = testField.getTalbeName();
			sb.append("select ");
			sb.append(select);
			if (dataType.equals("vachar") || dataType.equals("char")) {
				sb.append(" from " + dbName).append(".")
						.append(talbeName + " ").append("where")
						.append(" " + fieldName).append(" " + rule1)
						.append(" " + andOr).append(" " + fieldName)
						.append(" " + rule2).append(" '" + rightValue)
						.append("' " + andOr).append(" " + fieldName)
						.append(" " + rule3).append(" '" + leftValue)
						.append("'");
			} else {
				sb.append(" from " + dbName).append(".")
						.append(talbeName + " ").append("where")
						.append(" " + fieldName).append(" " + rule1)
						.append(" " + andOr).append(" " + fieldName)
						.append(" " + rule2).append(" " + rightValue)
						.append(" " + andOr).append(" " + fieldName)
						.append(" " + rule3).append(" " + leftValue);
			}
			qualityTestQuery.setSqlText(sb.toString());
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setDataSource(dataSource);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			Integer testSuiteId = Integer.parseInt(map.get("testSuiteId") + "");
			/* TestSuite testSuite = testSuiteService.getOneById(testSuiteId); */
			TestSuite testSuite = testSuiteService.findById(testSuiteId);
			QualityTestCase qualityTestCase = new QualityTestCase();
			qualityTestCase.setQualityTestQuery(qualityTestQuery);
			// qualityTestCase.setTestSuite(testSuite);
			qualityTestCase.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName() + "的测试案例");
			IQualityTestCaseService.save(qualityTestCase);
		}
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}

	/**
	 * 字段长度检查3
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public ResultVO tableCheckLength(Map<Object, Object> map) {
		Integer tableId = Integer.parseInt(map.get("tableId") + "");
		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
				+ "");
		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteId).get();
		;
		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
		String rule1 = qualityRules.get(0).getName();
		String rule2 = qualityRules.get(1).getName();
		String rule3 = qualityRules.get(2).getName();
		String andOr = qualitySuite.getAndOr();
		Integer leftValue = qualitySuite.getLeftValue();
		Integer rightValue = qualitySuite.getRightValue();
		DataSource dataSource = testFields.get(0).getDataTable()
				.getDataSchema().getDataSource();
		String dbName = testFields.get(0).getDataTable().getDataSchema()
				.getName();
		List<String> selectFields = (List<String>) map.get("selectFields");
		StringBuilder select = new StringBuilder();
		for (int i = 0; i < selectFields.size(); i++) {
			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				select.append(selectFields.get(i));
			} else {
				select.append(selectFields.get(i));
				select.append(",");
			}
		}
		for (DataField testField : testFields) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();
			StringBuilder sb = new StringBuilder();
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			// 配置检查点
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			String talbeName = testField.getTalbeName();
			sb.append("select ");
			sb.append(select);
			sb.append(" from " + dbName).append(".").append(talbeName + " ")
					.append("where").append(" " + fieldName)
					.append(" " + rule1).append(" " + andOr)
					.append(" length(" + fieldName + ")").append(" " + rule2)
					.append(" " + rightValue).append(" " + andOr)
					.append(" length(" + fieldName + ")").append(" " + rule3)
					.append(" " + leftValue);
			qualityTestQuery.setSqlText(sb.toString());
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setDataSource(dataSource);
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			Object testSuiteIdobj = map.get("testSuiteId");
			this.createCase(testSuiteIdobj, qualityTestQuery, dbName,
					talbeName, fieldName, qualitySuite);
		}
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}

	/**
	 * 字段唯一性检查4 20190219
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public ResultVO tableCheckUnique(Map<Object, Object> map) {
		Integer tableId = Integer.parseInt(map.get("tableId") + "");
		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
				+ "");
		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteId).get();
		;
		DataSource dataSource = testFields.get(0).getDataTable()
				.getDataSchema().getDataSource();
		String dbName = testFields.get(0).getDataTable().getDataSchema()
				.getName();
		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
		String rule = qualityRules.get(0).getName();
		List<String> selectFields = (List<String>) map.get("selectFields");
		StringBuilder select = new StringBuilder();
		for (int i = 0; i < selectFields.size(); i++) {
			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				select.append(selectFields.get(i));
			} else {
				select.append(selectFields.get(i));
				select.append(",");
			}
		}
		for (DataField testField : testFields) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();
			StringBuilder sb = new StringBuilder();
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			// 配置检查点
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			String talbeName = testField.getTalbeName();
			sb.append("select ");
			sb.append(select);
			sb.append(" from " + dbName).append(".").append(talbeName + " ")
					.append("group by").append(" concat(" + fieldName + ")")
					.append(" " + rule);
			qualityTestQuery.setSqlText(sb.toString());
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setDataSource(dataSource);
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			Object testSuiteIdobj = map.get("testSuiteId");
			this.createCase(testSuiteIdobj, qualityTestQuery, dbName,
					talbeName, fieldName, qualitySuite);
		}
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}

	/**
	 * 码值范围检查 5 20190219
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public ResultVO tableCheckNotIn(Map<Object, Object> map) {
		Integer tableId = Integer.parseInt(map.get("tableId") + "");
		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
				+ "");
		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteId).get();
		;
		String talbeName = testFields.get(0).getTalbeName();
		DataSource dataSource = testFields.get(0).getDataTable()
				.getDataSchema().getDataSource();
		String dbName = testFields.get(0).getDataTable().getDataSchema()
				.getName();
		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
		String rule1 = qualityRules.get(0).getName();
		String rule2 = qualityRules.get(1).getName();
		// 不在码值范围内 not in暂时写死
		String andOr = qualitySuite.getAndOr();
		// 选中的要查询的字段名称的集合
		List<String> selectFields = (List<String>) map.get("selectFields");
		StringBuilder select = new StringBuilder();
		for (int i = 0; i < selectFields.size(); i++) {
			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				select.append(selectFields.get(i));
			} else {
				select.append(selectFields.get(i));
				select.append(",");
			}
		}
		for (DataField testField : testFields) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();
			StringBuilder sb = new StringBuilder();
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			// 配置检查点
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			sb.append("select ");
			sb.append(select);
			sb.append(" from " + dbName).append(".").append(talbeName + " ")
					.append("where").append(" " + fieldName)
					.append(" " + rule1).append(" " + andOr)
					.append(" " + fieldName).append(" " + rule2).append(" (")
					.append(32 + "," + "fdsh" + "," + 56 + "," + "")
					.append(")");
			qualityTestQuery.setSqlText(sb.toString());
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setDataSource(dataSource);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			Object testSuiteIdobj = map.get("testSuiteId");
			this.createCase(testSuiteIdobj, qualityTestQuery, dbName,
					talbeName, fieldName, qualitySuite);
		}
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}

	/**
	 * 筛选非浮点数的记录6 20190219
	 * 
	 * @param map
	 * @return ResultVO
	 */
	@Override
	public ResultVO tableCheckNotF(Map<Object, Object> map) {
		Integer tableId = Integer.parseInt(map.get("tableId") + "");
		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
				+ "");
		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteId).get();
		;
		String talbeName = testFields.get(0).getTalbeName();
		DataSource dataSource = testFields.get(0).getDataTable()
				.getDataSchema().getDataSource();
		String dbName = testFields.get(0).getDataTable().getDataSchema()
				.getName();
		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
		String rule1 = qualityRules.get(0).getName();
		String rule2 = qualityRules.get(1).getName();
		String andOr = qualitySuite.getAndOr();
		List<String> selectFields = (List<String>) map.get("selectFields");
		StringBuilder select = new StringBuilder();
		for (int i = 0; i < selectFields.size(); i++) {
			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				select.append(selectFields.get(i));
			} else {
				select.append(selectFields.get(i));
				select.append(",");
			}
		}
		for (DataField testField : testFields) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();
			StringBuilder sb = new StringBuilder();
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			// 配置检查点
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			sb.append("select ");
			sb.append(select);
			sb.append(" from " + dbName).append(".").append(talbeName + " ")
					.append("where").append(" " + fieldName)
					.append(" " + rule1).append(" " + andOr)
					.append(" CAST(" + fieldName).append(" " + rule2 + ")")
					.append(" " + rule1);
			qualityTestQuery.setSqlText(sb.toString());
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setDataSource(dataSource);
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			Object testSuiteIdobj = map.get("testSuiteId");
			this.createCase(testSuiteIdobj, qualityTestQuery, dbName,
					talbeName, fieldName, qualitySuite);
		}
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}

	/**
	 * 筛选非金额范围的记录7 20190219
	 * 
	 * @param map
	 * @return
	 */
	@Override
	public ResultVO tableCheckNonAmountRange(Map<Object, Object> map) {

		Integer tableId = Integer.parseInt(map.get("tableId") + "");
		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
				+ "");
		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteId).get();
		;
		String talbeName = testFields.get(0).getTalbeName();
		DataSource dataSource = testFields.get(0).getDataTable()
				.getDataSchema().getDataSource();
		String dbName = testFields.get(0).getDataTable().getDataSchema()
				.getName();
		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
		List<String> rules = new ArrayList<String>();
		for (int i = 0; i < qualityRules.size(); i++) {
			rules.add(qualityRules.get(i).getName());
		}
		String andOr = qualitySuite.getAndOr();
		Integer leftValue = qualitySuite.getLeftValue();
		Integer rightValue = qualitySuite.getRightValue();
		StringBuilder select = new StringBuilder();
		// 选中的要查询的字段名称的集合
		List<String> selectFields = (List<String>) map.get("selectFields");
		for (int i = 0; i < selectFields.size(); i++) {
			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				select.append(selectFields.get(i));
			} else {
				select.append(selectFields.get(i));
				select.append(",");
			}
		}
		for (DataField testField : testFields) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();
			StringBuilder sb = new StringBuilder();
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			// 配置检查点
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			sb.append("select ");
			sb.append(" from " + dbName)
					.append(".")
					.append(talbeName + " ")
					.append("where")
					.append(" " + fieldName)
					.append(" " + rules.get(0))
					.append(" " + andOr + "(")
					.append(" CAST(" + fieldName)
					.append(" " + rules.get(3) + ")" + rules.get(4))
					.append(" and CAST(" + fieldName + " " + rules.get(3) + ")"
							+ rules.get(2) + leftValue).append(" " + andOr)
					.append(" CAST(" + fieldName + " " + rules.get(3) + ")")
					.append(" " + rules.get(1)).append(" " + rightValue + ")");

			qualityTestQuery.setSqlText(sb.toString());
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setDataSource(dataSource);
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			Object testSuiteIdobj = map.get("testSuiteId");
			this.createCase(testSuiteIdobj, qualityTestQuery, dbName,
					talbeName, fieldName, qualitySuite);
		}

		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");

	}

	/*
	 * 判断a+b！=c c为入参，ab为规则集里的leftvalue,rightvalue
	 */
	public String checkSum(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		List<String> rules = new ArrayList<String>();
		for (int i = 0; i < qualityRules.size(); i++) {
			rules.add(qualityRules.get(i).getName());
		}
		Integer leftValue = qualitySuite.getLeftValue();// a
		Integer rightValue = qualitySuite.getRightValue();// b
		sb.append("select ");
		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}
		sb.append(" from " + dbName).append(".").append(talbeName + " ")
				.append("where ").append(leftValue + "+" + rightValue)
				.append(rules.get(0)).append(fieldName);

		return sb.toString();
	}

	/*
	 * 表间关系检查点配置 判断表1的外键字段和表2的主键字段不包含。
	 */
	public String checkForeignkey(List<QualityRule> qualityRules,
			QualitySuite qualitySuite, String dbName, String talbeName,
			String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		List<String> rules = new ArrayList<String>();
		for (int i = 0; i < qualityRules.size(); i++) {
			rules.add(qualityRules.get(i).getName());
		}
		sb.append("select ");
		for (int i = 0; i < selectFields.size(); i++) {

			// 当循环到最后一个的时候 就不添加逗号,
			if (i == selectFields.size() - 1) {
				sb.append(selectFields.get(i));
			} else {
				sb.append(selectFields.get(i));
				sb.append(",");
			}
		}
		sb.append(" from " + dbName)
				.append(".")
				.append(talbeName + " ")
				.append("where ")
				.append(rules.get(0))
				.append("( select * from " + dbName + "."
						+ fieldName.split("_id")[0] + " where ")
				.append(dbName + "." + talbeName + "." + fieldName + "="
						+ dbName + "." + fieldName.split("_id")[0] + "."
						+ "id)");

		return sb.toString();
	}

	/**
	 * 新增/配置检查点(根据多个或一个字段，一个规则集，产生多个或一个检查点) 并自动生成sql脚本， 自动生成测试案例，测试任务，
	 * 
	 * @param map
	 *            List<Integer> testfieldIds, List<Integer>qualitySuiteIds,
	 *            String ruleNum, List<String> selectFieldNames, Integer
	 *            testSuiteId
	 * @return
	 */
	@Override
	public ResultVO checkPoint(Map<Object, Object> map) {
		List<Integer> testfieldIds = (List<Integer>) map.get("testfieldIds");// 多个字段id
		List<Integer> qualitySuiteIds = (List<Integer>) map
				.get("qualitySuiteIds");// 多个规则集
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteIds.get(0)).get();

		String ruleNum = (String) map.get("ruleNum");
		for (Integer fieldId : testfieldIds) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();

			/* TestField testField = testFieldService.getOneById(fieldId); */
			DataField testField = testFieldService.findById(fieldId);
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			// 配置检查点
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			String talbeName = testField.getTalbeName();
			DataSource dataSource = testField.getDataTable().getDataSchema()
					.getDataSource();
			String dbName = testField.getDataTable().getDataSchema().getName();
			List<QualityRule> qualityRules = qualitySuite.getQualityRules();
			List<String> selectFields = (List<String>) map
					.get("selectFieldNames");
			String sb = null;
			// rule规则
			switch (ruleNum) {
			case CHECK_NULL:
				sb = checkNull(qualityRules, qualitySuite, dbName, talbeName,
						fieldName, selectFields);
				break;
			case CHECK_RANGE:
				String dataType = testField.getDataType();
				sb = checkRange(qualityRules, qualitySuite, dbName, talbeName,
						fieldName, selectFields, dataType);
				break;
			case CHECK_LENGTH:
				sb = checkLength(qualityRules, qualitySuite, dbName, talbeName,
						fieldName, selectFields);
				break;
			case CHECK_UNIQUE:
				sb = checkUnique(qualityRules, qualitySuite, dbName, talbeName,
						fieldName, selectFields);
				break;
			case CHECK_NOTIN:
				sb = checkNotIn(qualityRules, qualitySuite, dbName, talbeName,
						fieldName, selectFields);
				break;
			case CHECK_NOTFLOAT:
				sb = checkNotFloat(qualityRules, qualitySuite, dbName,
						talbeName, fieldName, selectFields);
				break;
			case CHECK_NON_AMOUNT_RANGE:
				sb = checkNonAmountRange(qualityRules, qualitySuite, dbName,
						talbeName, fieldName, selectFields);
				break;
			case CHECK_POINT_TIME_AND_DATE:
				sb = checkPointTimeAndDate(qualityRules, qualitySuite, dbName,
						talbeName, fieldName, selectFields);
				break;
			case CHECK_SUM:
				sb = checkSum(qualityRules, qualitySuite, dbName, talbeName,
						fieldName, selectFields);
				break;
			case CHECK_FOREIGN_KEY:
				sb = checkForeignkey(qualityRules, qualitySuite, dbName,
						talbeName, fieldName, selectFields);
				break;
			}

			qualityTestQuery.setSqlText(sb);
			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
			qualityTestQuery.setDataSource(dataSource);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			Object testSuiteIdobj = map.get("testSuiteId");
			this.createCase(testSuiteIdobj, qualityTestQuery, dbName,
					talbeName, fieldName, qualitySuite);
		}
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}

	@Override
	@Transactional
	public ResultVO batchCreateQualityCase(Map<Object, Object> map) throws Exception{
		Integer dataSourceID = Integer.parseInt(map.get("dataSourceID") + "");//数据源ID
		Integer schemaID = Integer.parseInt(map.get("schemaID") + "");//schemaID
		DataSchema dataSchema = testDatabaseService.findSchemaByID(schemaID);//schema
		Integer tableId = Integer.parseInt(map.get("tableId")+"");//数据库表ID
		List<String> selectFields = (List<String>) map.get("selectFields");
		for(String fiedlStr:selectFields){
			String field = fiedlStr.split("___")[0];//字段名
			Integer qualitySuiteId = Integer.parseInt(fiedlStr.split("___")[1]);//规则集
			QualitySuite qualitySuite = qualitySuiteRepository.findById(qualitySuiteId).get();
			List<QualityRule> qualityRules = qualitySuite.getQualityRules();
			for(QualityRule qr:qualityRules){
				
			}
		}
		//DataSource dataSource = testFields.get(0).getDataTable().getDataSchema().getDataSource();
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}
	
//	@Override
//	public ResultVO tableCheckNull(Map<Object, Object> map) {
//		Integer tableId = Integer.parseInt(map.get("tableId") + "");
//		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
//				+ "");
//		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
//		QualitySuite qualitySuite = qualitySuiteRepository.findById(
//				qualitySuiteId).get();
//		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
//		String rule1 = qualityRules.get(0).getName();
//		String rule2 = qualityRules.get(1).getName();
//		String andOr = qualitySuite.getAndOr();
//		List<String> selectFields = (List<String>) map.get("selectFields");
//		// DataSource dataSource =
//		// testField.getTestTable().getTestDatabase().getDataSource();
//		// String dbName = testField.getTestTable().getTestDatabase().getName();
//		DataSource dataSource = testFields.get(0).getDataTable()
//				.getDataSchema().getDataSource();
//		String dbName = testFields.get(0).getDataTable().getDataSchema()
//				.getName();
//		for (DataField testField : testFields) {
//			QualityTestPoint qualityTestPoint = new QualityTestPoint();
//			QualityTestQuery qualityTestQuery = new QualityTestQuery();
//			StringBuilder sb = new StringBuilder();
//			qualityTestPoint.setDataField(testField);
//			qualityTestPoint.setQualitySuite(qualitySuite);
//			qualityTestPointRepository.save(qualityTestPoint);
//			String fieldName = testField.getName();
//			String talbeName = testField.getTalbeName();
//			sb.append("select ");
//			// 选中的要查询的字段名称的集合
//			for (int i = 0; i < selectFields.size(); i++) {
//				// 当循环到最后一个的时候 就不添加逗号,
//				if (i == selectFields.size() - 1) {
//					sb.append(selectFields.get(i));
//				} else {
//					sb.append(selectFields.get(i));
//					sb.append(",");
//				}
//			}
//			sb.append(" from " + dbName).append(".").append(talbeName + " ")
//					.append("where").append(" " + fieldName)
//					.append(" " + rule1).append(" " + andOr)
//					.append(" " + fieldName).append(" " + rule2);
//			qualityTestQuery.setSqlText(sb.toString());
//			// qualityTestQuery.setQualityTestPoint(qualityTestPoint);
//			qualityTestQuery.setDataSource(dataSource);
//			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
//					+ fieldName + "字段" + qualitySuite.getName());
//			// 自动生成脚本
//			qualityTestQueryService.save(qualityTestQuery);
//			// 自动生成案例
//			Object testSuiteIdobj = map.get("testSuiteId");
//			this.createCase(testSuiteIdobj, qualityTestQuery, dbName,
//					talbeName, fieldName, qualitySuite);
//		}
//		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
//	}
}
