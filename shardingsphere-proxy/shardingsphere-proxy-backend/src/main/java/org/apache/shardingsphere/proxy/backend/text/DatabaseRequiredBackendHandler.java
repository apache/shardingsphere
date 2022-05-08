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

package org.apache.shardingsphere.proxy.backend.text;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.proxy.backend.exception.UnknownDatabaseException;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.SchemaSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.available.FromSchemaAvailable;

import java.sql.SQLException;
import java.util.Optional;

/**
 * Database required backend handler.
 *
 * @param <T> type of SQL statement
 */
@RequiredArgsConstructor
@Getter
public abstract class DatabaseRequiredBackendHandler<T extends SQLStatement> implements TextProtocolBackendHandler {
    
    private final T sqlStatement;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public final ResponseHeader execute() throws SQLException {
        String databaseName = getDatabaseName(connectionSession, sqlStatement);
        checkDatabaseName(databaseName);
        return execute(databaseName, sqlStatement);
    }
    
    protected abstract ResponseHeader execute(String databaseName, T sqlStatement) throws SQLException;
    
    private String getDatabaseName(final ConnectionSession connectionSession, final T sqlStatement) {
        Optional<SchemaSegment> schemaFromSQL = sqlStatement instanceof FromSchemaAvailable ? ((FromSchemaAvailable) sqlStatement).getSchema() : Optional.empty();
        return schemaFromSQL.isPresent() ? schemaFromSQL.get().getIdentifier().getValue() : connectionSession.getDatabaseName();
    }
    
    private void checkDatabaseName(final String databaseName) {
        if (null == databaseName) {
            throw new NoDatabaseSelectedException();
        }
        if (!ProxyContext.getInstance().databaseExists(databaseName)) {
            throw new UnknownDatabaseException(databaseName);
        }
    }
}
