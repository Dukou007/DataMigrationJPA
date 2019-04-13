package com.jettech.thread;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jettech.entity.CodeMap;
import com.jettech.entity.CodeMapItem;
import com.jettech.entity.DataField;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestQueryField;
import com.jettech.entity.TestRule;
import com.jettech.util.DateUtil;

public class FiledValueConvert {

	public void filedConvert(TestQuery testQuery,Map<String, List<Object>> map){
		
		List<TestQueryField> testFields = testQuery.getTestFields();
		List<TestRule> testRules = testQuery.getTestRules();
		
		Iterator<Map.Entry<String, List<Object>>> entries = map.entrySet().iterator();
		while (entries.hasNext()) {
			Map.Entry<String, List<Object>> valueEntry = entries.next();
			List<Object> valueList = valueEntry.getValue();
			for(int x = 0;x < valueList.size();x++){//理论上valueList集合数据个数等于testFields集合数据个数
				for(int i = 0;i < testFields.size();i++){
					Object obj = valueList.get(x);
					out:for(int j = 0;j < testRules.size();j++){
						if(testFields.get(i).getId() != null && testRules.get(j).getDataField() != null && testFields.get(i).getId() == testRules.get(j).getDataField().getId()){
							String ruleStr = testRules.get(j).getRule().toString();
							switch(ruleStr){
								case "CodeMap":
									CodeMap cm = testRules.get(j).getCodeMap();
									List<CodeMapItem> items = cm.getItems();
									for(CodeMapItem item:items){
										if(obj.toString().equals(item.getOrgValue())){
											obj = item.getRefValue();
											break;
										}
									}
								    break out;
								case "Default":
									obj = testRules.get(j).getRuleValue();
									break out;
								case "DateFormat":
									obj = DateUtil.getDateStr(new Date(obj.toString()),testRules.get(j).getRuleValue());
									break out;
								case "SubStr":
									obj = obj.toString().substring(testRules.get(j).getPosition());
									break out;
								case "LeftTrim":
									obj = obj.toString().replaceAll("^[ ]*","");
									break out;
								case "RightTrim":
									obj = obj.toString().replaceAll("[ ]*$","");
									break out;
								case "Trim":
									obj = obj.toString().replaceAll(" ", "");
									break out;
								case "LeftPad":
									obj = testRules.get(j).getRuleValue()+obj.toString();
									break out;
								case "RightPad":
									obj = obj.toString()+testRules.get(j).getRuleValue();
									break out;
								case "Nvl":
									if(obj == null || "".equals(obj.toString())){
										obj = testRules.get(j).getRuleValue();
									}
									break out;
								case "Query":
									break out;
							}
						}
					}
				}
			}
		}
	}

}
