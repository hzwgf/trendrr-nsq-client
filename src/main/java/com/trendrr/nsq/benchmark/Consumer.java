package com.trendrr.nsq.benchmark;

import java.text.DecimalFormat;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trendrr.nsq.NSQConsumer;
import com.trendrr.nsq.NSQMessage;
import com.trendrr.nsq.NSQMessageCallback;

/**
 * 
 * 
 * 
 * @author ford
 *
 */
public class Consumer {

	private static final Logger logger = LoggerFactory.getLogger(Consumer.class);
	
	public static final Dashboard dashboard=new Dashboard();
	
	public static final int maxIdle=20;
	
	public static final AtomicInteger idleCount=new AtomicInteger(0);
	
	public static NSQConsumer consumer;
	
	

	public static void main(String[] args) {
		logger.info("consumer start...");
		consumer=BenchmarkUtil.buildConsumer(new NSQMessageCallback(){

			@Override
			public void message(NSQMessage message) {
				message.finished();
				dashboard.updateMsg();
			}

			@Override
			public void error(Exception x) {
				dashboard.addError();
			}
			
		});
		
		
		new Thread(new Snapshot(),"check-msg-over").start();
	}
	
	
	public static class Snapshot implements Runnable{
		
		private int count=0;

		@Override
		public void run() {
			logger.info("start check...");
			while(true){
				try {
					TimeUnit.SECONDS.sleep(1);
				} catch (InterruptedException e) {
					logger.info("snapshot线程中断。。。");
					break;
				}
				try{
					if(idleCount.incrementAndGet()>=maxIdle){
						try{
							if(dashboard._msgCount.get()>0){
								dashboard.print();
							}
						} finally{
							logger.info("{}s没有消息消费，客户端自动关闭。",new Object[]{idleCount.get()});
							consumer.close();
						}
						break;
					} else if(++count%maxIdle==0){
						dashboard.print();
						dashboard.flush();
					}
				} catch(Throwable t){
					logger.info("",t);
				}
				
			}
			
		}
	}
	
	public static class Dashboard{
		public AtomicLong msgCount=new AtomicLong(0);
		
		public AtomicLong startTime=new AtomicLong(0);
		
		public AtomicLong endTime=new AtomicLong(0);
		
		public AtomicLong errorCount=new AtomicLong(0);
		
		//每20s刷新一次数据
		public AtomicLong _msgCount=new AtomicLong(0);
		
		public AtomicLong _startTime=new AtomicLong(0);
		
		public AtomicLong _endTime=new AtomicLong(0);
		
		public void print(){
			double currentTps=_msgCount.doubleValue()*1000/(_endTime.longValue()-_startTime.longValue());
			long consumerTime=endTime.get()-startTime.get();
			Object[] args=new Object[]{msgCount.get(),consumerTime,new DecimalFormat("#0.00").format(currentTps),errorCount.get()};
			logger.info("消费消息总数：{}，消费时间：{}，当前tps：{},错误数：{}",args);
			
			
		}
		
		public void flush(){
			_msgCount.set(0);
			_startTime.set(0);
			_endTime.set(0);
		}
		
		public void updateMsg(){
			idleCount.set(0);
			msgCount.incrementAndGet();
			long now=System.currentTimeMillis();
			if(startTime.get()<=0){
				startTime.set(now);
			}
			endTime.set(now);
			
			_msgCount.incrementAndGet();
			if(_startTime.get()<=0){
				_startTime.set(now);
			}
			_endTime.set(now);
		}
		
		public void addError(){
			errorCount.incrementAndGet();
		}
		
	}
	
	
	
//	public static class Dashboard{
//		public AtomicLong msgNum=new AtomicLong(0l);
//		
//		//计算一定时间段的tps
//		public AtomicLong consumerNum=new AtomicLong(0l);
//		//计算一定时间段的消费延时
//		public AtomicLong consumerLatency=new AtomicLong(0l);
//		public AtomicLong maxLatency=new AtomicLong(0l);
//		public AtomicLong minLatency=new AtomicLong(Long.MAX_VALUE);
//		
//		public volatile long startTime;
//		
//		public long runTimeTotal=0;
//		
//		
//		public Dashboard(){
//		}
//		
//		public void flush(){
//			consumerNum.set(0);
//			consumerLatency.set(0);
//			maxLatency.set(0);
//			minLatency.set(Long.MAX_VALUE);
//			startTime=System.currentTimeMillis();
//		}
//		
//		public void print(){
//			String tps=new DecimalFormat("#0.00").format(consumerNum.doubleValue()*1000/(System.currentTimeMillis()-startTime));
//			long avgLatency=consumerLatency.longValue()/consumerNum.longValue();
//			runTimeTotal+=(System.currentTimeMillis()-startTime);
//			Object[] args=new Object[]{msgNum.get(),runTimeTotal/1000,consumerNum.get(),tps,avgLatency,maxLatency,minLatency};
//			logger.info("总消息数：{}，累计执行时间：{}s，新增消息数：{}，tps：{}，该时间段平均消费延时：{}ms，maxLatency:{}ms,minLatency:{}ms",args);
//		}
//		
//		public void update(Message m){
//			if(startTime<=0){
//				startTime=System.currentTimeMillis();
//			}
//			this.msgNum.incrementAndGet();
//			this.consumerNum.incrementAndGet();
//			long currentLatency=System.currentTimeMillis()-m.getBirthDate();
//			consumerLatency.addAndGet(currentLatency);
//			this.updataMaxLatency(currentLatency);
//			this.updataMinLatency(currentLatency);
//		}
//		
//		public void updataMaxLatency(long currentLatency){
//			long prevMaxLatency=maxLatency.get();
//			while(currentLatency>prevMaxLatency){
//				boolean cas=maxLatency.compareAndSet(prevMaxLatency, currentLatency);
//				if(cas)break;
//				prevMaxLatency=maxLatency.get();
//			}
//		}
//		
//		public void updataMinLatency(long currentLatency){
//			long prevMinLatency=minLatency.get();
//			while(currentLatency<prevMinLatency){
//				boolean cas=minLatency.compareAndSet(prevMinLatency, currentLatency);
//				if(cas)break;
//				prevMinLatency=minLatency.get();
//			}
//		}
//		
//
////		@Override
////		public int hashCode() {
////			return batchid;
////		}
////
////		@Override
////		public boolean equals(Object obj) {
////			if(!(obj instanceof Dashboard) || obj==null){
////				return false;
////			}
////			Dashboard d=(Dashboard) obj;
////			boolean result=this.batchid==d.batchid
////							&&this.msgNum.get()==d.msgNum.get()
////							&&this.consumerLatency.get()==d.consumerLatency.get()
////							&&this.maxLatency.get()==d.maxLatency.get()
////							&&this.minLatency.get()==d.minLatency.get();
////			return result;
////		}
//
////		@Override
////		public String toString() {
////			avgLatency=new DecimalFormat("#.00").format(consumerLatency.doubleValue()/msgNum.longValue());
////			return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
////		}
//	}

}
