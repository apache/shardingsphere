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

package org.apache.shardingsphere.dbdiscovery.distsql.update;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.dbdiscovery.api.config.DatabaseDiscoveryRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.api.config.rule.DatabaseDiscoveryDataSourceRuleConfiguration;
import org.apache.shardingsphere.dbdiscovery.distsql.exception.DatabaseDiscoveryRuleNotExistedException;
import org.apache.shardingsphere.dbdiscovery.distsql.parser.statement.DropDatabaseDiscoveryRuleStatement;
import org.apache.shardingsphere.infra.distsql.update.RDLDropUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop database discovery rule statement updater.
 */
public final class DropDatabaseDiscoveryRuleStatementUpdater implements RDLDropUpdater<DropDatabaseDiscoveryRuleStatement, DatabaseDiscoveryRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement, 
                                  final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
        checkToBeDroppedRuleNames(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        if (null == currentRuleConfig) {
            throw new DatabaseDiscoveryRuleNotExistedException(schemaName, sqlStatement.getRuleNames());
        }
    }
    
    private void checkToBeDroppedRuleNames(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(DatabaseDiscoveryDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new DatabaseDiscoveryRuleNotExistedException(schemaName, notExistedRuleNames);
        }
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final String schemaName, final DropDatabaseDiscoveryRuleStatement sqlStatement, final DatabaseDiscoveryRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getRuleNames()) {
            dropRule(currentRuleConfig, each);
        }
        return currentRuleConfig.getDataSources().isEmpty();
    }
    
    private void dropRule(final DatabaseDiscoveryRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<DatabaseDiscoveryDataSourceRuleConfiguration> dataSourceRuleConfig = currentRuleConfig.getDataSources().stream().filter(dataSource -> dataSource.getName().equals(ruleName)).findAny();
        Preconditions.checkState(dataSourceRuleConfig.isPresent());
        currentRuleConfig.getDataSources().remove(dataSourceRuleConfig.get());
        // TODO Do we need to check DiscoveryType not in use before drop it? 
        currentRuleConfig.getDiscoveryTypes().remove(dataSourceRuleConfig.get().getDiscoveryTypeName());
    }
    
    @Override
    public String getType() {
        return DropDatabaseDiscoveryRuleStatement.class.getCanonicalName();
    }
}
