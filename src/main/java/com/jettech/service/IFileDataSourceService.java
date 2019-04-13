package com.jettech.service;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.jettech.entity.FileDataSource;
import com.jettech.vo.FileDataSourceVO;
import com.jettech.vo.ResultVO;


public interface IFileDataSourceService{
	public ResultVO add(FileDataSourceVO fileDataSourceVO);
	public ResultVO delete(Integer id);
	public  ResultVO getAllByPage(String name,Pageable pageable);
	public String preview(String filePath,String characterSet,int row);

	/**
	 * 创建连接 20190312
	 * @param fileDataSourceVO
	 * @return
	 */
	public ResultVO testConnect(FileDataSourceVO fileDataSourceVO);
	
    public ResultVO copyFileDataSource(int id,String name);
}