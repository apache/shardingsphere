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

package org.apache.shardingsphere.proxy.frontend.command.executor;

import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;

import java.sql.SQLException;

/**
 * Query command executor.
 */
public interface QueryCommandExecutor extends CommandExecutor {
    
    /**
     * Get response type.
     *
     * @return response type
     */
    ResponseType getResponseType();
    
    /**
     * Goto next result value.
     *
     * @return has more result value or not
     * @throws SQLException SQL exception
     */
    boolean next() throws SQLException;
    
    /**
     * Get query row packet.
     *
     * @return database packet of query row
     * @throws SQLException SQL exception
     */
    DatabasePacket<?> getQueryRowPacket() throws SQLException;
}
