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

package org.apache.shardingsphere.proxy.backend.text.distsql;

import com.mchange.v1.db.sql.UnsupportedTypeException;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.distsql.parser.statement.DistSQLStatement;
import org.apache.shardingsphere.distsql.parser.statement.ral.RALStatement;
import org.apache.shardingsphere.distsql.parser.statement.rdl.RDLStatement;
import org.apache.shardingsphere.distsql.parser.statement.rql.RQLStatement;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.text.TextProtocolBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.distsql.ral.RALBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.rdl.RDLBackendHandlerFactory;
import org.apache.shardingsphere.proxy.backend.text.distsql.rql.RQLBackendHandlerFactory;

import java.sql.SQLException;

/**
 * DistSQL backend handler factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistSQLBackendHandlerFactory {
    
    /**
     * Create new instance of DistSQL backend handler.
     *
     * @param databaseType database type
     * @param sqlStatement dist SQL statement
     * @param connectionSession connection session
     * @return text protocol backend handler
     * @throws SQLException SQL exception
     */
    public static TextProtocolBackendHandler newInstance(final DatabaseType databaseType, final DistSQLStatement sqlStatement, final ConnectionSession connectionSession) throws SQLException {
        if (sqlStatement instanceof RQLStatement) {
            return RQLBackendHandlerFactory.newInstance((RQLStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof RDLStatement) {
            return RDLBackendHandlerFactory.newInstance(databaseType, (RDLStatement) sqlStatement, connectionSession);
        }
        if (sqlStatement instanceof RALStatement) {
            return RALBackendHandlerFactory.newInstance(databaseType, (RALStatement) sqlStatement, connectionSession);
        }
        throw new UnsupportedTypeException(sqlStatement.getClass().getCanonicalName());
    }
}
