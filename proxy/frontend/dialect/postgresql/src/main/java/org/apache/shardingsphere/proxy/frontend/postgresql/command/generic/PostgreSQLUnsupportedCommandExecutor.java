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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.generic;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.exception.postgresql.vendor.PostgreSQLVendorError;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.constant.PostgreSQLMessageSeverityLevel;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLErrorResponsePacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;

import java.util.Collection;
import java.util.Collections;

/**
 * Unsupported command executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLUnsupportedCommandExecutor implements CommandExecutor {
    
    @Override
    public Collection<DatabasePacket> execute() {
        // TODO consider what severity and error code to use
        PostgreSQLErrorResponsePacket packet = PostgreSQLErrorResponsePacket.newBuilder(PostgreSQLMessageSeverityLevel.ERROR, PostgreSQLVendorError.FEATURE_NOT_SUPPORTED,
                PostgreSQLVendorError.FEATURE_NOT_SUPPORTED.getReason()).build();
        return Collections.singleton(packet);
    }
}
