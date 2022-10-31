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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.AddResourceStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.DataSourcePropertiesValidator;
import org.apache.shardingsphere.infra.distsql.exception.resource.DuplicateResourceException;
import org.apache.shardingsphere.infra.distsql.exception.resource.InvalidResourcesException;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.server.ShardingSphereServerException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.DatabaseRequiredBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ResourceSegmentsConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;

/**
 * Add resource backend handler.
 */
@Slf4j
public final class AddResourceBackendHandler extends DatabaseRequiredBackendHandler<AddResourceStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourcePropertiesValidator validator;
    
    public AddResourceBackendHandler(final AddResourceStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
        databaseType = connectionSession.getDatabaseType();
        validator = new DataSourcePropertiesValidator();
    }
    
    @Override
    public ResponseHeader execute(final String databaseName, final AddResourceStatement sqlStatement) {
        checkSQLStatement(databaseName, sqlStatement);
        Map<String, DataSourceProperties> dataSourcePropsMap = ResourceSegmentsConverter.convert(databaseType, sqlStatement.getDataSources());
        DatabaseType storeType = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData().getDatabaseType();
        validator.validate(dataSourcePropsMap, storeType);
        try {
            ProxyContext.getInstance().getContextManager().addResources(databaseName, dataSourcePropsMap);
        } catch (final SQLException | ShardingSphereServerException ex) {
            log.error("Add resource failed", ex);
            throw new InvalidResourcesException(Collections.singleton(ex.getMessage()));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    private void checkSQLStatement(final String databaseName, final AddResourceStatement sqlStatement) {
        Collection<String> dataSourceNames = new ArrayList<>(sqlStatement.getDataSources().size());
        Collection<String> duplicateDataSourceNames = new HashSet<>(sqlStatement.getDataSources().size(), 1);
        for (DataSourceSegment each : sqlStatement.getDataSources()) {
            if (dataSourceNames.contains(each.getName()) || ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources().containsKey(each.getName())) {
                duplicateDataSourceNames.add(each.getName());
            }
            dataSourceNames.add(each.getName());
        }
        ShardingSpherePreconditions.checkState(duplicateDataSourceNames.isEmpty(), () -> new DuplicateResourceException(duplicateDataSourceNames));
    }
}
