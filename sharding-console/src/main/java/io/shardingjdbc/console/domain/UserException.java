package io.shardingjdbc.console.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Define the user exception.
 *
 * @author panjuan
 */

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserException extends Exception {
    public UserException(final String errorMessage) {
        super(errorMessage);
    }
}
