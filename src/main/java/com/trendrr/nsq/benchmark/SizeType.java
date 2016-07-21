package com.trendrr.nsq.benchmark;

import java.util.concurrent.ConcurrentHashMap;



/**
 * 
 * @author ford
 *
 */
public enum SizeType {

	L_500B(500),L_1KB(1024),L_2KB(2048),L_3KB(3072);
	
	public int size;
	public String content;
	
	static {
		StringBuilder s_3kb = new StringBuilder();
		for (int i = 0; i < 3072; i++) {
			s_3kb.append("a");
		}
//		
//		实际的传输字节大小：headSize+4+messageSize,忽略消息头的大小
//		int headSize=new Publish(Consumer.topic,new byte[0]).getHeader().length();
		SizeType.L_3KB.content=s_3kb.substring(0, 3072-56);
		SizeType.L_2KB.content=s_3kb.substring(0, 2048-56);
		SizeType.L_1KB.content=s_3kb.substring(0, 1024-56);
		SizeType.L_500B.content=s_3kb.substring(0, 500-56);
	}
	
	public static final ConcurrentHashMap<Integer,String> pair=new ConcurrentHashMap<Integer,String>();
	
	
	
	
	private SizeType(int size){
		this.size=size;
	}
	
	public static SizeType get(int size){
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
		for(SizeType t:SizeType.values()){
			if(t.size==size)return t;
		}
		return null;
	}
	
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
