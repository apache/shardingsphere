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

import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRulesNotExistedException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop readwrite splitting rule backend handler.
 */
public final class DropReadwriteSplittingRuleBackendHandler extends RDLBackendHandler<DropReadwriteSplittingRuleStatement> {
    
    public DropReadwriteSplittingRuleBackendHandler(final DropReadwriteSplittingRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    public void before(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement) {
        Optional<ReadwriteSplittingRuleConfiguration> optional = getReadwriteSplittingRuleConfiguration(schemaName);
        if (!optional.isPresent()) {
            throw new ReadwriteSplittingRulesNotExistedException(schemaName, sqlStatement.getRuleNames());
        }
        Collection<String> existRuleNames = optional.get().getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !existRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new ReadwriteSplittingRulesNotExistedException(schemaName, sqlStatement.getRuleNames());
        }
    }
    
    @Override
    public void doExecute(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement) {
        ReadwriteSplittingRuleConfiguration readwriteSplittingRuleConfiguration = getReadwriteSplittingRuleConfiguration(schemaName).get();
        sqlStatement.getRuleNames().forEach(each -> {
            ReadwriteSplittingDataSourceRuleConfiguration readwriteSplittingDataSourceRuleConfiguration = readwriteSplittingRuleConfiguration
                    .getDataSources().stream().filter(dataSource -> each.equals(dataSource.getName())).findAny().get();
            readwriteSplittingRuleConfiguration.getDataSources().remove(readwriteSplittingDataSourceRuleConfiguration);
            readwriteSplittingRuleConfiguration.getLoadBalancers().remove(readwriteSplittingDataSourceRuleConfiguration.getLoadBalancerName());
        });
        if (readwriteSplittingRuleConfiguration.getDataSources().isEmpty()) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().remove(readwriteSplittingRuleConfiguration);
        }
    }
}
