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

import org.apache.shardingsphere.infra.distsql.exception.DistSQLException;
import org.apache.shardingsphere.infra.distsql.exception.rule.DuplicateKeyGeneratorException;
import org.apache.shardingsphere.infra.distsql.exception.rule.RequiredKeyGeneratorMissedException;
import org.apache.shardingsphere.infra.distsql.update.RuleDefinitionDropUpdater;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.distsql.parser.statement.DropShardingKeyGeneratorStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Drop sharding key generator statement updater.
 */
public final class DropShardingKeyGeneratorStatementUpdater implements RuleDefinitionDropUpdater<DropShardingKeyGeneratorStatement, ShardingRuleConfiguration> {
    
    @Override
    public void checkSQLStatement(final ShardingSphereMetaData shardingSphereMetaData, final DropShardingKeyGeneratorStatement sqlStatement,
                                  final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        String schemaName = shardingSphereMetaData.getName();
        LinkedList<String> keyGeneratorNames = sqlStatement.getKeyGeneratorNames().stream().collect(Collectors.toCollection(LinkedList::new));
        checkDuplicate(schemaName, keyGeneratorNames);
        checkExist(schemaName, keyGeneratorNames, currentRuleConfig);
    }

    private void checkDuplicate(final String schemaName, final Collection<String> keyGeneratorNames) throws DistSQLException {
        Set<String> duplicateNames = keyGeneratorNames.stream().collect(Collectors.groupingBy(each -> each, Collectors.counting())).entrySet().stream()
                .filter(each -> each.getValue() > 1).map(Map.Entry::getKey).collect(Collectors.toSet());
        DistSQLException.predictionThrow(duplicateNames.isEmpty(), new DuplicateKeyGeneratorException("sharding", schemaName, duplicateNames));
    }
    
    private void checkExist(final String schemaName, final Collection<String> keyGeneratorNames, final ShardingRuleConfiguration currentRuleConfig) throws DistSQLException {
        LinkedList<String> notExistKeyGenerators = keyGeneratorNames.stream().filter(each -> !currentRuleConfig.getKeyGenerators().containsKey(each)).collect(Collectors.toCollection(LinkedList::new));
        DistSQLException.predictionThrow(notExistKeyGenerators.isEmpty(), new RequiredKeyGeneratorMissedException("sharding", schemaName, notExistKeyGenerators));
    }

    @Override
    public boolean updateCurrentRuleConfiguration(final DropShardingKeyGeneratorStatement sqlStatement, final ShardingRuleConfiguration currentRuleConfig) {
        currentRuleConfig.getKeyGenerators().keySet().removeIf(sqlStatement.getKeyGeneratorNames()::contains);
        return false;
    }
    
    @Override
    public Class<ShardingRuleConfiguration> getRuleConfigurationClass() {
        return ShardingRuleConfiguration.class;
    }
    
    @Override
    public String getType() {
        return DropShardingKeyGeneratorStatement.class.getCanonicalName();
    }
}
