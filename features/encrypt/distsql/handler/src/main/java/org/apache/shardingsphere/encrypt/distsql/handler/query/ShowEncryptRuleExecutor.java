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

import org.apache.shardingsphere.distsql.handler.query.RQLExecutor;
import org.apache.shardingsphere.encrypt.api.config.CompatibleEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnItemRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.props.PropertiesConverter;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Show encrypt rule executor.
 */
public final class ShowEncryptRuleExecutor implements RQLExecutor<ShowEncryptRulesStatement> {
    
    @Override
    public Collection<LocalDataQueryResultRow> getRows(final ShardingSphereDatabase database, final ShowEncryptRulesStatement sqlStatement) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        Collection<LocalDataQueryResultRow> result = new LinkedList<>();
        if (rule.isPresent()) {
            EncryptRuleConfiguration ruleConfig = rule.get().getConfiguration() instanceof CompatibleEncryptRuleConfiguration
                    ? ((CompatibleEncryptRuleConfiguration) rule.get().getConfiguration()).convertToEncryptRuleConfiguration()
                    : (EncryptRuleConfiguration) rule.get().getConfiguration();
            result = buildData(ruleConfig, sqlStatement);
        }
        return result;
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
            result.add(new LocalDataQueryResultRow(Arrays.asList(
                    tableRuleConfig.getName(),
                    each.getName(),
                    each.getCipher().getName(),
                    each.getAssistedQuery().map(EncryptColumnItemRuleConfiguration::getName).orElse(""),
                    each.getLikeQuery().map(EncryptColumnItemRuleConfiguration::getName).orElse(""),
                    encryptorAlgorithmConfig.getType(),
                    PropertiesConverter.convert(encryptorAlgorithmConfig.getProps()),
                    null == assistedQueryEncryptorAlgorithmConfig ? nullToEmptyString(null) : assistedQueryEncryptorAlgorithmConfig.getType(),
                    null == assistedQueryEncryptorAlgorithmConfig ? nullToEmptyString(null) : PropertiesConverter.convert(assistedQueryEncryptorAlgorithmConfig.getProps()),
                    null == likeQueryEncryptorAlgorithmConfig ? nullToEmptyString(null) : likeQueryEncryptorAlgorithmConfig.getType(),
                    null == likeQueryEncryptorAlgorithmConfig ? nullToEmptyString(null) : PropertiesConverter.convert(likeQueryEncryptorAlgorithmConfig.getProps()))));
        }
        return result;
    }
    
    private Object nullToEmptyString(final Object obj) {
        return null == obj ? "" : obj;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "logic_column", "cipher_column",
                "assisted_query_column", "like_query_column", "encryptor_type", "encryptor_props",
                "assisted_query_type", "assisted_query_props", "like_query_type", "like_query_props");
    }
    
    @Override
    public String getType() {
        return ShowEncryptRulesStatement.class.getName();
    }
}
