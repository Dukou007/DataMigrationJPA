package com.jettech.service;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Pageable;

import com.jettech.vo.FileDataSourceVO;
import com.jettech.vo.ResultVO;


public interface IFileDataSourceService{
	public ResultVO add(FileDataSourceVO fileDataSourceVO);
	public ResultVO delete(Integer id);
	public  ResultVO getAllByPage(String name,Pageable pageable);
	public Map preview(String filePath,String fileName,String fileType,String characterSet,int row,Integer pageNum,Integer pageSize);
	public Map preview(FileDataSourceVO fileDataSourceVO,HttpServletRequest request);

	/**
	 * 创建连接 20190312
	 * @param fileDataSourceVO
	 * @return
	 */
	public ResultVO testConnect(FileDataSourceVO fileDataSourceVO);
	
    public ResultVO copyFileDataSource(int id,String name);
}