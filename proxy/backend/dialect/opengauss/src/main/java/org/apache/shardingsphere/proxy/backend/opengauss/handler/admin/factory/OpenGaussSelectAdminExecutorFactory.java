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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.factory.PostgreSQLSelectAdminExecutorFactory;

import java.util.List;
import java.util.Optional;

/**
 * Select admin executor factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class OpenGaussSelectAdminExecutorFactory {
    
    /**
     * Create new instance of database admin executor.
     *
     * @param selectStatementContext select statement context
     * @param sql SQL
     * @param parameters SQL parameters
     * @return created instance
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatementContext selectStatementContext, final String sql, final List<Object> parameters) {
        Optional<DatabaseAdminExecutor> systemTableQueryExecutor = OpenGaussSystemTableQueryExecutorFactory.newInstance(selectStatementContext, sql, parameters);
        if (systemTableQueryExecutor.isPresent()) {
            return systemTableQueryExecutor;
        }
        Optional<DatabaseAdminExecutor> systemFunctionQueryExecutor = OpenGaussSystemFunctionQueryExecutorFactory.newInstance(selectStatementContext);
        if (systemFunctionQueryExecutor.isPresent()) {
            return systemFunctionQueryExecutor;
        }
        return PostgreSQLSelectAdminExecutorFactory.newInstance(selectStatementContext, sql, parameters);
    }
}
