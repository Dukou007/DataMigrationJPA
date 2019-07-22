package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.transaction.Transactional;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jettech.entity.DataField;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataSource;
import com.jettech.entity.DataTable;
import com.jettech.entity.QualityRule;
import com.jettech.entity.QualitySuite;
import com.jettech.entity.QualityTestCase;
import com.jettech.entity.QualityTestPoint;
import com.jettech.entity.QualityTestQuery;
import com.jettech.entity.TestSuite;
import com.jettech.repostory.QualitySuiteRepository;
import com.jettech.repostory.QualityTestPointRepository;
import com.jettech.service.DataSchemaService;
import com.jettech.service.IDataSourceService;
import com.jettech.service.IQualityTestCaseService;
import com.jettech.service.IQualityTestPointService;
import com.jettech.service.IQualityTestQueryService;
import com.jettech.service.ITestFieldService;
import com.jettech.service.ITestTableService;
import com.jettech.service.TestSuiteService;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

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
	
	@Autowired
	private ITestTableService dataTableService;
	
	@Autowired
	private IDataSourceService dataSourceService;

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

//	@Override
//	@Transactional
//	public ResultVO batchCreateQualityCase(Map<Object, Object> map) throws Exception{
//		log.info("进入批量生成案例主流程。。。。。");
//		List<String> selectFields = (List<String>) map.get("selectFields");
//		Integer dataSourceID = Integer.parseInt(map.get("dataSourceID") + "");
//		DataSource ds = dataSourceService.findById(dataSourceID);//数据源
//		Integer schemaID = Integer.parseInt(map.get("schemaID") + "");
//		DataSchema dataSchema = testDatabaseService.findSchemaByID(schemaID);//schema
//		Integer tableId = Integer.parseInt(map.get("tableId")+"");
//		DataTable dt = dataTableService.findById(tableId);//数据库表
//		String dbName = dataSchema.getName();
//		String talbeName = dt.getName();
//		
//		for(String fieldStr:selectFields){
//			QualityTestQuery qualityTestQuery = new QualityTestQuery();
//			String fieldID = fieldStr.split("___")[0];
//			DataField df = testFieldService.findById(Integer.valueOf(fieldID));//字段
//			String fieldName = df.getName();//字段名
//			
//			Integer qualitySuiteId = Integer.parseInt(fieldStr.split("___")[1]);
//			QualitySuite qualitySuite = qualitySuiteRepository.findById(qualitySuiteId).get();//规则集
//			String andOr = qualitySuite.getAndOr();//规则及下sql拼接符and/or
//			List<QualityRule> qualityRules = qualitySuite.getQualityRules();//获取规则集下所有规则
//			
//			QualityTestPoint qualityTestPoint = new QualityTestPoint();
//			qualityTestPoint.setDataField(df);
//			qualityTestPoint.setQualitySuite(qualitySuite);
//			qualityTestPointRepository.save(qualityTestPoint);//生成检查点
//			StringBuilder sb = new StringBuilder();
//			
//			String dataType = df.getDataType();
//			Integer leftValue = qualitySuite.getLeftValue();
//			Integer rightValue = qualitySuite.getRightValue();
//			
//			StringBuilder lengthStr = new StringBuilder();
//			lengthStr.append("select ");
//			if (dataType.equals("vachar") || dataType.equals("char")) {
//				lengthStr.append(" from " + dbName).append(".")
//						.append(talbeName + " ").append("where")
//						.append(" " + fieldName).append(" " + qualityRules.get(0))
//						.append(" " + andOr).append(" " + fieldName)
//						.append(" " + qualityRules.get(1)).append(" '" + rightValue)
//						.append("' " + andOr).append(" " + fieldName)
//						.append(" " + qualityRules.get(2)).append(" '" + leftValue)
//						.append("'");
//			} else {
//				lengthStr.append(" from " + dbName).append(".")
//						.append(talbeName + " ").append("where")
//						.append(" " + fieldName).append(" " + qualityRules.get(0))
//						.append(" " + andOr).append(" " + fieldName)
//						.append(" " + qualityRules.get(1)).append(" " + rightValue)
//						.append(" " + andOr).append(" " + fieldName)
//						.append(" " + qualityRules.get(2)).append(" " + leftValue);
//			}
//			
//			log.info("从前端获取到的规则集==================="+qualitySuiteId);
//			switch(qualitySuiteId){
//				case 1://空值检查
//					sb.append("select ").append(fieldName);
//					sb.append(" from " + dbName).append(".").append(talbeName + " ")
//							.append("where").append(" " + fieldName)
//							.append(" " + qualityRules.get(0)).append(" " + andOr)//is null
//							.append(" " + fieldName).append(" " + qualityRules.get(1));//=''
//					break;
//				case 2://值域或字段长度检测左闭右闭>=or<=
//					sb = lengthStr;
//					break;
//				case 3://值域或字段长度检测左闭右开>=or<
//					sb = lengthStr;
//					break;
//				case 4://值域或字段长度检测左开右闭>or<=
//					sb = lengthStr;
//					break;
//				case 5://值域或字段长度检测左开右开>or<
//					sb = lengthStr;
//					break;
//	//				this.tableCheckNull(map);
//				case 7://唯一值检查
//					sb.append("select ");
//					sb.append(" from " + dbName).append(".").append(talbeName + " ")
//							.append("group by").append(" concat(" + fieldName + ")")
//							.append(" " + qualityRules.get(0));
//					break;
//				case 8:
//	//				this.tableCheckNull(map);
//				case 9://码值范围检查
//					sb.append("select ");
//					sb.append(" from " + dbName).append(".").append(talbeName + " ")
//							.append("where").append(" " + fieldName)
//							.append(" " + qualityRules.get(0)).append(" " + andOr)
//							.append(" " + fieldName).append(" " + qualityRules.get(1)).append(" (")
//							.append(32 + "," + "fdsh" + "," + 56 + "," + "")
//							.append(")");
//					//this.tableCheckNotIn(map);
//					break;
//				case 10://非浮点数检查
//					sb.append("select ");
//					sb.append(" from " + dbName).append(".").append(talbeName + " ")
//							.append("where").append(" " + fieldName)
//							.append(" " +  qualityRules.get(0)).append(" " + andOr)
//							.append(" CAST(" + fieldName).append(" " +  qualityRules.get(1) + ")")
//							.append(" " +  qualityRules.get(0));
//					//this.tableCheckNotF(map);
//					break;
//				case 11://非金额检查
//					sb.append("select ");
//					sb.append(" from " + dbName)
//							.append(".")
//							.append(talbeName + " ")
//							.append("where")
//							.append(" " + fieldName)
//							.append(" " + qualityRules.get(0))
//							.append(" " + andOr + "(")
//							.append(" CAST(" + fieldName)
//							.append(" " + qualityRules.get(3) + ")" + qualityRules.get(4))
//							.append(" and CAST(" + fieldName + " " + qualityRules.get(3) + ")"
//									+ qualityRules.get(2) + leftValue).append(" " + andOr)
//							.append(" CAST(" + fieldName + " " + qualityRules.get(3) + ")")
//							.append(" " + qualityRules.get(1)).append(" " + rightValue + ")");
//					//this.tableCheckNonAmountRange(map);
//					break;
//				case 12://日期格式不正确
//	//				this.tableCheckNull(map);
//				case 13://时间格式不正确
//	//				this.tableCheckNull(map);
//				case 14://默认值
//					sb.append("select * from "+dbName+"."+talbeName+" where "+fieldName+"="+qualitySuite.getDefaultValue());
//					break;
//				case 15://检查float小数点位数如> < = <>
//					sb.append("select * from "+dbName+"."+talbeName+" where LENGTH(SUBSTRING_INDEX("+fieldName+",'.' ,- 1)) "+qualityRules.get(0)+" "+qualitySuite.getFloatFormat());
//					break;
//			}
//			System.out.println("组装后的SQL=======================:"+sb.toString());
//			log.info("组装后的SQL=======================:"+sb.toString());
//			qualityTestQuery.setSqlText(sb.toString());
//			qualityTestQuery.setDataSource(ds);
//			qualityTestQuery.setName("数据源->"+ds.getName()+"schema->"+dataSchema.getName()+"表->"+dt.getName()+"字段->"+df.getName()+"规则集->"+qualitySuite.getName()+",sql脚本生成");
//			qualityTestQueryService.save(qualityTestQuery);//生成SQL脚本
//			
//			QualityTestCase qualityTestCase = new QualityTestCase();
//			qualityTestCase.setQualityTestQuery(qualityTestQuery);
//			qualityTestCase.setName("批量生成数据源->"+ds.getName()+"schema->"+dataSchema.getName()+"表->"+dt.getName()+"字段->"+df.getName()+"规则集->"+qualitySuite.getName()+"测试案例");
//			qualityTestCase.setVersion("1.0");
//			qualityTestCase.setIsSQLCase(true);
//			qualityTestCase.setUsePage(false);
//			IQualityTestCaseService.save(qualityTestCase);//生成测试案例
//			
//			int qtcID = qualityTestCase.getId();
//			String num = "";
//			if(qtcID > 0){
//				if(String.valueOf(qtcID).length() == 1){
//					num = "000"+qtcID;
//				}else if(String.valueOf(qtcID).length() == 2){
//					num = "00"+qtcID;
//				}else if(String.valueOf(qtcID).length() == 3){
//					num = "0"+qtcID;
//				}else if(String.valueOf(qtcID).length() == 4){
//					num = qtcID+"";
//				}
//			}
//			//更新案例编号
//			qualityTestCase.setCaseCode("SIT-SJJX-"+dt.getName()+"-"+num);
//			IQualityTestCaseService.save(qualityTestCase);
//			log.info("批量生成SQL案例完成。。。。。。");
//		}
//		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
//	}
	
	@Override
	@Transactional
	public ResultVO batchCreateQualityCase(Map<Object, Object> map) throws Exception{
		log.info("进入批量生成案例主流程。。。。。");
		Integer dataSourceID = Integer.parseInt(map.get("dataSourceID") + "");
		DataSource ds = dataSourceService.findById(dataSourceID);//数据源
		Integer schemaID = Integer.parseInt(map.get("schemaID") + "");
		DataSchema dataSchema = testDatabaseService.findSchemaByID(schemaID);//schema
		Integer tableId = Integer.parseInt(map.get("tableId")+"");
		DataTable dt = dataTableService.findById(tableId);//数据库表
		String dbName = dataSchema.getName();
		String talbeName = dt.getName();
		List<String> selectFields = (List<String>) map.get("selectFields");
		for(String fieldStrs:selectFields){
			String fields[] = fieldStrs.split(",");
			for(String fieldStr:fields){
				QualityTestQuery qualityTestQuery = new QualityTestQuery();
				String fieldID = fieldStr.split("___")[0].trim();
				DataField df = testFieldService.findById(Integer.valueOf(fieldID));//字段
				String fieldName = df.getName();//字段名

				//值为空时跳出
				Integer qualitySuiteId = 0;
				try {
					qualitySuiteId = Integer.parseInt(fieldStr.split("___")[1]);
				}catch (Exception e){
					log.info("没有选择规则集"+e);
					continue;
				}
				QualitySuite qualitySuite = qualitySuiteRepository.findById(qualitySuiteId).get();//规则集
	//			String andOr = qualitySuite.getAndOr();//规则及下sql拼接符and/or
				List<QualityRule> qualityRules = qualitySuite.getQualityRules();//获取规则集下所有规则
				
				QualityTestPoint qualityTestPoint = new QualityTestPoint();
				qualityTestPoint.setDataField(df);
				qualityTestPoint.setQualitySuite(qualitySuite);
				qualityTestPointRepository.save(qualityTestPoint);//生成检查点
	//			StringBuilder sb = new StringBuilder();
				String sqlStr = "SELECT * FROM "+dbName+"."+talbeName;
				String whereSql = " WHERE ";
				log.info("从前端获取到的字段："+fieldName+",规则集："+qualitySuiteId);
				//浮点数选择SQL
				String floatSql = " where LOCATE('.'," + fieldName  +") > 0  " ;
                //日期格式的SQL    CHECK_POINT_TIME_AND_DATE
                String checkPointDateSql = "where ";
				String andOr = "or";

				for(QualityRule qr:qualityRules){
					String ruleStr = qr.getName();
					String whereNullSql = (StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+" "+fieldName+" "+ruleStr+" ";
					String whereRangeSql = (StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+" "+fieldName+" "+ruleStr+" "+qr.getDefaultValue();
					String whereLengthSql = (StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+" "+"LENGTH("+fieldName+") "+ruleStr+" "+qr.getDefaultValue();
					String whereCodeInOrNotSql = (StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+" "+fieldName+" "+ruleStr+" "+" ("+qr.getDefaultValue()+")";

					switch(qr.getRuleType().getCode()){
						case "_IS_NULL"://空值 is null
							whereSql += " "+whereNullSql;
							break;
						case "_IS_NULL_STR"://空字符串 =''
							whereSql += " "+whereNullSql;
							break;
						case "_RANGE_GREAT"://范围大于>
							whereSql += " "+whereRangeSql;
							break;
						case "_RANGE_GREAT_EQUALS"://范围大于等于>=
							whereSql += " "+whereRangeSql;
							break;
						case "_RANGE_SMALL"://范围小于<
							whereSql += " "+whereRangeSql;
							break;
						case "_RANGE_SMALL_EQUALS"://范围小于等于<=
							whereSql += " "+whereRangeSql;
							break;
						case "_RANGE_EQUALS"://范围等于=
							whereSql += " "+whereRangeSql;
							break;
						case "_LENGTH_GREAT"://长度大于>
							whereSql += " "+whereLengthSql;
							break;
						case "_LENGTH_GREAT_EQUALS"://长度大于等于>=
							whereSql += " "+whereLengthSql;
							break;
						case "_LENGTH_SMALL"://长度小于<
							whereSql += " "+whereLengthSql;
							break;
						case "_LENGTH_SMALL_EQUALS"://长度小于等于<=
							whereSql += " "+whereLengthSql;
							break;
						case "_LENGTH_EQUALS"://长度等于=
							whereSql += " "+whereLengthSql;
							break;
						case "_LENGTH_NOT_EQUALS": //长度不等于 //
							whereSql +=whereLengthSql;
							break;
						case "_CODE_IN"://码值包含in
							whereSql += " "+whereCodeInOrNotSql;
							break;
						case "_CODE_NOTIN"://码值不包含not in
							whereSql += " "+whereCodeInOrNotSql;
							break;
						case "_NOT_EQUALS"://不等于<>
							String whereNotEqualsSql = (StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+" "+fieldName+" "+ruleStr+" "+" ("+qr.getDefaultValue()+")";
							whereSql += " "+whereNotEqualsSql;
							break;
						case "_DEAULT_VALUE"://默认值=
							String whereDefaultValueSql = (StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+" "+fieldName+" "+ruleStr+" "+qr.getDefaultValue();
							whereSql += " "+whereDefaultValueSql;
							break;
						case "_NOT_FLOAT"://非浮点数
							String whereNotFloatSql = (StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+" "+"CAST("+fieldName+"AS FLOAT) "+ruleStr;
							whereSql += " "+whereNotFloatSql;
							break;
						case "_IN_TALBE_COMPARE"://表内比较 如 a>b
							String whereInTableCompareSql = (StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+" "+fieldName+" "+ruleStr+" "+qr.getDefaultValue();
							whereSql += " "+whereInTableCompareSql;
							break;
						case "_CHAIN_RUPTURE"://数仓拉链表断链
							//select * from biao a left join biao b on a.name=b.name and a.end_dt=b.start_dt
							//sqlStr = "SELECT * FROM "+dbName+"."+talbeName+" A LEFT JOIN "+dbName+"."+talbeName+" B ON A.ID=B.ID AND A."+fieldName+" "+ruleStr+" "+qr.getDefaultValue();
							String strQr[] = qr.getDefaultValue().split("/");

							sqlStr = "SELECT * FROM "+dbName+"."+talbeName+" A LEFT JOIN "+dbName+"."+talbeName+" B ON A."+ strQr[0] +" = B."+ strQr[0] +" AND A."+fieldName+" "+ruleStr+" "+strQr[1];
							whereSql = "";
							break;
						case "_IS_PRIMARY_KEY"://是否主键查询
				//			sqlStr = "select * from \"" + dbName + "\"." + talbeName;
				//			whereSql += "\"" + fieldName + "\" in ( select \"" +
				//					fieldName + "\" from \"" + dbName + "\"." + talbeName + " GROUP BY \"" +
				//					fieldName + "\" HAVING COUNT (\"" + fieldName + "\") " + ruleStr + " " + qr.getDefaultValue() + " AND \"" + fieldName + "\" IS NOT NULL )";
							sqlStr = "select * from " + dbName + "." + talbeName;
							whereSql += " " + fieldName + " in ( select " +
									fieldName + " from " + dbName + "." + talbeName + " GROUP BY " +
									fieldName + " HAVING COUNT (" + fieldName + ") " + ruleStr + " " + qr.getDefaultValue() + " AND " + fieldName + " IS NOT NULL )";

						//	sqlStr = "select column_name from INFORMATION_SCHEMA.`KEY_COLUMN_USAGE` where ";
					//		whereSql = "table_name = "+ "'"+talbeName+"'"+" and TABLE_SCHEMA = " + "'"+dbName + "'"+" AND constraint_name = 'PRIMARY' "+"and column_name = " + "'"+fieldName + "'";
							break;
						case "_CHECK_FLOAT_LENGTH": //浮点范围长度选择
						//	floatSql =  " where LOCATE('.'," + fieldName  +") "+ ruleStr + " " + qr.getDefaultValue() ;
						//	whereSql =  floatSql;
							/*String strs[] = qr.getDefaultValue().split("/");
							sqlStr = "SELECT * FROM \""+dbName+"\"."+talbeName;
							whereSql = " where (LENGTH (SUBSTR ( \"" + fieldName + "\",(INSTR( \"" + fieldName + "\",'.') + 1 ) ) ) + INSTR(\"" + fieldName + "\",'.')) = " + strs[0] + " AND LENGTH (" +
									"SUBSTR (\"" + fieldName +"\",(INSTR( \"" + fieldName + "\",'.') + 1) )) = " + strs[1];*/
							String strs[] = qr.getDefaultValue().split("/");
							sqlStr = "SELECT * FROM "+dbName+"."+talbeName;
					//		whereSql = " where (LENGTH (SUBSTR ( " + fieldName + ",(INSTR( " + fieldName + ",'.') + 1 ) ) ) + INSTR(" + fieldName + ",'.')) = " + strs[0] + " AND LENGTH (" +
					//				"SUBSTR (" + fieldName +",(INSTR( " + fieldName + ",'.') + 1) )) = " + strs[1];
							whereSql = " WHERE ( ( select DATA_SCALE from (select a.owner r0,a.table_name r1,a.column_name r2,a.comments r3" +
									" from all_col_comments a),(select t.owner r4,t.table_name r5,t.column_name  r6 ,t.* from all_tab_columns t) " +
									" where r4=r0 and r5=r1 and r6=r2 and owner = '" + dbName + "' and TABLE_NAME= '" + talbeName + "' AND r2 = '"+ fieldName +"') " +
									" + LENGTH(to_char("+ fieldName +",'FM9999999999999999999999999')) ) = " + strs[0] + " " +
									"AND (select DATA_SCALE from (select a.owner r0,a.table_name r1,a.column_name r2,a.comments r3" +
									" from all_col_comments a),(select t.owner r4,t.table_name r5,t.column_name  r6 ,t.* from all_tab_columns t)" +
									" where r4=r0 and r5=r1 and r6=r2" +
									" and owner = '" + dbName + "' and TABLE_NAME= '" + talbeName + "' AND r2 = '"+ fieldName +"' " +
									") = " + strs[1] ;
						//	whereSql = " WHERE ( ( select DATA_SCALE from user_tab_columns " +
						//			" where TABLE_NAME= '" + talbeName + "' AND column_name = '"+ fieldName +"') " +
						//			" + LENGTH(to_char("+ fieldName +",'FM9999999999999999999999999')) ) = " + strs[0] + " " +
						//			"AND (select DATA_SCALE from user_tab_columns" +
						//			" where TABLE_NAME= '" + talbeName + "' AND column_name = '"+ fieldName +"' " +
						//			") = " + strs[1] ;


							//	whereSql +=  CheckFloatSql;
							break;
						case "_CHECK_FLOAT":// 浮点数范围检测
							floatSql += " AND LENGTH(SUBSTRING_INDEX(" + fieldName +",'.' ,- 1)) "+ ruleStr + qr.getDefaultValue();
							whereSql =  floatSql;
						//	whereSql +=  CheckFloatSql;
							break;
						case "_IS_NOT_NULL"://非空
						//	sqlStr ="select * from "+dbName+"."+talbeName;
						//	whereSql=" where "+(StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr():" ")+fieldName+" "+ruleStr;
							whereSql +=" "+(StringUtils.isNotBlank(qr.getAndOr())?qr.getAndOr()+" ":" ")+fieldName+" "+ruleStr;
							break;
						case "_UNIQUE"://唯一性
							sqlStr ="select * from "+dbName+"."+talbeName+" group by concat(" + fieldName + ")";
							whereSql=ruleStr;
						break;
						case "_CHECK_POINT_DATE":// 日期格式校验 CHECK_POINT_TIME_AND_DATE

							whereSql  += " " + andOr +
										" from_unixtime(unix_timestamp(" + fieldName +"),"+"'"+ qr.getDefaultValue()+"') <> "+ fieldName;
							andOr = "and";
							break;
						case "_CHECK_POINT_TIME":// 时间格式校验
							whereSql  += " " + andOr +
									" from_unixtime(unix_timestamp( " + fieldName +" ),"+"'"+ qr.getDefaultValue()+"') <> "+ fieldName;
							andOr = "and";
							break;
						case "_DECIMAL_CHOICE"://获取相应小数位数
							String strL[] = qr.getDefaultValue().split("/");
							sqlStr = "SELECT id,CAST("+fieldName+" AS decimal( "+Integer.parseInt(strL[0])+ ","+Integer.parseInt(strL[1])+" )) as " + fieldName + " from "+dbName+"."+talbeName;
							whereSql = "";
							break;

					}
				}
				
				//System.out.println("组装后的SQL=======================:"+sqlStr+whereSql);
				log.info("组装后的SQL=======================:"+sqlStr+whereSql);
				qualityTestQuery.setSqlText(sqlStr+whereSql);
				qualityTestQuery.setDataSource(ds);
				qualityTestQuery.setDataSchema(dataSchema);
				qualityTestQuery.setDataTable(dt);
				qualityTestQuery.setDataField(df);
				qualityTestQuery.setQualitySuite(qualitySuite);
				qualityTestQuery.setName("数据源:"+ds.getName()+"->schema:"+dataSchema.getName()+"->表:"+dt.getName()+"->字段:"+df.getName()+"->规则集:"+qualitySuite.getName()+",sql脚本生成");
				qualityTestQueryService.save(qualityTestQuery);//生成SQL脚本
				
				QualityTestCase qualityTestCase = new QualityTestCase();
				qualityTestCase.setQualityTestQuery(qualityTestQuery);
				qualityTestCase.setName("数据源:"+ds.getName()+"->schema:"+dataSchema.getName()+"->表:"+dt.getName()+"->字段:"+df.getName()+"->规则集:"+qualitySuite.getName()+",批量生成测试案例");
				qualityTestCase.setVersion("1.0");
				qualityTestCase.setIsSQLCase(true);
				qualityTestCase.setUsePage(false);
				IQualityTestCaseService.save(qualityTestCase);//生成测试案例
				
				int qtcID = qualityTestCase.getId();
				String num ="";
				if(qtcID > 0){
					if(String.valueOf(qtcID).length() == 1){
						num = "000"+qtcID;
					}else if(String.valueOf(qtcID).length() == 2){
						num = "00"+qtcID;
					}else if(String.valueOf(qtcID).length() == 3){
						num = "0"+qtcID;
					}else if(String.valueOf(qtcID).length() == 4){
						num = qtcID+"";
					}
				}
				//更新案例编号
				qualityTestCase.setCaseCode("SIT-SJJS-"+dataSchema.getName()+"."+dt.getName()+"-"+num);
				IQualityTestCaseService.save(qualityTestCase);
			}
		}
		log.info("批量生成SQL案例完成。。。。。。");
		return new ResultVO(true, StatusCode.OK, "设置检查点，新增脚本成功");
	}

	//查询是否为主键
	public String isPrimaryKey(List<QualityRule> qualityRules,
							QualitySuite qualitySuite, String dbName, String talbeName,
							String fieldName, List<String> selectFields) {
		StringBuilder sb = new StringBuilder();
		sb.append("select column_name ");
		// 选中的要查询的字段名称的集合
		sb.append("from INFORMATION_SCHEMA.`KEY_COLUMN_USAGE` where ");
		sb.append("table_name = "+ "'"+talbeName+"'");
		sb.append(" and TABLE_SCHEMA = " + "'"+dbName + "'"+" AND constraint_name = 'PRIMARY' ");
		sb.append("and column_name = " + "'"+fieldName + "'");
		return sb.toString();
	}
	@Override
	@Transactional
	public ResultVO checkPointByTable(Map<Object, Object> map) {
		Integer tableId = Integer.parseInt(map.get("tableId") + "");
		Integer qualitySuiteId = Integer.parseInt(map.get("qualitySuiteId")
				+ "");
		List<DataField> testFields = testFieldService.findAllByTableId(tableId);
		QualitySuite qualitySuite = qualitySuiteRepository.findById(
				qualitySuiteId).get();
		DataSource dataSource = testFields.get(0).getDataTable()
				.getDataSchema().getDataSource();
		String dbName = testFields.get(0).getDataTable().getDataSchema()
				.getName();
		List<QualityRule> qualityRules = qualitySuite.getQualityRules();
		// 选中的要查询的字段名称的集合
		List<String> selectFields = (List<String>) map.get("selectFields");

		for (DataField testField : testFields) {
			QualityTestPoint qualityTestPoint = new QualityTestPoint();
			QualityTestQuery qualityTestQuery = new QualityTestQuery();
			String dataType = testField.getDataType();
			qualityTestPoint.setDataField(testField);
			qualityTestPoint.setQualitySuite(qualitySuite);
			// 配置检查点
			qualityTestPointRepository.save(qualityTestPoint);
			String fieldName = testField.getName();
			String talbeName = testField.getTalbeName();
			String sb = null;
			String ruleNum = null;
			if(qualitySuiteId == 1 ){
				ruleNum = CHECK_NULL;
			}else if(qualitySuiteId == 2 || qualitySuiteId == 3 || qualitySuiteId == 4 || qualitySuiteId == 5){
				ruleNum = CHECK_RANGE;
			}else if(qualitySuiteId == 7){
				ruleNum = CHECK_UNIQUE;
			}else if(qualitySuiteId == 9){
				ruleNum = CHECK_NOTIN;
			}else if(qualitySuiteId == 10){
				ruleNum = CHECK_NOTFLOAT;
			}else if(qualitySuiteId == 11){
				ruleNum = CHECK_NON_AMOUNT_RANGE;
			}else if(qualitySuiteId == 12 || qualitySuiteId == 13){
				ruleNum =  CHECK_POINT_TIME_AND_DATE;
			}else if(qualitySuiteId == 14 ){
				ruleNum = "CHECK__PRIMARY_KEY";
			}
			// rule规则
			switch (ruleNum) {
				case CHECK_NULL:
					sb = checkNull(qualityRules, qualitySuite, dbName, talbeName,
							fieldName, selectFields);
					break;
				case CHECK_RANGE:
					dataType = testField.getDataType();
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
				case "CHECK__PRIMARY_KEY":
					sb = isPrimaryKey(qualityRules, qualitySuite, dbName,
							talbeName, fieldName, selectFields);
					break;
			}

			qualityTestQuery.setSqlText(sb);
			qualityTestQuery.setDataSource(dataSource);
			qualityTestQuery.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName());
			// 自动生成脚本
			qualityTestQueryService.save(qualityTestQuery);
			// 自动生成案例
			QualityTestCase qualityTestCase = new QualityTestCase();
			qualityTestCase.setQualityTestQuery(qualityTestQuery);
			qualityTestCase.setName(dbName + "数据库" + talbeName + "表"
					+ fieldName + "字段" + qualitySuite.getName() + "的测试案例");
			IQualityTestCaseService.save(qualityTestCase);
		}
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
