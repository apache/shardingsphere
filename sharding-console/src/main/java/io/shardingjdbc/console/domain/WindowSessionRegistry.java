package io.shardingjdbc.console.domain;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.util.HashMap;
import java.util.Map;

/**
 * Define window session registry.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WindowSessionRegistry {
    
    private static final WindowSessionRegistry INSTANCE = new WindowSessionRegistry();
    
    private final Map<String, Connection> windowSessions = new HashMap<>(128, 1);
    
    /**
     * Return the WindowSessionRegistry INSTANCE.
     *
     * @return window session registry
     */
    public static WindowSessionRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Find window session.
     *
     * @param windowID window ID
     * @return connection or null
     */
    public synchronized Optional<Connection> findSession(final String windowID) {
        return Optional.fromNullable(windowSessions.get(windowID));
    }
    
    /**
     * Add window session.
     *
     * @param windowID window ID
     * @param connection connection
     */
    public synchronized void addSession(final String windowID, final Connection connection) {
        windowSessions.put(windowID, connection);
    }
    
    /**
     * Remove window session.
     *
     * @param windowID window ID
     */
    public synchronized void removeSession(final String windowID) {
        windowSessions.remove(windowID);
    }
}
