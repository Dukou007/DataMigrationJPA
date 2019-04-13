package com.jettech.service.Impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jettech.EnumFieldType;
import com.jettech.entity.DataSource;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestQueryField;
import com.jettech.entity.TestSuite;
import com.jettech.entity.DataField;
import com.jettech.entity.DataTable;
import com.jettech.repostory.CompFieldsRepository;
import com.jettech.repostory.DataFieldRepository;
import com.jettech.repostory.DataSourceRepository;
import com.jettech.repostory.DataTableRepository;
import com.jettech.repostory.MapperEntityRepository;
import com.jettech.repostory.TestCaseRepository;
import com.jettech.repostory.TestSuiteRepository;
import com.jettech.service.IMapperService;
import com.jettech.util.ExcelReader;
import com.jettech.util.StringUtil;

@Service
public class MapperServiceImpl implements IMapperService {
	private static Logger logger = LoggerFactory.getLogger(MapperServiceImpl.class);
	@Autowired
	private ExcelReader excelReader;

	@Autowired
	private MapperEntityRepository mapperEntityRepository;
	@Autowired
	private TestSuiteRepository testSuiteRepository;
	@Autowired
	private DataSourceRepository dataSourceRepository;
	@Autowired
	private TestCaseRepository testCaseRepository;
	@Autowired
	private CompFieldsRepository compFieldsRepository;
	@Autowired
	private DataTableRepository testTableRepository;
	@Autowired
	private DataFieldRepository testFieldRepository;

	@Override
	public void loadMapper(String filePath, Class clazz) throws Exception {

		/*
		 * String newSQLText = "SELECT "; String oldSQLText = "SELECT "; String
		 * oldTableName = ""; String title = ""; DataSource newDataSource =
		 * null; List<TestField> newTestFields = new ArrayList<>();
		 * List<TestField> oldTestFields = new ArrayList<>(); List<TestField>
		 * newKeyFields = new ArrayList<>(); List<TestField> oldKeyFields = new
		 * ArrayList<>(); TestQuery newTestQuery = new TestQuery(); TestQuery
		 * oldTestQuery = new TestQuery();
		 */

		// testCase.setTestSuite(testSuite);
		// newTestQuery.setDataSource(newDataSource);
		// oldTestQuery.setDataSource(oldDataSource);
		// 读取Excel的title
		// String title = excelReader.readExcelTitle(filePath);
		// 获取比较的keyField
		// CompFields compFields =
		// compFieldsRepository.findByNewTableName(title);

		List<List<Map<String, Object>>> sheetList = excelReader.read(filePath, clazz, 4);

		//List<TestCase> testCaseList = new ArrayList<>();
		// testTable.getTestFileds().stream().findFirst(p=>p.);
		for (List<Map<String, Object>> list : sheetList) {
			if (list != null) {

				String newSQLText = "SELECT ";
				String oldSQLText = "SELECT ";
				String oldTableName = "";
				String title = "";
				DataSource newDataSource = null;
				List<TestQueryField> newTestFields = new ArrayList<>();
				List<TestQueryField> oldTestFields = new ArrayList<>();
				List<TestQueryField> newKeyFields = new ArrayList<>();
				List<TestQueryField> oldKeyFields = new ArrayList<>();
				TestQuery newTestQuery = new TestQuery();
				TestQuery oldTestQuery = new TestQuery();

				DataField newField = null;
				DataField oldField = null;
				for (Map<String, Object> map : list) {

					// System.out.println(map);

					if ((map.get("newfield") != null && !(map.get("newfield").toString().trim()).equals("")
					        && !StringUtil.isAllMandarin(map.get("newfield").toString()))
					        && (map.get("oldTableName") != null
					                && !(map.get("oldTableName").toString().trim()).equals("")
					                && !StringUtil.isAllMandarin(map.get("oldTableName").toString()))
					        && (map.get("oldField") != null && !(map.get("oldField").toString().trim()).equals("")
					                && !StringUtil.isAllMandarin(map.get("oldField").toString()))
					        && (map.get("newTableName") != null
					                && !(map.get("newTableName").toString().trim()).equals("")
					                && !StringUtil.isAllMandarin(map.get("newTableName").toString()))) {
						title = map.get("newTableName").toString();
						newField = testFieldRepository.findByNameAndTableName(map.get("newfield").toString(), title);
						if (newField == null) {
							logger.info("###########not found field:" + map.get("newfield").toString());
							TestQueryField queryField = new TestQueryField();
							queryField.setExpression(map.get("newfield").toString());
							queryField.setFieldType(EnumFieldType.Expression);
							newTestFields.add(queryField);
						} else {
							newTestFields.add(convertToQueryField(newField));
							// newKeyFields.add(convertToQueryField(newField));
						}
						if (!title.equals("")) {
							DataTable testTable = testTableRepository.findByName(title);
							if (testTable == null) {
								logger.info("############not found table:" + title);
							}
							newDataSource = testTable.getDataSchema().getDataSource();
						}

						oldField = testFieldRepository.findByNameAndTableName(map.get("oldField").toString(),
						        map.get("oldTableName").toString());
						if (oldField == null) {
							logger.info("###########not found field:" + map.get("oldField").toString());
							TestQueryField queryField = new TestQueryField();
							queryField.setExpression(map.get("oldField").toString());
							queryField.setFieldType(EnumFieldType.Expression);
							oldTestFields.add(queryField);
						} else {
							// oldKeyFields.add(oldField);
							oldTestFields.add(convertToQueryField(oldField));
						}

						oldTableName = map.get("oldTableName").toString();
						newSQLText += (map.get("newfield").toString().trim() + " ,");
						oldSQLText += (map.get("oldField").toString().trim() + " ,");
					}

					/*
					 * if ((map.get("oldTableName") != null &&
					 * !(map.get("oldTableName").toString().trim()).equals("")
					 * && !StringUtil.isAllMandarin(map.get("oldTableName").
					 * toString())) && (map.get("oldField") != null &&
					 * !(map.get("oldField").toString().trim()).equals("") &&
					 * !StringUtil.isAllMandarin(map.get("oldField").toString())
					 * )) { TestField oldField =
					 * testFieldRepository.findByNameAndTableName(map.get(
					 * "oldField").toString(),
					 * map.get("oldTableName").toString());
					 * oldKeyFields.add(oldField);
					 * 
					 * // TestTable testTable = //
					 * testTableRepository.findByName(map.get("oldTableName").
					 * toString()); // oldDataSource =
					 * testTable.getTestDatabase().getDataSource();
					 * 
					 * 
					 * oldField.setTalbeName(map.get("oldTableName").toString())
					 * ; oldField.setName(map.get("oldField").toString());
					 * TestTable oldTestTable =
					 * testTableRepository.findByName(oldTableName);
					 * oldField.setTestTable(oldTestTable);
					 * 
					 * //oldTestFields.add(oldField); }
					 */ else {
						continue;
					}

					/*
					 * if (map.get("oldTableName") != null &&
					 * !(map.get("oldTableName").toString().trim()).equals("")
					 * && !StringUtil.isAllMandarin(map.get("oldTableName").
					 * toString()) && map.get("newfield") != null &&
					 * !(map.get("newfield").toString().trim()).equals("") &&
					 * !StringUtil.isAllMandarin(map.get("newfield").toString())
					 * && map.get("oldField") != null &&
					 * !(map.get("oldField").toString().trim()).equals("") &&
					 * !StringUtil.isAllMandarin(map.get("oldField").toString())
					 * ) { oldTableName = map.get("oldTableName").toString();
					 * newSQLText += (map.get("newfield").toString().trim() +
					 * " ,"); oldSQLText +=
					 * (map.get("oldField").toString().trim() + " ,"); }
					 */
					/*
					 * if (map.get("newfield") != null &&
					 * !(map.get("newfield").toString().trim()).equals("") &&
					 * !StringUtil.isAllMandarin(map.get("newfield").toString())
					 * ) { newSQLText += (map.get("newfield").toString() +
					 * " ,"); } if (map.get("oldField") != null &&
					 * !(map.get("oldField").toString().trim()).equals("") &&
					 * !StringUtil.isAllMandarin(map.get("oldField").toString())
					 * ) { oldSQLText += (map.get("oldField").toString() +
					 * " ,"); }
					 */

				}

				String newSubstr = newSQLText.substring(0, newSQLText.length() - 1);
				newSubstr += (" FROM " + title);
				String oldSubstr = oldSQLText.substring(0, oldSQLText.length() - 1);
				oldSubstr += (" FROM " + oldTableName);

				newTestQuery.setTestFields(newTestFields);
				newTestQuery.setSqlText(newSubstr);
				newTestQuery.setDataSource(newDataSource);
				newTestQuery.setKeyFields(newKeyFields);

				oldTestQuery.setTestFields(oldTestFields);
				oldTestQuery.setSqlText(oldSubstr);
				System.out.println("******************" + newSubstr);
				System.out.println("&&&&&&&&&&&&&&&&&&" + oldSubstr);

				DataSource oldDataSource = dataSourceRepository.getOne(1);
				oldTestQuery.setDataSource(oldDataSource);
				oldTestQuery.setKeyFields(oldKeyFields);

				TestSuite testSuite = testSuiteRepository.getOne(1);

				TestCase testCase = new TestCase();
//				testCase.setTestSuite(testSuite);
				testCase.setSourceQuery(oldTestQuery);
				testCase.setTargetQuery(newTestQuery);
				testCase.setName(title);
				testCase.setCreateUser("zhouxinfei");
				testCase.setEditUser("zhouxinfei");
				testCase.setIsSQLCase(true);
				testCase.setUsePage(false);

//				newTestQuery.setTestCase(testCase);
//				oldTestQuery.setTestCase(testCase);
				// System.out.println(testCase);
				testCaseRepository.save(testCase);
				logger.info("save testCase:" + testCase.getName());
				
			}
		}
		
	}

	private List<TestQueryField> convert2QueryFileds(List<DataField> newTestFields) {
		List<TestQueryField> newQueryFields = new ArrayList<>();
		for (DataField field : newTestFields) {
			TestQueryField queryField = convertToQueryField(field);
			newQueryFields.add(queryField);
		}
		return newQueryFields;
	}

	private TestQueryField convertToQueryField(DataField field) {
		TestQueryField newQueryField = new TestQueryField();
		newQueryField.setDataField(field);
		newQueryField.setFieldType(EnumFieldType.TestField);
		return newQueryField;
	}

	private final static String _CLLAZZ_MAPPER_ENTITTY = "MapperEntity.class";



}
