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
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
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
public final class EncryptRuleQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Collection<Object>> data = Collections.emptyIterator();
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<EncryptRuleConfiguration> ruleConfiguration = metaData.getRuleMetaData().findRuleConfiguration(EncryptRuleConfiguration.class).stream().findAny();
        ruleConfiguration.ifPresent(op -> data = buildData(op, (ShowEncryptRulesStatement) sqlStatement).iterator());
    }
    
    private Collection<Collection<Object>> buildData(final EncryptRuleConfiguration configuration, final ShowEncryptRulesStatement sqlStatement) {
        return configuration.getTables().stream().filter(each -> Objects.isNull(sqlStatement.getTableName()) || each.getName().equals(sqlStatement.getTableName()))
                .map(each -> buildColumnData(each, configuration.getEncryptors())).flatMap(Collection::stream).collect(Collectors.toCollection(LinkedList::new));
    }
    
    private Collection<Collection<Object>> buildColumnData(final EncryptTableRuleConfiguration tableRuleConfiguration, final Map<String, ShardingSphereAlgorithmConfiguration> algorithmMap) {
        Collection<Collection<Object>> result = new LinkedList<>();
        tableRuleConfiguration.getColumns().forEach(each -> {
            ShardingSphereAlgorithmConfiguration algorithmConfiguration = algorithmMap.get(each.getEncryptorName());
            result.add(Arrays.asList(tableRuleConfiguration.getName(), each.getLogicColumn(), nullToEmptyString(each.getLogicDataType()),
                    each.getCipherColumn(), nullToEmptyString(each.getCipherDataType()),
                    nullToEmptyString(each.getPlainColumn()), nullToEmptyString(each.getPlainDataType()),
                    nullToEmptyString(each.getAssistedQueryColumn()), nullToEmptyString(each.getAssistedQueryDataType()),
                    algorithmConfiguration.getType(), PropertiesConverter.convert(algorithmConfiguration.getProps()),
                    Objects.isNull(tableRuleConfiguration.getQueryWithCipherColumn()) ? Boolean.TRUE.toString() : tableRuleConfiguration.getQueryWithCipherColumn().toString()));
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
