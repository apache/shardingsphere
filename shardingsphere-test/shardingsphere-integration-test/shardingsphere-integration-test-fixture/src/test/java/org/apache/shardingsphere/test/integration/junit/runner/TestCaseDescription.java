package org.apache.shardingsphere.test.integration.junit.runner;

import lombok.Builder;
import lombok.Getter;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.test.integration.cases.SQLCommandType;
import org.apache.shardingsphere.test.integration.common.ExecutionMode;
import org.apache.shardingsphere.test.integration.common.SQLExecuteType;
import org.apache.shardingsphere.test.integration.junit.annotation.StorageType;

@Getter
@Builder
@RequiredArgsConstructor
public class TestCaseDescription {
    
    @NonNull
    private final String database;
    
    @NonNull
    private final String scenario;
    
    @NonNull
    private final String adapter;
    
    @NonNull
    private final SQLExecuteType executeType;
    
    @NonNull
    private final SQLCommandType commandType;
    
    @NonNull
    private final ExecutionMode executionMode;
    
    public StorageType getStorageType() {
        return StorageType.valueOf(database);
    }
    
    public DatabaseType getDatabaseType() {
        return DatabaseTypeRegistry.getActualDatabaseType(database);
    }
}
