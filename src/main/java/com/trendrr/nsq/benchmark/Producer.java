package com.trendrr.nsq.benchmark;



import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.trendrr.nsq.NSQProducer;


/**
 * 
 * 1，获得系统的性能数据；
 * 2，系统的性能瓶颈。
 * 
 * 
 * @author ford
 *
 */
public class Producer {
	
	private static final Logger logger=LoggerFactory.getLogger(Producer.class);
	
	private static volatile int barrierNumber=0;
	
	private static volatile boolean stop=false;
	
	private static final Dashboard dashboard=new Dashboard("nsq-producer");
	
	private static AtomicLong startRun=new AtomicLong(0);
	

	public static void main(String[] args) {
		if(BenchmarkUtil.producerSnapshot==1){
			new Snapshot().start();
		}
		Producer client=new Producer();
		client.start(BenchmarkUtil.threadNum);
		
	}
	
	
	public void start(int threadNumber){
		try {
			logger.info("\n\n\n开始,线程数->"+threadNumber);
			final CyclicBarrier barrier=new CyclicBarrier(threadNumber+1,new Runnable(){

				@Override
				public void run() {
					logger.info("关卡重置:"+(++barrierNumber));
				}
				
			}); 
			ExecutorService service=Executors.newFixedThreadPool(threadNumber);
			ArrayList<Future<Integer>> futures=new ArrayList<Future<Integer>>(); 
			NSQProducer producer=BenchmarkUtil.buildProducer();
			for(int i=0;i<threadNumber;i++){
				RequestCallable callable=new RequestCallable(barrier);
				callable.setProducer(producer);
				Future<Integer> futrue=service.submit(callable);
				futures.add(futrue);
			}
			TimeUnit.SECONDS.sleep(2);
			//这行代码必须在barrier.await()的前面，线程池中的线程比main线程先执行
			startRun.set(System.currentTimeMillis());
			barrier.await();
			logger.info("线程开始了,数量:{}",threadNumber);
			TimeUnit.SECONDS.sleep(BenchmarkUtil.testTimeInSecond);
			stop=true;
			int resultNum=0;
			for(Future<Integer> future:futures){
				resultNum +=future.get();
			}
			
			long runTime=System.currentTimeMillis()-startRun.get();
			logger.info("线程返回正常,数量:{},预计执行时间：{}s,实际执行时间:{}ms",new Object[]{resultNum,BenchmarkUtil.testTimeInSecond,runTime});
			dashboard.print();
			service.shutdownNow();
			producer.close();
			TimeUnit.SECONDS.sleep(5);
			/////reset
			dashboard.reset();
//			stop=false;
//			start(threadNumber+intervalThread);
		} catch (Exception e) {
			logger.error("",e);
		}
	}
	
	
	
	public class RequestCallable implements Callable<Integer>{
		
		private CyclicBarrier barrier;
		private NSQProducer producer;
		
		
		public RequestCallable(CyclicBarrier barrier){
			this.barrier=barrier;
		}
		
		public void setProducer(NSQProducer producer){
			this.producer=producer;
		}
		
		public Integer call(){
			////////////////validate//////
			if(producer==null){
				logger.error("生产者为null");
				return 1;
			}
			////////////////validate//////
			try {
				this.barrier.await();
			} catch (Exception e1) {
				logger.error("",e1);
			}
			while(!stop){
				try{
					long start=System.currentTimeMillis();
					
					////////////判断是否需要等待，并执行等待////////////
					long period=BenchmarkUtil.testTimeInSecond/BenchmarkUtil.produceSpeed.size();
					long position=(start - startRun.get())/(period*1000);
					if(position>=BenchmarkUtil.produceSpeed.size()){
						position=BenchmarkUtil.produceSpeed.size()-1;
					}
					int sleepTime=BenchmarkUtil.produceSpeed.get((int) position);
					if(sleepTime>0){
						TimeUnit.SECONDS.sleep(sleepTime);
					}
					////////////判断是否需要等待，并执行等待////////////
					producer.produce(BenchmarkUtil.topic, Message.build(barrierNumber,BenchmarkUtil.sizeType).getBytes("utf-8"));
					long interval=System.currentTimeMillis()-start;
					dashboard.requestSuccessCount.incrementAndGet();
					dashboard.requestSuccessTotalTime.addAndGet(interval);
					dashboard.updateMaxRt(interval);
					dashboard.updateMinRt(interval);
				} catch(Throwable e){
//					logger.error("",e);
					dashboard.requestFailCount.incrementAndGet();
					dashboard.addException(e.toString());
				}
			}
			return 1;
		}
	}
	
	public static class Snapshot extends Thread{
		
		public Snapshot(){
			super.setName("producer-snapshot");
			super.setDaemon(true);
		}

		@Override
		public void run() {
			try{
				int count=0;
				while(!stop){
					TimeUnit.SECONDS.sleep(1);
					count++;
					if(count%Consumer.maxIdle==0){
						dashboard.print();
					}
				}
			} catch(Throwable t){
				logger.error("快照线程异常退出。",t);
			}
			
		}
	}
	
	
	public static class Dashboard{
		
		public final String name;
		
		public AtomicLong requestSuccessCount=new AtomicLong();
		
		public AtomicLong requestSuccessTotalTime=new AtomicLong();
		
		public AtomicLong requestFailCount=new AtomicLong();
		
		public AtomicLong maxRt=new AtomicLong(-1);
		
		public AtomicLong minRt=new AtomicLong(Long.MAX_VALUE);
		
		
		public ConcurrentHashMap<String,AtomicInteger> exceptions=new ConcurrentHashMap<String,AtomicInteger>();
		
		public Dashboard(String name){
			this.name=name;
		}
		
		public void print(){
			double averageRt=requestSuccessTotalTime.doubleValue()/requestSuccessCount.longValue();
			double tps=requestSuccessCount.doubleValue()*1000/(System.currentTimeMillis()-startRun.get());
			Object[] args=new Object[]{new DecimalFormat("#0.00").format(tps),
										new DecimalFormat("#0.00").format(averageRt),
										maxRt.intValue(),
										minRt.intValue(),
										requestSuccessCount.longValue(),
										requestFailCount.intValue()};
			logger.info("tps:{},avg rt:{}ms,max rt:{}ms,min rt:{}ms,success:{},fail:{}.",args);
			logger.info("异常统计：{}",new Object[]{exceptions.toString()});
		}
		
		public void reset(){
			requestSuccessCount.set(0);
			requestSuccessTotalTime.set(0);
			requestFailCount.set(0);
			maxRt.set(-1);
			minRt.set(Long.MAX_VALUE);
			exceptions.clear();
		}
		
		
		public void addException(String exception){
			AtomicInteger count=exceptions.get(exception);
			if(count==null){
				count=new AtomicInteger(0);
				AtomicInteger old=exceptions.putIfAbsent(exception, count);
				count=old==null?count:old;
			}
			count.incrementAndGet();
		}
		
		public void updateMaxRt(long currentRt){
			long prevMaxRt=this.maxRt.get();
			while(currentRt>prevMaxRt){
				boolean cas=this.maxRt.compareAndSet(prevMaxRt, currentRt);
				if(cas){
					break;
				}
				prevMaxRt=this.maxRt.get();
			}
		}
		
		public void updateMinRt(long currentRt){
			long prevMinRt=this.minRt.get();
			while(currentRt<prevMinRt){
				boolean cas=this.minRt.compareAndSet(prevMinRt, currentRt);
				if(cas){
					break;
				}
				prevMinRt=this.minRt.get();
			}
		}
	}

}