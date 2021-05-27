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
import com.google.common.collect.Maps;
import org.apache.shardingsphere.distsql.parser.statement.rql.show.ShowEncryptTableRuleStatement;
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Backend handler for show encrypt table rule.
 */
public final class EncryptTableRuleQueryBackendHandler extends SchemaRequiredBackendHandler<ShowEncryptTableRuleStatement> {

    private Iterator<EncryptColumnRuleConfiguration> data;

    private Map<String, ShardingSphereAlgorithmConfiguration> encryptors;

    public EncryptTableRuleQueryBackendHandler(final ShowEncryptTableRuleStatement sqlStatement, final BackendConnection backendConnection) {
        super(sqlStatement, backendConnection);
    }
    
    @Override
    protected ResponseHeader execute(final String schemaName, final ShowEncryptTableRuleStatement sqlStatement) {
        loadTableRuleConfiguration(schemaName, sqlStatement.getTableName());
        return new QueryResponseHeader(getQueryHeader(schemaName));
    }
    
    private void loadTableRuleConfiguration(final String schemaName, final String tableName) {
        Optional<EncryptRuleConfiguration> ruleConfig = ProxyContext.getInstance().getMetaData(schemaName).getRuleMetaData().getConfigurations()
                .stream().filter(each -> each instanceof EncryptRuleConfiguration).map(each -> (EncryptRuleConfiguration) each).findAny();
        Optional<EncryptTableRuleConfiguration> table = ruleConfig.map(optional -> optional.getTables()
                .stream().filter(each -> tableName.equalsIgnoreCase(each.getName())).findAny()).orElse(Optional.empty());
        data = table.map(optional -> optional.getColumns().stream().iterator()).orElse(Collections.emptyIterator());
        encryptors = ruleConfig.map(EncryptRuleConfiguration::getEncryptors).orElse(Maps.newHashMap());
    }

    private List<QueryHeader> getQueryHeader(final String schemaName) {
        List<QueryHeader> result = new LinkedList<>();
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
        EncryptColumnRuleConfiguration column = data.next();
        Properties encryptProps = encryptors.get(column.getEncryptorName()).getProps();
        return Arrays.asList(column.getLogicColumn(), column.getCipherColumn(), column.getPlainColumn(), encryptors.get(column.getEncryptorName()).getType(),
                Objects.nonNull(encryptProps) ? Joiner.on(",").join(encryptProps.entrySet().stream()
                        .map(each -> Joiner.on(":").join(each.getKey(), each.getValue())).collect(Collectors.toList())) : "");
    }
}
