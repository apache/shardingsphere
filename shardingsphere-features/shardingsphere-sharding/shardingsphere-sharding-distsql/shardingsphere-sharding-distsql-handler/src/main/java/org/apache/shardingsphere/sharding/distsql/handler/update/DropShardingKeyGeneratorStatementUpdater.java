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
import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.AlgorithmInUsedException;
import org.apache.shardingsphere.infra.distsql.exception.rule.MissingRequiredAlgorithmException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.keygen.KeyGenerateStrategyConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingKeyGeneratorStatement;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Drop sharding key generator statement updater.
 */
public final class DropShardingKeyGeneratorStatementUpdater implements RuleDefinitionDropUpdater<DropShardingKeyGeneratorStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereDatabase database,
                                  final DropShardingKeyGeneratorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        if (null == currentRuleConfig && sqlStatement.isIfExists()) {
            return;
        }
        String databaseName = database.getName();
        Collection<String> keyGeneratorNames = new LinkedList<>(sqlStatement.getKeyGeneratorNames());
        checkExist(databaseName, keyGeneratorNames, currentRuleConfig, sqlStatement);
        checkInUsed(databaseName, keyGeneratorNames, currentRuleConfig);
    }
    
    private void checkExist(final String databaseName, final Collection<String> keyGeneratorNames, final ShardingRuleConfiguration currentRuleConfig,
                            final DropShardingKeyGeneratorStatement sqlStatement) throws DistSQLException {
        if (sqlStatement.isIfExists()) {
            return;
        }
        Collection<String> notExistKeyGenerators = keyGeneratorNames.stream().filter(each -> !currentRuleConfig.getKeyGenerators().containsKey(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(notExistKeyGenerators.isEmpty(), () -> new MissingRequiredAlgorithmException("Key generator", databaseName, notExistKeyGenerators));
    }
    
    private void checkInUsed(final String databaseName, final Collection<String> keyGeneratorNames, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        Collection<String> usedKeyGenerators = getUsedKeyGenerators(currentRuleConfig);
        Collection<String> inUsedNames = keyGeneratorNames.stream().filter(usedKeyGenerators::contains).collect(Collectors.toList());
        ShardingSpherePreconditions.checkState(inUsedNames.isEmpty(), () -> new AlgorithmInUsedException("Key generator", databaseName, inUsedNames));
    }
    
    private Collection<String> getUsedKeyGenerators(final ShardingRuleConfiguration shardingRuleConfig) {
        Collection<String> result = new LinkedHashSet<>();
        shardingRuleConfig.getTables().stream().filter(each -> Objects.nonNull(each.getKeyGenerateStrategy())).forEach(each -> result.add(each.getKeyGenerateStrategy().getKeyGeneratorName()));
        shardingRuleConfig.getAutoTables().stream().filter(each -> Objects.nonNull(each.getKeyGenerateStrategy())).forEach(each -> result.add(each.getKeyGenerateStrategy().getKeyGeneratorName()));
        KeyGenerateStrategyConfiguration keyGenerateStrategy = shardingRuleConfig.getDefaultKeyGenerateStrategy();
        if (Objects.nonNull(keyGenerateStrategy) && !Strings.isNullOrEmpty(keyGenerateStrategy.getKeyGeneratorName())) {
            result.add(keyGenerateStrategy.getKeyGeneratorName());
        }
        return result;
    }
    
    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingKeyGeneratorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getKeyGenerators().keySet().removeIf(sqlStatement.getKeyGeneratorNames()::contains);
        return false;
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropShardingKeyGeneratorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        return null != currentRuleConfig && !getIdenticalData(currentRuleConfig.getKeyGenerators().keySet(), sqlStatement.getKeyGeneratorNames()).isEmpty();
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingKeyGeneratorStatement.class.getName();
    }
}
