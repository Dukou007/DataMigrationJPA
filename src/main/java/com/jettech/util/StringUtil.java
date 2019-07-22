package com.jettech.util;

import java.util.List;

public class StringUtil {
    
    /**
     * 随机生成n为大写字符串
     * @param length 生成长度
     * @return
     */
    public static String generateString(int length){
        
        if(length < 1) length = 6;
        
        String str = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        String genStr = "";
        
        for(int index = 0; index < length; index++){
            
            genStr = genStr + str.charAt((int) ((Math.random() * 100) % 26));
            
        }
//        System.out.println(genStr);
        return genStr;
    }
    
	public static String toLowerFirstOne(String string){
		if(Character.isLowerCase(string.charAt(0))){
			return string;
		}else{
			return (new StringBuilder()).append(Character.toLowerCase(string.charAt(0))).append(string.substring(1)).toString();
		}
	}
	
	/*
	 * 判断字符串是否全为中文
	 */
	public static boolean isAllMandarin(String chineseStr) {
		 int n = 0;
	        for(int i = 0; i < chineseStr.length(); i++) {
	            n = (int)chineseStr.charAt(i);
	            if(!(19968 <= n && n <40869)) {
	                return false;
	            }
	        }
	        return true;
	}
	
	public static void main(String[] args) {
				String str1 = "java判断是否为汉字";
		        String str2 = "全为汉字";
		        System.out.println(isAllMandarin(str1));
		        System.out.println(isAllMandarin(str2));
	}
	
	/**
	 * 判断字符串是否全为数字
	 */
	public static boolean isNumeric(String str) {
	        for (int i = str.length(); --i >= 0;) {
	            if (!Character.isDigit(str.charAt(i))) {
	                return false;
	            }
	        }
	        return true;
		}

	/**
	 * 判断集合中的元素是否全是数字
	 * @param list
	 * @return
	 */
	public static boolean isNumeric(List<String> list) {
		boolean numeric = true;
		for (String string : list) {
			numeric= isNumeric(string);
			if(numeric==true) {
				continue;
			}
			return false;
		}

		return true;
		
	}
}
