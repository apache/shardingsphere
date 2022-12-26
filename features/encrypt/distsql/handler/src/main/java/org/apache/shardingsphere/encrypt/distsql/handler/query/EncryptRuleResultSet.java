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

import org.apache.shardingsphere.distsql.handler.resultset.DatabaseDistSQLResultSet;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.util.props.PropertiesConverter;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Result set for show encrypt rule.
 */
public final class EncryptRuleResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        rule.ifPresent(optional -> data = buildData((EncryptRuleConfiguration) optional.getConfiguration(), (ShowEncryptRulesStatement) sqlStatement).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final EncryptRuleConfiguration ruleConfig, final ShowEncryptRulesStatement sqlStatement) {
        return ruleConfig.getTables().stream().filter(each -> Objects.isNull(sqlStatement.getTableName()) || each.getName().equals(sqlStatement.getTableName()))
                .map(each -> buildColumnData(each, ruleConfig.getEncryptors(), ruleConfig.isQueryWithCipherColumn())).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<Collection<Object>> buildColumnData(final EncryptTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> algorithmMap,
                                                           final boolean queryWithCipherColumn) {
        Collection<Collection<Object>> result = new LinkedList<>();
        tableRuleConfig.getColumns().forEach(each -> {
            AlgorithmConfiguration encryptorAlgorithmConfig = algorithmMap.get(each.getEncryptorName());
            AlgorithmConfiguration assistedQueryEncryptorAlgorithmConfig = algorithmMap.get(each.getAssistedQueryEncryptorName());
            AlgorithmConfiguration likeQueryEncryptorAlgorithmConfig = algorithmMap.get(each.getLikeQueryEncryptorName());
            result.add(Arrays.asList(tableRuleConfig.getName(), each.getLogicColumn(),
                    each.getCipherColumn(),
                    nullToEmptyString(each.getPlainColumn()),
                    nullToEmptyString(each.getAssistedQueryColumn()),
                    nullToEmptyString(each.getLikeQueryColumn()),
                    encryptorAlgorithmConfig.getType(), PropertiesConverter.convert(encryptorAlgorithmConfig.getProps()),
                    Objects.isNull(assistedQueryEncryptorAlgorithmConfig) ? nullToEmptyString(null) : assistedQueryEncryptorAlgorithmConfig.getType(),
                    Objects.isNull(assistedQueryEncryptorAlgorithmConfig) ? nullToEmptyString(null) : PropertiesConverter.convert(assistedQueryEncryptorAlgorithmConfig.getProps()),
                    Objects.isNull(likeQueryEncryptorAlgorithmConfig) ? nullToEmptyString(null) : likeQueryEncryptorAlgorithmConfig.getType(),
                    Objects.isNull(likeQueryEncryptorAlgorithmConfig) ? nullToEmptyString(null) : PropertiesConverter.convert(likeQueryEncryptorAlgorithmConfig.getProps()),
                    isQueryWithCipherColumn(queryWithCipherColumn, tableRuleConfig, each).toString()));
        });
        return result;
    }
    
    private Object nullToEmptyString(final Object obj) {
        return null == obj ? "" : obj;
    }
    
    private Boolean isQueryWithCipherColumn(final boolean queryWithCipherColumn, final EncryptTableRuleConfiguration tableRuleConfig, final EncryptColumnRuleConfiguration columnRuleConfig) {
        if (Objects.nonNull(columnRuleConfig.getQueryWithCipherColumn())) {
            return columnRuleConfig.getQueryWithCipherColumn();
        }
        if (Objects.nonNull(tableRuleConfig.getQueryWithCipherColumn())) {
            return tableRuleConfig.getQueryWithCipherColumn();
        }
        return queryWithCipherColumn;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "logic_column", "cipher_column", "plain_column",
                "assisted_query_column", "like_query_column", "encryptor_type", "encryptor_props",
                "assisted_query_type", "assisted_query_props", "like_query_type", "like_query_props", "query_with_cipher_column");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        return data.next();
    }
    
    @Override
    public String getType() {
        return ShowEncryptRulesStatement.class.getName();
    }
}
