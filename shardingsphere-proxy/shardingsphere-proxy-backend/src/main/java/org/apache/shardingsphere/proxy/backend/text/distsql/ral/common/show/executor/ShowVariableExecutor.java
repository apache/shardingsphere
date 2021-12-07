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
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.impl.QueryHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.sharding.merge.dal.common.MultipleLocalDataMergedResult;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Types;
import java.util.Collections;
import java.util.List;

/**
 * Show variable executor.
 */
@RequiredArgsConstructor
@Getter
public final class ShowVariableExecutor extends AbstractShowExecutor {
    
    private final ShowVariableStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    protected List<QueryHeader> createQueryHeaders() {
        return Collections.singletonList(new QueryHeader("", "", sqlStatement.getName().toLowerCase(), sqlStatement.getName(), Types.VARCHAR, "VARCHAR", 100, 0, false, false, false, false));
    }
    
    @Override
    protected MergedResult createMergedResult() {
        if (ConfigurationPropertyKey.getKeyNames().contains(sqlStatement.getName())) {
            ConfigurationProperties configurationProperties = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps();
            String propertyValue = configurationProperties.getValue(ConfigurationPropertyKey.valueOf(sqlStatement.getName())).toString();
            return new MultipleLocalDataMergedResult(Collections.singletonList(Collections.singletonList(propertyValue)));
        }
        VariableEnum variable = VariableEnum.getValueOf(sqlStatement.getName());
        switch (variable) {
            case AGENT_PLUGINS_ENABLED:
                return new MultipleLocalDataMergedResult(Collections.singletonList(Collections.singletonList(SystemPropertyUtil.getSystemProperty(variable.name(), Boolean.FALSE.toString()))));
            case CACHED_CONNECTIONS:
                if (connectionSession.getBackendConnection() instanceof JDBCBackendConnection) {
                    int connectionSize = ((JDBCBackendConnection) connectionSession.getBackendConnection()).getConnectionSize();
                    return new MultipleLocalDataMergedResult(Collections.singletonList(Collections.singletonList(connectionSize)));
                }
                break;
            case TRANSACTION_TYPE:
                TransactionType transactionType = connectionSession.getTransactionStatus().getTransactionType();
                return new MultipleLocalDataMergedResult(Collections.singletonList(Collections.singletonList(transactionType.name())));
            default:
        }
        throw new UnsupportedVariableException(sqlStatement.getName());
    }
}
