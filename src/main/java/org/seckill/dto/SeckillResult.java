package org.seckill.dto;

import lombok.Data;


/**
 * 所有ajax请求返回的数据类型，封装json结果
 * @param <T>
 */
@Data
public class SeckillResult<T> {

    private boolean success;
    private T data;
    private String error;

    public SeckillResult(boolean success, T data) {
        this.success = success;
        this.data = data;
    }

    public SeckillResult(boolean success, String error) {
        this.success = success;
        this.error = error;
    }
}
