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

package org.apache.shardingsphere.proxy.frontend.postgresql;

import lombok.Getter;
import org.apache.shardingsphere.database.exception.core.exception.transaction.InTransactionException;
import org.apache.shardingsphere.database.protocol.codec.DatabasePacketCodecEngine;
import org.apache.shardingsphere.database.protocol.postgresql.codec.PostgreSQLPacketCodecEngine;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.authentication.AuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.authentication.PostgreSQLAuthenticationEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLCommandExecuteEngine;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLPortalContextRegistry;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.postgresql.core.Oid;
import org.postgresql.core.QueryExecutor;
import org.postgresql.jdbc.PgConnection;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * Frontend engine for PostgreSQL.
 */
@Getter
public final class PostgreSQLFrontendEngine implements DatabaseProtocolFrontendEngine {
    
    private final AuthenticationEngine authenticationEngine = new PostgreSQLAuthenticationEngine();
    
    private final PostgreSQLCommandExecuteEngine commandExecuteEngine = new PostgreSQLCommandExecuteEngine();
    
    private final DatabasePacketCodecEngine codecEngine = new PostgreSQLPacketCodecEngine();
    
    @Override
    public void release(final ConnectionSession connectionSession) {
        PostgreSQLPortalContextRegistry.getInstance().remove(connectionSession.getConnectionId());
    }
    
    @Override
    public void handleException(final ConnectionSession connectionSession, final Exception exception) {
        if (connectionSession.getTransactionStatus().isInTransaction() && !connectionSession.getConnectionContext().getTransactionContext().isExceptionOccur()
                && !(exception instanceof InTransactionException)) {
            connectionSession.getConnectionContext().getTransactionContext().setExceptionOccur(true);
        }
    }
    
    @Override
    public String getDatabaseType() {
        return "PostgreSQL";
    }
    
    @Override
    public void init(final ConnectionSession connectionSession) {
        connectionSession.getDatabaseConnectionManager().getAfterCreateConnectionPostProcessors().add(this::initConnection);
    }
    
    /**
     * init connection.
     * @param target Connection to set
     * @throws SQLException unwrap error
     */
    private void initConnection(final Connection target) throws SQLException {
        PgConnection pgConnection = target.unwrap(PgConnection.class);
        QueryExecutor queryExecutor = pgConnection.getQueryExecutor();
        queryExecutor.addBinarySendOid(Oid.DATE);
        queryExecutor.addBinarySendOid(Oid.BOOL_ARRAY);
        queryExecutor.addBinarySendOid(Oid.DATE_ARRAY);
        queryExecutor.addBinarySendOid(Oid.TIME);
        queryExecutor.addBinarySendOid(Oid.TIME_ARRAY);
        queryExecutor.addBinarySendOid(Oid.TIMESTAMP);
        queryExecutor.addBinarySendOid(Oid.TIMESTAMP_ARRAY);
        queryExecutor.addBinarySendOid(Oid.NUMERIC);
        queryExecutor.addBinarySendOid(Oid.NUMERIC_ARRAY);
    }
}
