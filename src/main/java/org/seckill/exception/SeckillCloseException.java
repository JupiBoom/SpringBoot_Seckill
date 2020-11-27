package org.seckill.exception;

/**
 * 秒杀关闭异常
 */
public class SeckillCloseException extends SeckillExeption {

    public SeckillCloseException(String message) {
        super(message);
    }

    public SeckillCloseException(String message, Throwable cause) {
        super(message, cause);
    }
}
