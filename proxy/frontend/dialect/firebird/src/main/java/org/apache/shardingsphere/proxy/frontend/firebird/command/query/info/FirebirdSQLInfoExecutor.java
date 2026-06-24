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

package org.apache.shardingsphere.proxy.frontend.firebird.command.query.info;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLRecordsInfo;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Collection;
import java.util.Collections;

/**
 * Database info command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdSQLInfoExecutor implements CommandExecutor {
    
    private final FirebirdInfoPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() {
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(new FirebirdSQLInfoReturnPacket(packet.getInfoItems(), getRecordsInfo())));
    }
    
    private FirebirdSQLRecordsInfo getRecordsInfo() {
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(packet.getHandle());
        if (null == preparedStatement) {
            return new FirebirdSQLRecordsInfo(0L, 0L, 0L);
        }
        long affectedRows = preparedStatement.getAffectedRows();
        SQLStatement sqlStatement = preparedStatement.getSqlStatementContext().getSqlStatement();
        long insertCount = sqlStatement instanceof InsertStatement ? affectedRows : 0L;
        long updateCount = sqlStatement instanceof UpdateStatement ? affectedRows : 0L;
        long deleteCount = sqlStatement instanceof DeleteStatement ? affectedRows : 0L;
        return new FirebirdSQLRecordsInfo(insertCount, updateCount, deleteCount);
    }
}
