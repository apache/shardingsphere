package org.apache.shardingsphere.core.exception;

public class UnrecognizeDatasourceInfoException extends ShardingException {

    private static final long serialVersionUID = 6724405529843267928L;

    public UnrecognizeDatasourceInfoException(final Exception cause) {
        super("The datasource is not recognized.", cause);
    }
}
