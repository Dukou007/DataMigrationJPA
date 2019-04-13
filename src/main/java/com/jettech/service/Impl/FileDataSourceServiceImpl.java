package com.jettech.service.Impl;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.io.UnsupportedEncodingException;
import java.net.SocketException;
import java.nio.charset.Charset;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import com.jcraft.jsch.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.jettech.EnumFileType;
import com.jettech.controller.FileDataSourceController;
import com.jettech.entity.FileDataSource;
import com.jettech.entity.DataSchema;
import com.jettech.entity.DataField;
import com.jettech.entity.DataTable;
import com.jettech.repostory.FileDataSourceRepository;
import com.jettech.service.IFileDataSourceService;
import com.jettech.util.ExcelUtil;
import com.jettech.vo.FileDataSourceVO;
import com.jettech.vo.ResultVO;
import com.jettech.vo.StatusCode;

@Service
public class FileDataSourceServiceImpl implements IFileDataSourceService {
	private static Logger log = LoggerFactory
			.getLogger(FileDataSourceServiceImpl.class);

	@Autowired
	private FileDataSourceRepository repository;

	@Override
	public ResultVO add(FileDataSourceVO fileDataSourceVO) {
		FileDataSource fileDataSource = new FileDataSource();
		BeanUtils.copyProperties(fileDataSourceVO, fileDataSource);
		String fileType=fileDataSourceVO.getFileType();
		if(fileType.equals(EnumFileType.CSV.toString())){
			fileDataSource.setFileType(EnumFileType.CSV);
		}else if(fileType.equals(EnumFileType.DBF.toString())){
			fileDataSource.setFileType(EnumFileType.DBF);
		}else if(fileType.equals(EnumFileType.DOC.toString())){
			fileDataSource.setFileType(EnumFileType.DOC);
		}else if(fileType.equals(EnumFileType.JSON.toString())){
			fileDataSource.setFileType(EnumFileType.JSON);
		}else if(fileType.equals(EnumFileType.None.toString())){
			fileDataSource.setFileType(EnumFileType.None);
		}else if(fileType.equals(EnumFileType.TXT.toString())){
			fileDataSource.setFileType(EnumFileType.TXT);
		}else if(fileType.equals(EnumFileType.XLS.toString())){
			fileDataSource.setFileType(EnumFileType.XLS);
		}else if(fileType.equals(EnumFileType.XML.toString())){
			fileDataSource.setFileType(EnumFileType.XML);
		}
		if (fileDataSourceVO.getId() != null) {
			FileDataSource fileDataSource1 = repository.findById(
					fileDataSourceVO.getId()).get();
			repository.update(fileDataSource1.getVersion() + 1,
					fileDataSourceVO.getId(),
					fileDataSourceVO.getCharacterSet(),
					fileDataSourceVO.getConnectionType(),
					fileDataSourceVO.getEditTime(),
					fileDataSourceVO.getEditUser(),
					fileDataSourceVO.getFilePath(),
					fileDataSource.getFileType(), fileDataSourceVO.getHost(),
					fileDataSourceVO.getName(), fileDataSourceVO.getPageSize(),
					fileDataSourceVO.getPassword(),
					fileDataSourceVO.getUsePage(),
					fileDataSourceVO.getUserName());

			return new ResultVO(true, StatusCode.OK, "修改成功");
		} else {
			repository.save(fileDataSource);
			return new ResultVO(true, StatusCode.OK, "添加成功");
		}

	}

	@Override
	public ResultVO delete(Integer id) {
		repository.deleteById(id);
		return new ResultVO(true, StatusCode.OK, "删除成功");
	}

	@Override
	public ResultVO getAllByPage(String name, Pageable pageable) {
		Map<String, Object> map = new HashMap<String, Object>();
		List<FileDataSourceVO> files = new ArrayList<FileDataSourceVO>();
		Page<FileDataSource> fileDataSources = repository.findByNameByPage(
				name, pageable);
		for (FileDataSource fileDataSource : fileDataSources) {
			FileDataSourceVO fileDataSourceVO = new FileDataSourceVO(
					fileDataSource);
			String fileType=fileDataSource.getFileType().toString();
			if(fileType.equals(EnumFileType.CSV.toString())){
				fileDataSourceVO.setFileType(EnumFileType.CSV.toString());
			}else if(fileType.equals(EnumFileType.DBF.toString())){
				fileDataSourceVO.setFileType(EnumFileType.DBF.toString());
			}else if(fileType.equals(EnumFileType.DOC.toString())){
				fileDataSourceVO.setFileType(EnumFileType.DOC.toString());
			}else if(fileType.equals(EnumFileType.JSON.toString())){
				fileDataSourceVO.setFileType(EnumFileType.JSON.toString());
			}else if(fileType.equals(EnumFileType.None.toString())){
				fileDataSourceVO.setFileType(EnumFileType.None.toString());
			}else if(fileType.equals(EnumFileType.TXT.toString())){
				fileDataSourceVO.setFileType(EnumFileType.TXT.toString());
			}else if(fileType.equals(EnumFileType.XLS.toString())){
				fileDataSourceVO.setFileType(EnumFileType.XLS.toString());
			}else if(fileType.equals(EnumFileType.XML.toString())){
				fileDataSourceVO.setFileType(EnumFileType.XML.toString());
			}
			files.add(fileDataSourceVO);
		}
		map.put("totalElements", fileDataSources.getTotalElements());
		map.put("totalPages", fileDataSources.getTotalPages());
		map.put("list", files);
		return new ResultVO(true, StatusCode.OK, "查询成功", map);
	}

	@Override
	public String preview(String filePath, String characterSet, int row) {
		int lastIndex = filePath.lastIndexOf("\\");
		String name = filePath.substring(lastIndex + 1);
		String suffix = name.split("\\.")[1];
		String everyLine = "";
		List<String> allString = new ArrayList<>();
		String line = "";
		if (row == 0) {
			row = 1000;// 如果不传行数则默认查看1000行
		}
		if (suffix.equals("txt")) {
			RandomAccessFile raf = null;
			try {
				raf = new RandomAccessFile(filePath, "rw");
				int linenum = 0;
				while ((line = raf.readLine()) != null && linenum < row) {
					everyLine = new String(line.getBytes("ISO-8859-1"),
							characterSet);
					allString.add(everyLine);
					linenum++;
				}
				raf.close();
			} catch (Exception e) {
				log.error("读取报错", e);
				e.printStackTrace();
			}

		} else if (suffix.equals("csv")) {
			BufferedReader br = null;
			try {
				File csv = new File(filePath);
				// br = new BufferedReader(new FileReader(csv));
				DataInputStream in = new DataInputStream(new FileInputStream(
						csv));

				br = new BufferedReader(new InputStreamReader(in, characterSet));
				int linenum = 0;
				while ((line = br.readLine()) != null && linenum < row) // 读取到的内容给line变量
				{
					everyLine = line;
					System.out.println(everyLine);
					allString.add(everyLine);
					// allString.add("  ");
					linenum++;
				}
				br.close();
				System.out.println("csv表格中所有行数：" + allString.size());
			} catch (Exception e) {
				log.error("读取失败", e);
				e.printStackTrace();
			}
		}
		return JSONObject.toJSONString(allString);
	}

	@Override
	public ResultVO testConnect(FileDataSourceVO fileDataSourceVO) {
		Boolean flag = false;
        String stat = "连接状态";
		if(fileDataSourceVO.getConnectionType()==2){
			flag = sftpConnect(fileDataSourceVO);
			return new ResultVO(flag, StatusCode.OK, "连接状态", flag);
		}else if(fileDataSourceVO.getConnectionType()==1){
			String userName=null;
			String host=null;
			String password = null;
			if(fileDataSourceVO.getUserName()!="" && !fileDataSourceVO.getUserName().equals("")){
				userName = fileDataSourceVO.getUserName();
			}
			if(fileDataSourceVO.getHost()!="" && !fileDataSourceVO.getHost().equals("")){
				host = fileDataSourceVO.getHost();
			}
			if(fileDataSourceVO.getPassword()!="" && !fileDataSourceVO.getPassword().equals("")){
				password = fileDataSourceVO.getHost();
			}
			FTPClient ftpClient = new FTPClient();
			try {
				ftpClient = new FTPClient();
				ftpClient.connect(host, 21);// 连接FTP服务器
				ftpClient.login(userName, password);// 登陆FTP服务器
				if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					System.out.println("未连接到FTP，用户名或密码错误。");
					stat = "未连接到FTP，用户名或密码错误。";
					ftpClient.disconnect();
				} else {
					flag = true;
					System.out.println("FTP连接成功。");
					stat = "FTP连接成功。";
					ftpClient.disconnect();
				}
			} catch (SocketException e) {
				e.printStackTrace();
				System.out.println("FTP的IP地址可能错误，请正确配置。");
				stat = "FTP的IP地址可能错误，请正确配置。";
			} catch (IOException e) {
				e.printStackTrace();
				System.out.println("FTP的端口错误,请正确配置。");
				stat = "FTP的端口错误,请正确配置。";
			}finally {
				return new ResultVO(flag, StatusCode.OK, stat, flag);
			}
			//return new ResultVO(true, StatusCode.OK, "连接状态", flag);
		}else if(fileDataSourceVO.getConnectionType()==3){
			flag = sshConnect(fileDataSourceVO);
			return new ResultVO(flag, StatusCode.OK, "连接状态", flag);
		}
		return new ResultVO(flag, StatusCode.OK, "连接状态", flag);
	}

	public Boolean sftpConnect(FileDataSourceVO fileDataSourceVO) {
		Boolean flag = false;
		Session session;
		try {
			JSch jsch = new JSch();
			/*if (privateKey != ) {
				jsch.addIdentity(privateKey);// 设置私钥
			}*/
			String userName=null;
			String host=null;
			if(fileDataSourceVO.getUserName()!="" && !fileDataSourceVO.getUserName().equals("")){
				userName = fileDataSourceVO.getUserName();
			}
			if(fileDataSourceVO.getHost()!="" && !fileDataSourceVO.getHost().equals("")){
				host = fileDataSourceVO.getHost();
			}
			session = jsch.getSession(userName, host, 22);
			//session = jsch.getSession("root", "192.168.63.128", 22);
			//if (password != ) {
			if(fileDataSourceVO.getPassword() != ""){
				//	session.setPassword("root");
				session.setPassword(fileDataSourceVO.getPassword());
			}
			//}
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftp = (ChannelSftp) channel;
			if(sftp!= null)
				flag = true;

			//关闭sftp
			if (sftp !=null){
				sftp.disconnect();
			}
			if (session!=null) {
				session.disconnect();
			}
		} catch (JSchException e) {
			e.printStackTrace();
		}
		return flag;
	}

	public Boolean sshConnect(FileDataSourceVO fileDataSourceVO) {
		Boolean flag = false;
		String userName = null;
		String host = null;
		String password = null;
		if(fileDataSourceVO.getUserName()!="" && !fileDataSourceVO.getUserName().equals("")){
			userName = fileDataSourceVO.getUserName();
		}else {return false;}
		if(fileDataSourceVO.getHost()!="" && !fileDataSourceVO.getHost().equals("")){
			host = fileDataSourceVO.getHost();
		}else {return false;}
		if(fileDataSourceVO.getPassword() != ""){
			password = fileDataSourceVO.getPassword();
		}else {return false;}
		int returnCode = 0;
		JSch jsch = new JSch();
		Session session;
		try {
			//创建session并且打开连接，因为创建session之后要主动打开连接
			session = jsch.getSession(userName, host, 22);
			session.setPassword(password);
			//关闭主机密钥检查，否则会导致连接失败，重要！！！
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			System.out.println("连接服务器" + session.getHost());
			session.connect();
			//打开通道，设置通道类型，和执行的命令
			Channel channel = session.openChannel("exec");
			ChannelExec channelExec = (ChannelExec) channel;
			if(channelExec!=null){
				flag = true;
			}
			//关闭通道
			channelExec.disconnect();
			//关闭session
			session.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public ResultVO copyFileDataSource(int id, String name) {
		FileDataSource fileDataSource=repository.findById(id).get();
		FileDataSource exiteFileDataSource=repository.findByName(name);
		if(exiteFileDataSource!=null){
			return new ResultVO(false, StatusCode.ERROR, "名称重复");
		}
		FileDataSource copyfileDataSource=new FileDataSource();
		BeanUtils.copyProperties(fileDataSource, copyfileDataSource);
		copyfileDataSource.setCreateTime(new Date());
		copyfileDataSource.setId(null);
		copyfileDataSource.setName(name);
		repository.save(copyfileDataSource);

		return new ResultVO(true, StatusCode.OK, "复制成功");

	}

	

}