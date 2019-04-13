package com.jettech.service;

import java.util.List;

import com.jettech.entity.TestCase;

public interface IMapperService {

	/**
	 * 
	 * @param filePath
	 *            服务器是上需要处理的文件名称(Excel)
	 * @param clazz
	 *            映射的Mapper实体类型
	 * @throws Exception
	 */
	public void loadMapper(String filePath, Class clazz) throws Exception;
	
	
}
