package io.shardingjdbc.console.entity;

import io.shardingjdbc.console.constant.ResponseCode;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ResponseObject {
    
    private int statusCode;
    
    private String errMsg;
    
    private Object resultInfo;
    
    public ResponseObject(final ResponseCode responseCode) {
        this.statusCode = responseCode.getCode();
        this.errMsg = responseCode.getMsg();
        this.resultInfo = null;
    }
    
    public ResponseObject(final ResponseCode responseCode, final Object data) {
        this(responseCode);
        this.resultInfo = data;
    }
}
