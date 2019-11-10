package org.apache.shardingsphere.core.exception;

import org.apache.shardingsphere.core.exception.ShardingException;

public class UnrecognizeDatasourceInfoException extends ShardingException {

    private static final long serialVersionUID = 6724405529843267928L;

    public UnrecognizeDatasourceInfoException(final Exception cause) {
        super("The datasource is not recognized.", cause);
    }
}
