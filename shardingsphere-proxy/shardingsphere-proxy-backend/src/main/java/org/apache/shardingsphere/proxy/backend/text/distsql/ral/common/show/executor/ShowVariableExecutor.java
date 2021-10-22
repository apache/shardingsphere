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

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowVariableStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;

import java.sql.Types;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Show variable executor.
 */
@RequiredArgsConstructor
@Getter
public final class ShowVariableExecutor extends AbstractShowExecutor {
    
    private final ShowVariableStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    private final Collection<VariableEnum> propsVariables = Arrays.asList(
            VariableEnum.MAX_CONNECTIONS_SIZE_PER_QUERY,
            VariableEnum.KERNEL_EXECUTOR_SIZE,
            VariableEnum.PROXY_FRONTEND_FLUSH_THRESHOLD,
            VariableEnum.PROXY_OPENTRACING_ENABLED,
            VariableEnum.PROXY_HINT_ENABLED,
            VariableEnum.SQL_SHOW,
            VariableEnum.CHECK_TABLE_METADATA_ENABLED,
            VariableEnum.LOCK_WAIT_TIMEOUT_MILLISECONDS,
            VariableEnum.SHOW_PROCESS_LIST_ENABLED,
            VariableEnum.PROXY_BACKEND_QUERY_FETCH_SIZE,
            VariableEnum.CHECK_DUPLICATE_TABLE_ENABLED,
            VariableEnum.SQL_COMMENT_PARSE_ENABLED,
            VariableEnum.PROXY_FRONTEND_EXECUTOR_SIZE,
            VariableEnum.PROXY_BACKEND_EXECUTOR_SUITABLE,
            VariableEnum.PROXY_FRONTEND_CONNECTION_LIMIT);
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        VariableEnum variable = VariableEnum.getValueOf(sqlStatement.getName());
        return Collections.singletonList(new QueryHeader("", "", variable.name().toLowerCase(), variable.name(), Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false));
    }
    
    @Override
    protected MergedResult createMergedResult() {
        VariableEnum variable = VariableEnum.getValueOf(sqlStatement.getName());
        if (propsVariables.contains(variable)) {
            ConfigurationProperties configurationProperties = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps();
            String propertyValue = configurationProperties.getValue(ConfigurationPropertyKey.valueOf(variable.name())).toString();
            return new MultipleLocalDataMergedResult(Collections.singletonList(Collections.singletonList(propertyValue)));
        }
        switch (variable) {
            case AGENT_PLUGINS_ENABLED:
                return new MultipleLocalDataMergedResult(Collections.singletonList(Collections.singletonList(SystemPropertyUtil.getSystemProperty(variable.name(), Boolean.FALSE.toString()))));
            case CACHED_CONNECTIONS:
                return new MultipleLocalDataMergedResult(Collections.singletonList(Collections.singletonList(backendConnection.getConnectionSize())));
            case TRANSACTION_TYPE:
                return new MultipleLocalDataMergedResult(Collections.singletonList(Collections.singletonList(backendConnection.getTransactionStatus().getTransactionType().name())));
            default:
                throw new UnsupportedVariableException(sqlStatement.getName());
        }
    }
}
