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

package org.apache.shardingsphere.encrypt.distsql.handler.update;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.engine.update.rdl.rule.spi.database.type.DatabaseRuleDropExecutor;
import org.apache.shardingsphere.distsql.handler.required.DistSQLExecutorCurrentRuleRequired;
import org.apache.shardingsphere.encrypt.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.statement.DropEncryptRuleStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.algorithm.core.config.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.kernel.metadata.rule.MissingRequiredRuleException;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Drop encrypt rule executor.
 */
@DistSQLExecutorCurrentRuleRequired(EncryptRule.class)
@Setter
public final class DropEncryptRuleExecutor implements DatabaseRuleDropExecutor<DropEncryptRuleStatement, EncryptRule, EncryptRuleConfiguration> {
    
    private ShardingSphereDatabase database;
    
    private EncryptRule rule;
    
    @Override
    public void checkBeforeUpdate(final DropEncryptRuleStatement sqlStatement) {
        if (!sqlStatement.isIfExists()) {
            checkToBeDroppedEncryptTableNames(sqlStatement);
        }
    }
    
    private void checkToBeDroppedEncryptTableNames(final DropEncryptRuleStatement sqlStatement) {
        Collection<String> notExistedTableNames = sqlStatement.getTables().stream().filter(each -> !rule.getAllTableNames().contains(each)).collect(Collectors.toList());
        ShardingSpherePreconditions.checkMustEmpty(notExistedTableNames, () -> new MissingRequiredRuleException("Encrypt", database.getName(), notExistedTableNames));
    }
    
    @Override
    public boolean hasAnyOneToBeDropped(final DropEncryptRuleStatement sqlStatement) {
        return !Collections.disjoint(rule.getAllTableNames(), sqlStatement.getTables());
    }
    
    @Override
    public EncryptRuleConfiguration buildToBeDroppedRuleConfiguration(final DropEncryptRuleStatement sqlStatement) {
        Collection<EncryptTableRuleConfiguration> toBeDroppedTables = new LinkedList<>();
        Map<String, AlgorithmConfiguration> toBeDroppedEncryptors = new HashMap<>(sqlStatement.getTables().size(), 1F);
        for (String each : sqlStatement.getTables()) {
            toBeDroppedTables.add(new EncryptTableRuleConfiguration(each, Collections.emptyList()));
            dropRule(each);
        }
        UnusedAlgorithmFinder.findUnusedEncryptor(rule.getConfiguration()).forEach(each -> toBeDroppedEncryptors.put(each, rule.getConfiguration().getEncryptors().get(each)));
        return new EncryptRuleConfiguration(toBeDroppedTables, toBeDroppedEncryptors);
    }
    
    private void dropRule(final String ruleName) {
        Optional<EncryptTableRuleConfiguration> encryptTableRuleConfig = rule.getConfiguration().getTables().stream().filter(each -> each.getName().equalsIgnoreCase(ruleName)).findAny();
        encryptTableRuleConfig.ifPresent(optional -> rule.getConfiguration().getTables().remove(encryptTableRuleConfig.get()));
    }
    
    @Override
    public Class<EncryptRule> getRuleClass() {
        return EncryptRule.class;
    }
    
    @Override
    public Class<DropEncryptRuleStatement> getType() {
        return DropEncryptRuleStatement.class;
    }
}
