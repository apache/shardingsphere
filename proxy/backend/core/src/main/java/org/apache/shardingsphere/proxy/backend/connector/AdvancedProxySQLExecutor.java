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

package org.apache.shardingsphere.proxy.backend.connector;

import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.List;

/**
 * Advanced proxy SQL executor.
 */
@SingletonSPI
public interface AdvancedProxySQLExecutor {
    
    /**
     * Execute.
     *
     * @param executionContext execution context
     * @param contextManager context manager
     * @param database database
     * @param databaseConnector database connector
     * @return execute result
     * @throws SQLException SQL exception
     */
    List<ExecuteResult> execute(ExecutionContext executionContext, ContextManager contextManager, ShardingSphereDatabase database, DatabaseConnector databaseConnector) throws SQLException;
}
