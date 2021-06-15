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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql.impl;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.Maps;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.impl.ShowEncryptRulesStatement;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptColumnRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.rule.EncryptTableRuleConfiguration;
import org.apache.shardingsphere.infra.config.algorithm.ShardingSphereAlgorithmConfiguration;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.SchemaRequiredBackendHandler;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Backend handler for show encrypt rules.
 */
public final class EncryptRulesQueryBackendHandler extends SchemaRequiredBackendHandler<ShowEncryptRulesStatement> {

    private Iterator<Entry<String, EncryptColumnRuleConfiguration>> data;

    private Map<String, ShardingSphereAlgorithmConfiguration> encryptors;

    public EncryptRulesQueryBackendHandler(final ShowEncryptRulesStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final ShowEncryptRulesStatement sqlStatement) {
        loadRuleConfiguration(schemaName, sqlStatement.getTableName());
        return new QueryResponseHeader(getQueryHeader(schemaName));
    }
    
    private void loadRuleConfiguration(final String schemaName, final String tableName) {
        Optional<EncryptRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof EncryptRuleConfiguration).map(each -> (EncryptRuleConfiguration) each).findAny();
        data = ruleConfig.map(optional -> getAllEncryptColumns(optional, tableName).entrySet().iterator()).orElse(Collections.emptyIterator());
        encryptors = ruleConfig.map(EncryptRuleConfiguration::getEncryptors).orElse(Maps.newHashMap());
    }

    private Map<String, EncryptColumnRuleConfiguration> getAllEncryptColumns(final EncryptRuleConfiguration encryptRuleConfiguration, final String tableName) {
        Map<String, EncryptColumnRuleConfiguration> result = new HashMap<>();
        if (Objects.nonNull(tableName)) {
            encryptRuleConfiguration.getTables().stream().filter(each -> tableName.equalsIgnoreCase(each.getName()))
                    .findAny().ifPresent(each -> result.putAll(buildEncryptColumnRuleConfigurationMap(each)));
        } else {
            encryptRuleConfiguration.getTables().forEach(each -> result.putAll(buildEncryptColumnRuleConfigurationMap(each)));
        }
        return result;
    }

    private Map<String, EncryptColumnRuleConfiguration> buildEncryptColumnRuleConfigurationMap(final EncryptTableRuleConfiguration encryptTableRuleConfiguration) {
        return encryptTableRuleConfiguration.getColumns().stream().collect(Collectors.toMap(each -> Joiner.on(".")
                .join(encryptTableRuleConfiguration.getName(), each.getLogicColumn()), each -> each));
    }
    
    private List<QueryHeader> getQueryHeader(final String schemaName) {
        List<QueryHeader> result = new LinkedList<>();
        result.add(new QueryHeader(schemaName, "", "table", "table", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "logicColumn", "logicColumn", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "cipherColumn", "cipherColumn", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "plainColumn", "plainColumn", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "encryptorType", "encryptorType", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        result.add(new QueryHeader(schemaName, "", "encryptorProps", "encryptorProps", Types.CHAR, "CHAR", 255, 0, false, false, false, false));
        return result;
    }
    
    @Override
    public boolean next() {
        return data.hasNext();
    }
    
    @Override
    public Collection<Object> getRowData() {
        Entry<String, EncryptColumnRuleConfiguration> entry = data.next();
        Properties encryptProps = Objects.nonNull(encryptors.get(entry.getValue().getEncryptorName()))
                ? encryptors.get(entry.getValue().getEncryptorName()).getProps() : null;
        return Arrays.asList(Splitter.on(".").splitToList(entry.getKey()).get(0), entry.getValue().getLogicColumn(),
                entry.getValue().getCipherColumn(), entry.getValue().getPlainColumn(), encryptors.get(entry.getValue().getEncryptorName()).getType(),
                Objects.nonNull(encryptProps) ? Joiner.on(",").join(encryptProps.entrySet().stream()
                        .map(each -> Joiner.on("=").join(each.getKey(), each.getValue())).collect(Collectors.toList())) : "");
    }
}
