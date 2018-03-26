package io.shardingjdbc.console.entity;

import io.shardingjdbc.console.constant.RespCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RespObj {

    private int statusCode;

    private String errMsg;

    private Object resultInfo;

    public RespObj(final RespCode respCode) {
        this.statusCode = respCode.getCode();
        this.errMsg = respCode.getMsg();
        this.resultInfo = null;
    }

    public RespObj(final RespCode respCode, final Object data) {
        this(respCode);
        this.resultInfo = data;
    }
}
