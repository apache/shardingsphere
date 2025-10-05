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

package org.apache.shardingsphere.proxy.backend.handler.tcl.local.type;

import org.apache.shardingsphere.proxy.backend.connector.jdbc.transaction.ProxyBackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.tcl.TCLStatement;

import java.sql.SQLException;

/**
 * Rollback proxy backend handler.
 */
public final class RollbackProxyBackendHandler implements ProxyBackendHandler {
    
    private final TCLStatement sqlStatement;
    
    private final ProxyBackendTransactionManager transactionManager;
    
    public RollbackProxyBackendHandler(final TCLStatement sqlStatement, final ConnectionSession connectionSession) {
        this.sqlStatement = sqlStatement;
        transactionManager = new ProxyBackendTransactionManager(connectionSession.getDatabaseConnectionManager());
    }
    
    @Override
    public ResponseHeader execute() throws SQLException {
        transactionManager.rollback();
        return new UpdateResponseHeader(sqlStatement);
    }
}
