package io.shardingjdbc.console.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * AccountResponseResult.
 *
 * @author zhangyonglun
 */
@Getter
@Setter
public class AccountResponseResult {
    
    private Integer statusCode;
    
    private String errMsg;
    
    public AccountResponseResult() {
        statusCode = -1;
        errMsg = "";
    }
}
