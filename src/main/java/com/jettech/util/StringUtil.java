package com.jettech.util;

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
	
}
