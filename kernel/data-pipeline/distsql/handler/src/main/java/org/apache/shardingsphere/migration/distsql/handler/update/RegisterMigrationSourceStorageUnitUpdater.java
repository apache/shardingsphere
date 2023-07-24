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

package org.apache.shardingsphere.migration.distsql.handler.update;

import org.apache.shardingsphere.data.pipeline.common.context.PipelineContextKey;
import org.apache.shardingsphere.data.pipeline.scenario.migration.api.impl.MigrationJobAPI;
import org.apache.shardingsphere.distsql.handler.ral.update.RALUpdater;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeFactory;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.migration.distsql.statement.RegisterMigrationSourceStorageUnitStatement;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Register migration source storage unit updater.
 */
public final class RegisterMigrationSourceStorageUnitUpdater implements RALUpdater<RegisterMigrationSourceStorageUnitStatement> {
    
    private final MigrationJobAPI jobAPI = new MigrationJobAPI();
    
    private final DataSourcePropertiesValidateHandler validateHandler = new DataSourcePropertiesValidateHandler();
    
    @Override
    public void executeUpdate(final String databaseName, final RegisterMigrationSourceStorageUnitStatement sqlStatement) {
        List<DataSourceSegment> dataSources = new ArrayList<>(sqlStatement.getDataSources());
        ShardingSpherePreconditions.checkState(dataSources.stream().noneMatch(HostnameAndPortBasedDataSourceSegment.class::isInstance),
                () -> new UnsupportedSQLOperationException("Not currently support add hostname and port, please use url"));
        URLBasedDataSourceSegment urlBasedDataSourceSegment = (URLBasedDataSourceSegment) dataSources.get(0);
        DatabaseType databaseType = DatabaseTypeFactory.get(urlBasedDataSourceSegment.getUrl());
        Map<String, DataSourceProperties> sourcePropertiesMap = DataSourceSegmentsConverter.convert(databaseType, dataSources);
        validateHandler.validate(sourcePropertiesMap);
        jobAPI.addMigrationSourceResources(PipelineContextKey.buildForProxy(), sourcePropertiesMap);
    }
    
    @Override
    public String getType() {
        return RegisterMigrationSourceStorageUnitStatement.class.getName();
    }
}
