package com.jettech.entity;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

/**
 * 元数据更改历史记录
 * @author tan
 *
 */
@Entity
@Table(name = "meta_history")
public class MetaHistory extends BaseEntity {

	/**
	 * 
	 */
	private static final long serialVersionUID = -514398365048785878L;

	private DataSchema testDatabase;
	private Date startTime;
	private Date endTime;
	private List<MetaHistoryItem> items;
	
	public Date getStartTime() {
		return startTime;
	}
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "test_database_id")
	public DataSchema getDataSchema() {
		return testDatabase;
	}

	public void setDataSchema(DataSchema testDatabase) {
		this.testDatabase = testDatabase;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	public Date getEndTime() {
		return endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@OneToMany(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
	@JoinColumn(name = "meta_history_id", referencedColumnName = "id")
	public List<MetaHistoryItem> getItems() {
		return items;
	}

	public void setItems(List<MetaHistoryItem> items) {
		this.items = items;
	}
}
