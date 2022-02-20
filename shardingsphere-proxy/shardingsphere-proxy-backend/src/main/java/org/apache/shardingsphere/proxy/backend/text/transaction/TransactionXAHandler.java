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

package org.apache.shardingsphere.proxy.backend.text.transaction;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.data.impl.SchemaAssignedDatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.TCLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.XAStatement;
import org.apache.shardingsphere.transaction.TransactionHolder;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;

/**
 * XA transaction handler.
 * TODO Currently XA transaction started with `XA START` doesn't support for database with multiple datasource, a flag should be added for this both in init progress and add datasource from distSQL.
 */
@RequiredArgsConstructor
public final class TransactionXAHandler implements TextProtocolBackendHandler {

    private final XAStatement tclStatement;

    private final ConnectionSession connectionSession;

    private final SchemaAssignedDatabaseBackendHandler backendHandler;

    public TransactionXAHandler(final SQLStatementContext<? extends TCLStatement> sqlStatementContext, final String sql, final ConnectionSession connectionSession) {
        this.tclStatement = (XAStatement) sqlStatementContext.getSqlStatement();
        this.connectionSession = connectionSession;
        this.backendHandler = new SchemaAssignedDatabaseBackendHandler(sqlStatementContext, sql, connectionSession);
    }

    @Override
    public boolean next() throws SQLException {
        return this.tclStatement.getOp().equals("RECOVER") && this.backendHandler.next();
    }

    @Override
    public Collection<Object> getRowData() throws SQLException {
        return this.tclStatement.getOp().equals("RECOVER") ? this.backendHandler.getRowData() : Collections.emptyList();
    }

    @Override
    public ResponseHeader execute() throws SQLException {
        switch (tclStatement.getOp()) {
            case "START":
            case "BEGIN":
                /**
                 * we have to let session occupy the thread when doing xa transaction.
                 * according to https://dev.mysql.com/doc/refman/5.7/en/xa-states.html XA and local transactions are mutually exclusive
                 */
                if (connectionSession.getTransactionStatus().isInTransaction()) {
                    throw new SQLException("can not start in a Active transaction");
                }
                ResponseHeader header = backendHandler.execute();
                TransactionHolder.setInTransaction();
                connectionSession.getTransactionStatus().setManualXA(true);
                return header;
            case "END":
            case "PREPARE":
            case "RECOVER":
                return backendHandler.execute();
            case "COMMIT":
            case "ROLLBACK":
                try {
                    return backendHandler.execute();
                } finally {
                    connectionSession.getTransactionStatus().setManualXA(false);
                    TransactionHolder.clear();
                }
            default:
                throw new SQLException("unrecognized XA statement " + tclStatement.getOp());
        }
    }
}
