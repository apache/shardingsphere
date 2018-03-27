package io.shardingjdbc.console.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

/**
 * user session .
 *
 * @author panjuan
 */
@AllArgsConstructor
@Getter
@Setter
public final class UserSession {
    
    private String uuid;
    
    private String userName;
    
    private String passWord;
    
    private String targetURL;
    
    private String driver;
    
    public UserSession() {
        uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        driver = "MySQL";
    }
}
