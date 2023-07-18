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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.storage.unit;

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePropertiesValidateHandler;
import org.apache.shardingsphere.distsql.parser.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.parser.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.util.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Register storage unit backend handler.
 */
@Slf4j
public final class RegisterStorageUnitBackendHandler extends StorageUnitDefinitionBackendHandler<RegisterStorageUnitStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourcePropertiesValidateHandler validateHandler;
    
    public RegisterStorageUnitBackendHandler(final RegisterStorageUnitStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
        databaseType = connectionSession.getProtocolType();
        validateHandler = new DataSourcePropertiesValidateHandler();
    }
    
    @Override
    public ResponseHeader execute(final String databaseName, final RegisterStorageUnitStatement sqlStatement) {
        checkSQLStatement(databaseName, sqlStatement);
        Map<String, DataSourceProperties> dataSourcePropsMap = DataSourceSegmentsConverter.convert(databaseType, sqlStatement.getStorageUnits());
        if (sqlStatement.isIfNotExists()) {
            Collection<String> currentStorageUnits = getCurrentStorageUnitNames(databaseName);
            Collection<String> logicalDataSourceNames = getLogicalDataSourceNames(databaseName);
            dataSourcePropsMap.keySet().removeIf(currentStorageUnits::contains);
            dataSourcePropsMap.keySet().removeIf(logicalDataSourceNames::contains);
        }
        if (dataSourcePropsMap.isEmpty()) {
            return new UpdateResponseHeader(sqlStatement);
        }
        validateHandler.validate(dataSourcePropsMap);
        try {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().registerStorageUnits(databaseName, dataSourcePropsMap);
        } catch (final SQLException | ShardingSphereExternalException ex) {
            log.error("Register storage unit failed", ex);
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    @Override
    public void checkSQLStatement(final String databaseName, final RegisterStorageUnitStatement sqlStatement) {
        Collection<String> dataSourceNames = new ArrayList<>(sqlStatement.getStorageUnits().size());
        if (!sqlStatement.isIfNotExists()) {
            checkDuplicatedDataSourceNames(databaseName, dataSourceNames, sqlStatement);
            checkDuplicatedLogicalDataSourceNames(databaseName, dataSourceNames);
        }
    }
    
    private void checkDuplicatedDataSourceNames(final String databaseName, final Collection<String> dataSourceNames, final RegisterStorageUnitStatement sqlStatement) {
        Collection<String> duplicatedDataSourceNames = new HashSet<>(sqlStatement.getStorageUnits().size(), 1F);
        for (DataSourceSegment each : sqlStatement.getStorageUnits()) {
            if (dataSourceNames.contains(each.getName()) || getCurrentStorageUnitNames(databaseName).contains(each.getName())) {
                duplicatedDataSourceNames.add(each.getName());
            }
            dataSourceNames.add(each.getName());
        }
        ShardingSpherePreconditions.checkState(duplicatedDataSourceNames.isEmpty(), () -> new DuplicateStorageUnitException(duplicatedDataSourceNames));
    }
    
    private void checkDuplicatedLogicalDataSourceNames(final String databaseName, final Collection<String> requiredDataSourceNames) {
        Collection<String> logicalDataSourceNames = getLogicalDataSourceNames(databaseName);
        if (logicalDataSourceNames.isEmpty()) {
            return;
        }
        Collection<String> duplicatedDataSourceNames = requiredDataSourceNames.stream().filter(logicalDataSourceNames::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedDataSourceNames.isEmpty(),
                () -> new InvalidStorageUnitsException(Collections.singleton(String.format("%s already existed in rule", duplicatedDataSourceNames))));
    }
    
    private Collection<String> getCurrentStorageUnitNames(final String databaseName) {
        return ProxyContext.getInstance().getContextManager().getDataSourceMap(databaseName).keySet();
    }
    
    private Collection<String> getLogicalDataSourceNames(final String databaseName) {
        return ProxyContext.getInstance().getDatabase(databaseName).getRuleMetaData().findRules(DataSourceContainedRule.class).stream()
                .map(each -> each.getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toList());
    }
}
