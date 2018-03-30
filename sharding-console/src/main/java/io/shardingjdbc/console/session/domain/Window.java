package io.shardingjdbc.console.session.domain;

import com.google.common.base.Optional;
import lombok.Getter;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.UUID;

/**
 * Define common session.
 *
 * @author panjuan
 */
@Getter
public class Window {
    
    private String id;
    
    private Connection connection;
    
    public Window(String userUUID) throws SessionException {
        id = UUID.randomUUID().toString().replace("-", "").toLowerCase();
        Optional<Session> sessionOptional = SessionRegistry.getInstance().findSession(userUUID);
        if (!sessionOptional.isPresent()) {
           throw new SessionException("Please login first.");
        }
        Session session = sessionOptional.get();
        try {
            this.connection =  DBConnector.getConnection(session.getUserName(), session.getPassWord(), session.getTargetURL(), session.getDriver());
        } catch (final ClassNotFoundException | SQLException ex) {
            throw new SessionException(ex.toString());
        }
    }
}
