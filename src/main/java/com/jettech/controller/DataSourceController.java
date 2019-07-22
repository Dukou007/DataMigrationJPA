package com.jettech.controller;

import java.util.ArrayList;
import java.util.List;

import javax.websocket.server.PathParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.jettech.EnumDatabaseType;
import com.jettech.entity.DataSource;
import com.jettech.service.IDataSourceService;
import com.jettech.service.IMetaDataManageService;
import com.jettech.vo.DataSourceVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

/**
 * 数据源的对外接口层
 * 
 * @author tan
 *
 */
@RestController
@RequestMapping(value = "/dataSource")
public class DataSourceController {

	private static Logger log = LoggerFactory.getLogger(DataSourceController.class);

	@Autowired
	private IDataSourceService dataSourceService;
	@Autowired
	private IMetaDataManageService metaDataManageService;

	/**
	 * 获得所有的数据源,无参数
	 * 
	 * @return data=List<DataSourceVO>
	 */
	@ResponseBody
	@RequestMapping(value = "/getAllDataSource", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public String getAllDataSource() {
		JSONObject result = new JSONObject();
		try {
			List<DataSource> arr = dataSourceService.findAll();
			List<DataSourceVO> dsvolist = new ArrayList<DataSourceVO>();
			for (int i = 0; i < arr.size(); i++) {
				DataSourceVO dataSourceVO = new DataSourceVO(arr.get(i));
				dsvolist.add(dataSourceVO);
			}
			result.put("result", dsvolist);
			result.put("state", "1");
			log.info("getAllDataSource success");
		} catch (Exception e) {
			result.put("state", "0");
			log.error("getAllDataSource error.", e);
		}

		return result.toJSONString();
	}

	@ResponseBody
	@RequestMapping(value = "/getDataSource", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public String getDataSource(int dataSourceId) {
		JSONObject result = new JSONObject();
		try {
			DataSource entity = dataSourceService.findById(dataSourceId);
			if (entity != null) {
				DataSourceVO vo = new DataSourceVO(entity);
				result.put("result", vo);
				result.put("state", "1");
				log.info("getDataSource success:" + dataSourceId);
			} else {
				result.put("state", "0");
				result.put("result", "not found DataSource by id:" + dataSourceId);
			}
		} catch (Exception e) {
			result.put("state", "0");
			log.error("getAllDataSource error.", e);
		}

		return result.toJSONString();
	}

	@ResponseBody
	@RequestMapping(value = "/getDataSourceByName", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public String getDataSourceByName(String name) {
		JSONObject result = new JSONObject();
		try {
			DataSource entity = dataSourceService.findByName(name);
			DataSourceVO vo = new DataSourceVO(entity);
			result.put("result", vo);
			result.put("state", "1");
			log.info("getDataSourceByName success:" + name);
		} catch (Exception e) {
			result.put("state", "0");
			log.error("getAllDataSource error.", e);
		}

		return result.toJSONString();
	}

	@ResponseBody
	@RequestMapping(value = "/getDataSourceByNameLike", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public String getDataSourceByNameLike(String name) {
		JSONObject result = new JSONObject();
		try {
			List<DataSource> list = dataSourceService.findByNameLike(name);
			List<DataSourceVO> voList = new ArrayList<DataSourceVO>();
			for (int i = 0; i < list.size(); i++) {
				DataSourceVO dataSourceVO = new DataSourceVO(list.get(i));
				voList.add(dataSourceVO);
			}
			result.put("result", voList);
			result.put("state", "1");
			log.info("getDataSourceByName success:" + name);
		} catch (Exception e) {
			result.put("state", "0");
			log.error("getAllDataSource error.", e);
		}

		return result.toJSONString();
	}

	/**
	 * 添加一个数据源
	 * 
	 * @param dsVO
	 *            DataSourceVO
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/addDataSource", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.POST)
	public String addDataSource(@RequestBody DataSourceVO dsVO) {
		JSONObject result = new JSONObject();
		try {

			DataSource ds = new DataSource();
			BeanUtils.copyProperties(dsVO, ds);
			ds.setId(null);
			String type = dsVO.getDatabaseType();
			if (type.equals("Mysql")) {
				ds.setDatabaseType(EnumDatabaseType.Mysql);
			} else if (type.equals("Oracle")) {
				ds.setDatabaseType(EnumDatabaseType.Oracle);
			} else if (type.equals("DB2")) {
				ds.setDatabaseType(EnumDatabaseType.DB2);
			} else if (type.equals("Informix")) {
				ds.setDatabaseType(EnumDatabaseType.Informix);
			} else if (type.equals("SyBase")) {
				ds.setDatabaseType(EnumDatabaseType.SyBase);
			}
			dataSourceService.save(ds);
			result.put("result", "success");
			result.put("state", "1");
			log.info("AddDataSource success:" + ds.getName());
		} catch (Exception e) {
			result.put("state", "0");
			log.error("AddDataSource error.", e);
		}

		return result.toJSONString();
	}

	/**
	 * 修改一个数据源
	 * 
	 * @param dsVO
	 *            DataSourceVO
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/updateDataSource", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.POST)
	public String updateDataSource(@RequestBody DataSourceVO dsvo) {
		JSONObject result = new JSONObject();
		try {
			DataSource ds = new DataSource();
			BeanUtils.copyProperties(dsvo, ds);
			String type = dsvo.getDatabaseType();
			if (type.equals("Mysql")) {
				ds.setDatabaseType(EnumDatabaseType.Mysql);
			} else if (type.equals("Oracle")) {
				ds.setDatabaseType(EnumDatabaseType.Oracle);
			} else if (type.equals("DB2")) {
				ds.setDatabaseType(EnumDatabaseType.DB2);
			} else if (type.equals("Informix")) {
				ds.setDatabaseType(EnumDatabaseType.Informix);
			} else if (type.equals("SyBase")) {
				ds.setDatabaseType(EnumDatabaseType.SyBase);
			}
			metaDataManageService.updateOneDatasource(ds);
			result.put("result", "success");
			result.put("state", "1");
			log.info("updateDataSource success:" + ds.getName());
		} catch (Exception e) {
			result.put("state", "0");
			log.error("updateDataSource error.", e);
		}

		return result.toJSONString();
	}

	/**
	 * 删除一个数据源
	 * 
	 * @param map,包含key=dataSourceId的参数
	 * @return
	 */

	@ResponseBody
	@RequestMapping(value = "/deleteDataSource", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.POST)
	public String deleteDataSource(int dataSourceId) {
		JSONObject result = new JSONObject();
		try {
			DataSource e = dataSourceService.findById(dataSourceId);
			if (e != null) {
				result = dataSourceService.delOneDatasource(e.getId());
				;
			} else {
				result.put("state", "0");
				result.put("result", "not found DataSource by id:" + dataSourceId);
			}
		} catch (Exception e) {
			result.put("state", "0");
			result.put("result", "deleteDataSource error" + e);
			log.error("deleteDataSource error.", e);
		}

		return result.toJSONString();
	}

	/**
	 * 数据源的复制
	 * 
	 * @param testDatabaseVO
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.POST, value = "/copyDataSource")
	public ResultVO copyDataSource(@PathParam(value = "id") Integer id, @PathParam(value = "name") String name) {
		return dataSourceService.copyDataSource(id, name);
	}

	/**
	 * 根据数据库类型返回驱动
	 * 
	 * @param id
	 * @param name
	 * @return
	 */
	@ResponseBody
	@RequestMapping(method = RequestMethod.GET, value = "/getDriver")
	public List<String> getDriver(@PathParam(value = "type") String type) {
		return dataSourceService.getDriver(type);
	}

	/**
	 * 查询所有的支持数据库的类型 20190326
	 * 
	 * @return
	 */
	@RequestMapping(value = "/getAllDataSourceType", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.GET)
	public List<String> getAllDataSourceType() {
		return dataSourceService.selectDataSourceType();
	}

	/**
	 * 同步源下面所有的库表字段
	 * 
	 * @param map
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/setOneMetaData", produces = { "application/json;charset=UTF-8" })
	public ResultVO setOneMetaData(@PathParam(value = "dataSourceId") Integer dataSourceId) {
		try {
			String message = dataSourceService.GetMetaData(dataSourceId);
			if (!message.equals("")) {
				return new ResultVO(false, StatusCode.ERROR, message);
			} else {
				log.info("同步成功");
				return new ResultVO(true, StatusCode.OK, "同步成功");
			}
		} catch (Exception e) {
			log.info("同步失败" + e, e);
			return new ResultVO(false, StatusCode.ERROR, "同步失败");

		}

	}

	/**
	 * 测试新添加的数据源的连通性
	 * 
	 * @return
	 */
	@ResponseBody
	@RequestMapping(value = "/TestDBLinkDataSource", produces = {
	        "application/json;charset=UTF-8" }, method = RequestMethod.POST)
	public String TestDBLinkDataSource(@RequestBody DataSourceVO dsVO) {
		JSONObject result = new JSONObject();
		try {

			DataSource ds = new DataSource();
			BeanUtils.copyProperties(dsVO, ds);
			String type = dsVO.getDatabaseType();
			if (type.equals("Mysql")) {
				ds.setDatabaseType(EnumDatabaseType.Mysql);
			} else if (type.equals("Oracle")) {
				ds.setDatabaseType(EnumDatabaseType.Oracle);
			} else if (type.equals("DB2")) {
				ds.setDatabaseType(EnumDatabaseType.DB2);
			} else if (type.equals("Informix")) {
				ds.setDatabaseType(EnumDatabaseType.Informix);
			} else if (type.equals("SyBase")) {
				ds.setDatabaseType(EnumDatabaseType.SyBase);
			}
			boolean re = dataSourceService.GetDBLink(ds);
			result.put("result", String.valueOf(re));
		} catch (Exception e) {
			e.getLocalizedMessage();
		}

		return JSONObject.toJSONString(result, SerializerFeature.WriteMapNullValue,
		        SerializerFeature.WriteDateUseDateFormat);
	}

	@ResponseBody
	@RequestMapping(value = "/getUrl", produces = { "application/json;charset=UTF-8" }, method = RequestMethod.POST)
	public String getUrl(@RequestBody DataSourceVO dsVO) {
		return dataSourceService.getUrl(dsVO);
	}

}
