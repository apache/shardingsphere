package io.shardingjdbc.console.sql.domain;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * SQL execute exception.
 *
 * @author panjuan
 */
@ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
public final class SQLExecuteException extends Exception {
    public SQLExecuteException(final String errorMessage) {
        super(errorMessage);
    }
}
