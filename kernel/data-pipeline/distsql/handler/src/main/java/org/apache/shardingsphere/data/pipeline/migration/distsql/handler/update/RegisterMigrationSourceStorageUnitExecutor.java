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

package org.apache.shardingsphere.data.pipeline.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.core.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.core.job.api.TransmissionJobAPI;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.MigrationJobAPI;
import org.apache.shardingsphere.distsql.handler.type.ral.update.UpdatableRALExecutor;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePoolPropertiesValidateHandler;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.data.pipeline.migration.distsql.statement.RegisterMigrationSourceStorageUnitStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Register migration source storage unit executor.
 */
public final class RegisterMigrationSourceStorageUnitExecutor implements UpdatableRALExecutor<RegisterMigrationSourceStorageUnitStatement> {
    
    private final MigrationJobAPI jobAPI = (MigrationJobAPI) TypedSPILoader.getService(TransmissionJobAPI.class, "MIGRATION");
    
    private final DataSourcePoolPropertiesValidateHandler validateHandler = new DataSourcePoolPropertiesValidateHandler();
    
    @Override
    public void executeUpdate(final RegisterMigrationSourceStorageUnitStatement sqlStatement) {
        List<DataSourceSegment> dataSources = new ArrayList<>(sqlStatement.getDataSources());
        ShardingSpherePreconditions.checkState(dataSources.stream().noneMatch(HostnameAndPortBasedDataSourceSegment.class::isInstance),
                () -> new UnsupportedSQLOperationException("Not currently support add hostname and port, please use url"));
        URLBasedDataSourceSegment urlBasedDataSourceSegment = (URLBasedDataSourceSegment) dataSources.get(0);
        DatabaseType databaseType = DatabaseTypeFactory.get(urlBasedDataSourceSegment.getUrl());
        Map<String, DataSourcePoolProperties> propsMap = DataSourceSegmentsConverter.convert(databaseType, dataSources);
        validateHandler.validate(propsMap);
        jobAPI.addMigrationSourceResources(new PipelineContextKey(InstanceType.PROXY), propsMap);
    }
    
    @Override
    public Class<RegisterMigrationSourceStorageUnitStatement> getType() {
        return RegisterMigrationSourceStorageUnitStatement.class;
    }
}
