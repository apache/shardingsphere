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

package org.apache.shardingsphere.proxy.backend.handler.admin.postgresql;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.AbstractDatabaseMetadataExecutor.DefaultDatabaseMetadataExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.executor.SelectDatabaseExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.postgresql.executor.SelectTableExecutor;
import org.apache.shardingsphere.sql.parser.sql.common.extractor.TableExtractor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ResetParameterStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.SetStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.ShowStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Database admin executor creator for PostgreSQL.
 */
public final class PostgreSQLAdminExecutorCreator implements DatabaseAdminExecutorCreator {
    
    private static final String PG_TABLESPACE = "pg_tablespace";
    
    private static final String PG_DATABASE = "pg_database";
    
    private static final String PG_TRIGGER = "pg_trigger";
    
    private static final String PG_INHERITS = "pg_inherits";
    
    private static final String PG_CLASS = "pg_class";
    
    private static final String PG_PREFIX = "pg_";
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext<?> sqlStatementContext) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof ShowStatement) {
            return Optional.of(new PostgreSQLShowVariableExecutor((ShowStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    @Override
    public Optional<DatabaseAdminExecutor> create(final SQLStatementContext<?> sqlStatementContext, final String sql, final String databaseName) {
        SQLStatement sqlStatement = sqlStatementContext.getSqlStatement();
        if (sqlStatement instanceof SelectStatement) {
            Collection<String> selectedTableNames = getSelectedTableNames((SelectStatement) sqlStatement);
            if (selectedTableNames.contains(PG_DATABASE)) {
                return Optional.of(new SelectDatabaseExecutor((SelectStatement) sqlStatement, sql));
            }
            if (isQueryPgTable(selectedTableNames)) {
                return Optional.of(new SelectTableExecutor(sql));
            }
            for (String each : selectedTableNames) {
                if (each.startsWith(PG_PREFIX)) {
                    return Optional.of(new DefaultDatabaseMetadataExecutor(sql));
                }
            }
        }
        if (sqlStatement instanceof SetStatement) {
            return Optional.of(new PostgreSQLSetVariableAdminExecutor((SetStatement) sqlStatement));
        }
        if (sqlStatement instanceof ResetParameterStatement) {
            return Optional.of(new PostgreSQLResetVariableAdminExecutor((ResetParameterStatement) sqlStatement));
        }
        return Optional.empty();
    }
    
    private boolean isQueryPgTable(final Collection<String> selectedTableNames) {
        boolean isComplexQueryTable = selectedTableNames.contains(PG_CLASS) && selectedTableNames.contains(PG_TRIGGER) && selectedTableNames.contains(PG_INHERITS);
        return isComplexQueryTable || selectedTableNames.contains(PG_TABLESPACE);
    }
    
    private Collection<String> getSelectedTableNames(final SelectStatement sqlStatement) {
        TableExtractor extractor = new TableExtractor();
        extractor.extractTablesFromSelect(sqlStatement);
        List<TableSegment> extracted = new LinkedList<>(extractor.getTableContext());
        for (TableSegment each : extractor.getTableContext()) {
            if (each instanceof SubqueryTableSegment) {
                TableExtractor subExtractor = new TableExtractor();
                subExtractor.extractTablesFromSelect(((SubqueryTableSegment) each).getSubquery().getSelect());
                extracted.addAll(subExtractor.getTableContext());
            }
        }
        List<String> result = new ArrayList<>(extracted.size());
        for (TableSegment each : extracted) {
            if (each instanceof SimpleTableSegment) {
                result.add(((SimpleTableSegment) each).getTableName().getIdentifier().getValue());
            }
        }
        return result;
    }
    
    @Override
    public String getType() {
        return "PostgreSQL";
    }
}
