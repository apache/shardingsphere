package io.shardingjdbc.console.session.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * User session.
 * 
 * @author panjuan
 */
@AllArgsConstructor
@Getter
@Setter
public final class Session {
    
    private String id;
    
    private String userName;
    
    private String passWord;
    
    private String targetURL;
    
    private String driver;
    
    private List<String> windowIDList;
    
    public Session() {
        id = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        driver = "MySQL";
        windowIDList = new ArrayList<>(128);
    }
    
    public void addWindowID(final String windowID) {
        windowIDList.add(windowID);
    }
    
    public void delWindowID(final String windowID) {
        windowIDList.remove(windowID);
    }
}
