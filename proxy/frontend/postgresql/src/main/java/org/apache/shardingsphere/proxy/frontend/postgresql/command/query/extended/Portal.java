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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.util.List;

/**
 * PostgreSQL portal.
 *
 * @param <T> type of bind result
 */
public interface Portal<T> {
    
    /**
     * Get portal's name.
     *
     * @return portal's name
     */
    String getName();
    
    /**
     * Get SQL statement.
     *
     * @return SQL statement
     */
    SQLStatement getSqlStatement();
    
    /**
     * Do bind.
     *
     * @return bind result, could be null
     */
    T bind();
    
    /**
     * Describe portal.
     *
     * @return portal description packet
     */
    PostgreSQLPacket describe();
    
    /**
     * Execute portal.
     *
     * @param maxRows max rows of query result
     * @return execute result
     */
    List<PostgreSQLPacket> execute(int maxRows);
    
    /**
     * Close portal.
     */
    void close();
}
