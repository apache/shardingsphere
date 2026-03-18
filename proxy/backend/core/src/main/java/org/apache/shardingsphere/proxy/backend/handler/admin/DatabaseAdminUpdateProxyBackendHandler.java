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

package org.apache.shardingsphere.proxy.backend.handler.admin;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.sql.SQLException;

/**
 * Database admin update proxy backend handler.
 */
@RequiredArgsConstructor
public final class DatabaseAdminUpdateProxyBackendHandler implements ProxyBackendHandler {
    
    private final ContextManager contextManager;
    
    private final ConnectionSession connectionSession;
    
    private final SQLStatement sqlStatement;
    
    private final DatabaseAdminExecutor executor;
    
    @Override
    public ResponseHeader execute() throws SQLException {
        executor.execute(connectionSession, contextManager.getMetaDataContexts().getMetaData());
        return new UpdateResponseHeader(sqlStatement);
    }
}
