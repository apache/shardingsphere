package io.shardingjdbc.server.constant;

/**
 * Sharding-JDBC-Server's information.
 * 
 * @author zhangliang 
 */
public interface ServerInfo {
    
    /**
     * Protocol version is always 0x0A.
     */
    int PROTOCOL_VERSION = 0x0A;
    
    /**
     * Server version.
     */
    String SERVER_VERSION = "Sharding-JDBC-Server 2.1.0";
    
    /**
     * Charset code 0x21 is utf8_general_ci.
     */
    int CHARSET = 0x21;
}
