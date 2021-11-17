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

package org.apache.shardingsphere.proxy.backend.text.admin.postgresql;

import org.apache.shardingsphere.proxy.backend.text.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutorFactory;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor.SelectDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.executor.SelectTableExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Admin executor factory for PostgreSQL.
 */
public final class PostgreSQLAdminExecutorFactory implements DatabaseAdminExecutorFactory {
    
    private static final String PG_TABLESPACE = "pg_tablespace";
    
    private static final String PG_DATABASE = "pg_database";
    
    private static final String PG_NAMESPACE = "pg_namespace";
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final SQLStatement sqlStatement) {
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> newInstance(final SQLStatement sqlStatement, final String sql) {
        if (sqlStatement instanceof SelectStatement) {
            Collection<String> selectedTableNames = getSelectedTableNames((SelectStatement) sqlStatement);
            if (selectedTableNames.contains(PG_DATABASE)) {
                return Optional.of(new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql));
            }
            if (selectedTableNames.contains(PG_TABLESPACE)) {
                return Optional.of(new SelectTableExecutor(sql));
            }
            if (selectedTableNames.contains(PG_NAMESPACE)) {
                return Optional.of(new DefaultDatabaseMetadataExecutor(sql));
            }
        }
        return Optional.empty();
    }
    
    private Collection<String> getSelectedTableNames(final SelectStatement sqlStatement) {
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(sqlStatement);
        return extractor.getTableContext().stream().filter(each -> each instanceof SimpleTableSegment)
                .map(each -> ((SimpleTableSegment) each).getTableName().getIdentifier().getValue()).collect(Collectors.toCollection(LinkedList::new));
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
