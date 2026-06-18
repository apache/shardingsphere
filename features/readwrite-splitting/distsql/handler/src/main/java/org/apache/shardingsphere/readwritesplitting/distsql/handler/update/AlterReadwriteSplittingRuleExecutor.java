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

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleAlterExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.readwritesplitting.config.ReadwriteSplittingRuleConfiguration;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.checker.ReadwriteSplittingRuleStatementChecker;
import org.apache.shardingsphere.readwritesplitting.distsql.handler.converter.ReadwriteSplittingRuleStatementConverter;
import org.apache.shardingsphere.readwritesplitting.distsql.statement.AlterReadwriteSplittingRuleStatement;
import org.apache.shardingsphere.readwritesplitting.rule.ReadwriteSplittingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Alter readwrite-splitting rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(ReadwriteSplittingRule.class)
@Setter
public final class AlterReadwriteSplittingRuleExecutor implements DatabaseRuleAlterExecutor<AlterReadwriteSplittingRuleStatement, ReadwriteSplittingRule, ReadwriteSplittingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ReadwriteSplittingRule rule;
    
    @Override
    public void checkBeforeUpdate(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        ReadwriteSplittingRuleStatementChecker.checkAlteration(database, sqlStatement.getRules(), rule.getConfiguration());
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration buildToBeAlteredRuleConfiguration(final AlterReadwriteSplittingRuleStatement sqlStatement) {
        return ReadwriteSplittingRuleStatementConverter.convert(sqlStatement.getRules());
    }
    
    @Override
    public ReadwriteSplittingRuleConfiguration buildToBeDroppedRuleConfiguration(final ReadwriteSplittingRuleConfiguration toBeAlteredRuleConfig) {
        Collection<String> unusedLoadBalancers = UnusedAlgorithmFinder.findUnusedLoadBalancers(rule.getConfiguration());
        Map<String, AlgorithmConfiguration> toBeDroppedLoadBalancers = new HashMap<>(unusedLoadBalancers.size(), 1F);
        unusedLoadBalancers.forEach(each -> toBeDroppedLoadBalancers.put(each, rule.getConfiguration().getLoadBalancers().get(each)));
        return new ReadwriteSplittingRuleConfiguration(Collections.emptyList(), toBeDroppedLoadBalancers);
    }
    
    @Override
    public Class<ReadwriteSplittingRule> getRuleClass() {
        return ReadwriteSplittingRule.class;
    }
    
    @Override
    public Class<AlterReadwriteSplittingRuleStatement> getType() {
        return AlterReadwriteSplittingRuleStatement.class;
    }
}
