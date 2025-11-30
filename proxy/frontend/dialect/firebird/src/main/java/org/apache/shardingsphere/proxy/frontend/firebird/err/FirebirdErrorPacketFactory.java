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

package org.apache.shardingsphere.proxy.frontend.firebird.err;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.exception.core.SQLExceptionTransformEngine;
import org.apache.shardingsphere.database.protocol.firebird.packet.FirebirdPacket;
import org.apache.shardingsphere.database.protocol.firebird.packet.generic.FirebirdGenericResponsePacket;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;

import java.sql.SQLException;

/**
 * Error packet factory for Firebird.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class FirebirdErrorPacketFactory {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "Firebird");
    
    /**
     * Create new instance of Firebird error packet.
     *
     * @param cause cause
     * @return created instance
     */
    public static FirebirdPacket newInstance(final Exception cause) {
        SQLException sqlEx = SQLExceptionTransformEngine.toSQLException(cause, DATABASE_TYPE);
        return FirebirdGenericResponsePacket.getPacket().setErrorStatusVector(sqlEx);
    }
}
