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
     * Judge whether query response.
     *
     * @return is query response or not
     */
    boolean isQueryResponse();
    
    /**
     * Judge whether update response.
     *
     * @return is update response or not
     */
    boolean isUpdateResponse();
    
    /**
     * Judge whether error response.
     *
     * @return is error response or not
     */
    boolean isErrorResponse();
    
    /**
     * Goto next result value.
     *
     * @return has more result value or not
     * @throws SQLException SQL exception
     */
    boolean next() throws SQLException;
    
    /**
     * Get query data.
     *
     * @return database packet of query data
     * @throws SQLException SQL exception
     */
    DatabasePacket<?> getQueryData() throws SQLException;
}
