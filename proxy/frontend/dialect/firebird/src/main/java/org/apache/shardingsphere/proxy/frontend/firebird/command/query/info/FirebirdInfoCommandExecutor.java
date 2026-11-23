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
import org.apache.shardingsphere.database.protocol.firebird.packet.command.FirebirdCommandPacketType;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.FirebirdInfoCommandPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.blob.FirebirdBlobInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.database.FirebirdDatabaseInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLInfoReturnPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.info.type.sql.FirebirdSQLRecordsInfo;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.FirebirdServerPreparedStatement;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobReadCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.cache.FirebirdBlobWriteCache;
import org.apache.shardingsphere.proxy.frontend.firebird.command.query.blob.generator.FirebirdBlobHandleGenerator;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.OptionalInt;

/**
 * Info command executor for Firebird.
 */
@RequiredArgsConstructor
public final class FirebirdInfoCommandExecutor implements CommandExecutor {
    
    private final FirebirdInfoCommandPacket packet;
    
    private final ConnectionSession connectionSession;
    
    private final FirebirdCommandPacketType commandPacketType;
    
    @Override
    public Collection<DatabasePacket> execute() {
        if (FirebirdCommandPacketType.INFO_BLOB == commandPacketType) {
            return getBlobInfo();
        }
        if (FirebirdCommandPacketType.INFO_DATABASE == commandPacketType) {
            return getDatabaseInfo();
        }
        return getSQLInfo();
    }
    
    private Collection<DatabasePacket> getBlobInfo() {
        int connectionId = connectionSession.getConnectionId();
        int blobHandle = FirebirdBlobHandleGenerator.getInstance().resolveBlobHandle(connectionId, packet.getInfoObject());
        FirebirdBlobInfoReturnPacket returnPacket = new FirebirdBlobInfoReturnPacket(packet.getItems(), getBlobLength(connectionId, blobHandle), isStreamBlob(connectionId, blobHandle));
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(returnPacket));
    }
    
    private int getBlobLength(final int connectionId, final int blobHandle) {
        OptionalInt totalSize = FirebirdBlobReadCache.getInstance().getTotalSize(connectionId, blobHandle);
        return totalSize.isPresent() ? totalSize.getAsInt() : FirebirdBlobWriteCache.getInstance().getBlobSizeByHandle(connectionId, blobHandle).orElse(0);
    }
    
    @HighFrequencyInvocation
    private boolean isStreamBlob(final int connectionId, final int blobHandle) {
        return FirebirdBlobReadCache.getInstance().isStreamBlob(connectionId, blobHandle).orElse(false);
    }
    
    private Collection<DatabasePacket> getDatabaseInfo() {
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(new FirebirdDatabaseInfoReturnPacket(packet.getItems())));
    }
    
    private Collection<DatabasePacket> getSQLInfo() {
        return Collections.singleton(new FirebirdGenericResponsePacket().setData(new FirebirdSQLInfoReturnPacket(packet.getItems(), getRecordsInfo())));
    }
    
    private FirebirdSQLRecordsInfo getRecordsInfo() {
        FirebirdServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(packet.getInfoObject());
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
