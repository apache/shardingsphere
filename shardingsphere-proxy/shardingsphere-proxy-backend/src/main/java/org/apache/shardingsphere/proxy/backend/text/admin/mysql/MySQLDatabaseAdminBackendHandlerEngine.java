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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql;

import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.text.admin.DatabaseAdminBackendHandler;
import org.apache.shardingsphere.proxy.backend.text.admin.DatabaseAdminBackendHandlerEngine;
import org.apache.shardingsphere.proxy.backend.text.metadata.schema.SchemaBackendHandlerFactory;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.Optional;

/**
 * Database admin backend handler engine for MySQL.
 */
public final class MySQLDatabaseAdminBackendHandlerEngine implements DatabaseAdminBackendHandlerEngine {
    
    @Override
    public Optional<DatabaseAdminBackendHandler> newInstance(final SQLStatement sqlStatement, final BackendConnection backendConnection) {
        return SchemaBackendHandlerFactory.newInstance(sqlStatement, backendConnection);
    }
    
    @Override
    public String getType() {
        return "MySQL";
    }
}
