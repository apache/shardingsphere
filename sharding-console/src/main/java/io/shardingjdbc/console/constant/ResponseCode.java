package io.shardingjdbc.console.constant;

public enum ResponseCode {

    SUCCESS(0, "success"),
    FAILURE(-1, "failure");

    private int code;
    private String msg;

    ResponseCode(final int code, final String msg) {
        this.code = code;
        this.msg = msg;
    }

    /**
     * get code.
     * @return code
     */
    public int getCode() {
        return code;
    }

    /**
     * get msg.
     * @return msg
     */
    public String getMsg() {
        return msg;
    }
}
