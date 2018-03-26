package io.shardingjdbc.console.constant;

public enum RespCode {

    SUCCESS(0, "请求成功"),
    ERR_GEN(-1, "请求失败"),
    ERR_USER(-2, "用户权限验证失败");

    private int code;
    private String msg;

    RespCode(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     *
     * @return code
     */
    public int getCode() {
        return code;
    }

    /**
     *
     * @return msg
     */
    public String getMsg() {
        return msg;
    }
}