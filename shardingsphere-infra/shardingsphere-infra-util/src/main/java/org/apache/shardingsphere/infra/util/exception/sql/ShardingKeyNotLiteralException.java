package org.apache.shardingsphere.infra.util.exception.sql;

import org.apache.shardingsphere.infra.util.exception.ShardingSphereException;

public class ShardingKeyNotLiteralException extends ShardingSphereException {

    private static final String ERROR_CATEGORY = "SHARDING_KEY";
    private static final int ERROR_CODE = 1;
    private static final String ERROR_MSG = "Sharding key is not literal";

    public ShardingKeyNotLiteralException() {
        this(null);
    }

    public ShardingKeyNotLiteralException(Exception cause) {
        super(ERROR_CATEGORY, ERROR_CODE, ERROR_MSG, cause);
    }
}
