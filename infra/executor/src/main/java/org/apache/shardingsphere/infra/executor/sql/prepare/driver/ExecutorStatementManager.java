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

package org.apache.shardingsphere.infra.executor.sql.prepare.driver;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;

import java.sql.SQLException;

/**
 * Executor statement manager.
 * 
 * @param <C> type of resource connection
 * @param <R> type of storage resource
 * @param <O> type of storage resource option
 */
public interface ExecutorStatementManager<C, R, O extends StorageResourceOption> {
    
    /**
     * Create storage resource.
     *
     * @param connection connection
     * @param connectionMode connection mode
     * @param option storage resource option
     * @param databaseType database type
     * @return storage resource
     * @throws SQLException SQL exception
     */
    R createStorageResource(C connection, ConnectionMode connectionMode, O option, DatabaseType databaseType) throws SQLException;
    
    /**
     * Create storage resource.
     *
     * @param executionUnit execution unit
     * @param connection connection
     * @param connectionOffset connection offset
     * @param connectionMode connection mode
     * @param option storage resource option
     * @param databaseType database type
     * @return storage resource
     * @throws SQLException SQL exception
     */
    R createStorageResource(ExecutionUnit executionUnit, C connection, int connectionOffset, ConnectionMode connectionMode, O option, DatabaseType databaseType) throws SQLException;
}
