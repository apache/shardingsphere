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

package org.apache.shardingsphere.proxy.backend.mysql.handler.admin;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.infra.metadata.database.schema.builder.SystemSchemaBuilderRule;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetaDataExecutor.DefaultDatabaseMetaDataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.List;
import java.util.Optional;

/**
 * Construct the performance schema executor's factory.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class MySQLPerformanceSchemaExecutorFactory {
    
    /**
     * Create executor.
     *
     * @param sqlStatement SQL statement
     * @param sql SQL being executed
     * @param parameters parameters
     * @return executor
     */
    public static Optional<DatabaseAdminExecutor> newInstance(final SelectStatement sqlStatement, final String sql, final List<Object> parameters) {
        if (!(sqlStatement.getFrom() instanceof SimpleTableSegment)) {
            return Optional.empty();
        }
        String tableName = ((SimpleTableSegment) sqlStatement.getFrom()).getTableName().getIdentifier().getValue();
        if (SystemSchemaBuilderRule.MYSQL_PERFORMANCE_SCHEMA.getTables().contains(tableName.toLowerCase())) {
            return Optional.of(new DefaultDatabaseMetaDataExecutor(sql, parameters));
        }
        return Optional.empty();
    }
}
