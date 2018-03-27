package io.shardingjdbc.console.entity;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/**
 * user session .
 *
 * @author panjuan
 */
@Getter
@Setter
public class UserSession {
    
    private String userName;
    
    private String passWord;
    
    private String targetURL;
    
    private String driver = "MySQL";
    
    private String uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    
    public UserSession() {
    }
    
    public UserSession(final String userName, final String passWord, final String targetURL) {
        
        this.userName = userName;
        
        this.passWord = passWord;
        
        this.targetURL = targetURL;
        
        this.uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
    
    public UserSession(final String userName, final String passWord, final String targetURL, final String driver) {
        
        this.userName = userName;
        
        this.passWord = passWord;
        
        this.targetURL = targetURL;
        
        this.driver = driver;
        
        this.uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
    
    public UserSession(final String userName, final String passWord, final String targetURL, final String uuid, final String driver) {
        
        this.userName = userName;
        
        this.passWord = passWord;
        
        this.targetURL = targetURL;
        
        this.driver = driver;
        
        this.uuid = uuid;
    }
}
