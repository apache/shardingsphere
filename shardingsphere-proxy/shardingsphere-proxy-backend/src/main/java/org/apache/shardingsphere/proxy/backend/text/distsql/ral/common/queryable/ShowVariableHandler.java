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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Show variable handler.
 */
public final class ShowVariableHandler extends QueryableRALBackendHandler<ShowVariableStatement, ShowVariableHandler> {
    
    private ConnectionSession connectionSession;
    
    @Override
    public ShowVariableHandler init(final HandlerParameter<ShowVariableStatement> parameter) {
        initStatement(parameter.getStatement());
        connectionSession = parameter.getConnectionSession();
        return this;
    }
    
    @Override
    protected Collection<String> getColumnNames() {
        return Collections.singletonList(sqlStatement.getName().toLowerCase());
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        if (ConfigurationPropertyKey.getKeyNames().contains(sqlStatement.getName())) {
            ConfigurationProperties configurationProperties = contextManager.getMetaDataContexts().getProps();
            String propertyValue = configurationProperties.getValue(ConfigurationPropertyKey.valueOf(sqlStatement.getName())).toString();
            return Collections.singletonList(Collections.singletonList(propertyValue));
        }
        VariableEnum variable = VariableEnum.getValueOf(sqlStatement.getName());
        switch (variable) {
            case AGENT_PLUGINS_ENABLED:
                return Collections.singletonList(Collections.singletonList(SystemPropertyUtil.getSystemProperty(variable.name(), Boolean.TRUE.toString())));
            case CACHED_CONNECTIONS:
                if (connectionSession.getBackendConnection() instanceof JDBCBackendConnection) {
                    int connectionSize = ((JDBCBackendConnection) connectionSession.getBackendConnection()).getConnectionSize();
                    return Collections.singletonList(Collections.singletonList(connectionSize));
                }
                break;
            case TRANSACTION_TYPE:
                TransactionType transactionType = connectionSession.getTransactionStatus().getTransactionType();
                return Collections.singletonList(Collections.singletonList(transactionType.name()));
            default:
        }
        throw new UnsupportedVariableException(sqlStatement.getName());
    }
}
