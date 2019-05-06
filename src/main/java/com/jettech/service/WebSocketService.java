package com.jettech.service;

import java.io.IOException;
import java.util.concurrent.CopyOnWriteArraySet;
import javax.websocket.OnClose;
import javax.websocket.OnError;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.PathParam;
import javax.websocket.server.ServerEndpoint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Component;
 
@ServerEndpoint(value = "/client/{userId}")
@Component
public class WebSocketService {
	
    private static Logger logger = LogManager.getLogger(WebSocketService.class.getName());
	
    //静态变量，用来记录当前在线连接数。应该把它设计成线程安全的。
    private static int onlineCount = 0;
	  
    //concurrent包的线程安全Set，用来存放每个客户端对应的MyWebSocket对象。
    private static CopyOnWriteArraySet<WebSocketService> wsClientMap = new CopyOnWriteArraySet<>();  
  
    //与某个客户端的连接会话，需要通过它来给客户端发送数据
    private Session session;
    
    /**
     * 连接建立成功调用的方法
     * @param session 当前会话session
     */
    @OnOpen
    public void onOpen (@PathParam("userId")String userId,Session session){
        this.session = session;
        wsClientMap.add(this);
        addOnlineCount();
        logger.info(session.getId()+"有新链接加入，当前链接数为：" + wsClientMap.size());
    }
  
    /**
     * 连接关闭
     */
    @OnClose
    public void onClose (){
    	wsClientMap.remove(this);  
        subOnlineCount();  
        logger.info("有一链接关闭，当前链接数为：" + wsClientMap.size());
    }
  
    /**
     * 收到客户端消息
     * @param message 客户端发送过来的消息
     * @param session 当前会话session
     * @throws IOException
     */
    @OnMessage
    public void onMessage (String message, Session session) throws IOException {
        logger.info("来终端的警情消息:" + message);
        sendMsgToAll(message); 
    }
    
    /**
     * 发生错误
     */
    @OnError
    public void onError(Session session, Throwable error) {
    	logger.info("wsClientMap发生错误!");
        error.printStackTrace();
    }
    
    /**
     * 给所有客户端群发消息
     * @param message 消息内容
     * @throws IOException
     */
    public static void sendMsgToAll(String message) throws IOException {
    	for ( WebSocketService item : wsClientMap ){  
            item.session.getBasicRemote().sendText(message);
    	}
    	logger.info("成功群送一条消息:" + wsClientMap.size());
    }
  
    public void sendMessage (String message) throws IOException {
        this.session.getBasicRemote().sendText(message);  
        logger.info("成功发送一条消息:" + message);
    }
  
    public static synchronized  int getOnlineCount (){
        return WebSocketService.onlineCount;  
    }
  
    public static synchronized void addOnlineCount (){
    	WebSocketService.onlineCount++;  
    }
  
    public static synchronized void subOnlineCount (){
    	WebSocketService.onlineCount--;  
    }
}
