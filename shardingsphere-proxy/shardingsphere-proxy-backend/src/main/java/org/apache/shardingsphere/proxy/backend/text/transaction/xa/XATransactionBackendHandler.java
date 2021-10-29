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

package org.apache.shardingsphere.proxy.backend.text.transaction.xa;

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.BackendTransactionManager;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.impl.BroadcastDatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.XAStatement;

import java.sql.SQLException;

/**
 * XA related operations.
 */
public class XATransactionBackendHandler implements TextProtocolBackendHandler {

    private final XAStatement xaStatement;

    private final BackendTransactionManager backendTransactionManager;

    private final BackendConnection backendConnection;

    private final BroadcastDatabaseBackendHandler broadcastDatabaseBackendHandler;

    public XATransactionBackendHandler(final XAStatement xaStatement,
                                       final BroadcastDatabaseBackendHandler broadcastDatabaseBackendHandler,
                                       final BackendConnection backendConnection) {
        this.xaStatement = xaStatement;
        this.backendConnection = backendConnection;
        this.broadcastDatabaseBackendHandler = broadcastDatabaseBackendHandler;
        this.backendTransactionManager = new BackendTransactionManager(backendConnection);
    }

    @Override
    public ResponseHeader execute() throws SQLException {
        switch (xaStatement.getOp()) {
            case "START":
            case "BEGIN":
                if (backendConnection.getTransactionStatus().isInTransaction()) {
                    backendTransactionManager.commit();
                }
                backendConnection.getTransactionStatus().setInXA(true);
                break;
            case "COMMIT":
            case "ROLLBACK":
                backendConnection.getTransactionStatus().setInXA(false);
                break;
            default:
                break;
        }
        return broadcastDatabaseBackendHandler.execute();
    }
}
