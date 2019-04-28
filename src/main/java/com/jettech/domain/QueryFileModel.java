package com.jettech.domain;

import java.util.ArrayList;
import java.util.List;
import com.jettech.EnumTestCaseType;
import com.jettech.entity.BaseEntity;
import com.jettech.entity.TestFileQuery;
import com.jettech.entity.TestFileQueryField;
import com.jettech.entity.TestRule;

public class QueryFileModel extends BaseModel {

	private String fileName;
	private String keyText;
	private String pageText;
	private Integer maxNullKeyCount;
	private Integer maxDuplicatedKeyCount;
	private FileDataSourceModel fileDataSource;
	private EnumTestCaseType testCaseType = EnumTestCaseType.None;

	private List<FieldModel> testFields = new ArrayList<>();// 被测字段
	private List<FieldModel> keyFields = new ArrayList<>();// 业务主键
	private List<FieldModel> pageFields = new ArrayList<>();// 业务主键

	private List<RuleModel> testRules = new ArrayList<>();

	private List<com.jettech.entity.DataField> queryColumns;

	public QueryFileModel(TestFileQuery testQuery) throws Exception {
		// super.parse(testQuery);
		if (testQuery == null) {
			return;
		}
		parseEntity(testQuery);
		this.fileDataSource = new FileDataSourceModel(testQuery.getFileDataSource());
		StringBuilder keyBuilder = new StringBuilder();
		for (TestFileQueryField field : testQuery.getKeyFields()) {
			FieldModel fieldModel = new FieldModel(field);
			keyFields.add(fieldModel);
			if (keyBuilder.length() > 0)
				keyBuilder.append(",");
			keyBuilder.append(fieldModel.getName());
		}
		// 当没有手工设置keyText的时候，将Key转换为keyText
		if (this.keyText == null || this.keyText.trim().isEmpty())
			this.setKeyText(keyBuilder.toString());

		for (TestFileQueryField field : testQuery.getTestFields()) {
			testFields.add(new FieldModel(field));
		}
		for (TestRule rule : testQuery.getTestRules()) {
			testRules.add(new RuleModel(rule));
		}

		//
		StringBuilder pageBuilder = new StringBuilder();
		for (TestFileQueryField field : testQuery.getPageFields()) {
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
		TestFileQuery testQuery = (TestFileQuery) entity;
		super.parseEntity(testQuery);
		this.setFileName(testQuery.getFileName());
		this.setMaxDuplicatedKeyCount(testQuery.getMaxDuplicatedKeyCount());
		this.setMaxNullKeyCount(testQuery.getMaxNullKeyCount());
		this.setKeyText(testQuery.getKeyText());
		this.pageText = testQuery.getPageText();
	}


	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public FileDataSourceModel getFileDataSource() {
		return fileDataSource;
	}

	public void setFileDataSource(FileDataSourceModel fileDataSource) {
		this.fileDataSource = fileDataSource;
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

	/*public DbModel getDataSource() {
		return dbModel;
	}*/

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
	public List<com.jettech.entity.DataField> getQueryColumns() {
		return queryColumns;
	}

	public void setQueryColumns(List<com.jettech.entity.DataField> filedColumns) {
		this.queryColumns = filedColumns;
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

}
