package com.jettech.domain;

import java.io.Serializable;
import java.util.Date;

import org.springframework.util.StringUtils;

import com.jettech.db.AbstractModel;

/**
 * 模型的数据字段
 * 
 * @author tan
 *
 */
public class DataField extends BaseModel implements Serializable {

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
	private Integer dataLength;

	// 是否是主键
	private Boolean isPrimaryKey;
	private Boolean isForeignKey;
	// 抽取数量
	private Integer samplingCount;

	// 最后采样日期
	private Date lastSamplingDate = null;

	private String label;
	private Integer scale;

	public DataField() {
	}

	public DataField(com.jettech.entity.DataField field) {
		this();
		this.tableName = field.getTalbeName();
		this.columnName = field.getName();
		if (field.getDataTable() != null && field.getDataTable().getDataSchema() != null)
			this.dataBaseName = field.getDataTable().getDataSchema().getName();
		this.dataLength = field.getDataLength();
		this.dataType = field.getDataType();
		this.isPrimaryKey = field.getIsPrimaryKey();
		this.isForeignKey = field.getIsForeignKey();
		this.label = field.getDes();
		this.scale = field.getDataPrecision();

		// col.setTableName(rsmd.getTableName(i));// 表名称
		// col.setColumnName(rsmd.getColumnName(i));// 名称
		// col.setLabel(rsmd.getColumnLabel(i));// 标签
		// col.setDataType(rsmd.getColumnTypeName(i));// 数据类型名称
		// col.setDataLength(rsmd.getPrecision(i));// 长度？
		// col.setScale(rsmd.getScale(i));// 小数精度?
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

	public Integer getDataLength() {

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

	public void setDataLength(Integer dataLength) {

		this.dataLength = dataLength;

	}

	public void setDataType(String dataType) {

		this.dataType = dataType;

	}

	public void setTableName(String tableName) {

		this.tableName = tableName;

	}

	public boolean isKey() {

		return isPrimaryKey;

	}

	public void setKey(boolean isKey) {

		this.isPrimaryKey = isKey;

	}

//	@Override
	public StringBuilder buildInfo() {

		StringBuilder builder = new StringBuilder();

		builder.append("" + columnName);

		builder.append("," + dataType);

		builder.append("," + dataLength);

		if (isPrimaryKey)

			builder.append(",key");

		return builder;

	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public Integer getScale() {
		return scale;
	}

	public void setScale(Integer scale) {
		this.scale = scale;
	}

}
