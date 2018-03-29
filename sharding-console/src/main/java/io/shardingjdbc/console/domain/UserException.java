package io.shardingjdbc.console.domain;

import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.jws.soap.SOAPBinding;

/**
 * Define the user exception.
 *
 * @author panjuan
 */

@ResponseStatus(HttpStatus.FORBIDDEN)
public class UserException extends Exception {
    public UserException (final String errorMessage) {
        super(errorMessage);
    }
}
