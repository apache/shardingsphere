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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable.updater;

import org.apache.shardingsphere.distsql.handler.ral.update.RALUpdater;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import java.sql.SQLException;

/**
 * Connection session required RAL updater.
 * 
 * @param <T> type of SQL statement
 */
public interface ConnectionSessionRequiredRALUpdater<T extends SQLStatement> extends RALUpdater<T> {
    
    /**
     * Execute update.
     *
     * @param connectionSession connection session
     * @param sqlStatement updatable RAL statement
     * @throws SQLException SQL exception
     */
    void executeUpdate(ConnectionSession connectionSession, T sqlStatement) throws SQLException;
}
