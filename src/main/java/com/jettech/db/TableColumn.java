package com.jettech.db;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

public class TableColumn extends AbstractModel implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 7748794220247140708L;
	
	// 来源名称
	private String sourceName;
	
	// 数据库名称
	private String dataBaseName;
	
	// 表名
	private String tableName;
	
	// 字段名称
	private String columnName;
	
	// 数据类型
	private String dataType;
	
	// 数据长度
	private int dataLength;
	
	// 是否是主键
	private boolean isKey;

	// 抽取数量
	private int samplingCount;
	
	// 最后采样日期
	private Date lastSamplingDate;

	public TableColumn() {
		lastSamplingDate = null;
	}

	public Date getLastSamplingDate() {
		return lastSamplingDate;
	}

	public void setLastSamplingDate(Date lastSamplingDate) {
		this.lastSamplingDate = lastSamplingDate;
	}

	public int getSamplingCount() {
		return samplingCount;
	}

	public void setSamplingCount(int samplingCount) {
		this.samplingCount = samplingCount;
	}

	private long getSamplingDataCount() {
		// long rnt = 0;
		// if (dataList != null) {
		// for (DataWithWeight data : dataList) {
		// rnt += data.getCountOfValue();
		// }
		// }
		return samplingCount;
	}

	// public float getSamplingDataWeight(DataWithWeight data) {
	// long samplingDataCount = getSamplingDataCount();
	// if (samplingDataCount > 0) {
	// data.setWeight(data.getCountOfValue() / samplingDataCount);
	// }
	//
	// return data.getWeight();
	// }

	/**
	 * get column's key ,return dataBaseName.tableName.columnName (if
	 * dataBaseName is null/empty return tableName.columnName)
	 * 
	 * @return
	 */
	public String getKey() {
		
		if (null != sourceName && !StringUtils.isEmpty(sourceName)) {
			
			return sourceName + "." + dataBaseName + "." + tableName + "." + columnName;
			
		} else if (null != dataBaseName && !StringUtils.isEmpty(dataBaseName)) {
			
			return dataBaseName + "." + tableName + "." + columnName;
			
		} else
			
			return tableName + "." + columnName;
	}

	/**
	 * return dataBaseName.tableName (if database is null return tableName)
	 * 
	 * @return
	 */
	public String getFullTableName() {
		
		if (null != dataBaseName && !StringUtils.isEmpty(dataBaseName)) {
			
			return dataBaseName + "." + tableName;
			
		} else
			
			return tableName;
		
	}

	public String getSourceName() {
		
		return sourceName;
		
	}
	
	public void setSourceName(String sourceName) {
		
		this.sourceName = sourceName;
		
	}

	public String getDataBaseName() {
		
		return dataBaseName;
		
	}

	public void setDataBaseName(String dataBaseName) {
		
		this.dataBaseName = dataBaseName;
		
	}

	public String getColumnName() {
		
		return columnName;
		
	}

	public int getDataLength() {
		
		return dataLength;
		
	}

	public String getDataType() {
		
		return dataType;
		
	}

	public String getTableName() {
		
		return tableName;
		
	}
	
	public void setColumnName(String columnName) {
		
		this.columnName = columnName;
		
	}

	public void setDataLength(int dataLength) {
		
		this.dataLength = dataLength;
		
	}

	public void setDataType(String dataType) {
		
		this.dataType = dataType;
		
	}

	public void setTableName(String tableName) {
		
		this.tableName = tableName;
		
	}

	public boolean isKey() {
		
		return isKey;
		
	}

	public void setKey(boolean isKey) {
		
		this.isKey = isKey;
		
	}

	@Override
	public StringBuilder buildInfo() {
		
		StringBuilder builder = new StringBuilder();
		
		builder.append("" + columnName);
		
		builder.append("," + dataType);
		
		builder.append("," + dataLength);
		
		if (isKey)
			
			builder.append(",key");
		
		return builder;
		
	}

}
