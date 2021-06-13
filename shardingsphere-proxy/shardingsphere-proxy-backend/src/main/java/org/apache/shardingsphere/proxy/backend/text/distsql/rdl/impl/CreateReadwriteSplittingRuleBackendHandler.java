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
import org.apache.shardingsphere.distsql.parser.statement.rdl.create.impl.CreateReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.typed.TypedSPIRegistry;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.DuplicateRuleNamesException;
import org.apache.shardingsphere.proxy.backend.exception.InvalidLoadBalancersException;
import org.apache.shardingsphere.proxy.backend.exception.ResourceNotExistedException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.config.YamlReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.yaml.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.spi.ReplicaLoadBalanceAlgorithm;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Create readwrite splitting rule backend handler.
 */
public final class CreateReadwriteSplittingRuleBackendHandler extends RDLBackendHandler<CreateReadwriteSplittingRuleStatement> {
    
    static {
        // TODO consider about register once only
        ShardingSphereServiceLoader.register(ReplicaLoadBalanceAlgorithm.class);
    }
    
    public CreateReadwriteSplittingRuleBackendHandler(final CreateReadwriteSplittingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement) {
        checkDuplicateRuleNames(schemaName, sqlStatement);
        checkResources(schemaName, sqlStatement);
        checkLoadBalancers(sqlStatement);
    }
    
    @Override
    public void doExecute(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement) {
        YamlReadwriteSplittingRuleConfiguration yamlReadwriteSplittingRuleConfiguration = ReadwriteSplittingRuleStatementConverter.convert(sqlStatement);
        ReadwriteSplittingRuleConfiguration createdReadwriteSplittingRuleConfiguration = new YamlRuleConfigurationSwapperEngine()
                .swapToRuleConfigurations(Collections.singleton(yamlReadwriteSplittingRuleConfiguration))
                .stream().filter(each -> each instanceof ReadwriteSplittingRuleConfiguration).findAny().map(each -> (ReadwriteSplittingRuleConfiguration) each).get();
        if (getReadwriteSplittingRuleConfiguration(schemaName).isPresent()) {
            ReadwriteSplittingRuleConfiguration existReadwriteSplittingRuleConfiguration = getReadwriteSplittingRuleConfiguration(schemaName).get();
            existReadwriteSplittingRuleConfiguration.getDataSources().addAll(createdReadwriteSplittingRuleConfiguration.getDataSources());
            existReadwriteSplittingRuleConfiguration.getLoadBalancers().putAll(createdReadwriteSplittingRuleConfiguration.getLoadBalancers());
        } else {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().add(createdReadwriteSplittingRuleConfiguration);
        }
    }
    
    private void checkDuplicateRuleNames(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement) {
        Optional<ReadwriteSplittingRuleConfiguration> optional = getReadwriteSplittingRuleConfiguration(schemaName);
        if (optional.isPresent()) {
            Collection<String> existRuleNames = getRuleNames(optional.get());
            Collection<String> duplicateRuleNames = sqlStatement.getRules()
                    .stream().map(ReadwriteSplittingRuleSegment::getName).filter(existRuleNames::contains).collect(Collectors.toList());
            if (!duplicateRuleNames.isEmpty()) {
                throw new DuplicateRuleNamesException(schemaName, duplicateRuleNames);
            }
        }
    }
    
    private void checkResources(final String schemaName, final CreateReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> notExistResources = getInvalidResources(schemaName, getResources(sqlStatement));
        if (!notExistResources.isEmpty()) {
            throw new ResourceNotExistedException(schemaName, notExistResources);
        }
    }
    
    private void checkLoadBalancers(final CreateReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> invalidLoadBalances = sqlStatement.getRules().stream().map(ReadwriteSplittingRuleSegment::getLoadBalancer).distinct()
                .filter(each -> !TypedSPIRegistry.findRegisteredService(ReplicaLoadBalanceAlgorithm.class, each, new Properties()).isPresent())
                .collect(Collectors.toList());
        if (!invalidLoadBalances.isEmpty()) {
            throw new InvalidLoadBalancersException(invalidLoadBalances);
        }
    }
    
    private Collection<String> getResources(final CreateReadwriteSplittingRuleStatement sqlStatement) {
        Collection<String> result = new LinkedHashSet<>();
        sqlStatement.getRules().stream().filter(each -> Strings.isNullOrEmpty(each.getAutoAwareResource())).forEach(each -> {
            result.add(each.getWriteDataSource());
            result.addAll(each.getReadDataSources());
        });
        return result;
    }
    
    private Collection<String> getRuleNames(final ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration) {
        return readwriteSplittingRuleConfiguration.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
    }
}
