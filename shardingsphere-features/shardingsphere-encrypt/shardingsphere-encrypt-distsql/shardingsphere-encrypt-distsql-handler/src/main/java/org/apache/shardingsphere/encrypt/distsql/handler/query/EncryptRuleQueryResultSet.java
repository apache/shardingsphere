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

import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.config.algorithm.AlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DatabaseDistSQLResultSet;
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
 * Query result set for show encrypt rule.
 */
public final class EncryptRuleQueryResultSet implements DatabaseDistSQLResultSet {
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereDatabase database, final SQLStatement sqlStatement) {
        Optional<EncryptRule> rule = database.getRuleMetaData().findSingleRule(EncryptRule.class);
        rule.ifPresent(optional -> data = buildData((EncryptRuleConfiguration) optional.getConfiguration(), (ShowEncryptRulesStatement) sqlStatement).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final EncryptRuleConfiguration ruleConfig, final ShowEncryptRulesStatement sqlStatement) {
        return ruleConfig.getTables().stream().filter(each -> Objects.isNull(sqlStatement.getTableName()) || each.getName().equals(sqlStatement.getTableName()))
                .map(each -> buildColumnData(each, ruleConfig.getEncryptors())).flatMap(Collection::stream).collect(Collectors.toList());
    }
    
    private Collection<Collection<Object>> buildColumnData(final EncryptTableRuleConfiguration tableRuleConfig, final Map<String, AlgorithmConfiguration> algorithmMap) {
        Collection<Collection<Object>> result = new LinkedList<>();
        tableRuleConfig.getColumns().forEach(each -> {
            AlgorithmConfiguration algorithmConfig = algorithmMap.get(each.getEncryptorName());
            result.add(Arrays.asList(tableRuleConfig.getName(), each.getLogicColumn(), nullToEmptyString(null),
                    each.getCipherColumn(), nullToEmptyString(null),
                    nullToEmptyString(each.getPlainColumn()), nullToEmptyString(null),
                    nullToEmptyString(each.getAssistedQueryColumn()), nullToEmptyString(null),
                    algorithmConfig.getType(), PropertiesConverter.convert(algorithmConfig.getProps()),
                    Objects.isNull(tableRuleConfig.getQueryWithCipherColumn()) ? Boolean.TRUE.toString() : tableRuleConfig.getQueryWithCipherColumn().toString()));
        });
        return result;
    }
    
    private Object nullToEmptyString(final Object obj) {
        return null == obj ? "" : obj;
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "logic_column", "logic_data_type", "cipher_column", "cipher_data_type", "plain_column", "plain_data_type",
                "assisted_query_column", "assisted_query_data_type", "encryptor_type", "encryptor_props", "query_with_cipher_column");
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
