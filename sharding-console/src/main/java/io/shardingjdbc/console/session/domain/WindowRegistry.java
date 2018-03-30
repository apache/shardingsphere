package io.shardingjdbc.console.session.domain;

import com.google.common.base.Optional;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

/**
 * Define common session registry.
 *
 * @author panjuan
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class WindowRegistry {
    
    private static final WindowRegistry INSTANCE = new WindowRegistry();
    
    private final Map<String, Window> windows = new HashMap<>(128, 1);
    
    /**
     * Return the WindowRegistry INSTANCE.
     *
     * @return common session registry
     */
    public static WindowRegistry getInstance() {
        return INSTANCE;
    }
    
    /**
     * Find common session.
     *
     * @param windowID common ID
     * @return connection or null
     */
    public synchronized Optional<Window> findWindow(final String windowID) {
        return Optional.fromNullable(windows.get(windowID));
    }
    
    /**
     * Add common session.
     *
     * @param windowID window ID
     * @param window window
     */
    public synchronized void addWindow(final String windowID, final Window window) {
        windows.put(windowID, window);
    }
    
    /**
     * Remove common session.
     *
     * @param windowID common ID
     */
    public synchronized void removeWindow(final String windowID) throws SQLException {
        Connection connection = windows.remove(windowID).getConnection();
        connection.close();
    }
}
