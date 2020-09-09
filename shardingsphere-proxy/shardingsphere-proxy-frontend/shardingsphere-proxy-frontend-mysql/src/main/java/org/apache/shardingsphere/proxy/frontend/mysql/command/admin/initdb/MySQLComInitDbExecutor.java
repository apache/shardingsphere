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

package org.apache.shardingsphere.proxy.frontend.mysql.command.admin.initdb;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLServerErrorCode;
import org.apache.shardingsphere.db.protocol.mysql.packet.command.admin.initdb.MySQLComInitDbPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLErrPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.generic.MySQLOKPacket;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;

import java.util.Collection;
import java.util.Collections;

/**
 * COM_INIT_DB command executor for MySQL.
 */
@RequiredArgsConstructor
public final class MySQLComInitDbExecutor implements CommandExecutor {
    
    private final MySQLComInitDbPacket packet;
    
    private final BackendConnection backendConnection;
    
    @Override
    public Collection<DatabasePacket<?>> execute() {
        String schema = SQLUtil.getExactlyValue(packet.getSchema());
        if (ProxyContext.getInstance().schemaExists(schema) && isAuthorizedSchema(schema)) {
            backendConnection.setCurrentSchema(packet.getSchema());
            return Collections.singletonList(new MySQLOKPacket(1));
        }
        return Collections.singletonList(new MySQLErrPacket(1, MySQLServerErrorCode.ER_BAD_DB_ERROR, packet.getSchema()));
    }
    
    private boolean isAuthorizedSchema(final String schema) {
        Collection<String> authorizedSchemas = ProxyContext.getInstance().getSchemaContexts().getAuthentication().getUsers().get(backendConnection.getUsername()).getAuthorizedSchemas();
        return authorizedSchemas.isEmpty() || authorizedSchemas.contains(schema);
    }
}
