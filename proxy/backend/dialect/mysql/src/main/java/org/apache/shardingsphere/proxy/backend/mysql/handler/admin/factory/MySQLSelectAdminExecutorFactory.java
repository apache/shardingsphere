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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.schema.MySQLSystemSchemaExecutorFactory;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.schema.MySQLInformationSchemaExecutorFactory;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.factory.withoutfrom.MySQLSelectWithoutFromAdminExecutorFactory;

import java.util.List;
import java.util.Optional;

/**
 * Select admin executor factory for MySQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLSelectAdminExecutorFactory {
    
    private static final String INFORMATION_SCHEMA = "information_schema";
    
    private static final String PERFORMANCE_SCHEMA = "performance_schema";
    
    private static final String MYSQL_SCHEMA = "mysql";
    
    private static final String SYS_SCHEMA = "sys";
    
    /**
     * New instance of select admin executor for MySQL.
     *
     * @param selectStatementContext select statement context
     * @param sql SQL
     * @param parameters SQL parameters
     * @param databaseName database name
     * @param metaData meta data
     * @return created instance
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatementContext selectStatementContext, final String sql, final List<Object> parameters,
                                                              final String databaseName, final ShardingSphereMetaData metaData) {
        if (!selectStatementContext.getSqlStatement().getFrom().isPresent()) {
            return MySQLSelectWithoutFromAdminExecutorFactory.newInstance(selectStatementContext, sql, databaseName, metaData);
        }
        if (INFORMATION_SCHEMA.equalsIgnoreCase(databaseName) && !metaData.getDatabase(databaseName).isComplete()) {
            return MySQLInformationSchemaExecutorFactory.newInstance(selectStatementContext, sql, parameters);
        }
        if (PERFORMANCE_SCHEMA.equalsIgnoreCase(databaseName) && !metaData.getDatabase(databaseName).isComplete()) {
            return MySQLSystemSchemaExecutorFactory.newInstance(selectStatementContext, sql, parameters, "performance_schema");
        }
        if (MYSQL_SCHEMA.equalsIgnoreCase(databaseName) && !metaData.getDatabase(databaseName).isComplete()) {
            return MySQLSystemSchemaExecutorFactory.newInstance(selectStatementContext, sql, parameters, "mysql");
        }
        if (SYS_SCHEMA.equalsIgnoreCase(databaseName) && !metaData.getDatabase(databaseName).isComplete()) {
            return MySQLSystemSchemaExecutorFactory.newInstance(selectStatementContext, sql, parameters, "sys");
        }
        return Optional.empty();
    }
}
