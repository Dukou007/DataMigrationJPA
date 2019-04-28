package com.jettech.service.Impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.RandomAccessFile;
import java.net.SocketException;
import java.util.*;

import javax.servlet.http.HttpServletRequest;

import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.SCPClient;
import ch.ethz.ssh2.StreamGobbler;

import com.jcraft.jsch.*;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPFile;
import org.apache.commons.net.ftp.FTPReply;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.jettech.EnumFileType;
import com.jettech.entity.FileDataSource;
import com.jettech.repostory.FileDataSourceRepository;
import com.jettech.service.IFileDataSourceService;
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
		String fileType = fileDataSourceVO.getFileType();
		if (fileType.equals(EnumFileType.CSV.toString())) {
			fileDataSource.setFileType(EnumFileType.CSV);
		} else if (fileType.equals(EnumFileType.DBF.toString())) {
			fileDataSource.setFileType(EnumFileType.DBF);
		} else if (fileType.equals(EnumFileType.DOC.toString())) {
			fileDataSource.setFileType(EnumFileType.DOC);
		} else if (fileType.equals(EnumFileType.JSON.toString())) {
			fileDataSource.setFileType(EnumFileType.JSON);
		} else if (fileType.equals(EnumFileType.None.toString())) {
			fileDataSource.setFileType(EnumFileType.None);
		} else if (fileType.equals(EnumFileType.TXT.toString())) {
			fileDataSource.setFileType(EnumFileType.TXT);
		} else if (fileType.equals(EnumFileType.XLS.toString())) {
			fileDataSource.setFileType(EnumFileType.XLS);
		} else if (fileType.equals(EnumFileType.XML.toString())) {
			fileDataSource.setFileType(EnumFileType.XML);
		}
		FileDataSource exitesfileDataSource = repository
				.findByName(fileDataSourceVO.getName());

		if (fileDataSourceVO.getId() != null) {
			if (exitesfileDataSource != null
					&& exitesfileDataSource.getId() != fileDataSourceVO.getId()) {
				return new ResultVO(false, StatusCode.ERROR, "名称重复");
			}
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
					fileDataSourceVO.getUserName(),
					fileDataSourceVO.getFileName());
			return new ResultVO(true, StatusCode.OK, "修改成功");
		} else {
			if (exitesfileDataSource != null) {
				return new ResultVO(false, StatusCode.ERROR, "名称重复");
			}
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
			String fileType = fileDataSource.getFileType().toString();
			if (fileType.equals(EnumFileType.CSV.toString())) {
				fileDataSourceVO.setFileType(EnumFileType.CSV.toString());
			} else if (fileType.equals(EnumFileType.DBF.toString())) {
				fileDataSourceVO.setFileType(EnumFileType.DBF.toString());
			} else if (fileType.equals(EnumFileType.DOC.toString())) {
				fileDataSourceVO.setFileType(EnumFileType.DOC.toString());
			} else if (fileType.equals(EnumFileType.JSON.toString())) {
				fileDataSourceVO.setFileType(EnumFileType.JSON.toString());
			} else if (fileType.equals(EnumFileType.None.toString())) {
				fileDataSourceVO.setFileType(EnumFileType.None.toString());
			} else if (fileType.equals(EnumFileType.TXT.toString())) {
				fileDataSourceVO.setFileType(EnumFileType.TXT.toString());
			} else if (fileType.equals(EnumFileType.XLS.toString())) {
				fileDataSourceVO.setFileType(EnumFileType.XLS.toString());
			} else if (fileType.equals(EnumFileType.XML.toString())) {
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
	public Map preview(String filePath, String fileName,
			String fileType, String characterSet, int row, Integer pageNum,
			Integer pageSize) {
		List<String> r = new ArrayList<String>();
		// 读取这个目录下的文件
		File file = new File(filePath);
		//List<String> result = new ArrayList<String>();
		Map result=new HashMap<>();
		//Map result1=new HashMap<>();

		if (!file.isDirectory()) {
			log.info("文件" + "路径：" + file.getPath() + "绝对路径："
					+ file.getAbsolutePath());
			if (fileName.contains(file.getName())) {// 如果这个文件名称符合要找的文件则读取文件
				return readFile(file.getPath(), fileType, characterSet, row,
						pageNum, pageSize);
			}
		} else if (file.isDirectory()) {
			System.out.println("文件夹");
			String[] filelist = file.list();
			for (int i = 0; i < filelist.length; i++) {
				File readfile = new File(filePath + "\\" + filelist[i]);
				if (!readfile.isDirectory()) {
					log.info("文件" + "路径：" + file.getPath() + "绝对路径："
							+ file.getAbsolutePath());
					// 获取这个文件的后缀名
					String suffix = readfile.getName().split("\\.")[1];
					String name = readfile.getName().split("\\.")[0];
					if (suffix.equals(fileType)) {
						// 判断包不包含通配符
						if (fileName.contains("?")) {
							fileName = fileName.replace("?", "*");
						}
						if (fileName.contains("*")
								&& (!fileName.startsWith("^") && !fileName
										.endsWith("$"))) {
							String namesuffix = fileName.split("\\*")[0];
							if (name.contains(namesuffix)) {// 如果这个文件名称符合要找的文件则读取文件
								result = readFile(readfile.getPath(), fileType,
										characterSet, row, pageNum, pageSize);
							//	result1.put("map"+i, result);
							}

						} else if (fileName.startsWith("^")
								&& fileName.endsWith("$")) {// 判断是不是正则表达式
							if (name.matches(fileName)) {
								result = readFile(readfile.getPath(), fileType,
										characterSet, row, pageNum, pageSize);
								//result1.put("map"+i, result);

							}
						} else {// 正常的文件名,精确匹配
							if (fileName.equals(name)) {// 如果这个文件名称符合要找的文件则读取文件
								result = readFile(readfile.getPath(), fileType,
										characterSet, row, pageNum, pageSize);
								//result1.put("map"+i, result);
							}
						}

					}

				}
			}

		}
		log.info("输出内容为" + result);
		return result;
	}

	public Map readFile(String filePath, String fileType,
			String characterSet, int row, Integer pageNum, Integer pageSize) {
		Map result=new HashMap<>();
		int flag=0;
		int first = 0;
		if (row == 0) {
			row = 1000;// 如果不传行数则默认查看1000行
		}
		int linenum = 1;
		int lines = 0;
		if (pageSize != null) {
			first = (pageNum - 1) * pageSize + 1;
		}
		if(fileType.equalsIgnoreCase("csv")){
			flag=1;
		}
		if (fileType.equals("txt") || fileType.equals("csv")) {
			result = readTxt(filePath, characterSet, row, pageNum, pageSize,
					first, linenum, lines);

		}
		result.put("flag", flag);
		return result;
	}

	private Map readTxt(String filePath, String characterSet, int row,
			Integer pageNum, Integer pageSize, int first, int linenum, int lines) {
		String everyLine;
		String line;
		RandomAccessFile raf = null;
		List<String> allString = new ArrayList<>();
		Map result=new HashMap<>();
		try {
			// 若为txt格式先转为csv格式
			int index = filePath.lastIndexOf("\\");
			String type = filePath.substring(index + 1).split("\\.")[1];
			String name = filePath.substring(index + 1).split("\\.")[0];
			StringBuilder s = new StringBuilder();
			raf = new RandomAccessFile(filePath, "rw");
			while ((line = raf.readLine()) != null && linenum <= row) {
				if (pageSize == null) {// 不分页

					everyLine = new String(line.getBytes("ISO-8859-1"),
							characterSet);
					log.info("第" + linenum + "行的内容" + everyLine);
					allString.add(everyLine);
					linenum++;
				} else {
					// 分页,//读指定行的数据
					lines++;
					int end = pageNum * pageSize;
					if (lines == first && lines <= end) {
						everyLine = new String(line.getBytes("ISO-8859-1"),
								characterSet);
						log.info("第" + linenum + "行的内容" + everyLine);
						allString.add(everyLine);
						first++;
					}
					linenum++;
				}

			}
			raf.close();
			int totalLine = 0;
			int totalPage = 0;
			if (pageSize == null) {
				totalPage = 1;
				if (row < linenum) {
					totalLine = row;
				} else {
					totalLine = linenum - 1;
				}
			} else {
				if (row < linenum) {
					totalLine = row;
				} else {
					totalLine = linenum - 1;
				}
				totalPage = totalLine % pageSize == 0 ? (totalLine / pageSize)
						: (totalLine / pageSize) + 1;
			}
			result.put("list",allString);
			result.put("totalLine", totalLine);
			result.put("totalPage", totalPage);
			result.put("msg", "读取成功");
		} catch (Exception e) {
			log.error("读取报错", e);
			e.printStackTrace();
		}
		return result;
	}

	@Override
	public ResultVO testConnect(FileDataSourceVO fileDataSourceVO) {
		Boolean flag = false;
		String stat = "连接状态";
		if (fileDataSourceVO.getConnectionType() == 2) {
			flag = sftpConnect(fileDataSourceVO);
			return new ResultVO(flag, StatusCode.OK, "连接状态", flag);
		} else if (fileDataSourceVO.getConnectionType() == 1) {
			String userName = null;
			String host = null;
			String password = null;
			if (fileDataSourceVO.getUserName() != ""
					&& !fileDataSourceVO.getUserName().equals("")) {
				userName = fileDataSourceVO.getUserName();
			}
			if (fileDataSourceVO.getHost() != ""
					&& !fileDataSourceVO.getHost().equals("")) {
				host = fileDataSourceVO.getHost();
			}
			if (fileDataSourceVO.getPassword() != ""
					&& !fileDataSourceVO.getPassword().equals("")) {
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
			} finally {
				return new ResultVO(flag, StatusCode.OK, stat, flag);
			}
		} else if (fileDataSourceVO.getConnectionType() == 3) {
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
			/*
			 * if (privateKey != ) { jsch.addIdentity(privateKey);// 设置私钥 }
			 */
			String userName = null;
			String host = null;
			if (fileDataSourceVO.getUserName() != ""
					&& !fileDataSourceVO.getUserName().equals("")) {
				userName = fileDataSourceVO.getUserName();
			}
			if (fileDataSourceVO.getHost() != ""
					&& !fileDataSourceVO.getHost().equals("")) {
				host = fileDataSourceVO.getHost();
			}
			session = jsch.getSession(userName, host, 22);
			// session = jsch.getSession("root", "192.168.63.128", 22);
			// if (password != ) {
			if (fileDataSourceVO.getPassword() != "") {
				// session.setPassword("root");
				session.setPassword(fileDataSourceVO.getPassword());
			}
			// }
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			Channel channel = session.openChannel("sftp");
			channel.connect();
			ChannelSftp sftp = (ChannelSftp) channel;
			if (sftp != null)
				flag = true;

			// 关闭sftp
			if (sftp != null) {
				sftp.disconnect();
			}
			if (session != null) {
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
		if (fileDataSourceVO.getUserName() != ""
				&& !fileDataSourceVO.getUserName().equals("")) {
			userName = fileDataSourceVO.getUserName();
		} else {
			return false;
		}
		if (fileDataSourceVO.getHost() != ""
				&& !fileDataSourceVO.getHost().equals("")) {
			host = fileDataSourceVO.getHost();
		} else {
			return false;
		}
		if (fileDataSourceVO.getPassword() != "") {
			password = fileDataSourceVO.getPassword();
		} else {
			return false;
		}
		int returnCode = 0;
		JSch jsch = new JSch();
		Session session;
		try {
			// 创建session并且打开连接，因为创建session之后要主动打开连接
			session = jsch.getSession(userName, host, 22);
			session.setPassword(password);
			// 关闭主机密钥检查，否则会导致连接失败，重要！！！
			Properties config = new Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			System.out.println("连接服务器" + session.getHost());
			session.connect();
			// 打开通道，设置通道类型，和执行的命令
			Channel channel = session.openChannel("exec");
			ChannelExec channelExec = (ChannelExec) channel;
			if (channelExec != null) {
				flag = true;
			}
			// 关闭通道
			channelExec.disconnect();
			// 关闭session
			session.disconnect();
		} catch (JSchException e) {
			e.printStackTrace();
		}
		return flag;
	}

	@Override
	public ResultVO copyFileDataSource(int id, String name) {
		FileDataSource fileDataSource = repository.findById(id).get();
		FileDataSource exiteFileDataSource = repository.findByName(name);
		if (exiteFileDataSource != null) {
			return new ResultVO(false, StatusCode.ERROR, "名称重复");
		}
		FileDataSource copyfileDataSource = new FileDataSource();
		BeanUtils.copyProperties(fileDataSource, copyfileDataSource);
		copyfileDataSource.setCreateTime(new Date());
		copyfileDataSource.setId(null);
		copyfileDataSource.setName(name);
		repository.save(copyfileDataSource);

		return new ResultVO(true, StatusCode.OK, "复制成功");

	}

	@Override
	public Map preview(FileDataSourceVO fileDataSourceVO,
			HttpServletRequest request) {
		// 获取连接类型连接协议类型 1:ftp,2:sftp,3:ssh
		Map result=new HashMap<>();
		int flag=0;//0表示txt,1表示csv
		List<String> allString = new ArrayList<>();
		int contentType = fileDataSourceVO.getConnectionType();
		String localfile = request.getSession(true).getServletContext()
				.getRealPath("");
		String fileType = fileDataSourceVO.getFileType().toLowerCase();
		String filePath = fileDataSourceVO.getFilePath();
		String fileName = fileDataSourceVO.getFileName();
		String characterSet = fileDataSourceVO.getCharacterSet();
		Integer row = fileDataSourceVO.getRow();
		String userName = fileDataSourceVO.getUserName();
		String password = fileDataSourceVO.getPassword();
		String host = fileDataSourceVO.getHost();
		Integer pageNum = fileDataSourceVO.getPageNum();
		Integer pageSize = fileDataSourceVO.getPageSize();
		if(fileType.equalsIgnoreCase("csv")){
			flag=1;
		}
		try {
			if (contentType == 3 || contentType == 2) {
				JSch jsch = new JSch();
				Session session;
				session = jsch.getSession(userName, host, 22);
				session.setPassword(password);
				// 关闭主机密钥检查，否则会导致连接失败，重要！！！
				Properties config = new Properties();
				config.put("StrictHostKeyChecking", "no");
				session.setConfig(config);
				System.out.println("连接服务器" + session.getHost());
				session.connect();
				String path = readServerFile(userName, password, fileType,
						filePath, fileName, host, contentType, request,
						localfile, session);
				if (path == null) {
					String info = "在此路径及其子路径下没有找到相应的文件";
					allString.add(info);
					result.put("msg", info);
					result.put("list",allString);
					result.put("totalLine", null);
					result.put("totalPage", null);
					result.put("flag", flag);
					
				}
				result = preview(localfile, fileName, fileType,
						characterSet, row, pageNum, pageSize);
				// 关闭sftp
				session.disconnect();

			} else if (contentType == 1) {// ftp协议,直接读取无需下载
				FTPClient ftpClient = new FTPClient();
				ftpClient.connect(host, 8010);// 连接FTP服务器
				ftpClient.login(userName, password);// 登陆FTP服务器
				if (!FTPReply.isPositiveCompletion(ftpClient.getReplyCode())) {
					log.info("連接失敗");
				}
				if (filePath.startsWith("/") && filePath.endsWith("/")) {
					String directory = fileDataSourceVO.getFilePath();
					// 更换目录到当前目录
					ftpClient.changeWorkingDirectory(directory);
					ftpClient.enterLocalPassiveMode();
					FTPFile[] files = ftpClient.listFiles();
					System.out.println(files);
					if (fileName.contains("?")) {
						fileName = fileName.replace("?", "*");
					}
					if (fileName.contains("*")) {
						fileName = fileName.split("\\*")[0];
					}
					if (files != null) {
						// StringBuilder builder = new StringBuilder();
						for (int i = 0; i < files.length; i++) {
							if (files[i].isFile()
									&& files[i].getName().contains(fileName)
									&& files[i].getName().contains(fileType)) {
								InputStream in = ftpClient
										.retrieveFileStream(files[i].getName());
								BufferedReader br = new BufferedReader(
										new InputStreamReader(in, characterSet));
								String line = null;
								String everyLine = null;
								int first = 0;
								if (row == 0) {
									row = 1000;// 如果不传行数则默认查看1000行
								}
								int linenum = 1;
								int lines = 0;
								if (pageSize != null) {
									first = (pageNum - 1) * pageSize + 1;
								}
								while ((line = br.readLine()) != null
										&& linenum <= row) {
									if (pageSize == null) {// 不分页
										everyLine = new String(
												line.getBytes(characterSet),
												characterSet);
										log.info("第" + linenum + "行的内容"
												+ everyLine);
										allString.add(everyLine);
										linenum++;
										
									} else {
										// 分页,//读指定行的数据
										lines++;
										int end = pageNum * pageSize;
										if (lines == first && lines <= end) {
											everyLine = new String(
													line.getBytes(characterSet),
													characterSet);
											log.info("第" + linenum + "行的内容"
													+ everyLine);
											allString.add(everyLine);
											first++;
											
										}
										linenum++;
									}

								}
								br.close();
								int totalLine = 0;
								int totalPage = 0;
								if (pageSize == null) {
									totalPage = 1;
									if (row < linenum) {
										totalLine = row;
									} else {
										totalLine = linenum - 1;
									}
								} else {
									if (row < linenum) {
										totalLine = row;
									} else {
										totalLine = linenum - 1;
									}
									totalPage = totalLine % pageSize == 0 ? (totalLine / pageSize)
											: (totalLine / pageSize) + 1;
								}
								result.put("list",allString);
								result.put("totalLine", totalLine);
								result.put("totalPage", totalPage);
								result.put("flag", flag);
								result.put("msg", "读取成功");
								if (in != null) {
									in.close();
								}
								// 主动调用一次getReply()
								// 这样做是可以解决这个返回null问题
								ftpClient.getReply();

							}
						}
						return result;
					}
				}

			}
		} catch (Exception e) {
			log.info("发生异常" + e);
			result.put("msg", "连接超时"+e);
		}
		return result;
	}

	public String readServerFile(String userName, String password,
			String fileType, String filePath, String fileName, String host,
			Integer connectionType, HttpServletRequest request,
			String localfile, Session session) {
		Connection conn = new Connection(host, 22);
		ch.ethz.ssh2.Session ssh = null;
		boolean flag = false;
		try {
			conn.connect();
			boolean isconn = conn.authenticateWithPassword(userName, password);
			if (!isconn) {
				log.info("连接失败");
			}
			ssh = conn.openSession();
			InputStream is = new StreamGobbler(ssh.getStdout());
			BufferedReader brs = new BufferedReader(new InputStreamReader(is));
			// 判断包不包含通配符
			if (fileName.contains("?")) {
				fileName = fileName.replace("?", "*");
			}
			ssh.execCommand("find " + filePath + " -name " + "'" + fileName
					+ "." + fileType + "'");
			Channel channel;
			ChannelSftp c;
			if (connectionType == 2) {// sftp
				channel = session.openChannel("sftp");
				channel.connect();
				;
				c = (ChannelSftp) channel;
				while (true) {
					String line = brs.readLine();
					if (line == null) {
						break;
					}
					c.get(line, localfile);
					flag = true;
					log.info("已经下载到本地" + localfile);
				}
			} else {// ssh
				SCPClient scpClient = conn.createSCPClient();
				while (true) {
					String line = brs.readLine();
					if (line == null) {
						break;
					}
					// 去除文件名中的空格,不然下面执行命令会报错
					line = line.replace(" ", "");
					scpClient.get(line, localfile);
					flag = true;
					log.info("已经下载到本地" + localfile);
				}
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			// 连接的Session和Connection对象都需要关闭
			if (ssh != null) {
				ssh.close();
			}
			if (conn != null) {
				conn.close();
			}

		}
		if (flag) {
			return localfile;
		} else {
			log.info("在此路径及其子路径下没有找到相应的文件");
			return null;
		}

	}

}