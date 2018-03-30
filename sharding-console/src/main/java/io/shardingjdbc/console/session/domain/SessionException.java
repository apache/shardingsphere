package io.shardingjdbc.console.session.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Define the user exception.
 *
 * @author panjuan
 */

@ResponseStatus(HttpStatus.FORBIDDEN)
public class SessionException extends Exception {
    public SessionException(final String errorMessage) {
        super(errorMessage);
    }
}
