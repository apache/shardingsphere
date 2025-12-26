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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.metadata.database.schema.manager.SystemSchemaManager;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.mysql.handler.admin.executor.select.SelectInformationSchemataExecutor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.List;
import java.util.Optional;

/**
 * MySQL information schema executor factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLInformationSchemaExecutorFactory {
    
    private static final String SCHEMATA_TABLE = "SCHEMATA";
    
    /**
     * Create executor.
     *
     * @param selectStatementContext select statement context
     * @param sql SQL being executed
     * @param parameters parameters
     * @return executor
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatementContext selectStatementContext, final String sql, final List<Object> parameters) {
        SelectStatement sqlStatement = selectStatementContext.getSqlStatement();
        if (!sqlStatement.getFrom().isPresent() || !(sqlStatement.getFrom().get() instanceof SimpleTableSegment)) {
            return Optional.empty();
        }
        String tableName = ((SimpleTableSegment) sqlStatement.getFrom().get()).getTableName().getIdentifier().getValue();
        if (SCHEMATA_TABLE.equalsIgnoreCase(tableName)) {
            return Optional.of(new SelectInformationSchemataExecutor(sqlStatement, sql, parameters));
        }
        if (SystemSchemaManager.isSystemTable("mysql", "information_schema", tableName)) {
            return Optional.of(new DatabaseMetaDataExecutor(sql, parameters));
        }
        return Optional.empty();
    }
}
