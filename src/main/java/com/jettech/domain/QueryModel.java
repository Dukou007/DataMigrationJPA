package com.jettech.domain;

import java.util.ArrayList;
import java.util.List;

import com.jettech.EnumExecuteStatus;
import com.jettech.EnumTestCaseType;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestQueryField;
import com.jettech.entity.TestRule;

public class QueryModel extends BaseModel {

	private String name;
	private String sqlText;
	private String keyText;
	private String pageText;
	private Integer maxNullKeyCount;
	private Integer maxDuplicatedKeyCount;
	private DbModel dbModel;
	private EnumTestCaseType testCaseType = EnumTestCaseType.None;

	private List<FieldModel> testFields = new ArrayList<>();// 被测字段
	private List<FieldModel> keyFields = new ArrayList<>();// 业务主键
	private List<FieldModel> pageFields = new ArrayList<>();// 业务主键

	private List<RuleModel> testRules = new ArrayList<>();

	private List<DataField> queryColumns;

	private EnumExecuteStatus execState;
	private Integer queryErrCount = 0;

	public QueryModel(TestQuery testQuery) throws Exception {
		// super.parse(testQuery);
		if (testQuery == null) {
			return;
		}
		parseEntity(testQuery);
		this.dbModel = new DbModel(testQuery.getDataSource());
		StringBuilder keyBuilder = new StringBuilder();
		for (TestQueryField field : testQuery.getKeyFields()) {
			FieldModel fieldModel = new FieldModel(field);
			keyFields.add(fieldModel);
			if (keyBuilder.length() > 0)
				keyBuilder.append(",");
			keyBuilder.append(fieldModel.getName());
		}
		// 当没有手工设置keyText的时候，将Key转换为keyText
		if (this.keyText == null || this.keyText.trim().isEmpty())
			this.setKeyText(keyBuilder.toString());

		for (TestQueryField field : testQuery.getTestFields()) {
			testFields.add(new FieldModel(field));
		}
		for (TestRule rule : testQuery.getTestRules()) {
			testRules.add(new RuleModel(rule));
		}

		//
		StringBuilder pageBuilder = new StringBuilder();
		for (TestQueryField field : testQuery.getPageFields()) {
			FieldModel fieldModel = new FieldModel(field);
			pageFields.add(fieldModel);
			if (pageBuilder.length() > 0)
				pageBuilder.append(",");
			pageBuilder.append(fieldModel.getName());
		}
		if (this.pageText == null || this.pageText.trim().isEmpty())
			this.setPageText(pageBuilder.toString());

	}

	@Override
	public void parseEntity(BaseEntity entity) {
		TestQuery testQuery = (TestQuery) entity;
		super.parseEntity(testQuery);
		this.setName(testQuery.getName());
		this.setMaxDuplicatedKeyCount(testQuery.getMaxDuplicatedKeyCount());
		this.setMaxNullKeyCount(testQuery.getMaxNullKeyCount());
		this.setSqlText(testQuery.getSqlText());
		this.setKeyText(testQuery.getKeyText());
		this.pageText = testQuery.getPageText();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getSqlText() {
		return sqlText;
	}

	public void setSqlText(String sqlText) {
		this.sqlText = sqlText;
	}

	public Integer getMaxNullKeyCount() {
		return maxNullKeyCount;
	}

	public void setMaxNullKeyCount(Integer maxNullKeyCount) {
		this.maxNullKeyCount = maxNullKeyCount;
	}

	public Integer getMaxDuplicatedKeyCount() {
		return maxDuplicatedKeyCount;
	}

	public void setMaxDuplicatedKeyCount(Integer maxDuplicatedKeyCount) {
		this.maxDuplicatedKeyCount = maxDuplicatedKeyCount;
	}

	public DbModel getDbModel() {
		return dbModel;
	}

	public void setDbModel(DbModel dbModel) {
		this.dbModel = dbModel;
	}

	public List<FieldModel> getTestFields() {
		return testFields;
	}

	public void setTestFields(List<FieldModel> testFields) {
		this.testFields = testFields;
	}

	public List<FieldModel> getKeyFields() {
		return keyFields;
	}

	public void setKeyFields(List<FieldModel> keyFields) {
		this.keyFields = keyFields;
	}

	public List<RuleModel> getTestRules() {
		return testRules;
	}

	public void setTestRules(List<RuleModel> testRules) {
		this.testRules = testRules;
	}

	public DbModel getDataSource() {
		return dbModel;
	}

	public String getKeyText() {
		return keyText;
	}

	public void setKeyText(String keyText) {
		this.keyText = keyText;
	}

	/**
	 * SQL查询实际的列表定义
	 * 
	 * @return
	 */
	public List<DataField> getQueryColumns() {
		return queryColumns;
	}

	public void setQueryColumns(List<DataField> queryColumns) {
		this.queryColumns = queryColumns;
	}

	public List<FieldModel> getPageFields() {
		return pageFields;
	}

	public void setPageFields(List<FieldModel> pageFields) {
		this.pageFields = pageFields;
	}

	public String getPageText() {
		return pageText;
	}

	public void setPageText(String pageText) {
		this.pageText = pageText;
	}

	public EnumTestCaseType getTestCaseType() {
		return testCaseType;
	}

	public void setTestCaseType(EnumTestCaseType testCaseType) {
		this.testCaseType = testCaseType;
	}

	public EnumExecuteStatus getExecState() {
		return execState;
	}

	public void setExecState(EnumExecuteStatus execState) {
		this.execState = execState;
	}

	public Integer getQueryErrCount() {
		return queryErrCount;
	}

	public void setQueryErrCount(Integer queryErrCount) {
		this.queryErrCount = queryErrCount;
	}

}
