package com.Market.common;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 统一返回格式：所有 Controller 接口统一返回此类
 *
 * @param <T> data 字段的类型
 */
@Data //相当于getter/setter/toString
@NoArgsConstructor//无参构造
@AllArgsConstructor//全参构造
public class Result<T> {

    private Integer code;   // 状态码：0=成功，其他=失败
    private String msg;     // 信息描述
    private T data;        // 泛型数据体

    /**
     * 成功返回，无数据
     */
    public static <T> Result<T> success() {
        return new Result<>(0, "操作成功", null);
    }

    /**
     * 成功返回，带数据
     */
    public static <T> Result<T> success(T data) {
        return new Result<>(0, "操作成功", data);
    }

    /**
     * 成功返回，自定义消息
     */
    public static <T> Result<T> success(String msg, T data) {
        return new Result<>(0, msg, data);
    }

    /**
     * 失败返回，自定义错误码和消息
     */
    public static <T> Result<T> error(Integer code, String msg) {
        return new Result<>(code, msg, null);
    }

    /**
     * 失败返回，默认错误码 500
     */
    public static <T> Result<T> error(String msg) {
        return new Result<>(500, msg, null);
    }
}
