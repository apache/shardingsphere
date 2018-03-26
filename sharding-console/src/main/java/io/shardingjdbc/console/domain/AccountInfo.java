package io.shardingjdbc.console.domain;

import lombok.Getter;
import lombok.Setter;

/**
 * AccountInfo.
 *
 * @author zhangyonglun
 */
@Getter
@Setter
public class AccountInfo {
    
    private String driver;
    
    private String url;
    
    private String username;
    
    private String password;
}
