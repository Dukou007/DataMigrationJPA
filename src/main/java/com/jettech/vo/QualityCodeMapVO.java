package com.jettech.vo;


public class QualityCodeMapVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = 713579784722948727L;
	private String name;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public QualityCodeMapVO(String name) {
		super();
		this.name = name;
	}
	public QualityCodeMapVO() {
		super();
	}
	
}
