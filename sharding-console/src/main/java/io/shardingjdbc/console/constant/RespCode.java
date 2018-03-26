package io.shardingjdbc.console.constant;

public enum RespCode {

    SUCCESS(0, "请求成功"),
    ERR_GEN(-1, "请求失败"),
    ERR_USER(-2, "用户权限验证失败");

    private int code;
    private String msg;

    RespCode(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

    public int getCode() {
        return code;
    }
    public String getMsg() {
        return msg;
    }
}