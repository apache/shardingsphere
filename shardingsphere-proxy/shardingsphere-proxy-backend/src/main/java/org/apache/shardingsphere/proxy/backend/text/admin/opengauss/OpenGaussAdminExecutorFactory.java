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

package org.apache.shardingsphere.proxy.backend.text.admin.opengauss;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutorFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.PostgreSQLAdminExecutorFactory;

import java.util.Optional;

/**
 * Admin executor factory for openGauss.
 */
public final class OpenGaussAdminExecutorFactory implements DatabaseAdminExecutorFactory {
    
    private static final String OG_DATABASE = "pg_database";
    
    private final PostgreSQLAdminExecutorFactory delegated = new PostgreSQLAdminExecutorFactory();
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final SQLStatementContext<?> sqlStatementContext) {
        return delegated.newInstance(sqlStatementContext);
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final SQLStatementContext<?> sqlStatementContext, final String sql, final String schemaName) {
        if (sqlStatementContext.getTablesContext().getTableNames().contains(OG_DATABASE)) {
            return Optional.of(new OpenGaussSelectDatabaseExecutor(sql));
        }
        return delegated.newInstance(sqlStatementContext, sql, schemaName);
    }
    
    @Override
    public String getType() {
        return "openGauss";
    }
}
