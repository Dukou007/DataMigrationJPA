package com.jettech.domain;

import java.util.ArrayList;
import java.util.List;

import com.jettech.EnumTestCaseType;
import com.jettech.entity.CaseModelSet;
import com.jettech.entity.CaseModelSetDetails;
import com.jettech.entity.DataTable;

import javafx.util.Pair;

public class ModelCaseModel extends CaseModel {

	// 需要对比的成对的表模型对象
	List<Pair<DataModel, DataModel>> list = new ArrayList<>();
	
    
	public ModelCaseModel(CaseModelSet testCase) {
		super.setId(testCase.getModelTestCase().getId());
		super.setCreateUser(testCase.getModelTestCase().getCreateUser());
		super.setEditUser(testCase.getModelTestCase().getEditUser());
		super.setCreateTime(testCase.getModelTestCase().getCreateTime());
		super.setEditTime(testCase.getModelTestCase().getEditTime());
		super.setName(testCase.getModelTestCase().getName());
		super.setVersion(testCase.getModelTestCase().getVersion());
		super.setIsSQLCase(testCase.getModelTestCase().getIsSQLCase());
		super.setMaxResultRows(testCase.getModelTestCase().getMaxResultRows());
		super.setUsePage(testCase.getModelTestCase().getUsePage());
		super.setPageSize(0);
		super.setTestCaseType(EnumTestCaseType.QualityCheck);
		this.testCaseType = EnumTestCaseType.DataModel;
		
		for (CaseModelSetDetails detail : testCase.getDetails()) {
			DataTable leftTable = detail.getDatumModelSetTable();
			DataModel leftModel = new DataModel(leftTable);
			DataTable rightTable = detail.getTestModelSetTable();
			DataModel rightModel = new DataModel(rightTable);
			list.add(new Pair<>(leftModel, rightModel));
		}
	}
	
	public List<Pair<DataModel, DataModel>> getModelPairList()
	{
		return list;
	}
}
