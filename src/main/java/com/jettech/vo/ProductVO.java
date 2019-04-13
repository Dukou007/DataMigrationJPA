package com.jettech.vo;


import com.jettech.entity.BaseEntity;
import com.jettech.entity.Product;

public class ProductVO extends BaseVO {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1253542586274952204L;

	private String name;

	private Integer parentID;
	private String parentName;
//	
//	private List<ProductVO> subProducts ;
//	
//
//	public List<ProductVO> getSubProducts() {
//		return subProducts;
//	}

//	public void setSubProducts(List<ProductVO> subProducts) {
//		this.subProducts = subProducts;
//	}

	public ProductVO() {
	}

	public ProductVO(BaseEntity entity) {
		super(entity);
		if (entity != null) {
			Product e = (Product) entity;
			if (e.getParent() != null) {
				this.parentID = e.getParent().getId();
				this.setParentName(e.getParent().getName());
			}
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Integer getParentID() {
		return parentID;
	}

	public void setParentID(Integer parentID) {
		this.parentID = parentID;
	}

	public String getParentName() {
		return parentName;
	}

	public void setParentName(String parentName) {
		this.parentName = parentName;
	}

}
