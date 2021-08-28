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

package org.apache.shardingsphere.proxy.backend.text.admin.mysql;

import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.mysql.executor.information.SelectSchemataExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

/**
 * Construct the information schema executor's factory.
 */
public final class MySQLInformationSchemaExecutorFactory {
    
    public static final String SCHEMATA = "schemata";
    
    /**
     * Create executor.
     *
     * @param sqlStatement SQL statement
     * @param sql SQL being executed
     * @return executor
     */
    public static DatabaseAdminQueryExecutor newInstance(final SelectStatement sqlStatement, final String sql) {
        String tableName = ((SimpleTableSegment) sqlStatement.getFrom()).getTableName().getIdentifier().getValue();
        if (SCHEMATA.equalsIgnoreCase(tableName)) {
            return new SelectSchemataExecutor(sqlStatement, sql);
        }
        throw new UnsupportedOperationException(String.format("unsupported table : `%s`", tableName));
    }
}
