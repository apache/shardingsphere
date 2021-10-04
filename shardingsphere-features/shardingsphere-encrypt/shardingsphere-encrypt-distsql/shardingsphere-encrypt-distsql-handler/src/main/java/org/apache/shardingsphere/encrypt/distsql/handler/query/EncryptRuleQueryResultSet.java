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
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.encrypt.distsql.parser.statement.ShowEncryptRulesStatement;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.properties.PropertiesConverter;
import org.apache.shardingsphere.infra.distsql.query.DistSQLResultSet;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Result set for show encrypt rule.
 */
public final class EncryptRuleQueryResultSet implements DistSQLResultSet {
    
    private Iterator<Entry<String, EncryptColumnRuleConfiguration>> data;
    
    private Map<String, ShardingSphereAlgorithmConfiguration> encryptors;
    
    @Override
    public void init(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement) {
        Optional<EncryptRuleConfiguration> ruleConfig = metaData.getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof EncryptRuleConfiguration).map(each -> (EncryptRuleConfiguration) each).findAny();
        data = ruleConfig.map(optional -> getAllEncryptColumns(optional, ((ShowEncryptRulesStatement) sqlStatement).getTableName()).entrySet().iterator()).orElse(Collections.emptyIterator());
        encryptors = ruleConfig.map(EncryptRuleConfiguration::getEncryptors).orElse(Collections.emptyMap());
    }
    
    private Map<String, EncryptColumnRuleConfiguration> getAllEncryptColumns(final EncryptRuleConfiguration encryptRuleConfig, final String tableName) {
        Map<String, EncryptColumnRuleConfiguration> result = new HashMap<>();
        if (Objects.nonNull(tableName)) {
            encryptRuleConfig.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getName()))
                    .findAny().ifPresent(each -> result.putAll(buildEncryptColumnRuleConfigurationMap(each)));
        } else {
            encryptRuleConfig.getTables().forEach(each -> result.putAll(buildEncryptColumnRuleConfigurationMap(each)));
        }
        return result;
    }
    
    private Map<String, EncryptColumnRuleConfiguration> buildEncryptColumnRuleConfigurationMap(final EncryptTableRuleConfiguration encryptTableRuleConfig) {
        return encryptTableRuleConfig.getColumns().stream().collect(Collectors.toMap(each -> String.join(".", encryptTableRuleConfig.getName(), each.getLogicColumn()), each -> each));
    }
    
    @Override
    public Collection<String> getColumnNames() {
        return Arrays.asList("table", "logic_column", "cipher_column", "plain_column", "encryptor_type", "encryptor_props");
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Entry<String, EncryptColumnRuleConfiguration> entry = data.next();
        return Arrays.asList(entry.getKey().split("\\.")[0], entry.getValue().getLogicColumn(), entry.getValue().getCipherColumn(), entry.getValue().getPlainColumn(), 
                encryptors.get(entry.getValue().getEncryptorName()).getType(), PropertiesConverter.convert(encryptors.get(entry.getValue().getEncryptorName()).getProps()));
    }
    
    @Override
    public String getType() {
        return ShowEncryptRulesStatement.class.getCanonicalName();
    }
}
