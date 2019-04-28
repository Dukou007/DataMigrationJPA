package com.jettech.entity;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

@Entity
@Table(name = "test_file_query")
public class TestFileQuery extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 835636110481852173L;
	private String fileName;
	private List<TestRule> testRules;
	private String selectText;
	private FileDataSource fileDataSource;
	private Integer maxNullKeyCount;
	private Integer maxDuplicatedKeyCount;
	private List<TestFileQueryField> testFields;
	private List<TestFileQueryField> keyFields;
	private List<TestFileQueryField> pageFields;
	private String keyText;
	private String pageText;
	
	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}


	@OneToMany(cascade = CascadeType.ALL)
	@JoinColumn(name = "test_query_id", referencedColumnName = "id")
	public List<TestRule> getTestRules() {
		return testRules;
	}

	public void setTestRules(List<TestRule> testRules) {
		this.testRules = testRules;
	}

	public Integer getMaxNullKeyCount() {
		return maxNullKeyCount;
	}

	public void setMaxNullKeyCount(Integer maxNullKeyCount) {
		this.maxNullKeyCount = maxNullKeyCount;
	}

	public String getSelectText() {
		return selectText;
	}

	public void setSelectText(String selectText) {
		this.selectText = selectText;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "file_data_source_id")
	public FileDataSource getFileDataSource() {
		return fileDataSource;
	}

	public void setFileDataSource(FileDataSource fileDataSource) {
		this.fileDataSource = fileDataSource;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "select_query_id", referencedColumnName = "id")
	public List<TestFileQueryField> getTestFields() {
		return testFields;
	}

	public void setTestFields(List<TestFileQueryField> testFields) {
		this.testFields = testFields;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "key_query_id", referencedColumnName = "id")
	public List<TestFileQueryField> getKeyFields() {
		return keyFields;
	}

	public void setKeyFields(List<TestFileQueryField> keyFields) {
		this.keyFields = keyFields;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "page_query_id", referencedColumnName = "id")
	public List<TestFileQueryField> getPageFields() {
		return pageFields;
	}

	public void setPageFields(List<TestFileQueryField> pageFields) {
		this.pageFields = pageFields;
	}

	public String getKeyText() {
		return keyText;
	}

	public void setKeyText(String keyText) {
		this.keyText = keyText;
	}

	public String getPageText() {
		return pageText;
	}

	public void setPageText(String pageText) {
		this.pageText = pageText;
	}

	public Integer getMaxDuplicatedKeyCount() {
		return maxDuplicatedKeyCount;
	}

	public void setMaxDuplicatedKeyCount(Integer maxDuplicatedKeyCount) {
		this.maxDuplicatedKeyCount = maxDuplicatedKeyCount;
	}

}
