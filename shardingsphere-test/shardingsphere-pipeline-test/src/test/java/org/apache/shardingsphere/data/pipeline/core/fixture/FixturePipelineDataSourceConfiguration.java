package org.apache.shardingsphere.data.pipeline.core.fixture;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.api.datasource.config.PipelineDataSourceConfiguration;
import org.apache.shardingsphere.infra.database.type.DatabaseType;

@RequiredArgsConstructor
public final class FixturePipelineDataSourceConfiguration implements PipelineDataSourceConfiguration {
    
    private final DatabaseType databaseType;
    
    @Override
    public String getParameter() {
        return null;
    }
    
    @Override
    public Object getDataSourceConfiguration() {
        return null;
    }
    
    @Override
    public DatabaseType getDatabaseType() {
        return databaseType;
    }
    
    @Override
    public String getType() {
        return "FIXTURE";
    }
}
