package com.reggie.exception.customException;

/*自定义业务异常*/
public class CustomException extends RuntimeException {
    public CustomException(String msg) {
        super(msg);
    }
}
