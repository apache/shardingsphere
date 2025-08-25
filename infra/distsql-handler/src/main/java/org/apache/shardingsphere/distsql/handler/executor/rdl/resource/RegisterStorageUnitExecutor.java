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

package org.apache.shardingsphere.distsql.handler.executor.rdl.resource;

import lombok.Setter;
import org.apache.shardingsphere.database.connector.core.checker.PrivilegeCheckType;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorDatabaseAware;
import org.apache.shardingsphere.distsql.handler.engine.update.DistSQLUpdateExecutor;
import org.apache.shardingsphere.distsql.handler.validate.DistSQLDataSourcePoolPropertiesValidator;
import org.apache.shardingsphere.distsql.segment.DataSourceSegment;
import org.apache.shardingsphere.distsql.segment.converter.DataSourceSegmentsConverter;
import org.apache.shardingsphere.distsql.statement.type.rdl.resource.unit.type.RegisterStorageUnitStatement;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.ShardingSphereExternalException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.DuplicateStorageUnitException;
import org.apache.shardingsphere.infra.exception.kernel.metadata.resource.storageunit.StorageUnitsOperateException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.attribute.datasource.DataSourceMapperRuleAttribute;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Register storage unit executor.
 */
@Setter
public final class RegisterStorageUnitExecutor implements DistSQLUpdateExecutor<RegisterStorageUnitStatement>, DistSQLExecutorDatabaseAware {
    
    private final DistSQLDataSourcePoolPropertiesValidator validateHandler = new DistSQLDataSourcePoolPropertiesValidator();
    
    private ShardingSphereDatabase database;
    
    @Override
    public void executeUpdate(final RegisterStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        checkDataSource(sqlStatement, contextManager);
        Map<String, DataSourcePoolProperties> propsMap = DataSourceSegmentsConverter.convert(database.getProtocolType(), sqlStatement.getStorageUnits());
        if (sqlStatement.isIfNotExists()) {
            Collection<String> currentStorageUnits = getCurrentStorageUnitNames(contextManager);
            Collection<String> logicalDataSourceNames = getLogicalDataSourceNames();
            propsMap.keySet().removeIf(currentStorageUnits::contains);
            propsMap.keySet().removeIf(logicalDataSourceNames::contains);
        }
        if (propsMap.isEmpty()) {
            return;
        }
        validateHandler.validate(propsMap, getExpectedPrivileges(sqlStatement));
        try {
            contextManager.getPersistServiceFacade().getModeFacade().getMetaDataManagerService().registerStorageUnits(database.getName(), propsMap);
        } catch (final ShardingSphereExternalException ex) {
            throw new StorageUnitsOperateException("register", propsMap.keySet(), ex);
        }
    }
    
    private void checkDataSource(final RegisterStorageUnitStatement sqlStatement, final ContextManager contextManager) {
        if (!sqlStatement.isIfNotExists()) {
            Collection<String> dataSourceNames = new ArrayList<>(sqlStatement.getStorageUnits().size());
            checkDuplicatedDataSourceNames(contextManager, dataSourceNames, sqlStatement);
            checkDuplicatedLogicalDataSourceNames(dataSourceNames);
        }
    }
    
    private void checkDuplicatedDataSourceNames(final ContextManager contextManager, final Collection<String> dataSourceNames, final RegisterStorageUnitStatement sqlStatement) {
        Collection<String> duplicatedDataSourceNames = new HashSet<>(sqlStatement.getStorageUnits().size(), 1F);
        for (DataSourceSegment each : sqlStatement.getStorageUnits()) {
            if (dataSourceNames.contains(each.getName()) || getCurrentStorageUnitNames(contextManager).contains(each.getName())) {
                duplicatedDataSourceNames.add(each.getName());
            }
            dataSourceNames.add(each.getName());
        }
        ShardingSpherePreconditions.checkMustEmpty(duplicatedDataSourceNames, () -> new DuplicateStorageUnitException(database.getName(), duplicatedDataSourceNames));
    }
    
    private void checkDuplicatedLogicalDataSourceNames(final Collection<String> requiredDataSourceNames) {
        Collection<String> logicalDataSourceNames = getLogicalDataSourceNames();
        if (logicalDataSourceNames.isEmpty()) {
            return;
        }
        Collection<String> duplicatedDataSourceNames = requiredDataSourceNames.stream().filter(logicalDataSourceNames::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkMustEmpty(duplicatedDataSourceNames, () -> new DuplicateStorageUnitException(database.getName(), duplicatedDataSourceNames));
    }
    
    private Collection<String> getCurrentStorageUnitNames(final ContextManager contextManager) {
        return contextManager.getStorageUnits(database.getName()).keySet();
    }
    
    private Collection<String> getLogicalDataSourceNames() {
        return database.getRuleMetaData().getAttributes(DataSourceMapperRuleAttribute.class).stream().flatMap(each -> each.getDataSourceMapper().keySet().stream()).collect(Collectors.toList());
    }
    
    private Collection<PrivilegeCheckType> getExpectedPrivileges(final RegisterStorageUnitStatement sqlStatement) {
        Collection<PrivilegeCheckType> result = sqlStatement.getExpectedPrivileges().stream().map(each -> PrivilegeCheckType.valueOf(each.toUpperCase())).collect(Collectors.toSet());
        if (result.isEmpty()) {
            result.add(PrivilegeCheckType.SELECT);
        }
        return result;
    }
    
    @Override
    public Class<RegisterStorageUnitStatement> getType() {
        return RegisterStorageUnitStatement.class;
    }
}
