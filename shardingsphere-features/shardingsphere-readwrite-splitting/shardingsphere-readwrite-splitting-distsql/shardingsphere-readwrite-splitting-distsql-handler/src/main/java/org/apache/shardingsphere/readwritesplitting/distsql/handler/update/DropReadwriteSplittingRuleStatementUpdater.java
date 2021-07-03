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

package org.apache.shardingsphere.readwritesplitting.distsql.handler.update;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.distsql.update.RDLDropUpdater;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.exception.ReadwriteSplittingRuleNotExistedException;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop readwrite-splitting rule statement updater.
 */
public final class DropReadwriteSplittingRuleStatementUpdater implements RDLDropUpdater<DropReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement, 
                                  final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
        checkCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
        checkToBeDroppedRuleNames(schemaName, sqlStatement, currentRuleConfig);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement, 
                                               final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws ReadwriteSplittingRuleNotExistedException {
        if (null == currentRuleConfig) {
            throw new ReadwriteSplittingRuleNotExistedException(schemaName, sqlStatement.getRuleNames());
        }
    }
    
    private void checkToBeDroppedRuleNames(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement, 
                                           final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws ReadwriteSplittingRuleNotExistedException {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new ReadwriteSplittingRuleNotExistedException(schemaName, sqlStatement.getRuleNames());
        }
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        for (String each : sqlStatement.getRuleNames()) {
            dropRule(currentRuleConfig, each);
        }
        return currentRuleConfig.getDataSources().isEmpty();
    }
    
    private void dropRule(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final String ruleName) {
        Optional<ReadwriteSplittingDataSourceRuleConfiguration> dataSourceRuleConfig
                = currentRuleConfig.getDataSources().stream().filter(dataSource -> ruleName.equals(dataSource.getName())).findAny();
        Preconditions.checkState(dataSourceRuleConfig.isPresent());
        currentRuleConfig.getDataSources().remove(dataSourceRuleConfig.get());
        currentRuleConfig.getLoadBalancers().remove(dataSourceRuleConfig.get().getLoadBalancerName());
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getRuleConfigurationClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropReadwriteSplittingRuleStatement.class.getCanonicalName();
    }
}
