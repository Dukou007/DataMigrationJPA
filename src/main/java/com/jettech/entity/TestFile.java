package com.jettech.entity;

import java.util.ArrayList;
import java.util.List;

public class TestFile extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = 835636110481852173L;

	private FileDataSource dataSource;
	
	private List<DataField> fields=new ArrayList<>();

	public FileDataSource getDataSource() {
		return dataSource;
	}

	public void setDataSource(FileDataSource dataSource) {
		this.dataSource = dataSource;
	}

	public List<DataField> getFields() {
		return fields;
	}

	public void setFields(List<DataField> fields) {
		this.fields = fields;
	}
	
}
