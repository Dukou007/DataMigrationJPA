package com.jettech.thread;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.jettech.entity.DataSource;
import com.jettech.entity.DataField;
import com.jettech.entity.TestQuery;
import com.jettech.entity.TestQueryField;
import com.jettech.entity.TestRule;

public class FiledConvert {

	public void filedConvert(TestQuery testQuery){
		DataSource dataSource = testQuery.getDataSource();
		String sqlText = testQuery.getSqlText();
		List<TestQueryField> testFields = testQuery.getTestFields();
		//List<TestField> keyFields = testQuery.getKeyFields();
		List<TestRule> testRules = testQuery.getTestRules();
		for(TestQueryField tf:testFields){
			this.keyWordConvert(sqlText, tf, testRules, dataSource);
		}
	}
	
	public void keyWordConvert(String sqlText,TestQueryField tf,List<TestRule> testRules,DataSource dataSource){
		String databaseType = dataSource.getDatabaseType().name();//数据库类型
		String returnStr = "";
		if(sqlText.contains(tf.getName())){
			returnStr = this.getConvertMethod(tf, testRules, databaseType);
			sqlText.replace(tf.getName(), returnStr);//把sql关键字段按规则转换
		}
	}
	
	private String getConvertMethod(TestQueryField tf,List<TestRule> testRules,String databaseType){
		String returnStr = "";
		//String ruleStr = rule.getRule().toString();
		for(TestRule rule:testRules){
			String ruleStr = rule.getRule().toString();
			switch(ruleStr){
				case "CodeMap":
					Map<String, String> converCodeMap = rule.getConverCodeMap();
					Iterator<Map.Entry<String, String>> ccEntries = converCodeMap.entrySet().iterator();
					//Map.Entry<String, Map<String, String>> sourceEntry = ccEntries.next();
					String caseWhen = "case";
					while (ccEntries.hasNext()) {
						Map.Entry<String, String> ccEntry = ccEntries.next();
						String key = ccEntry.getKey();
						String value = ccEntry.getValue();
						caseWhen += " when "+tf.getName()+"="+key+" then "+value;
					}
					returnStr += caseWhen+" else 0 end";
				    break;
				case "Default":
					returnStr = rule.getRuleValue();
				    break;
				case "DateFormat":
					if("mysql".equals(databaseType)){
						returnStr = "date_format("+tf.getName()+","+rule.getDateFormat()+")";
					}else if("oracle".equals(databaseType)){
						returnStr = "to_char("+tf.getName()+","+rule.getDateFormat()+")";
					}
				    break;
				case "SubStr":
					if("mysql".equals(databaseType)){
						returnStr = "substring("+tf.getName()+","+rule.getPosition()+")";
					}else if("oracle".equals(databaseType)){
						returnStr = "substr("+tf.getName()+","+rule.getPosition()+")";
					}
				    break;
				case "LeftTrim":
					returnStr = "ltrim("+tf.getName()+")";
				    break;
				case "RightTrim":
					returnStr = "rtrim("+tf.getName()+")";
				    break;
				case "Trim":
					if("mysql".equals(databaseType)){
						returnStr = "trim("+tf.getName()+")";
					}else if("oracle".equals(databaseType)){
						returnStr = "ltrim(rtrim("+tf.getName()+"))";
					}
				    break;
				case "LeftPad":
						returnStr = "lpad("+tf.getName()+","+tf.getDataLength()+","+rule.getRuleValue()+"";
				    break;
				case "RightPad":
						returnStr = "rpad("+tf.getName()+","+tf.getDataLength()+","+rule.getRuleValue()+"";
				    break;
				case "Nvl":
					if("mysql".equals(databaseType)){
						returnStr = "ifnull("+tf.getName()+","+rule.getRuleValue()+")";
					}else if("oracle".equals(databaseType)){
						returnStr = "nvl("+tf.getName()+","+rule.getRuleValue()+")";
					}
				    break;
				case "Query":
					returnStr = rule.getRuleValue();
				    break;
			}
		}
		return returnStr;//返回转换完的字段信息
	}
	
}
