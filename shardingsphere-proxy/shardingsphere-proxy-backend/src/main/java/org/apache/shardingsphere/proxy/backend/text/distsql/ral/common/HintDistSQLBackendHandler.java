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

package org.apache.shardingsphere.proxy.backend.text.distsql.ral.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.common.HintDistSQLStatement;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.HintStatementExecutor;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.common.hint.HintStatementExecutorFactory;

import java.sql.SQLException;
import java.util.Collection;

/**
 * Hint dist sql backend handler.
 */
@RequiredArgsConstructor
@Getter
public final class HintDistSQLBackendHandler implements TextProtocolBackendHandler {
    
    private final HintDistSQLStatement sqlStatement;
    
    private final BackendConnection backendConnection;
    
    private HintStatementExecutor hintStatementExecutor;
    
    @SuppressWarnings("unchecked")
    @Override
    public ResponseHeader execute() throws SQLException {
        if (!ProxyContext.getInstance().getMetaDataContexts().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)) {
            throw new UnsupportedOperationException(String.format("%s should be true, please check your config", ConfigurationPropertyKey.PROXY_HINT_ENABLED.getKey()));
        }
        hintStatementExecutor = HintStatementExecutorFactory.newInstance(sqlStatement, backendConnection);
        return hintStatementExecutor.execute();
    }
    
    @Override
    public boolean next() throws SQLException {
        return hintStatementExecutor.next();
    }
    
    @Override
    public Collection<Object> getRowData() throws SQLException {
        return hintStatementExecutor.getQueryResponseRow().getData();
    }
}
