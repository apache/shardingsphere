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
import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.distsql.update.RDLAlterUpdater;
import org.apache.shardingsphere.infra.exception.rule.ResourceNotExistedException;
import org.apache.shardingsphere.infra.exception.rule.RuleDefinitionViolationException;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.exception.InvalidLoadBalancersException;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.exception.ReadwriteSplittingRuleNotExistedException;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Alter readwrite-splitting rule statement updater.
 */
public final class AlterReadwriteSplittingRuleStatementUpdater implements RDLAlterUpdater<AlterReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, 
                                  final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) throws RuleDefinitionViolationException {
        checkCurrentRuleConfiguration(schemaName, sqlStatement, currentRuleConfig);
        checkToBeAlteredRules(schemaName, sqlStatement, currentRuleConfig);
        checkToBeAlteredResources(schemaName, sqlStatement, resource);
        checkToBeAlteredLoadBalancer(sqlStatement);
    }
    
    private void checkCurrentRuleConfiguration(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, 
                                               final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws ReadwriteSplittingRuleNotExistedException {
        if (null == currentRuleConfig) {
            throw new ReadwriteSplittingRuleNotExistedException(schemaName, getToBeAlteredRuleNames(sqlStatement));
        }
    }
    
    private void checkToBeAlteredRules(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, 
                                       final ReadwriteSplittingRuleConfiguration currentRuleConfig) throws ReadwriteSplittingRuleNotExistedException {
        Collection<String> currentRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistedRuleNames = getToBeAlteredRuleNames(sqlStatement).stream().filter(each -> !currentRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new ReadwriteSplittingRuleNotExistedException(schemaName, notExistedRuleNames);
        }
    }
    
    private Collection<String> getToBeAlteredRuleNames(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
    
    private void checkToBeAlteredLoadBalancer(final AlterReadwriteSplittingRuleStatement sqlStatement) throws InvalidLoadBalancersException {
        Collection<String> invalidLoadBalances = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!invalidLoadBalances.isEmpty()) {
            throw new InvalidLoadBalancersException(invalidLoadBalances);
        }
    }
    
    private void checkToBeAlteredResources(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, 
                                           final ShardingSphereResource resource) throws ResourceNotExistedException {
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getRules().stream().filter(each -> Strings.isNullOrEmpty(each.getAutoAwareResource())).forEach(each -> {
            resources.add(each.getWriteDataSource());
            resources.addAll(each.getReadDataSources());
        });
        Collection<String> notExistedResources = resource.getNotExistedResources(resources);
        if (!notExistedResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistedResources);
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        dropRuleConfiguration(sqlStatement, currentRuleConfig);
        addRuleConfiguration(sqlStatement, currentRuleConfig);
    }
    
    private void dropRuleConfiguration(final AlterReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        for (String each : getToBeAlteredRuleNames(sqlStatement)) {
            Optional<ReadwriteSplittingDataSourceRuleConfiguration> toBeRemovedDataSourceRuleConfig
                    = currentRuleConfig.getDataSources().stream().filter(dataSource -> each.equals(dataSource.getName())).findAny();
            Preconditions.checkState(toBeRemovedDataSourceRuleConfig.isPresent());
            currentRuleConfig.getDataSources().remove(toBeRemovedDataSourceRuleConfig.get());
            currentRuleConfig.getLoadBalancers().remove(toBeRemovedDataSourceRuleConfig.get().getLoadBalancerName());
        }
    }
    
    private void addRuleConfiguration(final AlterReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        ReadwriteSplittingRuleConfiguration toBeAlteredRuleConfig = ReadwriteSplittingRuleStatementConverter.convert(sqlStatement.getRules());
        currentRuleConfig.getDataSources().addAll(toBeAlteredRuleConfig.getDataSources());
        currentRuleConfig.getLoadBalancers().putAll(toBeAlteredRuleConfig.getLoadBalancers());
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getRuleConfigurationClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterReadwriteSplittingRuleStatement.class.getCanonicalName();
    }
}
