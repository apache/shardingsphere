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

package org.apache.shardingsphere.proxy.backend.handler.admin.executor;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.util.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPI;

import java.util.Optional;

/**
 * Database admin executor creator.
 */
@SingletonSPI
public interface DatabaseAdminExecutorCreator extends TypedSPI {
    
    /**
     * Create database admin executor, and this executor requires a connection containing a schema to be used.
     *
     * @param sqlStatementContext SQL statement context
     * @return created instance
     */
    Optional<DatabaseAdminExecutor> create(SQLStatementContext<?> sqlStatementContext);
    
    /**
     * Create database admin executor.
     *
     * @param sqlStatementContext SQL statement context
     * @param sql SQL
     * @param databaseName database name
     * @return created instance
     */
    Optional<DatabaseAdminExecutor> create(SQLStatementContext<?> sqlStatementContext, String sql, String databaseName);
}
