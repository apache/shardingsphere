package io.shardingjdbc.console.domain;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import java.util.HashMap;
import java.util.Map;
import com.google.common.base.Optional;

/**
 * Define user session.
 * 
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserSessionRegistry {
    
    private static final UserSessionRegistry INSTANCE = new UserSessionRegistry();
    
    private final Map<String, UserSession> userSessions = new HashMap<>(128, 1);
    
    /**
     * Get UserSessionRegistry instance.
     * 
     * @return session registry
     */
    public static UserSessionRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Create the map of id and connection.
     *
     * @param sessionId session id
     * @return user and connection info
     */
    public synchronized Optional<UserSession> findSession(final String sessionId) {
        return Optional.fromNullable(userSessions.get(sessionId));
    }
    
    /**
     * Add session.
     * 
     * @param sessionId session id
     * @param userSession user session
     */
    public synchronized void addSession(final String sessionId, final UserSession userSession) {
        userSessions.put(sessionId, userSession);
    }

    /**
     * Remove session.
     * 
     * @param sessionId session id
     */
    public synchronized void removeSession(final String sessionId) {
        userSessions.remove(sessionId);
    }
}
