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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.schema;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;

import java.util.List;
import java.util.Optional;

/**
 * MySQL special table query executor factory.
 */
public interface MySQLSpecialTableQueryExecutorFactory {
    
    /**
     * Whether to be processed special table.
     * 
     * @param schemaName schema name
     * @param tableName table name
     * @return to be processed special table or not
     */
    boolean accept(String schemaName, String tableName);
    
    /**
     * Create executor.
     *
     * @param selectStatementContext select statement context
     * @param sql SQL being executed
     * @param parameters parameters
     * @param tableName table name
     * @return executor
     */
    Optional<DatabaseAdminExecutor> newInstance(SelectStatementContext selectStatementContext, String sql, List<Object> parameters, String tableName);
}
