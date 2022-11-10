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
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionAlterUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.readwritesplitting.api.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.api.rule.ReadwriteSplittingDataSourceRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.checker.ReadwriteSplittingRuleStatementChecker;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.distsql.parser.statement.AlterReadwriteSplittingRuleStatement;

import java.util.Optional;

/**
 * Alter readwrite-splitting rule statement updater.
 */
public final class AlterReadwriteSplittingRuleStatementUpdater implements RuleDefinitionAlterUpdater<AlterReadwriteSplittingRuleStatement, ReadwriteSplittingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database, final AlterReadwriteSplittingRuleStatement sqlStatement, final ReadwriteSplittingRuleConfiguration currentRuleConfig) {
        ReadwriteSplittingRuleStatementChecker.checkAlteration(database, sqlStatement.getRules(), currentRuleConfig);
    }
    
    @Override
    public RuleConfiguration buildToBeAlteredRuleConfiguration(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return ReadwriteSplittingRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public void updateCurrentRuleConfiguration(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ReadwriteSplittingRuleConfiguration toBeAlteredRuleConfig) {
        dropRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
        addRuleConfiguration(currentRuleConfig, toBeAlteredRuleConfig);
    }
    
    private void dropRuleConfiguration(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ReadwriteSplittingRuleConfiguration toBeAlteredRuleConfig) {
        for (ReadwriteSplittingDataSourceRuleConfiguration each : toBeAlteredRuleConfig.getDataSources()) {
            Optional<ReadwriteSplittingDataSourceRuleConfiguration> toBeRemovedDataSourceRuleConfig =
                    currentRuleConfig.getDataSources().stream().filter(dataSource -> each.getName().equals(dataSource.getName())).findAny();
            Preconditions.checkState(toBeRemovedDataSourceRuleConfig.isPresent());
            currentRuleConfig.getDataSources().remove(toBeRemovedDataSourceRuleConfig.get());
            currentRuleConfig.getLoadBalancers().remove(toBeRemovedDataSourceRuleConfig.get().getLoadBalancerName());
        }
    }
    
    private void addRuleConfiguration(final ReadwriteSplittingRuleConfiguration currentRuleConfig, final ReadwriteSplittingRuleConfiguration toBeAlteredRuleConfig) {
        currentRuleConfig.getDataSources().addAll(toBeAlteredRuleConfig.getDataSources());
        currentRuleConfig.getLoadBalancers().putAll(toBeAlteredRuleConfig.getLoadBalancers());
    }
    
    @Override
    public Class<ReadwriteSplittingRuleConfiguration> getRuleConfigurationClass() {
        return ReadwriteSplittingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return AlterReadwriteSplittingRuleStatement.class.getName();
    }
}
