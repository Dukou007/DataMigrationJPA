package com.jettech.domain;

public class CompareResult {
	private Long sourceCount;
	private Long targetCount;
	
	private Long sameRow;
	private Long notSameData;
	private Long notSameRow;

	public CompareResult(Long sameRow2, Long notSameData) {
		this.setSameRow(sameRow2);
		this.setNotSameData(notSameData);
	}

	public CompareResult(Long sameRow2, Long notSameData, Long notSameRow) {
		this.sameRow=sameRow2;
		this.notSameData=notSameData;
		this.notSameRow=notSameRow;
	}
	public Long getSourceCount() {
		return sourceCount;
	}
	public void setSourceCount(Long sourceCount) {
		this.sourceCount = sourceCount;
	}
	public Long getTargetCount() {
		return targetCount;
	}
	public void setTargetCount(Long targetCount) {
		this.targetCount = targetCount;
	}
	public Long getNotSameRow() {
		return notSameRow;
	}

	public void setNotSameRow(Long notSameRow) {
		this.notSameRow = notSameRow;
	}

	public Long getSameRow() {
		return sameRow;
	}

	public void setSameRow(Long sameRow) {
		this.sameRow = sameRow;
	}

	public Long getNotSameData() {
		return notSameData;
	}

	public void setNotSameData(Long notSameData) {
		this.notSameData = notSameData;
	}
}
