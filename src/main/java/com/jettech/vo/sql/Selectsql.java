package com.jettech.vo.sql;

import java.util.List;

public class Selectsql {
    List<Selectlist> selectlist;
    List<Fromlist> fromlist;
    boolean distinct;
    List<Grouplist> grouplist;
    List<Havinglist> havinglist;
    List<Joinlist> joinlist;
    List<Leftjoinlist> leftjoinlist;
    List<Wherelist> wherelist;
    List<Orderbylist> orderbylist;
	public List<Selectlist> getSelectlist() {
		return selectlist;
	}
	public void setSelectlist(List<Selectlist> selectlist) {
		this.selectlist = selectlist;
	}
	public List<Fromlist> getFromlist() {
		return fromlist;
	}
	public void setFromlist(List<Fromlist> fromlist) {
		this.fromlist = fromlist;
	}
	public boolean isDistinct() {
		return distinct;
	}
	public void setDistinct(boolean distinct) {
		this.distinct = distinct;
	}
	public List<Grouplist> getGrouplist() {
		return grouplist;
	}
	public void setGrouplist(List<Grouplist> grouplist) {
		this.grouplist = grouplist;
	}
	public List<Havinglist> getHavinglist() {
		return havinglist;
	}
	public void setHavinglist(List<Havinglist> havinglist) {
		this.havinglist = havinglist;
	}
	public List<Joinlist> getJoinlist() {
		return joinlist;
	}
	public void setJoinlist(List<Joinlist> joinlist) {
		this.joinlist = joinlist;
	}
	public List<Leftjoinlist> getLeftjoinlist() {
		return leftjoinlist;
	}
	public void setLeftjoinlist(List<Leftjoinlist> leftjoinlist) {
		this.leftjoinlist = leftjoinlist;
	}
	public List<Wherelist> getWherelist() {
		return wherelist;
	}
	public void setWherelist(List<Wherelist> wherelist) {
		this.wherelist = wherelist;
	}
	public List<Orderbylist> getOrderbylist() {
		return orderbylist;
	}
	public void setOrderbylist(List<Orderbylist> orderbylist) {
		this.orderbylist = orderbylist;
	}
    
}
