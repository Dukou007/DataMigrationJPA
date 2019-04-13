package com.jettech.vo;

import java.io.Serializable;

/**
 * 结果集封装
 * @author Administrator
 *
 */
public class ResultVO implements Serializable{

    /**
	 * 
	 */
	private static final long serialVersionUID = 23265402163736205L;
	private boolean flag;//是否成功  true和false
    private int code;//执行结果状态码
    private String message;//执行结果的信息提示
    private Object data;//响应的数据 （此数据不是什么操作都有。例如：查询带有结果集，增删改就没有）

    public ResultVO(){

    }

    /**
     * 增删改方法使用
     * @param flag
     * @param code
     * @param message
     */
    public ResultVO(boolean flag, int code, String message) {
        this.flag = flag;
        this.code = code;
        this.message = message;
    }

    /**
     * 查询方法使用
     * @param flag
     * @param code
     * @param message
     * @param data
     */
    public ResultVO(boolean flag, int code, String message, Object data) {
        this.flag = flag;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public boolean isFlag() {
        return flag;
    }

    public void setFlag(boolean flag) {
        this.flag = flag;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
