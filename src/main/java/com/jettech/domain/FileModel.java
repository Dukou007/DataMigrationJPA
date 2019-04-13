package com.jettech.domain;

import java.util.ArrayList;
import java.util.List;

import com.jettech.entity.TestFile;

public class FileModel extends BaseModel {

	private String localFileName;
	private FileDataSourceModel dataSoruce;
	private List<FieldModel> columnList = new ArrayList<>();

	public List<FieldModel> getColumnList() {
		return columnList;
	}

	public void setColumnList(List<FieldModel> columnList) {
		this.columnList = columnList;
	}

	public FileModel(TestFile testFile) {
		if (testFile.getDataSource() != null) {
			this.dataSoruce = new FileDataSourceModel(testFile.getDataSource());
		} else {
			// TODO: 处理异常
		}
		if (testFile.getFields() != null) {
			for (com.jettech.entity.DataField field : testFile.getFields()) {
				FieldModel fieldModel = new FieldModel(field);
				columnList.add(fieldModel);
			}

		} else {
			// TODO: 处理异常
		}

	}

	public FileDataSourceModel getDataSoruce() {
		return dataSoruce;
	}

	public void setDataSoruce(FileDataSourceModel dataSoruce) {
		this.dataSoruce = dataSoruce;
	}

	public String getLocalFileName() {
		return localFileName;
	}

	public void setLocalFileName(String localFileName) {
		this.localFileName = localFileName;
	}
}
