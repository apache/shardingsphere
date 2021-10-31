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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.show.executor;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * Show all variables executor.
 */
@RequiredArgsConstructor
public final class ShowAllVariablesExecutor extends AbstractShowExecutor {
    
    private final BackendConnection backendConnection;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        List<QueryHeader> result = new LinkedList<>();
        result.add(new QueryHeader("", "", "variable_name", "variable_name", Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false));
        result.add(new QueryHeader("", "", "variable_value", "variable_value", Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false));
        return result;
    }
    
    @Override
    protected MergedResult createMergedResult() {
        List<List<Object>> rows = new LinkedList<>();
        ConfigurationProperties configurationProperties = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps();
        ConfigurationPropertyKey.getKeyNames().stream().forEach(each -> {
            String propertyValue = configurationProperties.getValue(ConfigurationPropertyKey.valueOf(each)).toString();
            rows.add(Arrays.asList(each.toLowerCase(), propertyValue));
        });
        rows.add(Arrays.asList(VariableEnum.AGENT_PLUGINS_ENABLED.name().toLowerCase(), SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.FALSE.toString())));
        rows.add(Arrays.asList(VariableEnum.CACHED_CONNECTIONS.name().toLowerCase(), backendConnection.getConnectionSize()));
        rows.add(Arrays.asList(VariableEnum.TRANSACTION_TYPE.name().toLowerCase(), backendConnection.getTransactionStatus().getTransactionType().name()));
        return new MultipleLocalDataMergedResult(rows);
    }
}
