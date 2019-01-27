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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command;

import com.google.common.base.Optional;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.DatabasePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.generic.ErrorResponse;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;

/**
 * PostgreSQL command response packets.
 *
 * @author zhangyonglun
 */
@NoArgsConstructor
@Getter
public class PostgreSQLCommandResponsePackets {
    
    private final Collection<DatabasePacket> packets = new LinkedList<>();
    
    public PostgreSQLCommandResponsePackets(final DatabasePacket databasePacket) {
        packets.add(databasePacket);
    }
    
    public PostgreSQLCommandResponsePackets(final Exception exception) {
        Optional<SQLException> sqlException = findSQLException(exception);
        packets.add(sqlException.isPresent() ? new ErrorResponse() : new ErrorResponse());
    }
    
    private Optional<SQLException> findSQLException(final Exception exception) {
        if (exception instanceof SQLException) {
            return Optional.of((SQLException) exception);
        }
        if (null == exception.getCause()) {
            return Optional.absent();
        }
        if (exception.getCause() instanceof SQLException) {
            return Optional.of((SQLException) exception.getCause());
        }
        if (null == exception.getCause().getCause()) {
            return Optional.absent();
        }
        if (exception.getCause().getCause() instanceof SQLException) {
            return Optional.of((SQLException) exception.getCause().getCause());
        }
        return Optional.absent();
    }
    
    /**
     * Get head packet.
     *
     * @return head packet
     */
    public DatabasePacket getHeadPacket() {
        return packets.iterator().next();
    }
}
