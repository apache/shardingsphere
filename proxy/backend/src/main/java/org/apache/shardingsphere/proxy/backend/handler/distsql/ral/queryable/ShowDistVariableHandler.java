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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.queryable;

import org.apache.shardingsphere.distsql.parser.statement.ral.queryable.ShowDistVariableStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.exception.UnsupportedVariableException;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * Show dist variable handler.
 */
public final class ShowDistVariableHandler extends QueryableRALBackendHandler<ShowDistVariableStatement> {
    
    private static final String VARIABLE_NAME = "variable_name";
    
    private static final String VARIABLE_VALUE = "variable_value";
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(VARIABLE_NAME, VARIABLE_VALUE);
    }
    
    @Override
    protected Collection<LocalDataQueryResultRow> getRows(final ContextManager contextManager) {
        return buildSpecifiedRow(contextManager, getSqlStatement().getName());
    }
    
    private Collection<LocalDataQueryResultRow> buildSpecifiedRow(final ContextManager contextManager, final String key) {
        return isConfigurationKey(key)
                ? Collections.singletonList(new LocalDataQueryResultRow(key.toLowerCase(), getConfigurationValue(contextManager, key)))
                : Collections.singletonList(new LocalDataQueryResultRow(key.toLowerCase(), getSpecialValue(key)));
    }
    
    private boolean isConfigurationKey(final String key) {
        return ConfigurationPropertyKey.getKeyNames().contains(key);
    }
    
    private String getConfigurationValue(final ContextManager contextManager, final String key) {
        return contextManager.getMetaDataContexts().getMetaData().getProps().getValue(ConfigurationPropertyKey.valueOf(key)).toString();
    }
    
    private String getSpecialValue(final String key) {
        VariableEnum variable = VariableEnum.getValueOf(key);
        switch (variable) {
            case AGENT_PLUGINS_ENABLED:
                return SystemPropertyUtil.getSystemProperty(variable.name(), Boolean.TRUE.toString());
            case CACHED_CONNECTIONS:
                if (getConnectionSession().getBackendConnection() instanceof JDBCBackendConnection) {
                    int connectionSize = ((JDBCBackendConnection) getConnectionSession().getBackendConnection()).getConnectionSize();
                    return String.valueOf(connectionSize);
                }
                break;
            case TRANSACTION_TYPE:
                TransactionType transactionType = getConnectionSession().getTransactionStatus().getTransactionType();
                return transactionType.name();
            default:
        }
        throw new UnsupportedVariableException(key);
    }
}
