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

package org.apache.shardingsphere.sharding.distsql.handler.update;

import com.google.common.base.Strings;
import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.infra.algorithm.core.exception.InUsedAlgorithmException;
import org.apache.shardingsphere.infra.algorithm.core.exception.UnregisteredAlgorithmException;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.external.sql.identifier.SQLExceptionIdentifier;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.statement.DropShardingKeyGeneratorStatement;
import org.apache.shardingsphere.sharding.rule.ShardingRule;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.stream.Collectors;

/**
 * Drop sharding key generator executor.
 */
@DistSQLExecutorCurrentRuleRequired(ShardingRule.class)
@Setter
public final class DropShardingKeyGeneratorExecutor implements DatabaseRuleDropExecutor<DropShardingKeyGeneratorStatement, ShardingRule, ShardingRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private ShardingRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropShardingKeyGeneratorStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkExist(sqlStatement);
        }
        if (null != rule) {
            checkInUsed(sqlStatement);
        }
    }
    
    private void checkExist(final DropShardingKeyGeneratorStatement sqlStatement) {
        Collection<String> notExistKeyGenerators = sqlStatement.getNames().stream().filter(each -> !rule.getConfiguration().getKeyGenerators().containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistKeyGenerators,
                () -> new UnregisteredAlgorithmException("Key generator", notExistKeyGenerators, new SQLExceptionIdentifier(database.getName())));
    }
    
    private void checkInUsed(final DropShardingKeyGeneratorStatement sqlStatement) {
        Collection<String> usedKeyGenerators = getUsedKeyGenerators();
        Collection<String> inUsedNames = sqlStatement.getNames().stream().filter(usedKeyGenerators::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(inUsedNames, () -> new InUsedAlgorithmException("Key generator", database.getName(), inUsedNames));
    }
    
    private Collection<String> getUsedKeyGenerators() {
        Collection<String> result = new LinkedHashSet<>();
        rule.getConfiguration().getTables().stream().filter(each -> null != each.getKeyGenerateStrategy()).forEach(each -> result.add(each.getKeyGenerateStrategy().getKeyGeneratorName()));
        rule.getConfiguration().getAutoTables().stream().filter(each -> null != each.getKeyGenerateStrategy()).forEach(each -> result.add(each.getKeyGenerateStrategy().getKeyGeneratorName()));
        KeyGenerateStrategyConfiguration keyGenerateStrategy = rule.getConfiguration().getDefaultKeyGenerateStrategy();
        if (null != keyGenerateStrategy && !Strings.isNullOrEmpty(keyGenerateStrategy.getKeyGeneratorName())) {
            result.add(keyGenerateStrategy.getKeyGeneratorName());
        }
        return result;
    }
    
    @Override
    public ShardingRuleConfiguration buildToBeDroppedRuleConfiguration(final DropShardingKeyGeneratorStatement sqlStatement) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        for (String each : sqlStatement.getNames()) {
            result.getKeyGenerators().put(each, rule.getConfiguration().getKeyGenerators().get(each));
        }
        return result;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingKeyGeneratorStatement sqlStatement) {
        return !Collections.disjoint(rule.getConfiguration().getKeyGenerators().keySet(), sqlStatement.getNames());
    }
    
    @Override
    public Class<ShardingRule> getRuleClass() {
        return ShardingRule.class;
    }
    
    @Override
    public Class<DropShardingKeyGeneratorStatement> getType() {
        return DropShardingKeyGeneratorStatement.class;
    }
}
