package io.shardingjdbc.console.domain;

import lombok.Getter;

import java.sql.Connection;
import java.util.UUID;

/**
 * Define window session.
 *
 * @author panjuan
 */
@Getter
public class Window {
    
    private String id;
    
    private Connection connection;
    
    public Window(final Connection conn) {
        id = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        connection = conn;
    }
}
