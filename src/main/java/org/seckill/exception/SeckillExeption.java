package org.seckill.exception;

/**
 * 秒杀相关的公共异常
 */
public class SeckillExeption extends RuntimeException {
    public SeckillExeption(String message) {
        super(message);
    }

    public SeckillExeption(String message, Throwable cause) {
        super(message, cause);
    }
}
