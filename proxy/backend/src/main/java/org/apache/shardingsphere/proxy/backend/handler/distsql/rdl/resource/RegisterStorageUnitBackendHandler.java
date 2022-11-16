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
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.RegisterStorageUnitStatement;
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
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;
import org.apache.shardingsphere.sharding.distsql.handler.converter.ResourceSegmentsConverter;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Register storage unit backend handler.
 */
@Slf4j
public final class RegisterStorageUnitBackendHandler extends DatabaseRequiredBackendHandler<RegisterStorageUnitStatement> implements StorageUnitBackendHandler<RegisterStorageUnitStatement> {
    
    private final DatabaseType databaseType;
    
    private final DataSourcePropertiesValidator validator;
    
    public RegisterStorageUnitBackendHandler(final RegisterStorageUnitStatement sqlStatement, final ConnectionSession connectionSession) {
        super(sqlStatement, connectionSession);
        databaseType = connectionSession.getProtocolType();
        validator = new DataSourcePropertiesValidator();
    }
    
    @Override
    public ResponseHeader execute(final String databaseName, final RegisterStorageUnitStatement sqlStatement) {
        checkSQLStatement(databaseName, sqlStatement);
        Map<String, DataSourceProperties> dataSourcePropsMap = ResourceSegmentsConverter.convert(databaseType, sqlStatement.getStorageUnits());
        validator.validate(dataSourcePropsMap);
        try {
            ProxyContext.getInstance().getContextManager().addResources(databaseName, dataSourcePropsMap);
        } catch (final SQLException | ShardingSphereServerException ex) {
            log.error("Register storage unit failed", ex);
            throw new InvalidResourcesException(Collections.singleton(ex.getMessage()));
        }
        return new UpdateResponseHeader(sqlStatement);
    }
    
    @Override
    public void checkSQLStatement(final String databaseName, final RegisterStorageUnitStatement sqlStatement) {
        Collection<String> dataSourceNames = new ArrayList<>(sqlStatement.getStorageUnits().size());
        checkDuplicatedDataSourceNames(databaseName, dataSourceNames, sqlStatement);
        checkDuplicatedDataSourceNameWithReadwriteSplittingRule(databaseName, dataSourceNames);
    }
    
    private void checkDuplicatedDataSourceNames(final String databaseName, final Collection<String> dataSourceNames, final RegisterStorageUnitStatement sqlStatement) {
        Collection<String> duplicatedDataSourceNames = new HashSet<>(sqlStatement.getStorageUnits().size(), 1);
        for (DataSourceSegment each : sqlStatement.getStorageUnits()) {
            if (dataSourceNames.contains(each.getName()) || ProxyContext.getInstance().getDatabase(databaseName).getResourceMetaData().getDataSources().containsKey(each.getName())) {
                duplicatedDataSourceNames.add(each.getName());
            }
            dataSourceNames.add(each.getName());
        }
        ShardingSpherePreconditions.checkState(duplicatedDataSourceNames.isEmpty(), () -> new DuplicateResourceException(duplicatedDataSourceNames));
    }
    
    private void checkDuplicatedDataSourceNameWithReadwriteSplittingRule(final String databaseName, final Collection<String> requiredDataSourceNames) {
        // TODO use SPI to decouple features
        Optional<ReadwriteSplittingRule> rule = ProxyContext.getInstance().getDatabase(databaseName).getRuleMetaData().findSingleRule(ReadwriteSplittingRule.class);
        if (!rule.isPresent()) {
            return;
        }
        ReadwriteSplittingRuleConfiguration ruleConfig = (ReadwriteSplittingRuleConfiguration) rule.get().getConfiguration();
        Collection<String> existedRuleNames = ruleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> duplicatedDataSourceNames = requiredDataSourceNames.stream().filter(existedRuleNames::contains).collect(Collectors.toSet());
        ShardingSpherePreconditions.checkState(duplicatedDataSourceNames.isEmpty(),
                () -> new InvalidResourcesException(Collections.singleton(String.format("%s already exists in readwrite splitting", duplicatedDataSourceNames))));
    }
}
