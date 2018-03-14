package io.shardingjdbc.dbtest.exception;

public class DbTestException extends RuntimeException {

    private static final long serialVersionUID = 8269224755642356888L;

    public DbTestException(final String message) {
        super(message);
    }

}
