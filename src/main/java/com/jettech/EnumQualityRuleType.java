package com.jettech;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public enum EnumQualityRuleType {
	_IS_NULL("空值", "_IS_NULL"),
	_IS_NULL_STR("空字符串", "_IS_NULL_STR"),
	_RANGE_GREAT("范围大于>", "_RANGE_GREAT"),
	_RANGE_GREAT_EQUALS("范围大于等于>=", "_RANGE_GREAT_EQUALS"),
	_RANGE_SMALL("范围小于<", "_RANGE_SMALL"),
	_RANGE_SMALL_EQUALS("范围小于等于<=", "_RANGE_SMALL_EQUALS"),
	_RANGE__EQUALS("范围等于=", "_RANGE__EQUALS"),
	_LENGTH_GREAT("长度大于>", "_LENGTH_GREAT"),
	_LENGTH_GREAT_EQUALS("长度大于等于>=", "_LENGTH_GREAT_EQUALS"),
	_LENGTH_SMALL("长度小于<", "_LENGTH_SMALL"),
	_LENGTH_SMALL_EQUALS("长度小于等于<=", "_LENGTH_SMALL_EQUALS"),
	_LENGTH_EQUALS("长度等于=", "_LENGTH_EQUALS"),
	_CODE_IN("码值包含", "_CODE_IN"),
	_CODE_NOTIN("码值不包含", "_CODE_NOTIN"),
	_NOT_EQUALS("不等于<>", "_NOT_EQUALS"),
	_DEAULT_VALUE("默认值=", "_DEAULT_VALUE"),
	_NOT_FLOAT("非浮点数", "_NOT_FLOAT"),
	_CHAIN_RUPTURE("数仓拉链表断链", "_CHAIN_RUPTURE"),
	_IN_TALBE_COMPARE("表内比较", "_IN_TALBE_COMPARE");

	private String name;
	private String code;
	
	EnumQualityRuleType(String name,String code){
        this.name=name;
        this.code=code;
    }
	
	/**
	 * 将枚举转换成list
	 * @return
	 */
    public static List<Map<String,Object>> toList() {
        List<Map<String,Object>> list = new ArrayList<Map<String,Object>>();
        for (EnumQualityRuleType eqrt: EnumQualityRuleType.values()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("code", eqrt.getCode());
            map.put("name", eqrt.getName());
            list.add(map);
        }
        return list;
    }
    
    /**
     * 判断枚举中是否包含某一个code值
     * @param code
     * @return
     */
    public static boolean contains(String code) {
        for (EnumQualityRuleType eqrt: EnumQualityRuleType.values()) {
        	code.equals(eqrt.getCode());
        }
        return false;
    }
    
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getCode() {
		return code;
	}
	public void setCode(String code) {
		this.code = code;
	}
}
