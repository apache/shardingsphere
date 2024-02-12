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

package org.apache.shardingsphere.encrypt.distsql.handler.query;

import lombok.Setter;
import org.apache.shardingsphere.distsql.handler.aware.DistSQLExecutorRuleAware;
import org.apache.shardingsphere.distsql.handler.engine.query.DistSQLQueryExecutor;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Show encrypt rule executor.
 */
@Setter
public final class ShowEncryptRuleExecutor implements DistSQLQueryExecutor<ShowEncryptRulesStatement>, DistSQLExecutorRuleAware<EncryptRule> {
    
    private EncryptRule rule;
    
    @Override
    public Collection<String> getColumnNames(final ShowEncryptRulesStatement sqlStatement) {
        return Arrays.asList("table", "logic_column", "cipher_column",
                "assisted_query_column", "like_query_column", "encryptor_type", "encryptor_props", "assisted_query_type", "assisted_query_props", "like_query_type", "like_query_props");
    }
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShowEncryptRulesStatement sqlStatement, final ContextManager contextManager) {
        return buildData(rule.getConfiguration(), sqlStatement);
    }
    
    private Collection<LocalDataQueryResultRow> buildData(final EncryptRuleConfiguration ruleConfig, final ShowEncryptRulesStatement sqlStatement) {
        return ruleConfig.getTables().stream().filter(each -> null == sqlStatement.getTableName() || each.getName().equals(sqlStatement.getTableName()))
                .map(each -> buildColumnData(each, ruleConfig.getEncryptors()))
                .flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<LocalDataQueryResultRow> buildColumnData(final EncryptTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> encryptors) {
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        for (EncryptColumnRuleConfiguration each : tableRuleConfig.getColumns()) {
            AlgorithmConfiguration encryptorAlgorithmConfig = encryptors.get(each.getCipher().getEncryptorName());
            AlgorithmConfiguration assistedQueryEncryptorAlgorithmConfig = each.getAssistedQuery().isPresent() ? encryptors.get(each.getAssistedQuery().get().getEncryptorName()) : null;
            AlgorithmConfiguration likeQueryEncryptorAlgorithmConfig = each.getLikeQuery().isPresent() ? encryptors.get(each.getLikeQuery().get().getEncryptorName()) : null;
            result.add(new LocalDataQueryResultRow(
                    tableRuleConfig.getName(),
                    each.getName(),
                    each.getCipher().getName(),
                    each.getAssistedQuery().map(EncryptColumnItemRuleConfiguration::getName).orElse(""),
                    each.getLikeQuery().map(EncryptColumnItemRuleConfiguration::getName).orElse(""),
                    encryptorAlgorithmConfig.getType(),
                    encryptorAlgorithmConfig.getProps(),
                    null == assistedQueryEncryptorAlgorithmConfig ? "" : assistedQueryEncryptorAlgorithmConfig.getType(),
                    null == assistedQueryEncryptorAlgorithmConfig ? "" : assistedQueryEncryptorAlgorithmConfig.getProps(),
                    null == likeQueryEncryptorAlgorithmConfig ? "" : likeQueryEncryptorAlgorithmConfig.getType(),
                    null == likeQueryEncryptorAlgorithmConfig ? "" : likeQueryEncryptorAlgorithmConfig.getProps()));
        }
        return result;
    }
    
    @Override
    public Class<EncryptRule> getRuleClass() {
        return EncryptRule.class;
    }
    
    @Override
    public Class<ShowEncryptRulesStatement> getType() {
        return ShowEncryptRulesStatement.class;
    }
}
