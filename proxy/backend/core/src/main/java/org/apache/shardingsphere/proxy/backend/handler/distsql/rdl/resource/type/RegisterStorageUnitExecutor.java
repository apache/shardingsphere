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

package org.apache.shardingsphere.proxy.backend.handler.distsql.rdl.resource.type;

import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.InvalidStorageUnitsException;
import org.apache.shardingsphere.distsql.handler.type.rdl.resource.aware.DatabaseAwareResourceDefinitionExecutor;
import org.apache.shardingsphere.distsql.handler.validate.DataSourcePoolPropertiesValidateHandler;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.distsql.statement.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.core.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.identifier.type.DataSourceContainedRule;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Register storage unit executor.
 */
@Setter
@Slf4j
public final class RegisterStorageUnitExecutor implements DatabaseAwareResourceDefinitionExecutor<RegisterStorageUnitStatement> {
    
    private final DataSourcePoolPropertiesValidateHandler validateHandler = new DataSourcePoolPropertiesValidateHandler();
    
    private ShardingSphereDatabase database;
    
    @Override
    public void execute(final RegisterStorageUnitStatement sqlStatement) {
        checkSQLStatement(sqlStatement);
        Map<String, DataSourcePoolProperties> propsMap = DataSourceSegmentsConverter.convert(database.getProtocolType(), sqlStatement.getStorageUnits());
        if (sqlStatement.isIfNotExists()) {
            Collection<String> currentStorageUnits = getCurrentStorageUnitNames();
            Collection<String> logicalDataSourceNames = getLogicalDataSourceNames();
            propsMap.keySet().removeIf(currentStorageUnits::contains);
            propsMap.keySet().removeIf(logicalDataSourceNames::contains);
        }
        if (propsMap.isEmpty()) {
            return;
        }
        validateHandler.validate(propsMap);
        try {
            ProxyContext.getInstance().getContextManager().getInstanceContext().getModeContextManager().registerStorageUnits(database.getName(), propsMap);
        } catch (final SQLException | ShardingSphereExternalException ex) {
            log.error("Register storage unit failed", ex);
            throw new InvalidStorageUnitsException(Collections.singleton(ex.getMessage()));
        }
    }
    
    private void checkSQLStatement(final RegisterStorageUnitStatement sqlStatement) {
        Collection<String> dataSourceNames = new ArrayList<>(sqlStatement.getStorageUnits().size());
        if (!sqlStatement.isIfNotExists()) {
            checkDuplicatedDataSourceNames(dataSourceNames, sqlStatement);
            checkDuplicatedLogicalDataSourceNames(dataSourceNames);
        }
    }
    
    private void checkDuplicatedDataSourceNames(final Collection<String> dataSourceNames, final RegisterStorageUnitStatement sqlStatement) {
        Collection<String> duplicatedDataSourceNames = new HashSet<>(sqlStatement.getStorageUnits().size(), 1F);
        for (DataSourceSegment each : sqlStatement.getStorageUnits()) {
            if (dataSourceNames.contains(each.getName()) || getCurrentStorageUnitNames().contains(each.getName())) {
                duplicatedDataSourceNames.add(each.getName());
            }
            dataSourceNames.add(each.getName());
        }
        ShardingSpherePreconditions.checkState(duplicatedDataSourceNames.isEmpty(), () -> new DuplicateStorageUnitException(duplicatedDataSourceNames));
    }
    
    private void checkDuplicatedLogicalDataSourceNames(final Collection<String> requiredDataSourceNames) {
        Collection<String> logicalDataSourceNames = getLogicalDataSourceNames();
        if (logicalDataSourceNames.isEmpty()) {
            return;
        }
        Collection<String> duplicatedDataSourceNames = requiredDataSourceNames.stream().filter(logicalDataSourceNames::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedDataSourceNames.isEmpty(),
                () -> new InvalidStorageUnitsException(Collections.singleton(String.format("%s already existed in rule", duplicatedDataSourceNames))));
    }
    
    private Collection<String> getCurrentStorageUnitNames() {
        return ProxyContext.getInstance().getContextManager().getStorageUnits(database.getName()).keySet();
    }
    
    private Collection<String> getLogicalDataSourceNames() {
        return database.getRuleMetaData().findRules(DataSourceContainedRule.class).stream().map(each -> each.getDataSourceMapper().keySet()).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    @Override
    public Class<RegisterStorageUnitStatement> getType() {
        return RegisterStorageUnitStatement.class;
    }
}
