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

import org.apache.shardingsphere.distsql.parser.statement.ral.common.show.ShowAllVariablesStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.QueryableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.enums.VariableEnum;
import org.apache.shardingsphere.proxy.backend.util.SystemPropertyUtil;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Show all variables handler.
 */
public final class ShowAllVariablesHandler extends QueryableRALBackendHandler<ShowAllVariablesStatement, ShowAllVariablesHandler> {
    
    private static final String VARIABLE_NAME = "variable_name";
    
    private static final String VARIABLE_VALUE = "variable_value";
    
    private ConnectionSession connectionSession;
    
    @Override
    public ShowAllVariablesHandler init(final HandlerParameter<ShowAllVariablesStatement> parameter) {
        connectionSession = parameter.getConnectionSession();
        return super.init(parameter);
    }
    
    @Override
    protected Collection<String> getColumnNames() {
        return Arrays.asList(VARIABLE_NAME, VARIABLE_VALUE);
    }
    
    @Override
    protected Collection<List<Object>> getRows(final ContextManager contextManager) {
        List<List<Object>> result = new LinkedList<>();
        ConfigurationProperties configurationProperties = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getProps();
        ConfigurationPropertyKey.getKeyNames().forEach(each -> {
            String propertyValue = configurationProperties.getValue(ConfigurationPropertyKey.valueOf(each)).toString();
            result.add(Arrays.asList(each.toLowerCase(), propertyValue));
        });
        result.add(Arrays.asList(VariableEnum.AGENT_PLUGINS_ENABLED.name().toLowerCase(), SystemPropertyUtil.getSystemProperty(VariableEnum.AGENT_PLUGINS_ENABLED.name(), Boolean.TRUE.toString())));
        if (connectionSession.getBackendConnection() instanceof JDBCBackendConnection) {
            result.add(Arrays.asList(VariableEnum.CACHED_CONNECTIONS.name().toLowerCase(), ((JDBCBackendConnection) connectionSession.getBackendConnection()).getConnectionSize()));
        }
        result.add(Arrays.asList(VariableEnum.TRANSACTION_TYPE.name().toLowerCase(), connectionSession.getTransactionStatus().getTransactionType().name()));
        return result;
    }
}
