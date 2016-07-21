/**
 *
 */
package com.trendrr.nsq.exceptions;


/**
 * 
 * @author ford
 *
 */
public class TimeoutException extends Exception {

    private static final long serialVersionUID = 6014743379707601555L;

    public TimeoutException(String message, Throwable cause) {
        super(message, cause);
    }
}
