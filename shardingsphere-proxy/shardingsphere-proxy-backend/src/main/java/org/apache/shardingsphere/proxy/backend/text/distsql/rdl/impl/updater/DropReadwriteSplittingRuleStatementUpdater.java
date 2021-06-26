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

import org.apache.shardingsphere.infra.distsql.RDLUpdater;
import org.apache.shardingsphere.infra.metadata.resource.ShardingSphereResource;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.ReadwriteSplittingRuleNotExistedException;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.DropReadwriteSplittingRuleStatement;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Drop readwrite-splitting rule statement updater.
 */
public final class DropReadwriteSplittingRuleStatementUpdater implements RDLUpdater<DropReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement, 
                                  final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ShardingSphereResource resource) {
        if (null == currentRuleConfig) {
            throw new ReadwriteSplittingRuleNotExistedException(schemaName, sqlStatement.getRuleNames());
        }
        Collection<String> existRuleNames = currentRuleConfig.getDataSources().stream().map(ReadwriteSplittingDataSourceRuleConfiguration::getName).collect(Collectors.toList());
        Collection<String> notExistedRuleNames = sqlStatement.getRuleNames().stream().filter(each -> !existRuleNames.contains(each)).collect(Collectors.toList());
        if (!notExistedRuleNames.isEmpty()) {
            throw new ReadwriteSplittingRuleNotExistedException(schemaName, sqlStatement.getRuleNames());
        }
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final String schemaName, final DropReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        sqlStatement.getRuleNames().forEach(each -> {
            ReadwriteSplittingDataSourceRuleConfiguration dataSourceRuleConfig = currentRuleConfig.getDataSources().stream().filter(dataSource -> each.equals(dataSource.getName())).findAny().get();
            currentRuleConfig.getDataSources().remove(dataSourceRuleConfig);
            currentRuleConfig.getLoadBalancers().remove(dataSourceRuleConfig.getLoadBalancerName());
        });
        if (currentRuleConfig.getDataSources().isEmpty()) {
            ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations().remove(currentRuleConfig);
        }
    }
    
    @Override
    public String getType() {
        return DropReadwriteSplittingRuleStatement.class.getCanonicalName();
    }
}
