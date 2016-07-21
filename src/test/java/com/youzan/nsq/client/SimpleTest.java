package com.youzan.nsq.client;


/**
 * 
 * @author ford
 *
 */
public class SimpleTest {
	
	
	
	public static void main(String[] args){
		int i=10;
		System.out.println("i="+i);
	}
	
	public boolean _test(){
		try {
			return throwException();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return true;
	}
	
	public boolean throwException() throws Exception{
		throw new Exception("exception");
	}

}
