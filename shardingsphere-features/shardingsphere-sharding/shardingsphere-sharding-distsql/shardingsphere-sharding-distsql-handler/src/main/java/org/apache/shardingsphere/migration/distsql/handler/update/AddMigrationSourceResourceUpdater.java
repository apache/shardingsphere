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

import org.apache.shardingsphere.data.pipeline.api.MigrationJobPublicAPI;
import org.apache.shardingsphere.data.pipeline.api.PipelineJobPublicAPIFactory;
import org.apache.shardingsphere.data.pipeline.core.exception.connection.AddMigrationSourceResourceException;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.HostnameAndPortBasedDataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.URLBasedDataSourceSegment;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.distsql.update.RALUpdater;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.sql.type.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.migration.distsql.statement.AddMigrationSourceResourceStatement;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ResourceSegmentsConverter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Add migration source resource updater.
 */
public final class AddMigrationSourceResourceUpdater implements RALUpdater<AddMigrationSourceResourceStatement> {
    
    private static final MigrationJobPublicAPI JOB_API = PipelineJobPublicAPIFactory.getMigrationJobPublicAPI();
    
    @Override
    public void executeUpdate(final String databaseName, final AddMigrationSourceResourceStatement sqlStatement) {
        List<DataSourceSegment> dataSources = new ArrayList<>(sqlStatement.getDataSources());
        ShardingSpherePreconditions.checkState(dataSources.stream().noneMatch(each -> each instanceof HostnameAndPortBasedDataSourceSegment),
                () -> new UnsupportedSQLOperationException("Not currently support add hostname and port, please use url"));
        URLBasedDataSourceSegment urlBasedDataSourceSegment = (URLBasedDataSourceSegment) dataSources.get(0);
        DatabaseType databaseType = DatabaseTypeEngine.getDatabaseType(urlBasedDataSourceSegment.getUrl());
        Map<String, DataSourceProperties> sourcePropertiesMap = ResourceSegmentsConverter.convert(databaseType, dataSources);
        DataSourcePropertiesValidator validator = new DataSourcePropertiesValidator();
        try {
            validator.validate(sourcePropertiesMap, databaseType);
        } catch (final InvalidResourcesException ex) {
            throw new AddMigrationSourceResourceException(ex);
        }
        JOB_API.addMigrationSourceResources(sourcePropertiesMap);
    }
    
    @Override
    public String getType() {
        return AddMigrationSourceResourceStatement.class.getName();
    }
}
