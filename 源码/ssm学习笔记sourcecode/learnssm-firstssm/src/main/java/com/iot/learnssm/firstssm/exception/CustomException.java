package com.iot.learnssm.firstssm.exception;

/**
 * Created by brian on 2016/3/7.
 *
 * 系统 自定义异常类，针对预期的异常，需要在程序中抛出此类的异常
 */
public class CustomException extends Exception {
    //异常信息
    public String message;

    public CustomException(String message) {
        super(message);
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

}

