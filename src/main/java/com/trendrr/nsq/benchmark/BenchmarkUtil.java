package com.trendrr.nsq.benchmark;

import java.io.FileInputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trendrr.nsq.ExampleMain;
import com.trendrr.nsq.NSQConsumer;
import com.trendrr.nsq.NSQLookup;
import com.trendrr.nsq.NSQMessage;
import com.trendrr.nsq.NSQMessageCallback;
import com.trendrr.nsq.NSQProducer;
import com.trendrr.nsq.exceptions.BadMessageException;
import com.trendrr.nsq.exceptions.BadTopicException;
import com.trendrr.nsq.exceptions.DisconnectedException;
import com.trendrr.nsq.exceptions.NoConnectionsException;
import com.trendrr.nsq.lookup.NSQLookupDynMapImpl;

/**
 * 
 * @author ford
 *
 */
public class BenchmarkUtil {

	public static Logger log = LoggerFactory.getLogger(BenchmarkUtil.class);
	
//	#p=producer,c=consumer
	public static String type="p";
	public static int threadNum=50;
	public static int sizeType=3072;
	public static String nsqdAddrs="192.168.66.202:24150,192.168.66.202:54150";
	public static int testTimeInSecond=30;
	public static int connectionNum=1;
	public static int producerSnapshot=0;
	public static ArrayList<Integer> produceSpeed=new ArrayList<Integer>();
	
	public static String topic="full_disk_topic";
	public static String channel="default";
	
	public static String lookupdHost="192.168.66.204";
	public static int lookupdPort=4161;
	public static int consumerSnapshot=1;
	
	
	
	static {
		try{
			Properties prop=new Properties();
			prop.load(ClassLoader.getSystemResourceAsStream("nsq.properties"));
			type=prop.getProperty("type");
			threadNum=Integer.parseInt(prop.getProperty("threadNum"));
			sizeType=Integer.parseInt(prop.getProperty("sizeType"));
			nsqdAddrs=prop.getProperty("nsqdAddrs");
			testTimeInSecond=Integer.parseInt(prop.getProperty("testTimeInSecond"));
			connectionNum=Integer.parseInt(prop.getProperty("connectionNum"));
			producerSnapshot=Integer.parseInt(prop.getProperty("producerSnapshot"));
			String temp=prop.getProperty("produceSpeed");
			for(String s:temp.split(",")){
				produceSpeed.add(Integer.parseInt(s));
			}
			topic=prop.getProperty("topic");
			channel=prop.getProperty("channel");
			lookupdHost=prop.getProperty("lookupdHost");
			lookupdPort=Integer.parseInt(prop.getProperty("lookupdPort"));
			consumerSnapshot=Integer.parseInt(prop.getProperty("consumerSnapshot"));
		} catch(Exception e){
			log.error("解析启动参数错误",e);
			System.exit(0);
		}
	}
	
	public static void main(String[] args){
		if("p".equals(type)){
			Producer.main(args);
		} else if("c".equals(type)){
			Consumer.main(args);
		} else {
			log.error("参数错误，程序未启动。");
		}
	}
	
	public static NSQProducer buildProducer(){
		NSQProducer producer = new NSQProducer();
		String[] addres=nsqdAddrs.split(",");
		for(String addr:addres){
			String[] pair=addr.split(":");
			producer.addAddress(pair[0], Integer.parseInt(pair[1]), connectionNum);
		}
        producer.start();
        return producer;
	}
	
	
	public static NSQConsumer buildConsumer(NSQMessageCallback callback){
		NSQLookup lookup = new NSQLookupDynMapImpl();
        lookup.addAddr(lookupdHost, lookupdPort);

        NSQConsumer consumer = new NSQConsumer(lookup, topic, channel, callback);

        consumer.start();
        return consumer;
	}

//	public static void main(String[] args) throws Exception {
//		NSQProducer producer=BenchmarkUtil.buildProducer();
//		final int num=50000;
//		int i=num;
//		long start=System.currentTimeMillis();
//		while(i-->0){
//			producer.produce(topic, Message.build(1).getBytes("utf-8"));
//		}
//		System.out.println("生产耗时耗时："+(System.currentTimeMillis()-start)/1000);
//	}

}
