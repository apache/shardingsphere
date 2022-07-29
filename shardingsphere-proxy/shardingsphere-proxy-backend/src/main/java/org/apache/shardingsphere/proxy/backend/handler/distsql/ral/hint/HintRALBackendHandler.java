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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.hint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.ral.HintRALStatement;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.data.QueryResponseRow;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.RALBackendHandler;

import java.sql.SQLException;

/**
 * Hint RAL backend handler.
 */
@RequiredArgsConstructor
@Getter
public final class HintRALBackendHandler extends RALBackendHandler {
    
    private final HintRALStatement sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    private HintRALStatementExecutor<? extends HintRALStatement> hintRALStatementExecutor;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        if (!ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Boolean>getValue(ConfigurationPropertyKey.PROXY_HINT_ENABLED)) {
            throw new UnsupportedOperationException(String.format("%s should be true, please check your config", ConfigurationPropertyKey.PROXY_HINT_ENABLED.getKey()));
        }
        hintRALStatementExecutor = HintRALStatementExecutorFactory.newInstance(sqlStatement, connectionSession);
        return hintRALStatementExecutor.execute();
    }
    
    @Override
    public boolean next() throws SQLException {
        return hintRALStatementExecutor.next();
    }
    
    @Override
    public QueryResponseRow getRowData() throws SQLException {
        return hintRALStatementExecutor.getQueryResponseRow();
    }
}
