package com.chaos.smarttodo.common.constant;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    FAILURE(400, "业务异常"),
    UN_AUTHORIZED(401, "请求未授权"),
    NOT_FOUND(404, "404 没找到请求"),
    MSG_NOT_READABLE(400, "消息不能读取"),
    INTERNAL_SERVER_ERROR(500, "服务器异常"),

    // 业务错误码 (10000 - 19999)
    USER_EXIST(10001, "用户已存在"),
    USER_NOT_EXIST(10002, "用户不存在"),
    PARAM_ERROR(10003, "参数错误");

    final int code;
    final String msg;
}