/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.MigrationJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.distsql.statement.updatable.RegisterMigrationSourceStorageUnitStatement;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorClusterModeRequired;
import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Register migration source storage unit executor.
 */
@DistSQLExecutorClusterModeRequired
public final class RegisterMigrationSourceStorageUnitExecutor implements DistSQLUpdateExecutor<RegisterMigrationSourceStorageUnitStatement> {
    
    private final MigrationJobAPI jobAPI = (MigrationJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION");
    
    private final DistSQLDataSourcePoolPropertiesValidator validateHandler = new DistSQLDataSourcePoolPropertiesValidator();
    
    @Override
    public void executeUpdate(final RegisterMigrationSourceStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        checkDataSource(sqlStatement);
        List<DataSourceSegment> dataSources = new ArrayList<>(sqlStatement.getDataSources());
        URLBasedDataSourceSegment urlBasedDataSourceSegment = (URLBasedDataSourceSegment) dataSources.get(0);
        DatabaseType databaseType = DatabaseTypeFactory.get(urlBasedDataSourceSegment.getUrl());
        Map<String, DataSourcePoolProperties> propsMap = DataSourceSegmentsConverter.convert(databaseType, dataSources);
        validateHandler.validate(propsMap);
        jobAPI.registerMigrationSourceStorageUnits(new PipelineContextKey(InstanceType.PROXY), propsMap);
    }
    
    private void checkDataSource(final RegisterMigrationSourceStorageUnitStatement sqlStatement) {
        ShardingSpherePreconditions.checkState(sqlStatement.getDataSources().stream().noneMatch(HostnameAndPortBasedDataSourceSegment.class::isInstance),
                () -> new UnsupportedSQLOperationException("Not currently support add hostname and port, please use url"));
    }
    
    @Override
    public Class<RegisterMigrationSourceStorageUnitStatement> getType() {
        return RegisterMigrationSourceStorageUnitStatement.class;
    }
}
