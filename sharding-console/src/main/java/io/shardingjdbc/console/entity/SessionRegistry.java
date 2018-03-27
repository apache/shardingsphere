package io.shardingjdbc.console.entity;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;

import java.util.HashMap;
import java.util.Map;
import com.google.common.base.Optional;

/**
 * Global user session.
 * 
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SessionRegistry {
    
    private static final SessionRegistry INSTANCE = new SessionRegistry();
    
    private static final Map<String, Connection> SESSIONS = new HashMap<>(128, 1);
    
    /**
     * Get SessionRegistry instance.
     * 
     * @return session registry
     */
    public static SessionRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create the map of id and connection.
     *
     * @return  user and connection info
     */
    public Optional<Connection> findSession(final String sessionId) {
        synchronized (SESSIONS) {
            return Optional.fromNullable(SESSIONS.get(sessionId));
        }
    }
    
    /**
     * Add session
     * 
     * @param sessionId
     * @param connection
     */
    public void addSession(final String sessionId, final Connection connection) {
        synchronized (SESSIONS) {
            SESSIONS.put(sessionId, connection);
        }
    }

    /**
     * Remove session
     * 
     * @param sessionId
     */
    public void removeSession(final String sessionId) {
        synchronized (SESSIONS) {
            SESSIONS.remove(sessionId);
        }
    }
}
