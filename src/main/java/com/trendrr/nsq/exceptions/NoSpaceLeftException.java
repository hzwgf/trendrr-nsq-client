package com.trendrr.nsq.exceptions;



/**
 * 
 * 
 * @author ford
 *
 */
public class NoSpaceLeftException extends Exception{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public NoSpaceLeftException(String message,Throwable cause){
		super(message,cause);
	}

}
