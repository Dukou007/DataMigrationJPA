package com.jettech.thread;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jettech.EnumConvertRule;
import com.jettech.EnumFieldPropertyType;
import com.jettech.entity.DataField;
import com.jettech.entity.DataTable;
import com.jettech.entity.TestCase;
import com.jettech.entity.TestResult;
import com.jettech.entity.TestResultItem;
import com.jettech.service.ITestCaseService;
import com.jettech.service.ITestFieldService;
import com.jettech.service.ITestResultItemService;
import com.jettech.service.ITestReusltService;
import com.jettech.service.ITestTableService;
import com.jettech.service.Impl.DataFieldServiceImpl;
import com.jettech.service.Impl.DataTableServiceImpl;
import com.jettech.service.Impl.TestCaseServiceImpl;
import com.jettech.service.Impl.TestResultItemImpl;
import com.jettech.service.Impl.TestResultServiceImpl;
import com.jettech.util.SpringUtils;
import javafx.util.*;

public class CompareDataWorker implements Runnable {

	private Logger logger = LoggerFactory.getLogger(CompareDataWorker.class);

	private int leftdbId;
	private int rightdbId;
	ITestTableService testTableService = null;
	ITestCaseService testCaseService = null;
	ITestReusltService testReusltService = null;
	ITestFieldService testFieldService = null;
	ITestResultItemService testResultItemService = null;
	List<DataTable> leftTableList = null;
	List<DataTable> rightTableList = null;
	Map<String, DataTable> leftTableMaps = new HashMap<>();
	Map<String, DataTable> rightTableMaps = new HashMap<>();
	Map<String, DataField> leftFieldMaps = new HashMap<>();
	Map<String, DataField> rightFieldMaps = new HashMap<>();

	public CompareDataWorker(int leftdbId, int rightdbId) {
		try {
			this.leftdbId = leftdbId;
			this.rightdbId = rightdbId;
			testTableService = (ITestTableService) SpringUtils.getBean(DataTableServiceImpl.class);
			leftTableList = testTableService.findByForeignKey(leftdbId);
			rightTableList = testTableService.findByForeignKey(rightdbId);
			for (DataTable testTable : leftTableList) {
				leftTableMaps.put(testTable.getName(), testTable);
			}
			for (DataTable testTable : rightTableList) {
				rightTableMaps.put(testTable.getName(), testTable);
			}
		} catch (Exception e) {
			e.getLocalizedMessage();
		}
	}

	public CompareDataWorker(List<Pair<DataField, DataField>> list) {
		
		
	}

	@Override
	public void run() {
		boolean tablecompareResult = compareTable();
		boolean fieldcompareResult = compareField();
		if (tablecompareResult && fieldcompareResult) {
			recordFieldSame();
		}
	}

	public boolean compareFieldMap(Map<String, DataField> leftFieldMaps, Map<String, DataField> rightFieldMaps) {
		int fieldNameIfSameFlag = 0;
		EnumFieldPropertyType fieldProperty;
		boolean fieldNameSameflag = true;
		for (String keyField : leftFieldMaps.keySet()) {
			fieldNameIfSameFlag = 0;
			int left_data_len = leftFieldMaps.get(keyField).getDataLength();
			int left_data_pre = leftFieldMaps.get(keyField).getDataPrecision();
			String left_data_type = leftFieldMaps.get(keyField).getDataType();
			if (rightFieldMaps.containsKey(keyField)) {
				fieldNameIfSameFlag = 1;
				int right_data_len = rightFieldMaps.get(keyField).getDataLength();
				int right_data_pre = rightFieldMaps.get(keyField).getDataPrecision();
				String right_data_type = rightFieldMaps.get(keyField).getDataType();
				if (left_data_len != right_data_len || left_data_pre != right_data_pre
						|| !left_data_type.equals(right_data_type)) {
					fieldNameIfSameFlag = 2;
					if (left_data_len != right_data_len) {
						fieldProperty = EnumFieldPropertyType.FiledLengthNotSame;
						recordFieldPropertyNotSame(keyField, fieldProperty);
					}
					if (left_data_pre != right_data_pre) {
						fieldProperty = EnumFieldPropertyType.FiledPrecisionNotSame;
						recordFieldPropertyNotSame(keyField, fieldProperty);
					}
					if (!left_data_type.equals(right_data_type)) {
						fieldProperty = EnumFieldPropertyType.FiledTypeNotSame;
						recordFieldPropertyNotSame(keyField, fieldProperty);
					}
				}
			}
			if (fieldNameIfSameFlag == 0) {
				fieldNameSameflag = false;
				recordFieldNameNotSame(keyField);
			}

			if (fieldNameIfSameFlag == 2) {
				fieldNameSameflag = false;
			}
		}

		for (String keyField : rightFieldMaps.keySet()) {
			fieldNameIfSameFlag = 0;
			if (leftFieldMaps.containsKey(keyField)) {
				fieldNameIfSameFlag = 1;
			}
			if (fieldNameIfSameFlag == 0) {
				fieldNameSameflag = false;
				recordFieldNameNotSame(keyField);
			}
		}
		return fieldNameSameflag;
	}

	public boolean compareField() {
		boolean fieldNameSameflag = true;
		for (String leftkey : leftTableMaps.keySet()) {
			int lefttable_id = leftTableMaps.get(leftkey).getId();
			testFieldService = (ITestFieldService) SpringUtils.getBean(DataFieldServiceImpl.class);
			List<DataField> leftfieldList = testFieldService.findByForeignKey(lefttable_id);
			for (DataField testField : leftfieldList) {
				leftFieldMaps.put(testField.getName(), testField);
			}

			if (rightTableMaps.containsKey(leftkey)) {
				int righttable_id = rightTableMaps.get(leftkey).getId();
				testFieldService = (ITestFieldService) SpringUtils.getBean(DataFieldServiceImpl.class);
				List<DataField> rightfieldList = testFieldService.findByForeignKey(righttable_id);
				if (rightfieldList.size() != 0) {
					for (DataField testField : rightfieldList) {
						rightFieldMaps.put(testField.getName(), testField);
					}
					fieldNameSameflag = compareFieldMap(leftFieldMaps, rightFieldMaps);
				}
			}
		}
		return fieldNameSameflag;
	}

	public boolean compareTable() {
		int tableNameIfSameFlag = 0;
		boolean tableNameSameflag = true;
		try {
			for (String leftkey : leftTableMaps.keySet()) {
				tableNameIfSameFlag = 0;
				if (rightTableMaps.containsKey(leftkey)) {
					tableNameIfSameFlag = 1;
				}
				if (tableNameIfSameFlag == 0) {
					tableNameSameflag = false;
					recordTableNotSame(leftkey);
				}
			}
			for (String rightkey : rightTableMaps.keySet()) {
				tableNameIfSameFlag = 0;
				if (leftTableMaps.containsKey(rightkey)) {
					tableNameIfSameFlag = 1;
				}
				if (tableNameIfSameFlag == 0) {
					tableNameSameflag = false;
					recordTableNotSame(rightkey);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();

		}
		return tableNameSameflag;
	}

	public void recordTableNotSame(String keyValue) {
		TestCase tcase = new TestCase();
//		tcase.setExpertValue("Dict And Table");
		tcase.setName("Dict And Table");
		testCaseService = (ITestCaseService) SpringUtils.getBean(TestCaseServiceImpl.class);
		testCaseService.save(tcase);
		TestResult tresult = new TestResult();
		tresult.setCaseId(String.valueOf(tcase.getId()));
		tresult.setResult("Dict table not same source table");
		tresult.setSecordaryTable(keyValue);
		testReusltService = (ITestReusltService) SpringUtils.getBean(TestResultServiceImpl.class);
		testReusltService.save(tresult);

	}

	public void recordFieldNameNotSame(String keyValue) {
		TestCase tcase = new TestCase();
//		tcase.setExpertValue("Dict field And field");
		tcase.setName("Dict And field");
		testCaseService = (ITestCaseService) SpringUtils.getBean(TestCaseServiceImpl.class);
		testCaseService.save(tcase);
		TestResult tresult = new TestResult();
		tresult.setCaseId(String.valueOf(tcase.getId()));
		tresult.setResult("Dict field not same source field");
		tresult.setSecordaryTable(keyValue);
		testReusltService = (ITestReusltService) SpringUtils.getBean(TestResultServiceImpl.class);
		testReusltService.save(tresult);
		TestResultItem tri = new TestResultItem();
		tri.setTestResultId(tresult.getId());
		tri.setSoruceValue(keyValue);
		testResultItemService = (ITestResultItemService) SpringUtils.getBean(TestResultItemImpl.class);
		testResultItemService.save(tri);
	}

	public void recordFieldPropertyNotSame(String keyValue, EnumFieldPropertyType fieldProperty) {
		TestCase tcase = new TestCase();
//		tcase.setExpertValue("Dict field And field");
		tcase.setName("Dict And field");
		testCaseService = (ITestCaseService) SpringUtils.getBean(TestCaseServiceImpl.class);
		testCaseService.save(tcase);
		TestResult tresult = new TestResult();
		tresult.setCaseId(String.valueOf(tcase.getId()));
		tresult.setResult("Dict field not same source field property");
		tresult.setSecordaryTable(keyValue);
		testReusltService = (ITestReusltService) SpringUtils.getBean(TestResultServiceImpl.class);
		testReusltService.save(tresult);
		TestResultItem tri = new TestResultItem();
		tri.setTestResultId(tresult.getId());
		tri.setSoruceValue(keyValue);
		if (fieldProperty == EnumFieldPropertyType.FiledLengthNotSame) {
			tri.setResult("data_len not same");
		}
		if (fieldProperty == EnumFieldPropertyType.FiledPrecisionNotSame) {
			tri.setResult("data_precision not same");
		}
		if (fieldProperty == EnumFieldPropertyType.FiledTypeNotSame) {
			tri.setResult("data_type not same");
		}
		testResultItemService = (ITestResultItemService) SpringUtils.getBean(TestResultItemImpl.class);
		testResultItemService.save(tri);

	}

	public void recordFieldSame() {
		TestCase tcase = new TestCase();
//		tcase.setExpertValue("field And Dict field");
		tcase.setName("field And Dict field");
		testCaseService = (ITestCaseService) SpringUtils.getBean(TestCaseServiceImpl.class);
		testCaseService.save(tcase);
		TestResult tresult = new TestResult();
		tresult.setCaseId(String.valueOf(tcase.getId()));
		tresult.setResult("field same source Dict field property");
		testReusltService = (ITestReusltService) SpringUtils.getBean(TestResultServiceImpl.class);
		testReusltService.save(tresult);
		TestResultItem tri = new TestResultItem();
		tri.setTestResultId(tresult.getId());
		tri.setResult("field property same");
		testResultItemService = (ITestResultItemService) SpringUtils.getBean(TestResultItemImpl.class);
		testResultItemService.save(tri);
	}

}
