package io.shardingjdbc.console.entity;

import com.sun.tools.internal.xjc.reader.xmlschema.bindinfo.BIConversion;
import io.shardingjdbc.console.constant.ConnDriver;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class UserSession {
    private String userName;
    private String passWord;
    private String targetURL;
    // MySQL Oracle
    private String driver= "MySQL";
    private String uuid=UUID.randomUUID().toString().replace("-", "").toLowerCase();

    public UserSession(){

    }
    public UserSession(String userName, String passWord, String targetURL){
        this.userName = userName;
        this.passWord = passWord;
        this.targetURL = targetURL;
        this.uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }
    public UserSession(String userName, String passWord, String targetURL, String driver){
        this.userName = userName;
        this.passWord = passWord;
        this.targetURL = targetURL;
        this.driver = driver;
        this.uuid = UUID.randomUUID().toString().replace("-", "").toLowerCase();
    }

    public UserSession(String userName, String passWord, String targetURL, String uuid, String driver){
        this.userName = userName;
        this.passWord = passWord;
        this.targetURL = targetURL;
        this.driver = driver;
        this.uuid = uuid;
    }

}
