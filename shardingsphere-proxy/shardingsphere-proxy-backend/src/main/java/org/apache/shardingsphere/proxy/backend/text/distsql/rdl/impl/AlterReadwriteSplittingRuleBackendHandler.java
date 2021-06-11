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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl;

import com.google.common.base.Strings;
import org.apache.shardingsphere.distsql.parser.segment.rdl.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.distsql.parser.statement.rdl.alter.impl.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.exception.InvalidLoadBalancersException;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRulesNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter readwrite-splitting rule backend handler.
 */
public final class AlterReadwriteSplittingRuleBackendHandler extends RDLBackendHandler<AlterReadwriteSplittingRuleStatement> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }
    
    public AlterReadwriteSplittingRuleBackendHandler(final AlterReadwriteSplittingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement) {
        Optional<ReadwriteSplittingRuleConfiguration> ruleConfig = getReadwriteSplittingRuleConfiguration(schemaName);
        if (!ruleConfig.isPresent()) {
            throw new ReadwriteSplittingRulesNotExistedException(schemaName, getAlteredRuleNames(sqlStatement));
        }
        check(schemaName, sqlStatement, ruleConfig.get(), getAlteredRuleNames(sqlStatement));
    }
    
    @Override
    public void doExecute(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement) {
        ReadwriteSplittingRuleConfiguration alterReadwriteSplittingRuleConfiguration = new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singletonList(ReadwriteSplittingRuleStatementConverter.convert(sqlStatement))).stream()
                .map(each -> (ReadwriteSplittingRuleConfiguration) each).findFirst().get();
        ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration = getReadwriteSplittingRuleConfiguration(schemaName).get();
        drop(sqlStatement, readwriteSplittingRuleConfiguration);
        readwriteSplittingRuleConfiguration.getDataSources().addAll(alterReadwriteSplittingRuleConfiguration.getDataSources());
        readwriteSplittingRuleConfiguration.getLoadBalancers().putAll(alterReadwriteSplittingRuleConfiguration.getLoadBalancers());
    }
    
    private void drop(final AlterReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration) {
        getAlteredRuleNames(sqlStatement).forEach(each -> {
            ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfiguration = readwriteSplittingRuleConfiguration
                    .getDataSources().stream().filter(dataSource -> each.equals(dataSource.getName())).findAny().get();
            readwriteSplittingRuleConfiguration.getDataSources().remove(readwriteSplittingDataSourceRuleConfiguration);
            readwriteSplittingRuleConfiguration.getLoadBalancers().remove(readwriteSplittingDataSourceRuleConfiguration.getLoadBalancerName());
        });
    }
    
    private void check(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement,
                       final ReadwriteSplittingRuleConfiguration ruleConfig, final Collection<String> alteredRuleNames) {
        checkAlteredRules(schemaName, ruleConfig, alteredRuleNames);
        checkResources(sqlStatement, schemaName);
        checkLoadBalancer(sqlStatement);
    }
    
    private void checkAlteredRules(final String schemaName, final ReadwriteSplittingRuleConfiguration ruleConfig, final Collection<String> alteredRuleNames) {
        Set<String> existRuleNames = ruleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistRuleNames = alteredRuleNames.stream()
                .filter(each -> !existRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistRuleNames.isEmpty()) {
            throw new ReadwriteSplittingRulesNotExistedException(schemaName, notExistRuleNames);
        }
    }
    
    private void checkResources(final AlterReadwriteSplittingRuleStatement sqlStatement, final String schemaName) {
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getRules().stream().filter(each -> Strings.isNullOrEmpty(each.getAutoAwareResource())).forEach(each -> {
            resources.add(each.getWriteDataSource());
            resources.addAll(each.getReadDataSources());
        });
        Collection<String> notExistResources = getInvalidResources(schemaName, resources);
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }
    
    private void checkLoadBalancer(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> invalidLoadBalances = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidLoadBalances.isEmpty()) {
            throw new InvalidLoadBalancersException(invalidLoadBalances);
        }
    }
    
    private Collection<String> getAlteredRuleNames(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return sqlStatement.getRules()
                .stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
}
