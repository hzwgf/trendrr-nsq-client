package com.trendrr.nsq.benchmark;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSONObject;

/**
 * 
 * @author ford
 *
 */
public class Message {

	private int batchid;
	private long id;
	private long birthDate;
	private String body;
	
	private static final ConcurrentHashMap<Integer,String> pair=new ConcurrentHashMap<Integer,String>();
	
	private static AtomicLong idGenerator = new AtomicLong();
	

	/**
	 * 500b
	 * 
	 * @param batchid
	 * @return
	 */
	public static String build(int batchid) {
		return build(batchid,500);
	}
	
	/**
	 * 
	 * @param batchid
	 * @param size 必须大于56
	 * @return
	 */
	public static String build(int batchid,int size) {
		if(size<56){
			return null;
		}
		return JSONObject.toJSONString(new Message(batchid,size));
	}
	
	public Message(){
		
	}

	private Message(int batchid) {
		this(batchid,500);
	}
	
	private Message(int batchid,int size) {
		this.batchid=batchid;
		this.id = idGenerator.incrementAndGet();
		this.birthDate = System.currentTimeMillis();
		this.body=getBody(size);
	}
	
	private String getBody(int size){
		String str=pair.get(size);
		if(str==null){
			StringBuilder sb=new StringBuilder();
			for(int i=0;i<size-56;i++){
				sb.append("a");
			}
			str=sb.toString();
			pair.putIfAbsent(size, str);
			str=pair.get(size);
		}
		return str;
	}

	public static void main(String[] args) {
		System.out.println(Message.build(1, 1024).length());
		System.out.println(Message.build(1, 3072).length());
		System.out.println(Message.build(1, 8192).length());
	}
	
	

	public int getBatchid() {
		return batchid;
	}

	public void setBatchid(int batchid) {
		this.batchid = batchid;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getBirthDate() {
		return birthDate;
	}

	public void setBirthDate(long birthDate) {
		this.birthDate = birthDate;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

}
