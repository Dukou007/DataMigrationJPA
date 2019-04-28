package com.jettech.domain;

import com.jettech.EnumTestCaseType;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.QualityTestQuery;

import java.util.ArrayList;
import java.util.List;

public class QualityQueryModel extends BaseModel {

	private String name;
	private String sqlText;
	private String keyText;
	private String pageText;
	private Integer maxNullKeyCount;
	private Integer maxDuplicatedKeyCount;
	private DbModel dbModel;
	private EnumTestCaseType testCaseType = EnumTestCaseType.None;
	public EnumTestCaseType getTestCaseType() {
		return testCaseType;
	}

	//添加结果集主表的id 20190412
	private Integer QualityTestResultId;
	public void setTestCaseType(EnumTestCaseType testCaseType) {
		this.testCaseType = testCaseType;
	}


	private List<FieldModel> testFields = new ArrayList<>();
//	private List<FieldModel> keyFields = new ArrayList<>();
	private List<RuleModel> testRules = new ArrayList<>();
	private List<FieldModel> pageFields = new ArrayList<>();//
	
	private List<DataField> queryColumns;

	public QualityQueryModel(QualityTestQuery qualityTestQuery) throws Exception {
		// super.parse(qualityTestQuery);
		if (qualityTestQuery == null) {
			return;
		}
		parseEntity(qualityTestQuery);
		this.dbModel = new DbModel(qualityTestQuery.getDataSource());
		//质量代码添加 20190408  qualityTestQuery.getDataFields(); =============
		List<FieldModel> fieldModels = new ArrayList<>();
		for(com.jettech.entity.DataField s : qualityTestQuery.getDataFields()){
			FieldModel fm = new FieldModel(s);
			fieldModels.add(fm);
		}
		this.testFields = fieldModels;
		// =====================================================


//		StringBuilder keyBuilder = new StringBuilder();
//		for (TestQueryField field : qualityTestQuery.getKeyFields()) {
//			FieldModel fieldModel = new FieldModel(field);
//			keyFields.add(fieldModel);
//			if (keyBuilder.length() > 0)
//				keyBuilder.append(",");
//			keyBuilder.append(fieldModel.getName());
//		}
		// 当没有手工设置keyText的时候，将Key转换为keyText
//		if (this.keyText == null || this.keyText.trim().isEmpty())
//			this.setKeyText(keyBuilder.toString());
		
		/*for (TestQueryField field : qualityTestQuery.getTestFields()) {
			testFields.add(new FieldModel(field));
		}*/
//		for (TestRule rule : qualityTestQuery.getTestRules()) {
//			testRules.add(new RuleModel(rule));
//		}
		
		//
		StringBuilder pageBuilder = new StringBuilder();
		/*for (TestQueryField field : qualityTestQuery.getPageFields()) {
			FieldModel fieldModel = new FieldModel(field);
			pageFields.add(fieldModel);
			if (pageBuilder.length() > 0)
				pageBuilder.append(",");
			pageBuilder.append(fieldModel.getName());
		}*/
		if (this.getPageText() == null || this.getPageText().trim().isEmpty())
			this.setPageText(pageBuilder.toString());
		
	}


	@Override
	public void parseEntity(BaseEntity entity) {
		QualityTestQuery qualityTestQuery = (QualityTestQuery) entity;
		super.parseEntity(qualityTestQuery);
		this.setName(qualityTestQuery.getName());
//		this.setMaxDuplicatedKeyCount(qualityTestQuery.getMaxDuplicatedKeyCount());
//		this.setMaxNullKeyCount(qualityTestQuery.getMaxNullKeyCount());
		this.setSqlText(qualityTestQuery.getSqlText());
		//this.setKeyText(qualityTestQuery.getKeyText());
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

//	public List<FieldModel> getKeyFields() {
//		return keyFields;
//	}
//
//	public void setKeyFields(List<FieldModel> keyFields) {
//		this.keyFields = keyFields;
//	}

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


	public String getPageText() {
		return pageText;
	}


	public void setPageText(String pageText) {
		this.pageText = pageText;
	}


	public List<FieldModel> getPageFields() {
		return pageFields;
	}


	public void setPageFields(List<FieldModel> pageFields) {
		this.pageFields = pageFields;
	}

	public Integer getQualityTestResultId() {
		return QualityTestResultId;
	}

	public void setQualityTestResultId(Integer qualityTestResultId) {
		QualityTestResultId = qualityTestResultId;
	}
}
