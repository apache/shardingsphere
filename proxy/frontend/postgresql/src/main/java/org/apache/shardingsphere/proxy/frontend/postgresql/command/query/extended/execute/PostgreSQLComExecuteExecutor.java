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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PortalContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.Portal;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;

import java.util.List;

/**
 * Command execute executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComExecuteExecutor implements CommandExecutor {
    
    private final PortalContext portalContext;
    
    private final PostgreSQLComExecutePacket packet;
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    @Override
    public List execute() {
        return portalContext.get(packet.getPortal()).execute(packet.getMaxRows());
    }
    
    @Override
    public void close() {
        Portal<?> portal = portalContext.get(packet.getPortal());
        if (portal.getSqlStatement() instanceof CommitStatement || portal.getSqlStatement() instanceof RollbackStatement) {
            portalContext.closeAll();
        }
    }
}
