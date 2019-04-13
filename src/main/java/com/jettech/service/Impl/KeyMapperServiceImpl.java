package com.jettech.service.Impl;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jettech.entity.CompFields;
import com.jettech.util.ExcelReader;
import com.jettech.repostory.CompFieldsRepository;
import com.jettech.service.IKeyMapperService;

@Service
public class KeyMapperServiceImpl implements IKeyMapperService {

	@Autowired
	private ExcelReader excelReader;

	@Autowired
	private CompFieldsRepository compFieldsRepository;

	@Override
	public void loadKeyMapper(String filePath) {

		List<List<Map<String, Object>>> sheetList = excelReader.read(filePath, CompFields.class, 0);
		// System.out.println(keyList);
		for (List<Map<String, Object>> keyList : sheetList) {

			for (Map<String, Object> map : keyList) {
				if (map.get("newTableName") != null && !map.get("newTableName").toString().equals("")
						&& map.get("newKeyField") != null && !map.get("newKeyField").toString().equals("")
						&& map.get("oldTableName") != null && !map.get("oldTableName").toString().equals("")
						&& map.get("oldKeyField") != null && !map.get("oldKeyField").toString().equals("")) {

					CompFields compField = new CompFields();
					compField.setNewTableName(map.get("newTableName").toString());
					compField.setNewKeyField(map.get("newKeyField").toString());
					compField.setOldTableName(map.get("oldTableName").toString());
					compField.setOldKeyField(map.get("oldKeyField").toString());
					compFieldsRepository.save(compField);
				} 

			}
		}
	}

}
