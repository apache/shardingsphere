package org.apache.shardingsphere.core.metadata.datasource.exception;

import org.apache.shardingsphere.core.exception.ShardingException;

public class UnBuildDataSourceMetaException extends ShardingException {
    
    private static final long serialVersionUID = -5557961244810380123L;
    
    public UnBuildDataSourceMetaException(final Exception e) {
        super(e);
    }
}
