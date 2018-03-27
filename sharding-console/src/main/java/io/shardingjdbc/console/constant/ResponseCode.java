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
     * Get code.
     * 
     * @return code
     */
    public int getCode() {
        return code;
    }
    
    /**
     * Get msg.
     * 
     * @return msg
     */
    public String getMsg() {
        return msg;
    }
}
