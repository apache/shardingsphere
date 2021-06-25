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

package org.apache.shardingsphere.proxy.backend.text.distsql.rdl.impl.updater;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.distsql.RDLUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.exception.InvalidLoadBalancersException;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRuleNotExistedException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.segment.ReadwriteSplittingRuleSegment;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;
import org.apache.shardingsphere.readwritesplitting.yaml.converter.ReadwriteSplittingRuleStatementConverter;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Alter readwrite-splitting rule statement updater.
 */
public final class AlterReadwriteSplittingRuleStatementUpdater implements RDLUpdater<AlterReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }
    
    @Override
    public void checkSQLStatement(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, 
                                  final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        if (null == currentRuleConfig) {
            throw new ReadwriteSplittingRuleNotExistedException(schemaName, getAlteredRuleNames(sqlStatement));
        }
        check(schemaName, sqlStatement, getAlteredRuleNames(sqlStatement), currentRuleConfig, resource);
    }
    
    private void check(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement,
                       final Collection<String> alteredRuleNames, final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        checkAlteredRules(schemaName, alteredRuleNames, currentRuleConfig);
        checkResources(schemaName, sqlStatement, resource);
        checkLoadBalancer(sqlStatement);
    }
    
    private void checkAlteredRules(final String schemaName, final Collection<String> alteredRuleNames, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        Set<String> existRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toSet());
        Collection<String> notExistRuleNames = alteredRuleNames.stream()
                .filter(each -> !existRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistRuleNames.isEmpty()) {
            throw new ReadwriteSplittingRuleNotExistedException(schemaName, notExistRuleNames);
        }
    }
    
    private void checkResources(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, final ShardingSphereResource resource) {
        Collection<String> resources = new LinkedHashSet<>();
        sqlStatement.getRules().stream().filter(each -> Strings.isNullOrEmpty(each.getAutoAwareResource())).forEach(each -> {
            resources.add(each.getWriteDataSource());
            resources.addAll(each.getReadDataSources());
        });
        Collection<String> notExistResources = resource.getNotExistedResources(resources);
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }
    
    private void checkLoadBalancer(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> invalidLoadBalances = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent()).collect(Collectors.toList());
        if (!invalidLoadBalances.isEmpty()) {
            throw new InvalidLoadBalancersException(invalidLoadBalances);
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final AlterReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        ReadwriteSplittingRuleConfiguration alterReadwriteSplittingRuleConfig = new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singletonList(ReadwriteSplittingRuleStatementConverter.convert(sqlStatement))).stream()
                .map(each -> (ReadwriteSplittingRuleConfiguration) each).findFirst().get();
        drop(sqlStatement, currentRuleConfig);
        currentRuleConfig.getDataSources().addAll(alterReadwriteSplittingRuleConfig.getDataSources());
        currentRuleConfig.getLoadBalancers().putAll(alterReadwriteSplittingRuleConfig.getLoadBalancers());
    }
    
    private void drop(final AlterReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration ruleConfig) {
        getAlteredRuleNames(sqlStatement).forEach(each -> {
            ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = ruleConfig.getDataSources().stream().filter(dataSource -> each.equals(dataSource.getName())).findAny().get();
            ruleConfig.getDataSources().remove(dataSourceRuleConfig);
            ruleConfig.getLoadBalancers().remove(dataSourceRuleConfig.getLoadBalancerName());
        });
    }
    
    private Collection<String> getAlteredRuleNames(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return sqlStatement.getRules()
                .stream().map(ReadwriteSplittingRuleSegment::getName).collect(Collectors.toSet());
    }
    
    @Override
    public String getType() {
        return AlterReadwriteSplittingRuleStatement.class.getCanonicalName();
    }
}
